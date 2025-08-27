package com.bearmod.patch.injection;

import com.bearmod.injection.HybridInjectionManager;
import com.bearmod.injection.KeyAuthInjectionManager;
import com.bearmod.patch.model.PatchResult;

/** //latter
 * Standardized injection callback interface for all injection managers
 * Provides consistent callback methods across HybridInjectionManager, InjectionManager, 
 * Eliminates callback interface inconsistencies and improves integration
 */
public interface StandardInjectionCallback {
    
    /**
     * Called when injection process starts
     * @param targetPackage The package being injected
     */
    void onInjectionStarted(String targetPackage);
    
    /**
     * Called to report injection progress
     * @param targetPackage The package being injected
     * @param progress Progress percentage (0-100)
     * @param message Descriptive message about current progress
     */
    void onInjectionProgress(String targetPackage, int progress, String message);
    
    /**
     * Called when injection completes successfully
     * @param targetPackage The package that was injected
     * @param result Detailed result information about the injection
     */
    void onInjectionSuccess(String targetPackage, PatchResult result);
    
    /**
     * Called when injection fails
     * @param targetPackage The package that failed injection
     * @param error Error message describing the failure
     */
    void onInjectionFailed(String targetPackage, String error);
    
    /**
     * Called when injection is stopped (either completed or cancelled)
     * @param targetPackage The package for which injection was stopped
     */
    void onInjectionStopped(String targetPackage);
    
    /**
     * Adapter class for backward compatibility with existing callback interfaces
     * Allows gradual migration to the standardized interface
     */
    abstract class Adapter implements StandardInjectionCallback {
        
        @Override
        public void onInjectionStarted(String targetPackage) {
            // Default implementation - can be overridden
        }
        
        @Override
        public void onInjectionProgress(String targetPackage, int progress, String message) {
            // Default implementation - can be overridden
        }
        
        @Override
        public void onInjectionStopped(String targetPackage) {
            // Default implementation - can be overridden
        }
    }
    
    /**
     * Adapter for HybridInjectionManager.InjectionCallback
     */
    class HybridInjectionAdapter extends Adapter {
        private final HybridInjectionManager.InjectionCallback originalCallback;
        
        public HybridInjectionAdapter(HybridInjectionManager.InjectionCallback originalCallback) {
            this.originalCallback = originalCallback;
        }
        
        @Override
        public void onInjectionStarted(String targetPackage) {
            originalCallback.onInjectionStarted(targetPackage);
        }
        
        @Override
        public void onInjectionProgress(String targetPackage, int progress, String message) {
            originalCallback.onInjectionProgress(targetPackage, progress, message);
        }
        
        @Override
        public void onInjectionSuccess(String targetPackage, PatchResult result) {
            originalCallback.onInjectionSuccess(targetPackage, result);
        }
        
        @Override
        public void onInjectionFailed(String targetPackage, String error) {
            originalCallback.onInjectionFailed(targetPackage, error);
        }
        
        @Override
        public void onInjectionStopped(String targetPackage) {
            // HybridInjectionManager doesn't have onInjectionStopped, so we don't call anything
        }
    }
    
    /**
     * Adapter for InjectionManager.InjectionCallback
     */
    class InjectionManagerAdapter extends Adapter {
        private final InjectionManager.InjectionCallback originalCallback;
        
        public InjectionManagerAdapter(InjectionManager.InjectionCallback originalCallback) {
            this.originalCallback = originalCallback;
        }
        
        @Override
        public void onInjectionStarted(String targetPackage) {
            // InjectionManager doesn't have onInjectionStarted, so we don't call anything
        }
        
        @Override
        public void onInjectionProgress(String targetPackage, int progress, String message) {
            originalCallback.onInjectionProgress(targetPackage, progress);
        }
        
        @Override
        public void onInjectionSuccess(String targetPackage, PatchResult result) {
            // Convert PatchResult to library path (simplified)
            String libraryPath = result != null ? result.getPatchId() : "unknown";
            originalCallback.onInjectionSuccess(targetPackage, libraryPath);
        }
        
        @Override
        public void onInjectionFailed(String targetPackage, String error) {
            originalCallback.onInjectionFailed(targetPackage, error);
        }
        
        @Override
        public void onInjectionStopped(String targetPackage) {
            // InjectionManager doesn't have onInjectionStopped, so we don't call anything
        }
    }
    
    /**
     * Adapter for KeyAuthInjectionManager.InjectionCallback
     */
    class KeyAuthInjectionAdapter extends Adapter {
        private final KeyAuthInjectionManager.InjectionCallback originalCallback;
        
        public KeyAuthInjectionAdapter(KeyAuthInjectionManager.InjectionCallback originalCallback) {
            this.originalCallback = originalCallback;
        }
        
        @Override
        public void onInjectionStarted(String targetPackage) {
            originalCallback.onInjectionStarted();
        }
        
        @Override
        public void onInjectionProgress(String targetPackage, int progress, String message) {
            originalCallback.onInjectionProgress(progress, message);
        }
        
        @Override
        public void onInjectionSuccess(String targetPackage, PatchResult result) {
            originalCallback.onInjectionSuccess(result);
        }
        
        @Override
        public void onInjectionFailed(String targetPackage, String error) {
            originalCallback.onInjectionFailed(error);
        }
        
        @Override
        public void onInjectionStopped(String targetPackage) {
            // KeyAuthInjectionManager doesn't have onInjectionStopped, so we don't call anything
        }
    }
    
    /**
     * Utility methods for creating adapters
     */
    class Utils {
        
        /**
         * Create adapter for HybridInjectionManager callback
         */
        public static StandardInjectionCallback fromHybridCallback(HybridInjectionManager.InjectionCallback callback) {
            return new HybridInjectionAdapter(callback);
        }
        
        /**
         * Create adapter for InjectionManager callback
         */
        public static StandardInjectionCallback fromInjectionManagerCallback(InjectionManager.InjectionCallback callback) {
            return new InjectionManagerAdapter(callback);
        }
        
        /**
         * Create adapter for KeyAuthInjectionManager callback
         */
        public static StandardInjectionCallback fromKeyAuthCallback(KeyAuthInjectionManager.InjectionCallback callback) {
            return new KeyAuthInjectionAdapter(callback);
        }
    }
}
