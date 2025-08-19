package com.bearmod.patch.model;

/**
 * Result of a patch operation
 */
public class PatchResult {
    private final boolean success;
    private final String message;
    private final String patchId;
    private final long timestamp;
    private final String targetPackage;
    
    public PatchResult(boolean success, String message, String patchId) {
        this.success = success;
        this.message = message;
        this.patchId = patchId;
        this.timestamp = System.currentTimeMillis();
        this.targetPackage = null;
    }
    
    public PatchResult(boolean success, String message, String patchId, String targetPackage) {
        this.success = success;
        this.message = message;
        this.patchId = patchId;
        this.targetPackage = targetPackage;
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getPatchId() {
        return patchId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getTargetPackage() {
        return targetPackage;
    }
    
    @Override
    public String toString() {
        return "PatchResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", patchId='" + patchId + '\'' +
                ", targetPackage='" + targetPackage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
