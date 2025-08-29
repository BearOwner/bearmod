package com.bearmod.util;

import android.util.Log;

import com.bearmod.BuildConfig;

/**
 * Logx - minimal logging wrapper using short codes to avoid sensitive cleartext strings in APK.
 * In debug builds, codes are mapped to descriptive messages; in release builds, only codes are logged
 * or logs can be disabled entirely.
 */
public final class Logx {
    private Logx() {}

    // Single short tag to reduce footprint
    private static final String T = "BM"; // short tag

    // Optional: map codes to verbose messages in debug only
    private static String msg(String code) {
        if (!BuildConfig.DEBUG) return code; // return code only in release
        switch (code) {
            // SplashActivity codes
            case "SPL_LOAD_OK": return "Native library loaded successfully";
            case "SPL_REG_OK": return "NativeLib.registerNatives executed successfully";
            case "SPL_REG_FAIL": return "registerNatives failed (limited mode)";
            case "SPL_INIT_SKIP": return "Skipping LoginActivity.safeInit() temporarily for crash isolation";
            case "SPL_NATIVE_INIT_FAIL": return "Native init failed (continuing in limited mode)";
            case "SPL_LOAD_FAIL": return "Failed to load native library";
            case "SPL_UNEXPECTED": return "Unexpected error loading native library";
            case "SPL_AUTH_CHECK": return "Checking authentication...";
            case "SPL_AUTH_OK": return "Auto-login successful - navigating to MainActivity";
            case "SPL_AUTH_FAIL": return "Auto-login failed - navigating to LoginActivity";
            case "SPL_NO_AUTH": return "No stored authentication - navigating to LoginActivity";
            case "SPL_SEC_ALERT": return "Suspicious package detected";
            // LoginActivity codes
            case "LOG_INIT_OK": return "Native Init called successfully";
            case "LOG_INIT_MISS": return "Native Init not available (library not loaded yet)";
            case "LOG_INIT_FAIL": return "Init call failed";
            case "LOG_DESTROY": return "LoginActivity destroyed";
            case "LOG_VERIFY_START": return "Verifying license with KeyAuth...";
            case "LOG_VERIFY_OK": return "License verification successful!";
            case "LOG_VERIFY_FAIL": return "License verification failed";
            default: return code;
        }
    }

    public static void d(String code) { Log.d(T, msg(code)); }
    public static void w(String code) { Log.w(T, msg(code)); }
    public static void e(String code) { Log.e(T, msg(code)); }

    // Overloads with throwable
    public static void w(String code, Throwable t) { Log.w(T, msg(code), t); }
    public static void e(String code, Throwable t) { Log.e(T, msg(code), t); }
}
