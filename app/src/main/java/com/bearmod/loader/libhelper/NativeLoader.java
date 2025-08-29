package com.bearmod.loader.libhelper;

import android.content.Context;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.NativeUtils;

/**
 * NativeLoader - Handles native library loading and JNI initialization
 *
 * Extracted from SplashActivity.java to centralize native library management.
 * Provides clean interface for loading bearmod native library and JNI setup.
 *
 * Migrated from com.bearmod.activity.SplashActivity (449 LOC → focused native loader)
 */
public class NativeLoader {
    private static final String TAG = "NativeLoader";

    /**
     * Load the native bearmod library and perform JNI initialization
     */
    public static boolean loadNativeLibrary(Context context) {
        try {
            Logx.d("Loading native bearmod library");

            // Load the native library
            System.loadLibrary("bearmod");
            Logx.d("Native library loaded successfully");
            NativeUtils.setNativeLoaded(true);

            // Perform JNI initialization
            return initializeJNI(context);

        } catch (UnsatisfiedLinkError e) {
            Logx.w("Native library load failed: " + e.getMessage());
            NativeUtils.setNativeLoaded(false);
            return false;
        } catch (Exception e) {
            Logx.e("Unexpected error loading native library", e);
            NativeUtils.setNativeLoaded(false);
            return false;
        }
    }

    /**
     * Initialize JNI bridge and register natives
     */
    private static boolean initializeJNI(Context context) {
        try {
            Logx.d("Initializing JNI bridge");

            // Initialize NativeLib with application context
            com.bearmod.bridge.NativeLib.initialize(context.getApplicationContext());
            Logx.d("NativeLib initialized successfully");

            // Register native methods for all classes that need them
            registerNativeMethods();
            Logx.d("JNI registration completed successfully");

            return true;

        } catch (Throwable e) {
            Logx.w("JNI initialization failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Register native methods for all required classes
     */
    private static void registerNativeMethods() {
        try {
            // Register natives for Floating service
            com.bearmod.bridge.NativeLib.registerNatives(
                com.bearmod.Floating.class,
                com.bearmod.activity.LoginActivity.class,
                com.bearmod.bridge.NativeLib.class
            );
            Logx.d("Native methods registered successfully");

        } catch (Throwable e) {
            Logx.w("Native method registration failed: " + e.getMessage());
            // Continue anyway - some functionality may still work
        }
    }

    /**
     * Check if native library is loaded and ready
     */
    public static boolean isNativeLibraryReady() {
        return NativeUtils.isNativeLoaded();
    }

    /**
     * Get native library load status for debugging
     */
    public static String getNativeLoadStatus() {
        if (NativeUtils.isNativeLoaded()) {
            return "Native library loaded and ready";
        } else {
            return "Native library not loaded";
        }
    }
}