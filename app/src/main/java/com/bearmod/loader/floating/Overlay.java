package com.bearmod.loader.floating;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Overlay - UI overlay and touch hook management
 * Handles overlay positioning, touch events, and user interactions
 * Extracted from Floating.java to separate UI overlay concerns
 */
public class Overlay {

    private static final String TAG = "Overlay";

    private final Context context;
    private final WindowManager windowManager;

    // Overlay state
    private boolean isVisible = false;
    private boolean isMinimized = false;

    // Touch handling
    private float initialX;
    private float initialY;
    private float initialTouchX;
    private float initialTouchY;

    // Callbacks
    private OverlayCallback callback;

    public interface OverlayCallback {
        void onOverlayClick();
        void onOverlayMove(float x, float y);
        void onOverlayMinimize();
        void onOverlayMaximize();
    }

    public Overlay(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * Set overlay callback for handling events
     */
    public void setCallback(OverlayCallback callback) {
        this.callback = callback;
    }

    /**
     * Create overlay layout parameters
     */
    public LayoutParams createLayoutParams(int width, int height) {
        LayoutParams params = new LayoutParams(
            width,
            height,
            getOverlayType(),
            LayoutParams.FLAG_NOT_FOCUSABLE |
            LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            params.layoutInDisplayCutoutMode =
                LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        return params;
    }

    /**
     * Get appropriate overlay type
     */
    private int getOverlayType() {
        return LayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    /**
     * Handle touch events for overlay
     */
    public boolean handleTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = view.getLayoutParams() instanceof LayoutParams ?
                    ((LayoutParams) view.getLayoutParams()).x : 0;
                initialY = view.getLayoutParams() instanceof LayoutParams ?
                    ((LayoutParams) view.getLayoutParams()).y : 0;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (view.getLayoutParams() instanceof LayoutParams) {
                    LayoutParams params = (LayoutParams) view.getLayoutParams();
                    params.x = (int) (initialX + (event.getRawX() - initialTouchX));
                    params.y = (int) (initialY + (event.getRawY() - initialTouchY));

                    try {
                        windowManager.updateViewLayout(view, params);
                        if (callback != null) {
                            callback.onOverlayMove(params.x, params.y);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating overlay position", e);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                // Check if it was a click (minimal movement)
                float deltaX = Math.abs(event.getRawX() - initialTouchX);
                float deltaY = Math.abs(event.getRawY() - initialTouchY);

                if (deltaX < 10 && deltaY < 10) {
                    if (callback != null) {
                        callback.onOverlayClick();
                    }
                }
                return true;
        }
        return false;
    }

    /**
     * Show overlay
     */
    public void show(View overlayView, LayoutParams params) {
        if (isVisible) {
            Log.d(TAG, "Overlay already visible");
            return;
        }

        try {
            windowManager.addView(overlayView, params);
            isVisible = true;
            isMinimized = false;
            Log.d(TAG, "Overlay shown");
        } catch (Exception e) {
            Log.e(TAG, "Error showing overlay", e);
        }
    }

    /**
     * Hide overlay
     */
    public void hide(View overlayView) {
        if (!isVisible) {
            Log.d(TAG, "Overlay already hidden");
            return;
        }

        try {
            windowManager.removeView(overlayView);
            isVisible = false;
            Log.d(TAG, "Overlay hidden");
        } catch (Exception e) {
            Log.e(TAG, "Error hiding overlay", e);
        }
    }

    /**
     * Minimize overlay (show icon only)
     */
    public void minimize(View mainView, View iconView, LayoutParams iconParams) {
        if (isMinimized) return;

        try {
            // Hide main overlay
            if (mainView.getParent() != null) {
                windowManager.removeView(mainView);
            }

            // Show icon
            if (iconView.getParent() == null) {
                windowManager.addView(iconView, iconParams);
            }

            isMinimized = true;
            if (callback != null) {
                callback.onOverlayMinimize();
            }
            Log.d(TAG, "Overlay minimized");
        } catch (Exception e) {
            Log.e(TAG, "Error minimizing overlay", e);
        }
    }

    /**
     * Maximize overlay (show full interface)
     */
    public void maximize(View mainView, View iconView, LayoutParams mainParams) {
        if (!isMinimized) return;

        try {
            // Hide icon
            if (iconView.getParent() != null) {
                windowManager.removeView(iconView);
            }

            // Show main overlay
            if (mainView.getParent() == null) {
                windowManager.addView(mainView, mainParams);
            }

            isMinimized = false;
            if (callback != null) {
                callback.onOverlayMaximize();
            }
            Log.d(TAG, "Overlay maximized");
        } catch (Exception e) {
            Log.e(TAG, "Error maximizing overlay", e);
        }
    }

    /**
     * Update overlay position
     */
    public void updatePosition(View overlayView, int x, int y) {
        if (overlayView.getLayoutParams() instanceof LayoutParams) {
            LayoutParams params = (LayoutParams) overlayView.getLayoutParams();
            params.x = x;
            params.y = y;

            try {
                windowManager.updateViewLayout(overlayView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error updating overlay position", e);
            }
        }
    }

    /**
     * Check if overlay is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Check if overlay is minimized
     */
    public boolean isMinimized() {
        return isMinimized;
    }

    /**
     * Clean up overlay resources
     */
    public void cleanup() {
        isVisible = false;
        isMinimized = false;
        callback = null;
        Log.d(TAG, "Overlay cleanup completed");
    }
}