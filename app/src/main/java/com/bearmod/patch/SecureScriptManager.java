package com.bearmod.patch;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure script manager for production deployment
 * Handles script loading from different sources based on build type
 */
public class SecureScriptManager {
    private static final String TAG = "SecureScriptManager";
    
    private static SecureScriptManager instance;
    private final Context context;
    private final boolean isDebugBuild;
    
    // Script cache for performance
    private final Map<String, String> scriptCache = new HashMap<>();
    
    private SecureScriptManager(Context context) {
        this.context = context;
        this.isDebugBuild = isDebugBuild(context);
        Log.d(TAG, "SecureScriptManager initialized - Debug mode: " + isDebugBuild);
    }
    
    public static SecureScriptManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecureScriptManager(context);
        }
        return instance;
    }
    
    /**
     * Load script content based on build type
     */
    public String loadScript(String patchId) {
        // Check cache first
        if (scriptCache.containsKey(patchId)) {
            Log.d(TAG, "Loading script from cache: " + patchId);
            return scriptCache.get(patchId);
        }
        
        String scriptContent = null;
        
        if (isDebugBuild) {
            // Debug: Load from App\Scripts\ directory
            scriptContent = loadFromScriptsDirectory(patchId);
            if (scriptContent == null) {
                // Fallback to assets for debug
                scriptContent = loadFromAssets(patchId);
            }
        } else {
            // Production: Load from secure embedded resources
            scriptContent = loadFromSecureStorage(patchId);
        }
        
        if (scriptContent != null) {
            // Cache the script
            scriptCache.put(patchId, scriptContent);
            Log.d(TAG, "Script loaded and cached: " + patchId);
        } else {
            Log.e(TAG, "Failed to load script: " + patchId);
        }
        
        return scriptContent;
    }
    
    /**
     * Load script from App\Scripts\ directory (Debug only)
     */
    private String loadFromScriptsDirectory(String patchId) {
        try {
            // Get app's external files directory
            File scriptsDir = new File(context.getExternalFilesDir(null), "Scripts");
            File patchDir = new File(scriptsDir, patchId);
            File scriptFile = new File(patchDir, "script.js");
            
            if (!scriptFile.exists()) {
                Log.d(TAG, "Script file not found in Scripts directory: " + scriptFile.getAbsolutePath());
                return null;
            }
            
            // Read script content
            FileInputStream fis = new FileInputStream(scriptFile);
            byte[] buffer = new byte[(int) scriptFile.length()];
            fis.read(buffer);
            fis.close();
            
            String content = new String(buffer);
            Log.d(TAG, "Loaded script from Scripts directory: " + patchId);
            return content;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading script from Scripts directory", e);
            return null;
        }
    }
    
    /**
     * Load script from assets (Debug fallback)
     */
    private String loadFromAssets(String patchId) {
        try {
            String assetPath = "patches/" + patchId + "/script.js";
            InputStream input = context.getAssets().open(assetPath);
            
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            
            String content = new String(buffer);
            Log.d(TAG, "Loaded script from assets: " + patchId);
            return content;
            
        } catch (Exception e) {
            Log.d(TAG, "Script not found in assets: " + patchId);
            return null;
        }
    }
    
    /**
     * Load script from secure storage (Production)
     */
    private String loadFromSecureStorage(String patchId) {
        try {
            // In production, scripts are embedded as encrypted resources
            String resourceName = "script_" + patchId.replace("-", "_");
            int resourceId = context.getResources().getIdentifier(
                resourceName, "raw", context.getPackageName());
            
            if (resourceId == 0) {
                Log.w(TAG, "Secure script resource not found: " + resourceName);
                return null;
            }
            
            InputStream input = context.getResources().openRawResource(resourceId);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();
            
            // Decrypt script content (implement your encryption)
            String content = decryptScript(buffer);
            Log.d(TAG, "Loaded script from secure storage: " + patchId);
            return content;
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading script from secure storage", e);
            return null;
        }
    }
    
    /**
     * Create Scripts directory structure for debug builds
     */
    public void initializeScriptsDirectory() {
        if (!isDebugBuild) {
            Log.d(TAG, "Scripts directory initialization skipped - not debug build");
            return;
        }
        
        try {
            File scriptsDir = new File(context.getExternalFilesDir(null), "Scripts");
            if (!scriptsDir.exists()) {
                scriptsDir.mkdirs();
                Log.d(TAG, "Created Scripts directory: " + scriptsDir.getAbsolutePath());
            }
            
            // Create subdirectories for each patch type
            String[] patchTypes = {"bypass-signkill", "bypass-ssl", "anti-detection", "analyzer"};
            for (String patchType : patchTypes) {
                File patchDir = new File(scriptsDir, patchType);
                if (!patchDir.exists()) {
                    patchDir.mkdirs();
                    
                    // Copy default script from assets if available
                    copyDefaultScript(patchType, patchDir);
                }
            }
            
            Log.d(TAG, "Scripts directory structure initialized");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Scripts directory", e);
        }
    }
    
    /**
     * Copy default script from assets to Scripts directory
     */
    private void copyDefaultScript(String patchId, File patchDir) {
        try {
            String assetPath = "patches/" + patchId + "/script.js";
            InputStream input = context.getAssets().open(assetPath);
            
            File scriptFile = new File(patchDir, "script.js");
            FileOutputStream output = new FileOutputStream(scriptFile);
            
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            
            input.close();
            output.close();
            
            Log.d(TAG, "Copied default script to Scripts directory: " + patchId);
            
        } catch (Exception e) {
            Log.d(TAG, "No default script to copy for: " + patchId);
        }
    }
    
    /**
     * Check if this is a debug build
     */
    private boolean isDebugBuild(Context context) {
        try {
            return (context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Decrypt script content (implement your encryption method)
     */
    private String decryptScript(byte[] encryptedData) {
        // For now, just return as string (implement proper decryption)
        // In production, you would decrypt the embedded script here
        return new String(encryptedData);
    }
    
    /**
     * Get Scripts directory path (Debug only)
     */
    public String getScriptsDirectoryPath() {
        if (!isDebugBuild) {
            return null;
        }
        
        File scriptsDir = new File(context.getExternalFilesDir(null), "Scripts");
        return scriptsDir.getAbsolutePath();
    }
    
    /**
     * Clear script cache
     */
    public void clearCache() {
        scriptCache.clear();
        Log.d(TAG, "Script cache cleared");
    }
    
    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugMode() {
        return isDebugBuild;
    }
}
