package com.bearmod.patch;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-patch configuration manager
 * Manages which patches to apply to which target packages
 */
public class AutoPatchConfig {
    private static final String TAG = "AutoPatchConfig";
    private static final String PREFS_NAME = "auto_patch_config";
    
    // Patch configurations for different target packages
    private static final Map<String, List<String>> TARGET_PATCH_MAP = new HashMap<>();
    
    static {
        // PUBG Mobile Global patches
        TARGET_PATCH_MAP.put("com.tencent.ig", Arrays.asList(
            "bypass-signkill",
            "bypass-ssl", 
            "anti-detection",
            "analyzer"
        ));
        
        // PUBG Mobile Korea patches
        TARGET_PATCH_MAP.put("com.pubg.krmobile", Arrays.asList(
            "bypass-signkill",
            "bypass-ssl",
            "anti-detection"
        ));
        
        // PUBG Mobile Vietnam patches
        TARGET_PATCH_MAP.put("com.vng.pubgmobile", Arrays.asList(
            "bypass-signkill",
            "bypass-ssl",
            "anti-detection"
        ));
        
        // PUBG Mobile Taiwan patches
        TARGET_PATCH_MAP.put("com.rekoo.pubgm", Arrays.asList(
            "bypass-signkill",
            "bypass-ssl",
            "anti-detection"
        ));
        
        // PUBG Mobile India (BGMI) patches
        TARGET_PATCH_MAP.put("com.pubg.imobile", Arrays.asList(
            "bypass-signkill",
            "bypass-ssl",
            "anti-detection"
        ));
    }
    
    private final Context context;
    private final SharedPreferences prefs;
    
    public AutoPatchConfig(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get patches for target package
     */
    public List<String> getPatchesForTarget(String targetPackage) {
        List<String> patches = TARGET_PATCH_MAP.get(targetPackage);
        if (patches == null) {
            Log.w(TAG, "No patches configured for target: " + targetPackage);
            return new ArrayList<>();
        }
        
        // Filter based on user preferences
        List<String> enabledPatches = new ArrayList<>();
        for (String patch : patches) {
            if (isPatchEnabled(patch)) {
                enabledPatches.add(patch);
            }
        }
        
        Log.d(TAG, "Enabled patches for " + targetPackage + ": " + enabledPatches);
        return enabledPatches;
    }
    
    /**
     * Check if auto-patching is enabled
     */
    public boolean isAutoPatchEnabled() {
        return prefs.getBoolean("auto_patch_enabled", true);
    }
    
    /**
     * Enable/disable auto-patching
     */
    public void setAutoPatchEnabled(boolean enabled) {
        prefs.edit().putBoolean("auto_patch_enabled", enabled).apply();
        Log.d(TAG, "Auto-patch " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if specific patch is enabled
     */
    public boolean isPatchEnabled(String patchId) {
        return prefs.getBoolean("patch_" + patchId + "_enabled", true);
    }
    
    /**
     * Enable/disable specific patch
     */
    public void setPatchEnabled(String patchId, boolean enabled) {
        prefs.edit().putBoolean("patch_" + patchId + "_enabled", enabled).apply();
        Log.d(TAG, "Patch " + patchId + " " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Get auto-patch trigger mode
     */
    public AutoPatchTrigger getAutoPatchTrigger() {
        String trigger = prefs.getString("auto_patch_trigger", AutoPatchTrigger.ON_SERVICE_START.name());
        try {
            return AutoPatchTrigger.valueOf(trigger);
        } catch (IllegalArgumentException e) {
            return AutoPatchTrigger.ON_SERVICE_START;
        }
    }
    
    /**
     * Set auto-patch trigger mode
     */
    public void setAutoPatchTrigger(AutoPatchTrigger trigger) {
        prefs.edit().putString("auto_patch_trigger", trigger.name()).apply();
        Log.d(TAG, "Auto-patch trigger set to: " + trigger);
    }
    
    /**
     * Get all supported target packages
     */
    public List<String> getSupportedTargets() {
        return new ArrayList<>(TARGET_PATCH_MAP.keySet());
    }
    
    /**
     * Check if target package is supported
     */
    public boolean isTargetSupported(String targetPackage) {
        return TARGET_PATCH_MAP.containsKey(targetPackage);
    }
    
    /**
     * Auto-patch trigger modes
     */
    public enum AutoPatchTrigger {
        ON_TARGET_SELECTION,    // Apply patches when target is selected
        ON_SERVICE_START,       // Apply patches when mod service starts
        ON_APP_LAUNCH,         // Apply patches when target app is launched
        MANUAL_ONLY            // No auto-patching, manual only
    }
    
    /**
     * Reset all patch configurations to defaults
     */
    public void resetToDefaults() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Auto-patch configuration reset to defaults");
    }
}
