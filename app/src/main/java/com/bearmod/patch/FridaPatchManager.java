package com.bearmod.patch; 

import android.content.Context;
import android.util.Log;

import com.bearmod.security.SignatureVerifier; 
import com.bearmod.patch.model.PatchResult; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Manages Frida-based patching operations
 */
public class FridaPatchManager {
    private static final String TAG = "FridaPatchManager";
    
    private static FridaPatchManager instance;
    private final Executor executor;
    
    private FridaPatchManager() {
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static FridaPatchManager getInstance() {
        if (instance == null) {
            instance = new FridaPatchManager();
        }
        return instance;
    }
    
    public interface PatchCallback {
        void onPatchSuccess(PatchResult result);
        void onPatchFailed(String error);
        void onPatchProgress(int progress);
    }
    
    /**
     * Apply Frida patch to target app
     */
    public void applyPatch(Context context, String targetPackage, String patchId, PatchCallback callback) {
        executor.execute(() -> {
            try {
                // Verify target app is installed
                if (!isPackageInstalled(context, targetPackage)) {
                    callback.onPatchFailed("Target app not installed: " + targetPackage);
                    return;
                }
                
                // Load Frida script using secure manager
                String scriptPath = loadFridaScript(context, patchId);
                if (scriptPath == null) {
                    callback.onPatchFailed("Failed to load Frida script for patch: " + patchId);
                    return;
                }
                
                // Verify script signature
                if (!SignatureVerifier.verifyPatchSignature(context, scriptPath)) {
                    callback.onPatchFailed("Invalid patch signature");
                    return;
                }
                
                // Start Frida daemon if not running
                if (!startFridaDaemon()) {
                    callback.onPatchFailed("Failed to start Frida daemon");
                    return;
                }
                
                // Inject Frida script
                injectFridaScript(targetPackage, scriptPath, new InjectionCallback() {
                    @Override
                    public void onInjectionComplete(boolean success, String message) {
                        if (success) {
                            PatchResult result = new PatchResult(true, "Patch applied successfully", patchId);
                            callback.onPatchSuccess(result);
                        } else {
                            callback.onPatchFailed("Injection failed: " + message);
                        }
                    }
                    
                    @Override
                    public void onInjectionProgress(int progress) {
                        callback.onPatchProgress(progress);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error applying patch", e);
                callback.onPatchFailed("Patch failed: " + e.getMessage());
            }
        });
    }
    
    private boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String loadFridaScript(Context context, String patchId) {
        try {
            // Load script content using secure manager
            String scriptContent = SecureScriptManager.getInstance(context).loadScript(patchId);
            if (scriptContent == null) {
                Log.e(TAG, "Failed to load script content for: " + patchId);
                return null;
            }

            // Copy to cache directory
            File cacheDir = new File(context.getCacheDir(), "frida_scripts");
            cacheDir.mkdirs();

            File scriptFile = new File(cacheDir, patchId + ".js");
            FileOutputStream output = new FileOutputStream(scriptFile);
            output.write(scriptContent.getBytes());
            output.close();

            return scriptFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error loading Frida script", e);
            return null;
        }
    }
    
    private boolean startFridaDaemon() {
        try {
            // Check if daemon is already running
            if (isFridaDaemonRunning()) {
                return true;
            }
            
            // Start daemon
            Process process = Runtime.getRuntime().exec("frida --daemon");
            return process.waitFor() == 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting Frida daemon", e);
            return false;
        }
    }
    
    private boolean isFridaDaemonRunning() {
        try {
            Process process = Runtime.getRuntime().exec("frida-ps -U");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void injectFridaScript(String targetPackage, String scriptPath, InjectionCallback callback) {
        try {
            // Build injection command
            String command = String.format(
                "frida -U -l %s -f %s --no-pause",
                scriptPath,
                targetPackage
            );
            
            // Start injection process
            Process process = Runtime.getRuntime().exec(command);
            
            // Monitor injection progress
            new Thread(() -> {
                try {
                    int progress = 0;
                    while (progress < 100) {
                        Thread.sleep(100);
                        progress += 10;
                        callback.onInjectionProgress(progress);
                    }
                    
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        callback.onInjectionComplete(true, "Injection successful");
                    } else {
                        callback.onInjectionComplete(false, "Injection failed with code: " + exitCode);
                    }
                    
                } catch (Exception e) {
                    callback.onInjectionComplete(false, "Injection error: " + e.getMessage());
                }
            }).start();
            
        } catch (Exception e) {
            callback.onInjectionComplete(false, "Injection error: " + e.getMessage());
        }
    }
    
    private interface InjectionCallback {
        void onInjectionComplete(boolean success, String message);
        void onInjectionProgress(int progress);
    }
}
