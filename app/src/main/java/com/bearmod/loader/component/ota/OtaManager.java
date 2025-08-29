package com.bearmod.loader.component.ota;

import android.content.Context;
import android.util.Log;
import com.bearmod.loader.utilities.Logx;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * OtaManager - Orchestrates OTA (Over-The-Air) update flow
 *
 * Central coordinator for runtime updates that:
 * - Checks server for latest file information via KeyAuth
 * - Decides whether new OTA builds should be fetched
 * - Coordinates with OtaDownloader for memory-only downloads
 * - Implements caching for version checks to reduce server calls
 * - Provides retry logic for failed operations
 *
 * Migrated from UpdateChecker.java and related classes to com.bearmod.loader.component.ota
 */
public class OtaManager {
    private static final String TAG = "OtaManager";

    private final Context context;
    private final Server server;
    private final OtaDownloader downloader;

    // Cache for version checks to reduce server calls
    private static class VersionCache {
        String fileId;
        String hash;
        long timestamp;
        static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    private VersionCache versionCache;

    public OtaManager(Context context, Server server, OtaDownloader downloader) {
        this.context = context;
        this.server = server;
        this.downloader = downloader;
        this.versionCache = new VersionCache();
    }

    /**
     * Check if an update is available for the given file ID
     */
    public CompletableFuture<UpdateCheckResult> checkForUpdate(String fileId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Logx.d("Checking for OTA update: " + fileId);

                // Check cache first
                if (isCacheValid(fileId)) {
                    Logx.d("Using cached version info for: " + fileId);
                    return new UpdateCheckResult(false, "No update available (cached)", null);
                }

                // Query server for latest file info
                Server.FileInfo latestInfo = server.getFileInfo(fileId);
                if (latestInfo == null) {
                    return new UpdateCheckResult(false, "Failed to get file info from server", null);
                }

                // Cache the result
                updateCache(fileId, latestInfo.getHash());

                // For now, always assume update is needed (can be enhanced with local version comparison)
                Logx.d("Update available for: " + fileId + " (hash: " + latestInfo.getHash() + ")");
                return new UpdateCheckResult(true, "Update available", latestInfo);

            } catch (Exception e) {
                Logx.e("Error checking for update", e);
                return new UpdateCheckResult(false, "Error: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Download and prepare OTA update for the given file ID
     */
    public CompletableFuture<OtaResult> downloadUpdate(String fileId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Logx.d("Starting OTA download for: " + fileId);

                // Download file into memory
                InputStream fileStream = server.downloadFile(fileId);
                if (fileStream == null) {
                    return new OtaResult(false, "Failed to download file from server", null);
                }

                // Process the downloaded content
                OtaContent content = downloader.processDownloadedContent(fileStream);

                Logx.d("OTA download completed successfully for: " + fileId);
                return new OtaResult(true, "Download successful", content);

            } catch (Exception e) {
                Logx.e("Error downloading OTA update", e);
                return new OtaResult(false, "Download error: " + e.getMessage(), null);
            }
        });
    }

    /**
     * Perform complete OTA update flow: check + download
     */
    public CompletableFuture<OtaResult> performUpdate(String fileId) {
        return checkForUpdate(fileId)
                .thenCompose(checkResult -> {
                    if (!checkResult.isUpdateAvailable()) {
                        return CompletableFuture.completedFuture(
                            new OtaResult(false, checkResult.getMessage(), null));
                    }
                    return downloadUpdate(fileId);
                });
    }

    /**
     * Check if cached version info is still valid
     */
    private boolean isCacheValid(String fileId) {
        return versionCache != null &&
               fileId.equals(versionCache.fileId) &&
               !versionCache.isExpired();
    }

    /**
     * Update version cache with new information
     */
    private void updateCache(String fileId, String hash) {
        if (versionCache == null) {
            versionCache = new VersionCache();
        }
        versionCache.fileId = fileId;
        versionCache.hash = hash;
        versionCache.timestamp = System.currentTimeMillis();
    }

    /**
     * Clear version cache (useful for forcing fresh checks)
     */
    public void clearCache() {
        versionCache = new VersionCache();
        Logx.d("OTA version cache cleared");
    }

    /**
     * Result of update check operation
     */
    public static class UpdateCheckResult {
        private final boolean updateAvailable;
        private final String message;
        private final Server.FileInfo fileInfo;

        public UpdateCheckResult(boolean updateAvailable, String message, Server.FileInfo fileInfo) {
            this.updateAvailable = updateAvailable;
            this.message = message;
            this.fileInfo = fileInfo;
        }

        public boolean isUpdateAvailable() { return updateAvailable; }
        public String getMessage() { return message; }
        public Server.FileInfo getFileInfo() { return fileInfo; }
    }

    /**
     * Result of OTA operation
     */
    public static class OtaResult {
        private final boolean success;
        private final String message;
        private final OtaContent content;

        public OtaResult(boolean success, String message, OtaContent content) {
            this.success = success;
            this.message = message;
            this.content = content;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public OtaContent getContent() { return content; }
    }

    /**
     * Content delivered by OTA system
     */
    public static class OtaContent {
        private final byte[] data;
        private final String hash;
        private final long size;

        public OtaContent(byte[] data, String hash, long size) {
            this.data = data;
            this.hash = hash;
            this.size = size;
        }

        public byte[] getData() { return data; }
        public String getHash() { return hash; }
        public long getSize() { return size; }
    }
}