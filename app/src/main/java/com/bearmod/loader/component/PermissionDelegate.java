package com.bearmod.loader.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bearmod.loader.utilities.Logx;
import com.bearmod.loader.utilities.PermissionManager;
import com.bearmod.loader.utilities.ResourceProvider;

/**
 * PermissionDelegate - Handles permission management for activities
 * Extracted from MainActivity to centralize permission logic
 *
 * Responsibilities:
 * - Permission checking and requesting
 * - Permission result handling
 * - User guidance for permission issues
 * - Integration with PermissionManager utility
 */
public class PermissionDelegate {

    private static final String TAG = "PermissionDelegate";

    private final ResourceProvider rp;
    private final PermissionManager permissionManager;
    private PermissionCallback callback;

    // Activity Result Launchers for modern permission handling
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private ActivityResultLauncher<Intent> unknownSourcesPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;

    /**
     * Interface for permission events
     */
    public interface PermissionCallback {
        void onPermissionGranted(String permission);
        void onPermissionDenied(String permission, boolean canShowRationale);
        void onPermissionPermanentlyDenied(String permission);
        void onAllPermissionsGranted();
        void onPermissionError(String error);
    }

    public PermissionDelegate(ResourceProvider rp) {
        this.rp = rp;
        this.permissionManager = PermissionManager.Companion.getInstance(rp.app());
    }

    /**
     * Initialize with activity for result launchers
     */
    public void initialize(AppCompatActivity activity) {
        setupPermissionLaunchers(activity);
    }

    /**
     * Set permission callback for events
     */
    public void setCallback(PermissionCallback callback) {
        this.callback = callback;
    }

    /**
     * Setup modern Activity Result API launchers
     */
    private void setupPermissionLaunchers(AppCompatActivity activity) {
        // Overlay permission launcher
        overlayPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                boolean granted = Settings.canDrawOverlays(rp.app());
                handlePermissionResult("SYSTEM_ALERT_WINDOW", granted);
            });

        // Unknown sources permission launcher
        unknownSourcesPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                PermissionManager.PermissionStatus status = permissionManager.checkUnknownSourcesPermission();
                handlePermissionResult("INSTALL_UNKNOWN_APPS", status.isGranted());
            });

        // Storage permission launcher
        storagePermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean allGranted = true;
                for (boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                handlePermissionResult("STORAGE", allGranted);
            });
    }

    /**
     * Check if all required permissions are granted
     */
    public boolean areAllRequiredPermissionsGranted() {
        try {
            PermissionManager.PermissionStatus unknownSources = permissionManager.checkUnknownSourcesPermission();
            PermissionManager.PermissionStatus storage = permissionManager.checkStoragePermission();
            PermissionManager.PermissionStatus overlay = permissionManager.checkOverlayPermission();

            return unknownSources.isGranted() && storage.isGranted() && overlay.isGranted();

        } catch (Exception e) {
            Logx.e("Error checking permissions", e);
            return false;
        }
    }

    /**
     * Request unknown sources permission
     */
    public void requestUnknownSourcesPermission() {
        try {
            if (permissionManager != null) {
                Logx.d("Requesting unknown sources permission via PermissionManager");
                permissionManager.requestUnknownSourcesPermission(null, PermissionManager.REQUEST_UNKNOWN_SOURCES);
            } else {
                Logx.w("PermissionManager not initialized - using fallback");
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + rp.app().getPackageName()));
                unknownSourcesPermissionLauncher.launch(intent);
            }
        } catch (Exception e) {
            Logx.e("Error requesting unknown sources permission", e);
            notifyError("Error requesting unknown sources permission: " + e.getMessage());
        }
    }

    /**
     * Request storage permission
     */
    public void requestStoragePermission() {
        try {
            if (permissionManager != null) {
                Logx.d("Requesting storage permission via PermissionManager");
                permissionManager.requestStoragePermission(null, PermissionManager.REQUEST_STORAGE_PERMISSION);
            } else {
                Logx.w("PermissionManager not initialized - using fallback");
                // Request basic storage permission
                String[] permissions = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                storagePermissionLauncher.launch(permissions);
            }
        } catch (Exception e) {
            Logx.e("Error requesting storage permission", e);
            notifyError("Error requesting storage permission: " + e.getMessage());
        }
    }

    /**
     * Request overlay permission
     */
    public void requestOverlayPermission() {
        try {
            if (permissionManager != null) {
                Logx.d("Requesting overlay permission via PermissionManager");
                permissionManager.requestOverlayPermission(null, PermissionManager.REQUEST_OVERLAY_PERMISSION);
            } else {
                Logx.w("PermissionManager not initialized - using fallback");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + rp.app().getPackageName()));
                overlayPermissionLauncher.launch(intent);
            }
        } catch (Exception e) {
            Logx.e("Error requesting overlay permission", e);
            notifyError("Error requesting overlay permission: " + e.getMessage());
        }
    }

    /**
     * Request all required permissions in sequence
     */
    public void requestAllRequiredPermissions() {
        try {
            // Check current status first
            PermissionManager.PermissionStatus unknownSources = permissionManager.checkUnknownSourcesPermission();
            PermissionManager.PermissionStatus storage = permissionManager.checkStoragePermission();
            PermissionManager.PermissionStatus overlay = permissionManager.checkOverlayPermission();

            // Request missing permissions
            if (!unknownSources.isGranted()) {
                requestUnknownSourcesPermission();
            } else if (!storage.isGranted()) {
                requestStoragePermission();
            } else if (!overlay.isGranted()) {
                requestOverlayPermission();
            } else {
                // All permissions granted
                if (callback != null) {
                    callback.onAllPermissionsGranted();
                }
            }

        } catch (Exception e) {
            Logx.e("Error requesting all permissions", e);
            notifyError("Error requesting permissions: " + e.getMessage());
        }
    }

    /**
     * Handle permission result
     */
    private void handlePermissionResult(String permission, boolean granted) {
        try {
            if (granted) {
                Logx.d("Permission granted: " + permission);
                if (callback != null) {
                    callback.onPermissionGranted(permission);
                }

                // Check if all permissions are now granted
                if (areAllRequiredPermissionsGranted()) {
                    if (callback != null) {
                        callback.onAllPermissionsGranted();
                    }
                }
            } else {
                Logx.w("Permission denied: " + permission);
                showPermissionDeniedMessage(permission);

                if (callback != null) {
                    callback.onPermissionDenied(permission, false); // Assume can't show rationale for system settings
                }
            }
        } catch (Exception e) {
            Logx.e("Error handling permission result", e);
        }
    }

    /**
     * Show user-friendly permission denied message
     */
    private void showPermissionDeniedMessage(String permission) {
        try {
            String message = getPermissionExplanation(permission);
            Toast.makeText(rp.app(), message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Logx.w("Error showing permission message", e);
        }
    }

    /**
     * Get user-friendly permission explanation
     */
    private String getPermissionExplanation(String permission) {
        switch (permission) {
            case "INSTALL_UNKNOWN_APPS":
                return "Unknown Sources permission is required to install APK files for game packages.";
            case "STORAGE":
                return "Storage permission is required to access OBB files and game data.";
            case "SYSTEM_ALERT_WINDOW":
                return "Overlay permission is required for ESP and mod interface functionality.";
            default:
                return "This permission is required for BearMod to function properly.";
        }
    }

    /**
     * Show detailed permission denied dialog
     */
    public void showPermissionDeniedDialog(String permission) {
        try {
            String title = "Permission Required";
            String message = getPermissionExplanation(permission) +
                "\n\nWould you like to grant this permission now?";

            new android.app.AlertDialog.Builder(rp.app())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Grant", (dialog, which) -> {
                    switch (permission) {
                        case "INSTALL_UNKNOWN_APPS":
                            requestUnknownSourcesPermission();
                            break;
                        case "STORAGE":
                            requestStoragePermission();
                            break;
                        case "SYSTEM_ALERT_WINDOW":
                            requestOverlayPermission();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();

        } catch (Exception e) {
            Logx.e("Error showing permission dialog", e);
        }
    }

    /**
     * Log current permission status for debugging
     */
    public void logPermissionStatus() {
        try {
            Logx.d("Permission Status on Startup:");
            if (permissionManager != null) {
                PermissionManager.PermissionStatus unknownSources = permissionManager.checkUnknownSourcesPermission();
                PermissionManager.PermissionStatus storage = permissionManager.checkStoragePermission();
                PermissionManager.PermissionStatus overlay = permissionManager.checkOverlayPermission();

                Logx.d("Unknown Sources: " + unknownSources.isGranted());
                Logx.d("Storage: " + storage.isGranted());
                Logx.d("Overlay: " + overlay.isGranted());
            } else {
                Logx.w("PermissionManager not initialized - using fallback checks");
                Logx.d("Storage: " + isStoragePermissionGrantedFallback());
                Logx.d("Overlay: " + Settings.canDrawOverlays(rp.app()));
            }
        } catch (Exception e) {
            Logx.e("Error checking permission status", e);
        }
    }

    /**
     * Fallback storage permission check
     */
    private boolean isStoragePermissionGrantedFallback() {
        try {
            return androidx.core.content.ContextCompat.checkSelfPermission(rp.app(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Logx.w("Error checking storage permission fallback", e);
            return false;
        }
    }

    /**
     * Check if storage permission is granted
     */
    public boolean isStoragePermissionGranted() {
        try {
            if (permissionManager != null) {
                PermissionManager.PermissionStatus status = permissionManager.checkStoragePermission();
                return status.isGranted();
            } else {
                return isStoragePermissionGrantedFallback();
            }
        } catch (Exception e) {
            Logx.e("Error checking storage permission", e);
            return false;
        }
    }

    /**
     * Notify error to callback
     */
    private void notifyError(String error) {
        Logx.e("Permission error: " + error);
        if (callback != null) {
            callback.onPermissionError(error);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        callback = null;
        Logx.d("PermissionDelegate cleanup completed");
    }
}