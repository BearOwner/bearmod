package com.bearmod.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer; // Legacy CountDownTimer for cleanup compatibility
import android.os.Handler; // Legacy Handler for cleanup compatibility
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.bearmod.R;
import com.bearmod.TargetAppManager;
import com.bearmod.AntiDetectionManager;
import com.bearmod.InstallerPackageManager;
import com.bearmod.PermissionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * MainActivity.java - Main application functionality and service management
 *
 * Responsibilities:
 * - Target PUBG Mobile package selection and management via TargetAppManager
 * - System permission management (overlay, storage, unknown sources)
 * - Mod service lifecycle management (START/STOP floating services)
 * - License countdown timer display and expiration tracking
 * - Region selection and game detection integration
 * - Auto-patch system integration and trigger management
 *
 * Key Methods:
 * - startModService() - Launch floating mod services with permission checks
 * - stopModService() - Stop and cleanup mod services
 * - startLicenseCountdownTimer() - Display license expiration countdown
 * - setupAutoDetection() - Auto-detect installed PUBG Mobile variants
 * - handleTargetBundleSelection(int) - Process target app selection events
 *
 * Dependencies:
 * - TargetAppManager - PUBG Mobile variant detection and management
 * - SimpleLicenseVerifier - License expiration data retrieval
 * - PermissionManager - System permission handling
 * - AutoPatchManager - Automatic patch application system
 * - Floating.class - Mod service implementation
 *
 * Prerequisites:
 * - User must be authenticated (handled by SplashActivity → LoginActivity)
 * - Native library should be loaded (handled by SplashActivity)
 *
 * Navigation Flow:
 * SplashActivity → LoginActivity → MainActivity (this activity)
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSIONS = 1001;

    private TextView serviceStatus;
    private TextView targetStatusText;
    private Button startButton, stopButton, exitButton;
    private Spinner targetBundleSpinner;

    // License countdown timer UI components
    private TextView countdownDays, countdownHours, countdownMinutes, countdownSeconds;

    // Countdown timer - using Android's CountDownTimer for better performance
    private CountDownTimer androidCountdownTimer;
    private static final long ONE_SECOND_IN_MILLIS = 1000;
    private static final long ONE_MINUTE_IN_MILLIS = 60 * ONE_SECOND_IN_MILLIS;
    private static final long ONE_HOUR_IN_MILLIS = 60 * ONE_MINUTE_IN_MILLIS;
    private static final long ONE_DAY_IN_MILLIS = 24 * ONE_HOUR_IN_MILLIS;

    // Region selection buttons
    private Button regionGlobal, regionKorea, regionVietnam, regionTaiwan, regionIndia;

    // Status display components
    private TextView serverStatus;
    private ImageView detectionStatusIcon;
    private TextView safeStatus, unsafeStatus;

    // Navigation buttons
    private Button navHome, navSettings;

    // Main app layout component
    private ScrollView mainAppLayout;

    // Managers
    private TargetAppManager targetAppManager;
    private InstallerPackageManager installerPackageManager;
    private PermissionManager permissionManager;
    private String selectedTargetPackage;

    // Auto-patch management
    private com.bearmod.patch.AutoPatchManager autoPatchManager;

    // State Management
    private boolean isServiceRunning = false;

    // Legacy countdown timer variables (kept for cleanup compatibility)
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    // Modern Activity Result API for overlay permission
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Check if overlay permission was granted
                        if (Settings.canDrawOverlays(this)) {
                            Log.d(TAG, "Overlay permission granted");
                            android.widget.Toast.makeText(this, "Overlay permission granted", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Overlay permission denied");
                            android.widget.Toast.makeText(this, "Overlay permission is required for ESP functionality", android.widget.Toast.LENGTH_LONG).show();
                            showPermissionDeniedDialog();
                        }
                    });

    // Modern Activity Result API for unknown sources permission
    private final ActivityResultLauncher<Intent> unknownSourcesPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Handle unknown sources permission result
                        if (permissionManager != null && permissionManager.checkUnknownSourcesPermission().isGranted()) {
                            Log.d(TAG, "Unknown sources permission granted");
                            android.widget.Toast.makeText(this, "Unknown sources permission granted", android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "Unknown sources permission denied");
                            android.widget.Toast.makeText(this, "Permission denied - package installation may not work", android.widget.Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Configure window for fullscreen experience
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            // Strict auth gate: block access to MainActivity without valid license
            if (!LoginActivity.hasValidKey(this)) {
                android.widget.Toast.makeText(this, "License key required. Please login.", android.widget.Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, com.bearmod.activity.LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                return;
            }

            setContentView(R.layout.activity_main);
            initializeViews();
            initializeManagers();
            setupButtonListeners();
            setupTargetBundleSpinner();

            // Initialize native library (should already be loaded by SplashActivity)
            LoginActivity.safeInit(this);

            // Setup auto-detection and UI
            setupAutoDetection();
            updateServiceStatus();

            // Log permission status for debugging
            logPermissionStatus();

            // Ensure main UI is visible (no splash transition needed in MainActivity)
            ensureMainUIVisible();

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            showErrorAndExit("Failed to initialize MainActivity: " + e.getMessage());
        }
    }

    private void initializeViews() {
        // Main app components
        // UI Components
        TextView welcomeText = findViewById(R.id.welcome_text);
        serviceStatus = findViewById(R.id.service_status);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        exitButton = findViewById(R.id.exit_button);

        // Target bundle selection
        targetBundleSpinner = findViewById(R.id.target_bundle_spinner);
        targetStatusText = findViewById(R.id.target_status_text);

        // License countdown timer components
        countdownDays = findViewById(R.id.countdown_days);
        countdownHours = findViewById(R.id.countdown_hours);
        countdownMinutes = findViewById(R.id.countdown_minutes);
        countdownSeconds = findViewById(R.id.countdown_seconds);
        ImageView mainLogo = findViewById(R.id.main_logo);

        // Region selection buttons
        regionGlobal = findViewById(R.id.region_global);
        regionKorea = findViewById(R.id.region_korea);
        regionVietnam = findViewById(R.id.region_vietnam);
        regionTaiwan = findViewById(R.id.region_taiwan);
        regionIndia = findViewById(R.id.region_india);

        // Status displays
        serverStatus = findViewById(R.id.server_status);
        detectionStatusIcon = findViewById(R.id.detection_status_icon);
        safeStatus = findViewById(R.id.safe_status);
        unsafeStatus = findViewById(R.id.unsafe_status);

        // Navigation buttons
        navHome = findViewById(R.id.nav_home);
        navSettings = findViewById(R.id.nav_settings);

        // Main app layout component
        mainAppLayout = findViewById(R.id.main_app_layout);
    }

    private void initializeManagers() {
        // Initialize managers
        targetAppManager = new TargetAppManager(this);
        AntiDetectionManager antiDetectionManager = new AntiDetectionManager(this);
        installerPackageManager = new InstallerPackageManager(this);
        autoPatchManager = com.bearmod.patch.AutoPatchManager.getInstance(this);
        permissionManager = PermissionManager.Companion.getInstance(this); // Initialize PermissionManager singleton

        // Initialize secure script manager and Scripts directory
        com.bearmod.patch.SecureScriptManager.getInstance(this).initializeScriptsDirectory();

        // Initialize plugin loader
        com.bearmod.plugin.PluginLoader.getInstance(this).loadPlugins();
    }

    private void setupButtonListeners() {
        // START button - Launch game and start floating services
        startButton.setOnClickListener(v -> startModService());

        // STOP button - Stop floating services and cleanup
        stopButton.setOnClickListener(v -> stopModService());

        // EXIT button
        exitButton.setOnClickListener(v -> finish());

        // Region selection buttons
        setupRegionButtons();

        // Navigation buttons
        setupNavigationButtons();
    }

    private void setupRegionButtons() {
        if (regionGlobal != null) {
            regionGlobal.setOnClickListener(v -> selectRegion("com.tencent.ig", regionGlobal, "GLOBAL"));
        }
        if (regionKorea != null) {
            regionKorea.setOnClickListener(v -> selectRegion("com.pubg.krmobile", regionKorea, "KOREA"));
        }
        if (regionVietnam != null) {
            regionVietnam.setOnClickListener(v -> selectRegion("com.vng.pubgmobile", regionVietnam, "VIETNAM"));
        }
        if (regionTaiwan != null) {
            regionTaiwan.setOnClickListener(v -> selectRegion("com.rekoo.pubgm", regionTaiwan, "TAIWAN"));
        }
        if (regionIndia != null) {
            regionIndia.setOnClickListener(v -> selectRegion("com.pubg.imobile", regionIndia, "INDIA"));
        }
    }

    private void selectRegion(String packageName, Button selectedButton, String regionName) {
        // Clear all region button selections
        clearRegionSelections();

        // Highlight selected button
        selectedButton.setSelected(true);

        // Update target package
        selectedTargetPackage = packageName;
        updateServerStatus(regionName, packageName);

        Log.d(TAG, "Region selected: " + regionName + " (" + packageName + ")");
    }

    private void clearRegionSelections() {
        if (regionGlobal != null) regionGlobal.setSelected(false);
        if (regionKorea != null) regionKorea.setSelected(false);
        if (regionVietnam != null) regionVietnam.setSelected(false);
        if (regionTaiwan != null) regionTaiwan.setSelected(false);
        if (regionIndia != null) regionIndia.setSelected(false);
    }

    private void setupNavigationButtons() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> selectNavigation(navHome, "Home"));
            navHome.setSelected(true); // Default selection
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> selectNavigation(navSettings, "Settings"));
        }
    }

    private void selectNavigation(Button selectedButton, String navName) {
        // Clear all navigation selections
        if (navHome != null) navHome.setSelected(false);
        if (navSettings != null) navSettings.setSelected(false);

        // Select current button
        selectedButton.setSelected(true);

        Log.d(TAG, "Navigation selected: " + navName);
    }

    private void setupTargetBundleSpinner() {
        // Use TargetAppManager to get target options
        List<String> targetOptions = targetAppManager.getTargetOptions();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, targetOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        targetBundleSpinner.setAdapter(adapter);

        // Set selection listener
        targetBundleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleTargetBundleSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set default selection to auto-detect
        targetBundleSpinner.setSelection(0);
    }

    @SuppressLint("SetTextI18n")
    private void handleTargetBundleSelection(int position) {
        // Use TargetAppManager to handle selection logic
        TargetAppManager.TargetSelectionResult result = targetAppManager.selectTargetByPosition(position);

        // Update UI based on result
        if (result.isValid()) {
            selectedTargetPackage = result.getPackageName();
            updateTargetStatus(result.getPackageName(), result.getStatus());
            Log.d(TAG, "Target package selected via TargetAppManager: " + selectedTargetPackage);
        } else {
            selectedTargetPackage = null;
            if (targetStatusText != null) {
                targetStatusText.setText("No PUBG Mobile version found installed");
            }
            Log.w(TAG, "No valid target package selected");
        }

        // Trigger auto-patch if configured for target selection
        if (selectedTargetPackage != null &&
            autoPatchManager.shouldTriggerAutoPatch(com.bearmod.patch.AutoPatchConfig.AutoPatchTrigger.ON_TARGET_SELECTION)) {
            triggerAutoPatch();
        }
    }

    /**
     * Update target status with installation and OBB information
     */
    @SuppressLint("SetTextI18n")
    private void updateTargetStatus(String packageName, String prefix) {
        if (packageName == null) {
            targetStatusText.setText("No package selected");
            return;
        }

        String displayName = installerPackageManager.getPackageDisplayName(packageName);
        boolean isInstalled = installerPackageManager.isPackageInstalled(packageName);
        boolean obbRequired = installerPackageManager.requiresObb(packageName);
        boolean obbInstalled = !obbRequired || installerPackageManager.isObbInstalled(packageName);

        StringBuilder status = new StringBuilder();
        status.append(prefix).append(": ").append(displayName);

        if (isInstalled) {
            String version = installerPackageManager.getInstalledPackageVersion(packageName);
            status.append(" (v").append(version != null ? version : "unknown").append(")");

            if (obbRequired) {
                if (obbInstalled) {
                    status.append(" ✓ OBB OK");
                } else {
                    status.append(" ⚠ OBB Missing");
                }
            }
        } else {
            status.append(" (Not Installed)");
        }

        targetStatusText.setText(status.toString());
    }

    @SuppressLint("SetTextI18n")
    private void updateServerStatus(String regionName, String packageName) {
        if (serverStatus != null) {
            // Check if the package is actually installed
            boolean isInstalled = targetAppManager.isPackageInstalled(packageName);

            if (isInstalled) {
                serverStatus.setText("SERVER " + regionName);
                if (detectionStatusIcon != null) {
                    // Set icon to green checkmark
                    detectionStatusIcon.setImageResource(R.drawable.ic_check_circle);
                    detectionStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.premium_accent_green));
                }
                updateSafetyStatus(true);
            } else {
                serverStatus.setText("SERVER " + regionName + " (NOT INSTALLED)");
                if (detectionStatusIcon != null) {
                    // Set icon to red error
                    detectionStatusIcon.setImageResource(R.drawable.ic_error);
                    detectionStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.premium_accent_red));
                }
                updateSafetyStatus(false);
            }
        }
    }

    private void updateSafetyStatus(boolean isSafe) {
        if (safeStatus != null && unsafeStatus != null) {
            if (isSafe) {
                safeStatus.setVisibility(View.VISIBLE);
                unsafeStatus.setVisibility(View.GONE);
            } else {
                safeStatus.setVisibility(View.GONE);
                unsafeStatus.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Ensure main UI is visible and properly initialized
     * MainActivity should only handle main app UI, not splash transitions
     */
    private void ensureMainUIVisible() {
        try {
            Log.d(TAG, "Ensuring main UI is visible");

            if (mainAppLayout != null) {
                // Ensure main app layout is visible
                mainAppLayout.setVisibility(View.VISIBLE);
                mainAppLayout.setAlpha(1.0f);

                Log.d(TAG, "Main UI visibility confirmed");
            } else {
                Log.e(TAG, "Main UI layout not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring main UI visibility", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity resumed");

        // Verify authentication state when app resumes
        if (!LoginActivity.hasValidKey(this)) {
            Log.w(TAG, "Authentication invalid on resume - redirecting to login");
            finish(); // Close MainActivity and return to SplashActivity flow
            return;
        }

        // Re-enable countdown timer after authentication verification
        startLicenseCountdownTimer();

        // Update service status
        updateServiceStatus();
    }

    /**
     * Start license countdown timer using Android's CountDownTimer for better performance.
     * This approach calculates the duration once and uses an optimized timer that doesn't
     * block the main UI thread, fixing the "Choreographer: Skipped frames" issue.
     */
    private void startLicenseCountdownTimer() {
        try {
            // Stop any existing countdown timer
            stopCountdownTimer();

            // Get expiration date once (not every second like before)
            String expirationString = EXP();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            Date expiryDate = dateFormat.parse(expirationString);

            if (expiryDate == null) {
                Log.e(TAG, "Failed to parse expiration date: " + expirationString);
                return;
            }

            long now = System.currentTimeMillis();
            long durationMillis = expiryDate.getTime() - now;

            if (durationMillis <= 0) {
                // License already expired
                Log.i(TAG, "License has already expired");
                updateCountdownDisplay(0, 0, 0, 0);
                return;
            }

            // Start the optimized CountDownTimer
            startNewCountdownTimer(durationMillis);
            Log.d(TAG, "License countdown timer started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error starting license countdown timer", e);
            // Show fallback countdown (30 days) if there's an error
            startNewCountdownTimer(30L * ONE_DAY_IN_MILLIS);
        }
    }

    /**
     * Create and start a new CountDownTimer with the specified duration.
     * This uses Android's optimized CountDownTimer which runs on a background thread.
     */
    private void startNewCountdownTimer(long durationMillis) {
        stopCountdownTimer(); // Ensure no existing timer is running

        androidCountdownTimer = new CountDownTimer(durationMillis, 1000) { // Tick every 1 second
            public void onTick(long millisUntilFinished) {
                long days = millisUntilFinished / ONE_DAY_IN_MILLIS;
                long hours = (millisUntilFinished % ONE_DAY_IN_MILLIS) / ONE_HOUR_IN_MILLIS;
                long minutes = (millisUntilFinished % ONE_HOUR_IN_MILLIS) / ONE_MINUTE_IN_MILLIS;
                long seconds = (millisUntilFinished % ONE_MINUTE_IN_MILLIS) / ONE_SECOND_IN_MILLIS;

                // Update UI on main thread
                runOnUiThread(() -> updateCountdownDisplay(days, hours, minutes, seconds));
            }

            public void onFinish() {
                // Update UI on main thread
                runOnUiThread(() -> {
                    updateCountdownDisplay(0, 0, 0, 0);
                    Log.i(TAG, "License has expired (CountDownTimer finished).");
                    // You can add additional expired license handling here
                });
            }
        }.start();
    }

    /**
     * Update the countdown display UI elements
     */
    private void updateCountdownDisplay(long days, long hours, long minutes, long seconds) {
        try {
            if (countdownDays != null) {
                countdownDays.setText(String.format(java.util.Locale.getDefault(), "%02d", days));
            }
            if (countdownHours != null) {
                countdownHours.setText(String.format(java.util.Locale.getDefault(), "%02d", hours));
            }
            if (countdownMinutes != null) {
                countdownMinutes.setText(String.format(java.util.Locale.getDefault(), "%02d", minutes));
            }
            if (countdownSeconds != null) {
                countdownSeconds.setText(String.format(java.util.Locale.getDefault(), "%02d", seconds));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating countdown display", e);
        }
    }

    /**
     * Stop the countdown timer and clean up resources
     */
    private void stopCountdownTimer() {
        // Stop and clean up Android CountDownTimer
        if (androidCountdownTimer != null) {
            androidCountdownTimer.cancel();
            androidCountdownTimer = null;
        }

        // Legacy cleanup (in case old handler/runnable still exists)
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
            countdownHandler = null;
            countdownRunnable = null;
        }
    }

    /**
     * Get license expiration date from SimpleLicenseVerifier
     * Integrated with KeyAuth API authentication system
     */
    private String EXP() {
        // Get real expiration date from SimpleLicenseVerifier
        String expiration = com.bearmod.auth.SimpleLicenseVerifier.getUserExpiration();

        if (expiration != null && !expiration.isEmpty()) {
            Log.d(TAG, "Using real license expiration: " + expiration);
            return expiration;
        } else {
            // Fallback to default expiration if no real data available
            Log.d(TAG, "No real expiration data available, using fallback");
            long futureTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 days
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(new Date(futureTime));
        }
    }

    /**
     * Log current permission status for debugging (using PermissionManager)
     */
    private void logPermissionStatus() {
        try {
            Log.d(TAG, "Permission Status on Startup:");
            if (permissionManager != null) {
                Log.d(TAG, "Unknown Sources: " + permissionManager.checkUnknownSourcesPermission().isGranted());
                Log.d(TAG, "Storage: " + permissionManager.checkStoragePermission().isGranted());
                Log.d(TAG, "Overlay: " + permissionManager.checkOverlayPermission().isGranted());
            } else {
                Log.w(TAG, "PermissionManager not initialized - using fallback checks");
                Log.d(TAG, "Storage: " + isStoragePermissionGranted());
                Log.d(TAG, "Overlay: " + Settings.canDrawOverlays(this));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission status", e);
        }
    }

    /**
     * Request Unknown Sources permission (using PermissionManager)
     */
    private void requestUnknownSourcesPermission() {
        if (permissionManager != null) {
            Log.d(TAG, "Requesting unknown sources permission via PermissionManager");
            permissionManager.requestUnknownSourcesPermission(this, PermissionManager.REQUEST_UNKNOWN_SOURCES);
        } else {
            Log.w(TAG, "PermissionManager not initialized - using fallback");
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    android.net.Uri.parse("package:" + getPackageName()));
            unknownSourcesPermissionLauncher.launch(intent);
        }
    }

    /**
     * Check if storage permissions are granted (using PermissionManager)
     */
    private boolean isStoragePermissionGranted() {
        try {
            if (permissionManager != null) {
                PermissionManager.PermissionStatus status = permissionManager.checkStoragePermission();
                return status.isGranted();
            } else {
                Log.w(TAG, "PermissionManager not initialized - using fallback storage permission check");
                // Fallback: Check basic storage permission
                return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking storage permission", e);
            return false;
        }
    }

    /**
     * Request storage permission (using PermissionManager)
     */
    private void requestStoragePermission() {
        try {
            if (permissionManager != null) {
                Log.d(TAG, "Requesting storage permission via PermissionManager");
                permissionManager.requestStoragePermission(this, PermissionManager.REQUEST_STORAGE_PERMISSION);
            } else {
                Log.w(TAG, "PermissionManager not initialized - using fallback storage permission request");
                // Fallback: Request basic storage permission
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting storage permission", e);
            android.widget.Toast.makeText(this, "Error requesting storage permission: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Request overlay permission (using PermissionManager)
     */
    private void requestOverlayPermission() {
        if (permissionManager != null) {
            Log.d(TAG, "Requesting overlay permission via PermissionManager");
            permissionManager.requestOverlayPermission(this, PermissionManager.REQUEST_OVERLAY_PERMISSION);
        } else {
            Log.w(TAG, "PermissionManager not initialized - using fallback");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
        }
    }

    /**
     * Show dialog when overlay permission is denied
     */
    @SuppressLint("SetTextI18n")
    private void showPermissionDeniedDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("""
                    BearMod requires overlay permission to display ESP and mod features over games.
                    
                    Please grant the permission to continue.""")
            .setPositiveButton("Retry", (dialog, which) -> {
                requestOverlayPermission();
            })
            .setNegativeButton("Exit", (dialog, which) -> {
                Log.d(TAG, "User denied overlay permission - exiting app");
                finish();
            })
            .setCancelable(false)
            .show();
    }

    @SuppressLint("SetTextI18n")
    private void setupAutoDetection() {
        // Use TargetAppManager for auto-detection
        TargetAppManager.TargetSelectionResult result = targetAppManager.selectTarget(TargetAppManager.SelectionMode.AUTO_DETECT);

        if (result.isValid() && result.isInstalled()) {
            selectedTargetPackage = result.getPackageName();
            String regionName = targetAppManager.getPackageDisplayName(result.getPackageName());

            // Update server status
            updateServerStatus(regionName, result.getPackageName());

            // Select corresponding region button
            selectRegionButtonForPackage(result.getPackageName());

            Log.d(TAG, "Auto-detected target package via TargetAppManager: " + result.getPackageName() + " (" + regionName + ")");
        } else {
            // No package detected
            selectedTargetPackage = null;
            if (serverStatus != null) {
                serverStatus.setText("NO GAME DETECTED");
            }
            if (detectionStatusIcon != null) {
                detectionStatusIcon.setImageResource(R.drawable.ic_error);
                detectionStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.premium_accent_red));
            }
            updateSafetyStatus(false);
            Log.d(TAG, "No target package detected via TargetAppManager");
        }
    }

    private void selectRegionButtonForPackage(String packageName) {
        clearRegionSelections();

        switch (packageName) {
            case "com.tencent.ig":
                if (regionGlobal != null) regionGlobal.setSelected(true);
                break;
            case "com.pubg.krmobile":
                if (regionKorea != null) regionKorea.setSelected(true);
                break;
            case "com.pubg.imobile":
                if (regionIndia != null) regionIndia.setSelected(true);
                break;
            case "com.rekoo.pubgm":
                if (regionTaiwan != null) regionTaiwan.setSelected(true);
                break;
            case "com.vng.pubgmobile":
                if (regionVietnam != null) regionVietnam.setSelected(true);
                break;
        }
    }

    /**
     * Start the mod service with stealth measures (correct START button functionality)
     */
    private void startModService() {
        if (isServiceRunning) {
            Log.d(TAG, "Service already running");
            return;
        }

        try {
            Log.d(TAG, "Starting mod service...");

            // Require valid authentication before starting any service
            if (!LoginActivity.hasValidKey(this)) {
                android.widget.Toast.makeText(this, "License key required. Please login.", android.widget.Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, com.bearmod.activity.LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            }

            // Check if a target package is selected
            if (selectedTargetPackage == null) {
                android.widget.Toast.makeText(this, "Please select a target game version first", android.widget.Toast.LENGTH_LONG).show();
                Log.w(TAG, "No target package selected");
                return;
            }

            // Check if the selected target app is installed using TargetAppManager
            if (!targetAppManager.isPackageInstalled(selectedTargetPackage)) {
                showPackageInstallationGuidance(selectedTargetPackage);
                Log.w(TAG, "Selected target app not installed: " + selectedTargetPackage);
                return;
            }

            // Check Unknown Sources permission
            if (permissionManager != null && !permissionManager.checkUnknownSourcesPermission().isGranted()) {
                Log.d(TAG, "Unknown Sources permission not enabled - requesting");
                android.widget.Toast.makeText(this, "Requesting Unknown Sources permission for optimal functionality...", android.widget.Toast.LENGTH_SHORT).show();
                requestUnknownSourcesPermission();
                return;
            }

            // Check storage permission for OBB access
            if (!isStoragePermissionGranted()) {
                Log.d(TAG, "Storage permission not granted - requesting");
                android.widget.Toast.makeText(this, "Requesting storage permission for OBB file access...", android.widget.Toast.LENGTH_SHORT).show();
                requestStoragePermission();
                return;
            }

            // Check overlay permission
            if (permissionManager != null && !permissionManager.checkOverlayPermission().isGranted()) {
                Log.d(TAG, "Overlay permission not granted - requesting");
                android.widget.Toast.makeText(this, "Requesting overlay permission for ESP functionality...", android.widget.Toast.LENGTH_SHORT).show();
                requestOverlayPermission();
                return;
            }

            proceedWithServiceStart();

        } catch (Exception e) {
            Log.e(TAG, "Error starting mod service", e);
            android.widget.Toast.makeText(this, "Error starting service: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Proceed with starting the service after all checks pass
     */
    private void proceedWithServiceStart() {
        try {
            // Apply auto-patches if configured for service start
            if (autoPatchManager.shouldTriggerAutoPatch(com.bearmod.patch.AutoPatchConfig.AutoPatchTrigger.ON_SERVICE_START)) {
                Log.d(TAG, "Applying auto-patches before service start");
                triggerAutoPatch();
            }

            // Start the floating service
            Intent serviceIntent = new Intent(this, com.bearmod.Floating.class);
            serviceIntent.putExtra("TARGET_PACKAGE", selectedTargetPackage);
            startService(serviceIntent);

            isServiceRunning = true;
            updateServiceStatus();

            android.widget.Toast.makeText(this, "Mod service started successfully", android.widget.Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Mod service started for package: " + selectedTargetPackage);

        } catch (Exception e) {
            Log.e(TAG, "Error starting floating service", e);
            android.widget.Toast.makeText(this, "Error starting floating service: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Stop the mod service and cleanup (correct STOP button functionality)
     */
    private void stopModService() {
        if (!isServiceRunning) {
            Log.d(TAG, "Service not running");
            return;
        }

        try {
            Log.d(TAG, "Stopping mod service...");

            // Stop the floating service
            Intent serviceIntent = new Intent(this, com.bearmod.Floating.class);
            stopService(serviceIntent);

            isServiceRunning = false;
            updateServiceStatus();

            android.widget.Toast.makeText(this, "Mod service stopped", android.widget.Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Mod service stopped");

        } catch (Exception e) {
            Log.e(TAG, "Error stopping mod service", e);
            android.widget.Toast.makeText(this, "Error stopping service: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateServiceStatus() {
        if (serviceStatus != null) {
            if (isServiceRunning) {
                serviceStatus.setText("Service Status: Running");
                serviceStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            } else {
                serviceStatus.setText("Service Status: Stopped");
                serviceStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        }
    }

    private void triggerAutoPatch() {
        // Trigger auto-patch functionality
        Log.d(TAG, "Triggering auto-patch");
        // Implementation depends on AutoPatchManager
    }

    /**
     * Show package installation guidance
     */
    private void showPackageInstallationGuidance(String packageName) {
        String displayName = installerPackageManager.getPackageDisplayName(packageName);
        boolean unknownSourcesEnabled = (permissionManager != null && permissionManager.checkUnknownSourcesPermission().isGranted());

        StringBuilder message = getStringBuilder(displayName, unknownSourcesEnabled);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
            .setTitle("Game Not Installed")
            .setMessage(message.toString())
            .setNegativeButton("Cancel", null);

        if (!unknownSourcesEnabled) {
            builder.setPositiveButton("Enable Permission", (dialog, which) -> {
                requestUnknownSourcesPermission();
            });
        } else {
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }

    @NonNull
    private static StringBuilder getStringBuilder(String displayName, boolean unknownSourcesEnabled) {
        StringBuilder message = new StringBuilder();
        message.append("The selected game (").append(displayName).append(") is not installed.\n\n");

        if (!unknownSourcesEnabled) {
            message.append("⚠ Unknown Sources permission is required to install APK files.\n\n");
        }

        message.append("To install ").append(displayName).append(":\n\n");
        message.append("1. Download the official APK from a trusted source\n");
        message.append("2. Enable Unknown Sources permission if needed\n");
        message.append("3. Install the APK file\n");
        message.append("4. Launch the game to download OBB data\n");
        message.append("5. Return to BearMod and try again\n\n");
        message.append("Would you like to enable Unknown Sources permission now?");
        return message;
    }

    private void showErrorAndExit(String message) {
        try {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Startup Error")
                .setMessage(message)
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
        } catch (Exception dialogError) {
            // If we can't even show a dialog, just finish
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                android.widget.Toast.makeText(this, "Storage permission denied", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCountdownTimer();
        Log.d(TAG, "MainActivity destroyed");
    }
}
