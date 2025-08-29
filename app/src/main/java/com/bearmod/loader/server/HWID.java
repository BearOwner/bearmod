package com.bearmod.loader.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.bearmod.loader.utilities.Logx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;

/**
 * HWID - Hardware ID generation and validation utility
 *
 * Centralized hardware fingerprinting for device identification and license binding.
 * Provides secure, stable HWID generation with caching and validation capabilities.
 *
 * Migrated from SimpleLicenseVerifier.java to com.bearmod.loader.server/
 */
public class HWID {
    private static final String TAG = "HWID";

    // Obfuscated preference keys using StrObf
    private static final int OBF_SALT = 413;
    private static String PREFS_DEVICE() { return com.bearmod.loader.utilities.StrObf.d(new int[]{498,392}, OBF_SALT); } // "o4"
    private static String K_CACHED_HWID() { return com.bearmod.loader.utilities.StrObf.d(new int[]{495,394}, OBF_SALT); } // "d2"
    private static String K_HWID_LAST_RESET() { return com.bearmod.loader.utilities.StrObf.d(new int[]{495,393}, OBF_SALT); } // "d1"

    // Security constants
    private static final int MIN_HWID_LENGTH = 20;
    private static final long HWID_RESET_COOLDOWN_MS = 72L * 60 * 60 * 1000; // 72 hours

    /**
     * Generate or retrieve cached hardware ID
     * Uses device fingerprinting to create stable, unique identifier
     */
    public static String getHWID() {
        try {
            // Try cache first
            String cached = getCachedHWID();
            if (cached != null && !cached.isEmpty()) {
                Logx.d("Using cached HWID: " + cached.substring(0, 8) + "...");
                return cached;
            }

            // Generate new HWID
            String hwid = generateHWID();
            if (hwid != null && !hwid.isEmpty()) {
                cacheHWID(hwid);
                Logx.d("Generated new HWID: " + hwid.substring(0, 8) + "...");
                return hwid;
            }

            Logx.e("Failed to generate HWID");
            return null;

        } catch (Exception e) {
            Logx.e("Error getting HWID", e);
            return null;
        }
    }

    /**
     * Generate hardware ID from device properties
     */
    private static String generateHWID() {
        try {
            // Collect device properties for fingerprinting
            StringBuilder fingerprint = new StringBuilder();

            // Add Android ID (fallback if available)
            String androidId = getAndroidId();
            if (androidId != null) {
                fingerprint.append(androidId);
            }

            // Add device manufacturer and model
            fingerprint.append(android.os.Build.MANUFACTURER);
            fingerprint.append(android.os.Build.MODEL);
            fingerprint.append(android.os.Build.BRAND);

            // Add serial number if available
            String serial = getDeviceSerial();
            if (serial != null) {
                fingerprint.append(serial);
            }

            // Add WiFi MAC address
            String wifiMac = getWifiMacAddress();
            if (wifiMac != null) {
                fingerprint.append(wifiMac);
            }

            // Generate MD5 hash of fingerprint
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(fingerprint.toString().getBytes());
            StringBuilder hwid = new StringBuilder();

            for (byte b : hashBytes) {
                hwid.append(String.format("%02x", b));
            }

            return hwid.toString();

        } catch (Exception e) {
            Logx.e("Error generating HWID", e);
            return null;
        }
    }

    /**
     * Get Android ID from system
     */
    private static String getAndroidId() {
        try {
            // This method requires a Context, so we'll return null for now
            // It should be called from a context-aware method
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get device serial number
     */
    private static String getDeviceSerial() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return android.os.Build.getSerial();
            } else {
                return android.os.Build.SERIAL;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get WiFi MAC address
     */
    private static String getWifiMacAddress() {
        try {
            // This method requires a Context, so we'll return null for now
            // It should be called from a context-aware method
            return null;
        } catch (Exception e) {
            // MAC address access may be restricted on newer Android versions
        }
        return null;
    }

    /**
     * Validate HWID format and security requirements
     */
    public static boolean isValidHWID(String hwid) {
        if (hwid == null || hwid.trim().isEmpty()) {
            return false;
        }

        String trimmed = hwid.trim();

        // Check minimum length
        if (trimmed.length() < MIN_HWID_LENGTH) {
            return false;
        }

        // Check if it's hex format (our MD5 hash)
        if (!trimmed.matches("[A-Fa-f0-9]{32,}")) {
            Logx.w("HWID format validation failed - not hex format");
            return false;
        }

        return true;
    }

    /**
     * Cache HWID in multiple persistent locations
     * Note: This simplified version skips caching due to static context limitations
     * In a full implementation, this would require a Context parameter
     */
    private static void cacheHWID(String hwid) {
        // Simplified implementation - caching disabled for static context
        Logx.d("HWID caching disabled in static context: " + hwid.substring(0, 8) + "...");
    }

    /**
     * Retrieve cached HWID from persistent storage
     * Note: This simplified version returns null due to static context limitations
     * In a full implementation, this would require a Context parameter
     */
    private static String getCachedHWID() {
        // Simplified implementation - caching disabled for static context
        Logx.d("HWID cache retrieval disabled in static context");
        return null;
    }

    /**
     * Clear cached HWID and force regeneration
     * Note: This simplified version skips reset due to static context limitations
     * In a full implementation, this would require a Context parameter
     */
    public static void resetHWID() {
        // Simplified implementation - reset disabled for static context
        Logx.d("HWID reset disabled in static context");
    }

}