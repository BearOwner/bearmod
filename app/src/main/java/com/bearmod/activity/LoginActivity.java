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

import com.bearmod.R;
import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.util.Logx;
import com.bearmod.PermissionManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

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
    private PermissionManager permissionManager;
    // Verification watchdog: prevents UI from getting stuck on long/failed network
    private final Handler verifyWatchdog = new Handler(Looper.getMainLooper());
    private Runnable verifyTimeoutTask; // scheduled on attempt; canceled on success/failure

    /**
     * Check if user has valid stored authentication
     * Delegates to SimpleLicenseVerifier for centralized authentication logic
     */
    public static boolean hasValidKey(Context context) {
        // Check for valid KeyAuth authentication
        return SimpleLicenseVerifier.hasValidStoredAuth(context);
    }

    private void checkAndPromptPermissions() {
        try {
            // 1) Overlay (floating) permission
            PermissionManager.PermissionStatus overlay = permissionManager.checkOverlayPermission();
            if (!overlay.isGranted()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Please allow permission: Floating")
                        .setMessage("BearMod needs overlay permission to display its floating interface over the game.")
                        .setCancelable(false)
                        .setPositiveButton("Grant", (d, w) -> permissionManager.requestOverlayPermission(this, PermissionManager.REQUEST_OVERLAY_PERMISSION))
                        .setNegativeButton("Not now", null)
                        .show();
                return;
            }

            // 2) Storage readiness check (version-aware). SAF is still used for OBB operations later.
            PermissionManager.PermissionStatus storage = permissionManager.checkStoragePermission();
            if (!storage.isGranted()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("File Access Permission")
                        .setMessage("This app needs permission to access files needed for game data validation. Please grant permission to continue.")
                        .setCancelable(false)
                        .setPositiveButton("Grant Permission", (d, w) -> permissionManager.requestStoragePermission(this, PermissionManager.REQUEST_STORAGE_PERMISSION))
                        .setNegativeButton("Exit", (d, w) -> {
                            setResult(RESULT_CANCELED);
                            finish();
                        })
                        .show();
            }
        } catch (Throwable t) {
            Logx.w("LOG_PERM_PREFLIGHT_FAIL", t);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (permissionManager != null) {
                permissionManager.handlePermissionResult(requestCode, resultCode, null);
            }
        } catch (Throwable t) {
            Logx.w("LOG_PERM_RES_FAIL", t);
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
        LoginActivity.nativeLibraryLoaded = nativeLibraryLoaded;
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
            permissionManager = PermissionManager.Companion.getInstance(this);
            
            // Load saved license key if remember key is enabled
            loadSavedLicenseKey();
            
        } catch (Exception e) {
            Logx.e("LOG_ONCREATE_ERR", e);
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

        // Remember Key checkbox: persist immediately
        checkboxRememberKey.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SimpleLicenseVerifier.setRememberKeyPreference(LoginActivity.this, isChecked);
            } catch (Throwable t) {
                Logx.w("LOG_REMEMBER_PREF_FAIL", t);
            }
        });

        // Auto-login checkbox: persist immediately
        checkboxAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                SimpleLicenseVerifier.saveAutoLoginPreference(LoginActivity.this, isChecked);
            } catch (Throwable t) {
                Logx.w("LOG_AUTOLOGIN_PREF_FAIL", t);
            }
        });
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

        // Start a timeout to avoid stuck UI if callbacks never arrive (network/hangs)
        // This is a UI safeguard only; real result may still arrive and will cancel this.
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
        }
        verifyTimeoutTask = () -> {
            Logx.w("LOG_VERIFY_TIMEOUT");
            setLoadingState(false);
            statusText.setText("Verification timed out. Please try again.");
            statusText.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_red_light));
            Toast.makeText(LoginActivity.this, "Verification timed out", Toast.LENGTH_SHORT).show();
        };
        verifyWatchdog.postDelayed(verifyTimeoutTask, 12_000);

        // Use KeyAuth license verification flow
        SimpleLicenseVerifier.verifyLicense(this, licenseKey, new SimpleLicenseVerifier.LicenseCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    // Cancel timeout guard
                    if (verifyTimeoutTask != null) {
                        verifyWatchdog.removeCallbacks(verifyTimeoutTask);
                        verifyTimeoutTask = null;
                    }
                    setLoadingState(false);
                    statusText.setText("License verification successful!");
                    statusText.setTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_green_light));

                    // Persist user preferences and optionally store the license key
                    boolean remember = checkboxRememberKey.isChecked();
                    boolean autoLogin = checkboxAutoLogin.isChecked();

                    // Remember Key: set preference first, then save the key
                    SimpleLicenseVerifier.setRememberKeyPreference(LoginActivity.this, remember);
                    if (remember) {
                        SimpleLicenseVerifier.saveLicenseKey(LoginActivity.this, licenseKey);
                    }

                    // Auto-login preference (self-selection)
                    SimpleLicenseVerifier.saveAutoLoginPreference(LoginActivity.this, autoLogin);

                    Toast.makeText(LoginActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                    // Return success result to SplashActivity immediately (avoid UI getting stuck)
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // Cancel timeout guard
                    if (verifyTimeoutTask != null) {
                        verifyWatchdog.removeCallbacks(verifyTimeoutTask);
                        verifyTimeoutTask = null;
                    }
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
            Logx.d("LOG_INIT_OK");
        } catch (UnsatisfiedLinkError e) {
            Logx.w("LOG_INIT_MISS");
        } catch (Throwable t) {
            Logx.w("LOG_INIT_FAIL", t);
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
        Logx.d("LOG_DESTROY");
        // Ensure watchdog is cleared to avoid leaking callbacks
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
            verifyTimeoutTask = null;
        }
    }

    public static native void updateAuthenticationState(String sessionId, String token, String hwid, boolean isValid);
}
