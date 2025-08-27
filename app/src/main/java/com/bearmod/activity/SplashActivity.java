package com.bearmod.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.bearmod.R;
import com.bearmod.security.AntiDetectionManager;
import com.bearmod.BuildConfig;
import com.bearmod.util.NativeUtils;
import com.bearmod.bridge.NativeLib;
import com.bearmod.Floating;
import com.bearmod.util.Logx;

/**
 * SplashActivity - Initial app launch and loading screen
 * <p>
 * Responsibilities:
 * - Show animated splash screen with BearMod branding
 * - Load native library and perform system checks
 * - Anti-detection and security validation
 * - Navigate to LoginActivity if no valid license, or MainActivity if authenticated
 * <p>
 * Navigation Flow:
 * SplashActivity → LoginActivity (if needed) → MainActivity
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    
    // UI Components
    private ImageView logo;
    private TextView title;
    private TextView status;
    private ProgressBar logoProgress;
    
    // Animation and state management
    private AnimatorSet currentAnimation;
    private boolean isInitializing = false;
    // Navigation race guard: ensures we only navigate once (Login OR Main)
    private boolean hasNavigated = false;
    // Main-thread scheduler for time-based UI tasks (timeouts, delayed nav)
    private final Handler watchdog = new Handler(Looper.getMainLooper());
    // Auto-login timeout task: if auth takes too long, we fall back to Login
    private Runnable authTimeoutTask;
    // Deferred navigation to Login, kept as a field so we can cancel it on success
    private Runnable navigateToLoginTask;
    
    // Anti-detection manager
    private AntiDetectionManager antiDetectionManager;
    
    // Activity Result Launcher for login
    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            // Login successful - navigate to MainActivity
                            Log.d(TAG, "Login successful - navigating to MainActivity");
                            // Ensure no pending tasks can relaunch Login after success
                            cancelAuthTimeoutWatchdog();
                            cancelLoginNavigateTask();
                            navigateToMainActivity();
                        } else {
                            // Login failed or cancelled - exit app
                            Log.d(TAG, "Login failed or cancelled - exiting");
                            finish();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Configure window for fullscreen experience
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            
            setContentView(R.layout.activity_splash);
            initializeViews();
            
            // Initialize anti-detection manager (singleton)
            antiDetectionManager = AntiDetectionManager.getInstance(this);

            // Load native library (bearmod). Mundo loading is deferred to future container/runtime.
            loadNativeLibrary();

            // Start initialization sequence
            startInitializationSequence();
            
        } catch (Exception e) {
            Logx.e("SPL_ONCREATE_ERR", e);
            showErrorAndExit("Startup Error", "Failed to initialize BearMod: " + e.getMessage());
        }
    }
    
    private void initializeViews() {
        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);
        status = findViewById(R.id.status);
        logoProgress = findViewById(R.id.logo_progress);
        
        // Set initial states for animation
        logo.setScaleX(0);
        logo.setScaleY(0);
        title.setAlpha(0);
        status.setAlpha(0);
        logoProgress.setProgress(0);
    }
    
    private void startInitializationSequence() {
        if (isInitializing) return;
        isInitializing = true;
        
        // Start splash animation
        startSplashAnimation();
    }
    
    @SuppressLint("SetTextI18n")
    private void startSplashAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();

        // Logo scale animation with bounce
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());

        // Progress bar animation
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.addUpdateListener(animation -> 
            logoProgress.setProgress((Integer) animation.getAnimatedValue()));

        // Title and status fade in
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
        ObjectAnimator statusFade = ObjectAnimator.ofFloat(status, "alpha", 0f, 1f);

        // Logo rotation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());

        // Combine animations
        AnimatorSet initialSet = new AnimatorSet();
        initialSet.playTogether(scaleX, scaleY, progressAnimator, rotation);

        animatorSet.playSequentially(initialSet, titleFade, statusFade);

        // Set durations
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        progressAnimator.setDuration(1500);
        rotation.setDuration(1000);
        titleFade.setDuration(500);
        statusFade.setDuration(500);

        // Initialize system after animations
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                status.setText("Initializing...");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (!isInitializing) return;
                
                // Start system initialization
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    initializeSystem();
                }, 500);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                isInitializing = false;
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        currentAnimation = animatorSet;
        animatorSet.start();
    }
    
    @SuppressLint("SetTextI18n")
    private void initializeSystem() {
        try {
            // Step 1: Initialize native core (local readiness; MundoCore removed for now)
            status.setText("Initializing core...");
            
            // Step 2: Perform anti-detection checks
            status.setText("Checking security...");
            if (!performSecurityChecks()) {
                return; // Security check failed, app will exit
            }
            
            // Step 3: Check authentication status
            Logx.d("SPL_AUTH_CHECK");
            status.setText("Checking auth...");
            checkAuthenticationAndNavigate();
            
        } catch (Exception e) {
            Log.e(TAG, "Error during system initialization", e);
            showErrorAndExit("Initialization Error", "Failed to initialize system: " + e.getMessage());
        }
    }
    
    private void loadNativeLibrary() {
        try {
            System.loadLibrary("bearmod");
            Logx.d("SPL_LOAD_OK");
            NativeUtils.setNativeLoaded(true);

            // Perform native registrations in the correct App ClassLoader context
            try {
                NativeLib.initialize(getApplicationContext());
                Logx.d("SPL_INIT_OK");
                // Register natives using Class objects to avoid FindClass on string names
                try {
                    NativeLib.registerNatives(Floating.class, com.bearmod.activity.LoginActivity.class, com.bearmod.bridge.NativeLib.class);
                    Logx.d("SPL_REG_OK");
                } catch (Throwable regErr) {
                    Logx.w("SPL_REG_FAIL");
                }
            } catch (Throwable initErr) {
                Logx.w("SPL_NATIVE_INIT_FAIL");
            }
        } catch (UnsatisfiedLinkError e) {
            Logx.w("SPL_LOAD_FAIL");
            // Continue anyway - app can work in demo mode
            NativeUtils.setNativeLoaded(false);
        } catch (Exception e) {
            Logx.e("SPL_UNEXPECTED", e);
            // Continue anyway - app can work in demo mode
            NativeUtils.setNativeLoaded(false);
        }
    }
    
    private boolean performSecurityChecks() {
        // Perform anti-detection checks (adapted from sample code)
        String[] suspiciousPackages = {
            "com.guoshi.httpcanary.premium", 
            "com.guoshi.httpcanary", 
            "com.sniffer", 
            "com.httpcanary.pro",
            "de.robv.android.xposed.installer",
            "org.meowcat.edxposed.manager",
            "io.va.exposed"
        };
        
        for (String packageName : suspiciousPackages) {
            if (isAppInstalled(this, packageName)) {
                Logx.w("SPL_SEC_ALERT");
                showErrorAndExit("Security Alert", 
                    "Please remove debugging/hooking tools and restart the app.");
                return false;
            }
        }
        
        // Verify anti-detection assets
        if (!antiDetectionManager.verifyAntiDetectionAssets()) {
            Log.w(TAG, "Anti-detection assets verification failed");
            // Continue anyway - not critical for basic functionality
        }
        
        return true;
    }
    
    @SuppressLint("SetTextI18n")
    private void checkAuthenticationAndNavigate() {
        // Check if user has valid stored authentication
        if (LoginActivity.hasValidKey(this) && com.bearmod.auth.SimpleLicenseVerifier.isAutoLoginEnabled(this)) {
            status.setText("Validating stored authentication...");
            // If offline, skip auto-login and go to Login gracefully
            if (!isNetworkAvailable()) {
                status.setText("Offline - please login");
                safeNavigateToLoginDelayed(500);
                return;
            }

            // Start a timeout watchdog to avoid getting stuck
            startAuthTimeoutWatchdog();

            // Attempt auto-login with stored session/token
            com.bearmod.auth.SimpleLicenseVerifier.autoLogin(this, new com.bearmod.auth.SimpleLicenseVerifier.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        cancelAuthTimeoutWatchdog();
                        cancelLoginNavigateTask();
                        Logx.d("SPL_AUTH_OK");
                        status.setText("Authentication verified!");

                        // Navigate to MainActivity after short delay
                        watchdog.postDelayed(() -> navigateMainSafe(), 600);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        cancelAuthTimeoutWatchdog();
                        Logx.d("SPL_AUTH_FAIL");
                        status.setText("Authentication required...");

                        // Navigate to LoginActivity after short delay
                        safeNavigateToLoginDelayed(800);
                    });
                }
            });
        } else {
            Logx.d("SPL_NO_AUTH");
            status.setText("Authentication required...");

            // Navigate to LoginActivity after short delay
            safeNavigateToLoginDelayed(800);
        }
    }

    private void startAuthTimeoutWatchdog() {
        cancelAuthTimeoutWatchdog();
        authTimeoutTask = () -> {
            if (hasNavigated) return;
            Log.w(TAG, "Auto-login timeout reached; navigating to LoginActivity");
            status.setText("Authentication timeout - please login");
            safeNavigateToLoginDelayed(0);
        };
        watchdog.postDelayed(authTimeoutTask, 12_000); // 12s timeout
    }

    private void cancelAuthTimeoutWatchdog() {
        if (authTimeoutTask != null) {
            watchdog.removeCallbacks(authTimeoutTask);
            authTimeoutTask = null;
        }
    }

    private void cancelLoginNavigateTask() {
        if (navigateToLoginTask != null) {
            watchdog.removeCallbacks(navigateToLoginTask);
            navigateToLoginTask = null;
        }
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (Exception ignored) {
            return false;
        }
    }

    private void safeNavigateToLoginDelayed(long delayMs) {
        if (hasNavigated) return;
        navigateToLoginTask = this::navigateToLoginActivity;
        watchdog.postDelayed(navigateToLoginTask, delayMs);
    }

    private void navigateMainSafe() {
        if (hasNavigated) return;
        cancelLoginNavigateTask();
        hasNavigated = true;
        navigateToMainActivity();
    }
    
    private void navigateToLoginActivity() {
        if (hasNavigated) return;
        hasNavigated = true;
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginLauncher.launch(loginIntent);
    }
    
    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(this, com.bearmod.activity.MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    
    private void showErrorAndExit(String title, String message) {
        try {
            new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
        } catch (Exception dialogError) {
            // If we can't even show a dialog, just finish
            finish();
        }
    }
    
    /**
     * Check if a specific app is installed
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        isInitializing = false;
        cancelAuthTimeoutWatchdog();
        cancelLoginNavigateTask();
    }
}
