package com.bearmod.loader.floating;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * HideRecorder - Manages screen/audio recording hide logic
 * Handles overlay visibility during screen recording sessions
 * Extracted from Floating.java to separate recording concerns
 */
public class HideRecorder {

    private static final String TAG = "HideRecorder";

    private final Context context;
    private final WindowManager windowManager;

    // Layout references for hiding/showing
    private View mainLayout;
    private View iconLayout;
    private View canvasLayout;

    // Layout parameters
    private LayoutParams mainLayoutParams;
    private LayoutParams iconLayoutParams;
    private LayoutParams canvasLayoutParams;

    // State
    private boolean isRecordingHidden = false;

    public HideRecorder(Context context, WindowManager windowManager) {
        this.context = context;
        this.windowManager = windowManager;
    }

    /**
     * Set layout references for hide/show operations
     */
    public void setLayouts(View mainLayout, View iconLayout, View canvasLayout) {
        this.mainLayout = mainLayout;
        this.iconLayout = iconLayout;
        this.canvasLayout = canvasLayout;
    }

    /**
     * Set layout parameters for hide/show operations
     */
    public void setLayoutParams(LayoutParams mainParams, LayoutParams iconParams, LayoutParams canvasParams) {
        this.mainLayoutParams = mainParams;
        this.iconLayoutParams = iconParams;
        this.canvasLayoutParams = canvasParams;
    }

    /**
     * Hide overlay when recording starts
     */
    public void hideForRecording() {
        if (isRecordingHidden) {
            Log.d(TAG, "Already hidden for recording");
            return;
        }

        try {
            // Hide main layout
            if (mainLayout != null && mainLayout.getParent() != null) {
                windowManager.removeView(mainLayout);
            }

            // Hide canvas layout
            if (canvasLayout != null && canvasLayout.getParent() != null) {
                windowManager.removeView(canvasLayout);
            }

            // Show icon layout (minimized state)
            if (iconLayout != null && iconLayout.getParent() == null) {
                windowManager.addView(iconLayout, iconLayoutParams);
            }

            isRecordingHidden = true;
            Log.d(TAG, "Overlay hidden for recording");

        } catch (Exception e) {
            Log.e(TAG, "Error hiding overlay for recording", e);
        }
    }

    /**
     * Show overlay when recording stops
     */
    public void showAfterRecording() {
        if (!isRecordingHidden) {
            Log.d(TAG, "Not hidden, no need to show");
            return;
        }

        try {
            // Hide icon layout
            if (iconLayout != null && iconLayout.getParent() != null) {
                windowManager.removeView(iconLayout);
            }

            // Show main layout
            if (mainLayout != null && mainLayout.getParent() == null) {
                windowManager.addView(mainLayout, mainLayoutParams);
            }

            // Show canvas layout
            if (canvasLayout != null && canvasLayout.getParent() == null) {
                windowManager.addView(canvasLayout, canvasLayoutParams);
            }

            isRecordingHidden = false;
            Log.d(TAG, "Overlay shown after recording");

        } catch (Exception e) {
            Log.e(TAG, "Error showing overlay after recording", e);
        }
    }

    /**
     * Check if overlay is currently hidden for recording
     */
    public boolean isHiddenForRecording() {
        return isRecordingHidden;
    }

    /**
     * Toggle recording hide state
     */
    public void toggleRecordingHide() {
        if (isRecordingHidden) {
            showAfterRecording();
        } else {
            hideForRecording();
        }
    }

    /**
     * Update layout parameters (useful when screen size changes)
     */
    public void updateLayoutParams(LayoutParams mainParams, LayoutParams iconParams, LayoutParams canvasParams) {
        this.mainLayoutParams = mainParams;
        this.iconLayoutParams = iconParams;
        this.canvasLayoutParams = canvasParams;

        // Re-apply layouts if currently visible
        if (!isRecordingHidden) {
            try {
                if (mainLayout != null && mainLayout.getParent() != null) {
                    windowManager.updateViewLayout(mainLayout, mainLayoutParams);
                }
                if (canvasLayout != null && canvasLayout.getParent() != null) {
                    windowManager.updateViewLayout(canvasLayout, canvasLayoutParams);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating layout parameters", e);
            }
        } else {
            // Update icon layout if hidden
            try {
                if (iconLayout != null && iconLayout.getParent() != null) {
                    windowManager.updateViewLayout(iconLayout, iconLayoutParams);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating icon layout parameters", e);
            }
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        // Ensure all views are removed
        try {
            if (mainLayout != null && mainLayout.getParent() != null) {
                windowManager.removeView(mainLayout);
            }
            if (iconLayout != null && iconLayout.getParent() != null) {
                windowManager.removeView(iconLayout);
            }
            if (canvasLayout != null && canvasLayout.getParent() != null) {
                windowManager.removeView(canvasLayout);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }

        isRecordingHidden = false;
    }
}