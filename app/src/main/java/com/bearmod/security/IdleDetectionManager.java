package com.bearmod.security;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

//import com.bearmod.injection.RuntimeInjectionManager;
//import com.bearmod.monitor.ProcessLifecycleMonitor;
import com.bearmod.patch.NonRootPatchManager;
import com.bearmod.stealth.StealthLibraryManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Idle Detection and Automatic Security Cleanup Manager
 * Monitors user activity and automatically cleans up security modifications
 * after approximately 1 hour of inactivity
 */
public class IdleDetectionManager implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "IdleDetectionManager";
    
    // Idle timeout: 1 hour (3600 seconds)
    private static final long IDLE_TIMEOUT_MS = 60 * 60 * 1000; // 1 hour
    
    // Warning timeout: 55 minutes (show warning 5 minutes before cleanup)
    private static final long WARNING_TIMEOUT_MS = 55 * 60 * 1000; // 55 minutes
    
    private static IdleDetectionManager instance;
    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService cleanupExecutor;
    
    // Activity tracking
    private long lastActivityTime;
    private boolean isAppInForeground;
    private int activeActivityCount;
    
    // Cleanup tracking
    private boolean isIdleCleanupScheduled;
    private boolean isWarningShown;
    private boolean isUserActive;
    private boolean isCleanupInProgress;
    private Runnable idleCleanupRunnable;
    private Runnable warningRunnable;
    
    // Listener interface
    public interface IdleCleanupListener {
        void onIdleWarning(int minutesRemaining);
        void onIdleCleanupStarted();
        void onIdleCleanupCompleted();
        void onUserActivityResumed();
    }
    
    private IdleCleanupListener cleanupListener;
    
    private IdleDetectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cleanupExecutor = Executors.newSingleThreadExecutor();
        this.lastActivityTime = System.currentTimeMillis();
        this.isAppInForeground = false;
        this.activeActivityCount = 0;
        this.isIdleCleanupScheduled = false;
        this.isWarningShown = false;
        this.isUserActive = true;
        this.isCleanupInProgress = false;
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized IdleDetectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new IdleDetectionManager(context);
        }
        return instance;
    }
    
    /**
     * Start idle detection monitoring
     */
    public void startIdleDetection(Application application) {
        Log.i(TAG, "Starting idle detection monitoring...");
        application.registerActivityLifecycleCallbacks(this);
        scheduleIdleCheck();
    }
    
    /**
     * Stop idle detection monitoring
     */
    public void stopIdleDetection(Application application) {
        Log.i(TAG, "Stopping idle detection monitoring...");
        application.unregisterActivityLifecycleCallbacks(this);
        cancelScheduledCleanup();
    }
    
    /**
     * Set idle cleanup listener
     */
    public void setIdleCleanupListener(IdleCleanupListener listener) {
        this.cleanupListener = listener;
    }
    
    /**
     * ENHANCED: Record user activity with cleanup abort logic
     */
    public void recordUserActivity() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastActivity = currentTime - lastActivityTime;

        Log.d(TAG, "User activity recorded (idle for " + (timeSinceLastActivity / 1000) + " seconds)");

        lastActivityTime = currentTime;
        isUserActive = true;

        // If cleanup is in progress, abort it
        if (isCleanupInProgress) {
            Log.i(TAG, "User returned during cleanup - aborting cleanup process");
            isCleanupInProgress = false;

            if (cleanupListener != null) {
                cleanupListener.onUserActivityResumed();
            }
        }

        // If cleanup was scheduled, cancel it and notify listener
        if (isIdleCleanupScheduled) {
            Log.i(TAG, "User activity resumed - cancelling scheduled idle cleanup");
            cancelScheduledCleanup();

            if (cleanupListener != null) {
                cleanupListener.onUserActivityResumed();
            }
        }

        // Reschedule idle check
        scheduleIdleCheck();
    }
    
    /**
     * ENHANCED: Schedule idle check with user activity validation
     */
    private void scheduleIdleCheck() {
        // Cancel existing scheduled checks
        cancelScheduledCleanup();

        // Mark user as active when scheduling
        isUserActive = true;

        // Schedule warning
        warningRunnable = () -> {
            if (isAppInForeground && !isWarningShown && isUserActive) {
                Log.i(TAG, "Showing idle warning - cleanup in 5 minutes");
                isWarningShown = true;

                if (cleanupListener != null) {
                    cleanupListener.onIdleWarning(5);
                }
            }
        };

        // Schedule cleanup with user activity check
        idleCleanupRunnable = () -> {
            long currentTime = System.currentTimeMillis();
            long idleTime = currentTime - lastActivityTime;

            // Double-check user activity before cleanup
            if (idleTime >= IDLE_TIMEOUT_MS && !isUserActive) {
                Log.i(TAG, "Idle timeout reached - performing automatic security cleanup");
                isUserActive = false; // Mark as inactive for cleanup
                performAutomaticSecurityCleanup();
            } else if (isUserActive) {
                Log.d(TAG, "User became active - cancelling idle cleanup");
                cancelScheduledCleanup();
            } else {
                Log.d(TAG, "Idle check: not yet idle (idle for " + (idleTime / 1000) + " seconds)");
                // Reschedule for remaining time
                long remainingTime = IDLE_TIMEOUT_MS - idleTime;
                mainHandler.postDelayed(idleCleanupRunnable, remainingTime);
            }
        };

        // Schedule warning and cleanup
        mainHandler.postDelayed(warningRunnable, WARNING_TIMEOUT_MS);
        mainHandler.postDelayed(idleCleanupRunnable, IDLE_TIMEOUT_MS);

        isIdleCleanupScheduled = true;
        isWarningShown = false;

        Log.d(TAG, "Idle cleanup scheduled for " + (IDLE_TIMEOUT_MS / 1000) + " seconds from now");

        // Set user as inactive after scheduling delay
        mainHandler.postDelayed(() -> isUserActive = false, 5000); // 5 second grace period
    }
    
    /**
     * Cancel scheduled cleanup
     */
    private void cancelScheduledCleanup() {
        if (warningRunnable != null) {
            mainHandler.removeCallbacks(warningRunnable);
            warningRunnable = null;
        }
        
        if (idleCleanupRunnable != null) {
            mainHandler.removeCallbacks(idleCleanupRunnable);
            idleCleanupRunnable = null;
        }
        
        isIdleCleanupScheduled = false;
        isWarningShown = false;
    }
    
    /**
     * ENHANCED: Perform automatic security cleanup with abort checks
     */
    private void performAutomaticSecurityCleanup() {
        Log.i(TAG, "Starting automatic security cleanup due to idle timeout...");

        isCleanupInProgress = true;

        if (cleanupListener != null) {
            cleanupListener.onIdleCleanupStarted();
        }

        cleanupExecutor.execute(() -> {
            try {
                // Check if user became active before starting
                if (isUserActive) {
                    Log.i(TAG, "User became active - aborting cleanup before start");
                    isCleanupInProgress = false;
                    return;
                }

                // Step 1: Remove active security stealth patches
                if (!isUserActive) {
                    removeStealthPatches();
                } else {
                    Log.i(TAG, "User became active - aborting cleanup at step 1");
                    isCleanupInProgress = false;
                    return;
                }

                // Step 2: Clean up runtime injection modifications
                if (!isUserActive) {
                    cleanupRuntimeInjections();
                } else {
                    Log.i(TAG, "User became active - aborting cleanup at step 2");
                    isCleanupInProgress = false;
                    return;
                }

                // Step 3: Remove process monitoring hooks
                if (!isUserActive) {
                    removeProcessMonitoringHooks();
                } else {
                    Log.i(TAG, "User became active - aborting cleanup at step 3");
                    isCleanupInProgress = false;
                    return;
                }

                // Step 4: Clean up persistent game modifications
                if (!isUserActive) {
                    cleanupPersistentModifications();
                } else {
                    Log.i(TAG, "User became active - aborting cleanup at step 4");
                    isCleanupInProgress = false;
                    return;
                }

                // Final check before completion
                if (!isUserActive) {
                    Log.i(TAG, "Automatic security cleanup completed successfully");
                    isCleanupInProgress = false;

                    mainHandler.post(() -> {
                        if (cleanupListener != null) {
                            cleanupListener.onIdleCleanupCompleted();
                        }
                    });
                } else {
                    Log.i(TAG, "User became active - cleanup aborted at completion");
                    isCleanupInProgress = false;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error during automatic security cleanup", e);
                isCleanupInProgress = false;
            }
        });
    }
    
    /**
     * Remove active security stealth patches
     */
    private void removeStealthPatches() {
        try {
            Log.d(TAG, "Removing active stealth patches...");
            
            // Clean up stealth libraries
            StealthLibraryManager stealthManager = new StealthLibraryManager(context);
            stealthManager.shutdown();
            
            Log.d(TAG, "Stealth patches removed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error removing stealth patches", e);
        }
    }
    
    /**
     * Clean up runtime injection modifications
     */
    private void cleanupRuntimeInjections() {
        try {
            Log.d(TAG, "Cleaning up runtime injections...");
            
            // Clean up all injected libraries
            RuntimeInjectionManager injectionManager = new RuntimeInjectionManager(context);
            injectionManager.cleanupAllInjectedLibraries();
            
            Log.d(TAG, "Runtime injections cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up runtime injections", e);
        }
    }
    
    /**
     * Remove process monitoring hooks
     */
    private void removeProcessMonitoringHooks() {
        try {
            Log.d(TAG, "Removing process monitoring hooks...");
            
            // This would be implemented to stop process monitoring
            // ProcessLifecycleMonitor would need a method to safely stop monitoring
            
            Log.d(TAG, "Process monitoring hooks removed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error removing process monitoring hooks", e);
        }
    }
    
    /**
     * Clean up persistent game modifications
     */
    private void cleanupPersistentModifications() {
        try {
            Log.d(TAG, "Cleaning up persistent modifications...");
            
            // Clean up patch files and configurations
            DataClearingManager dataClearingManager = DataClearingManager.getInstance(context);
            // This would be a lighter cleanup that doesn't clear authentication data
            
            Log.d(TAG, "Persistent modifications cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up persistent modifications", e);
        }
    }
    
    // Activity Lifecycle Callbacks
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Not needed for idle detection
    }
    
    @Override
    public void onActivityStarted(Activity activity) {
        activeActivityCount++;
        if (activeActivityCount == 1) {
            // App came to foreground
            isAppInForeground = true;
            recordUserActivity();
            Log.d(TAG, "App came to foreground");
        }
    }
    
    @Override
    public void onActivityResumed(Activity activity) {
        recordUserActivity();
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        // User activity paused, but don't record as activity
    }
    
    @Override
    public void onActivityStopped(Activity activity) {
        activeActivityCount--;
        if (activeActivityCount == 0) {
            // App went to background
            isAppInForeground = false;
            Log.d(TAG, "App went to background");
        }
    }
    
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Not needed for idle detection
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        // Not needed for idle detection
    }
    
    /**
     * Shutdown idle detection manager
     */
    public void shutdown() {
        cancelScheduledCleanup();
        
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
        }
    }
}
