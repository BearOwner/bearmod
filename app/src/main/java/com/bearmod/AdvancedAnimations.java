package com.bearmod;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced animation system for iOS-style floating UI
 * Provides smooth, performant animations with spring physics
 */
public class AdvancedAnimations {
    
    private static final String TAG = "AdvancedAnimations";
    
    // Animation constants
    private static final long DEFAULT_DURATION = 300;
    private static final long QUICK_DURATION = 150;
    private static final float SPRING_DAMPING = 0.7f;
    private static final float SPRING_STIFFNESS = 400f;
    
    // Animation constants for consistent timing
    public static final int DURATION_SHORT = 150;
    public static final int DURATION_MEDIUM = 300;
    public static final int DURATION_LONG = 500;
    
    // Spring constants for natural motion
    public static final float SPRING_STIFFNESS_LOW = SpringForce.STIFFNESS_LOW;
    public static final float SPRING_STIFFNESS_MEDIUM = SpringForce.STIFFNESS_MEDIUM;
    public static final float SPRING_STIFFNESS_HIGH = SpringForce.STIFFNESS_HIGH;
    
    public static final float SPRING_DAMPING_LOW = SpringForce.DAMPING_RATIO_LOW_BOUNCY;
    public static final float SPRING_DAMPING_MEDIUM = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY;
    public static final float SPRING_DAMPING_HIGH = SpringForce.DAMPING_RATIO_HIGH_BOUNCY;
    
    /**
     * Creates a smooth fade-in animation
     */
    public static void fadeIn(View view, long duration, Runnable onComplete) {
        if (view == null) return;
        
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) onComplete.run();
                }
            })
            .start();
    }
    
    /**
     * Creates a smooth fade-out animation
     */
    public static void fadeOut(View view, long duration, Runnable onComplete) {
        if (view == null) return;
        
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    if (onComplete != null) onComplete.run();
                }
            })
            .start();
    }
    
    /**
     * Creates an elastic bounce animation (iOS-style)
     */
    public static void elasticBounce(View view, float scale) {
        if (view == null) return;
        
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(QUICK_DURATION)
            .setInterpolator(new OvershootInterpolator(2f))
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(QUICK_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            })
            .start();
    }
    
    /**
     * Creates a spring-based selection animation
     */
    public static SpringAnimation createSpringAnimation(View view, DynamicAnimation.ViewProperty property, float targetValue) {
        SpringAnimation animation = new SpringAnimation(view, property, targetValue);
        animation.getSpring()
            .setDampingRatio(SPRING_DAMPING)
            .setStiffness(SPRING_STIFFNESS);
        return animation;
    }
    
    /**
     * Animates sidebar tab selection with ripple effect
     */
    public static void animateTabSelection(View selectedTab, View previousTab) {
        try {
            // Animate previous tab out
            if (previousTab != null) {
                previousTab.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .alpha(0.7f)
                    .setDuration(QUICK_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            }
            
            // Animate selected tab in
            if (selectedTab != null) {
                selectedTab.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .alpha(1f)
                    .setDuration(QUICK_DURATION)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .withEndAction(() -> {
                        selectedTab.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    })
                    .start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error animating tab selection", e);
        }
    }
    
    /**
     * Animates content transition between categories
     */
    public static void animateContentSwitch(ViewGroup contentFrame, Runnable onContentReady) {
        if (contentFrame == null) return;
        
        try {
            // Fade out current content
            contentFrame.animate()
                .alpha(0f)
                .translationX(-50f)
                .setDuration(QUICK_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // Update content
                    if (onContentReady != null) onContentReady.run();
                    
                    // Fade in new content
                    contentFrame.setTranslationX(50f);
                    contentFrame.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(DEFAULT_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                })
                .start();
        } catch (Exception e) {
            Log.e(TAG, "Error animating content switch", e);
            // Fallback: just run the content update
            if (onContentReady != null) onContentReady.run();
        }
    }
    
    /**
     * Creates a subtle breathing animation for logo
     */
    public static void startBreathingAnimation(View view) {
        if (view == null) return;
        
        try {
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1f);
            
            scaleUpX.setDuration(2000);
            scaleUpY.setDuration(2000);
            scaleDownX.setDuration(2000);
            scaleDownY.setDuration(2000);
            
            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(scaleUpX, scaleUpY);
            scaleUp.setInterpolator(new DecelerateInterpolator());
            
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(scaleDownX, scaleDownY);
            scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());
            
            AnimatorSet breathingAnimation = new AnimatorSet();
            breathingAnimation.playSequentially(scaleUp, scaleDown);
            breathingAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Restart the animation for continuous breathing effect
                    animation.start();
                }
            });
            
            view.setTag(R.id.breathing_animation, breathingAnimation);
            breathingAnimation.start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting breathing animation", e);
        }
    }
    
    /**
     * Stops breathing animation
     */
    public static void stopBreathingAnimation(View view) {
        if (view == null) return;
        
        try {
            AnimatorSet animation = (AnimatorSet) view.getTag(R.id.breathing_animation);
            if (animation != null) {
                animation.cancel();
                view.setScaleX(1f);
                view.setScaleY(1f);
                view.setTag(R.id.breathing_animation, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping breathing animation", e);
        }
    }
    
    /**
     * Creates ripple effect on touch
     */
    public static void createRippleEffect(View view, float centerX, float centerY) {
        if (view == null) return;
        
        try {
            // Simple scale-based ripple effect
            view.setPivotX(centerX);
            view.setPivotY(centerY);
            
            view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
                })
                .start();
        } catch (Exception e) {
            Log.e(TAG, "Error creating ripple effect", e);
        }
    }
    
    /**
     * Animates floating menu appearance
     */
    public static void animateMenuAppearance(View menuView) {
        if (menuView == null) return;
        
        try {
            menuView.setAlpha(0f);
            menuView.setScaleX(0.8f);
            menuView.setScaleY(0.8f);
            menuView.setTranslationY(50f);
            
            menuView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        } catch (Exception e) {
            Log.e(TAG, "Error animating menu appearance", e);
        }
    }
    
    /**
     * Animates floating menu disappearance
     */
    public static void animateMenuDisappearance(View menuView, Runnable onComplete) {
        if (menuView == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        try {
            menuView.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationY(50f)
                .setDuration(QUICK_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(onComplete)
                .start();
        } catch (Exception e) {
            Log.e(TAG, "Error animating menu disappearance", e);
            if (onComplete != null) onComplete.run();
        }
    }
    
    /**
     * Cancels all animations on a view
     */
    public static void cancelAllAnimations(View view) {
        if (view == null) return;
        
        try {
            view.animate().cancel();
            view.clearAnimation();
            
            // Reset transform properties
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setTranslationX(0f);
            view.setTranslationY(0f);
            view.setRotation(0f);
            view.setAlpha(1f);
        } catch (Exception e) {
            Log.e(TAG, "Error canceling animations", e);
        }
    }
    
    /**
     * Animates view entrance with staggered spring effect
     */
    public static void animateStaggeredEntrance(List<View> views, int delayBetween) {
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.setScaleX(0.8f);
            view.setScaleY(0.8f);
            
            SpringAnimation alphaAnim = createSpringAnimation(view, DynamicAnimation.ALPHA, 1f);
            SpringAnimation translationAnim = createSpringAnimation(view, DynamicAnimation.TRANSLATION_Y, 0f);
            SpringAnimation scaleXAnim = createSpringAnimation(view, DynamicAnimation.SCALE_X, 1f);
            SpringAnimation scaleYAnim = createSpringAnimation(view, DynamicAnimation.SCALE_Y, 1f);
            
            int delay = i * delayBetween;
            view.postDelayed(() -> {
                alphaAnim.start();
                translationAnim.start();
                scaleXAnim.start();
                scaleYAnim.start();
            }, delay);
        }
    }
    
    /**
     * Creates a morphing transition between two views
     */
    public static void morphViews(View fromView, View toView, long duration, Runnable onComplete) {
        // Get initial positions and sizes
        float fromX = fromView.getX();
        float fromY = fromView.getY();
        float fromWidth = fromView.getWidth();
        float fromHeight = fromView.getHeight();
        
        float toX = toView.getX();
        float toY = toView.getY();
        float toWidth = toView.getWidth();
        float toHeight = toView.getHeight();
        
        // Setup initial state
        toView.setX(fromX);
        toView.setY(fromY);
        toView.setScaleX(fromWidth / toWidth);
        toView.setScaleY(fromHeight / toHeight);
        toView.setAlpha(0f);
        toView.setVisibility(View.VISIBLE);
        
        // Create morph animation
        AnimatorSet morphSet = new AnimatorSet();
        morphSet.playTogether(
            ObjectAnimator.ofFloat(fromView, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(toView, "x", fromX, toX),
            ObjectAnimator.ofFloat(toView, "y", fromY, toY),
            ObjectAnimator.ofFloat(toView, "scaleX", fromWidth / toWidth, 1f),
            ObjectAnimator.ofFloat(toView, "scaleY", fromHeight / toHeight, 1f)
        );
        
        morphSet.setDuration(duration);
        morphSet.setInterpolator(new DecelerateInterpolator());
        morphSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fromView.setVisibility(View.GONE);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        morphSet.start();
    }
    
    /**
     * Creates a floating action button reveal animation
     */
    public static void revealFromFAB(View targetView, View fab, long duration) {
        // Get FAB center position
        float fabCenterX = fab.getX() + fab.getWidth() / 2f;
        float fabCenterY = fab.getY() + fab.getHeight() / 2f;
        
        // Setup initial state
        targetView.setScaleX(0f);
        targetView.setScaleY(0f);
        targetView.setPivotX(fabCenterX - targetView.getX());
        targetView.setPivotY(fabCenterY - targetView.getY());
        targetView.setAlpha(0f);
        targetView.setVisibility(View.VISIBLE);
        
        // Create reveal animation
        AnimatorSet revealSet = new AnimatorSet();
        revealSet.playTogether(
            ObjectAnimator.ofFloat(targetView, "scaleX", 0f, 1f),
            ObjectAnimator.ofFloat(targetView, "scaleY", 0f, 1f),
            ObjectAnimator.ofFloat(targetView, "alpha", 0f, 1f)
        );
        
        revealSet.setDuration(duration);
        revealSet.setInterpolator(new AnticipateOvershootInterpolator());
        revealSet.start();
    }
    
    /**
     * Creates a card flip animation
     */
    public static void flipCard(View frontView, View backView, long duration) {
        AnimatorSet flipOutSet = new AnimatorSet();
        AnimatorSet flipInSet = new AnimatorSet();
        
        // Flip out animation
        flipOutSet.playTogether(
            ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f),
            ObjectAnimator.ofFloat(frontView, "alpha", 1f, 0f)
        );
        flipOutSet.setDuration(duration / 2);
        flipOutSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Flip in animation
        flipInSet.playTogether(
            ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f),
            ObjectAnimator.ofFloat(backView, "alpha", 0f, 1f)
        );
        flipInSet.setDuration(duration / 2);
        flipInSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        flipOutSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frontView.setVisibility(View.GONE);
                backView.setVisibility(View.VISIBLE);
                backView.setRotationY(-90f);
                backView.setAlpha(0f);
                flipInSet.start();
            }
        });
        
        flipOutSet.start();
    }
    
    /**
     * Creates a parallax scroll effect for layered views
     */
    public static void setupParallaxEffect(View... layers) {
        if (layers.length < 2) return;
        
        View scrollView = layers[0]; // First view is the scroll container
        
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            for (int i = 1; i < layers.length; i++) {
                View layer = layers[i];
                float factor = 0.5f * i; // Each layer moves slower
                layer.setTranslationY(-scrollY * factor);
            }
        });
    }
    
    /**
     * Creates a liquid swipe transition between views
     */
    public static void liquidSwipeTransition(ViewGroup container, View fromView, View toView, boolean rightToLeft) {
        int containerWidth = container.getWidth();
        
        // Setup initial positions
        float startX = rightToLeft ? containerWidth : -containerWidth;
        toView.setTranslationX(startX);
        toView.setVisibility(View.VISIBLE);
        
        // Create wave effect using multiple segments
        List<ObjectAnimator> animators = new ArrayList<>();
        
        // From view animation
        ObjectAnimator fromAnim = ObjectAnimator.ofFloat(fromView, "translationX", 
            0f, rightToLeft ? -containerWidth : containerWidth);
        fromAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        animators.add(fromAnim);
        
        // To view animation
        ObjectAnimator toAnim = ObjectAnimator.ofFloat(toView, "translationX", startX, 0f);
        toAnim.setInterpolator(new OvershootInterpolator(0.8f));
        animators.add(toAnim);
        
        AnimatorSet liquidSet = new AnimatorSet();
        liquidSet.playTogether(animators.toArray(new ObjectAnimator[0]));
        liquidSet.setDuration(DURATION_LONG);
        liquidSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fromView.setVisibility(View.GONE);
                fromView.setTranslationX(0f);
                toView.setTranslationX(0f);
            }
        });
        
        liquidSet.start();
    }
    

    
    /**
     * Utility method to convert dp to pixels
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
} 