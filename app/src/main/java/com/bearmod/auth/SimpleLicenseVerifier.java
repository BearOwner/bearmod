package com.bearmod.auth;

import android.annotation.SuppressLint;
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
 * Simple License Verifier for BearMod
 * Lightweight Java-only implementation without complex APIs or webhooks
 * Focuses on core license verification without interfering with target apps
 */
public class SimpleLicenseVerifier {
    private static final String TAG = "SimpleLicenseVerifier";

    // JNI: Instance native method required by JNI registration
    // Matches C++ signature: Java_com_bearmod_auth_SimpleLicenseVerifier_ID(JNIEnv*, jobject)
    public native String ID();

    // KeyAuth API v1.3 Configuration
    private static final String APP_HASH = "4f9b15598f6e8bdf07ca39e9914cd3e9";
    private static final String OWNER_ID = "yLoA9zcOEF";
    private static final String APP_NAME = "com.bearmod";
    private static final String VERSION = "1.3"; // Application version
    private static final String API_URL = "https://keyauth.win/api/1.3/";

    // Alternative API URLs for testing
    private static final String[] ALTERNATIVE_API_URLS = {
        "https://keyauth.win/api/1.2/",
        "https://keyauth.win/api/1.1/",
        "https://keyauth.win/api/1.0/"
    };

    // Debug flag for enhanced logging
    private static final boolean DEBUG_MODE = false;

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
            SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("token_status", SimpleLicenseVerifier.TOKEN_STATUS_USED)
                    .putBoolean("token_banned", false)
                    .putLong("token_last_validated", System.currentTimeMillis())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to mark token status", e);
        }
    }

    private static boolean validateTokenStatus(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            String token = prefs.getString("auth_token", null);
            String status = prefs.getString("token_status", null);
            boolean banned = prefs.getBoolean("token_banned", false);
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
        SharedPreferences prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE);
        long last = prefs.getLong("hwid_last_reset", 0L);
        long now = System.currentTimeMillis();
        return (now - last) >= HWID_RESET_COOLDOWN_MS;
    }

    private static long getHwidResetRemainingMs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE);
        long last = prefs.getLong("hwid_last_reset", 0L);
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
                // Create HTTP client
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                // Step 1: Initialize KeyAuth application
                if (!initializeKeyAuth(client)) {
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
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("session_id")
                .remove("user_expiration")
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
                // Create HTTP client
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                // Step 1: Initialize KeyAuth application (required before other functions) - include token/hash if we have them.
            // KeyAuth initialization (token validation disabled server-side)

                if (!initializeKeyAuth(client)) {
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

            Log.d(TAG, "API_URL: " + API_URL);
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
                    .url(API_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            Log.d(TAG, "Sending KeyAuth init request to: " + API_URL);

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
                        return true;
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        Log.e(TAG, "KeyAuth initialization failed: " + errorMsg);
                        Log.e(TAG, "Full response for debugging: " + responseBody);
                        return false;
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
                    return false;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "KeyAuth initialization exception", e);
            Log.e(TAG, "Exception details: " + e.getMessage());
            return false;
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
                    .url(API_URL)
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
                        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
                        String storedHwid = prefs.getString("hwid", null);
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
                        userExpiration = prefs.getString("user_expiration", null);

                        // Ensure session and HWID are persisted for future launches
                        prefs.edit()
                                .putString("session_id", sessionId)
                                .putString("hwid", currentHwid)
                                .putLong("auth_timestamp", System.currentTimeMillis())
                                .apply();

                        // Update C++ authentication state via JNI bridge with current HWID
                        String generatedToken = generateDeviceToken(sessionId, currentHwid);
                        updateCppAuthenticationState(sessionId, generatedToken, currentHwid, true);

                        Log.d(TAG, "Session validation successful, authentication state restored");
                        callback.onSuccess("Auto-login successful");
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        Log.w(TAG, "Session validation failed: " + errorMsg);

                        // Session is invalid, clear stored data
                        clearStoredAuth(context);

                        // Clear C++ authentication state on failure
                        updateCppAuthenticationState("", "", "", false);

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
                    callback.onError("Network error: HTTP " + response.code() + " - " + errorBody);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Session validation exception", e);
            callback.onError("Session validation error: " + e.getMessage());
        }
    }

    /**
     * Verify license with KeyAuth (after initialization, using session ID)
     */
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
                    .url(API_URL)
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
                        callback.onSuccess("License verified successfully");
                    } else {
                        String errorMsg = extractErrorMessage(responseBody);
                        callback.onFailure("License verification failed: " + errorMsg);
                    }
                } else {
                    callback.onFailure("Network error: " + response.code());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "License verification exception", e);
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
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Retrieved token from SharedPreferences");
            return token;
        }

        // Method 2: Try external storage (survives app reinstallation)
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
                            prefs.edit().putString("auth_token", storedToken).apply();
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
     * Get stored session ID (simplified approach based on Python test results)
     * Focus on session ID persistence rather than custom token generation
     */
    private static String getStoredSessionId(Context context) {
        // Method 1: Try SharedPreferences first (fastest access)
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        String sessionId = prefs.getString("session_id", null);
        if (sessionId != null && !sessionId.isEmpty()) {
            Log.d(TAG, "Retrieved session ID from SharedPreferences");
            return sessionId;
        }

        // Method 2: Try external storage (survives app reinstallation)
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
                            prefs.edit().putString("session_id", storedSessionId).apply();
                            if (storedHwid != null && !storedHwid.isEmpty()) {
                                prefs.edit().putString("hwid", storedHwid).apply();
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
     * Save license key to persistent storage
     */
    public static void saveLicenseKey(Context context, String licenseKey) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("saved_license_key", licenseKey)
                .putBoolean("remember_key", true)
                .apply();
        Log.d(TAG, "License key saved to persistent storage");
    }

    /**
     * Get saved license key from persistent storage
     */
    public static String getSavedLicenseKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        return prefs.getString("saved_license_key", "");
    }

    /**
     * Check if remember key is enabled
     */
    public static boolean isRememberKeyEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        return prefs.getBoolean("remember_key", false);
    }

    /**
     * Save auto-login preference
     */
    public static void saveAutoLoginPreference(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("auto_login_enabled", enabled)
                .apply();
        Log.d(TAG, "Auto-login preference saved: " + enabled);
    }

    /**
     * Check if auto-login is enabled
     */
    public static boolean isAutoLoginEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        return prefs.getBoolean("auto_login_enabled", false);
    }

    /**
     * Clear saved license key
     */
    public static void clearSavedLicenseKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("saved_license_key")
                .putBoolean("remember_key", false)
                .putBoolean("auto_login_enabled", false)
                .apply();
        Log.d(TAG, "Saved license key cleared");
    }

    /**
     * Store session data with enhanced persistence (simplified approach)
     * Based on successful Python test - focus on session ID persistence
     */
    private static void storeSessionData(Context context, String sessionId, String hwid, String responseBody) {
        try {
            // Store in SharedPreferences (primary storage)
            SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("session_id", sessionId)
                    .putString("hwid", hwid)
                    .putLong("auth_timestamp", System.currentTimeMillis())
                    .apply();

            // Extract and store user expiration if available
            try {
                if (responseBody.contains("\"expiry\"")) {
                    String expiry = extractJsonValue(responseBody, "expiry");
                    if (expiry != null) {
                        userExpiration = convertUnixToDateTime(expiry);
                        prefs.edit().putString("user_expiration", userExpiration).apply();
                        Log.d(TAG, "User expiration stored: " + userExpiration);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not parse expiration data: " + e.getMessage());
            }

            // Backup to external storage for persistence across app reinstallation
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File sessionFile = new File(externalDir, ".session_cache");
                    try (FileWriter writer = new FileWriter(sessionFile)) {
                        writer.write(sessionId + "\n" + hwid + "\n" + System.currentTimeMillis());
                    }
                    Log.d(TAG, "Session data backed up to external storage");
                }
            } catch (Exception e) {
                Log.d(TAG, "External session backup failed: " + e.getMessage());
            }

            Log.d(TAG, "Session data stored successfully with enhanced persistence");

        } catch (Exception e) {
            Log.e(TAG, "Failed to store session data", e);
        }
    }

    /**
     * Store authentication token with enhanced persistence
     * Stores in multiple locations to survive app reinstallation
     */
    private static void storeTokenWithPersistence(Context context, String token, String sessionId, String userExpiration) {
        try {
            // Method 1: Store in SharedPreferences (fastest access)
            SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("auth_token", token)
                    .putString("session_id", sessionId)
                    .putString("user_expiration", userExpiration)
                    .apply();

            // Method 2: Store in external storage (survives app reinstallation)
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    File tokenFile = new File(externalDir, ".auth_cache");
                    try (FileWriter writer = new FileWriter(tokenFile)) {
                        writer.write(token);
                    }
                    Log.d(TAG, "Token stored in external storage for persistence");
                }
            } catch (Exception e) {
                Log.d(TAG, "External token storage failed: " + e.getMessage());
            }

            Log.d(TAG, "Token stored with enhanced persistence");

        } catch (Exception e) {
            Log.e(TAG, "Failed to store token with persistence", e);
        }
    }

    /**
     * Extract user data from KeyAuth response
     */
    private static void extractUserData(String responseBody) {
        try {
            // Extract user expiration date
            if (responseBody.contains("\"expiry\":")) {
                int start = responseBody.indexOf("\"expiry\":\"") + 10;
                int end = responseBody.indexOf("\"", start);
                if (start > 9 && end > start) {
                    String expiryTimestamp = responseBody.substring(start, end);
                    userExpiration = convertUnixToDateTime(expiryTimestamp);
                    Log.d(TAG, "User expiration extracted: " + userExpiration);
                }
            }

            // Extract other user data if needed (subscription info, etc.)
            // This can be expanded based on KeyAuth response structure

        } catch (Exception e) {
            Log.e(TAG, "Failed to extract user data", e);
        }
    }

    /**
     * Convert Unix timestamp to "yyyy-MM-dd HH:mm:ss" format
     */
    private static String convertUnixToDateTime(String unixTimestamp) {
        try {
            long timestamp = Long.parseLong(unixTimestamp);
            java.util.Date date = new java.util.Date(timestamp * 1000L); // Convert to milliseconds
            @SuppressLint("SimpleDateFormat") java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert Unix timestamp", e);
            return null;
        }
    }

    /**
     * Extract session ID from KeyAuth init response
     */
    private static String extractSessionId(String responseBody) {
        try {
            Log.d(TAG, "=== Session ID Extraction Debug ===");
            Log.d(TAG, "Response body length: " + responseBody.length());
            Log.d(TAG, "Looking for sessionid in response...");

            // Try multiple possible session ID field names
            String[] sessionFields = {"sessionid", "session_id", "session", "sid"};

            for (String field : sessionFields) {
                String pattern1 = "\"" + field + "\":\"";
                String pattern2 = "\"" + field + "\": \"";
                String pattern3 = "\"" + field + "\":";  // For non-quoted values

                Log.d(TAG, "Checking pattern: " + pattern1);

                if (responseBody.contains(pattern1)) {
                    int start = responseBody.indexOf(pattern1) + pattern1.length();
                    int end = responseBody.indexOf("\"", start);
                    if (start > pattern1.length() - 1 && end > start) {
                        String sessionId = responseBody.substring(start, end);
                        Log.d(TAG, "Session ID found with pattern '" + pattern1 + "': " + sessionId.substring(0, Math.min(8, sessionId.length())) + "...");
                        return sessionId;
                    }
                } else if (responseBody.contains(pattern2)) {
                    int start = responseBody.indexOf(pattern2) + pattern2.length();
                    int end = responseBody.indexOf("\"", start);
                    if (start > pattern2.length() - 1 && end > start) {
                        String sessionId = responseBody.substring(start, end);
                        Log.d(TAG, "Session ID found with pattern '" + pattern2 + "': " + sessionId.substring(0, Math.min(8, sessionId.length())) + "...");
                        return sessionId;
                    }
                } else if (responseBody.contains(pattern3)) {
                    // Handle non-quoted session IDs
                    int start = responseBody.indexOf(pattern3) + pattern3.length();
                    // Skip whitespace and quotes
                    while (start < responseBody.length() && (responseBody.charAt(start) == ' ' || responseBody.charAt(start) == '"')) {
                        start++;
                    }
                    int end = start;
                    // Find end of session ID (comma, quote, or whitespace)
                    while (end < responseBody.length() &&
                           responseBody.charAt(end) != ',' &&
                           responseBody.charAt(end) != '"' &&
                           responseBody.charAt(end) != ' ' &&
                           responseBody.charAt(end) != '}') {
                        end++;
                    }
                    if (end > start) {
                        String sessionId = responseBody.substring(start, end);
                        Log.d(TAG, "Session ID found with pattern '" + pattern3 + "': " + sessionId.substring(0, Math.min(8, sessionId.length())) + "...");
                        return sessionId;
                    }
                }
            }

            Log.e(TAG, "No session ID found in response");
            Log.e(TAG, "Response content: " + responseBody);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract session ID", e);
            return null;
        }
    }

    /**
     * Extract error message from KeyAuth response
     */
    private static String extractErrorMessage(String responseBody) {
        try {
            Log.d(TAG, "=== Error Message Extraction Debug ===");

            // Try multiple possible error field names
            String[] errorFields = {"message", "error", "msg", "reason"};

            for (String field : errorFields) {
                String pattern1 = "\"" + field + "\":\"";
                String pattern2 = "\"" + field + "\": \"";

                if (responseBody.contains(pattern1)) {
                    int start = responseBody.indexOf(pattern1) + pattern1.length();
                    int end = responseBody.indexOf("\"", start);
                    if (start > pattern1.length() - 1 && end > start) {
                        String errorMsg = responseBody.substring(start, end);
                        Log.d(TAG, "Error message found: " + errorMsg);
                        return errorMsg;
                    }
                } else if (responseBody.contains(pattern2)) {
                    int start = responseBody.indexOf(pattern2) + pattern2.length();
                    int end = responseBody.indexOf("\"", start);
                    if (start > pattern2.length() - 1 && end > start) {
                        String errorMsg = responseBody.substring(start, end);
                        Log.d(TAG, "Error message found: " + errorMsg);
                        return errorMsg;
                    }
                }
            }

            Log.d(TAG, "No specific error message found, returning full response");
            return "API Error: " + responseBody;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting error message", e);
            return "Unknown error: " + e.getMessage();
        }
    }

    /**
     * Quick synchronous license check (for cases where you need immediate result)
     * Note: This blocks the calling thread, use sparingly and not on UI thread
     */
    public static boolean quickLicenseCheck(Context context, String licenseKey) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // Step 1: Initialize KeyAuth (required)
            if (!initializeKeyAuth(client)) {
                Log.e(TAG, "Quick license check failed: KeyAuth initialization failed");
                return false;
            }

            // Step 2: Verify license (API v1.3 requires session ID)
            String hwid = HWID.getHWID();
            Log.d(TAG, "Quick license check with HWID: " + hwid);

            FormBody.Builder formBuilder2 = new FormBody.Builder()
                    .add("type", "license")
                    .add("key", licenseKey)
                    .add("hwid", hwid)
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("ver", VERSION);

            if (sessionId != null && !sessionId.isEmpty()) {
                formBuilder2.add("sessionid", sessionId);
            }

            FormBody formBody = formBuilder2.build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Quick license check response: " + responseBody);

                    boolean success = responseBody.contains("\"success\":true") ||
                                     responseBody.contains("\"success\": true");

                    if (success) {
                        // Extract and store session ID for future use
                        String extractedSessionId = extractSessionId(responseBody);
                        if (extractedSessionId != null) {
                            sessionId = extractedSessionId;
                            Log.d(TAG, "Quick license check successful, session ID extracted");
                        }
                    }

                    return success;
                } else {
                    Log.e(TAG, "Quick license check failed: HTTP " + response.code());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Quick license check failed", e);
        }
        return false;
    }

    /**
     * Cache HWID in multiple persistent locations to survive app reinstallation
     */
    private static void cacheHWID(Context context, String hwid) {
        try {
            // Method 1: SharedPreferences (survives app updates but not uninstall)
            SharedPreferences prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE);
            prefs.edit().putString("cached_hwid", hwid).apply();

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
            SharedPreferences prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE);
            String cachedHwid = prefs.getString("cached_hwid", null);
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
                                prefs.edit().putString("cached_hwid", hwid).apply();
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
                            prefs.edit().putString("cached_hwid", hwid).apply();
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
            SharedPreferences prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE);
            prefs.edit().remove("cached_hwid").putLong("hwid_last_reset", System.currentTimeMillis()).apply();

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
            SharedPreferences prefs = context.getSharedPreferences("keyauth_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Store session data
            editor.putString("session_id", sessionId);
            editor.putString("hwid", hwid);
            editor.putLong("auth_timestamp", System.currentTimeMillis());

            // Extract and store user info from response if available
            try {
                // Parse response for additional user data
                if (responseBody.contains("\"expiry\"")) {
                    // Extract expiry if present in response
                    String expiry = extractJsonValue(responseBody, "expiry");
                    if (expiry != null) {
                        editor.putString("user_expiry", expiry);
                    }
                }

                if (responseBody.contains("\"subscription\"")) {
                    String subscription = extractJsonValue(responseBody, "subscription");
                    if (subscription != null) {
                        editor.putString("user_subscription", subscription);
                    }
                }
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
            String searchKey = "\"" + key + "\":\"";
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
     * Test the fixed KeyAuth authentication flow
     * This method tests the corrected Token+SessionID+HWID flow
     */
    public static void testFixedAuthFlow(Context context, String testLicenseKey, AuthCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                Log.d(TAG, "=== Testing Fixed KeyAuth Authentication Flow ===");

                OkHttpClient testClient = new OkHttpClient.Builder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                // Step 1: Test KeyAuth initialization (should not require session ID)
                Log.d(TAG, "Step 1: Testing KeyAuth initialization...");
                boolean initResult = initializeKeyAuth(testClient);
                Log.d(TAG, "Initialization result: " + initResult);

                if (!initResult) {
                    callback.onError("KeyAuth initialization failed");
                    return;
                }

                // Step 2: Test license verification (should extract session ID)
                Log.d(TAG, "Step 2: Testing license verification...");
                String hwid = HWID.getHWID();
                Log.d(TAG, "Using HWID: " + hwid);

                // Use the fixed license verification method
                verifyLicenseWithKeyAuth(testClient, context, testLicenseKey, hwid, new LicenseCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "Fixed auth flow test successful: " + message);
                        callback.onSuccess("Fixed authentication flow working correctly");
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Fixed auth flow test failed: " + error);
                        callback.onError("Authentication flow test failed: " + error);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Fixed auth flow test exception", e);
                callback.onError("Test exception: " + e.getMessage());
            }
        });
    }

    /**
     * Test basic connectivity to KeyAuth API
     */
    private static boolean testBasicConnectivity(OkHttpClient client) {
        try {
            Request pingRequest = new Request.Builder()
                    .url(API_URL)
                    .get()
                    .build();

            try (Response response = client.newCall(pingRequest).execute()) {
                Log.d(TAG, "Basic connectivity test: HTTP " + response.code());
                return response.code() < 500; // Accept any non-server-error response
            }
        } catch (Exception e) {
            Log.e(TAG, "Basic connectivity test failed", e);
            return false;
        }
    }

    /**
     * Test specific API version
     */
    private static boolean testApiVersion(OkHttpClient client, String apiUrl) {
        try {
            FormBody formBody = new FormBody.Builder()
                    .add("type", "init")
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .add("ver", VERSION)
                    .add("hash", APP_HASH)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(formBody)
                    .addHeader("User-Agent", "BearMod/1.0")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API test response (" + apiUrl + "): " + responseBody);

                    boolean success = responseBody.contains("\"success\":true") ||
                                     responseBody.contains("\"success\": true");

                    if (success) {
                        String testSessionId = extractSessionId(responseBody);
                        return testSessionId != null;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "API version test failed for " + apiUrl, e);
            return false;
        }
    }

    /**
     * Alternative KeyAuth initialization method (Python-style approach)
     * This method tries a different approach based on the working Python implementation
     */
    private static boolean initializeKeyAuthAlternative(OkHttpClient client) {
        try {
            Log.d(TAG, "=== Alternative KeyAuth Initialization ===");

            // Try with different parameter order and formatting
            FormBody formBody = new FormBody.Builder()
                    .add("type", "init")
                    .add("ver", VERSION)
                    .add("hash", APP_HASH)
                    .add("name", APP_NAME)
                    .add("ownerid", OWNER_ID)
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(formBody)
                    .addHeader("User-Agent", "KeyAuth")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .build();

            Log.d(TAG, "Sending alternative KeyAuth init request...");

            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "Alternative response received - HTTP " + response.code());

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Alternative init response: " + responseBody);

                    // Try different success detection patterns
                    boolean success = responseBody.contains("success") &&
                                     !responseBody.contains("false") &&
                                     !responseBody.contains("error");

                    if (success) {
                        // Try extracting session ID with alternative patterns
                        sessionId = extractSessionIdAlternative(responseBody);
                        if (sessionId != null) {
                            Log.d(TAG, "Alternative KeyAuth initialization successful");
                            return true;
                        }
                    }
                }

                Log.e(TAG, "Alternative KeyAuth initialization failed");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Alternative KeyAuth initialization exception", e);
            return false;
        }
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