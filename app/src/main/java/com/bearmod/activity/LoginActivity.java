package com.bearmod.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.bearmod.Floating;
import com.bearmod.R;
import com.bearmod.auth.SimpleLicenseVerifier;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import android.net.Uri;
import android.provider.Settings;

/**
 * LoginActivity - KeyAuth license verification UI
 * 
 * Responsibilities:
 * - Display license key input interface
 * - Handle KeyAuth authentication via SimpleLicenseVerifier
 * - Return RESULT_OK on successful authentication
 * - Return RESULT_CANCELED on back press or authentication failure
 * 
 * Navigation Flow:
 * SplashActivity → LoginActivity → (returns to SplashActivity) → MainActivity
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI Components for KeyAuth license verification

    private static final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private static final Object lockObject = new Object();
    private static volatile SharedPreferences gifs;
    // ========================================
    private static boolean nativeLibraryLoaded = false;

    private EditText editLicenseKey;
    private ImageButton buttonCopyPaste;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView statusText;
    private CheckBox checkboxRememberKey;
    private CheckBox checkboxAutoLogin;

    /*
    void hideesp()
    {
        Floating.hideesp();
    }

    void stopHideesp()
    {
        Floating.stopHideesp();
    }

    static {
        try {
            System.loadLibrary("bearmod");
            nativeLibraryLoaded = true;
            android.util.Log.d("Launcher", "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.w("Launcher", "Native library not available: " + e.getMessage());
            nativeLibraryLoaded = false;
        }
    }
*/
    /**
     * Check if user has valid stored authentication
     * Delegates to SimpleLicenseVerifier for centralized authentication logic
     */
    public static boolean hasValidKey(Context context) {
        // Check for valid KeyAuth authentication
        return SimpleLicenseVerifier.hasValidStoredAuth(context);
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
            setupButtonListeners();
            
            // Load saved license key if remember key is enabled
            loadSavedLicenseKey();
            
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            setResult(RESULT_CANCELED);
            finish();
        }
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

    private void setupButtonListeners() {
        // Copy/Paste button
        buttonCopyPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
                String pastedText = item.getText().toString();
                editLicenseKey.setText(pastedText);
                Toast.makeText(this, "License key pasted from clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No text found in clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        // Login button
        buttonLogin.setOnClickListener(v -> attemptLicenseVerification());
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
     * KeyAuth license verification flow: Init → License → Token → Session ID
     */
    @SuppressLint("SetTextI18n")
    private void attemptLicenseVerification() {
        String licenseKey = editLicenseKey.getText().toString().trim();

        if (licenseKey.isEmpty()) {
            Toast.makeText(this, "Please enter your license key", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        setLoadingState(true);
        statusText.setText("Verifying license with KeyAuth...");
        statusText.setVisibility(View.VISIBLE);

        // Use KeyAuth license verification flow
        SimpleLicenseVerifier.verifyLicense(this, licenseKey, new SimpleLicenseVerifier.LicenseCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    statusText.setText("License verification successful!");
                    statusText.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_green_light));

                    // Save license key if remember key is checked
                    if (checkboxRememberKey.isChecked()) {
                        SimpleLicenseVerifier.saveLicenseKey(LoginActivity.this, licenseKey);
                    }

                    // Save auto login preference
                    if (checkboxAutoLogin.isChecked()) {
                        SimpleLicenseVerifier.saveAutoLoginPreference(LoginActivity.this, true);
                    }

                    Toast.makeText(LoginActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                    // Return success result to SplashActivity after short delay
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        setResult(RESULT_OK);
                        finish();
                    }, 1500);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    statusText.setText("License verification failed: " + error);
                    statusText.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_red_light));
                    
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Initialize native library and perform system checks
     */
    public static void Init(Object object) {
        try {
            if (object == null) return;
            if (!isInitializing.compareAndSet(false, true)) return;

            synchronized (lockObject) {
                final Context context = (Context) object;
                Activity activity = (Activity) object;

                safeInit(context);

                // Check overlay permission
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                }

                try {
                    gifs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                } catch (Exception e) {
                    e.printStackTrace();
                    isInitializing.set(false);
                    return;
                }

                android.util.Log.d("Launcher", "Native initialization completed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            isInitializing.set(false);
        }
    }

    /**
     * Safe initialization of native library
     */
    public static void safeInit(Context mContext) {
        // Do NOT load libraries here; SplashActivity controls load order (bearmod -> mundo)
        // Only attempt native Init; proceed gracefully if unavailable
        try {
            Init(mContext);
            android.util.Log.d("LoginActivity", "Native Init called successfully");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.w("LoginActivity", "Native Init not available (library not loaded yet) ");
        } catch (Throwable t) {
            android.util.Log.w("LoginActivity", "Init call failed", t);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setLoadingState(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Verifying...");
            editLicenseKey.setEnabled(false);
            buttonCopyPaste.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
            buttonLogin.setText("LOGIN");
            editLicenseKey.setEnabled(true);
            buttonCopyPaste.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity destroyed");
    }

    public static native void updateAuthenticationState(String sessionId, String token, String hwid, boolean isValid);
}
