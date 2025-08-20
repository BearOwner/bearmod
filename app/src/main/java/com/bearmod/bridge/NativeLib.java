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
     * Initialize native components (registration, safe setup).
     * Call early (e.g., from SplashActivity) after System.loadLibrary.
     */
    public static native void initialize(Context context);
}
