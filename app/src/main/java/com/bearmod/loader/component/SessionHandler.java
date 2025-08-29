package com.bearmod.loader.component;

import android.util.Log;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.ResourceProvider;

/**
 * SessionHandler - Bridges authentication components with session management
 * Extracted from LoginActivity to coordinate session state and preferences
 *
 * Responsibilities:
 * - Session state coordination between components
 * - User preference persistence and retrieval
 * - Authentication result handling
 * - Session lifecycle management
 */
public class SessionHandler {

    private static final String TAG = "SessionHandler";

    private final ResourceProvider rp;
    private final SessionManager sessionManager;
    private SessionHandlerCallback callback;

    // State tracking
    private boolean rememberKey = false;
    private boolean autoLogin = false;
    private String currentLicenseKey;

    /**
     * Interface for session handler events
     */
    public interface SessionHandlerCallback {
        void onAuthenticationSuccess(String userId);
        void onAuthenticationFailed(String error);
        void onSessionExpired();
        void onPreferencesLoaded(boolean rememberKey, boolean autoLogin, String savedKey);
    }

    public SessionHandler(ResourceProvider rp, SessionManager sessionManager) {
        this.rp = rp;
        this.sessionManager = sessionManager;
    }

    /**
     * Set session handler callback
     */
    public void setCallback(SessionHandlerCallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize session handler and load preferences
     */
    public void initialize() {
        try {
            // Load user preferences
            loadUserPreferences();

            // Set session manager callback
            sessionManager.setCallback(new SessionManager.SessionCallback() {
                @Override
                public void onSessionInitialized() {
                    // Session initialized successfully
                    Logx.d("Session manager initialized");
                }

                @Override
                public void onLoginSuccess(String userId) {
                    handleLoginSuccess(userId);
                }

                @Override
                public void onLoginFailed(String reason) {
                    handleLoginFailed(reason);
                }

                @Override
                public void onLogout() {
                    handleLogout();
                }
            });

            // Initialize session manager
            sessionManager.initialize();

            Logx.d("SessionHandler initialized");

        } catch (Exception e) {
            Logx.e("SessionHandler initialization failed", e);
        }
    }

    /**
     * Load user preferences from storage
     */
    private void loadUserPreferences() {
        try {
            String savedKey = "";
            boolean rememberEnabled = sessionManager.isRememberKeyEnabled();
            boolean autoLoginEnabled = sessionManager.isAutoLoginEnabled();

            if (rememberEnabled) {
                savedKey = sessionManager.getSavedLicenseKey();
                if (savedKey.isEmpty()) {
                    Logx.w("Remember key enabled but no saved key found");
                    rememberEnabled = false;
                }
            }

            this.rememberKey = rememberEnabled;
            this.autoLogin = autoLoginEnabled;

            Logx.d("Loaded user preferences - remember: " + rememberEnabled + ", autoLogin: " + autoLoginEnabled);

            // Notify callback about loaded preferences
            if (callback != null) {
                callback.onPreferencesLoaded(rememberEnabled, autoLoginEnabled, savedKey);
            }

        } catch (Exception e) {
            Logx.e("Error loading user preferences", e);
            // Reset to defaults on error
            this.rememberKey = false;
            this.autoLogin = false;
        }
    }

    /**
     * Handle login attempt with license key
     */
    public void handleLoginAttempt(String licenseKey, boolean remember, boolean autoLogin) {
        try {
            this.currentLicenseKey = licenseKey;
            this.rememberKey = remember;
            this.autoLogin = autoLogin;

            Logx.d("Handling login attempt for license key");

            // Perform login through session manager
            sessionManager.login(licenseKey);

        } catch (Exception e) {
            Logx.e("Error handling login attempt", e);
            if (callback != null) {
                callback.onAuthenticationFailed("Login error: " + e.getMessage());
            }
        }
    }

    /**
     * Handle successful login
     */
    private void handleLoginSuccess(String userId) {
        try {
            Logx.d("Login successful for user: " + userId);

            // Save user preferences
            saveUserPreferences();

            // Notify callback
            if (callback != null) {
                callback.onAuthenticationSuccess(userId);
            }

        } catch (Exception e) {
            Logx.e("Error handling login success", e);
        }
    }

    /**
     * Handle failed login
     */
    private void handleLoginFailed(String reason) {
        try {
            Logx.w("Login failed: " + reason);

            // Clear current license key
            currentLicenseKey = null;

            // Notify callback
            if (callback != null) {
                callback.onAuthenticationFailed(reason);
            }

        } catch (Exception e) {
            Logx.e("Error handling login failure", e);
        }
    }

    /**
     * Handle logout
     */
    private void handleLogout() {
        try {
            Logx.d("User logged out");

            // Clear local state
            currentLicenseKey = null;
            rememberKey = false;
            autoLogin = false;

        } catch (Exception e) {
            Logx.e("Error handling logout", e);
        }
    }

    /**
     * Handle session expiration
     */
    private void handleSessionExpired() {
        try {
            Logx.w("Session expired");

            // Clear local state
            currentLicenseKey = null;

            // Notify callback
            if (callback != null) {
                callback.onSessionExpired();
            }

        } catch (Exception e) {
            Logx.e("Error handling session expiration", e);
        }
    }

    /**
     * Save user preferences after successful login
     */
    private void saveUserPreferences() {
        try {
            // Save license key if remember is enabled
            if (rememberKey && currentLicenseKey != null) {
                sessionManager.saveLicenseKey(currentLicenseKey, true);
            }

            // Save auto-login preference
            sessionManager.saveAutoLoginPreference(autoLogin);

            Logx.d("User preferences saved - remember: " + rememberKey + ", autoLogin: " + autoLogin);

        } catch (Exception e) {
            Logx.e("Error saving user preferences", e);
        }
    }

    /**
     * Update remember key preference
     */
    public void updateRememberKeyPreference(boolean remember) {
        this.rememberKey = remember;
        Logx.d("Remember key preference updated: " + remember);
    }

    /**
     * Update auto-login preference
     */
    public void updateAutoLoginPreference(boolean autoLogin) {
        this.autoLogin = autoLogin;
        Logx.d("Auto-login preference updated: " + autoLogin);
    }

    /**
     * Check if user is currently logged in
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return sessionManager.getUserId();
    }

    /**
     * Get current license key
     */
    public String getCurrentLicenseKey() {
        return currentLicenseKey;
    }

    /**
     * Check if remember key is enabled
     */
    public boolean isRememberKeyEnabled() {
        return rememberKey;
    }

    /**
     * Check if auto-login is enabled
     */
    public boolean isAutoLoginEnabled() {
        return autoLogin;
    }

    /**
     * Perform logout
     */
    public void logout() {
        sessionManager.logout();
    }

    /**
     * Refresh session validity
     */
    public void refreshSession() {
        sessionManager.refreshSession();
    }

    /**
     * Get session info for debugging
     */
    public String getSessionInfo() {
        return sessionManager.getSessionInfo();
    }

    /**
     * Check if user has valid stored authentication
     */
    public boolean hasValidStoredAuth() {
        try {
            return sessionManager.hasValidStoredAuth();
        } catch (Exception e) {
            Logx.e("Error checking stored auth", e);
            return false;
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (sessionManager != null) {
            sessionManager.cleanup();
        }
        callback = null;
        currentLicenseKey = null;
        Logx.d("SessionHandler cleanup completed");
    }
}