package com.bearmod.loader.component;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.ResourceProvider;

import java.util.Objects;

/**
 * AuthUI - Manages authentication UI components and user interactions
 * Extracted from LoginActivity to separate presentation logic from business logic
 *
 * Responsibilities:
 * - UI component initialization and management
 * - User interaction handling (buttons, checkboxes, copy/paste)
 * - Loading state management
 * - Status display and user feedback
 */
public class AuthUI {

    private static final String TAG = "AuthUI";

    private final ResourceProvider rp;

    // UI Components
    private EditText editLicenseKey;
    private ImageButton buttonCopyPaste;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView statusText;
    private CheckBox checkboxRememberKey;
    private CheckBox checkboxAutoLogin;

    // State
    private AuthUICallback callback;
    private boolean isLoading = false;

    /**
     * Interface for AuthUI events
     */
    public interface AuthUICallback {
        void onLoginAttempt(String licenseKey);
        void onRememberKeyChanged(boolean remember);
        void onAutoLoginChanged(boolean autoLogin);
        void onBackPressed();
    }

    public AuthUI(ResourceProvider rp) {
        this.rp = rp;
    }

    /**
     * Set UI callback for events
     */
    public void setCallback(AuthUICallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize UI components from the activity
     */
    public void initializeViews(View rootView) {
        try {
            editLicenseKey = rootView.findViewById(rp.id("edit_license_key"));
            buttonCopyPaste = rootView.findViewById(rp.id("button_copy_paste"));
            buttonLogin = rootView.findViewById(rp.id("button_login"));
            progressBar = rootView.findViewById(rp.id("progress_bar"));
            statusText = rootView.findViewById(rp.id("status_text"));
            checkboxRememberKey = rootView.findViewById(rp.id("checkbox_remember_key"));
            checkboxAutoLogin = rootView.findViewById(rp.id("checkbox_auto_login"));

            // Set initial states
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (statusText != null) statusText.setVisibility(View.GONE);

            setupButtonListeners();

            Logx.d("AuthUI components initialized");

        } catch (Exception e) {
            Logx.e("AuthUI initialization failed", e);
        }
    }

    /**
     * Setup button listeners and interactions
     */
    private void setupButtonListeners() {
        // Copy/Paste button
        if (buttonCopyPaste != null) {
            buttonCopyPaste.setOnClickListener(v -> handleCopyPaste());
        }

        // Login button
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> handleLoginAttempt());
        }

        // Remember Key checkbox
        if (checkboxRememberKey != null) {
            checkboxRememberKey.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (callback != null) {
                    callback.onRememberKeyChanged(isChecked);
                }
            });
        }

        // Auto-login checkbox
        if (checkboxAutoLogin != null) {
            checkboxAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (callback != null) {
                    callback.onAutoLoginChanged(isChecked);
                }
            });
        }
    }

    /**
     * Handle copy/paste functionality
     */
    private void handleCopyPaste() {
        try {
            ClipboardManager clipboard = (ClipboardManager) rp.app().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
                String pastedText = item.getText().toString();

                if (editLicenseKey != null) {
                    editLicenseKey.setText(pastedText);
                }

                showToast("License key pasted from clipboard");
            } else {
                showToast("No text found in clipboard");
            }
        } catch (Exception e) {
            Logx.w("Copy/paste failed", e);
            showToast("Copy/paste failed");
        }
    }

    /**
     * Handle login button click
     */
    private void handleLoginAttempt() {
        if (isLoading) return; // Prevent multiple attempts

        String licenseKey = getLicenseKey().trim();

        if (licenseKey.isEmpty()) {
            showToast("Please enter your license key");
            return;
        }

        if (callback != null) {
            callback.onLoginAttempt(licenseKey);
        }
    }

    /**
     * Get current license key from input field
     */
    public String getLicenseKey() {
        return editLicenseKey != null ? editLicenseKey.getText().toString() : "";
    }

    /**
     * Set license key in input field
     */
    public void setLicenseKey(String licenseKey) {
        if (editLicenseKey != null) {
            editLicenseKey.setText(licenseKey);
        }
    }

    /**
     * Set loading state for UI components
     */
    public void setLoadingState(boolean loading) {
        this.isLoading = loading;

        try {
            if (loading) {
                // Show loading state
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(false);
                    buttonLogin.setText("Verifying...");
                }
                if (editLicenseKey != null) editLicenseKey.setEnabled(false);
                if (buttonCopyPaste != null) buttonCopyPaste.setEnabled(false);

                showStatus("Verifying license with KeyAuth...", false);

            } else {
                // Hide loading state
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (buttonLogin != null) {
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("LOGIN");
                }
                if (editLicenseKey != null) editLicenseKey.setEnabled(true);
                if (buttonCopyPaste != null) buttonCopyPaste.setEnabled(true);
            }
        } catch (Exception e) {
            Logx.w("Error setting loading state", e);
        }
    }

    /**
     * Show status message to user
     */
    public void showStatus(String message, boolean isSuccess) {
        try {
            if (statusText != null) {
                statusText.setText(message);
                statusText.setVisibility(View.VISIBLE);

                int color = isSuccess ?
                    android.graphics.Color.GREEN :
                    android.graphics.Color.RED;

                statusText.setTextColor(color);
            }
        } catch (Exception e) {
            Logx.w("Error showing status", e);
        }
    }

    /**
     * Hide status message
     */
    public void hideStatus() {
        try {
            if (statusText != null) {
                statusText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Logx.w("Error hiding status", e);
        }
    }

    /**
     * Set remember key checkbox state
     */
    public void setRememberKey(boolean remember) {
        if (checkboxRememberKey != null) {
            checkboxRememberKey.setChecked(remember);
        }
    }

    /**
     * Set auto-login checkbox state
     */
    public void setAutoLogin(boolean autoLogin) {
        if (checkboxAutoLogin != null) {
            checkboxAutoLogin.setChecked(autoLogin);
        }
    }

    /**
     * Get remember key checkbox state
     */
    public boolean isRememberKeyChecked() {
        return checkboxRememberKey != null && checkboxRememberKey.isChecked();
    }

    /**
     * Get auto-login checkbox state
     */
    public boolean isAutoLoginChecked() {
        return checkboxAutoLogin != null && checkboxAutoLogin.isChecked();
    }

    /**
     * Show toast message
     */
    private void showToast(String message) {
        try {
            Toast.makeText(rp.app(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logx.w("Error showing toast", e);
        }
    }

    /**
     * Enable/disable all interactive components
     */
    public void setInteractive(boolean interactive) {
        try {
            if (editLicenseKey != null) editLicenseKey.setEnabled(interactive);
            if (buttonCopyPaste != null) buttonCopyPaste.setEnabled(interactive);
            if (buttonLogin != null) buttonLogin.setEnabled(interactive);
            if (checkboxRememberKey != null) checkboxRememberKey.setEnabled(interactive);
            if (checkboxAutoLogin != null) checkboxAutoLogin.setEnabled(interactive);
        } catch (Exception e) {
            Logx.w("Error setting interactive state", e);
        }
    }

    /**
     * Get UI components for external access (if needed)
     */
    public EditText getEditLicenseKey() { return editLicenseKey; }
    public Button getButtonLogin() { return buttonLogin; }
    public ProgressBar getProgressBar() { return progressBar; }
    public TextView getStatusText() { return statusText; }

    /**
     * Load saved license key from preferences
     */
    public void loadSavedLicenseKey() {
        // TODO: Implement loading from SharedPreferences
        Logx.d("Loading saved license key");
    }

    /**
     * Show error message to user
     */
    public void showError(String message) {
        showStatus(message, false);
        showToast(message);
    }

    /**
     * Show success message to user
     */
    public void showSuccess(String message) {
        showStatus(message, true);
        showToast(message);
    }

    /**
     * Check if remember key is enabled
     */
    public boolean isRememberKeyEnabled() {
        return isRememberKeyChecked();
    }

    /**
     * Check if auto-login is enabled
     */
    public boolean isAutoLoginEnabled() {
        return isAutoLoginChecked();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        callback = null;
        Logx.d("AuthUI cleanup completed");
    }
}