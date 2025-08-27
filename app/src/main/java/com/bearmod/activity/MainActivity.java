package com.bearmod.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer; // Legacy CountDownTimer for cleanup compatibility
import android.os.Handler; // Legacy Handler for cleanup compatibility
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
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
import com.bearmod.Floating;
import com.bearmod.TargetAppManager;
import com.bearmod.InstallerPackageManager;
import com.bearmod.PermissionManager;
import com.bearmod.security.DataClearingManager;
import com.bearmod.patch.StartupOrchestrator;

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
    // SharedPreferences keys for persisting last selected target
    private static final String PREFS_NAME = "bearmod_prefs";
    private static final String KEY_LAST_TARGET = "last_target_pkg";

    private TextView serviceStatus;
    private TextView targetStatusText;
    // Game Status card views
    private TextView textGameName, textGameVersion, textObbStatus;
    private Button btnFixObb;
    private Button startButton, stopButton, exitButton;
    private Spinner targetBundleSpinner;
    // Patch progress UI removed (automatic, no user-facing UI)

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

    // Navigation (icon) buttons
    private ImageButton navHome, navSettings;

    // Main sections
    private ScrollView mainAppLayout;
    private ScrollView settingsLayout;

    // Settings controls
    private androidx.appcompat.widget.SwitchCompat switchAutoClear;
    private androidx.appcompat.widget.SwitchCompat switchFastDownload;
    private Button btnClearDataNow;
    private Button btnExitApp;

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

    // Pending OBB fix context
    private String pendingObbPackage;
    private java.io.File pendingObbSource;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Handle OBB tree selection result
            boolean handled = com.bearmod.PermissionManager.Companion.getInstance(this)
                    .handleObbTreeResult(this, requestCode, data,
                            pendingObbPackage != null ? pendingObbPackage : (selectedTargetPackage != null ? selectedTargetPackage : ""),
                            () -> {
                                try {
                                    if (pendingObbPackage != null && pendingObbSource != null) {
                                        boolean ok = com.bearmod.install.ObbInstaller.copyFromSource(this, pendingObbPackage, pendingObbSource);
                                        android.widget.Toast.makeText(this, ok ? "OBB placed successfully" : "OBB copy failed", android.widget.Toast.LENGTH_LONG).show();
                                        updateTargetStatus(pendingObbPackage, installerPackageManager.getPackageDisplayName(pendingObbPackage));
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "copy after SAF persist failed", e);
                                } finally {
                                    pendingObbPackage = null;
                                    pendingObbSource = null;
                                }
                                return kotlin.Unit.INSTANCE;
                            }
                    );
            if (handled) return;
        } catch (Exception e) {
            Log.w(TAG, "onActivityResult OBB tree handling failed", e);
        }
    }

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
        // Patch progress UI removed
        exitButton = findViewById(R.id.exit_button);

        // Target bundle selection
        targetBundleSpinner = findViewById(R.id.target_bundle_spinner);
        targetStatusText = findViewById(R.id.target_status_text);
        // Game Status card
        textGameName = findViewById(R.id.text_game_name);
        textGameVersion = findViewById(R.id.text_game_version);
        textObbStatus = findViewById(R.id.text_obb_status);
        btnFixObb = findViewById(R.id.btn_fix_obb);

        // Region buttons are assigned below and listeners are set in setupRegionButtons()

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

        // Navigation buttons (icons)
        navHome = findViewById(R.id.nav_home);
        navSettings = findViewById(R.id.nav_settings);

        // Main sections
        mainAppLayout = findViewById(R.id.main_app_layout);
        settingsLayout = findViewById(R.id.settings_layout);

        // Settings controls
        switchAutoClear = findViewById(R.id.switch_auto_clear);
        switchFastDownload = findViewById(R.id.switch_fast_download);
        btnClearDataNow = findViewById(R.id.btn_clear_data_now);
        btnExitApp = findViewById(R.id.btn_exit_app);
    }

    private void initializeManagers() {
        // Initialize managers
        targetAppManager = new TargetAppManager(this);
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

        // EXIT button (legacy hidden)
        exitButton.setOnClickListener(v -> handleExitRequested());

        // Region selection buttons
        setupRegionButtons();

        // Navigation buttons
        setupNavigationButtons();

        // Settings controls
        setupSettingsControls();

        // Fix OBB CTA
        if (btnFixObb != null) {
            btnFixObb.setOnClickListener(v -> {
                String pkg = selectedTargetPackage;
                if (pkg != null) {
                    attemptFixObbFlow(pkg);
                } else {
                    android.widget.Toast.makeText(this, "Select a game first", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        // Also refresh detailed game status card
        try {
            String displayName = installerPackageManager != null ? installerPackageManager.getPackageDisplayName(packageName) : packageName;
            updateTargetStatus(packageName, displayName);
        } catch (Exception ignored) {}

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
            navHome.setOnClickListener(v -> selectNavigation("Home"));
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> selectNavigation("Settings"));
        }
        // Default to Home
        selectNavigation("Home");
    }

    private void selectNavigation(String navName) {
        boolean isHome = "Home".equals(navName);
        if (mainAppLayout != null) mainAppLayout.setVisibility(isHome ? View.VISIBLE : View.GONE);
        if (settingsLayout != null) settingsLayout.setVisibility(isHome ? View.GONE : View.VISIBLE);

        // Tint icons
        try {
            if (navHome != null)
                navHome.setColorFilter(ContextCompat.getColor(this, isHome ? R.color.premium_text_primary : R.color.premium_text_secondary));
            if (navSettings != null)
                navSettings.setColorFilter(ContextCompat.getColor(this, !isHome ? R.color.premium_text_primary : R.color.premium_text_secondary));
        } catch (Exception ignored) {}

        Log.d(TAG, "Navigation selected: " + navName);
    }

    private static final String PREFS = "bearmod_prefs";
    private static final String KEY_AUTO_CLEAR = "auto_clear";
    private static final String KEY_FAST_DOWNLOAD = "fast_download";

    private void setupSettingsControls() {
        final android.content.SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean autoClear = sp.getBoolean(KEY_AUTO_CLEAR, true);
        boolean fastDl = sp.getBoolean(KEY_FAST_DOWNLOAD, false);

        if (switchAutoClear != null) {
            switchAutoClear.setChecked(autoClear);
            switchAutoClear.setOnCheckedChangeListener((b, checked) ->
                    sp.edit().putBoolean(KEY_AUTO_CLEAR, checked).apply());
        }
        if (switchFastDownload != null) {
            switchFastDownload.setChecked(fastDl);
            switchFastDownload.setOnCheckedChangeListener((b, checked) ->
                    sp.edit().putBoolean(KEY_FAST_DOWNLOAD, checked).apply());
        }

        if (btnClearDataNow != null) {
            btnClearDataNow.setOnClickListener(v -> performUserRequestedCleanup());
        }
        if (btnExitApp != null) {
            btnExitApp.setOnClickListener(v -> handleExitRequested());
        }
    }

    private boolean isAutoClearEnabled() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_AUTO_CLEAR, true);
    }

    private void performUserRequestedCleanup() {
        showLoadingSpinner("Cleaning up...");
        DataClearingManager.getInstance(this).performComprehensiveCleanup(new DataClearingManager.CleanupListener() {
            @Override public void onCleanupStarted() { updateLoadingMessage("Cleaning up..."); }
            @Override public void onCleanupProgress(int p, String task) { updateLoadingMessage(task + " (" + p + "%)"); }
            @Override public void onCleanupCompleted(boolean success) {
                hideLoadingSpinner();
                android.widget.Toast.makeText(MainActivity.this, success ? "Data cleared" : "Cleanup completed with warnings", android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override public void onCleanupFailed(String error) {
                hideLoadingSpinner();
                android.widget.Toast.makeText(MainActivity.this, "Cleanup failed: " + error, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleExitRequested() {
        if (isAutoClearEnabled()) {
            secureExit(true);
        } else {
            try { stopService(new Intent(this, Floating.class)); } catch (Exception ignored) {}
            finishAffinity();
        }
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

        // No manual patch UI; patching handled automatically during service start
    }

    /**
     * Update target status with installation and OBB information
     */
    @SuppressLint("SetTextI18n")
    private void updateTargetStatus(String packageName, String prefix) {
        if (packageName == null) {
            targetStatusText.setText("No package selected");
            if (textGameName != null) textGameName.setText("Game: Not selected");
            if (textGameVersion != null) textGameVersion.setText("Version: —");
            if (textObbStatus != null) textObbStatus.setText("OBB: —");
            if (btnFixObb != null) btnFixObb.setVisibility(View.GONE);
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
                    // Offer user-triggered fix via SAF (verify-only by default, copy only when user asks)
                    showObbFixSuggestion(packageName);
                }
            }
        } else {
            status.append(" (Not Installed)");
        }

        targetStatusText.setText(status.toString());

        // Update modern Game Status card
        try {
            if (textGameName != null) textGameName.setText("Game: " + displayName);
            if (textGameVersion != null) {
                String version = isInstalled ? installerPackageManager.getInstalledPackageVersion(packageName) : null;
                textGameVersion.setText("Version: " + (version != null ? version : (isInstalled ? "unknown" : "not installed")));
            }
            if (textObbStatus != null) {
                if (!obbRequired) {
                    textObbStatus.setText("OBB: Not required");
                    textObbStatus.setTextColor(ContextCompat.getColor(this, R.color.premium_text_secondary));
                } else if (obbInstalled) {
                    textObbStatus.setText("OBB: Present");
                    textObbStatus.setTextColor(ContextCompat.getColor(this, R.color.premium_accent_green));
                } else {
                    textObbStatus.setText("OBB: Missing");
                    textObbStatus.setTextColor(ContextCompat.getColor(this, R.color.premium_accent_red));
                }
            }
            if (btnFixObb != null) {
                btnFixObb.setVisibility(isInstalled && obbRequired && !obbInstalled ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {}
    }

    private void showObbFixSuggestion(String packageName) {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("OBB Missing")
                    .setMessage("The required game data (OBB) is missing. If you've already downloaded it (e.g., via Telegram or Download), I can place it into the official folder for you.")
                    .setPositiveButton("Fix OBB", (d, w) -> attemptFixObbFlow(packageName))
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.w(TAG, "Failed to show OBB fix dialog", e);
        }
    }

    private void attemptFixObbFlow(String packageName) {
        try {
            int vcode = getInstalledVersionCode(packageName);
            if (vcode <= 0) {
                android.widget.Toast.makeText(this, "Cannot resolve game version for OBB name.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            // Find candidate OBB in common download locations
            java.util.List<java.io.File> candidates = com.bearmod.install.ObbLocator.findCandidates(packageName, vcode);
            if (candidates == null || candidates.isEmpty()) {
                String expected = "main." + vcode + "." + packageName + ".obb";
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("OBB Not Found")
                        .setMessage("Please place '" + expected + "' into Download or Telegram folders, then tap Fix OBB again.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // Choose the largest candidate (more robust if multiple copies)
            java.io.File chosen = candidates.get(0);
            long max = chosen.length();
            for (java.io.File f : candidates) {
                if (f.length() > max) { chosen = f; max = f.length(); }
            }

            // If we already have SAF access to Android/obb/<pkg>, copy immediately
            boolean hasWrite = com.bearmod.storage.StorageAccessHelper.hasWriteAccess(this, packageName);
            if (!hasWrite) {
                // Request tree; remember context and continue in onActivityResult
                this.pendingObbPackage = packageName;
                this.pendingObbSource = chosen;
                com.bearmod.PermissionManager.Companion.getInstance(this)
                        .requestObbTree(this, packageName, com.bearmod.PermissionManager.REQUEST_OBB_TREE);
                return;
            }

            boolean ok = com.bearmod.install.ObbInstaller.copyFromSource(this, packageName, chosen);
            android.widget.Toast.makeText(this, ok ? "OBB placed successfully" : "OBB copy failed", android.widget.Toast.LENGTH_LONG).show();
            // Refresh status
            updateTargetStatus(packageName, installerPackageManager.getPackageDisplayName(packageName));
        } catch (Exception e) {
            Log.w(TAG, "attemptFixObbFlow error", e);
            android.widget.Toast.makeText(this, "Fix OBB failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private int getInstalledVersionCode(String packageName) {
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);
            return pi.versionCode;
        } catch (Exception e) {
            return -1;
        }
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
            // Gently redirect without abrupt close
            android.widget.Toast.makeText(this, "Session expired. Please login.", android.widget.Toast.LENGTH_SHORT).show();
            try {
                startActivity(new Intent(this, com.bearmod.activity.LoginActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Failed to start LoginActivity", e);
            }
            return;
        }

        // Re-enable countdown timer after authentication verification
        startLicenseCountdownTimer();

        // Update service status
        updateServiceStatus();
        // Load last selected target and update UI
        restoreLastSelectedTarget();
        updateRegionButtons();
    }

    private void onRegionSelected(String packageName) {
        this.selectedTargetPackage = packageName;
        if (targetStatusText != null) {
            String display = installerPackageManager != null ? installerPackageManager.getPackageDisplayName(packageName) : packageName;
            boolean installed = installerPackageManager != null && installerPackageManager.isPackageInstalled(packageName);
            targetStatusText.setText(display + (installed ? " (Installed)" : " (Not Installed)"));
        }
        saveLastSelectedTarget(packageName);
        updateRegionButtons();
        // No manual patch triggers here; handled automatically on service start

        // If not installed, guide user to install immediately
        try {
            boolean installed = installerPackageManager != null && installerPackageManager.isPackageInstalled(packageName);
            if (!installed) {
                showPackageInstallationGuidance(packageName);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error showing install guidance", e);
        }
    }

    private void updateRegionButtons() {
        if (installerPackageManager == null) return;
        setRegionButtonState(regionGlobal, "Global", "com.tencent.ig");
        setRegionButtonState(regionKorea, "Korea", "com.pubg.krmobile");
        setRegionButtonState(regionIndia, "India", "com.pubg.imobile");
        setRegionButtonState(regionTaiwan, "Taiwan", "com.rekoo.pubgm");
        setRegionButtonState(regionVietnam, "Vietnam", "com.vng.pubgmobile");
    }

    private void setRegionButtonState(Button button, String label, String pkg) {
        if (button == null) return;
        boolean installed = installerPackageManager.isPackageInstalled(pkg);
        String suffix = installed ? " — Open" : " — Install";
        button.setText(label + suffix);
        boolean isSelected = pkg.equals(selectedTargetPackage);
        button.setSelected(isSelected);
        button.setAlpha(isSelected ? 1.0f : 0.9f);

        // Long-press to open the game directly if installed
        button.setOnLongClickListener(v -> {
            if (installed) {
                try {
                    boolean launched = targetAppManager.launchTargetPackage(pkg);
                    Log.d(TAG, "Long-press launch (" + pkg + ") result: " + launched);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to launch on long-press", e);
                }
            }
            return false;
        });
        // Content description for accessibility
        button.setContentDescription(label + (installed ? ", installed, long-press to open" : ", not installed, tap to install guidance"));
    }

    private void saveLastSelectedTarget(String pkg) {
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_LAST_TARGET, pkg).apply();
        } catch (Exception ignored) { }
    }

    private void restoreLastSelectedTarget() {
        try {
            String saved = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_LAST_TARGET, null);
            if (saved != null) {
                this.selectedTargetPackage = saved;
            } else {
                // fallback to auto-detect
                String autodetected = targetAppManager != null ? targetAppManager.getInstalledTargetPackage() : null;
                this.selectedTargetPackage = autodetected;
            }
        } catch (Exception ignored) { }
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
            // 1) Launch target game immediately
            boolean launched = selectedTargetPackage != null && targetAppManager.launchTargetPackage(selectedTargetPackage);
            Log.d(TAG, "Requested launch of target app: " + launched + " (" + selectedTargetPackage + ")");

            // 2) Show loading spinner while we prepare in background
            showLoadingSpinner("Loading...");
            if (startButton != null) startButton.setEnabled(false);

            // 3) Kick off automatic injection/patch workflow (behind the scenes)
            injectionReady = false;
            injectionFailed = false;
            StartupOrchestrator.startAsync(this, selectedTargetPackage, new StartupOrchestrator.Callback() {
                @Override public void onProgress(int percent, String message) {
                    updateLoadingMessage(message);
                }
                @Override public void onSuccess() {
                    injectionReady = true;
                }
                @Override public void onFailure(String error) {
                    injectionFailed = true;
                    Log.e(TAG, "Injection failed: " + error);
                }
            });

            // 4) Poll until the target app is in foreground AND injections are ready, then start floating service
            pollAndStartFloatingWhenReady();
        } catch (Exception e) {
            Log.e(TAG, "proceedWithServiceStart failed", e);
            hideLoadingSpinner();
            if (startButton != null) startButton.setEnabled(true);
            android.widget.Toast.makeText(this, "Failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Poll the system until selected target package is in foreground, then start Floating service.
     */
    private volatile boolean injectionReady = false;
    private volatile boolean injectionFailed = false;

    private void pollAndStartFloatingWhenReady() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long[] waited = {0};
        final long interval = 500; // ms
        final long timeout = 20000; // 20s

        Runnable check = new Runnable() {
            @Override public void run() {
                try {
                    if (injectionFailed) {
                        hideLoadingSpinner();
                        if (startButton != null) startButton.setEnabled(true);
                        android.widget.Toast.makeText(MainActivity.this, "Preparation failed", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedTargetPackage != null && isTargetInForeground(selectedTargetPackage) && injectionReady) {
                        // Start overlay service only now
                        try {
                            Intent svc = new Intent(MainActivity.this, Floating.class);
                            startService(svc);
                            isServiceRunning = true;
                            updateServiceStatus();
                            android.widget.Toast.makeText(MainActivity.this, "Service started", android.widget.Toast.LENGTH_SHORT).show();
                        } catch (Exception se) {
                            Log.e(TAG, "Failed to start Floating service", se);
                        }
                        hideLoadingSpinner();
                        if (stopButton != null) stopButton.setEnabled(true);
                        return;
                    }

                    // Continue polling until timeout
                    waited[0] += interval;
                    if (waited[0] < timeout) {
                        handler.postDelayed(this, interval);
                    } else {
                        Log.w(TAG, "Timeout waiting for target foreground/injection ready");
                        hideLoadingSpinner();
                        if (startButton != null) startButton.setEnabled(true);
                        android.widget.Toast.makeText(MainActivity.this, "Unable to start safely", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Polling error", e);
                    hideLoadingSpinner();
                    if (startButton != null) startButton.setEnabled(true);
                }
            }
        };
        handler.postDelayed(check, interval);
    }

    /**
     * Check if the target package is currently in foreground.
     */
    private boolean isTargetInForeground(String pkg) {
        try {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am == null) return false;
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info != null && info.processName != null && info.processName.equals(pkg)) {
                    return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    private android.app.AlertDialog loadingDialog;

    private void showLoadingSpinner(String message) {
        try {
            if (loadingDialog != null && loadingDialog.isShowing()) return;
            android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
            progressBar.setIndeterminate(true);
            loadingDialog = new android.app.AlertDialog.Builder(this)
                    .setTitle("Please wait")
                    .setMessage(message)
                    .setView(progressBar)
                    .setCancelable(false)
                    .create();
            loadingDialog.show();
        } catch (Exception e) {
            Log.w(TAG, "Unable to show loading spinner", e);
        }
    }

    private void updateLoadingMessage(String msg) {
        try {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.setMessage(msg);
            }
        } catch (Exception ignored) { }
    }

    private void hideLoadingSpinner() {
        try {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        } catch (Exception ignored) { }
    }

    /**
     * Stop the floating mod service and update UI state
     */
    private void stopModService() {
        try {
            stopService(new Intent(this, Floating.class));
            isServiceRunning = false;
            updateServiceStatus();
            if (startButton != null) startButton.setEnabled(true);
            if (stopButton != null) stopButton.setEnabled(false);
            android.widget.Toast.makeText(this, "Service stopped", android.widget.Toast.LENGTH_SHORT).show();
            // Optional auto-clear per user setting
            if (isAutoClearEnabled()) {
                secureExit(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "stopModService failed", e);
        }
    }

    /**
     * Update UI to reflect current service running state
     */
    @SuppressLint("SetTextI18n")
    private void updateServiceStatus() {
        try {
            if (serviceStatus != null) {
                serviceStatus.setText(isServiceRunning ? "Service: Running" : "Service: Stopped");
            }
            if (startButton != null) startButton.setEnabled(!isServiceRunning);
            if (stopButton != null) stopButton.setEnabled(isServiceRunning);
        } catch (Exception e) {
            Log.w(TAG, "updateServiceStatus error", e);
        }
    }

    @Override
    public void onBackPressed() {
        // Respect auto-clear toggle on back
        if (isAutoClearEnabled()) {
            secureExit(true);
        } else {
            try { stopService(new Intent(this, Floating.class)); } catch (Exception ignored) {}
            finish();
        }
    }

    private void secureExit(boolean fromBack) {
        try {
            showLoadingSpinner("Cleaning up...");
            DataClearingManager.getInstance(this).performComprehensiveCleanup(new DataClearingManager.CleanupListener() {
                @Override public void onCleanupStarted() { updateLoadingMessage("Cleaning up..."); }
                @Override public void onCleanupProgress(int p, String task) { updateLoadingMessage(task + " (" + p + "%)"); }
                @Override public void onCleanupCompleted(boolean success) {
                    hideLoadingSpinner();
                    // Finish app completely
                    try { stopService(new Intent(MainActivity.this, Floating.class)); } catch (Exception ignored) {}
                    finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
                @Override public void onCleanupFailed(String error) {
                    hideLoadingSpinner();
                    Log.e(TAG, "Cleanup failed: " + error);
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "secureExit error", e);
            finish();
        }
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
