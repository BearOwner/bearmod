package com.bearmod.bridge;

import android.content.Context;

public final class NativeLib {
    static {
        try {
            System.loadLibrary("bearmod");
        } catch (Throwable t) {
            // Let caller handle init failure
        }
    }

    private NativeLib() {}

    /**
     * Initializes native components used by BearMod.
     * 
     * Lifecycle: should be called early (e.g., from `SplashActivity`) after process start.
     * Threading: call from main thread unless otherwise documented.
     * 
     * @param context Android context used for initialization hooks.
     */
    public static native void initialize(Context context);

    // Auth flows (fed by existing KeyAuth Java layer)
    /**
     * Sets the authentication token used by native code paths.
     * 
     * @param token opaque token string; null or empty clears stored token.
     */
    public static native void setAuthToken(String token);

    /**
     * Provides both user identifier and token to native code paths.
     * 
     * @param user  user identifier (username or id), may be empty
     * @param token authentication token, may be empty
     */
    public static native void setAuth(String user, String token);

    /**
     * Clears any native-side cached authentication state.
     */
    public static native void clearAuth();

    /**
     * Lightweight validation indicating whether a token is currently present.
     * 
     * Note: Full validation remains in the Java KeyAuth layer; this is a minimal signal.
     * 
     * @return true if native layer considers authentication present/valid; false otherwise.
     */
    public static native boolean isAuthValid();

    // Diagnostics
    /**
     * Returns a human-readable native library version string.
     * 
     * @return version string (e.g., "1.0").
     */
    public static native String getVersion();
}
