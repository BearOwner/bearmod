package com.bearmod.loader.component;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import com.bearmod.loader.utilities.ResourceProvider;
import com.bearmod.loader.utilities.Logx;

/**
 * NavigationManager - Handles tab navigation and UI section switching
 * Extracted from MainActivity to separate navigation concerns from business logic
 *
 * Responsibilities:
 * - Tab switching between Home and Settings
 * - UI section visibility management
 * - Navigation state tracking
 * - Icon tinting and visual feedback
 */
public class NavigationManager {

    private static final String TAG = "NavigationManager";

    private final ResourceProvider rp;
    private final ScrollView mainAppLayout;
    private final ScrollView settingsLayout;
    private final ImageButton navHome;
    private final ImageButton navSettings;

    private NavigationCallback callback;
    private NavigationState currentState = NavigationState.HOME;

    /**
     * Navigation state enum
     */
    public enum NavigationState {
        HOME, SETTINGS
    }

    /**
     * Interface for navigation events
     */
    public interface NavigationCallback {
        void onNavigationChanged(NavigationState newState);
    }

    public NavigationManager(ResourceProvider rp, ScrollView mainAppLayout, ScrollView settingsLayout,
                            ImageButton navHome, ImageButton navSettings) {
        this.rp = rp;
        this.mainAppLayout = mainAppLayout;
        this.settingsLayout = settingsLayout;
        this.navHome = navHome;
        this.navSettings = navSettings;
    }

    /**
     * Set navigation callback for events
     */
    public void setCallback(NavigationCallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize navigation with default state
     */
    public void initialize() {
        setupNavigationButtons();
        selectNavigation("Home");
        Logx.d("NavigationManager initialized");
    }

    /**
     * Setup navigation button listeners
     */
    private void setupNavigationButtons() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> selectNavigation("Home"));
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> selectNavigation("Settings"));
        }
    }

    /**
     * Select navigation section and update UI
     */
    public void selectNavigation(String section) {
        NavigationState newState = "Home".equals(section) ? NavigationState.HOME : NavigationState.SETTINGS;
        selectNavigation(newState);
    }

    /**
     * Select navigation state and update UI
     */
    public void selectNavigation(NavigationState state) {
        boolean isHome = state == NavigationState.HOME;

        // Update layout visibility
        if (mainAppLayout != null) {
            mainAppLayout.setVisibility(isHome ? View.VISIBLE : View.GONE);
        }
        if (settingsLayout != null) {
            settingsLayout.setVisibility(isHome ? View.GONE : View.VISIBLE);
        }

        // Update icon tinting
        updateNavigationIcons(isHome);

        // Track current state
        currentState = state;

        // Notify callback
        if (callback != null) {
            callback.onNavigationChanged(state);
        }

        Logx.d("Navigation selected: " + state);
    }

    /**
     * Update navigation icon colors based on selection
     */
    private void updateNavigationIcons(boolean isHome) {
        try {
            // Use default colors for now - can be made configurable later
            int primaryColor = 0xFFFFFFFF;   // White
            int secondaryColor = 0xFF8E8E93; // Gray

            if (navHome != null) {
                navHome.setColorFilter(isHome ? primaryColor : secondaryColor);
            }
            if (navSettings != null) {
                navSettings.setColorFilter(!isHome ? primaryColor : secondaryColor);
            }
        } catch (Exception e) {
            Logx.w("Error updating navigation icons", e);
        }
    }

    /**
     * Get current navigation state
     */
    public NavigationState getCurrentState() {
        return currentState;
    }

    /**
     * Check if currently on home section
     */
    public boolean isOnHome() {
        return currentState == NavigationState.HOME;
    }

    /**
     * Check if currently on settings section
     */
    public boolean isOnSettings() {
        return currentState == NavigationState.SETTINGS;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        callback = null;
        Log.d(TAG, "NavigationManager cleanup completed");
    }
}