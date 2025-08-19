package com.bearmod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages package installation and OBB file management for target game packages
 * Handles APK installation requests, OBB file detection/validation, and integration
 * with permission flows for optimal game installation experience.
 */
public class InstallerPackageManager {
    private static final String TAG = "InstallerPackageManager";
    
    // Installation request codes
    public static final int REQUEST_INSTALL_APK = 2001;
    public static final int REQUEST_UNKNOWN_SOURCES = 2002;
    
    // OBB file patterns and requirements
    private static final String OBB_DIRECTORY = "Android/obb";
    private static final String[] OBB_EXTENSIONS = {".obb", ".main", ".patch"};
    private static final long MIN_OBB_SIZE = 50 * 1024 * 1024; // 50MB minimum
    
    // Target package information with OBB requirements
    private static final Map<String, PackageInfo> TARGET_PACKAGE_INFO = new HashMap<>();
    
    static {
        // Initialize package information with OBB requirements
        TARGET_PACKAGE_INFO.put("com.tencent.ig", new PackageInfo("PUBG Mobile Global", true, 2000));
        TARGET_PACKAGE_INFO.put("com.pubg.krmobile", new PackageInfo("PUBG Mobile Korea", true, 1800));
        TARGET_PACKAGE_INFO.put("com.pubg.imobile", new PackageInfo("BGMI India", true, 1900));
        TARGET_PACKAGE_INFO.put("com.rekoo.pubgm", new PackageInfo("PUBG Mobile Taiwan", true, 1700));
        TARGET_PACKAGE_INFO.put("com.vng.pubgmobile", new PackageInfo("PUBG Mobile Vietnam", true, 1600));
    }
    
    private final Context context;
    private final PackageManager packageManager;
    
    public InstallerPackageManager(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }
    
    // Unknown Sources permission methods removed - use PermissionManager instead
    // For permission checks: PermissionManager.getInstance(context).checkUnknownSourcesPermission()
    // For permission requests: PermissionManager.getInstance(context).requestUnknownSourcesPermission()
    
    /**
     * Check if a specific package can be installed
     * Note: Unknown sources permission check should be done via PermissionManager
     */
    public boolean canInstallPackage(String packageName) {
        // Note: Caller should check unknown sources permission via PermissionManager
        // Example: PermissionManager.getInstance(context).checkUnknownSourcesPermission().isGranted()

        // Check if package is already installed
        if (isPackageInstalled(packageName)) {
            Log.d(TAG, "Package already installed: " + packageName);
            return true;
        }

        return true;
    }
    
    /**
     * Check if a package is installed
     */
    public boolean isPackageInstalled(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return appInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Get package version if installed
     */
    public String getInstalledPackageVersion(String packageName) {
        try {
            android.content.pm.PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Create an intent to install an APK file
     */
    public Intent createInstallIntent(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use FileProvider for Android N+
            Uri apkUri = FileProvider.getUriForFile(
                context, context.getPackageName() + ".fileprovider", apkFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Uri apkUri = Uri.fromFile(apkFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
    
    /**
     * Check if OBB files are installed for a specific package
     */
    public boolean isObbInstalled(String packageName) {
        if (packageName == null) {
            return false;
        }
        
        File obbDir = getObbDirectory(packageName);
        if (!obbDir.exists()) {
            Log.d(TAG, "OBB directory does not exist: " + obbDir.getAbsolutePath());
            return false;
        }
        
        // Check for OBB files
        File[] obbFiles = obbDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : OBB_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        if (obbFiles == null || obbFiles.length == 0) {
            Log.d(TAG, "No OBB files found in: " + obbDir.getAbsolutePath());
            return false;
        }
        
        // Validate OBB files
        return validateObbFiles(obbFiles, packageName);
    }
    
    /**
     * Get the OBB directory for a package
     */
    public File getObbDirectory(String packageName) {
        return new File(Environment.getExternalStorageDirectory(), OBB_DIRECTORY + "/" + packageName);
    }
    
    /**
     * Validate OBB files for completeness and integrity
     */
    private boolean validateObbFiles(File[] obbFiles, String packageName) {
        long totalSize = 0;
        boolean hasMainObb = false;
        
        for (File obbFile : obbFiles) {
            if (!obbFile.exists() || !obbFile.canRead()) {
                Log.w(TAG, "OBB file not accessible: " + obbFile.getName());
                continue;
            }
            
            long fileSize = obbFile.length();
            totalSize += fileSize;
            
            // Check for main OBB file
            if (obbFile.getName().contains("main") || obbFile.getName().endsWith(".obb")) {
                hasMainObb = true;
            }
            
            Log.d(TAG, "Found OBB file: " + obbFile.getName() + " (" + fileSize + " bytes)");
        }
        
        // Check minimum size requirement
        PackageInfo packageInfo = TARGET_PACKAGE_INFO.get(packageName);
        if (packageInfo != null && packageInfo.requiresObb) {
            long minSize = packageInfo.minObbSizeMB * 1024 * 1024;
            if (totalSize < minSize) {
                Log.w(TAG, "OBB files too small. Found: " + totalSize + ", Required: " + minSize);
                return false;
            }
        }
        
        if (!hasMainObb) {
            Log.w(TAG, "No main OBB file found for: " + packageName);
            return false;
        }
        
        Log.d(TAG, "OBB validation successful for " + packageName + ". Total size: " + totalSize);
        return true;
    }
    
    /**
     * Get OBB installation status and details
     */
    public ObbStatus getObbStatus(String packageName) {
        if (packageName == null) {
            return new ObbStatus(false, "No package specified", 0, new ArrayList<>());
        }
        
        PackageInfo packageInfo = TARGET_PACKAGE_INFO.get(packageName);
        if (packageInfo == null || !packageInfo.requiresObb) {
            return new ObbStatus(true, "OBB not required", 0, new ArrayList<>());
        }
        
        File obbDir = getObbDirectory(packageName);
        if (!obbDir.exists()) {
            return new ObbStatus(false, "OBB directory not found", 0, new ArrayList<>());
        }
        
        File[] obbFiles = obbDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            for (String ext : OBB_EXTENSIONS) {
                if (lowerName.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
        
        if (obbFiles == null || obbFiles.length == 0) {
            return new ObbStatus(false, "No OBB files found", 0, new ArrayList<>());
        }
        
        List<String> fileNames = new ArrayList<>();
        long totalSize = 0;
        
        for (File file : obbFiles) {
            fileNames.add(file.getName());
            totalSize += file.length();
        }
        
        boolean isValid = validateObbFiles(obbFiles, packageName);
        String status = isValid ? "OBB files valid" : "OBB files incomplete or corrupted";
        
        return new ObbStatus(isValid, status, totalSize, fileNames);
    }
    
    /**
     * Get display name for a package
     */
    public String getPackageDisplayName(String packageName) {
        PackageInfo packageInfo = TARGET_PACKAGE_INFO.get(packageName);
        if (packageInfo != null) {
            return packageInfo.displayName;
        }
        
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }
    
    /**
     * Check if a package requires OBB files
     */
    public boolean requiresObb(String packageName) {
        PackageInfo packageInfo = TARGET_PACKAGE_INFO.get(packageName);
        return packageInfo != null && packageInfo.requiresObb;
    }
    
    /**
     * Get all supported target packages
     */
    public List<String> getSupportedPackages() {
        return new ArrayList<>(TARGET_PACKAGE_INFO.keySet());
    }

    /**
         * Inner class to hold package information
         */
        private record PackageInfo(String displayName, boolean requiresObb, long minObbSizeMB) {
    }

    /**
         * Class to hold OBB status information
         */
        public record ObbStatus(boolean isValid, String statusMessage, long totalSizeBytes, List<String> fileNames) {

        @SuppressLint("DefaultLocale")
        public String getFormattedSize() {
                if (totalSizeBytes < 1024) {
                    return totalSizeBytes + " B";
                } else if (totalSizeBytes < 1024 * 1024) {
                    return String.format("%.1f KB", totalSizeBytes / 1024.0);
                } else if (totalSizeBytes < 1024 * 1024 * 1024) {
                    return String.format("%.1f MB", totalSizeBytes / (1024.0 * 1024.0));
                } else {
                    return String.format("%.1f GB", totalSizeBytes / (1024.0 * 1024.0 * 1024.0));
                }
            }
        }
}
