package com.bearmod.loader.floating;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingConfigManager - Manages configuration loading, saving, and persistence
 * Handles all configuration operations for the floating overlay system
 */
public class FloatingConfigManager {

    private static final String TAG = "FloatingConfigManager";

    private final Context context;
    private final Map<String, String> configMap;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor configEditor;

    // Configuration file
    private static final String CONFIG_FILE_NAME = "NRG_SaveFile.cfg";
    private static final String PREFS_NAME = "floating_config";

    // Default configuration values
    private static final Map<String, String> DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new HashMap<>();
        // ESP defaults
        DEFAULT_CONFIG.put("ESP_LINE", "0");
        DEFAULT_CONFIG.put("ESP_BONE", "0");
        DEFAULT_CONFIG.put("ESP_INFO", "0");
        DEFAULT_CONFIG.put("ESP_WEAPON", "0");
        DEFAULT_CONFIG.put("ESP_WARNING", "0");
        DEFAULT_CONFIG.put("ESP_ALERT", "0");
        DEFAULT_CONFIG.put("ESP_RADAR", "0");
        DEFAULT_CONFIG.put("ESP_IGNOREBOTS", "0");
        DEFAULT_CONFIG.put("RADAR_SIZE", "60");

        // AIM defaults
        DEFAULT_CONFIG.put("NRG_AIMBOT", "0");
        DEFAULT_CONFIG.put("AIM_MOD1", "1");
        DEFAULT_CONFIG.put("AIM_MOD2", "0");
        DEFAULT_CONFIG.put("AIM_MOD3", "0");
        DEFAULT_CONFIG.put("AIM_VISCHECK", "1");
        DEFAULT_CONFIG.put("AIM_KNOCKED", "0");
        DEFAULT_CONFIG.put("AIM_IGNOREBOTS", "1");
        DEFAULT_CONFIG.put("AIM_DISTANCE", "1");
        DEFAULT_CONFIG.put("AIM_SIZE", "1");
        DEFAULT_CONFIG.put("RECOIL_SIZE", "1");

        // Skin defaults
        DEFAULT_CONFIG.put("SKIN_ENABLE", "0");
        DEFAULT_CONFIG.put("SKIN_BOXENABLE", "0");
        DEFAULT_CONFIG.put("SKIN_XSUIT", "0");
        DEFAULT_CONFIG.put("SKIN_SET", "0");
        DEFAULT_CONFIG.put("SKIN_BACKPACK", "0");
        DEFAULT_CONFIG.put("SKIN_HELMET", "0");
        DEFAULT_CONFIG.put("SKIN_M416", "0");
        DEFAULT_CONFIG.put("SKIN_AKM", "0");
        DEFAULT_CONFIG.put("SKIN_SCARL", "0");
        DEFAULT_CONFIG.put("SKIN_M762", "0");
        DEFAULT_CONFIG.put("SKIN_M16A4", "0");
        DEFAULT_CONFIG.put("SKIN_GROZAR", "0");
        DEFAULT_CONFIG.put("SKIN_AUG", "0");
        DEFAULT_CONFIG.put("SKIN_ACE32", "0");
        DEFAULT_CONFIG.put("SKIN_M249", "0");
        DEFAULT_CONFIG.put("SKIN_DP28", "0");
        DEFAULT_CONFIG.put("SKIN_MG3", "0");
        DEFAULT_CONFIG.put("SKIN_P90", "0");
        DEFAULT_CONFIG.put("SKIN_UZI", "0");
        DEFAULT_CONFIG.put("SKIN_UMP45", "0");
        DEFAULT_CONFIG.put("SKIN_VECTOR", "0");
        DEFAULT_CONFIG.put("SKIN_THOMPSON", "0");
        DEFAULT_CONFIG.put("SKIN_M24", "0");
        DEFAULT_CONFIG.put("SKIN_KAR98K", "0");
        DEFAULT_CONFIG.put("SKIN_AWM", "0");
        DEFAULT_CONFIG.put("SKIN_AMR", "0");
        DEFAULT_CONFIG.put("SKIN_MK14", "0");
        DEFAULT_CONFIG.put("SKIN_DACIA", "0");
        DEFAULT_CONFIG.put("SKIN_COUPERP", "0");
        DEFAULT_CONFIG.put("SKIN_UAZ", "0");
        DEFAULT_CONFIG.put("SKIN_MOTO", "0");

        // UI defaults
        DEFAULT_CONFIG.put("SETTING_MENU", "0"); // 0 = English, 1 = Chinese
        DEFAULT_CONFIG.put("RECORDER_HIDE", "0");
        DEFAULT_CONFIG.put("MEMORY_WIDEVIEW", "0");
    }

    public FloatingConfigManager(Context context) {
        this.context = context;
        this.configMap = new HashMap<>();
        this.sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.configEditor = sharedPrefs.edit();

        Logx.d("FloatingConfigManager initialized");
    }

    /**
     * Load configuration from file
     */
    public void loadConfiguration() {
        try {
            Logx.d("Loading configuration from file...");

            // Clear existing config
            configMap.clear();

            // Load from file
            File configFile = new File(context.getFilesDir(), CONFIG_FILE_NAME);
            if (configFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" = ");
                    if (parts.length == 2) {
                        configMap.put(parts[0], parts[1]);
                    }
                }
                reader.close();
                Logx.d("Configuration loaded from file: " + configMap.size() + " entries");
            } else {
                Logx.d("Configuration file not found, using defaults");
                loadDefaultConfiguration();
            }

            // Ensure all default values are present
            ensureDefaultValues();

        } catch (Exception e) {
            Logx.e("Error loading configuration: " + e.getMessage(), e);
            loadDefaultConfiguration();
        }
    }

    /**
     * Save current configuration to file
     */
    public void saveConfiguration() {
        try {
            Logx.d("Saving configuration to file...");

            File configFile = new File(context.getFilesDir(), CONFIG_FILE_NAME);
            PrintWriter writer = new PrintWriter(new FileOutputStream(configFile), true);

            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                writer.println(entry.getKey() + " = " + entry.getValue());
            }

            writer.close();
            Logx.d("Configuration saved successfully");

        } catch (Exception e) {
            Logx.e("Error saving configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Update a configuration value
     */
    public void updateConfiguration(String key, Object value) {
        try {
            String stringValue = value != null ? value.toString() : "0";
            configMap.put(key, stringValue);

            // Also save to SharedPreferences for quick access
            configEditor.putString(key, stringValue);
            configEditor.apply();

            Logx.d("Configuration updated: " + key + " = " + stringValue);

        } catch (Exception e) {
            Logx.e("Error updating configuration '" + key + "': " + e.getMessage(), e);
        }
    }

    /**
     * Get a configuration value
     */
    public String getConfiguration(String key) {
        return configMap.get(key);
    }

    /**
     * Get a configuration value as integer
     */
    public int getConfigurationAsInt(String key) {
        try {
            String value = configMap.get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            Logx.e("Error parsing configuration '" + key + "' as int: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get a configuration value as boolean
     */
    public boolean getConfigurationAsBoolean(String key) {
        String value = configMap.get(key);
        return value != null && Integer.parseInt(value) == 1;
    }

    /**
     * Check if a configuration key exists
     */
    public boolean hasConfiguration(String key) {
        return configMap.containsKey(key);
    }

    /**
     * Remove a configuration key
     */
    public void removeConfiguration(String key) {
        configMap.remove(key);
        configEditor.remove(key);
        configEditor.apply();
        Logx.d("Configuration removed: " + key);
    }

    /**
     * Get all configuration entries
     */
    public Map<String, String> getAllConfigurations() {
        return new HashMap<>(configMap);
    }

    /**
     * Clear all configuration
     */
    public void clearConfiguration() {
        configMap.clear();
        configEditor.clear();
        configEditor.apply();
        Logx.d("Configuration cleared");
    }

    /**
     * Initialize default configuration values
     */
    public void initializeDefaults() {
        try {
            Logx.d("Initializing default configuration values...");

            // AIM defaults
            updateConfiguration("AIM::TRIGGER1", 1);
            updateConfiguration("AIM::TARGET1", 1);
            updateConfiguration("ESP::BOXTYPE1", 1);
            updateConfiguration("AIM_MOD1", 1);
            updateConfiguration("SMOOT::HNESS1", 1);
            updateConfiguration("RADAR::SIZE", 60);

            saveConfiguration();
            Logx.d("Default configuration values initialized");

        } catch (Exception e) {
            Logx.e("Error initializing defaults: " + e.getMessage(), e);
        }
    }

    /**
     * Export configuration to string
     */
    public String exportConfiguration() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Floating Configuration Export\n");
        sb.append("# Generated at: ").append(System.currentTimeMillis()).append("\n\n");

        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Import configuration from string
     */
    public void importConfiguration(String configData) {
        try {
            Logx.d("Importing configuration from string...");

            String[] lines = configData.split("\n");
            int importedCount = 0;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(" = ");
                if (parts.length == 2) {
                    updateConfiguration(parts[0], parts[1]);
                    importedCount++;
                }
            }

            saveConfiguration();
            Logx.d("Configuration imported successfully: " + importedCount + " entries");

        } catch (Exception e) {
            Logx.e("Error importing configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingConfigManager resources...");

            // Save any pending changes
            saveConfiguration();

            // Clear references
            configMap.clear();
            sharedPrefs = null;
            configEditor = null;

            Logx.d("FloatingConfigManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingConfigManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void loadDefaultConfiguration() {
        configMap.putAll(DEFAULT_CONFIG);
        Logx.d("Default configuration loaded: " + configMap.size() + " entries");
    }

    private void ensureDefaultValues() {
        for (Map.Entry<String, String> entry : DEFAULT_CONFIG.entrySet()) {
            if (!configMap.containsKey(entry.getKey())) {
                configMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}