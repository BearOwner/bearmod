package com.bearmod.loader.component;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.bearmod.InstallerPackageManager;
import com.bearmod.TargetAppManager;
import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.ResourceProvider;

import java.util.List;

/**
 * UIController - Manages UI components and user interactions
 * Extracted from MainActivity to separate presentation logic from business logic
 *
 * Responsibilities:
 * - View initialization and management
 * - Button listener setup
 * - Region selection handling
 * - Settings controls management
 * - UI state updates
 */
public class UIController {

    private static final String TAG = "UIController";
    private static final String PREFS_NAME = "bearmod_prefs";
    private static final String KEY_AUTO_CLEAR = "auto_clear";
    private static final String KEY_FAST_DOWNLOAD = "fast_download";

    private final ResourceProvider rp;
    private final TargetAppManager targetAppManager;
    private final InstallerPackageManager installerPackageManager;

    // UI Components
    private TextView serviceStatus, targetStatusText, textGameName, textGameVersion, textObbStatus;
    private Button btnFixObb, startButton, stopButton, exitButton;
    private Spinner targetBundleSpinner;
    private Button regionGlobal, regionKorea, regionVietnam, regionTaiwan, regionIndia;
    private TextView serverStatus;
    private ImageView detectionStatusIcon;
    private TextView safeStatus, unsafeStatus;
    private ImageButton navHome, navSettings;
    private ScrollView mainAppLayout, settingsLayout;
    private SwitchCompat switchAutoClear, switchFastDownload;
    private Button btnClearDataNow, btnExitApp;

    // State
    private String selectedTargetPackage;
    private UICallback callback;

    /**
     * Interface for UI events
     */
    public interface UICallback {
        void onStartServiceRequested();
        void onStopServiceRequested();
        void onExitRequested();
        void onRegionSelected(String packageName, String regionName);
        void onTargetBundleSelected(int position);
        void onFixObbRequested(String packageName);
        void onClearDataRequested();
        void onSettingsChanged(String key, boolean enabled);
    }

    public UIController(ResourceProvider rp, TargetAppManager targetAppManager,
                        InstallerPackageManager installerPackageManager) {
        this.rp = rp;
        this.targetAppManager = targetAppManager;
        this.installerPackageManager = installerPackageManager;
    }

    /**
     * Set UI callback for events
     */
    public void setCallback(UICallback callback) {
        this.callback = callback;
    }

    /**
     * Initialize all UI components
     */
    public void initializeViews(View rootView) {
        try {
            // Main components
            serviceStatus = rootView.findViewById(rp.id("service_status"));
            startButton = rootView.findViewById(rp.id("start_button"));
            stopButton = rootView.findViewById(rp.id("stop_button"));
            exitButton = rootView.findViewById(rp.id("exit_button"));

            // Target selection
            targetBundleSpinner = rootView.findViewById(rp.id("target_bundle_spinner"));
            targetStatusText = rootView.findViewById(rp.id("target_status_text"));

            // Game status card
            textGameName = rootView.findViewById(rp.id("text_game_name"));
            textGameVersion = rootView.findViewById(rp.id("text_game_version"));
            textObbStatus = rootView.findViewById(rp.id("text_obb_status"));
            btnFixObb = rootView.findViewById(rp.id("btn_fix_obb"));

            // Region buttons
            regionGlobal = rootView.findViewById(rp.id("region_global"));
            regionKorea = rootView.findViewById(rp.id("region_korea"));
            regionVietnam = rootView.findViewById(rp.id("region_vietnam"));
            regionTaiwan = rootView.findViewById(rp.id("region_taiwan"));
            regionIndia = rootView.findViewById(rp.id("region_india"));

            // Status displays
            serverStatus = rootView.findViewById(rp.id("server_status"));
            detectionStatusIcon = rootView.findViewById(rp.id("detection_status_icon"));
            safeStatus = rootView.findViewById(rp.id("safe_status"));
            unsafeStatus = rootView.findViewById(rp.id("unsafe_status"));

            // Navigation
            navHome = rootView.findViewById(rp.id("nav_home"));
            navSettings = rootView.findViewById(rp.id("nav_settings"));
            mainAppLayout = rootView.findViewById(rp.id("main_app_layout"));
            settingsLayout = rootView.findViewById(rp.id("settings_layout"));

            // Settings controls
            switchAutoClear = rootView.findViewById(rp.id("switch_auto_clear"));
            switchFastDownload = rootView.findViewById(rp.id("switch_fast_download"));
            btnClearDataNow = rootView.findViewById(rp.id("btn_clear_data_now"));
            btnExitApp = rootView.findViewById(rp.id("btn_exit_app"));

            Logx.d("UIController views initialized");

        } catch (Exception e) {
            Logx.e("UIController initialization failed", e);
        }
    }

    /**
     * Setup all button listeners
     */
    public void setupButtonListeners() {
        // Service control buttons
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                if (callback != null) callback.onStartServiceRequested();
            });
        }

        if (stopButton != null) {
            stopButton.setOnClickListener(v -> {
                if (callback != null) callback.onStopServiceRequested();
            });
        }

        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                if (callback != null) callback.onExitRequested();
            });
        }

        // Region selection buttons
        setupRegionButtons();

        // Settings controls
        setupSettingsControls();

        // Fix OBB button
        if (btnFixObb != null) {
            btnFixObb.setOnClickListener(v -> {
                if (callback != null && selectedTargetPackage != null) {
                    callback.onFixObbRequested(selectedTargetPackage);
                }
            });
        }

        Logx.d("UIController button listeners setup");
    }

    /**
     * Setup region selection buttons
     */
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

    /**
     * Handle region selection
     */
    private void selectRegion(String packageName, Button selectedButton, String regionName) {
        clearRegionSelections();
        selectedButton.setSelected(true);
        selectedTargetPackage = packageName;

        if (callback != null) {
            callback.onRegionSelected(packageName, regionName);
        }

        Logx.d("Region selected: " + regionName + " (" + packageName + ")");
    }

    /**
     * Clear all region button selections
     */
    private void clearRegionSelections() {
        if (regionGlobal != null) regionGlobal.setSelected(false);
        if (regionKorea != null) regionKorea.setSelected(false);
        if (regionVietnam != null) regionVietnam.setSelected(false);
        if (regionTaiwan != null) regionTaiwan.setSelected(false);
        if (regionIndia != null) regionIndia.setSelected(false);
    }

    /**
     * Setup settings controls
     */
    private void setupSettingsControls() {
        final SharedPreferences sp = rp.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean autoClear = sp.getBoolean(KEY_AUTO_CLEAR, true);
        boolean fastDl = sp.getBoolean(KEY_FAST_DOWNLOAD, false);

        if (switchAutoClear != null) {
            switchAutoClear.setChecked(autoClear);
            switchAutoClear.setOnCheckedChangeListener((b, checked) -> {
                sp.edit().putBoolean(KEY_AUTO_CLEAR, checked).apply();
                if (callback != null) {
                    callback.onSettingsChanged(KEY_AUTO_CLEAR, checked);
                }
            });
        }

        if (switchFastDownload != null) {
            switchFastDownload.setChecked(fastDl);
            switchFastDownload.setOnCheckedChangeListener((b, checked) -> {
                sp.edit().putBoolean(KEY_FAST_DOWNLOAD, checked).apply();
                if (callback != null) {
                    callback.onSettingsChanged(KEY_FAST_DOWNLOAD, checked);
                }
            });
        }

        if (btnClearDataNow != null) {
            btnClearDataNow.setOnClickListener(v -> {
                if (callback != null) callback.onClearDataRequested();
            });
        }

        if (btnExitApp != null) {
            btnExitApp.setOnClickListener(v -> {
                if (callback != null) callback.onExitRequested();
            });
        }
    }

    /**
     * Setup target bundle spinner
     */
    public void setupTargetBundleSpinner() {
        if (targetBundleSpinner == null || targetAppManager == null) return;

        List<String> targetOptions = targetAppManager.getTargetOptions();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(rp.getContext(),
            android.R.layout.simple_spinner_item, targetOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetBundleSpinner.setAdapter(adapter);

        targetBundleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null) {
                    callback.onTargetBundleSelected(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        targetBundleSpinner.setSelection(0);
    }

    /**
     * Update service status display
     */
    public void updateServiceStatus(boolean isRunning) {
        try {
            if (serviceStatus != null) {
                serviceStatus.setText(isRunning ? "Service: Running" : "Service: Stopped");
            }
            if (startButton != null) startButton.setEnabled(!isRunning);
            if (stopButton != null) stopButton.setEnabled(isRunning);
        } catch (Exception e) {
            Logx.w("Error updating service status", e);
        }
    }

    /**
     * Update target status with installation info
     */
    public void updateTargetStatus(String packageName, String prefix) {
        if (packageName == null || targetStatusText == null) return;

        try {
            String displayName = installerPackageManager != null ?
                installerPackageManager.getPackageDisplayName(packageName) : packageName;
            boolean isInstalled = installerPackageManager != null &&
                installerPackageManager.isPackageInstalled(packageName);
            boolean obbRequired = installerPackageManager != null &&
                installerPackageManager.requiresObb(packageName);
            boolean obbInstalled = !obbRequired || (installerPackageManager != null &&
                installerPackageManager.isObbInstalled(packageName));

            StringBuilder status = new StringBuilder();
            status.append(prefix).append(": ").append(displayName);

            if (isInstalled) {
                String version = installerPackageManager != null ?
                    installerPackageManager.getInstalledPackageVersion(packageName) : null;
                if (version != null) {
                    status.append(" (v").append(version).append(")");
                }

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
            updateGameStatusCard(packageName, displayName, isInstalled, obbRequired, obbInstalled);

        } catch (Exception e) {
            Logx.w("Error updating target status", e);
        }
    }

    /**
     * Update game status card
     */
    private void updateGameStatusCard(String packageName, String displayName, boolean isInstalled,
                                    boolean obbRequired, boolean obbInstalled) {
        try {
            if (textGameName != null) {
                textGameName.setText("Game: " + displayName);
            }

            if (textGameVersion != null) {
                String version = isInstalled && installerPackageManager != null ?
                    installerPackageManager.getInstalledPackageVersion(packageName) : null;
                textGameVersion.setText("Version: " + (version != null ? version :
                    (isInstalled ? "unknown" : "not installed")));
            }

            if (textObbStatus != null) {
                if (!obbRequired) {
                    textObbStatus.setText("OBB: Not required");
                    textObbStatus.setTextColor(rp.color("premium_text_secondary"));
                } else if (obbInstalled) {
                    textObbStatus.setText("OBB: Present");
                    textObbStatus.setTextColor(rp.color("premium_accent_green"));
                } else {
                    textObbStatus.setText("OBB: Missing");
                    textObbStatus.setTextColor(rp.color("premium_accent_red"));
                }
            }

            if (btnFixObb != null) {
                btnFixObb.setVisibility(isInstalled && obbRequired && !obbInstalled ? View.VISIBLE : View.GONE);
            }

        } catch (Exception e) {
            Logx.w("Error updating game status card", e);
        }
    }

    /**
     * Update server status display
     */
    public void updateServerStatus(String regionName, String packageName) {
        if (serverStatus == null) return;

        try {
            boolean isInstalled = targetAppManager != null &&
                targetAppManager.isPackageInstalled(packageName);

            // Get context from ResourceProvider for color access
            Context context = rp.getContext();

            if (isInstalled) {
                serverStatus.setText("SERVER " + regionName);
                updateDetectionStatus(true);
            } else {
                serverStatus.setText("SERVER " + regionName + " (NOT INSTALLED)");
                updateDetectionStatus(false);
            }
        } catch (Exception e) {
            Logx.w("Error updating server status", e);
        }
    }

    /**
     * Update detection status icon
     */
    private void updateDetectionStatus(boolean isDetected) {
        try {
            if (detectionStatusIcon != null) {
                detectionStatusIcon.setImageResource(isDetected ? rp.drawable("ic_check_circle") : rp.drawable("ic_error"));
                detectionStatusIcon.setColorFilter(rp.color(isDetected ? "premium_accent_green" : "premium_accent_red"));
            }
            updateSafetyStatus(isDetected);
        } catch (Exception e) {
            Logx.w("Error updating detection status", e);
        }
    }

    /**
     * Update safety status display
     */
    private void updateSafetyStatus(boolean isSafe) {
        try {
            if (safeStatus != null && unsafeStatus != null) {
                if (isSafe) {
                    safeStatus.setVisibility(View.VISIBLE);
                    unsafeStatus.setVisibility(View.GONE);
                } else {
                    safeStatus.setVisibility(View.GONE);
                    unsafeStatus.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Logx.w("Error updating safety status", e);
        }
    }

    /**
     * Get selected target package
     */
    public String getSelectedTargetPackage() {
        return selectedTargetPackage;
    }

    /**
     * Set selected target package
     */
    public void setSelectedTargetPackage(String packageName) {
        this.selectedTargetPackage = packageName;
    }

    /**
     * Get navigation components for NavigationManager
     */
    public ScrollView getMainAppLayout() { return mainAppLayout; }
    public ScrollView getSettingsLayout() { return settingsLayout; }
    public ImageButton getNavHome() { return navHome; }
    public ImageButton getNavSettings() { return navSettings; }

    /**
     * Clean up resources
     */
    public void cleanup() {
        callback = null;
        Logx.d("UIController cleanup completed");
    }
}