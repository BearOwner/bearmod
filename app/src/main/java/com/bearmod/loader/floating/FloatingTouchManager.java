package com.bearmod.loader.floating;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingTouchManager - Manages touch interactions and gesture handling
 * Handles touch events, drag operations, and gesture recognition for floating UI
 */
public class FloatingTouchManager {

    private static final String TAG = "FloatingTouchManager";

    private final Context context;

    // Touch state
    private float lastX, lastY;
    private float pressedX, pressedY;
    private float deltaX, deltaY;
    private boolean isDragging = false;
    private long touchStartTime = 0;

    // Gesture detection
    private GestureDetector gestureDetector;
    private TouchCallback callback;

    public interface TouchCallback {
        void onTap(float x, float y);
        void onDoubleTap(float x, float y);
        void onLongPress(float x, float y);
        void onDrag(float deltaX, float deltaY);
        void onDragEnd();
    }

    public FloatingTouchManager(Context context) {
        this.context = context;
        initializeGestureDetector();
        Logx.d("FloatingTouchManager initialized");
    }

    /**
     * Set touch callback
     */
    public void setTouchCallback(TouchCallback callback) {
        this.callback = callback;
    }

    /**
     * Handle touch event
     */
    public boolean handleTouch(View view, MotionEvent event) {
        // Update gesture detector
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }

        // Handle custom touch logic
        return handleCustomTouch(view, event);
    }

    /**
     * Create touch listener for a view
     */
    public View.OnTouchListener createTouchListener() {
        return new View.OnTouchListener() {
            private float initialX, initialY;
            private float viewX, viewY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(v, event);
            }
        };
    }

    /**
     * Trigger haptic feedback
     */
    public void triggerHapticFeedback(String type) {
        try {
            Vibrator vibrator = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vibrator = vm.getDefaultVibrator();
                }
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = switch (type.toLowerCase()) {
                    case "light" -> VibrationEffect.createOneShot(25, 50);
                    case "medium" -> VibrationEffect.createOneShot(50, 100);
                    case "heavy" -> VibrationEffect.createOneShot(75, 150);
                    case "success" -> VibrationEffect.createWaveform(new long[]{0, 50, 50, 50}, -1);
                    case "warning" -> VibrationEffect.createWaveform(new long[]{0, 100, 100, 100}, -1);
                    case "error" -> VibrationEffect.createWaveform(new long[]{0, 150, 50, 150}, -1);
                    default -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
                };
                vibrator.vibrate(effect);
            }
        } catch (Exception e) {
            Logx.e("Error triggering haptic feedback: " + e.getMessage(), e);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingTouchManager resources...");

            // Clear references
            gestureDetector = null;
            callback = null;

            Logx.d("FloatingTouchManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingTouchManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void initializeGestureDetector() {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (callback != null) {
                    callback.onTap(e.getX(), e.getY());
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                triggerHapticFeedback("MEDIUM");
                if (callback != null) {
                    callback.onDoubleTap(e.getX(), e.getY());
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                triggerHapticFeedback("HEAVY");
                if (callback != null) {
                    callback.onLongPress(e.getX(), e.getY());
                }
            }
        });
    }

    private boolean handleCustomTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                return handleActionMove(view, event);

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUp(view, event);
                return true;

            default:
                return false;
        }
    }

    private void handleActionDown(MotionEvent event) {
        touchStartTime = System.currentTimeMillis();
        pressedX = event.getRawX();
        pressedY = event.getRawY();
        lastX = event.getRawX();
        lastY = event.getRawY();
        isDragging = false;

        // Trigger light haptic feedback on touch
        triggerHapticFeedback("LIGHT");

        Logx.d("Touch started at: " + pressedX + ", " + pressedY);
    }

    private boolean handleActionMove(View view, MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // Check if this is a drag operation
        float deltaX = Math.abs(currentX - pressedX);
        float deltaY = Math.abs(currentY - pressedY);

        if (deltaX > 10 || deltaY > 10) {
            isDragging = true;

            // Calculate movement delta
            float moveDeltaX = currentX - lastX;
            float moveDeltaY = currentY - lastY;

            // Notify callback of drag
            if (callback != null) {
                callback.onDrag(moveDeltaX, moveDeltaY);
            }

            // Update last position
            lastX = currentX;
            lastY = currentY;

            return true;
        }

        return false;
    }

    private void handleActionUp(View view, MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // Check if this was a tap (not a drag)
        float deltaX = Math.abs(currentX - pressedX);
        float deltaY = Math.abs(currentY - pressedY);

        if (!isDragging && deltaX < 10 && deltaY < 10) {
            // This was a tap, gesture detector should handle it
            Logx.d("Tap detected at: " + currentX + ", " + currentY);
        } else if (isDragging) {
            // This was a drag, notify end
            if (callback != null) {
                callback.onDragEnd();
            }
            Logx.d("Drag ended");
        }

        isDragging = false;
    }
}