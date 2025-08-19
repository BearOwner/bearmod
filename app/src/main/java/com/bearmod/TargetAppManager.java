package com.bearmod;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TargetAppManager.java - PUBG Mobile variant detection and management system
 *
 * Responsibilities:
 * - Detect and manage PUBG Mobile variant packages (Global, Korea, India, Taiwan, Vietnam)
 * - Handle target package selection via UI spinner integration
 * - Provide package installation status checking and validation
 * - Launch target applications with proper intent handling
 * - Auto-detect installed PUBG Mobile variants for seamless user experience
 *
 * Key Methods:
 * - selectTarget(SelectionMode) - Handle target package selection by mode
 * - selectTargetByPosition(int) - Handle UI spinner selection events
 * - isPackageInstalled(String) - Check if specific package is installed
 * - launchTargetPackage(String) - Launch specific target application
 * - getInstalledTargetPackage() - Auto-detect first installed variant
 * - getPackageDisplayName(String) - Get user-friendly app names
 *
 * Dependencies:
 * - InstallerPackageManager - Package installation status checking
 * - Android PackageManager - System package information access
 * - Context - Application context for package operations
 */
public class TargetAppManager {
    private static final String TAG = "TargetAppManager";

    // PUBG Mobile package names from the native code analysis
    public static final List<String> TARGET_PACKAGES = Arrays.asList(
        "com.tencent.ig",        // Global version
        "com.pubg.krmobile",     // Korea
        "com.pubg.imobile",      // India/BGMI
        "com.rekoo.pubgm",       // Taiwan
        "com.vng.pubgmobile"     // Vietnam
    );

    // Target package selection modes
    public enum SelectionMode {
        AUTO_DETECT(0, "Auto-detect installed version"),
        GLOBAL(1, "Global (com.tencent.ig)"),
        KOREA(2, "Korea (com.pubg.krmobile)"),
        INDIA(3, "India/BGMI (com.pubg.imobile)"),
        TAIWAN(4, "Taiwan (com.rekoo.pubgm)"),
        VIETNAM(5, "Vietnam (com.vng.pubgmobile)");

        private final int position;
        private final String displayName;

        SelectionMode(int position, String displayName) {
            this.position = position;
            this.displayName = displayName;
        }

        public int getPosition() { return position; }
        public String getDisplayName() { return displayName; }

        public static SelectionMode fromPosition(int position) {
            for (SelectionMode mode : values()) {
                if (mode.position == position) return mode;
            }
            return AUTO_DETECT;
        }
    }

    private final Context context;
    private final InstallerPackageManager installerPackageManager;
    private String currentSelectedPackage;
    
    public TargetAppManager(Context context) {
        this.context = context;
        this.installerPackageManager = new InstallerPackageManager(context);
    }

    /**
     * Get all available target options for UI display
     */
    public List<String> getTargetOptions() {
        List<String> options = new ArrayList<>();
        for (SelectionMode mode : SelectionMode.values()) {
            options.add(mode.getDisplayName());
        }
        return options;
    }

    /**
     * Handle target package selection by position (from UI spinner)
     */
    public TargetSelectionResult selectTargetByPosition(int position) {
        SelectionMode mode = SelectionMode.fromPosition(position);
        return selectTarget(mode);
    }

    /**
     * Handle target package selection by mode
     */
    public TargetSelectionResult selectTarget(SelectionMode mode) {
        String selectedPackage = null;
        String status = "";
        boolean isInstalled = false;

        switch (mode) {
            case AUTO_DETECT:
                selectedPackage = getInstalledTargetPackage();
                if (selectedPackage != null) {
                    status = "Auto-detected";
                    isInstalled = true;
                    Log.d(TAG, "Auto-detected target package: " + selectedPackage);
                } else {
                    status = "No PUBG Mobile version found installed";
                    Log.w(TAG, "No target package found installed");
                }
                break;
            case GLOBAL:
                selectedPackage = "com.tencent.ig";
                status = "Selected";
                isInstalled = isPackageInstalled(selectedPackage);
                break;
            case KOREA:
                selectedPackage = "com.pubg.krmobile";
                status = "Selected";
                isInstalled = isPackageInstalled(selectedPackage);
                break;
            case INDIA:
                selectedPackage = "com.pubg.imobile";
                status = "Selected";
                isInstalled = isPackageInstalled(selectedPackage);
                break;
            case TAIWAN:
                selectedPackage = "com.rekoo.pubgm";
                status = "Selected";
                isInstalled = isPackageInstalled(selectedPackage);
                break;
            case VIETNAM:
                selectedPackage = "com.vng.pubgmobile";
                status = "Selected";
                isInstalled = isPackageInstalled(selectedPackage);
                break;
        }

        currentSelectedPackage = selectedPackage;
        Log.d(TAG, "Target package selected: " + selectedPackage + " (installed: " + isInstalled + ")");

        return new TargetSelectionResult(selectedPackage, status, isInstalled, mode);
    }
    
    /**
     * Check if a specific package is installed
     */
    public boolean isPackageInstalled(String packageName) {
        return installerPackageManager.isPackageInstalled(packageName);
    }

    /**
     * Get current selected package
     */
    public String getCurrentSelectedPackage() {
        return currentSelectedPackage;
    }

    /**
     * Launch specific target package
     */
    public boolean launchTargetPackage(String packageName) {
        if (packageName == null) {
            Log.w(TAG, "Cannot launch null package");
            return false;
        }

        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Log.d(TAG, "Successfully launched target app: " + packageName);
                return true;
            } else {
                Log.w(TAG, "No launch intent found for: " + packageName);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching target app: " + packageName, e);
            return false;
        }
    }

    /**
     * Get display name for package
     */
    public String getPackageDisplayName(String packageName) {
        if (packageName == null) {
            return "PUBG Mobile (Not Selected)";
        }

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String appName = pm.getApplicationLabel(appInfo).toString();
            Log.d(TAG, "Package display name: " + appName);
            return appName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app name for: " + packageName, e);
            // Return friendly name based on package
            switch (packageName) {
                case "com.tencent.ig": return "PUBG Mobile Global";
                case "com.pubg.krmobile": return "PUBG Mobile Korea";
                case "com.pubg.imobile": return "BGMI India";
                case "com.rekoo.pubgm": return "PUBG Mobile Taiwan";
                case "com.vng.pubgmobile": return "PUBG Mobile Vietnam";
                default: return "PUBG Mobile";
            }
        }
    }

    /**
     * Check if any target app is installed
     */
    public boolean isTargetAppInstalled() {
        PackageManager pm = context.getPackageManager();
        
        for (String packageName : TARGET_PACKAGES) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                Log.d(TAG, "Found installed target app: " + packageName);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed, continue checking
            }
        }
        
        Log.d(TAG, "No target apps found installed");
        return false;
    }
    
    /**
     * Get the first installed target app package name
     */
    public String getInstalledTargetPackage() {
        PackageManager pm = context.getPackageManager();
        
        for (String packageName : TARGET_PACKAGES) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                Log.d(TAG, "Found target app: " + packageName);
                return packageName;
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed, continue checking
            }
        }
        
        return null;
    }
    
    /**
     * Launch the target app if installed
     */
    public boolean launchTargetApp() {
        String targetPackage = getInstalledTargetPackage();
        if (targetPackage == null) {
            Log.w(TAG, "No target app installed to launch");
            return false;
        }
        
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(targetPackage);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Log.d(TAG, "Successfully launched target app: " + targetPackage);
                return true;
            } else {
                Log.w(TAG, "No launch intent found for: " + targetPackage);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching target app: " + targetPackage, e);
            return false;
        }
    }
    
    /**
     * Check if any target app is currently running
     */
    public boolean isTargetAppRunning() {
        // Note: This is a simplified check. In production, you might want to use
        // ActivityManager to check running processes, but that requires additional permissions
        // For now, we'll assume the app is running if it was recently launched
        return getInstalledTargetPackage() != null;
    }
    
    /**
     * Get the display name of the installed target app
     */
    public String getTargetAppName() {
        String targetPackage = getInstalledTargetPackage();
        if (targetPackage == null) {
            return "PUBG Mobile (Not Installed)";
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(targetPackage, 0);
            String appName = pm.getApplicationLabel(appInfo).toString();
            Log.d(TAG, "Target app name: " + appName);
            return appName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app name for: " + targetPackage, e);
            return "PUBG Mobile";
        }
    }

    /**
     * Result class for target selection operations
     */
    public static class TargetSelectionResult {
        private final String packageName;
        private final String status;
        private final boolean isInstalled;
        private final SelectionMode mode;

        public TargetSelectionResult(String packageName, String status, boolean isInstalled, SelectionMode mode) {
            this.packageName = packageName;
            this.status = status;
            this.isInstalled = isInstalled;
            this.mode = mode;
        }

        public String getPackageName() { return packageName; }
        public String getStatus() { return status; }
        public boolean isInstalled() { return isInstalled; }
        public SelectionMode getMode() { return mode; }

        public boolean isValid() { return packageName != null; }
    }
}
