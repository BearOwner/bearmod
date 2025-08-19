package com.bearmod.patch;

import android.content.Context;
import android.util.Log;

import com.bearmod.patch.model.PatchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages automatic patch application
 */
public class AutoPatchManager {
    private static final String TAG = "AutoPatchManager";
    
    private static AutoPatchManager instance;
    private final FridaPatchManager fridaPatchManager;
    private final NonRootPatchManager nonRootPatchManager;
    private final JSEnginePatchManager jsEnginePatchManager;
    private final AutoPatchConfig config;
    private PatchMode currentMode = PatchMode.AUTO_DETECT;
    
    private AutoPatchManager(Context context) {
        this.fridaPatchManager = FridaPatchManager.getInstance();
        this.nonRootPatchManager = NonRootPatchManager.getInstance();
        this.jsEnginePatchManager = JSEnginePatchManager.getInstance();
        this.config = new AutoPatchConfig(context);

        // Initialize JS engine
        this.jsEnginePatchManager.initialize(context);
    }
    
    public static AutoPatchManager getInstance(Context context) {
        if (instance == null) {
            instance = new AutoPatchManager(context);
        }
        return instance;
    }
    
    public enum PatchMode {
        FRIDA_ROOT,      // Traditional Frida with root
        NON_ROOT,        // Non-root native patching
        JS_ENGINE,       // JavaScript engine patching
        AUTO_DETECT      // Automatically detect best method
    }

    public interface AutoPatchCallback {
        void onAllPatchesComplete(List<PatchResult> results);
        void onPatchProgress(String patchId, int progress);
        void onPatchComplete(PatchResult result);
    }

    /**
     * Interface for service lifecycle integration
     */
    public interface ServiceIntegrationCallback {
        void onPatchesCompleteStartService();
        void onPatchesFailedStopService();
    }

    /**
     * Apply patches and integrate with service lifecycle
     */
    public void applyPatchesWithServiceIntegration(Context context, String targetPackage,
                                                 ServiceIntegrationCallback serviceCallback) {
        applyAutoPatch(context, targetPackage, new AutoPatchCallback() {
            @Override
            public void onAllPatchesComplete(List<PatchResult> results) {
                // Check if all patches were successful
                boolean allSuccessful = results.stream().allMatch(PatchResult::isSuccess);

                if (allSuccessful) {
                    Log.d(TAG, "All patches successful, starting service");
                    serviceCallback.onPatchesCompleteStartService();
                } else {
                    Log.w(TAG, "Some patches failed, stopping service");
                    serviceCallback.onPatchesFailedStopService();
                }
            }

            @Override
            public void onPatchProgress(String patchId, int progress) {
                Log.d(TAG, "Patch progress: " + patchId + " - " + progress + "%");
            }

            @Override
            public void onPatchComplete(PatchResult result) {
                Log.d(TAG, "Patch complete: " + result.getPatchId() + " - " +
                      (result.isSuccess() ? "SUCCESS" : "FAILED"));
            }
        });
    }

    /**
     * Apply all patches for target package automatically
     */
    public void applyAutoPatch(Context context, String targetPackage, AutoPatchCallback callback) {
        if (!config.isAutoPatchEnabled()) {
            Log.d(TAG, "Auto-patch disabled, skipping");
            callback.onAllPatchesComplete(new ArrayList<>());
            return;
        }
        
        if (!config.isTargetSupported(targetPackage)) {
            Log.w(TAG, "Target package not supported for auto-patch: " + targetPackage);
            callback.onAllPatchesComplete(new ArrayList<>());
            return;
        }
        
        List<String> patches = config.getPatchesForTarget(targetPackage);
        if (patches.isEmpty()) {
            Log.d(TAG, "No patches to apply for target: " + targetPackage);
            callback.onAllPatchesComplete(new ArrayList<>());
            return;
        }
        
        Log.d(TAG, "Starting auto-patch for " + targetPackage + " with patches: " + patches);
        
        // Apply patches sequentially
        applyPatchesSequentially(context, targetPackage, patches, callback);
    }
    
    /**
     * Apply patches one by one
     */
    private void applyPatchesSequentially(Context context, String targetPackage, 
                                        List<String> patches, AutoPatchCallback callback) {
        List<PatchResult> results = new ArrayList<>();
        AtomicInteger currentIndex = new AtomicInteger(0);
        
        applyNextPatch(context, targetPackage, patches, currentIndex, results, callback);
    }
    
    /**
     * Apply next patch in sequence
     */
    private void applyNextPatch(Context context, String targetPackage, List<String> patches,
                               AtomicInteger currentIndex, List<PatchResult> results,
                               AutoPatchCallback callback) {
        
        int index = currentIndex.get();
        if (index >= patches.size()) {
            // All patches completed
            Log.d(TAG, "All patches completed for " + targetPackage);
            callback.onAllPatchesComplete(results);
            return;
        }
        
        String patchId = patches.get(index);
        Log.d(TAG, "Applying patch " + (index + 1) + "/" + patches.size() + ": " + patchId);

        // Determine which patch manager to use
        PatchMode mode = determinePatchMode();
        Log.d(TAG, "Using patch mode: " + mode);

        applyPatchWithMode(context, targetPackage, patchId, mode, new UnifiedPatchCallback() {
            @Override
            public void onPatchSuccess(PatchResult result) {
                Log.d(TAG, "Patch " + patchId + " applied successfully");
                results.add(result);
                callback.onPatchComplete(result);
                
                // Move to next patch
                currentIndex.incrementAndGet();
                applyNextPatch(context, targetPackage, patches, currentIndex, results, callback);
            }
            
            @Override
            public void onPatchFailed(String error) {
                Log.e(TAG, "Patch " + patchId + " failed: " + error);
                PatchResult result = new PatchResult(false, error, patchId, targetPackage);
                results.add(result);
                callback.onPatchComplete(result);
                
                // Continue with next patch even if one fails
                currentIndex.incrementAndGet();
                applyNextPatch(context, targetPackage, patches, currentIndex, results, callback);
            }
            
            @Override
            public void onPatchProgress(int progress) {
                callback.onPatchProgress(patchId, progress);
            }
        });
    }
    
    /**
     * Check if auto-patch should trigger for current mode
     */
    public boolean shouldTriggerAutoPatch(AutoPatchConfig.AutoPatchTrigger currentTrigger) {
        AutoPatchConfig.AutoPatchTrigger configuredTrigger = config.getAutoPatchTrigger();
        return configuredTrigger == currentTrigger;
    }
    
    /**
     * Get auto-patch configuration
     */
    public AutoPatchConfig getConfig() {
        return config;
    }
    
    /**
     * Apply patches asynchronously and return CompletableFuture
     */
    public CompletableFuture<List<PatchResult>> applyAutoPatchAsync(Context context, String targetPackage) {
        CompletableFuture<List<PatchResult>> future = new CompletableFuture<>();
        
        applyAutoPatch(context, targetPackage, new AutoPatchCallback() {
            @Override
            public void onAllPatchesComplete(List<PatchResult> results) {
                future.complete(results);
            }
            
            @Override
            public void onPatchProgress(String patchId, int progress) {
                // Progress updates can be logged or ignored for async version
                Log.d(TAG, "Async patch progress - " + patchId + ": " + progress + "%");
            }
            
            @Override
            public void onPatchComplete(PatchResult result) {
                Log.d(TAG, "Async patch complete: " + result);
            }
        });
        
        return future;
    }

    /**
     * Unified callback interface for all patch managers
     */
    private interface UnifiedPatchCallback {
        void onPatchSuccess(PatchResult result);
        void onPatchFailed(String error);
        void onPatchProgress(int progress);
    }

    /**
     * Determine the best patch mode to use
     */
    private PatchMode determinePatchMode() {
        if (currentMode != PatchMode.AUTO_DETECT) {
            return currentMode;
        }

        // Auto-detect best method
        if (isRootAvailable()) {
            Log.d(TAG, "Root detected, using Frida mode");
            return PatchMode.FRIDA_ROOT;
        } else if (isNativeLibraryAvailable()) {
            Log.d(TAG, "Native library available, using non-root mode");
            return PatchMode.NON_ROOT;
        } else {
            Log.d(TAG, "Falling back to JS engine mode");
            return PatchMode.JS_ENGINE;
        }
    }

    /**
     * Apply patch using specified mode
     */
    private void applyPatchWithMode(Context context, String targetPackage, String patchId,
                                   PatchMode mode, UnifiedPatchCallback callback) {
        switch (mode) {
            case FRIDA_ROOT:
                fridaPatchManager.applyPatch(context, targetPackage, patchId,
                    new FridaPatchManager.PatchCallback() {
                        @Override
                        public void onPatchSuccess(PatchResult result) {
                            callback.onPatchSuccess(result);
                        }

                        @Override
                        public void onPatchFailed(String error) {
                            callback.onPatchFailed(error);
                        }

                        @Override
                        public void onPatchProgress(int progress) {
                            callback.onPatchProgress(progress);
                        }
                    });
                break;

            case NON_ROOT:
                nonRootPatchManager.applyPatch(context, targetPackage, patchId,
                    new NonRootPatchManager.PatchCallback() {
                        @Override
                        public void onPatchSuccess(PatchResult result) {
                            callback.onPatchSuccess(result);
                        }

                        @Override
                        public void onPatchFailed(String error) {
                            callback.onPatchFailed(error);
                        }

                        @Override
                        public void onPatchProgress(int progress) {
                            callback.onPatchProgress(progress);
                        }
                    });
                break;

            case JS_ENGINE:
                jsEnginePatchManager.applyPatch(context, targetPackage, patchId,
                    new JSEnginePatchManager.PatchCallback() {
                        @Override
                        public void onPatchSuccess(PatchResult result) {
                            callback.onPatchSuccess(result);
                        }

                        @Override
                        public void onPatchFailed(String error) {
                            callback.onPatchFailed(error);
                        }

                        @Override
                        public void onPatchProgress(int progress) {
                            callback.onPatchProgress(progress);
                        }
                    });
                break;

            default:
                callback.onPatchFailed("Unknown patch mode: " + mode);
                break;
        }
    }

    /**
     * Check if root access is available
     */
    private boolean isRootAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("su -c 'echo test'");
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if native library is available
     */
    private boolean isNativeLibraryAvailable() {
        // Do NOT attempt to load the library here. Loading order is controlled by SplashActivity.
        // Probing by loading can cause crashes or violate init sequencing.
        Log.d(TAG, "Skipping native availability probe to avoid unintended loads");
        return false;
    }

    /**
     * Set patch mode manually
     */
    public void setPatchMode(PatchMode mode) {
        this.currentMode = mode;
        Log.d(TAG, "Patch mode set to: " + mode);
    }

    /**
     * Get current patch mode
     */
    public PatchMode getPatchMode() {
        return currentMode;
    }
}
