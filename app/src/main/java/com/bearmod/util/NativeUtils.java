package com.bearmod.util;

import android.util.Log;

public final class NativeUtils {
    private static final String TAG = "NativeUtils";
    private static volatile boolean nativeLoaded = false;

    private NativeUtils() {}

    // SplashActivity should call this after System.loadLibrary succeeds
    public static void setNativeLoaded(boolean loaded) {
        nativeLoaded = loaded;
        Log.d(TAG, "Native loaded flag set to " + loaded);
    }

    public static boolean isNativeLoaded() {
        return nativeLoaded;
    }

    public static void assertReadyOrThrow() {
        if (!nativeLoaded) {
            throw new IllegalStateException("Native library not loaded yet; aborting JNI call");
        }
    }

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
