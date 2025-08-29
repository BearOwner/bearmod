package com.bearmod.loader.component;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bearmod.loader.utilities.Logx;

/**
 * SplashFacade - Coordinator for SplashActivity components
 *
 * Orchestrates all splash screen components and provides clean interface
 * for the refactored SplashActivity. Maintains backward compatibility
 * while using the new modular architecture.
 *
 * Created as part of Phase 7: Activity Refactor
 */
public class SplashFacade {
    private static final String TAG = "SplashFacade";

    // Core components
    private final SplashAnimationManager animationManager;
    private final SecurityChecker securityChecker;
    private final SplashNavigator navigator;

    // UI components
    private final ImageView logo;
    private final TextView title;
    private final TextView status;
    private final ProgressBar logoProgress;

    // Context and callbacks
    private final AppCompatActivity activity;
    private final SplashCallback callback;

    // Activity result launcher for login
    private final ActivityResultLauncher<Intent> loginLauncher;

    /**
     * Splash callback interface for activity communication
     */
    public interface SplashCallback {
        void onNavigateToLogin(Intent loginIntent);
        void onNavigateToMain(Intent mainIntent);
        void onShowError(String title, String message);
        void finishActivity();
    }

    public SplashFacade(AppCompatActivity activity, SplashCallback callback,
                       ImageView logo, TextView title, TextView status, ProgressBar logoProgress) {
        this.activity = activity;
        this.callback = callback;
        this.logo = logo;
        this.title = title;
        this.status = status;
        this.logoProgress = logoProgress;

        // Initialize components
        this.animationManager = new SplashAnimationManager(logo, title, status, logoProgress);
        this.securityChecker = new SecurityChecker();
        this.navigator = new SplashNavigator(activity, new SplashNavigator.NavigationCallback() {
            @Override
            public void onNavigateToLogin() {
                navigateToLogin();
            }

            @Override
            public void onNavigateToMain() {
                navigateToMain();
            }

            @Override
            public void onShowError(String title, String message) {
                callback.onShowError(title, message);
            }

            @Override
            public void updateStatus(String message) {
                animationManager.updateStatus(message);
            }
        });

        // Initialize login launcher
        this.loginLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    navigator.handleLoginSuccess();
                } else {
                    navigator.handleLoginFailure();
                    callback.finishActivity();
                }
            }
        );

        setupAnimationCallback();
    }

    /**
     * Setup animation completion callback
     */
    private void setupAnimationCallback() {
        animationManager.setAnimationCallback(new SplashAnimationManager.AnimationCallback() {
            @Override
            public void onAnimationComplete() {
                initializeSystem();
            }

            @Override
            public void onAnimationCancelled() {
                Logx.d("Splash animation cancelled");
            }
        });
    }

    /**
     * Start the complete splash sequence
     */
    public void startSplashSequence() {
        Logx.d("Starting splash sequence");

        // Load native library first
        if (!loadNativeLibrary()) {
            callback.onShowError("Startup Error", "Failed to load native library");
            return;
        }

        // Start animation sequence
        animationManager.startSplashAnimation();
    }

    /**
     * Load native library using NativeLoader
     */
    private boolean loadNativeLibrary() {
        try {
            return com.bearmod.loader.libhelper.NativeLoader.loadNativeLibrary(activity.getApplicationContext());
        } catch (Exception e) {
            Logx.e("Native library load failed", e);
            return false;
        }
    }

    /**
     * Initialize system after animation completes
     */
    private void initializeSystem() {
        try {
            // Perform security checks
            SecurityChecker.SecurityResult securityResult = securityChecker.performSecurityChecks(activity);

            if (securityResult != SecurityChecker.SecurityResult.SECURE) {
                String detectedApp = ""; // Would need to be passed from security checker
                String errorMessage = SecurityChecker.getSecurityErrorMessage(securityResult, detectedApp);
                callback.onShowError("Security Alert", errorMessage);
                return;
            }

            // Start authentication flow
            navigator.startAuthenticationFlow();

        } catch (Exception e) {
            Logx.e("System initialization error", e);
            callback.onShowError("Initialization Error", "Failed to initialize system: " + e.getMessage());
        }
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent loginIntent = new Intent(activity, com.bearmod.activity.LoginActivity.class);
        callback.onNavigateToLogin(loginIntent);
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent mainIntent = new Intent(activity, com.bearmod.activity.MainActivity.class);
        callback.onNavigateToMain(mainIntent);
    }

    /**
     * Handle login success from login activity
     */
    public void handleLoginSuccess() {
        navigator.handleLoginSuccess();
    }

    /**
     * Handle login failure/cancellation
     */
    public void handleLoginFailure() {
        navigator.handleLoginFailure();
    }

    /**
     * Launch login activity
     */
    public void launchLoginActivity(Intent loginIntent) {
        loginLauncher.launch(loginIntent);
    }

    /**
     * Cancel all operations and cleanup
     */
    public void cancel() {
        animationManager.cancelAnimation();
        navigator.cleanup();
    }

    /**
     * Get animation manager for direct access if needed
     */
    public SplashAnimationManager getAnimationManager() {
        return animationManager;
    }

    /**
     * Get navigator for direct access if needed
     */
    public SplashNavigator getNavigator() {
        return navigator;
    }
}