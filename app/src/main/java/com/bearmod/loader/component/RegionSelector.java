package com.bearmod.loader.component;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.bearmod.loader.utilities.Logx;

/**
 * RegionSelector - Manages PUBG Mobile game region selection and UI state
 *
 * Extracted from MainActivity.java to separate region selection logic.
 * Handles region button states, package mapping, and installation status.
 *
 * Migrated from com.bearmod.activity.MainActivity to com.bearmod.loader.component
 */
public class RegionSelector {
    private static final String TAG = "RegionSelector";

    // Region buttons
    private final Button regionGlobal;
    private final Button regionKorea;
    private final Button regionVietnam;
    private final Button regionTaiwan;
    private final Button regionIndia;

    // Current selection
    private String selectedPackageName;

    // Package mappings
    private static final String PACKAGE_GLOBAL = "com.tencent.ig";
    private static final String PACKAGE_KOREA = "com.pubg.krmobile";
    private static final String PACKAGE_VIETNAM = "com.vng.pubgmobile";
    private static final String PACKAGE_TAIWAN = "com.rekoo.pubgm";
    private static final String PACKAGE_INDIA = "com.pubg.imobile";

    public RegionSelector(Button regionGlobal, Button regionKorea, Button regionVietnam,
                         Button regionTaiwan, Button regionIndia) {
        this.regionGlobal = regionGlobal;
        this.regionKorea = regionKorea;
        this.regionVietnam = regionVietnam;
        this.regionTaiwan = regionTaiwan;
        this.regionIndia = regionIndia;
    }

    /**
     * Setup region button listeners
     */
    public void setupRegionButtons(RegionSelectionCallback callback) {
        setupButton(regionGlobal, PACKAGE_GLOBAL, "Global", callback);
        setupButton(regionKorea, PACKAGE_KOREA, "Korea", callback);
        setupButton(regionVietnam, PACKAGE_VIETNAM, "Vietnam", callback);
        setupButton(regionTaiwan, PACKAGE_TAIWAN, "Taiwan", callback);
        setupButton(regionIndia, PACKAGE_INDIA, "India", callback);
    }

    private void setupButton(Button button, String packageName, String regionName,
                           RegionSelectionCallback callback) {
        if (button != null) {
            button.setOnClickListener(v -> {
                selectRegion(packageName, regionName, callback);
            });
        }
    }

    /**
     * Select a specific region
     */
    public void selectRegion(String packageName, String regionName, RegionSelectionCallback callback) {
        clearAllSelections();
        selectedPackageName = packageName;

        // Highlight selected button
        highlightSelectedButton(packageName);

        Logx.d("REGION_SELECTED: " + regionName + " (" + packageName + ")");

        if (callback != null) {
            callback.onRegionSelected(packageName, regionName);
        }
    }

    /**
     * Clear all region button selections
     */
    public void clearAllSelections() {
        setButtonSelected(regionGlobal, false);
        setButtonSelected(regionKorea, false);
        setButtonSelected(regionVietnam, false);
        setButtonSelected(regionTaiwan, false);
        setButtonSelected(regionIndia, false);
    }

    /**
     * Update region button states based on installation status
     */
    public void updateRegionButtonStates(PackageStatusProvider statusProvider) {
        updateButtonState(regionGlobal, PACKAGE_GLOBAL, "Global", statusProvider);
        updateButtonState(regionKorea, PACKAGE_KOREA, "Korea", statusProvider);
        updateButtonState(regionVietnam, PACKAGE_VIETNAM, "Vietnam", statusProvider);
        updateButtonState(regionTaiwan, PACKAGE_TAIWAN, "Taiwan", statusProvider);
        updateButtonState(regionIndia, PACKAGE_INDIA, "India", statusProvider);
    }

    private void updateButtonState(Button button, String packageName, String regionName,
                                 PackageStatusProvider statusProvider) {
        if (button == null) return;

        boolean isInstalled = statusProvider.isPackageInstalled(packageName);
        String suffix = isInstalled ? " — Open" : " — Install";
        button.setText(regionName + suffix);

        boolean isSelected = packageName.equals(selectedPackageName);
        setButtonSelected(button, isSelected);

        button.setAlpha(isSelected ? 1.0f : 0.9f);

        // Content description for accessibility
        button.setContentDescription(regionName + (isInstalled ? ", installed, long-press to open" : ", not installed, tap to install guidance"));
    }

    /**
     * Select region button for given package name
     */
    public void selectRegionButtonForPackage(String packageName) {
        clearAllSelections();

        switch (packageName) {
            case PACKAGE_GLOBAL:
                if (regionGlobal != null) regionGlobal.setSelected(true);
                break;
            case PACKAGE_KOREA:
                if (regionKorea != null) regionKorea.setSelected(true);
                break;
            case PACKAGE_INDIA:
                if (regionIndia != null) regionIndia.setSelected(true);
                break;
            case PACKAGE_TAIWAN:
                if (regionTaiwan != null) regionTaiwan.setSelected(true);
                break;
            case PACKAGE_VIETNAM:
                if (regionVietnam != null) regionVietnam.setSelected(true);
                break;
        }
    }

    /**
     * Get currently selected package name
     */
    public String getSelectedPackageName() {
        return selectedPackageName;
    }

    /**
     * Set selected package name
     */
    public void setSelectedPackageName(String packageName) {
        this.selectedPackageName = packageName;
        selectRegionButtonForPackage(packageName);
    }

    private void highlightSelectedButton(String packageName) {
        switch (packageName) {
            case PACKAGE_GLOBAL:
                setButtonSelected(regionGlobal, true);
                break;
            case PACKAGE_KOREA:
                setButtonSelected(regionKorea, true);
                break;
            case PACKAGE_INDIA:
                setButtonSelected(regionIndia, true);
                break;
            case PACKAGE_TAIWAN:
                setButtonSelected(regionTaiwan, true);
                break;
            case PACKAGE_VIETNAM:
                setButtonSelected(regionVietnam, true);
                break;
        }
    }

    private void setButtonSelected(Button button, boolean selected) {
        if (button != null) {
            button.setSelected(selected);
        }
    }

    /**
     * Interface for region selection callbacks
     */
    public interface RegionSelectionCallback {
        void onRegionSelected(String packageName, String regionName);
    }

    /**
     * Interface for package status queries
     */
    public interface PackageStatusProvider {
        boolean isPackageInstalled(String packageName);
        String getPackageDisplayName(String packageName);
    }

    /**
     * Callback interface for region selection events
     */
    public interface RegionCallback {
        void onRegionSelected(String packageName, String regionName);
    }

    private RegionCallback callback;

    /**
     * Set callback for region selection events
     */
    public void setCallback(RegionCallback callback) {
        this.callback = callback;
    }
}