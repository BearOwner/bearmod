package com.bearmod.security;

import android.content.Context;

/**
 * NativeLib - Primary JNI bridge expected by native code (bearmod_jni.cpp).
 *
 * Methods map exactly to native implementations:
 * - initialize(Context)
 * - getVersion()
 * - isSecure()
 * - isInitialized()
 * - performRuntimeCheck(Context)
 *
 * Notes:
 * - Library loading is typically handled elsewhere (e.g. SplashActivity),
 *   but this class includes a defensive load to support direct use in tests.
 * - Keep signatures and instance/static status exactly aligned with native.
 */
public final class NativeLib {
    static {
        try {
            System.loadLibrary("bearmod");
        } catch (Throwable ignored) {
            // Caller can handle load failures; avoid crashing static init
        }
    }

    private NativeLib() {}

    /** Initialize the native library with application context. */
    public native boolean initialize(Context context);

    /** Get native library version string. */
    public native String getVersion();

    /** True if native security checks passed and secure mode is active. */
    public native boolean isSecure();

    /** True if native library has completed initialization. */
    public native boolean isInitialized();

    /** Perform runtime security checks (can be invoked periodically). */
    public native boolean performRuntimeCheck(Context context);
}
