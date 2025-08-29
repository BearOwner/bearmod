package com.bearmod.loader.floating;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingGestureManager - Manages advanced gesture recognition
 * Handles complex gestures like pinch-to-zoom, swipe, and multi-touch operations
 */
public class FloatingGestureManager {

    private static final String TAG = "FloatingGestureManager";

    private final Context context;

    // Gesture detectors
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    // Gesture state
    private boolean isScaling = false;
    private float scaleFactor = 1.0f;
    private float lastScaleFactor = 1.0f;

    // Callbacks
    private GestureCallback callback;

    public interface GestureCallback {
        void onSwipeLeft();
        void onSwipeRight();
        void onSwipeUp();
        void onSwipeDown();
        void onScale(float scaleFactor, float focusX, float focusY);
        void onScaleEnd();
        void onFling(float velocityX, float velocityY);
    }

    public FloatingGestureManager(Context context) {
        this.context = context;
        initializeGestureDetectors();
        Logx.d("FloatingGestureManager initialized");
    }

    /**
     * Set gesture callback
     */
    public void setGestureCallback(GestureCallback callback) {
        this.callback = callback;
    }

    /**
     * Handle touch event for gesture recognition
     */
    public boolean handleTouchEvent(MotionEvent event) {
        boolean handled = false;

        // Handle scale gestures first
        if (scaleGestureDetector != null) {
            handled = scaleGestureDetector.onTouchEvent(event);
        }

        // Handle other gestures
        if (gestureDetector != null && !isScaling) {
            handled = gestureDetector.onTouchEvent(event) || handled;
        }

        return handled;
    }

    /**
     * Get current scale factor
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Reset scale factor to default
     */
    public void resetScale() {
        scaleFactor = 1.0f;
        lastScaleFactor = 1.0f;
        Logx.d("Scale factor reset to: " + scaleFactor);
    }

    /**
     * Check if currently scaling
     */
    public boolean isScaling() {
        return isScaling;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingGestureManager resources...");

            // Clear references
            gestureDetector = null;
            scaleGestureDetector = null;
            callback = null;

            Logx.d("FloatingGestureManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingGestureManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void initializeGestureDetectors() {
        // Initialize basic gesture detector
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (callback == null) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            callback.onSwipeRight();
                        } else {
                            callback.onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    // Vertical swipe
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            callback.onSwipeDown();
                        } else {
                            callback.onSwipeUp();
                        }
                        return true;
                    }
                }

                // General fling
                callback.onFling(velocityX, velocityY);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Single tap confirmed (not part of double tap)
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Double tap detected
                return true;
            }
        });

        // Initialize scale gesture detector
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (callback == null) return false;

                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f)); // Limit scale range

                isScaling = true;

                callback.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());

                Logx.d("Scale gesture: " + scaleFactor + " at (" + detector.getFocusX() + ", " + detector.getFocusY() + ")");
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastScaleFactor = scaleFactor;
                isScaling = true;
                Logx.d("Scale gesture began");
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
                if (callback != null) {
                    callback.onScaleEnd();
                }
                Logx.d("Scale gesture ended. Final scale: " + scaleFactor);
            }
        });
    }
}