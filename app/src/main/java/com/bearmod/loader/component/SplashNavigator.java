package com.bearmod.loader.component;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.server.SimpleLicenseVerifier;

/**
 * SplashNavigator - Handles authentication flow and navigation decisions
 *
 * Extracted from SplashActivity.java to separate navigation logic from UI concerns.
 * Manages authentication checks, network validation, and navigation flow.
 *
 * Migrated from com.bearmod.activity.SplashActivity (449 LOC → focused navigation component)
 */
public class SplashNavigator {
    private static final String TAG = "SplashNavigator";

    // Navigation callbacks
    public interface NavigationCallback {
        void onNavigateToLogin();
        void onNavigateToMain();
        void onShowError(String title, String message);
        void updateStatus(String message);
    }

    private final Context context;
    private final NavigationCallback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Navigation guards
    private boolean hasNavigated = false;
    private Runnable authTimeoutTask;
    private Runnable navigateToLoginTask;

    public SplashNavigator(Context context, NavigationCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Start the authentication and navigation flow
     */
    public void startAuthenticationFlow() {
        Logx.d("Starting authentication flow");

        // Check if user has valid stored authentication
        if (hasValidStoredCredentials() && isAutoLoginEnabled()) {
            callback.updateStatus("Checking auth...");
            attemptAutoLogin();
        } else {
            Logx.d("No valid stored credentials - navigating to login");
            callback.updateStatus("Authentication required...");
            navigateToLoginDelayed(800);
        }
    }

    /**
     * Check if user has valid stored license key
     */
    private boolean hasValidStoredCredentials() {
        // This would need to be implemented based on your login activity logic
        // For now, delegate to a simple check
        try {
            return com.bearmod.activity.LoginActivity.hasValidKey(context) &&
                   SimpleLicenseVerifier.isAutoLoginEnabled(context);
        } catch (Exception e) {
            Logx.w("Error checking stored credentials: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if auto-login is enabled
     */
    private boolean isAutoLoginEnabled() {
        try {
            return SimpleLicenseVerifier.isAutoLoginEnabled(context);
        } catch (Exception e) {
            Logx.w("Error checking auto-login preference: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempt automatic login with stored credentials
     */
    private void attemptAutoLogin() {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            callback.updateStatus("Offline - please login");
            navigateToLoginDelayed(500);
            return;
        }

        // Start timeout watchdog
        startAuthTimeoutWatchdog();

        // Attempt auto-login
        SimpleLicenseVerifier.autoLogin(context, new SimpleLicenseVerifier.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                mainHandler.post(() -> {
                    cancelAuthTimeoutWatchdog();
                    cancelLoginNavigateTask();
                    Logx.d("Auto-login successful");
                    callback.updateStatus("Authentication verified!");
                    navigateToMainDelayed(600);
                });
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    cancelAuthTimeoutWatchdog();
                    Logx.d("Auto-login failed: " + error);
                    callback.updateStatus("Authentication required...");
                    navigateToLoginDelayed(800);
                });
            }
        });
    }

    /**
     * Check if network is available
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } catch (Exception e) {
            Logx.w("Network check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Navigate to login activity after delay
     */
    private void navigateToLoginDelayed(long delayMs) {
        if (hasNavigated) return;

        navigateToLoginTask = () -> {
            if (!hasNavigated) {
                hasNavigated = true;
                callback.onNavigateToLogin();
            }
        };

        mainHandler.postDelayed(navigateToLoginTask, delayMs);
    }

    /**
     * Navigate to main activity after delay
     */
    private void navigateToMainDelayed(long delayMs) {
        mainHandler.postDelayed(() -> {
            if (!hasNavigated) {
                hasNavigated = true;
                callback.onNavigateToMain();
            }
        }, delayMs);
    }

    /**
     * Start authentication timeout watchdog
     */
    private void startAuthTimeoutWatchdog() {
        cancelAuthTimeoutWatchdog();

        authTimeoutTask = () -> {
            if (!hasNavigated) {
                Logx.w("Auto-login timeout reached");
                callback.updateStatus("Authentication timeout - please login");
                navigateToLoginDelayed(0);
            }
        };

        mainHandler.postDelayed(authTimeoutTask, 12_000); // 12s timeout
    }

    /**
     * Cancel authentication timeout
     */
    private void cancelAuthTimeoutWatchdog() {
        if (authTimeoutTask != null) {
            mainHandler.removeCallbacks(authTimeoutTask);
            authTimeoutTask = null;
        }
    }

    /**
     * Cancel login navigation task
     */
    private void cancelLoginNavigateTask() {
        if (navigateToLoginTask != null) {
            mainHandler.removeCallbacks(navigateToLoginTask);
            navigateToLoginTask = null;
        }
    }

    /**
     * Handle successful login from login activity
     */
    public void handleLoginSuccess() {
        cancelAuthTimeoutWatchdog();
        cancelLoginNavigateTask();
        callback.onNavigateToMain();
    }

    /**
     * Handle login failure/cancellation
     */
    public void handleLoginFailure() {
        cancelAuthTimeoutWatchdog();
        // App will finish from the calling activity
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        cancelAuthTimeoutWatchdog();
        cancelLoginNavigateTask();
        hasNavigated = false;
    }
}