package com.bearmod.loader.floating;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingFacade - Central coordinator for all floating overlay components
 * Following the facade pattern established in Phase 3 for modular architecture
 */
public class FloatingFacade {

    private static final String TAG = "FloatingFacade";

    // Component references
    private final Context context;
    private FloatingUIManager uiManager;
    private FloatingTabManager tabManager;
    private FloatingMenuManager menuManager;
    private FloatingServiceManager serviceManager;
    private FloatingAuthManager authManager;
    private FloatingRenderer renderer;
    private FloatingConfigManager configManager;
    private FloatingTouchManager touchManager;
    private FloatingGestureManager gestureManager;
    private FloatingAnimationManager animationManager;
    private FloatingVisualEffectsManager visualEffectsManager;

    // State management
    private boolean isInitialized = false;
    private boolean isServiceRunning = false;

    public FloatingFacade(Context context) {
        this.context = context;
        Logx.d("FloatingFacade initialized with context: " + context.getClass().getSimpleName());
    }

    /**
     * Initialize all floating overlay components
     * Called during service onCreate()
     */
    public void initialize() {
        try {
            Logx.d("Initializing FloatingFacade components...");

            // Initialize core components in dependency order
            initializeCoreComponents();
            initializeUIManager();
            initializeServiceManager();
            initializeAuthManager();
            initializeConfigManager();
            initializeRenderer();
            initializeTouchManager();
            initializeGestureManager();
            initializeAnimationManager();
            initializeVisualEffectsManager();

            isInitialized = true;
            Logx.d("FloatingFacade initialization completed successfully");

        } catch (Exception e) {
            Logx.e("Error initializing FloatingFacade: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize FloatingFacade", e);
        }
    }

    /**
     * Start the floating overlay service
     * Called when service starts
     */
    public void startService() {
        if (!isInitialized) {
            throw new IllegalStateException("FloatingFacade must be initialized before starting service");
        }

        try {
            Logx.d("Starting floating overlay service...");

            // Start service components
            if (serviceManager != null) {
                serviceManager.startService();
            }

            if (renderer != null) {
                renderer.startRendering();
            }

            if (uiManager != null) {
                uiManager.showInterface();
            }

            isServiceRunning = true;
            Logx.d("Floating overlay service started successfully");

        } catch (Exception e) {
            Logx.e("Error starting floating service: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the floating overlay service
     * Called during service onDestroy()
     */
    public void stopService() {
        try {
            Logx.d("Stopping floating overlay service...");

            // Stop service components in reverse order
            if (renderer != null) {
                renderer.stopRendering();
            }

            if (serviceManager != null) {
                serviceManager.stopService();
            }

            if (uiManager != null) {
                uiManager.hideInterface();
            }

            isServiceRunning = false;
            Logx.d("Floating overlay service stopped successfully");

        } catch (Exception e) {
            Logx.e("Error stopping floating service: " + e.getMessage(), e);
        }
    }

    /**
     * Handle service intent
     */
    public void handleIntent(Intent intent) {
        if (serviceManager != null) {
            serviceManager.handleIntent(intent);
        }
    }

    /**
     * Check if user is authenticated for floating features
     */
    public boolean isUserAuthenticated() {
        return authManager != null && authManager.isUserAuthenticated();
    }

    /**
     * Check if stealth mode is active
     */
    public boolean isStealthModeActive() {
        return authManager != null && authManager.isStealthModeActive();
    }

    /**
     * Toggle stealth mode
     */
    public void toggleStealthMode() {
        if (authManager != null) {
            authManager.toggleStealthMode();
        }
    }

    /**
     * Show floating interface
     */
    public void showInterface() {
        if (uiManager != null) {
            uiManager.showInterface();
        }
    }

    /**
     * Hide floating interface
     */
    public void hideInterface() {
        if (uiManager != null) {
            uiManager.hideInterface();
        }
    }

    /**
     * Switch to specific tab
     */
    public void switchToTab(int tabIndex) {
        if (tabManager != null) {
            tabManager.switchToTab(tabIndex);
        }
    }

    /**
     * Get current tab index
     */
    public int getCurrentTabIndex() {
        return tabManager != null ? tabManager.getCurrentTabIndex() : 0;
    }

    /**
     * Update configuration
     */
    public void updateConfiguration(String key, Object value) {
        if (configManager != null) {
            configManager.updateConfiguration(key, value);
        }
    }

    /**
     * Get configuration value
     */
    public String getConfiguration(String key) {
        return configManager != null ? configManager.getConfiguration(key) : null;
    }

    /**
     * Save current configuration
     */
    public void saveConfiguration() {
        if (configManager != null) {
            configManager.saveConfiguration();
        }
    }

    /**
     * Load configuration from storage
     */
    public void loadConfiguration() {
        if (configManager != null) {
            configManager.loadConfiguration();
        }
    }

    /**
     * Get ESP rendering surface
     */
    public ESPView getEspView() {
        return renderer != null ? renderer.getEspView() : null;
    }

    /**
     * Trigger haptic feedback
     */
    public void triggerHapticFeedback(String type) {
        if (visualEffectsManager != null) {
            visualEffectsManager.triggerHapticFeedback(type);
        }
    }

    /**
     * Cleanup all resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingFacade resources...");

            // Cleanup components in reverse initialization order
            if (visualEffectsManager != null) {
                visualEffectsManager.cleanup();
            }

            if (animationManager != null) {
                animationManager.cleanup();
            }

            if (gestureManager != null) {
                gestureManager.cleanup();
            }

            if (touchManager != null) {
                touchManager.cleanup();
            }

            if (renderer != null) {
                renderer.cleanup();
            }

            if (configManager != null) {
                configManager.cleanup();
            }

            if (authManager != null) {
                authManager.cleanup();
            }

            if (serviceManager != null) {
                serviceManager.cleanup();
            }

            if (menuManager != null) {
                menuManager.cleanup();
            }

            if (tabManager != null) {
                tabManager.cleanup();
            }

            if (uiManager != null) {
                uiManager.cleanup();
            }

            isInitialized = false;
            Logx.d("FloatingFacade cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingFacade cleanup: " + e.getMessage(), e);
        }
    }

    // Private initialization methods

    private void initializeCoreComponents() {
        // Initialize tab manager first as it's needed by UI manager
        tabManager = new FloatingTabManager(context);
        menuManager = new FloatingMenuManager(context);
    }

    private void initializeUIManager() {
        uiManager = new FloatingUIManager(context, tabManager, menuManager);
    }

    private void initializeServiceManager() {
        serviceManager = new FloatingServiceManager(context);
    }

    private void initializeAuthManager() {
        authManager = new FloatingAuthManager(context);
    }

    private void initializeConfigManager() {
        configManager = new FloatingConfigManager(context);
    }

    private void initializeRenderer() {
        renderer = new FloatingRenderer(context);
    }

    private void initializeTouchManager() {
        touchManager = new FloatingTouchManager(context);
    }

    private void initializeGestureManager() {
        gestureManager = new FloatingGestureManager(context);
    }

    private void initializeAnimationManager() {
        animationManager = new FloatingAnimationManager(context);
    }

    private void initializeVisualEffectsManager() {
        visualEffectsManager = new FloatingVisualEffectsManager(context);
    }

    // Getters for component access (if needed by legacy code)

    public FloatingUIManager getUiManager() {
        return uiManager;
    }

    public FloatingTabManager getTabManager() {
        return tabManager;
    }

    public FloatingMenuManager getMenuManager() {
        return menuManager;
    }

    public FloatingServiceManager getServiceManager() {
        return serviceManager;
    }

    public FloatingAuthManager getAuthManager() {
        return authManager;
    }

    public FloatingRenderer getRenderer() {
        return renderer;
    }

    public FloatingConfigManager getConfigManager() {
        return configManager;
    }

    public FloatingTouchManager getTouchManager() {
        return touchManager;
    }

    public FloatingGestureManager getGestureManager() {
        return gestureManager;
    }

    public FloatingAnimationManager getAnimationManager() {
        return animationManager;
    }

    public FloatingVisualEffectsManager getVisualEffectsManager() {
        return visualEffectsManager;
    }
}