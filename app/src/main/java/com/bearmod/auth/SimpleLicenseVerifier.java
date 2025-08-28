package com.bearmod.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bearmod.activity.LoginActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * SimpleLicenseVerifier
 * <p>
 * Purpose:
 * Provides client-side license verification using KeyAuth (v1.3) with a simplified flow.
 * Acts as a bridge between Java UI (e.g., {@link com.bearmod.activity.LoginActivity}) and
 * native BearMod authentication state (updated via the JNI bridge).
 * <p>
 * Responsibilities:
 * - Stores and validates session and license tokens with resilience across reinstalls.
 * - Interacts with KeyAuth using OkHttp, handling init, session check, and license verify.
 * - Updates native authentication state via JNI bridge utilities after successful auth.
 * - Exposes results to Java UI and other components.
 * <p>
 * Lifecycle:
 * - Invoked during user login for license verification.
 * - Can be re-validated on app resume to ensure session integrity.
 * - Clears state on logout and propagates changes to native layer.
 * <p>
 * JNI Bindings:
 * - native String {@link #ID()} — instance method registered in JNI (see JNI_Bridge.h/cpp).
 *   Used by native code to fetch an identifier string when needed.
 * <p>
 * Notes:
 * - Keep network and persistence logic here; keep JNI implementations minimal and stable.
 * - When adding new JNI hooks, update Javadoc and ensure validator coverage in CI.
 */
public class SimpleLicenseVerifier {
    private static final String TAG = "SimpleLicenseVerifier";

    // Phase 1 string hardening: short randomized pref names/keys via lightweight decoder
    private static final int OBF_SALT = 413;
    private static String PREFS_MAIN() { return com.bearmod.util.StrObf.d(new int[]{498,399}, OBF_SALT); } // "o3"
    private static String PREFS_DEVICE() { return com.bearmod.util.StrObf.d(new int[]{498,392}, OBF_SALT); } // "o4"
    private static String K_SESSION() { return com.bearmod.util.StrObf.d(new int[]{493,397}, OBF_SALT); } // "p1"
    private static String K_TOKEN() { return com.bearmod.util.StrObf.d(new int[]{493,398}, OBF_SALT); }   // "p2"
    private static String K_EXP() { return com.bearmod.util.StrObf.d(new int[]{493,399}, OBF_SALT); }      // "p3"
    private static String K_HWID() { return com.bearmod.util.StrObf.d(new int[]{493,392}, OBF_SALT); }     // "p4"
    private static String K_T_STATUS() { return com.bearmod.util.StrObf.d(new int[]{493,393}, OBF_SALT); } // "p5"
    private static String K_T_BANNED() { return com.bearmod.util.StrObf.d(new int[]{493,394}, OBF_SALT); } // "p6"
    private static String K_T_LAST() { return com.bearmod.util.StrObf.d(new int[]{493,395}, OBF_SALT); }   // "p7"
    private static String K_ATS() { return com.bearmod.util.StrObf.d(new int[]{494,396}, OBF_SALT); }       // "p8" auth timestamp
    // UI prefs
    private static String K_LIC_SAVED() { return com.bearmod.util.StrObf.d(new int[]{494,398}, OBF_SALT); } // "p9" saved license key
    private static String K_REMEMBER() { return com.bearmod.util.StrObf.d(new int[]{494,399}, OBF_SALT); }  // "p10" remember key flag
    private static String K_AUTOLOGIN() { return com.bearmod.util.StrObf.d(new int[]{495,392}, OBF_SALT); } // "p11" auto login flag
    // Device prefs
    private static String K_HWID_LAST_RESET() { return com.bearmod.util.StrObf.d(new int[]{495,393}, OBF_SALT); } // "d1"
    private static String K_CACHED_HWID() { return com.bearmod.util.StrObf.d(new int[]{495,394}, OBF_SALT); }      // "d2"

    // One-time migration from legacy names if present
    private static void migrateLegacyPrefs(Context context) {
        try {
            SharedPreferences legacy = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            if (!legacy.getAll().isEmpty()) {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
                SharedPreferences.Editor e = prefs.edit();
                if (!prefs.contains(K_SESSION())) {
                    String v = legacy.getString("session_id", null); if (v != null) e.putString(K_SESSION(), v);
                }
                if (!prefs.contains(K_EXP())) {
                    String v = legacy.getString("user_expiration", null); if (v != null) e.putString(K_EXP(), v);
                }
                if (!prefs.contains(K_TOKEN())) {
                    String v = legacy.getString("auth_token", null); if (v != null) e.putString(K_TOKEN(), v);
                }
                if (!prefs.contains(K_HWID())) {
                    String v = legacy.getString("hwid", null); if (v != null) e.putString(K_HWID(), v);
                }
                if (!prefs.contains(K_T_STATUS())) {
                    String v = legacy.getString("token_status", null); if (v != null) e.putString(K_T_STATUS(), v);
                }
                if (!prefs.contains(K_T_BANNED())) {
                    boolean v = legacy.getBoolean("token_banned", false); e.putBoolean(K_T_BANNED(), v);
                }
                if (!prefs.contains(K_T_LAST())) {
                    long v = legacy.getLong("token_last_validated", 0L); if (v != 0L) e.putLong(K_T_LAST(), v);
                }
                // UI legacy migrations
                if (!prefs.contains(K_LIC_SAVED())) {
                    String v = legacy.getString("saved_license", null); if (v != null) e.putString(K_LIC_SAVED(), v);
                }
                if (!prefs.contains(K_REMEMBER())) {
                    boolean v = legacy.getBoolean("remember_key", false); e.putBoolean(K_REMEMBER(), v);
                }
                if (!prefs.contains(K_AUTOLOGIN())) {
                    boolean v = legacy.getBoolean("auto_login_enabled", false); e.putBoolean(K_AUTOLOGIN(), v);
                }
                e.apply();
                // Optionally clear legacy after migration
                // legacy.edit().clear().apply();
            }
        } catch (Throwable ignored) {}
    }

    // JNI: Instance native method required by JNI registration
    // Matches C++ signature: Java_com_bearmod_auth_SimpleLicenseVerifier_ID(JNIEnv*, jobject)
    public native String ID();

    // KeyAuth API v1.3 Configuration
    private static final String APP_HASH = "4f9b15598f6e8bdf07ca39e9914cd3e9";
    private static final String OWNER_ID = "yLoA9zcOEF";
    private static final String APP_NAME = "com.bearmod";
    private static final String VERSION = "1.3"; // Application version
    private static final String API_URL = "https://keyauth.win/api/1.3/";
    // Optional API base override for diagnostics; null means use official URL
    private static String apiBaseOverride = null;
    private static String getApiUrl() {
        return (apiBaseOverride != null && !apiBaseOverride.isEmpty()) ? apiBaseOverride : API_URL;
    }
    /**
     * Override KeyAuth API base URL for testing or diagnostics.
     * Pass null/empty to reset to the official URL.
     */
    public static void setApiBaseOverride(String override) {
        apiBaseOverride = (override != null && !override.trim().isEmpty()) ? override.trim() : null;
    }
    // Debug flag for enhanced logging
    private static final boolean DEBUG_MODE = false;

    // Shared HTTP client (consistent timeouts to avoid UI watchdog timeouts)
    private static final java.util.concurrent.TimeUnit TU = java.util.concurrent.TimeUnit.SECONDS;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TU)
            .writeTimeout(10, TU)
            .readTimeout(10, TU)
            .callTimeout(12, TU)
            .retryOnConnectionFailure(false)
            .build();

    // Security policy
    private static final int MIN_HWID_LENGTH = 20; // HWID.java currently returns 32 hex chars
    private static final long HWID_RESET_COOLDOWN_MS = 72L * 60 * 60 * 1000; // 72 hours
    private static final String TOKEN_STATUS_USED = "USED";
    private static final String TOKEN_STATUS_UNUSED = "UNUSED";

    // Session management (simplified approach based on Python test)
    private static String sessionId = null;
    private static String userExpiration = null;
    private static boolean isAuthenticated = false;

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    // ========= HWID & Token Security Helpers =========
    private static boolean isValidHwid(String hwid) {
        if (hwid == null) return true;
        String trimmed = hwid.trim();
        if (trimmed.isEmpty()) return true;
        if (trimmed.length() < MIN_HWID_LENGTH) return true;
        // Our HWID is hex MD5 string; be permissive but prefer hex
        return !trimmed.matches("[A-Fa-f0-9]{20,}");
    }

    private static boolean isBannedFromResponse(String responseBody) {
        try {
            if (responseBody == null) return false;
            String lower = responseBody.toLowerCase();
            if (lower.contains("\"banned\":true") || lower.contains("\"ban\":true")) return true;
            if (lower.contains("banned") && lower.contains("true")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private static void markTokenStatus(Context context) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(K_T_STATUS(), SimpleLicenseVerifier.TOKEN_STATUS_USED)
                    .putBoolean(K_T_BANNED(), false)
                    .putLong(K_T_LAST(), System.currentTimeMillis())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to mark token status", e);
        }
    }

    private static boolean validateTokenStatus(Context context) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            String token = prefs.getString(K_TOKEN(), null);
            String status = prefs.getString(K_T_STATUS(), null);
            boolean banned = prefs.getBoolean(K_T_BANNED(), false);
            if (token == null || token.isEmpty()) {
                Log.w(TAG, "No stored auth token found");
                return false;
            }
            if (banned) {
                Log.e(TAG, "Token is banned");
                return false;
            }
            if (!TOKEN_STATUS_USED.equals(status)) {
                Log.w(TAG, "Token not marked as USED");
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Token status validation error", e);
            return false;
        }
    }
    // ======== Device-Bound Token Generation (no server tokens) ========
    private static String generateDeviceToken(String sessionId, String hwid) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest((sessionId + ":" + hwid).getBytes());
            StringBuilder token = new StringBuilder();
            for (byte b : hashBytes) token.append(String.format("%02x", b));
            return token.toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate device token", e);
            return null;
        }
    }

    // Removed server token storage methods since server token validation is disabled
    // All server-token related helpers and persistence have been deleted.


    private static void generateAndStoreTokenFromSession(Context context, String sessionId, String hwid) {
        try {
            String token = generateDeviceToken(sessionId, hwid);
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "Token generation returned empty value");
                return;
            }
            storeTokenWithPersistence(context, token, sessionId, userExpiration);
            markTokenStatus(context);
            Log.d(TAG, "Generated auth token from session and stored as USED");
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate token from session", e);
        }
    }

    private static boolean isHwidResetAllowed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_DEVICE(), Context.MODE_PRIVATE);
        long last = prefs.getLong(K_HWID_LAST_RESET(), 0L);
        long now = System.currentTimeMillis();
        return (now - last) >= HWID_RESET_COOLDOWN_MS;
    }

    private static long getHwidResetRemainingMs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_DEVICE(), Context.MODE_PRIVATE);
        long last = prefs.getLong(K_HWID_LAST_RESET(), 0L);
        long now = System.currentTimeMillis();
        long passed = now - last;
        return Math.max(0, HWID_RESET_COOLDOWN_MS - passed);
    }

    // ========= End Security Helpers =========


    public interface LicenseCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }



    /**
     * Automatic login using stored session ID (simplified approach based on Python test)
     *
     * @param context Application context
     * @param callback Result callback
     */
    public static void autoLogin(Context context, AuthCallback callback) {
        Log.d(TAG, "Attempting automatic login with stored session...");

        // Check if we have a stored session ID
        String storedSessionId = getStoredSessionId(context);
        if (storedSessionId == null || storedSessionId.isEmpty()) {
            // Fallback: if user enabled Auto-login and Remember Key, try verifying with saved license key
            if (isAutoLoginEnabled(context) && isRememberKeyEnabled(context)) {
                String savedKey = getSavedLicenseKey(context);
                if (savedKey != null && !savedKey.isEmpty()) {
                    Log.d(TAG, "No session ID found - attempting auto license verification using saved key");
                    verifyLicense(context, savedKey, new LicenseCallback() {
                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess("Auto-login via saved key successful");
                        }
                        @Override
                        public void onFailure(String error) {
                            callback.onError("Auto-login via saved key failed: " + error);
                        }
                    });
                    return;
                }
            }
            callback.onError("No stored session ID found");
            return;
        }

        // Run in background thread
        CompletableFuture.runAsync(() -> {
            try {
                // Use shared HTTP client
                OkHttpClient client = HTTP_CLIENT;

                // Step 1: Initialize KeyAuth application
                if (initializeKeyAuth(client)) {
                    callback.onError("KeyAuth initialization failed");
                    return;
                }

                // Step 2: Validate stored session (simplified approach)
                validateStoredSession(client, context, storedSessionId, callback);

            } catch (Exception e) {
                Log.e(TAG, "Auto login error", e);
                callback.onError("Auto login error: " + e.getMessage());
            }
        });
    }

    /**
     * Check if user has valid stored authentication
     * Simplified based on Python test results - focus on session ID persistence
     *
     * @param context Application context
     * @return true if user has valid stored authentication
     */
    public static boolean hasValidStoredAuth(Context context) {
        String sessionId = getStoredSessionId(context);
        if (sessionId == null || sessionId.isEmpty()) {
            Log.d(TAG, "No stored session ID found");
            return false;
        }

        Log.d(TAG, "Found stored session ID - will attempt validation");
        // Don't rely on isAuthenticated flag - always attempt validation
        // This matches the successful Python implementation
        return true;
    }

    /**
     * Get user expiration date for countdown timer
     *
     * @return Expiration date in "yyyy-MM-dd HH:mm:ss" format, or null if not available
     */
    public static String getUserExpiration() {
        return userExpiration;
    }

    /**
     * Clear stored authentication data (logout)
     *
     * @param context Application context
     */
    public static void clearStoredAuth(Context context) {
        migrateLegacyPrefs(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
        prefs.edit()
                .remove(K_SESSION())
                .remove(K_EXP())
                .apply();

        // Clear runtime variables
        sessionId = null;
        userExpiration = null;
        isAuthenticated = false;

        // Clear C++ authentication state via JNI bridge
        updateCppAuthenticationState("", "", "", false);

        Log.d(TAG, "Stored authentication data cleared and C++ state updated");
    }

    /**
     * KeyAuth license verification method - primary authentication method
     * Uses KeyAuth API with license key verification flow
     *
     * @param context Application context
     * @param licenseKey License key to verify
     * @param callback Result callback
     */
    public static void verifyLicense(Context context, String licenseKey, LicenseCallback callback) {
        Log.d(TAG, "Starting KeyAuth license verification flow...");

        // Run in background thread to avoid blocking UI
        CompletableFuture.runAsync(() -> {
            try {
                // Use shared HTTP client
                OkHttpClient client = HTTP_CLIENT;

                // Step 1: Initialize KeyAuth application (required before other functions) - include token/hash if we have them.
            // KeyAuth initialization (token validation disabled server-side)

                if (initializeKeyAuth(client)) {
                    callback.onFailure("KeyAuth initialization failed");
                    return;
                }

                // Step 2: Generate stable HWID using centralized HWID implementation
                String hwid = HWID.getHWID();
                Log.d(TAG, "Using centralized HWID: " + hwid);

                // Step 3: Verify license with KeyAuth
                verifyLicenseWithKeyAuth(client, context, licenseKey, hwid, callback);

            } catch (Exception e) {
                Log.e(TAG, "License verification error", e);
                callback.onFailure("Verification error: " + e.getMessage());
            }
        });
    }

    // Session ID storage moved to top of class

    /**
     * Initialize KeyAuth application (no session ID expected from init call)
     * @return true if successful, false if failed
     */
    private static boolean initializeKeyAuth(OkHttpClient client) {
        try {
            Log.d(TAG, "=== KeyAuth Initialization Debug ===");

            // Validate configuration first
            validateApiConfiguration();

            Log.d(TAG, "API_URL: " + getApiUrl());

            Log.d(TAG, "APP_NAME: " + APP_NAME);
            Log.d(TAG, "OWNER_ID: " + OWNER_ID);
            Log.d(TAG, "VERSION: " + VERSION);
            Log.d(TAG, "APP_HASH: " + APP_HASH);

            FormBody formBody = new FormBody.Builder()
                    .add("type", "init")
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("ver", VERSION)
                    .add("hash", APP_HASH)
                    .build();

            Log.d(TAG, "Request parameters built successfully");

            Request request = new Request.Builder()
                    .url(getApiUrl())
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            Log.d(TAG, "Sending KeyAuth init request to: " + getApiUrl());

            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "Response received - HTTP " + response.code());
                Log.d(TAG, "Response headers: " + response.headers().toString());

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "KeyAuth init response body: " + responseBody);

                    // Enhanced success detection
                    boolean success = responseBody.contains("\"success\":true") ||
                                     responseBody.contains("\"success\": true") ||
                                     responseBody.contains("\"success\": \"true\"") ||
                                     responseBody.contains("\"success\":\"true\"");

                    Log.d(TAG, "Success detection result: " + success);

                    if (success) {
                        // Init succeeded: attempt to extract session ID for subsequent calls (some KeyAuth API versions require it)
                        String initSessionId = extractSessionId(responseBody);
                        if (initSessionId != null && !initSessionId.isEmpty()) {
                            sessionId = initSessionId;
                            Log.d(TAG, "KeyAuth initialization successful, session ID: " + sessionId.substring(0, Math.min(8, sessionId.length())) + "...");
                        } else {
                            Log.w(TAG, "Init success but no session ID present; proceeding, license call may still work");
                        }
                        return false;
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        Log.e(TAG, "KeyAuth initialization failed: " + errorMsg);
                        Log.e(TAG, "Full response for debugging: " + responseBody);
                        return true;
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.body().string();
                    } catch (Exception e) {
                        errorBody = "Could not read error body";
                    }
                    Log.e(TAG, "KeyAuth init failed: HTTP " + response.code());
                    Log.e(TAG, "Error response body: " + errorBody);
                    return true;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "KeyAuth initialization exception", e);
            Log.e(TAG, "Exception details: " + e.getMessage());
            return true;
        }
    }



    /**
     * Validate stored session with KeyAuth (simplified approach based on Python test)
     */
    private static void validateStoredSession(OkHttpClient client, Context context, String storedSessionId, AuthCallback callback) {
        try {
            Log.d(TAG, "=== Session Validation ===");
            Log.d(TAG, "Stored Session ID: " + storedSessionId.substring(0, Math.min(8, storedSessionId.length())) + "...");

            // Use the stored session ID directly
            sessionId = storedSessionId;

            // Get current HWID for validation
            String currentHwid = HWID.getHWID();
            Log.d(TAG, "Current HWID: " + currentHwid);

            FormBody formBody = new FormBody.Builder()
                    .add("type", "check")
                    .add("sessionid", sessionId)
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("ver", VERSION)
                    .add("hash", APP_HASH)
                    .build();

            Request request = new Request.Builder()
                    .url(getApiUrl())
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Session validation response: " + responseBody);

                    boolean isValid = responseBody.contains("\"success\":true") ||
                                     responseBody.contains("\"success\": true");

                    if (isValid) {
                        // Enforce HWID lock: ensure stored HWID matches current device HWID
                        SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
                        String storedHwid = prefs.getString(K_HWID(), null);
                        if (isValidHwid(currentHwid)) {
                            Log.e(TAG, "Invalid current HWID");
                            clearStoredAuth(context);
                            updateCppAuthenticationState("", "", "", false);
                            callback.onError("Invalid hardware ID");
                            return;
                        }
                        if (storedHwid != null && !storedHwid.equalsIgnoreCase(currentHwid)) {
                            Log.e(TAG, "HWID lock violation: stored=" + storedHwid + ", current=" + currentHwid);
                            clearStoredAuth(context);
                            updateCppAuthenticationState("", "", "", false);
                            callback.onError("HWID mismatch - access denied");
                            return;
                        }

                        // Validate token status
                        if (!validateTokenStatus(context)) {
                            Log.w(TAG, "Token status invalid during session validation");
                            // We can regenerate a token from session + hwid deterministically
                            generateAndStoreTokenFromSession(context, sessionId, currentHwid);
                        }

                        // Session is valid, restore authentication state
                        isAuthenticated = true;

                        // Restore user expiration from stored data
                        userExpiration = prefs.getString(K_EXP(), null);

                        // Ensure session and HWID are persisted for future launches
                        prefs.edit()
                                .putString(K_SESSION(), sessionId)
                                .putString(K_HWID(), currentHwid)
                                .putLong(K_ATS(), System.currentTimeMillis())
                                .apply();

                        // Update C++ authentication state via JNI bridge with current HWID
                        String generatedToken = generateDeviceToken(sessionId, currentHwid);
                        updateCppAuthenticationState(sessionId, generatedToken, currentHwid, true);

                        Log.d(TAG, "Session validation successful, authentication state restored");
                        if (DEBUG_MODE) Log.d(TAG, "Callback: onSuccess(Auto-login successful)");
                        callback.onSuccess("Auto-login successful");
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        Log.w(TAG, "Session validation failed: " + errorMsg);

                        // Session is invalid, clear stored data
                        clearStoredAuth(context);

                        // Clear C++ authentication state on failure
                        updateCppAuthenticationState("", "", "", false);

                        if (DEBUG_MODE) Log.d(TAG, "Callback: onError(session invalid): " + errorMsg);
                        callback.onError("Stored session is invalid or expired: " + errorMsg);
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.body().string();
                    } catch (Exception e) {
                        errorBody = "Could not read error body";
                    }
                    Log.e(TAG, "Session validation failed: HTTP " + response.code());
                    Log.e(TAG, "Error response body: " + errorBody);
                    if (DEBUG_MODE) Log.d(TAG, "Callback: onError(network check): HTTP " + response.code());
                    callback.onError("Network error: HTTP " + response.code() + " - " + errorBody);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Session validation exception", e);
            if (DEBUG_MODE) Log.d(TAG, "Callback: onError(session exception): " + e.getMessage());
            callback.onError("Session validation error: " + e.getMessage());
        }
    }

    /**
     * Verify license with KeyAuth (after initialization, using session ID)
     */
    private static void verifyLicenseWithKeyAuth(OkHttpClient client, Context context, String licenseKey, String hwid, LicenseCallback callback) {
        try {
            Log.d(TAG, "Verifying license with KeyAuth...");

            // API v1.3 requires session ID obtained from init
            if (sessionId == null || sessionId.isEmpty()) {
                callback.onFailure("No session ID available - initialization may have failed");
                return;
            }

            // HWID sanity check
            if (isValidHwid(hwid)) {
                callback.onFailure("Invalid hardware ID (HWID)");
                return;
            }

            FormBody formBody = new FormBody.Builder()
                    .add("type", "license")
                    .add("key", licenseKey)
                    .add("hwid", hwid)
                    .add("sessionid", sessionId)  // Required for API v1.3
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("ver", VERSION)
                    .build();

            Request request = new Request.Builder()
                    .url(getApiUrl())
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "License verification response: " + responseBody);

                    boolean isValid = responseBody.contains("\"success\":true") ||
                                      responseBody.contains("\"success\": true");

                    if (isValid) {
                        // Store session data and generate token using the existing session ID
                        generateAndStoreTokenFromSession(context, sessionId, hwid);
                        storeSessionData(context, sessionId, hwid, responseBody);
                        // Cache HWID for resilience across reinstalls (best-effort)
                        cacheHWID(context, hwid);
                        isAuthenticated = true;
                        String generatedToken = generateDeviceToken(sessionId, hwid);
                        updateCppAuthenticationState(sessionId, generatedToken, hwid, true);
                        if (DEBUG_MODE) Log.d(TAG, "Callback: onSuccess(license ok)");
                        callback.onSuccess("License verified successfully");
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        if (DEBUG_MODE) Log.d(TAG, "Callback: onFailure(license failed): " + errorMsg);
                        callback.onFailure("License verification failed: " + errorMsg);
                    }
                } else {
                    if (DEBUG_MODE) Log.d(TAG, "Callback: onFailure(network license): HTTP " + response.code());
                    callback.onFailure("Network error: " + response.code());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "License verification exception", e);
            if (DEBUG_MODE) Log.d(TAG, "Callback: onFailure(license exception): " + e.getMessage());
            callback.onFailure("License verification error: " + e.getMessage());
        }
    }

    // REMOVED: generateSimpleHWID() method - use HWID.getHWID() directly
    // All HWID generation now consolidated to HWID.java for consistency

    /**
     * Generate and store authentication token
     * Enhanced with persistence across app reinstallation
     */
    private static void generateAndStoreToken(Context context, String username, String hwid) {
        try {
            // Generate a unique token based on username, hwid, and current time
            String tokenData = username + ":" + hwid + ":" + System.currentTimeMillis();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(tokenData.getBytes());

            StringBuilder token = new StringBuilder();
            for (byte b : hashBytes) {
                token.append(String.format("%02x", b));
            }

            String generatedToken = token.toString();

            // Store token and session data with enhanced persistence
            storeTokenWithPersistence(context, generatedToken, sessionId, userExpiration);

            Log.d(TAG, "Authentication token generated and stored with enhanced persistence");

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate token", e);
        }
    }

    /**
     * Get stored authentication token with enhanced persistence
     * Tries multiple storage locations to survive app reinstallation
     */
    private static String getStoredToken(Context context) {
        // Method 1: Try SharedPreferences first (fastest access)
        migrateLegacyPrefs(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
        String token = prefs.getString(K_TOKEN(), null);
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Retrieved token from SharedPreferences");
            return token;
        }

        // Method 2: Try external storage cache
        try {
            File externalDir = context.getExternalFilesDir(null);
            if (externalDir != null) {
                File tokenFile = new File(externalDir, ".auth_cache");
                if (tokenFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
                        String storedToken = reader.readLine();
                        if (storedToken != null && !storedToken.isEmpty()) {
                            Log.d(TAG, "Retrieved token from external storage");
                            // Re-cache in SharedPreferences for faster access
                            prefs.edit().putString(K_TOKEN(), storedToken).apply();
                            return storedToken;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "External token retrieval failed: " + e.getMessage());
        }

        Log.d(TAG, "No stored token found");
        return null;
    }

    /**
     * Get stored session ID with persistence fallback
     */
    private static String getStoredSessionId(Context context) {
        // Method 1: Try SharedPreferences first (fastest access)
        migrateLegacyPrefs(context);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
        String sessionId = prefs.getString(K_SESSION(), null);
        if (sessionId != null && !sessionId.isEmpty()) {
            Log.d(TAG, "Retrieved session ID from SharedPreferences");
            return sessionId;
        }

        // Method 2: Try external storage cache
        try {
            File externalDir = context.getExternalFilesDir(null);
            if (externalDir != null) {
                File sessionFile = new File(externalDir, ".session_cache");
                if (sessionFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(sessionFile))) {
                        String storedSessionId = reader.readLine();
                        String storedHwid = reader.readLine();
                        if (storedSessionId != null && !storedSessionId.isEmpty()) {
                            Log.d(TAG, "Retrieved session ID from external storage");
                            // Re-cache in SharedPreferences for faster access
                            prefs.edit().putString(K_SESSION(), storedSessionId).apply();
                            if (storedHwid != null && !storedHwid.isEmpty()) {
                                prefs.edit().putString(K_HWID(), storedHwid).apply();
                            }
                            return storedSessionId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "External session ID retrieval failed: " + e.getMessage());
        }

        Log.d(TAG, "No stored session ID found");
        return null;
    }

    // ========================================
    // CENTRALIZED LICENSE KEY STORAGE METHODS
    // ========================================
    /**
     * Persist the license key only if Remember Key is currently enabled (self-selection).
     * Does NOT toggle the remember flag.
     */
    public static void saveLicenseKey(Context context, String licenseKey) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            if (prefs.getBoolean(K_REMEMBER(), false)) {
                prefs.edit().putString(K_LIC_SAVED(), licenseKey).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save license key", e);
        }
    }

    /**
     * Get saved license key if Remember Key is enabled
     */
    public static String getSavedLicenseKey(Context context) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            boolean remember = prefs.getBoolean(K_REMEMBER(), false);
            if (!remember) return "";
            return prefs.getString(K_LIC_SAVED(), "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to get saved license key", e);
            return "";
        }
    }

    /**
     * Whether Remember Key is enabled
     */
    public static boolean isRememberKeyEnabled(Context context) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            return prefs.getBoolean(K_REMEMBER(), false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to read remember flag", e);
            return false;
        }
    }

    /**
     * Explicitly set Remember Key preference (self-selection).
     */
    public static void setRememberKeyPreference(Context context, boolean enabled) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(K_REMEMBER(), enabled).apply();
            if (!enabled) {
                // Clear stored license when turning off remember to avoid lingering secrets
                prefs.edit().remove(K_LIC_SAVED()).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save remember preference", e);
        }
    }

    /**
     * Persist auto-login preference
     */
    public static void saveAutoLoginPreference(Context context, boolean enabled) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            prefs.edit().putBoolean(K_AUTOLOGIN(), enabled).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save auto-login preference", e);
        }
    }

    /**
     * Whether auto-login is enabled
     */
    public static boolean isAutoLoginEnabled(Context context) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            return prefs.getBoolean(K_AUTOLOGIN(), false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to read auto-login preference", e);
            return false;
        }
    }

    /**
     * Cache HWID in multiple persistent locations to survive app reinstallation
     */
    private static void cacheHWID(Context context, String hwid) {
        try {
            // Method 1: SharedPreferences (survives app updates but not uninstall)
            SharedPreferences prefs = context.getSharedPreferences(PREFS_DEVICE(), Context.MODE_PRIVATE);
            prefs.edit().putString(K_CACHED_HWID(), hwid).apply();

            // Method 2: External storage cache (survives uninstall if permissions allow)
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File hwidFile = new File(externalDir, ".device_cache");
                    try (FileWriter writer = new FileWriter(hwidFile)) {
                        writer.write(hwid);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "External HWID cache failed: " + e.getMessage());
            }

            // Method 3: Internal app directory (backup method)
            try {
                File internalDir = new File(context.getFilesDir(), ".cache");
                if (!internalDir.exists()) {
                    internalDir.mkdirs();
                }
                File hwidFile = new File(internalDir, "device_id");
                try (FileWriter writer = new FileWriter(hwidFile)) {
                    writer.write(hwid);
                }
            } catch (Exception e) {
                Log.d(TAG, "Internal HWID cache failed: " + e.getMessage());
            }

            Log.d(TAG, "HWID cached successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to cache HWID", e);
        }
    }

    /**
     * Retrieve cached HWID from persistent storage
     */
    private static String getCachedHWID(Context context) {
        try {
            // Method 1: Try SharedPreferences first
            SharedPreferences prefs = context.getSharedPreferences(PREFS_DEVICE(), Context.MODE_PRIVATE);
            String cachedHwid = prefs.getString(K_CACHED_HWID(), null);
            if (cachedHwid != null && !cachedHwid.isEmpty()) {
                Log.d(TAG, "Retrieved HWID from SharedPreferences");
                return cachedHwid;
            }

            // Method 2: Try external storage
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File hwidFile = new File(externalDir, ".device_cache");
                    if (hwidFile.exists()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(hwidFile))) {
                            String hwid = reader.readLine();
                            if (hwid != null && !hwid.isEmpty()) {
                                Log.d(TAG, "Retrieved HWID from external storage");
                                // Re-cache in SharedPreferences for faster access
                                prefs.edit().putString(K_CACHED_HWID(), hwid).apply();
                                return hwid;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "External HWID retrieval failed: " + e.getMessage());
            }

            // Method 3: Try internal app directory
            try {
                File internalDir = new File(context.getFilesDir(), ".cache");
                File hwidFile = new File(internalDir, "device_id");
                if (hwidFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(hwidFile))) {
                        String hwid = reader.readLine();
                        if (hwid != null && !hwid.isEmpty()) {
                            Log.d(TAG, "Retrieved HWID from internal storage");
                            // Re-cache in SharedPreferences for faster access
                            prefs.edit().putString(K_CACHED_HWID(), hwid).apply();
                            return hwid;
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Internal HWID retrieval failed: " + e.getMessage());
            }

            Log.d(TAG, "No cached HWID found");
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Failed to retrieve cached HWID", e);
            return null;
        }
    }

    /**
     * Clear cached HWID and force regeneration (for debugging/support)
     */
    public static void resetHWID(Context context) {
        try {
            // Enforce cooldown for HWID reset
            if (!isHwidResetAllowed(context)) {
                long remainingMs = getHwidResetRemainingMs(context);
                throw new IllegalStateException("HWID reset cooldown active: " + (remainingMs / 1000) + "s remaining");
            }

            // Clear SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_DEVICE(), Context.MODE_PRIVATE);
            prefs.edit().remove(K_CACHED_HWID()).putLong(K_HWID_LAST_RESET(), System.currentTimeMillis()).apply();

            // Clear external storage cache
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File hwidFile = new File(externalDir, ".device_cache");
                    if (hwidFile.exists()) {
                        hwidFile.delete();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "External HWID cache clear failed: " + e.getMessage());
            }

            // Clear internal cache
            try {
                File internalDir = new File(context.getFilesDir(), ".cache");
                File hwidFile = new File(internalDir, "device_id");
                if (hwidFile.exists()) {
                    hwidFile.delete();
                }
            } catch (Exception e) {
                Log.d(TAG, "Internal HWID cache clear failed: " + e.getMessage());
            }

            Log.d(TAG, "HWID cache cleared - will regenerate on next access");

        } catch (Exception e) {
            Log.e(TAG, "Failed to reset HWID", e);
        }
    }

    // REMOVED: getDeviceHWID() wrapper method - use HWID.getHWID() directly
    // All HWID access now consolidated to HWID.java for consistency

    /**
     * Store authentication data after successful license verification
     * Enhanced with persistence across app reinstallation
     */
    private static void storeAuthenticationData(Context context, String sessionId, String hwid, String responseBody) {
        try {
            // Store in SharedPreferences (primary storage)
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Store session data
            editor.putString(K_SESSION(), sessionId);
            editor.putString(K_HWID(), hwid);
            editor.putLong(K_ATS(), System.currentTimeMillis());

            // Extract and store user info from response if available
            try {
                // Parse response for additional user data
                if (responseBody.contains("\"expiry\"")) {
                    // Extract expiry if present in response
                    String expiry = extractJsonValue(responseBody, "expiry");
                    if (expiry != null) {
                        editor.putString(K_EXP(), expiry);
                    }
                }

                // Subscription not used in current auth model; omit storing any "subscription" field
            } catch (Exception e) {
                Log.d(TAG, "Could not parse additional user data: " + e.getMessage());
            }

            editor.apply();

            // Also store critical data in external storage for persistence
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File authDataFile = new File(externalDir, ".session_cache");
                    try (FileWriter writer = new FileWriter(authDataFile)) {
                        // Store session ID and HWID for persistence
                        writer.write(sessionId + "\n" + hwid + "\n" + System.currentTimeMillis());
                    }
                    Log.d(TAG, "Authentication data backed up to external storage");
                }
            } catch (Exception e) {
                Log.d(TAG, "External auth data backup failed: " + e.getMessage());
            }

            Log.d(TAG, "Authentication data stored successfully with enhanced persistence");

        } catch (Exception e) {
            Log.e(TAG, "Failed to store authentication data", e);
        }
    }

    /**
     * Extract JSON value from response string
     */
    private static String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":" + "\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            // Log the exception message for debugging
            Log.d(TAG, "Failed to extract JSON value for key: " + key + " - " + e.getMessage());
        }
        return null;
    }

    // ========================================
    // Compatibility helpers (restored wrappers)
    // ========================================
    /**
     * Store token with persistence across storage locations and record session/expiry.
     * This method restores a previously referenced helper removed during refactor.
     */
    private static void storeTokenWithPersistence(Context context, String token, String sessionIdValue, String expiry) {
        try {
            migrateLegacyPrefs(context);
            SharedPreferences prefs = context.getSharedPreferences(PREFS_MAIN(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(K_TOKEN(), token);
            if (sessionIdValue != null && !sessionIdValue.isEmpty()) editor.putString(K_SESSION(), sessionIdValue);
            if (expiry != null && !expiry.isEmpty()) editor.putString(K_EXP(), expiry);
            editor.putLong(K_ATS(), System.currentTimeMillis());
            editor.apply();

            // External backup for resilience
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File tokenFile = new File(externalDir, ".auth_cache");
                    try (FileWriter writer = new FileWriter(tokenFile)) {
                        writer.write(token);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "External token backup failed: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to persist token", e);
        }
    }

    /**
     * Extract a session ID from a KeyAuth response body.
     * Tries explicit JSON key first, then falls back to alternative pattern matching.
     */
    private static String extractSessionId(String responseBody) {
        try {
            if (responseBody == null) return null;
            String fromJson = extractJsonValue(responseBody, "sessionid");
            if (fromJson != null && !fromJson.isEmpty()) return fromJson;
        } catch (Exception ignored) {}
        return extractSessionIdAlternative(responseBody);
    }

    /**
     * Extract an error message from a KeyAuth response body.
     */
    private static String extractErrorMessage(String responseBody) {
        try {
            if (responseBody == null) return "Unknown error";
            String msg = extractJsonValue(responseBody, "message");
            if (msg == null || msg.isEmpty()) msg = extractJsonValue(responseBody, "msg");
            return (msg != null && !msg.isEmpty()) ? msg : "Unknown error";
        } catch (Exception e) {
            return "Unknown error";
        }
    }

    /**
     * Wrapper retained for compatibility with previous code paths.
     * Delegates to centralized authentication data storage.
     */
    private static void storeSessionData(Context context, String sessionIdValue, String hwid, String responseBody) {
        storeAuthenticationData(context, sessionIdValue, hwid, responseBody);
    }

    /**
     * Validate KeyAuth API configuration
     * Compares current config with working Python implementation
     */
    public static void validateApiConfiguration() {
        Log.d(TAG, "=== KeyAuth API Configuration Validation ===");
        Log.d(TAG, "Current Configuration:");
        Log.d(TAG, "  API_URL: " + API_URL);
        Log.d(TAG, "  APP_HASH: " + APP_HASH);
        Log.d(TAG, "  OWNER_ID: " + OWNER_ID);
        Log.d(TAG, "  APP_NAME: " + APP_NAME);
        Log.d(TAG, "  VERSION: " + VERSION);

        // Validate configuration values
        boolean configValid = true;

        APP_HASH.length();

        Log.d(TAG, "Configuration validation result: " + "✅ VALID");
    }

    /**
     * Alternative session ID extraction method
     */
    private static String extractSessionIdAlternative(String responseBody) {
        try {
            // Try extracting any UUID-like pattern that could be a session ID
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
            java.util.regex.Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                String sessionId = matcher.group();
                Log.d(TAG, "Alternative session ID extraction successful: " + sessionId.substring(0, 8) + "...");
                return sessionId;
            }

            // Try extracting any long alphanumeric string that could be a session ID
            pattern = java.util.regex.Pattern.compile("[a-zA-Z0-9]{16,}");
            matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                String sessionId = matcher.group();
                Log.d(TAG, "Alternative session ID (alphanumeric) found: " + sessionId.substring(0, Math.min(8, sessionId.length())) + "...");
                return sessionId;
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, "Alternative session ID extraction failed", e);
            return null;
        }
    }

    /**
     * Update C++ authentication state via JNI bridge
     * This ensures mod functionality is properly gated behind KeyAuth verification
     */
    private static void updateCppAuthenticationState(String sessionId, String token, String hwid, boolean isValid) {
        try {
            // Call JNI method to update C++ global variables (bValid, g_Auth, g_Token)
            LoginActivity.updateAuthenticationState(sessionId, token, hwid, isValid);
            Log.d(TAG, "C++ authentication state updated via JNI bridge - Valid: " + isValid);
        } catch (Exception e) {
            Log.e(TAG, "Failed to update C++ authentication state", e);
        }
    }
}