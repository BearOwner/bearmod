package com.bearmod.security;

import android.content.ComponentName;
import android.content.Intent;
import com.google.gson.Gson;
import java.util.Map;

/**
 * Secure Intent manager for encrypted Plugin-to-BearMod communication
 * This class will be obfuscated to prevent reverse engineering
 */
public class SecureIntentManager {
    private static final Gson gson = new Gson();
    private static final String SECURITY_VERSION = "1.0";
    private static final long INTENT_TIMEOUT_MS = 30000; // 30 seconds
    
    /**
     * Create secure Intent with encrypted payload
     */
    public static Intent createSecureIntent(String targetComponent, Map<String, String> data) {
        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString(targetComponent));
        
        try {
            // Add timestamp for replay attack prevention
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            data.put("security_version", SECURITY_VERSION);
            
            // Encrypt the entire payload
            String jsonData = gson.toJson(data);
            String encryptedPayload = AESUtil.encryptForBearMod(jsonData);
            
            // Add encrypted payload to Intent
            intent.putExtra("secure_payload", encryptedPayload);
            intent.putExtra("security_version", SECURITY_VERSION);
            
            return intent;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create secure Intent", e);
        }
    }
    
    /**
     * Extract secure data from encrypted Intent
     */
    public static Map<String, String> extractSecureData(Intent intent) {
        try {
            String encryptedPayload = intent.getStringExtra("secure_payload");
            if (encryptedPayload == null) {
                throw new SecurityException("No secure payload found in Intent");
            }
            
            String securityVersion = intent.getStringExtra("security_version");
            if (!SECURITY_VERSION.equals(securityVersion)) {
                throw new SecurityException("Incompatible security version: " + securityVersion);
            }
            
            String jsonData = AESUtil.decryptFromPlugin(encryptedPayload);
            
            @SuppressWarnings("unchecked")
            Map<String, String> data = gson.fromJson(jsonData, Map.class);
            
            // Verify timestamp (prevent replay attacks)
            String timestampStr = data.get("timestamp");
            if (timestampStr != null) {
                long timestamp = Long.parseLong(timestampStr);
                long currentTime = System.currentTimeMillis();
                if (currentTime - timestamp > INTENT_TIMEOUT_MS) {
                    throw new SecurityException("Intent timestamp too old: " + (currentTime - timestamp) + "ms");
                }
            }
            
            return data;
            
        } catch (Exception e) {
            throw new SecurityException("Failed to extract secure data from Intent", e);
        }
    }
    
    /**
     * Validate Intent security
     */
    public static boolean isSecureIntent(Intent intent) {
        try {
            String encryptedPayload = intent.getStringExtra("secure_payload");
            String securityVersion = intent.getStringExtra("security_version");
            
            return encryptedPayload != null && 
                   SECURITY_VERSION.equals(securityVersion);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Create error Intent for security failures
     */
    public static Intent createErrorIntent(String error) {
        Intent intent = new Intent();
        intent.putExtra("security_error", error);
        intent.putExtra("security_version", SECURITY_VERSION);
        return intent;
    }
}
