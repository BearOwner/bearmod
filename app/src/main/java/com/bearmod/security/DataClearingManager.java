package com.bearmod.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

//import com.bearmod.BearLoaderApplication;
//import com.bearmod.injection.RuntimeInjectionManager;
//import com.bearmod.monitor.ProcessLifecycleMonitor;
import com.bearmod.patch.NonRootPatchManager;
import com.bearmod.stealth.StealthLibraryManager;
import com.bearmod.auth.SimpleLicenseVerifier;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Comprehensive Data Clearing Manager
 * Handles secure cleanup of sensitive data including game modifications,
 * anti-cheat reports, authentication tokens, and runtime modifications
 */
public class DataClearingManager {
    private static final String TAG = "DataClearingManager";
    
    private static DataClearingManager instance;
    private final Context context;
    private final ExecutorService cleanupExecutor;
    private final Handler mainHandler;
    
    // Cleanup listener interface
    public interface CleanupListener {
        void onCleanupStarted();
        void onCleanupProgress(int percentage, String currentTask);
        void onCleanupCompleted(boolean success);
        void onCleanupFailed(String error);
    }
    
    private DataClearingManager(Context context) {
        this.context = context.getApplicationContext();
        this.cleanupExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized DataClearingManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataClearingManager(context);
        }
        return instance;
    }
    
    /**
     * Perform comprehensive data clearing
     */
    public void performComprehensiveCleanup(CleanupListener listener) {
        Log.i(TAG, "Starting data cleanup...");
        
        if (listener != null) {
            mainHandler.post(() -> listener.onCleanupStarted());
        }
        
        cleanupExecutor.execute(() -> {
            try {
                // Step 1: Clear game hack/modification files (20%)
                updateProgress(listener, 10, "Clearing game modification files...");
                clearGameModificationFiles();
                
                // Step 2: Clear Tencent anti-cheat reports (40%)
                updateProgress(listener, 30, "Clearing anti-cheat detection...");
                clearAntiCheatReports();
                
                // Step 3: Clear authentication tokens and session data (60%)
                updateProgress(listener, 50, "Clearing data...");
                clearAuthenticationData();
                
                // Step 4: Clear temporary injection files (80%)
                updateProgress(listener, 70, "Clearing runtime modifications...");
                clearRuntimeModifications();
                
                // Step 5: Clear stealth library cache (90%)
                updateProgress(listener, 85, "Clearing libraries...");
                clearStealthLibraries();
                
                // Step 6: Final cleanup and verification (100%)
                updateProgress(listener, 95, "Finalizing cleanup...");
                performFinalCleanup();
                
                updateProgress(listener, 100, "Cleanup completed successfully");
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onCleanupCompleted(true));
                }
                
                Log.i(TAG, "Comprehensive data cleanup successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during comprehensive cleanup", e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onCleanupFailed("Cleanup failed: " + e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Clear game hack/modification files and configurations
     */
    private void clearGameModificationFiles() {
        try {
            Log.d(TAG, "Clearing game modification files...");
            
            // Clear injected libraries through RuntimeInjectionManager
            RuntimeInjectionManager injectionManager = new RuntimeInjectionManager(context);
            injectionManager.cleanupAllInjectedLibraries();
            
            // Clear patch files and configurations
            File patchDir = new File(context.getFilesDir(), "patches");
            if (patchDir.exists()) {
                deleteDirectoryRecursively(patchDir);
            }
            
            // Clear mod configurations
            File modConfigDir = new File(context.getFilesDir(), "mod_configs");
            if (modConfigDir.exists()) {
                deleteDirectoryRecursively(modConfigDir);
            }
            
            // Clear downloaded enhancement files
            File enhancementDir = new File(context.getFilesDir(), "enhancements");
            if (enhancementDir.exists()) {
                deleteDirectoryRecursively(enhancementDir);
            }
            
            Log.d(TAG, "Game modification files cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing game modification files", e);
            throw e;
        }
    }
    
    /**
     * Clear Tencent anti-cheat reports and detection logs
     */
    private void clearAntiCheatReports() {
        try {
            Log.d(TAG, "Clearing anti-cheat logs...");
            
            // Clear anti-cheat detection logs
            File antiCheatDir = new File(context.getFilesDir(), "anticheat_logs");
            if (antiCheatDir.exists()) {
                deleteDirectoryRecursively(antiCheatDir);
            }
            
            // Clear Tencent detection reports
            File tencentReportsDir = new File(context.getFilesDir(), "tencent_reports");
            if (tencentReportsDir.exists()) {
                deleteDirectoryRecursively(tencentReportsDir);
            }
            
            // Clear detection cache
            File detectionCacheDir = new File(context.getCacheDir(), "detection_cache");
            if (detectionCacheDir.exists()) {
                deleteDirectoryRecursively(detectionCacheDir);
            }
            
            // Clear security scan results
            SharedPreferences securityPrefs = context.getSharedPreferences("security_scan_results", Context.MODE_PRIVATE);
            securityPrefs.edit().clear().apply();
            
            Log.d(TAG, "Anti-cheat cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing anti-cheat", e);
            throw e;
        }
    }
    
    /**
     * Clear cached authentication tokens and session data
     */
    private void clearAuthenticationData() {
        try {
            Log.d(TAG, "Clearing data...");
            
            // Clear KeyAuth session data
            SimpleLicenseVerifier.clearStoredAuth(context);
            
            // Clear cached tokens
            File tokenDir = new File(context.getFilesDir(), "tokens");
            if (tokenDir.exists()) {
                deleteDirectoryRecursively(tokenDir);
            }
            
            // Clear session cache
            File sessionCacheDir = new File(context.getCacheDir(), "sessions");
            if (sessionCacheDir.exists()) {
                deleteDirectoryRecursively(sessionCacheDir);
            }
            
            // Clear authentication preferences
            SharedPreferences authPrefs = context.getSharedPreferences("auth_cache", Context.MODE_PRIVATE);
            authPrefs.edit().clear().apply();
            
            Log.d(TAG, "Authentication cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing data", e);
            throw e;
        }
    }
    
    /**
     * Clear temporary injection files and runtime modifications
     */
    private void clearRuntimeModifications() {
        try {
            Log.d(TAG, "Clearing runtime modifications...");
            
            // Clear temporary injection files
            File tempInjectionDir = new File(context.getCacheDir(), "temp_injection");
            if (tempInjectionDir.exists()) {
                deleteDirectoryRecursively(tempInjectionDir);
            }
            
            // Clear runtime library cache
            File runtimeLibDir = new File(context.getFilesDir(), "runtime_libs");
            if (runtimeLibDir.exists()) {
                deleteDirectoryRecursively(runtimeLibDir);
            }
            
            // Clear process monitoring data
            SharedPreferences processPrefs = context.getSharedPreferences("process_monitoring", Context.MODE_PRIVATE);
            processPrefs.edit().clear().apply();
            
            Log.d(TAG, "Runtime modifications cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing runtime modifications", e);
            throw e;
        }
    }
    
    /**
     * Clear stealth libraries and randomized signatures
     */
    private void clearStealthLibraries() {
        try {
            Log.d(TAG, "Clearing stealth libraries...");
            
            // Clear stealth library directory
            File stealthDir = new File(context.getFilesDir(), "stealth_libs");
            if (stealthDir.exists()) {
                deleteDirectoryRecursively(stealthDir);
            }
            
            // Clear randomized library cache
            File randomizedLibDir = new File(context.getCacheDir(), "randomized_libs");
            if (randomizedLibDir.exists()) {
                deleteDirectoryRecursively(randomizedLibDir);
            }
            
            // Clear stealth configuration
            SharedPreferences stealthPrefs = context.getSharedPreferences("stealth_config", Context.MODE_PRIVATE);
            stealthPrefs.edit().clear().apply();
            
            Log.d(TAG, "Stealth libraries cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing stealth libraries", e);
            throw e;
        }
    }
    
    /**
     * Perform final cleanup and verification
     */
    private void performFinalCleanup() {
        try {
            Log.d(TAG, "Performing final cleanup...");
            
            // Clear general cache
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) {
                File[] cacheFiles = cacheDir.listFiles();
                if (cacheFiles != null) {
                    for (File file : cacheFiles) {
                        if (file.getName().contains("bearmod") || 
                            file.getName().contains("injection") ||
                            file.getName().contains("patch")) {
                            deleteDirectoryRecursively(file);
                        }
                    }
                }
            }
            
            // Clear temporary files
            File tempDir = new File(context.getFilesDir(), "temp");
            if (tempDir.exists()) {
                deleteDirectoryRecursively(tempDir);
            }
            
            Log.d(TAG, "Final cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during final cleanup", e);
            throw e;
        }
    }
    
    /**
     * Recursively delete directory and all contents
     */
    private void deleteDirectoryRecursively(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        
        boolean deleted = directory.delete();
        if (deleted) {
            Log.d(TAG, "Deleted: " + directory.getAbsolutePath());
        } else {
            Log.w(TAG, "Failed to delete: " + directory.getAbsolutePath());
        }
    }
    
    /**
     * Update cleanup progress
     */
    private void updateProgress(CleanupListener listener, int percentage, String task) {
        if (listener != null) {
            mainHandler.post(() -> listener.onCleanupProgress(percentage, task));
        }
        Log.d(TAG, "Cleanup progress: " + percentage + "% - " + task);
    }
    
    /**
     * Shutdown cleanup executor
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
        }
    }
}
