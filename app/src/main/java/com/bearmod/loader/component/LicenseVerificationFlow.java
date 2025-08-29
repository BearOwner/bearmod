package com.bearmod.loader.component;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.loader.utilities.Logx;
import java.util.Objects;

/**
 * LicenseVerificationFlow - Manages the complete license verification UI flow
 *
 * Extracted from LoginActivity.java to separate license verification logic.
 * Handles UI state management, verification timeout, and result processing.
 *
 * Migrated from com.bearmod.activity.LoginActivity to com.bearmod.loader.component
 */
public class LicenseVerificationFlow {
    private static final String TAG = "LicenseVerificationFlow";

    private final Context context;
    private final Activity activity;

    // UI Components
    private final EditText editLicenseKey;
    private final ImageButton buttonCopyPaste;
    private final Button buttonLogin;
    private final ProgressBar progressBar;
    private final TextView statusText;

    // Verification watchdog
    private final Handler verifyWatchdog = new Handler(Looper.getMainLooper());
    private Runnable verifyTimeoutTask;

    /**
     * Interface for verification flow callbacks
     */
    public interface VerificationCallback {
        void onVerificationSuccess();
        void onVerificationFailed(String error);
        void onVerificationTimeout();
    }

    public LicenseVerificationFlow(Context context, Activity activity,
                                 EditText editLicenseKey, ImageButton buttonCopyPaste,
                                 Button buttonLogin, ProgressBar progressBar, TextView statusText) {
        this.context = context;
        this.activity = activity;
        this.editLicenseKey = editLicenseKey;
        this.buttonCopyPaste = buttonCopyPaste;
        this.buttonLogin = buttonLogin;
        this.progressBar = progressBar;
        this.statusText = statusText;
    }

    /**
     * Setup button listeners for the verification flow
     */
    public void setupButtonListeners(VerificationCallback callback) {
        // Copy/Paste button
        buttonCopyPaste.setOnClickListener(v -> handleCopyPaste());

        // Login button
        buttonLogin.setOnClickListener(v -> attemptLicenseVerification(callback));
    }

    /**
     * Handle copy/paste functionality
     */
    private void handleCopyPaste() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData.Item item = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0);
            String pastedText = item.getText().toString();
            editLicenseKey.setText(pastedText);
            Toast.makeText(context, "License key pasted from clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No text found in clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Attempt license verification with timeout protection
     */
    private void attemptLicenseVerification(VerificationCallback callback) {
        String licenseKey = editLicenseKey.getText().toString().trim();

        if (licenseKey.isEmpty()) {
            Toast.makeText(context, "Please enter your license key", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        setLoadingState(true);
        statusText.setText("Verifying license with KeyAuth...");
        statusText.setVisibility(TextView.VISIBLE);

        // Start a timeout to avoid stuck UI if callbacks never arrive (network/hangs)
        // This is a UI safeguard only; real result may still arrive and will cancel this.
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
        }
        verifyTimeoutTask = () -> {
            Logx.w("Verification timeout");
            setLoadingState(false);
            statusText.setText("Verification timed out. Please try again.");
            statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            Toast.makeText(context, "Verification timed out", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onVerificationTimeout();
            }
        };
        verifyWatchdog.postDelayed(verifyTimeoutTask, 12_000);

        // Use KeyAuth license verification flow
        SimpleLicenseVerifier.verifyLicense(context, licenseKey, new SimpleLicenseVerifier.LicenseCallback() {
            @Override
            public void onSuccess(String message) {
                handleVerificationSuccess(callback);
            }

            @Override
            public void onFailure(String error) {
                handleVerificationFailure(error, callback);
            }
        });
    }

    /**
     * Handle successful license verification
     */
    private void handleVerificationSuccess(VerificationCallback callback) {
        // Cancel timeout guard
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
            verifyTimeoutTask = null;
        }

        setLoadingState(false);
        statusText.setText("License verification successful!");
        statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_light));

        Toast.makeText(context, "Authentication successful!", Toast.LENGTH_SHORT).show();

        if (callback != null) {
            callback.onVerificationSuccess();
        }
    }

    /**
     * Handle failed license verification
     */
    private void handleVerificationFailure(String error, VerificationCallback callback) {
        // Cancel timeout guard
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
            verifyTimeoutTask = null;
        }

        setLoadingState(false);
        statusText.setText("License verification failed: " + error);
        statusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));

        Toast.makeText(context, "Authentication failed: " + error, Toast.LENGTH_LONG).show();

        if (callback != null) {
            callback.onVerificationFailed(error);
        }
    }

    /**
     * Set loading state for UI components
     */
    private void setLoadingState(boolean loading) {
        if (loading) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Verifying...");
            editLicenseKey.setEnabled(false);
            buttonCopyPaste.setEnabled(false);
        } else {
            progressBar.setVisibility(ProgressBar.GONE);
            buttonLogin.setEnabled(true);
            buttonLogin.setText("LOGIN");
            editLicenseKey.setEnabled(true);
            buttonCopyPaste.setEnabled(true);
        }
    }

    /**
     * Get current license key from input field
     */
    public String getCurrentLicenseKey() {
        return editLicenseKey.getText().toString().trim();
    }

    /**
     * Set license key in input field
     */
    public void setLicenseKey(String licenseKey) {
        editLicenseKey.setText(licenseKey);
    }

    /**
     * Clear verification timeout task
     */
    public void clearTimeoutTask() {
        if (verifyTimeoutTask != null) {
            verifyWatchdog.removeCallbacks(verifyTimeoutTask);
            verifyTimeoutTask = null;
        }
    }

    /**
     * Check if verification is currently in progress
     */
    public boolean isVerificationInProgress() {
        return progressBar.getVisibility() == ProgressBar.VISIBLE;
    }
}