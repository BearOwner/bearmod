package com.bearmod.loader.component;

import android.content.Context;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.ResourceProvider;

/**
 * LoginFacade - Coordinator for LoginActivity components
 *
 * Orchestrates all login components and provides clean interface
 * for the refactored LoginActivity. Maintains backward compatibility
 * while using the new modular architecture.
 *
 * Created as part of Phase 8: LoginActivity Refactor
 */
public class LoginFacade {
    private static final String TAG = "LoginFacade";

    // Core components
    private final AuthUI authUI;
    private final SessionHandler sessionHandler;
    private final LoginManager loginManager;

    // Context and callbacks
    private final AppCompatActivity activity;
    private final LoginCallback callback;

    // Permission manager
    private com.bearmod.loader.utilities.PermissionManager permissionManager;

    /**
     * Login callback interface for activity communication
     */
    public interface LoginCallback {
        void onLoginSuccess();
        void onLoginFailure(String error);
        void onBackPressed();
        void onPermissionResult(int requestCode, int resultCode, android.content.Intent data);
    }

    public LoginFacade(AppCompatActivity activity, View rootView, LoginCallback callback) {
        this.activity = activity;
        this.callback = callback;

        // Create ResourceProvider for modular components
        ResourceProvider rp = ResourceProvider.from(activity);

        // Create SessionManager
        SessionManager sessionManager = new SessionManager(activity);

        // Initialize components with ResourceProvider
        this.authUI = new AuthUI(rp);
        this.authUI.initializeViews(rootView);
        this.sessionHandler = new SessionHandler(rp, sessionManager);
        this.loginManager = new LoginManager(rp);

        // Initialize permission manager
        this.permissionManager = com.bearmod.loader.utilities.PermissionManager.Companion.getInstance(activity);

        setupUICallbacks();
        setupBackPressedHandler();
    }

    /**
     * Setup UI callbacks
     */
    private void setupUICallbacks() {
        authUI.setCallback(new AuthUI.AuthUICallback() {
            @Override
            public void onLoginAttempt(String licenseKey) {
                handleLoginAttempt(licenseKey);
            }

            @Override
            public void onRememberKeyChanged(boolean remember) {
                // Handle remember key preference change
            }

            @Override
            public void onAutoLoginChanged(boolean autoLogin) {
                // Handle auto-login preference change
            }

            @Override
            public void onBackPressed() {
                callback.onBackPressed();
            }
        });
    }

    /**
     * Setup back pressed handler
     */
    private void setupBackPressedHandler() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                callback.onBackPressed();
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, backPressedCallback);
    }

    /**
     * Initialize the login interface
     */
    public void initializeLoginInterface() {
        Logx.d("LOGIN_FACADE_INITIALIZE");

        // Load saved preferences and license key
        authUI.loadSavedLicenseKey();

        // Check and prompt permissions
        checkAndPromptPermissions();
    }

    /**
     * Handle login attempt
     */
    private void handleLoginAttempt(String licenseKey) {
        // Validate license key format
        if (!LoginManager.isValidLicenseKeyFormat(licenseKey)) {
            authUI.showError("Invalid license key format");
            return;
        }

        // Set loading state
        authUI.setLoadingState(true);
        authUI.showStatus("Verifying license with KeyAuth...", false);

        // Set callback on LoginManager first
        loginManager.setCallback(new LoginManager.LoginCallback() {
            @Override
            public void onVerificationStarted(String licenseKey) {
                // Verification started - UI already shows loading state
                Logx.d("License verification started for: " + licenseKey);
            }

            @Override
            public void onVerificationSuccess(String message) {
                handleLoginSuccess(licenseKey, message);
            }

            @Override
            public void onVerificationFailed(String error) {
                handleLoginFailure(error);
            }

            @Override
            public void onVerificationTimeout() {
                handleLoginTimeout();
            }
        });

        // Attempt license verification
        loginManager.attemptLicenseVerification(licenseKey);
    }

    /**
     * Handle successful login
     */
    private void handleLoginSuccess(String licenseKey, String message) {
        try {
            // Update UI
            authUI.setLoadingState(false);
            authUI.showSuccess("License verification successful!");

            // Save user preferences
            boolean rememberKey = authUI.isRememberKeyEnabled();
            boolean autoLogin = authUI.isAutoLoginEnabled();
            loginManager.handleLoginSuccess(licenseKey, rememberKey, autoLogin);

            // Notify callback
            callback.onLoginSuccess();

        } catch (Exception e) {
            Logx.e("LOGIN_FACADE_SUCCESS_HANDLE_ERROR", e);
            handleLoginFailure("Failed to save login data: " + e.getMessage());
        }
    }

    /**
     * Handle login failure
     */
    private void handleLoginFailure(String error) {
        authUI.setLoadingState(false);
        String userFriendlyError = LoginManager.getErrorMessage(error);
        authUI.showError("License verification failed: " + userFriendlyError);
        callback.onLoginFailure(error);
    }

    /**
     * Handle login timeout
     */
    private void handleLoginTimeout() {
        authUI.setLoadingState(false);
        authUI.showError("Verification timed out. Please try again.");
        callback.onLoginFailure("Verification timeout");
    }

    /**
     * Check and prompt for required permissions
     */
    private void checkAndPromptPermissions() {
        try {
            // Check overlay permission
            com.bearmod.loader.utilities.PermissionManager.PermissionStatus overlay =
                permissionManager.checkOverlayPermission();
            if (!overlay.isGranted()) {
                showPermissionDialog(
                    "Please allow permission: Floating",
                    "BearMod needs overlay permission to display its floating interface over the game.",
                    com.bearmod.loader.utilities.PermissionManager.REQUEST_OVERLAY_PERMISSION
                );
                return;
            }

            // Check storage permission
            com.bearmod.loader.utilities.PermissionManager.PermissionStatus storage =
                permissionManager.checkStoragePermission();
            if (!storage.isGranted()) {
                showPermissionDialog(
                    "File Access Permission",
                    "This app needs permission to access files needed for game data validation. Please grant permission to continue.",
                    com.bearmod.loader.utilities.PermissionManager.REQUEST_STORAGE_PERMISSION
                );
            }

        } catch (Throwable t) {
            Logx.w("LOGIN_FACADE_PERMISSION_CHECK_FAIL", t);
        }
    }

    /**
     * Show permission dialog
     */
    private void showPermissionDialog(String title, String message, int requestCode) {
        new androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Grant", (d, w) -> {
                if (requestCode == com.bearmod.loader.utilities.PermissionManager.REQUEST_OVERLAY_PERMISSION) {
                    permissionManager.requestOverlayPermission(activity, requestCode);
                } else if (requestCode == com.bearmod.loader.utilities.PermissionManager.REQUEST_STORAGE_PERMISSION) {
                    permissionManager.requestStoragePermission(activity, requestCode);
                }
            })
            .setNegativeButton("Not now", null)
            .show();
    }

    /**
     * Handle permission result
     */
    public void handlePermissionResult(int requestCode, int resultCode, android.content.Intent data) {
        try {
            if (permissionManager != null) {
                permissionManager.handlePermissionResult(requestCode, resultCode, null);
            }
            // Re-check permissions after result
            checkAndPromptPermissions();
        } catch (Throwable t) {
            Logx.w("LOGIN_FACADE_PERMISSION_RESULT_FAIL", t);
        }
    }

    /**
     * Check if user has valid stored authentication
     */
    public boolean hasValidStoredAuth() {
        return sessionHandler.hasValidStoredAuth();
    }

    /**
     * Attempt auto-login
     */
    public void attemptAutoLogin(LoginManager.LoginCallback loginCallback) {
        loginManager.attemptAutoLogin(loginCallback);
    }

    /**
     * Get AuthUI component for direct access if needed
     */
    public AuthUI getAuthUI() {
        return authUI;
    }

    /**
     * Get SessionHandler component for direct access if needed
     */
    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    /**
     * Get LoginManager component for direct access if needed
     */
    public LoginManager getLoginManager() {
        return loginManager;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        loginManager.cleanup();
        authUI.setLoadingState(false);
    }
}