package com.bearmod.security;

import android.content.Context;
import android.util.Log;

/**
 * Minimal RuntimeInjectionManager stub to satisfy cleanup flows.
 * Provides no-op cleanup for injected libraries.
 */
public class RuntimeInjectionManager {
    private static final String TAG = "RuntimeInjectionManager";
    private final Context appContext;

    public RuntimeInjectionManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void cleanupAllInjectedLibraries() {
        try {
            Log.d(TAG, "cleanupAllInjectedLibraries (no-op)");
        } catch (Exception ignored) { }
    }
}
