package com.bearmod.loader.component;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.ResourceProvider;

/**
 * LoginManager - Handles authentication logic and KeyAuth integration
 * Extracted from LoginActivity to separate business logic from UI
 *
 * Responsibilities:
 * - License verification flow coordination
 * - KeyAuth API integration
 * - Timeout handling and error management
 * - Authentication state management
 */
public class LoginManager {

    private static final String TAG = "LoginManager";

    private final ResourceProvider rp;
    private final Handler mainHandler;
    private LoginCallback callback;

    // Timeout handling
    private final Handler verifyWatchdog = new Handler(Looper.getMainLooper());
    private Runnable verifyTimeoutTask;
    private static final long VERIFICATION_TIMEOUT_MS = 12_000; // 12 seconds

    // State tracking
    private boolean isVerifying = false;
    private String currentLicenseKey;

    /**
     * Interface for login events
     */
    public interface LoginCallback {
        void onVerificationStarted(String licenseKey);
        void onVerificationSuccess(String message);
        void onVerificationFailed(String error);
        void onVerificationTimeout();
    }

    public LoginManager(ResourceProvider rp) {
        this.rp = rp;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Set login callback for events
     */
    public void setCallback(LoginCallback callback) {
        this.callback = callback;
    }

    /**
     * Attempt license verification with KeyAuth
     */
    public void attemptLicenseVerification(String licenseKey) {
        if (isVerifying) {
            Logx.w("Verification already in progress");
            return;
        }

        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            notifyFailure("License key cannot be empty");
            return;
        }

        this.currentLicenseKey = licenseKey.trim();
        this.isVerifying = true;

        Logx.d("Starting license verification for key: " + maskLicenseKey(licenseKey));

        // Notify UI that verification started
        if (callback != null) {
            mainHandler.post(() -> callback.onVerificationStarted(licenseKey));
        }

        // Start timeout watchdog
        startVerificationTimeout();

        // Perform license verification
        performLicenseVerification(licenseKey);
    }

    /**
     * Perform the actual license verification with KeyAuth
     */
    private void performLicenseVerification(String licenseKey) {
        try {
            SimpleLicenseVerifier.verifyLicense(rp.app(), licenseKey, new SimpleLicenseVerifier.LicenseCallback() {
                @Override
                public void onSuccess(String message) {
                    mainHandler.post(() -> handleVerificationSuccess(message));
                }

                @Override
                public void onFailure(String error) {
                    mainHandler.post(() -> handleVerificationFailure(error));
                }
            });

        } catch (Exception e) {
            Logx.e("License verification error", e);
            handleVerificationFailure("Verification error: " + e.getMessage());
        }
    }

    /**
     * Handle successful license verification
     */
    private void handleVerificationSuccess(String message) {
        // Cancel timeout
        cancelVerificationTimeout();

        isVerifying = false;
        currentLicenseKey = null;

        Logx.d("License verification successful: " + message);

        if (callback != null) {
            callback.onVerificationSuccess(message);
        }
    }

    /**
     * Handle failed license verification
     */
    private void handleVerificationFailure(String error) {
        // Cancel timeout
        cancelVerificationTimeout();

        isVerifying = false;
        String maskedKey = currentLicenseKey != null ? maskLicenseKey(currentLicenseKey) : "unknown";
        currentLicenseKey = null;

        Logx.w("License verification failed for key " + maskedKey + ": " + error);

        notifyFailure(error);
    }

    /**
     * Start verification timeout watchdog
     */
    private void startVerificationTimeout() {
        cancelVerificationTimeout(); // Ensure no existing timeout

        verifyTimeoutTask = () -> {
            if (isVerifying) {
                Logx.w("License verification timeout");
                isVerifying = false;
                currentLicenseKey = null;

                if (callback != null) {
                    mainHandler.post(() -> callback.onVerificationTimeout());
                }
            }
        };

        verifyWatchdog.postDelayed(verifyTimeoutTask, VERIFICATION_TIMEOUT_MS);
    }

    /**
     * Cancel verification timeout
     */
    private void cancelVerificationTimeout() {
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
            verifyTimeoutTask = null;
        }
    }

    /**
     * Check if user has valid stored authentication
     */
    public boolean hasValidStoredAuth() {
        try {
            return SimpleLicenseVerifier.hasValidStoredAuth(rp.app());
        } catch (Exception e) {
            Logx.e("Error checking stored auth", e);
            return false;
        }
    }

    /**
     * Get saved license key from storage
     */
    public String getSavedLicenseKey() {
        try {
            return SimpleLicenseVerifier.getSavedLicenseKey(rp.app());
        } catch (Exception e) {
            Logx.w("Error getting saved license key", e);
            return "";
        }
    }

    /**
     * Save license key to storage
     */
    public void saveLicenseKey(String licenseKey, boolean remember) {
        try {
            SimpleLicenseVerifier.setRememberKeyPreference(rp.app(), remember);
            if (remember && licenseKey != null) {
                SimpleLicenseVerifier.saveLicenseKey(rp.app(), licenseKey);
            }
        } catch (Exception e) {
            Logx.w("Error saving license key", e);
        }
    }

    /**
     * Save auto-login preference
     */
    public void saveAutoLoginPreference(boolean autoLogin) {
        try {
            SimpleLicenseVerifier.saveAutoLoginPreference(rp.app(), autoLogin);
        } catch (Exception e) {
            Logx.w("Error saving auto-login preference", e);
        }
    }

    /**
     * Check if remember key is enabled
     */
    public boolean isRememberKeyEnabled() {
        try {
            return SimpleLicenseVerifier.isRememberKeyEnabled(rp.app());
        } catch (Exception e) {
            Logx.w("Error checking remember key preference", e);
            return false;
        }
    }

    /**
     * Check if auto-login is enabled
     */
    public boolean isAutoLoginEnabled() {
        try {
            return SimpleLicenseVerifier.isAutoLoginEnabled(rp.app());
        } catch (Exception e) {
            Logx.w("Error checking auto-login preference", e);
            return false;
        }
    }

    /**
     * Perform auto-login if enabled
     */
    public void performAutoLogin() {
        try {
            if (isAutoLoginEnabled()) {
                String savedKey = getSavedLicenseKey();
                if (!savedKey.isEmpty()) {
                    Logx.d("Performing auto-login with saved key");
                    attemptLicenseVerification(savedKey);
                } else {
                    Logx.w("Auto-login enabled but no saved key found");
                }
            }
        } catch (Exception e) {
            Logx.e("Auto-login error", e);
        }
    }

    /**
     * Check if verification is currently in progress
     */
    public boolean isVerifying() {
        return isVerifying;
    }

    /**
     * Get current license key being verified
     */
    public String getCurrentLicenseKey() {
        return currentLicenseKey;
    }

    /**
     * Mask license key for logging (show first 4 and last 4 characters)
     */
    private String maskLicenseKey(String licenseKey) {
        if (licenseKey == null || licenseKey.length() <= 8) {
            return "****";
        }

        String firstFour = licenseKey.substring(0, Math.min(4, licenseKey.length()));
        String lastFour = licenseKey.substring(Math.max(0, licenseKey.length() - 4));
        return firstFour + "****" + lastFour;
    }

    /**
     * Notify failure to callback
     */
    private void notifyFailure(String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onVerificationFailed(error));
        }
    }

    /**
     * Check if license key format is valid
     */
    public static boolean isValidLicenseKeyFormat(String licenseKey) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }
        // Basic format validation - can be enhanced
        return licenseKey.length() >= 8 && licenseKey.length() <= 64;
    }

    /**
     * Get user-friendly error message
     */
    public static String getErrorMessage(String error) {
        if (error == null) return "Unknown error";

        // Map technical errors to user-friendly messages
        if (error.contains("network") || error.contains("timeout")) {
            return "Network connection error. Please check your internet connection.";
        } else if (error.contains("invalid") || error.contains("license")) {
            return "Invalid license key. Please check your key and try again.";
        } else if (error.contains("expired")) {
            return "License has expired. Please renew your subscription.";
        } else {
            return "Authentication failed. Please try again.";
        }
    }

    /**
     * Handle successful login with preferences
     */
    public void handleLoginSuccess(String licenseKey, boolean rememberKey, boolean autoLogin) {
        try {
            saveLicenseKey(licenseKey, rememberKey);
            saveAutoLoginPreference(autoLogin);
            Logx.d("Login success handled - remember: " + rememberKey + ", auto: " + autoLogin);
        } catch (Exception e) {
            Logx.w("Error handling login success", e);
        }
    }

    /**
     * Attempt auto-login with callback
     */
    public void attemptAutoLogin(LoginCallback callback) {
        this.callback = callback;
        performAutoLogin();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        cancelVerificationTimeout();
        callback = null;
        isVerifying = false;
        currentLicenseKey = null;
        Logx.d("LoginManager cleanup completed");
    }
}