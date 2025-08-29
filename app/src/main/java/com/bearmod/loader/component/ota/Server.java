package com.bearmod.loader.component.ota;

import android.content.Context;
import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.loader.utilities.Logx;
import java.io.InputStream;

/**
 * Server - Thin KeyAuth wrapper providing clean OTA interface
 *
 * Slimmed-down bridge to SimpleLicenseVerifier that exposes only:
 * - getFileInfo(fileId) → returns hash/version
 * - downloadFile(fileId) → returns InputStream/byte[]
 *
 * All other logic (auth/session init) remains in SimpleLicenseVerifier.
 * This class provides dependency injection for better testability.
 *
 * Migrated from SimpleLicenseVerifier.java to com.bearmod.loader.component.ota
 */
public class Server {
    private static final String TAG = "Server";

    private final Context context;
    private final SimpleLicenseVerifier licenseVerifier;

    public Server(Context context) {
        this.context = context;
        this.licenseVerifier = new SimpleLicenseVerifier(); // Could be injected for testing
    }

    /**
     * Get file information from KeyAuth server
     * @param fileId The file ID to query
     * @return FileInfo containing hash/version, or null if failed
     */
    public FileInfo getFileInfo(String fileId) {
        try {
            Logx.d("Querying server for file info: " + fileId);

            // This would integrate with SimpleLicenseVerifier's file info methods
            // For now, return a placeholder - would need to be implemented based on actual KeyAuth API
            String hash = getFileHash(fileId);
            String version = getFileVersion(fileId);

            if (hash != null && version != null) {
                return new FileInfo(fileId, hash, version);
            }

            Logx.w("Failed to get file info for: " + fileId);
            return null;

        } catch (Exception e) {
            Logx.e("Error getting file info from server", e);
            return null;
        }
    }

    /**
     * Download file from KeyAuth server
     * @param fileId The file ID to download
     * @return InputStream of the file content, or null if failed
     */
    public InputStream downloadFile(String fileId) {
        try {
            Logx.d("Downloading file from server: " + fileId);

            // This would integrate with SimpleLicenseVerifier's download methods
            // For now, return null - would need to be implemented based on actual KeyAuth API
            // Example: return licenseVerifier.downloadFileStream(fileId);

            Logx.w("Download not yet implemented for: " + fileId);
            return null;

        } catch (Exception e) {
            Logx.e("Error downloading file from server", e);
            return null;
        }
    }

    /**
     * Get file hash from KeyAuth (placeholder - implement based on actual API)
     */
    private String getFileHash(String fileId) {
        // TODO: Implement based on actual KeyAuth API
        // Example: return licenseVerifier.getFileHash(fileId);
        return "placeholder_hash_" + fileId;
    }

    /**
     * Get file version from KeyAuth (placeholder - implement based on actual API)
     */
    private String getFileVersion(String fileId) {
        // TODO: Implement based on actual KeyAuth API
        // Example: return licenseVerifier.getFileVersion(fileId);
        return "1.0.0";
    }

    /**
     * File information returned by server queries
     */
    public static class FileInfo {
        private final String fileId;
        private final String hash;
        private final String version;

        public FileInfo(String fileId, String hash, String version) {
            this.fileId = fileId;
            this.hash = hash;
            this.version = version;
        }

        public String getFileId() { return fileId; }
        public String getHash() { return hash; }
        public String getVersion() { return version; }

        @Override
        public String toString() {
            return "FileInfo{" +
                   "fileId='" + fileId + '\'' +
                   ", hash='" + hash + '\'' +
                   ", version='" + version + '\'' +
                   '}';
        }
    }
}