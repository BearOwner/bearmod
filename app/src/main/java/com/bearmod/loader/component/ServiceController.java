package com.bearmod.loader.component;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.bearmod.TargetAppManager;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.patch.StartupOrchestrator;

/**
 * ServiceController - Manages mod service lifecycle and operations
 *
 * Extracted from MainActivity.java to separate service management logic.
 * Handles service start/stop, polling, and state management.
 *
 * Migrated from com.bearmod.activity.MainActivity to com.bearmod.loader.component
 */
public class ServiceController {
    private static final String TAG = "ServiceController";

    private final Context context;
    private final TargetAppManager targetAppManager;

    // UI components
    private final TextView serviceStatus;
    private final Button startButton;
    private final Button stopButton;

    // Service state
    private boolean isServiceRunning = false;
    private String selectedTargetPackage;

    // Polling state
    private volatile boolean injectionReady = false;
    private volatile boolean injectionFailed = false;

    // Callback interface
    private ServiceCallback callback;

    public interface ServiceCallback {
        void showLoadingSpinner(String message);
        void updateLoadingMessage(String message);
        void hideLoadingSpinner();
        void showPackageInstallationGuidance(String packageName);
        boolean checkPermissions();
        void onServiceStarted();
        void onServiceStopped();
        void onServiceError(String error);
    }

    public ServiceController(Context context, TargetAppManager targetAppManager,
                           TextView serviceStatus, Button startButton, Button stopButton,
                           ServiceCallback callback) {
        this.context = context;
        this.targetAppManager = targetAppManager;
        this.serviceStatus = serviceStatus;
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.callback = callback;
    }

    /**
     * Start the mod service with comprehensive checks
     */
    public void startModService() {
        if (isServiceRunning) {
            Logx.d("Service already running");
            return;
        }

        try {
            Logx.d("Starting mod service...");

            // Check if a target package is selected
            if (selectedTargetPackage == null) {
                Toast.makeText(context, "Please select a target game version first", Toast.LENGTH_LONG).show();
                Logx.w("No target package selected");
                return;
            }

            // Check if the selected target app is installed
            if (!targetAppManager.isPackageInstalled(selectedTargetPackage)) {
                callback.showPackageInstallationGuidance(selectedTargetPackage);
                Logx.w("Selected target app not installed: " + selectedTargetPackage);
                return;
            }

            // Check permissions
            if (!callback.checkPermissions()) {
                return;
            }

            proceedWithServiceStart();

        } catch (Exception e) {
            Logx.e("Error starting mod service", e);
            Toast.makeText(context, "Error starting service: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Proceed with starting the service after all checks pass
     */
    private void proceedWithServiceStart() {
        try {
            // 1) Launch target game immediately
            boolean launched = selectedTargetPackage != null &&
                             targetAppManager.launchTargetPackage(selectedTargetPackage);
            Logx.d("Requested launch of target app: " + launched + " (" + selectedTargetPackage + ")");

            // 2) Show loading spinner while we prepare in background
            callback.showLoadingSpinner("Loading...");
            if (startButton != null) startButton.setEnabled(false);

            // 3) Kick off automatic injection/patch workflow (behind the scenes)
            injectionReady = false;
            injectionFailed = false;
            StartupOrchestrator.startAsync(context, selectedTargetPackage, new StartupOrchestrator.Callback() {
                @Override public void onProgress(int percent, String message) {
                    callback.updateLoadingMessage(message);
                }
                @Override public void onSuccess() {
                    injectionReady = true;
                }
                @Override public void onFailure(String error) {
                    injectionFailed = true;
                    Logx.e("Injection failed: " + error);
                }
            });

            // 4) Poll until the target app is in foreground AND injections are ready, then start floating service
            pollAndStartFloatingWhenReady();
        } catch (Exception e) {
            Logx.e("proceedWithServiceStart failed", e);
            callback.hideLoadingSpinner();
            if (startButton != null) startButton.setEnabled(true);
            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Poll the system until selected target package is in foreground, then start Floating service
     */
    private void pollAndStartFloatingWhenReady() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long[] waited = {0};
        final long interval = 500; // ms
        final long timeout = 20000; // 20s

        Runnable check = new Runnable() {
            @Override public void run() {
                try {
                    if (injectionFailed) {
                        callback.hideLoadingSpinner();
                        if (startButton != null) startButton.setEnabled(true);
                        Toast.makeText(context, "Preparation failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedTargetPackage != null && isTargetInForeground(selectedTargetPackage) && injectionReady) {
                        // Start overlay service only now
                        try {
                            // Use new modular service approach
                            com.bearmod.loader.floating.FloatService.startService(context);
                            isServiceRunning = true;
                            updateServiceStatus();
                            Toast.makeText(context, "Service started", Toast.LENGTH_SHORT).show();
                            callback.onServiceStarted();
                        } catch (Exception se) {
                            Logx.e("Failed to start modular floating service", se);
                        }
                        callback.hideLoadingSpinner();
                        if (stopButton != null) stopButton.setEnabled(true);
                        return;
                    }

                    // Continue polling until timeout
                    waited[0] += interval;
                    if (waited[0] < timeout) {
                        handler.postDelayed(this, interval);
                    } else {
                        Logx.w("Timeout waiting for target foreground/injection ready");
                        callback.hideLoadingSpinner();
                        if (startButton != null) startButton.setEnabled(true);
                        Toast.makeText(context, "Unable to start safely", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Logx.e("Polling error", e);
                    callback.hideLoadingSpinner();
                    if (startButton != null) startButton.setEnabled(true);
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
        } catch (Exception ignored) { }
        return false;
    }

    /**
     * Stop the floating mod service and update UI state
     */
    public void stopModService() {
        try {
            // Use new modular service approach
            com.bearmod.loader.floating.FloatService.stopService(context);
            isServiceRunning = false;
            updateServiceStatus();
            if (startButton != null) startButton.setEnabled(true);
            if (stopButton != null) stopButton.setEnabled(false);
            Toast.makeText(context, "Service stopped", Toast.LENGTH_SHORT).show();
            callback.onServiceStopped();
        } catch (Exception e) {
            Logx.e("stopModService failed", e);
        }
    }

    /**
     * Update UI to reflect current service running state
     */
    public void updateServiceStatus() {
        updateServiceStatus(isServiceRunning);
    }

    /**
     * Update UI to reflect specified service running state
     */
    public void updateServiceStatus(boolean running) {
        try {
            if (serviceStatus != null) {
                serviceStatus.setText(running ? "Service: Running" : "Service: Stopped");
            }
            if (startButton != null) startButton.setEnabled(!running);
            if (stopButton != null) stopButton.setEnabled(running);
        } catch (Exception e) {
            Logx.w("updateServiceStatus error", e);
        }
    }

    /**
     * Set selected target package
     */
    public void setSelectedTargetPackage(String packageName) {
        this.selectedTargetPackage = packageName;
    }

    /**
     * Get selected target package
     */
    public String getSelectedTargetPackage() {
        return selectedTargetPackage;
    }

    /**
     * Check if service is running
     */
    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * Set service callback
     */
    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    // ===== STUB METHODS FOR COMPILATION =====
    // These are temporary implementations to fix compilation errors
    // TODO: Implement proper timer and cleanup logic

    /**
     * Start license timer (stub implementation)
     */
    public void startTimer() {
        Logx.d("License timer started (stub)");
        // TODO: Implement actual timer logic
    }

    /**
     * Stop license timer (stub implementation)
     */
    public void stopTimer() {
        Logx.d("License timer stopped (stub)");
        // TODO: Implement actual timer logic
    }

    /**
     * Cleanup service resources (stub implementation)
     */
    public void cleanup() {
        Logx.d("Service cleanup (stub)");
        // TODO: Implement actual cleanup logic
    }
}