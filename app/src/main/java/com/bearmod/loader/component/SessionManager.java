package com.bearmod.loader.component;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Manages user session state and preferences
 * Provides a clean interface for session-related operations
 */
public class SessionManager {

    private static final String PREFS_NAME = "bearmod_session";
    private static final String KEY_LICENSE_KEY = "license_key";
    private static final String KEY_REMEMBER_KEY = "remember_key";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LAST_LOGIN = "last_login";

    private final Context context;
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if remember key is enabled
     */
    public boolean isRememberKeyEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_KEY, false);
    }

    /**
     * Check if auto-login is enabled
     */
    public boolean isAutoLoginEnabled() {
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    /**
     * Get saved license key
     */
    public String getSavedLicenseKey() {
        return prefs.getString(KEY_LICENSE_KEY, "");
    }

    /**
     * Save license key with preferences
     */
    public void saveLicenseKey(String licenseKey, boolean remember) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMEMBER_KEY, remember);
        if (remember && licenseKey != null) {
            editor.putString(KEY_LICENSE_KEY, licenseKey);
        } else {
            editor.remove(KEY_LICENSE_KEY);
        }
        editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Save auto-login preference
     */
    public void saveAutoLoginPreference(boolean autoLogin) {
        prefs.edit().putBoolean(KEY_AUTO_LOGIN, autoLogin).apply();
    }

    /**
     * Check if user has valid stored authentication
     */
    public boolean hasValidStoredAuth() {
        return isRememberKeyEnabled() && !getSavedLicenseKey().isEmpty();
    }

    /**
     * Clear all session data
     */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    /**
     * Get last login timestamp
     */
    public long getLastLoginTime() {
        return prefs.getLong(KEY_LAST_LOGIN, 0);
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    /**
     * Session callback interface
     */
    public interface SessionCallback {
        void onSessionInitialized();
        void onLoginSuccess(String userId);
        void onLoginFailed(String error);
        void onLogout();
    }

    private SessionCallback callback;

    /**
     * Set session callback
     */
    public void setCallback(SessionCallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize session manager
     */
    public void initialize() {
        // Initialize session state
        if (callback != null) {
            callback.onSessionInitialized();
        }
    }

    /**
     * Login with license key
     */
    public void login(String licenseKey) {
        // Simulate login process
        if (licenseKey != null && !licenseKey.isEmpty()) {
            saveLicenseKey(licenseKey, true);
            if (callback != null) {
                callback.onLoginSuccess(getUserId());
            }
        } else {
            if (callback != null) {
                callback.onLoginFailed("Invalid license key");
            }
        }
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return hasValidStoredAuth();
    }

    /**
     * Logout user
     */
    public void logout() {
        clearSession();
        if (callback != null) {
            callback.onLogout();
        }
    }

    /**
     * Refresh session
     */
    public void refreshSession() {
        // Refresh session logic
        if (isLoggedIn()) {
            // Session is still valid
        } else {
            logout();
        }
    }

    /**
     * Get session information
     */
    public String getSessionInfo() {
        return "User: " + getUserId() + ", Valid: " + isLoggedIn();
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        callback = null;
    }
}