package com.bearmod.loader.component;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import com.bearmod.loader.utilities.Logx;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LicenseTimer - Manages license countdown timer display and logic
 *
 * Extracted from MainActivity.java to separate timer functionality.
 * Handles license expiration countdown with optimized performance.
 *
 * Migrated from com.bearmod.activity.MainActivity to com.bearmod.loader.component
 */
public class LicenseTimer {
    private static final String TAG = "LicenseTimer";

    // Timer components
    private final TextView countdownDays;
    private final TextView countdownHours;
    private final TextView countdownMinutes;
    private final TextView countdownSeconds;

    // Timer state
    private CountDownTimer androidCountdownTimer;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    // Constants
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private static final long ONE_MINUTE_IN_MILLIS = 60 * ONE_SECOND_IN_MILLIS;
    private static final long ONE_HOUR_IN_MILLIS = 60 * ONE_MINUTE_IN_MILLIS;
    private static final long ONE_DAY_IN_MILLIS = 24 * ONE_HOUR_IN_MILLIS;

    // Timer callback
    private TimerCallback callback;

    public interface TimerCallback {
        String getExpirationDate();
        void onTimerExpired();
    }

    public LicenseTimer(TextView countdownDays, TextView countdownHours,
                       TextView countdownMinutes, TextView countdownSeconds,
                       TimerCallback callback) {
        this.countdownDays = countdownDays;
        this.countdownHours = countdownHours;
        this.countdownMinutes = countdownMinutes;
        this.countdownSeconds = countdownSeconds;
        this.callback = callback;
    }

    /**
     * Start the license countdown timer
     */
    public void startLicenseCountdownTimer() {
        try {
            // Stop any existing timer
            stopCountdownTimer();

            // Get expiration date
            String expirationString = callback.getExpirationDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiryDate = dateFormat.parse(expirationString);

            if (expiryDate == null) {
                Logx.e("Failed to parse expiration date: " + expirationString);
                return;
            }

            long now = System.currentTimeMillis();
            long durationMillis = expiryDate.getTime() - now;

            if (durationMillis <= 0) {
                // License already expired
                Logx.d("License has already expired");
                updateCountdownDisplay(0, 0, 0, 0);
                callback.onTimerExpired();
                return;
            }

            // Start the optimized CountDownTimer
            startNewCountdownTimer(durationMillis);
            Logx.d("License countdown timer started successfully");

        } catch (Exception e) {
            Logx.e("Error starting license countdown timer", e);
            // Show fallback countdown (30 days) if there's an error
            startNewCountdownTimer(30L * ONE_DAY_IN_MILLIS);
        }
    }

    /**
     * Create and start a new CountDownTimer with the specified duration
     */
    private void startNewCountdownTimer(long durationMillis) {
        stopCountdownTimer(); // Ensure no existing timer is running

        androidCountdownTimer = new CountDownTimer(durationMillis, 1000) { // Tick every 1 second
            public void onTick(long millisUntilFinished) {
                long days = millisUntilFinished / ONE_DAY_IN_MILLIS;
                long hours = (millisUntilFinished % ONE_DAY_IN_MILLIS) / ONE_HOUR_IN_MILLIS;
                long minutes = (millisUntilFinished % ONE_HOUR_IN_MILLIS) / ONE_MINUTE_IN_MILLIS;
                long seconds = (millisUntilFinished % ONE_MINUTE_IN_MILLIS) / ONE_SECOND_IN_MILLIS;

                // Update UI on main thread
                new Handler(Looper.getMainLooper()).post(() ->
                    updateCountdownDisplay(days, hours, minutes, seconds));
            }

            public void onFinish() {
                // Update UI on main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    updateCountdownDisplay(0, 0, 0, 0);
                    Logx.d("License has expired (CountDownTimer finished).");
                    callback.onTimerExpired();
                });
            }
        }.start();
    }

    /**
     * Update the countdown display UI elements
     */
    @SuppressLint("SetTextI18n")
    private void updateCountdownDisplay(long days, long hours, long minutes, long seconds) {
        try {
            if (countdownDays != null) {
                countdownDays.setText(String.format(Locale.getDefault(), "%02d", days));
            }
            if (countdownHours != null) {
                countdownHours.setText(String.format(Locale.getDefault(), "%02d", hours));
            }
            if (countdownMinutes != null) {
                countdownMinutes.setText(String.format(Locale.getDefault(), "%02d", minutes));
            }
            if (countdownSeconds != null) {
                countdownSeconds.setText(String.format(Locale.getDefault(), "%02d", seconds));
            }
        } catch (Exception e) {
            Logx.e("Error updating countdown display", e);
        }
    }

    /**
     * Stop the countdown timer and clean up resources
     */
    public void stopCountdownTimer() {
        // Stop and clean up Android CountDownTimer
        if (androidCountdownTimer != null) {
            androidCountdownTimer.cancel();
            androidCountdownTimer = null;
        }

        // Legacy cleanup (in case old handler/runnable still exists)
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
            countdownHandler = null;
            countdownRunnable = null;
        }
    }

    /**
     * Check if timer is currently running
     */
    public boolean isTimerRunning() {
        return androidCountdownTimer != null;
    }

    /**
     * Set timer callback
     */
    public void setCallback(TimerCallback callback) {
        this.callback = callback;
    }

    /**
     * Start timer (alias for startLicenseCountdownTimer for MainFacade compatibility)
     */
    public void startTimer() {
        startLicenseCountdownTimer();
    }

    /**
     * Stop timer (alias for stopCountdownTimer for MainFacade compatibility)
     */
    public void stopTimer() {
        stopCountdownTimer();
    }
}