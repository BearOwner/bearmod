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

    // Bootstrap init from SplashActivity
    public static native void initialize(Context context);

    // Auth flows (fed by existing KeyAuth Java layer)
    public static native void setAuthToken(String token);
    public static native void setAuth(String user, String token);
    public static native void clearAuth();
    public static native boolean isAuthValid();

    // Diagnostics
    public static native String getVersion();
}
