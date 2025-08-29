package com.bearmod.patch.injection;

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
        public void onInjectionSuccess(String targetPackage, PatchResult result) {
            // Default implementation - can be overridden
        }
        
        @Override
        public void onInjectionFailed(String targetPackage, String error) {
            // Default implementation - can be overridden
        }
        
        @Override
        public void onInjectionStopped(String targetPackage) {
            // Default implementation - can be overridden
        }
    }
}
