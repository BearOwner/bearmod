package com.bearmod.loader.utilities;

import android.util.Log;

/**
 * NativeUtils - JNI/native helper bridge
 * Provides utilities for loading .so libraries and managing native method bindings
 *
 * Migrated from com.bearmod.util to com.bearmod.loader.utilities for better package organization.
 */
public final class NativeUtils {
    private static final String TAG = "NativeUtils";
    private static volatile boolean nativeLoaded = false;

    private NativeUtils() {}

    /**
     * Set native library loaded flag
     * Should be called by SplashActivity after System.loadLibrary succeeds
     */
    public static void setNativeLoaded(boolean loaded) {
        nativeLoaded = loaded;
        Log.d(TAG, "Native loaded flag set to " + loaded);
    }

    /**
     * Check if native library is loaded
     */
    public static boolean isNativeLoaded() {
        return nativeLoaded;
    }

    /**
     * Assert that native library is ready or throw exception
     * Use this before making JNI calls that require native library
     */
    public static void assertReadyOrThrow() {
        if (!nativeLoaded) {
            throw new IllegalStateException("Native library not loaded yet; aborting JNI call");
        }
    }

    /**
     * Run a runnable only if native library is ready
     * Safely handles cases where native library might not be loaded yet
     */
    public static void runIfReady(Runnable r) {
        if (nativeLoaded) {
            try {
                r.run();
            } catch (Throwable t) {
                Log.e(TAG, "Error running guarded native call", t);
            }
        } else {
            Log.w(TAG, "Native not ready; skipping guarded runnable");
        }
    }
}