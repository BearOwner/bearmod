package com.bearmod.loader.component;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bearmod.TargetAppManager;
import com.bearmod.loader.utilities.Logx;

/**
 * MainFacade - Coordinator for MainActivity components
 *
 * Orchestrates MainActivity with LoginFacade and other modular components.
 * Provides clean interface for authentication checks and navigation.
 *
 * Created as part of Phase 8: MainActivity Integration
 */
public class MainFacade {
    private static final String TAG = "MainFacade";

    // Core components
    private final LoginFacade loginFacade;
    private final NavigationManager navigationManager;
    private final RegionSelector regionSelector;
    private final LicenseTimer licenseTimer;
    private final ServiceController serviceController;

    // Context and callbacks
    private final AppCompatActivity activity;
    private final MainCallback callback;

    /**
     * Main callback interface for activity communication
     */
    public interface MainCallback {
        void onAuthenticationRequired();
        void onAuthenticationSuccess();
        void onServiceStartRequested();
        void onServiceStopRequested();
        void onExitRequested();
        void onPermissionResult(int requestCode, int resultCode, Intent data);
    }

    public MainFacade(AppCompatActivity activity, MainCallback callback,
                     // NavigationManager parameters
                     ScrollView mainAppLayout, ScrollView settingsLayout,
                     ImageButton navHome, ImageButton navSettings,
                     // RegionSelector parameters
                     Button regionGlobal, Button regionKorea, Button regionVietnam,
                     Button regionTaiwan, Button regionIndia,
                     // LicenseTimer parameters
                     TextView countdownDays, TextView countdownHours,
                     TextView countdownMinutes, TextView countdownSeconds,
                     // ServiceController parameters
                     TextView serviceStatus, Button startButton, Button stopButton,
                     TargetAppManager targetAppManager) {
        this.activity = activity;
        this.callback = callback;

        // Initialize components with proper parameters
        this.loginFacade = new LoginFacade(activity, null, new LoginFacade.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                callback.onAuthenticationSuccess();
            }

            @Override
            public void onLoginFailure(String error) {
                callback.onAuthenticationRequired();
            }

            @Override
            public void onBackPressed() {
                // Handle back press in main activity context
            }

            @Override
            public void onPermissionResult(int requestCode, int resultCode, Intent data) {
                callback.onPermissionResult(requestCode, resultCode, data);
            }
        });

        this.navigationManager = new NavigationManager(com.bearmod.loader.utilities.ResourceProvider.from(activity), mainAppLayout, settingsLayout, navHome, navSettings);
        this.regionSelector = new RegionSelector(regionGlobal, regionKorea, regionVietnam, regionTaiwan, regionIndia);
        this.licenseTimer = new LicenseTimer(countdownDays, countdownHours, countdownMinutes, countdownSeconds,
                                           new LicenseTimer.TimerCallback() {
            @Override
            public String getExpirationDate() {
                // This should be implemented by the activity
                return "2025-12-31 23:59:59"; // Default fallback
            }

            @Override
            public void onTimerExpired() {
                callback.onAuthenticationRequired();
            }
        });
        this.serviceController = new ServiceController(activity, targetAppManager, serviceStatus, startButton, stopButton,
                                                     new ServiceController.ServiceCallback() {
            @Override
            public void showLoadingSpinner(String message) {
                // This should be implemented by the activity
            }

            @Override
            public void updateLoadingMessage(String message) {
                // This should be implemented by the activity
            }

            @Override
            public void hideLoadingSpinner() {
                // This should be implemented by the activity
            }

            @Override
            public void showPackageInstallationGuidance(String packageName) {
                // This should be implemented by the activity
            }

            @Override
            public boolean checkPermissions() {
                // This should be implemented by the activity
                return true; // Default fallback
            }

            @Override
            public void onServiceStarted() {
                callback.onServiceStartRequested();
            }

            @Override
            public void onServiceStopped() {
                callback.onServiceStopRequested();
            }

            @Override
            public void onServiceError(String error) {
                Logx.e("Service error: " + error);
            }
        });

        setupComponentCallbacks();
    }

    /**
     * Setup callbacks between components
     */
    private void setupComponentCallbacks() {
        // Setup navigation callbacks
        navigationManager.setCallback(new NavigationManager.NavigationCallback() {
            @Override
            public void onNavigationChanged(com.bearmod.loader.component.NavigationManager.NavigationState newState) {
                // Handle navigation state changes
                Logx.d("Navigation changed to: " + newState);
            }
        });

        // Setup region selector callbacks
        regionSelector.setCallback(new RegionSelector.RegionCallback() {
            @Override
            public void onRegionSelected(String packageName, String regionName) {
                // Handle region selection
                Logx.d("MAIN_FACADE_REGION_SELECTED: " + packageName + " (" + regionName + ")");
            }
        });

        // Setup service controller callbacks
        serviceController.setCallback(new ServiceController.ServiceCallback() {
            @Override
            public void showLoadingSpinner(String message) {
                // This should be implemented by the activity
            }

            @Override
            public void updateLoadingMessage(String message) {
                // This should be implemented by the activity
            }

            @Override
            public void hideLoadingSpinner() {
                // This should be implemented by the activity
            }

            @Override
            public void showPackageInstallationGuidance(String packageName) {
                // This should be implemented by the activity
            }

            @Override
            public boolean checkPermissions() {
                // This should be implemented by the activity
                return true; // Default fallback
            }

            @Override
            public void onServiceStarted() {
                Logx.d("MAIN_FACADE_SERVICE_STARTED");
            }

            @Override
            public void onServiceStopped() {
                Logx.d("MAIN_FACADE_SERVICE_STOPPED");
            }

            @Override
            public void onServiceError(String error) {
                Logx.e("MAIN_FACADE_SERVICE_ERROR: " + error);
            }
        });
    }

    /**
     * Check if user has valid authentication
     */
    public boolean hasValidAuthentication() {
        return loginFacade.hasValidStoredAuth();
    }

    /**
     * Handle authentication requirement
     */
    public void handleAuthenticationRequired() {
        Logx.d("MAIN_FACADE_AUTH_REQUIRED");
        callback.onAuthenticationRequired();
    }

    /**
     * Initialize native library
     */
    public void initializeNativeLibrary() {
        try {
            // Use the new server component for native initialization
            com.bearmod.loader.server.SimpleLicenseVerifier.initializeNativeLibrary(activity);
            Logx.d("MAIN_FACADE_NATIVE_INIT_SUCCESS");
        } catch (Exception e) {
            Logx.e("MAIN_FACADE_NATIVE_INIT_FAILED", e);
        }
    }

    /**
     * Start license countdown timer
     */
    public void startLicenseTimer() {
        licenseTimer.startTimer();
    }

    /**
     * Stop license countdown timer
     */
    public void stopLicenseTimer() {
        licenseTimer.stopTimer();
    }

    /**
     * Handle service start request
     */
    public void handleServiceStart() {
        if (!hasValidAuthentication()) {
            handleAuthenticationRequired();
            return;
        }

        callback.onServiceStartRequested();
    }

    /**
     * Handle service stop request
     */
    public void handleServiceStop() {
        callback.onServiceStopRequested();
    }

    /**
     * Handle exit request
     */
    public void handleExitRequest() {
        callback.onExitRequested();
    }

    /**
     * Handle permission result
     */
    public void handlePermissionResult(int requestCode, int resultCode, Intent data) {
        loginFacade.handlePermissionResult(requestCode, resultCode, data);
    }

    /**
     * Update service status display
     */
    public void updateServiceStatus(boolean isRunning) {
        serviceController.updateServiceStatus(isRunning);
    }

    /**
     * Get navigation manager for direct access
     */
    public NavigationManager getNavigationManager() {
        return navigationManager;
    }

    /**
     * Get region selector for direct access
     */
    public RegionSelector getRegionSelector() {
        return regionSelector;
    }

    /**
     * Get license timer for direct access
     */
    public LicenseTimer getLicenseTimer() {
        return licenseTimer;
    }

    /**
     * Get service controller for direct access
     */
    public ServiceController getServiceController() {
        return serviceController;
    }

    /**
     * Get login facade for direct access
     */
    public LoginFacade getLoginFacade() {
        return loginFacade;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        loginFacade.cleanup();
        licenseTimer.stopTimer();
        serviceController.cleanup();
    }
}