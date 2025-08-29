package com.bearmod.loader.component;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bearmod.loader.utilities.Logx;

/**
 * SplashAnimationManager - Handles splash screen animations and UI state management
 *
 * Extracted from SplashActivity.java to separate animation concerns from activity lifecycle.
 * Provides clean interface for animation control and status updates.
 *
 * Migrated from com.bearmod.activity.SplashActivity (449 LOC → focused animation manager)
 */
public class SplashAnimationManager {
    private static final String TAG = "SplashAnimationManager";

    // UI Components
    private final ImageView logo;
    private final TextView title;
    private final TextView status;
    private final ProgressBar logoProgress;

    // Animation state
    private AnimatorSet currentAnimation;
    private boolean isAnimating = false;

    // Animation callbacks
    public interface AnimationCallback {
        void onAnimationComplete();
        void onAnimationCancelled();
    }

    private AnimationCallback callback;

    public SplashAnimationManager(ImageView logo, TextView title, TextView status, ProgressBar logoProgress) {
        this.logo = logo;
        this.title = title;
        this.status = status;
        this.logoProgress = logoProgress;

        initializeViewStates();
    }

    /**
     * Initialize view states for animation
     */
    private void initializeViewStates() {
        logo.setScaleX(0);
        logo.setScaleY(0);
        title.setAlpha(0);
        status.setAlpha(0);
        logoProgress.setProgress(0);
    }

    /**
     * Set animation callback for completion/cancellation events
     */
    public void setAnimationCallback(AnimationCallback callback) {
        this.callback = callback;
    }

    /**
     * Start the complete splash animation sequence
     */
    @SuppressLint("SetTextI18n")
    public void startSplashAnimation() {
        if (isAnimating) {
            Logx.w("Animation already in progress");
            return;
        }

        isAnimating = true;
        AnimatorSet animatorSet = new AnimatorSet();

        // Logo scale animation with bounce
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());

        // Progress bar animation
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.addUpdateListener(animation ->
            logoProgress.setProgress((Integer) animation.getAnimatedValue()));

        // Title and status fade in
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
        ObjectAnimator statusFade = ObjectAnimator.ofFloat(status, "alpha", 0f, 1f);

        // Logo rotation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logo, "rotation", 0f, 360f);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());

        // Combine animations
        AnimatorSet initialSet = new AnimatorSet();
        initialSet.playTogether(scaleX, scaleY, progressAnimator, rotation);

        animatorSet.playSequentially(initialSet, titleFade, statusFade);

        // Set durations
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        progressAnimator.setDuration(1500);
        rotation.setDuration(1000);
        titleFade.setDuration(500);
        statusFade.setDuration(500);

        // Animation listener
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                status.setText("Initializing...");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                isAnimating = false;
                if (callback != null) {
                    callback.onAnimationComplete();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                isAnimating = false;
                if (callback != null) {
                    callback.onAnimationCancelled();
                }
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });

        currentAnimation = animatorSet;
        animatorSet.start();

        Logx.d("Splash animation started");
    }

    /**
     * Update status text during animation/initialization
     */
    @SuppressLint("SetTextI18n")
    public void updateStatus(String message) {
        if (status != null) {
            status.setText(message);
        }
    }

    /**
     * Cancel current animation
     */
    public void cancelAnimation() {
        if (currentAnimation != null && isAnimating) {
            currentAnimation.cancel();
            Logx.d("Splash animation cancelled");
        }
    }

    /**
     * Check if animation is currently running
     */
    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        cancelAnimation();
        isAnimating = false;
    }
}