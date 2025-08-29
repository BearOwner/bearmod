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
 * Uses embedded libraries instead of OTA downloads for maximum reliability
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

    // Using direct native injection only

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

    /** Initialize the hybrid injection system */
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

            // Load all native libraries
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

    /** Perform direct native injection */
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

    /** Stop active injection */
    public void stopInjection() {
        if (injectionActive.get()) {
            Log.i(TAG, "Stopping active injection");
            nativeStopDirectInjection();
            injectionActive.set(false);
            currentCallback = null;
        }
    }

    public boolean isInjectionActive() { return injectionActive.get(); }
    public boolean isInitialized() { return initialized.get(); }

    /** Check if injection system is ready */
    public boolean isInjectionReady() {
        return initialized.get() && isNativeLibraryLoaded();
    }

    /** Get injection status */
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

    private boolean isNativeLibraryLoaded() {
        return nativeLibraryManager.isBearmodLibraryLoaded() && nativeLibraryManager.isMundoLibraryLoaded();
    }

    // Native method declarations (implemented in bearmod_jni.cpp)
    private native boolean nativeInjectDirect(String targetPackage, String keyAuthToken);
    private native void nativeStopDirectInjection();

    static {
        Log.d(TAG, "HybridInjectionManager class loaded - native library loading handled by NativeLibraryManager");
    }
}
