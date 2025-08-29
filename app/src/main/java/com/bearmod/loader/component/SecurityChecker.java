package com.bearmod.loader.component;

import android.content.Context;
import android.content.pm.PackageManager;
import com.bearmod.loader.utilities.Logx;

/**
 * SecurityChecker - Handles security validation and anti-detection checks
 *
 * Extracted from SplashActivity.java to centralize security concerns.
 * Provides clean interface for security validation during app initialization.
 *
 * Migrated from com.bearmod.activity.SplashActivity (449 LOC → focused security component)
 */
public class SecurityChecker {
    private static final String TAG = "SecurityChecker";

    // Suspicious packages to check for
    private static final String[] SUSPICIOUS_PACKAGES = {
        "com.guoshi.httpcanary.premium",
        "com.guoshi.httpcanary",
        "com.sniffer",
        "com.httpcanary.pro",
        "de.robv.android.xposed.installer",
        "org.meowcat.edxposed.manager",
        "io.va.exposed"
    };

    // Security check results
    public enum SecurityResult {
        SECURE,
        SUSPICIOUS_APP_DETECTED,
        VERIFICATION_FAILED
    }

    /**
     * Perform comprehensive security checks
     */
    public static SecurityResult performSecurityChecks(Context context) {
        Logx.d("Performing security checks");

        try {
            // Check for suspicious apps
            String detectedApp = checkForSuspiciousApps(context);
            if (detectedApp != null) {
                Logx.w("Suspicious app detected: " + detectedApp);
                return SecurityResult.SUSPICIOUS_APP_DETECTED;
            }

            // Additional security checks can be added here
            // - Root detection
            // - Emulator detection
            // - Tampering detection

            Logx.d("Security checks passed");
            return SecurityResult.SECURE;

        } catch (Exception e) {
            Logx.e("Error during security checks", e);
            return SecurityResult.VERIFICATION_FAILED;
        }
    }

    /**
     * Check for suspicious/debugging applications
     */
    private static String checkForSuspiciousApps(Context context) {
        for (String packageName : SUSPICIOUS_PACKAGES) {
            if (isAppInstalled(context, packageName)) {
                return packageName;
            }
        }
        return null;
    }

    /**
     * Check if a specific app is installed
     */
    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get user-friendly error message for security violations
     */
    public static String getSecurityErrorMessage(SecurityResult result, String detectedApp) {
        switch (result) {
            case SUSPICIOUS_APP_DETECTED:
                return "Please remove debugging/hooking tools (" + detectedApp + ") and restart the app.";
            case VERIFICATION_FAILED:
                return "Security verification failed. Please restart the app.";
            default:
                return "Security check failed.";
        }
    }
}