package com.bearmod.patch.injection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.bearmod.patch.model.PatchResult;
import com.bearmod.auth.SimpleLicenseVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hybrid Injection Manager - Main coordinator for all injection methods
 * Supports direct native injection only
 * Uses embedded libraries instead of OTA downloads for maximum reliability //later 
 */
public class HybridInjectionManager {
    private static final String TAG = "HybridInjectionManager";
    
    @SuppressLint("StaticFieldLeak")
    private static HybridInjectionManager instance;
    private static final Object lock = new Object();

    private HybridInjectionManager() {
    }
    
    public static HybridInjectionManager getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new HybridInjectionManager();
            }
            return instance;
        }
    }
    
    public interface InjectionCallback {
        void onInjectionStarted(String targetPackage);
        void onInjectionProgress(String targetPackage, int progress, String message);
        void onInjectionSuccess(String targetPackage, PatchResult result);
        void onInjectionFailed(String targetPackage, String error);
    }

    /**
     * Initialize the hybrid injection system
     */
    public boolean initialize(Context context) {
        Log.w(TAG, "Legacy HybridInjectionManager (patch) initialize called - no-op");
        return false;
    }
    
    /**
     * Perform direct native injection
     */
    public void performDirectInjection(String targetPackage, String keyAuthToken, InjectionCallback callback) {
        if (callback != null) {
            callback.onInjectionFailed(targetPackage, "Legacy HybridInjectionManager is deprecated - use com.bearmod.injection.HybridInjectionManager");
        }
    }
    
    /**
     * Perform hybrid injection with KeyAuth fallback
     * Tries direct injection first, falls back to KeyAuth if needed
     */
    public void performHybridInjection(String targetPackage, String keyAuthToken, String patchId, InjectionCallback callback) {
        if (callback != null) {
            callback.onInjectionFailed(targetPackage, "Hybrid injection not supported in legacy manager");
        }
    }

    // REMOVED: Frida script loading from assets
    // All scripts are now loaded via SecureScriptManager and executed through ptrace injection
    
    /**
     * Stop active injection
     */
    public void stopInjection() { /* no-op */ }
    
    /**
     * Check if injection is currently active
     */
    public boolean isInjectionActive() { return false; }
    
    /**
     * Check if manager is initialized
     */
    public boolean isInitialized() { return false; }

    /**
     * Check if injection system is ready (compatible with KeyAuthInjectionManager interface)
     */
    public boolean isInjectionReady() { return false; }

    /**
     * Get injection status (compatible with KeyAuthInjectionManager interface)
     */
    public String getInjectionStatus() { return "Legacy manager (disabled)"; }
    
    // REMOVED: Direct asset script loading
    // Scripts are now managed by SecureScriptManager with KeyAuth OTA delivery
    
    /**
     * Check if native libraries are loaded (FIXED - now uses shared manager)
     */
    private boolean isNativeLibraryLoaded() { return false; }
    
    // Native method declarations (implemented in bearmod_jni.cpp)
    // Updated use AuthToken consistently and removed Frida methods
    // Native methods are resolved in the delegate; keep none here
    // REMOVED: nativeInjectFridaScript() and nativeStopFridaInjection() - no longer needed

    // Static block removed - library loading now handled by NativeLibraryManager
    static {
        Log.d(TAG, "HybridInjectionManager class loaded - native library loading handled by NativeLibraryManager");
    }
}
