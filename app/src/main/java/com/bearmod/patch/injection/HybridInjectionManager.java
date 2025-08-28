package com.bearmod.injection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.bearmod.patch.model.PatchResult;
import com.bearmod.auth.SimpleLicenseVerifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    
    private final ExecutorService executor;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean injectionActive = new AtomicBoolean(false);

    private Context context;
    private InjectionCallback currentCallback;

    // Shared native library manager
    private final NativeLibraryManager nativeLibraryManager;
    
    // Integration with existing managers (simplified for embedded approach)
    // Removed KeyAuthInjectionManager - using direct native injection only
    
    private HybridInjectionManager() {
        this.executor = Executors.newSingleThreadExecutor();
        this.nativeLibraryManager = NativeLibraryManager.getInstance();
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
         * Adapter to bridge with existing KeyAuthInjectionManager.InjectionCallback
         */
        private record KeyAuthCallbackAdapter(InjectionCallback hybridCallback,
                                              String targetPackage) implements KeyAuthInjectionManager.InjectionCallback {

        @Override
            public void onInjectionStarted() {
                hybridCallback.onInjectionStarted(targetPackage);
            }

            @Override
            public void onInjectionProgress(int progress, String message) {
                hybridCallback.onInjectionProgress(targetPackage, progress, message);
            }

            @Override
            public void onInjectionSuccess(PatchResult result) {
                hybridCallback.onInjectionSuccess(targetPackage, result);
            }

            @Override
            public void onInjectionFailed(String error) {
                hybridCallback.onInjectionFailed(targetPackage, error);
            }
        }
    
    /**
     * Initialize the hybrid injection system
     */
    public boolean initialize(Context context) {
        if (initialized.get()) {
            Log.d(TAG, "Already initialized");
            return true;
        }
        
        this.context = context.getApplicationContext();
        
        try {
            Log.d(TAG, "Initializing HybridInjectionManager...");

            // Check authentication status with pure Java authentication system
            AuthenticationManager authManager = AuthenticationManager.getInstance(context);
            if (!authManager.isAuthenticated()) {
                Log.e(TAG, "Authentication required before injection initialization");
                return false;
            }

            // FIXED: Load all native libraries using enhanced manager with build-type detection
            if (!nativeLibraryManager.loadAllLibraries(context)) {
                Log.e(TAG, "Failed to load native libraries (bearmod + mundo)");
                return false;
            }

            // Verify both libraries are functional
            if (!nativeLibraryManager.isBearmodLibraryLoaded() || !nativeLibraryManager.isMundoLibraryLoaded()) {
                Log.e(TAG, "Native libraries not functional - Bearmod: " +
                    nativeLibraryManager.isBearmodLibraryLoaded() + ", Mundo: " +
                    nativeLibraryManager.isMundoLibraryLoaded());
                return false;
            }

            initialized.set(true);
            Log.d(TAG, "HybridInjectionManager initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize HybridInjectionManager", e);
            return false;
        }
    }
    
    /**
     * Perform direct native injection
     */
    public void performDirectInjection(String targetPackage, String keyAuthToken, InjectionCallback callback) {
        if (!initialized.get()) {
            callback.onInjectionFailed(targetPackage, "HybridInjectionManager not initialized");
            return;
        }
        
        // Use injection coordinator to prevent concurrent injections
        if (!InjectionCoordinator.startInjection("HybridInjectionManager", targetPackage)) {
            callback.onInjectionFailed(targetPackage, "Another injection is already active: " +
                InjectionCoordinator.getInjectionStatus());
            return;
        }
        
        currentCallback = callback;
        injectionActive.set(true);
        
        executor.execute(() -> {
            try {
                callback.onInjectionStarted(targetPackage);
                callback.onInjectionProgress(targetPackage, 10, "Starting direct injection");
                
                Log.i(TAG, "Starting direct injection for: " + targetPackage);
                
                // Call native injection method
                boolean result = nativeInjectDirect(targetPackage, keyAuthToken);
                
                if (result) {
                    callback.onInjectionProgress(targetPackage, 100, "Direct injection completed");
                    PatchResult patchResult = new PatchResult(true, 
                        "Direct injection successful", 
                        "direct-injection", 
                        targetPackage);
                    callback.onInjectionSuccess(targetPackage, patchResult);
                    Log.i(TAG, "Direct injection successful for: " + targetPackage);
                } else {
                    callback.onInjectionFailed(targetPackage, "Native direct injection failed");
                    Log.e(TAG, "Direct injection failed for: " + targetPackage);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error during direct injection", e);
                callback.onInjectionFailed(targetPackage, "Direct injection error: " + e.getMessage());
            } finally {
                injectionActive.set(false);
                currentCallback = null;
                InjectionCoordinator.stopInjection("HybridInjectionManager");
            }
        });
    }
    
    /**
     * Perform hybrid injection with KeyAuth fallback
     * Tries direct injection first, falls back to KeyAuth if needed
     */
    public void performHybridInjection(String targetPackage, String keyAuthToken, String patchId, InjectionCallback callback) {
        if (!initialized.get()) {
            callback.onInjectionFailed(targetPackage, "HybridInjectionManager not initialized");
            return;
        }

        if (injectionActive.get()) {
            callback.onInjectionFailed(targetPackage, "Another injection is already active");
            return;
        }

        currentCallback = callback;
        injectionActive.set(true);

        executor.execute(() -> {
            try {
                callback.onInjectionStarted(targetPackage);
                callback.onInjectionProgress(targetPackage, 5, "Starting hybrid injection");

                Log.i(TAG, "Starting hybrid injection for: " + targetPackage);

                // Try direct injection first
                callback.onInjectionProgress(targetPackage, 20, "Attempting direct injection");
                boolean directResult = nativeInjectDirect(targetPackage, keyAuthToken);

                if (directResult) {
                    callback.onInjectionProgress(targetPackage, 100, "Direct injection successful");
                    PatchResult patchResult = new PatchResult(true,
                        "Direct injection successful using integrated libraries",
                        patchId,
                        targetPackage);
                    callback.onInjectionSuccess(targetPackage, patchResult);
                    Log.i(TAG, "Direct injection successful for: " + targetPackage);
                } else {
                    // No KeyAuth fallback needed - use embedded approach only
                    Log.i(TAG, "Direct injection failed for: " + targetPackage);
                    callback.onInjectionFailed(targetPackage,
                        "Direct injection failed - embedded library approach unsuccessful");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error during hybrid injection", e);
                callback.onInjectionFailed(targetPackage, "Hybrid injection error: " + e.getMessage());
            } finally {
                if (currentCallback == callback) {
                    injectionActive.set(false);
                    currentCallback = null;
                }
            }
        });
    }

    // REMOVED: Frida script injection methods
    // All injection now uses ptrace-based native injection via nativeInjectDirect()
    // This eliminates Frida-gadget dependencies and improves stealth
    
    // REMOVED: Frida script loading from assets
    // All scripts are now loaded via SecureScriptManager and executed through ptrace injection
    
    /**
     * Stop active injection
     */
    public void stopInjection() {
        if (injectionActive.get()) {
            Log.i(TAG, "Stopping active injection");
            
            // Stop direct injection
            nativeStopDirectInjection();
            
            injectionActive.set(false);
            currentCallback = null;
        }
    }
    
    /**
     * Check if injection is currently active
     */
    public boolean isInjectionActive() {
        return injectionActive.get();
    }
    
    /**
     * Check if manager is initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Check if injection system is ready (compatible with KeyAuthInjectionManager interface)
     */
    public boolean isInjectionReady() {
        return initialized.get() && isNativeLibraryLoaded(); // FIXED: Check both libraries
    }

    /**
     * Get injection status (compatible with KeyAuthInjectionManager interface)
     */
    public String getInjectionStatus() {
        if (!initialized.get()) {
            return "Not initialized";
        } else if (injectionActive.get()) {
            return "Injection in progress";
        } else if (isNativeLibraryLoaded()) {
            return "Ready for injection";
        } else {
            return "Native libraries not loaded";
        }
    }
    
    // REMOVED: Direct asset script loading
    // Scripts are now managed by SecureScriptManager with KeyAuth OTA delivery
    
    /**
     * Check if native libraries are loaded (FIXED - now uses shared manager)
     */
    private boolean isNativeLibraryLoaded() {
        return nativeLibraryManager.isBearmodLibraryLoaded() && nativeLibraryManager.isMundoLibraryLoaded();
    }
    
    // Native method declarations (implemented in bearmod_jni.cpp)
    // Updated use AuthToken consistently and removed Frida methods
    private native boolean nativeInjectDirect(String targetPackage, String keyAuthToken);
    private native void nativeStopDirectInjection();
    // REMOVED: nativeInjectFridaScript() and nativeStopFridaInjection() - no longer needed

    // Static block removed - library loading now handled by NativeLibraryManager
    static {
        Log.d(TAG, "HybridInjectionManager class loaded - native library loading handled by NativeLibraryManager");
    }
}
