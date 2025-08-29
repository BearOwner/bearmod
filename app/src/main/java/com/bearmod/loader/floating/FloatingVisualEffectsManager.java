package com.bearmod.loader.floating;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.widget.Toast;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingVisualEffectsManager - Manages visual effects and haptic feedback
 * Handles iOS-style haptic feedback patterns and visual enhancements
 */
public class FloatingVisualEffectsManager {

    private static final String TAG = "FloatingVisualEffectsManager";

    private final Context context;

    // Haptic feedback types
    public enum HapticFeedbackType {
        LIGHT, MEDIUM, HEAVY, SUCCESS, WARNING, ERROR
    }

    public FloatingVisualEffectsManager(Context context) {
        this.context = context;
        Logx.d("FloatingVisualEffectsManager initialized");
    }

    /**
     * Trigger haptic feedback
     */
    public void triggerHapticFeedback(String type) {
        try {
            HapticFeedbackType feedbackType = parseFeedbackType(type);
            triggerHapticFeedback(feedbackType);
        } catch (Exception e) {
            Logx.e("Error triggering haptic feedback '" + type + "': " + e.getMessage(), e);
        }
    }

    /**
     * Trigger haptic feedback with enum
     */
    public void triggerHapticFeedback(HapticFeedbackType type) {
        try {
            Vibrator vibrator = getVibrator();
            if (vibrator == null || !vibrator.hasVibrator()) {
                Logx.w("Haptic feedback not available");
                return;
            }

            VibrationEffect effect = createVibrationEffect(type);
            vibrator.vibrate(effect);

            Logx.d("Haptic feedback triggered: " + type);

        } catch (Exception e) {
            Logx.e("Error triggering haptic feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Show toast message with haptic feedback
     */
    public void showToastWithHaptic(String message, int duration, HapticFeedbackType feedbackType) {
        try {
            // Trigger haptic feedback
            triggerHapticFeedback(feedbackType);

            // Show toast
            Toast.makeText(context, message, duration).show();

            Logx.d("Toast with haptic shown: " + message);

        } catch (Exception e) {
            Logx.e("Error showing toast with haptic: " + e.getMessage(), e);
        }
    }

    /**
     * Show success message with success haptic
     */
    public void showSuccessMessage(String message) {
        showToastWithHaptic(message, Toast.LENGTH_SHORT, HapticFeedbackType.SUCCESS);
    }

    /**
     * Show error message with error haptic
     */
    public void showErrorMessage(String message) {
        showToastWithHaptic(message, Toast.LENGTH_LONG, HapticFeedbackType.ERROR);
    }

    /**
     * Show warning message with warning haptic
     */
    public void showWarningMessage(String message) {
        showToastWithHaptic(message, Toast.LENGTH_SHORT, HapticFeedbackType.WARNING);
    }

    /**
     * Trigger light haptic feedback
     */
    public void triggerLightFeedback() {
        triggerHapticFeedback(HapticFeedbackType.LIGHT);
    }

    /**
     * Trigger medium haptic feedback
     */
    public void triggerMediumFeedback() {
        triggerHapticFeedback(HapticFeedbackType.MEDIUM);
    }

    /**
     * Trigger heavy haptic feedback
     */
    public void triggerHeavyFeedback() {
        triggerHapticFeedback(HapticFeedbackType.HEAVY);
    }

    /**
     * Trigger success haptic feedback
     */
    public void triggerSuccessFeedback() {
        triggerHapticFeedback(HapticFeedbackType.SUCCESS);
    }

    /**
     * Trigger warning haptic feedback
     */
    public void triggerWarningFeedback() {
        triggerHapticFeedback(HapticFeedbackType.WARNING);
    }

    /**
     * Trigger error haptic feedback
     */
    public void triggerErrorFeedback() {
        triggerHapticFeedback(HapticFeedbackType.ERROR);
    }

    /**
     * Check if haptic feedback is supported
     */
    public boolean isHapticFeedbackSupported() {
        try {
            Vibrator vibrator = getVibrator();
            return vibrator != null && vibrator.hasVibrator();
        } catch (Exception e) {
            Logx.e("Error checking haptic feedback support: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get device vibration capabilities
     */
    public String getVibrationCapabilities() {
        try {
            Vibrator vibrator = getVibrator();
            if (vibrator == null) {
                return "Not available";
            }

            StringBuilder capabilities = new StringBuilder();
            capabilities.append("Has vibrator: ").append(vibrator.hasVibrator()).append("\n");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                capabilities.append("Has amplitude control: ").append(vibrator.hasAmplitudeControl()).append("\n");
            }

            return capabilities.toString();

        } catch (Exception e) {
            Logx.e("Error getting vibration capabilities: " + e.getMessage(), e);
            return "Error retrieving capabilities";
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingVisualEffectsManager resources...");
            // No specific cleanup needed for this component
            Logx.d("FloatingVisualEffectsManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingVisualEffectsManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private Vibrator getVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    return vm.getDefaultVibrator();
                }
            } else {
                return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception e) {
            Logx.e("Error getting vibrator: " + e.getMessage(), e);
        }
        return null;
    }

    private VibrationEffect createVibrationEffect(HapticFeedbackType type) {
        return switch (type) {
            case LIGHT -> VibrationEffect.createOneShot(25, 50);
            case MEDIUM -> VibrationEffect.createOneShot(50, 100);
            case HEAVY -> VibrationEffect.createOneShot(75, 150);
            case SUCCESS -> VibrationEffect.createWaveform(new long[]{0, 50, 50, 50}, -1);
            case WARNING -> VibrationEffect.createWaveform(new long[]{0, 100, 100, 100}, -1);
            case ERROR -> VibrationEffect.createWaveform(new long[]{0, 150, 50, 150}, -1);
            default -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
        };
    }

    private HapticFeedbackType parseFeedbackType(String type) {
        if (type == null) {
            return HapticFeedbackType.MEDIUM;
        }

        return switch (type.toLowerCase()) {
            case "light" -> HapticFeedbackType.LIGHT;
            case "medium" -> HapticFeedbackType.MEDIUM;
            case "heavy" -> HapticFeedbackType.HEAVY;
            case "success" -> HapticFeedbackType.SUCCESS;
            case "warning" -> HapticFeedbackType.WARNING;
            case "error" -> HapticFeedbackType.ERROR;
            default -> HapticFeedbackType.MEDIUM;
        };
    }
}