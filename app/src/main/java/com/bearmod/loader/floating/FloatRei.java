package com.bearmod.loader.floating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;

/**
 * FloatRei - Specialized renderer for floating UI elements
 * Handles advanced rendering effects and animations
 * Extracted from Floating.java to separate specialized rendering logic
 */
public class FloatRei {

    private final Context context;
    private Paint reiPaint;
    private float scaleFactor = 1.0f;
    private boolean isAnimating = false;

    public FloatRei(Context context) {
        this.context = context;
        initializeReiPaint();
    }

    /**
     * Initialize paint for Rei rendering
     */
    private void initializeReiPaint() {
        reiPaint = new Paint();
        reiPaint.setAntiAlias(true);
        reiPaint.setDither(true);
        reiPaint.setStyle(Paint.Style.FILL);
        reiPaint.setTypeface(Typeface.DEFAULT_BOLD);
        reiPaint.setTextSize(convertSizeToDp(14));
    }

    /**
     * Render Rei elements on canvas
     */
    public void renderReiElements(Canvas canvas, float centerX, float centerY) {
        if (canvas == null) return;

        // Save canvas state
        canvas.save();

        // Apply scaling transformation
        canvas.scale(scaleFactor, scaleFactor, centerX, centerY);

        // Render Rei-specific elements
        drawReiCore(canvas, centerX, centerY);
        drawReiEffects(canvas, centerX, centerY);

        // Restore canvas state
        canvas.restore();
    }

    /**
     * Draw core Rei elements
     */
    private void drawReiCore(Canvas canvas, float centerX, float centerY) {
        reiPaint.setColor(Color.parseColor("#FF6B6B"));
        reiPaint.setAlpha(180);

        // Draw Rei core circle
        float coreRadius = convertSizeToDp(25);
        canvas.drawCircle(centerX, centerY, coreRadius, reiPaint);

        // Draw Rei inner highlight
        reiPaint.setColor(Color.WHITE);
        reiPaint.setAlpha(120);
        canvas.drawCircle(centerX - convertSizeToDp(8), centerY - convertSizeToDp(8),
                         convertSizeToDp(8), reiPaint);
    }

    /**
     * Draw Rei special effects
     */
    private void drawReiEffects(Canvas canvas, float centerX, float centerY) {
        reiPaint.setStyle(Paint.Style.STROKE);
        reiPaint.setStrokeWidth(convertSizeToDp(2));
        reiPaint.setColor(Color.parseColor("#4ECDC4"));
        reiPaint.setAlpha(150);

        // Draw rotating effect rings
        for (int i = 1; i <= 3; i++) {
            float radius = convertSizeToDp(25 + i * 8);
            canvas.drawCircle(centerX, centerY, radius, reiPaint);
        }

        // Reset paint style
        reiPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Start Rei animation
     */
    public void startReiAnimation() {
        isAnimating = true;
        // Animation logic would be implemented here
        // This could include scaling, rotation, or other effects
    }

    /**
     * Stop Rei animation
     */
    public void stopReiAnimation() {
        isAnimating = false;
        scaleFactor = 1.0f;
    }

    /**
     * Set Rei scale factor
     */
    public void setScaleFactor(float scale) {
        this.scaleFactor = Math.max(0.1f, Math.min(scale, 3.0f));
    }

    /**
     * Get current scale factor
     */
    public float getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Check if Rei is currently animating
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Convert dp to pixels
     */
    private float convertSizeToDp(int dp) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }

    /**
     * Update Rei configuration
     */
    public void updateReiConfig(boolean enableEffects, int effectColor) {
        // Update Rei-specific configuration
        reiPaint.setColor(effectColor);

        if (enableEffects) {
            startReiAnimation();
        } else {
            stopReiAnimation();
        }
    }
}