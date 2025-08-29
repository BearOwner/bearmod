package com.bearmod.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.bearmod.R;
import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.PermissionManager;

// New modular imports
import com.bearmod.loader.component.PermissionHandler;
import com.bearmod.loader.component.NativeInitializer;
import com.bearmod.loader.component.LicenseVerificationFlow;

/**
 * LoginActivity - KeyAuth license verification UI
 * <p>
 * Responsibilities:
 * - Display license key input interface
 * - Handle KeyAuth authentication via SimpleLicenseVerifier
 * - Return RESULT_OK on successful authentication
 * - Return RESULT_CANCELED on back press or authentication failure
 * <p>
 * Navigation Flow:
 * SplashActivity → LoginActivity → (returns to SplashActivity) → MainActivity
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI Components for KeyAuth license verification
    private EditText editLicenseKey;
    private ImageButton buttonCopyPaste;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView statusText;
    private CheckBox checkboxRememberKey;
    private CheckBox checkboxAutoLogin;

    // New modular components
    private PermissionHandler permissionHandler;
    private LicenseVerificationFlow verificationFlow;

    /**
     * Check if user has valid stored authentication
     * Delegates to SimpleLicenseVerifier for centralized authentication logic
     */
    public static boolean hasValidKey(Context context) {
        // Check for valid KeyAuth authentication
        return SimpleLicenseVerifier.hasValidStoredAuth(context);
    }

    /**
     * Check and prompt for critical permissions using modular PermissionHandler
     */
    private void checkAndPromptPermissions() {
        if (permissionHandler != null) {
            permissionHandler.checkAndPromptPermissions(new PermissionHandler.PermissionCallback() {
                @Override
                public void onPermissionGranted(String permission) {
                    Logx.d("All permissions granted: " + permission);
                }

                @Override
                public void onPermissionDenied(String permission) {
                    Logx.w("Permission denied: " + permission);
                    setResult(RESULT_CANCELED);
                    finish();
                }

                @Override
                public void onPermissionRequestCancelled() {
                    Logx.d("Permission request cancelled - will retry on resume");
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (permissionHandler != null) {
                permissionHandler.handlePermissionResult(requestCode, resultCode, data);
            }
        } catch (Throwable t) {
            Logx.w("Permission result handling failed", t);
        }
        // Re-check to continue the chain if user granted something
        checkAndPromptPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Preflight critical permissions before entering license
        checkAndPromptPermissions();
    }

    public static void setNativeLibraryLoaded(boolean nativeLibraryLoaded) {
        NativeInitializer.setNativeLibraryLoaded(nativeLibraryLoaded);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Configure window for fullscreen experience
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            setContentView(R.layout.activity_launcher_login);
            initializeViews();
            setupBackPressedHandler();

            // Initialize modular components
            initializeModularComponents();

            // Load saved license key if remember key is enabled
            loadSavedLicenseKey();

        } catch (Exception e) {
            Logx.e("LoginActivity initialization error", e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Initialize modular components
     */
    private void initializeModularComponents() {
        // Initialize PermissionHandler
        permissionHandler = new PermissionHandler(this, this);

        // Initialize LicenseVerificationFlow
        verificationFlow = new LicenseVerificationFlow(this, this,
                editLicenseKey, buttonCopyPaste, buttonLogin,
                progressBar, statusText);

        // Setup verification flow with callbacks
        verificationFlow.setupButtonListeners(new LicenseVerificationFlow.VerificationCallback() {
            @Override
            public void onVerificationSuccess() {
                handleVerificationSuccess();
            }

            @Override
            public void onVerificationFailed(String error) {
                handleVerificationFailure(error);
            }

            @Override
            public void onVerificationTimeout() {
                handleVerificationTimeout();
            }
        });
    }

    private void initializeViews() {
        editLicenseKey = findViewById(R.id.edit_license_key);
        buttonCopyPaste = findViewById(R.id.button_copy_paste);
        buttonLogin = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        checkboxRememberKey = findViewById(R.id.checkbox_remember_key);
        checkboxAutoLogin = findViewById(R.id.checkbox_auto_login);

        // Set initial states
        progressBar.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
    }

    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Return cancelled result to SplashActivity
                setResult(RESULT_CANCELED);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Setup button listeners for checkboxes (verification flow handles main buttons)
     */
    private void setupButtonListeners() {
        // Remember Key checkbox: persist immediately
        checkboxRememberKey.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SimpleLicenseVerifier.setRememberKeyPreference(LoginActivity.this, isChecked);
            } catch (Throwable t) {
                Logx.w("Remember key preference save failed", t);
            }
        });

        // Auto-login checkbox: persist immediately
        checkboxAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SimpleLicenseVerifier.saveAutoLoginPreference(LoginActivity.this, isChecked);
            } catch (Throwable t) {
                Logx.w("Auto-login preference save failed", t);
            }
        });
    }

    /**
     * Handle successful license verification
     */
    private void handleVerificationSuccess() {
        // Persist user preferences
        boolean remember = checkboxRememberKey.isChecked();
        boolean autoLogin = checkboxAutoLogin.isChecked();

        // Remember Key: set preference first, then save the key
        SimpleLicenseVerifier.setRememberKeyPreference(LoginActivity.this, remember);
        if (remember) {
            SimpleLicenseVerifier.saveLicenseKey(LoginActivity.this, verificationFlow.getCurrentLicenseKey());
        }

        // Auto-login preference (self-selection)
        SimpleLicenseVerifier.saveAutoLoginPreference(LoginActivity.this, autoLogin);

        // Return success result to SplashActivity
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Handle failed license verification
     */
    private void handleVerificationFailure(String error) {
        // Error already handled by LicenseVerificationFlow
        Logx.w("License verification failed: " + error);
    }

    /**
     * Handle verification timeout
     */
    private void handleVerificationTimeout() {
        // Timeout already handled by LicenseVerificationFlow
        Logx.w("License verification timed out");
    }

    private void loadSavedLicenseKey() {
        // Use centralized storage methods from SimpleLicenseVerifier
        String savedKey = SimpleLicenseVerifier.getSavedLicenseKey(this);
        boolean rememberKey = SimpleLicenseVerifier.isRememberKeyEnabled(this);
        boolean autoLogin = SimpleLicenseVerifier.isAutoLoginEnabled(this);

        if (rememberKey && !savedKey.isEmpty()) {
            editLicenseKey.setText(savedKey);
            checkboxRememberKey.setChecked(true);
        }

        if (autoLogin) {
            checkboxAutoLogin.setChecked(true);
            // Note: Auto-login is now handled by SplashActivity using SimpleLicenseVerifier.autoLogin()
            // No need to duplicate auto-login logic here
        }
    }

    /**
     * Initialize native library and perform system checks using NativeInitializer
     */
    public static void Init(Object object) {
        NativeInitializer.initialize(object);
    }

    /**
     * Safe initialization of native library using NativeInitializer
     */
    public static void safeInit(Context mContext) {
        NativeInitializer.safeInit(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logx.d("LoginActivity destroyed");

        // Clean up verification flow timeout task
        if (verificationFlow != null) {
            verificationFlow.clearTimeoutTask();
        }

        // Clean up modular components
        permissionHandler = null;
        verificationFlow = null;
    }

    public static native void updateAuthenticationState(String sessionId, String token, String hwid, boolean isValid);
}
