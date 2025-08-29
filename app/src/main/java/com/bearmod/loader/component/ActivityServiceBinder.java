package com.bearmod.loader.component;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.bearmod.TargetAppManager;
import com.bearmod.patch.StartupOrchestrator;
import com.bearmod.loader.utilities.Logx;

/**
 * ActivityServiceBinder - Manages Floating service lifecycle and binding
 * Extracted from MainActivity to separate service management concerns
 *
 * Responsibilities:
 * - Service start/stop operations
 * - Service binding and unbinding
 * - Target app launching and monitoring
 * - Injection workflow coordination
 * - Service state tracking
 */
public class ActivityServiceBinder {

    private static final String TAG = "ActivityServiceBinder";

    private final Context context;
    private final TargetAppManager targetAppManager;
    private ServiceCallback callback;

    private boolean isServiceRunning = false;
    private boolean injectionReady = false;
    private boolean injectionFailed = false;

    // Service connection for binding (if needed)
    private ServiceConnection serviceConnection;

    /**
     * Interface for service events
     */
    public interface ServiceCallback {
        void onServiceStarted();
        void onServiceStopped();
        void onServiceError(String error);
        void onInjectionReady();
        void onInjectionFailed(String error);
        void onTargetAppLaunched(boolean success);
    }

    public ActivityServiceBinder(Context context, TargetAppManager targetAppManager) {
        this.context = context;
        this.targetAppManager = targetAppManager;
    }

    /**
     * Set service callback for events
     */
    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    /**
     * Start the mod service with comprehensive checks
     */
    public void startModService(String selectedTargetPackage) {
        if (isServiceRunning) {
            Logx.d("Service already running");
            return;
        }

        try {
            Logx.d("Starting mod service...");

            // Validate target package
            if (selectedTargetPackage == null) {
                notifyError("Please select a target game version first");
                return;
            }

            // Check if target app is installed
            if (!targetAppManager.isPackageInstalled(selectedTargetPackage)) {
                notifyError("Selected target app not installed: " + selectedTargetPackage);
                return;
            }

            // Proceed with service start
            proceedWithServiceStart(selectedTargetPackage);

        } catch (Exception e) {
            Logx.e("Error starting mod service", e);
            notifyError("Error starting service: " + e.getMessage());
        }
    }

    /**
     * Proceed with service start after validation
     */
    private void proceedWithServiceStart(String selectedTargetPackage) {
        try {
            // 1) Launch target game immediately
            boolean launched = selectedTargetPackage != null &&
                targetAppManager.launchTargetPackage(selectedTargetPackage);
            Logx.d("Requested launch of target app: " + launched + " (" + selectedTargetPackage + ")");

            if (callback != null) {
                callback.onTargetAppLaunched(launched);
            }

            // 2) Show loading spinner while we prepare in background
            // (This would be handled by UIController)

            // 3) Kick off automatic injection/patch workflow
            injectionReady = false;
            injectionFailed = false;

            StartupOrchestrator.startAsync(context, selectedTargetPackage, new StartupOrchestrator.Callback() {
                @Override
                public void onProgress(int percent, String message) {
                    // Progress updates handled by UI
                    Logx.d("Injection progress: " + percent + "% - " + message);
                }

                @Override
                public void onSuccess() {
                    injectionReady = true;
                    Logx.d("Injection preparation completed successfully");
                    if (callback != null) {
                        callback.onInjectionReady();
                    }
                }

                @Override
                public void onFailure(String error) {
                    injectionFailed = true;
                    Logx.e("Injection failed: " + error);
                    if (callback != null) {
                        callback.onInjectionFailed(error);
                    }
                }
            });

            // 4) Poll until the target app is in foreground AND injections are ready
            pollAndStartFloatingWhenReady(selectedTargetPackage);

        } catch (Exception e) {
            Logx.e("proceedWithServiceStart failed", e);
            notifyError("Failed to start service: " + e.getMessage());
        }
    }

    /**
     * Poll the system until selected target package is in foreground, then start Floating service
     */
    private void pollAndStartFloatingWhenReady(String selectedTargetPackage) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long[] waited = {0};
        final long interval = 500; // ms
        final long timeout = 20000; // 20s

        Runnable check = new Runnable() {
            @Override
            public void run() {
                try {
                    if (injectionFailed) {
                        Logx.w("Injection failed during polling");
                        notifyError("Preparation failed");
                        return;
                    }

                    if (selectedTargetPackage != null &&
                        isTargetInForeground(selectedTargetPackage) && injectionReady) {

                        // Start overlay service only now
                        try {
                            // Use new modular service approach
                            com.bearmod.loader.floating.FloatService.startService(context);
                            isServiceRunning = true;

                            Logx.d("Modular floating service started successfully");
                            if (callback != null) {
                                callback.onServiceStarted();
                            }

                        } catch (Exception se) {
                            Logx.e("Failed to start modular floating service", se);
                            notifyError("Failed to start overlay service: " + se.getMessage());
                        }
                        return;
                    }

                    // Continue polling until timeout
                    waited[0] += interval;
                    if (waited[0] < timeout) {
                        handler.postDelayed(this, interval);
                    } else {
                        Logx.w("Timeout waiting for target foreground/injection ready");
                        notifyError("Unable to start safely - timeout");
                    }

                } catch (Exception e) {
                    Logx.e("Polling error", e);
                    notifyError("Service startup error: " + e.getMessage());
                }
            }
        };
        handler.postDelayed(check, interval);
    }

    /**
     * Check if the target package is currently in foreground
     */
    private boolean isTargetInForeground(String pkg) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return false;

            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info != null && info.processName != null && info.processName.equals(pkg)) {
                    return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                }
            }
        } catch (Exception ignored) {
            Logx.w("Error checking foreground status", ignored);
        }
        return false;
    }

    /**
     * Stop the floating mod service
     */
    public void stopModService() {
        try {
            // Use new modular service approach
            com.bearmod.loader.floating.FloatService.stopService(context);
            isServiceRunning = false;

            Logx.d("Modular service stopped");
            if (callback != null) {
                callback.onServiceStopped();
            }

        } catch (Exception e) {
            Logx.e("stopModService failed", e);
            notifyError("Error stopping service: " + e.getMessage());
        }
    }

    /**
     * Check if service is currently running
     */
    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * Update service running state (called by external state changes)
     */
    public void setServiceRunning(boolean running) {
        this.isServiceRunning = running;
    }

    /**
     * Get injection ready state
     */
    public boolean isInjectionReady() {
        return injectionReady;
    }

    /**
     * Get injection failed state
     */
    public boolean isInjectionFailed() {
        return injectionFailed;
    }

    /**
     * Bind to service (for future use if needed)
     */
    public void bindToService() {
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Logx.d("Service bound: " + name);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logx.d("Service unbound: " + name);
                }
            };
        }

        // Use new modular service approach for binding
        com.bearmod.loader.floating.FloatService.bindService(context, serviceConnection);
    }

    /**
     * Unbind from service
     */
    public void unbindFromService() {
        if (serviceConnection != null) {
            try {
                context.unbindService(serviceConnection);
                serviceConnection = null;
                Logx.d("Service unbound");
            } catch (Exception e) {
                Logx.w("Error unbinding service", e);
            }
        }
    }

    /**
     * Notify error to callback
     */
    private void notifyError(String error) {
        Logx.e("Service error: " + error);
        if (callback != null) {
            callback.onServiceError(error);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        try {
            unbindFromService();
            callback = null;
            Logx.d("ActivityServiceBinder cleanup completed");
        } catch (Exception e) {
            Logx.w("Error during cleanup", e);
        }
    }
}