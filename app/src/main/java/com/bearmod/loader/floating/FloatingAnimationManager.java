package com.bearmod.loader.floating;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingAnimationManager - Manages iOS-style animations and transitions
 * Handles smooth animations for UI elements, transitions, and visual feedback
 */
public class FloatingAnimationManager {

    private static final String TAG = "FloatingAnimationManager";

    private final Context context;

    // Animation constants
    private static final int ANIMATION_DURATION_FAST = 150;
    private static final int ANIMATION_DURATION_NORMAL = 300;
    private static final int ANIMATION_DURATION_SLOW = 500;

    public FloatingAnimationManager(Context context) {
        this.context = context;
        Logx.d("FloatingAnimationManager initialized");
    }

    /**
     * Animate view scale with bounce effect
     */
    public void animateScaleBounce(View view, float targetScale) {
        animateScaleBounce(view, targetScale, ANIMATION_DURATION_NORMAL);
    }

    /**
     * Animate view scale with bounce effect and custom duration
     */
    public void animateScaleBounce(View view, float targetScale, int duration) {
        if (view == null) return;

        try {
            view.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

            Logx.d("Scale bounce animation started: " + targetScale);

        } catch (Exception e) {
            Logx.e("Error animating scale bounce: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view press effect (iOS-style)
     */
    public void animatePress(View view) {
        animatePress(view, 0.95f, ANIMATION_DURATION_FAST);
    }

    /**
     * Animate view press effect with custom scale
     */
    public void animatePress(View view, float pressScale, int duration) {
        if (view == null) return;

        try {
            view.animate()
                .scaleX(pressScale)
                .scaleY(pressScale)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // Restore to original scale
                    animateRestore(view, duration);
                })
                .start();

            Logx.d("Press animation started: " + pressScale);

        } catch (Exception e) {
            Logx.e("Error animating press: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view restoration to original scale
     */
    public void animateRestore(View view) {
        animateRestore(view, ANIMATION_DURATION_FAST);
    }

    /**
     * Animate view restoration with custom duration
     */
    public void animateRestore(View view, int duration) {
        if (view == null) return;

        try {
            view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .start();

            Logx.d("Restore animation started");

        } catch (Exception e) {
            Logx.e("Error animating restore: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view fade in
     */
    public void animateFadeIn(View view) {
        animateFadeIn(view, ANIMATION_DURATION_NORMAL);
    }

    /**
     * Animate view fade in with custom duration
     */
    public void animateFadeIn(View view, int duration) {
        if (view == null) return;

        try {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

            Logx.d("Fade in animation started");

        } catch (Exception e) {
            Logx.e("Error animating fade in: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view fade out
     */
    public void animateFadeOut(View view) {
        animateFadeOut(view, ANIMATION_DURATION_NORMAL);
    }

    /**
     * Animate view fade out with custom duration
     */
    public void animateFadeOut(View view, int duration) {
        if (view == null) return;

        try {
            view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();

            Logx.d("Fade out animation started");

        } catch (Exception e) {
            Logx.e("Error animating fade out: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view slide in from left
     */
    public void animateSlideInFromLeft(View view, int distance) {
        animateSlideInFromLeft(view, distance, ANIMATION_DURATION_NORMAL);
    }

    /**
     * Animate view slide in from left with custom duration
     */
    public void animateSlideInFromLeft(View view, int distance, int duration) {
        if (view == null) return;

        try {
            view.setTranslationX(-distance);
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);

            view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

            Logx.d("Slide in from left animation started: " + distance);

        } catch (Exception e) {
            Logx.e("Error animating slide in from left: " + e.getMessage(), e);
        }
    }

    /**
     * Animate view slide out to right
     */
    public void animateSlideOutToRight(View view, int distance) {
        animateSlideOutToRight(view, distance, ANIMATION_DURATION_NORMAL);
    }

    /**
     * Animate view slide out to right with custom duration
     */
    public void animateSlideOutToRight(View view, int distance, int duration) {
        if (view == null) return;

        try {
            view.animate()
                .translationX(distance)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setTranslationX(0f);
                    view.setAlpha(1f);
                })
                .start();

            Logx.d("Slide out to right animation started: " + distance);

        } catch (Exception e) {
            Logx.e("Error animating slide out to right: " + e.getMessage(), e);
        }
    }

    /**
     * Animate tab selection with iOS-style feedback
     */
    public void animateTabSelection(View tabView) {
        if (tabView == null) return;

        try {
            // Scale down then bounce back
            tabView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(ANIMATION_DURATION_FAST)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    tabView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(ANIMATION_DURATION_FAST)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
                })
                .start();

            Logx.d("Tab selection animation started");

        } catch (Exception e) {
            Logx.e("Error animating tab selection: " + e.getMessage(), e);
        }
    }

    /**
     * Animate color transition
     */
    public void animateColorTransition(View view, int fromColor, int toColor, int duration) {
        if (view == null) return;

        try {
            ValueAnimator colorAnimator = ValueAnimator.ofArgb(fromColor, toColor);
            colorAnimator.setDuration(duration);
            colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            colorAnimator.addUpdateListener(animation -> {
                // This would be used with views that support background color changes
                // Implementation depends on the specific view type
            });
            colorAnimator.start();

            Logx.d("Color transition animation started");

        } catch (Exception e) {
            Logx.e("Error animating color transition: " + e.getMessage(), e);
        }
    }

    /**
     * Create a chained animation sequence
     */
    public Animator createAnimationSequence(AnimationStep... steps) {
        if (steps == null || steps.length == 0) {
            return null;
        }

        try {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(ANIMATION_DURATION_NORMAL);

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Execute animation steps in sequence
                    for (AnimationStep step : steps) {
                        if (step != null && step.action != null) {
                            step.action.run();
                        }
                    }
                }
            });

            return animator;

        } catch (Exception e) {
            Logx.e("Error creating animation sequence: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Animation step for sequences
     */
    public static class AnimationStep {
        public Runnable action;
        public int delay;

        public AnimationStep(Runnable action) {
            this(action, 0);
        }

        public AnimationStep(Runnable action, int delay) {
            this.action = action;
            this.delay = delay;
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingAnimationManager resources...");
            // No specific cleanup needed for this component
            Logx.d("FloatingAnimationManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingAnimationManager cleanup: " + e.getMessage(), e);
        }
    }
}