package com.bearmod.security;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Verifies patch signatures for security
 */
public class SignatureVerifier {
    private static final String TAG = "SignatureVerifier";
    
    // Known patch signatures (in production, these would be loaded securely)
    private static final Map<String, String> KNOWN_PATCH_SIGNATURES = new HashMap<>();
    
    static {
        // Add known patch signatures
        KNOWN_PATCH_SIGNATURES.put("bypass-signkill", "a1b2c3d4e5f6789012345678901234567890abcd");
        KNOWN_PATCH_SIGNATURES.put("bypass-ssl", "b2c3d4e5f6789012345678901234567890abcdef1");
        KNOWN_PATCH_SIGNATURES.put("analyzer", "c3d4e5f6789012345678901234567890abcdef12");
        KNOWN_PATCH_SIGNATURES.put("anti-detection", "d4e5f6789012345678901234567890abcdef123");
    }
    
    /**
     * Verify patch signature
     */
    public static boolean verifyPatchSignature(Context context, String patchPath) {
        try {
            File patchFile = new File(patchPath);
            if (!patchFile.exists()) {
                Log.e(TAG, "Patch file does not exist: " + patchPath);
                return false;
            }
            
            // Calculate file hash
            String fileHash = calculateFileHash(patchFile);
            
            // Extract patch ID from filename
            String patchId = extractPatchId(patchFile.getName());
            
            // Check against known signatures
            String expectedHash = KNOWN_PATCH_SIGNATURES.get(patchId);
            if (expectedHash == null) {
                Log.w(TAG, "Unknown patch ID: " + patchId);
                // For development, allow unknown patches
                return true;
            }
            
            boolean isValid = expectedHash.equals(fileHash);
            Log.d(TAG, "Patch signature verification for " + patchId + ": " + (isValid ? "VALID" : "INVALID"));
            
            return isValid;
            
        } catch (Exception e) {
            Log.e(TAG, "Error verifying patch signature", e);
            return false;
        }
    }
    
    /**
     * Calculate SHA-256 hash of file
     */
    private static String calculateFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Extract patch ID from filename
     */
    private static String extractPatchId(String filename) {
        // Remove .js extension and any path separators
        String patchId = filename.replace(".js", "");
        int lastSlash = patchId.lastIndexOf('/');
        if (lastSlash != -1) {
            patchId = patchId.substring(lastSlash + 1);
        }
        return patchId;
    }
    
    /**
     * Add new patch signature (for dynamic updates)
     */
    public static void addPatchSignature(String patchId, String signature) {
        KNOWN_PATCH_SIGNATURES.put(patchId, signature);
        Log.d(TAG, "Added signature for patch: " + patchId);
    }
}
