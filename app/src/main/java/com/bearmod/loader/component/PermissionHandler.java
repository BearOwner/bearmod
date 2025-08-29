package com.bearmod.loader.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import com.bearmod.PermissionManager;
import com.bearmod.loader.utilities.Logx;

/**
 * PermissionHandler - Manages system permission requests and status checks
 *
 * Extracted from LoginActivity.java to separate permission handling logic.
 * Handles overlay and storage permission requests with user-friendly dialogs.
 *
 * Migrated from com.bearmod.activity.LoginActivity to com.bearmod.loader.component
 */
public class PermissionHandler {
    private static final String TAG = "PermissionHandler";

    private final Context context;
    private final Activity activity;
    private final PermissionManager permissionManager;

    /**
     * Interface for permission result callbacks
     */
    public interface PermissionCallback {
        void onPermissionGranted(String permission);
        void onPermissionDenied(String permission);
        void onPermissionRequestCancelled();
    }

    public PermissionHandler(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.permissionManager = PermissionManager.Companion.getInstance(context);
    }

    /**
     * Check and prompt for critical permissions
     */
    public void checkAndPromptPermissions(PermissionCallback callback) {
        try {
            // 1) Overlay (floating) permission
            PermissionManager.PermissionStatus overlay = permissionManager.checkOverlayPermission();
            if (!overlay.isGranted()) {
                showOverlayPermissionDialog(callback);
                return;
            }

            // 2) Storage readiness check (version-aware). SAF is still used for OBB operations later.
            PermissionManager.PermissionStatus storage = permissionManager.checkStoragePermission();
            if (!storage.isGranted()) {
                showStoragePermissionDialog(callback);
                return;
            }

            // All permissions granted
            if (callback != null) {
                callback.onPermissionGranted("all");
            }

        } catch (Throwable t) {
            Logx.w("PERM_PREFLIGHT_FAIL", t);
            if (callback != null) {
                callback.onPermissionRequestCancelled();
            }
        }
    }

    /**
     * Show overlay permission dialog
     */
    private void showOverlayPermissionDialog(PermissionCallback callback) {
        new AlertDialog.Builder(context)
                .setTitle("Please allow permission: Floating")
                .setMessage("BearMod needs overlay permission to display its floating interface over the game.")
                .setCancelable(false)
                .setPositiveButton("Grant", (d, w) -> {
                    requestOverlayPermission();
                    if (callback != null) {
                        callback.onPermissionRequestCancelled(); // Will re-check on resume
                    }
                })
                .setNegativeButton("Not now", (d, w) -> {
                    if (callback != null) {
                        callback.onPermissionRequestCancelled();
                    }
                })
                .show();
    }

    /**
     * Show storage permission dialog
     */
    private void showStoragePermissionDialog(PermissionCallback callback) {
        new AlertDialog.Builder(context)
                .setTitle("File Access Permission")
                .setMessage("This app needs permission to access files needed for game data validation. Please grant permission to continue.")
                .setCancelable(false)
                .setPositiveButton("Grant Permission", (d, w) -> {
                    requestStoragePermission();
                    if (callback != null) {
                        callback.onPermissionRequestCancelled(); // Will re-check on resume
                    }
                })
                .setNegativeButton("Exit", (d, w) -> {
                    if (callback != null) {
                        callback.onPermissionDenied("storage");
                    }
                })
                .show();
    }

    /**
     * Request overlay permission
     */
    public void requestOverlayPermission() {
        try {
            Logx.d("Requesting overlay permission via PermissionManager");
            permissionManager.requestOverlayPermission(activity, PermissionManager.REQUEST_OVERLAY_PERMISSION);
        } catch (Exception e) {
            Logx.w("Failed to request overlay permission", e);
            // Fallback to direct intent
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            activity.startActivity(intent);
        }
    }

    /**
     * Request storage permission
     */
    public void requestStoragePermission() {
        try {
            Logx.d("Requesting storage permission via PermissionManager");
            permissionManager.requestStoragePermission(activity, PermissionManager.REQUEST_STORAGE_PERMISSION);
        } catch (Exception e) {
            Logx.w("Failed to request storage permission", e);
        }
    }

    /**
     * Handle permission result from onActivityResult
     */
    public void handlePermissionResult(int requestCode, int resultCode, Intent data) {
        try {
            if (permissionManager != null) {
                permissionManager.handlePermissionResult(requestCode, resultCode, null);
            }
        } catch (Throwable t) {
            Logx.w("Permission result handling failed", t);
        }
    }

    /**
     * Check if overlay permission is granted
     */
    public boolean isOverlayPermissionGranted() {
        try {
            return permissionManager != null && permissionManager.checkOverlayPermission().isGranted();
        } catch (Exception e) {
            Logx.w("Error checking overlay permission", e);
            return false;
        }
    }

    /**
     * Check if storage permission is granted
     */
    public boolean isStoragePermissionGranted() {
        try {
            return permissionManager != null && permissionManager.checkStoragePermission().isGranted();
        } catch (Exception e) {
            Logx.w("Error checking storage permission", e);
            return false;
        }
    }
}