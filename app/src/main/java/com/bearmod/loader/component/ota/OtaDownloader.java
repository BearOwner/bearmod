package com.bearmod.loader.component.ota;

import com.bearmod.loader.utilities.Logx;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * OtaDownloader - Handles memory-only downloads and extraction for OTA updates
 *
 * Consolidates functionality from:
 * - DownloadZip.java / Downtwo.java (HTTP download logic)
 * - Zip.java (unzip functionality)
 *
 * Features:
 * - Downloads files directly into memory (no disk writes)
 * - Extracts ZIP files in RAM using ZipInputStream
 * - Auto-clears memory after use (ephemeral)
 * - Thread-safe for concurrent downloads
 * - Comprehensive error handling for network failures and invalid ZIPs
 *
 * Migrated from DownloadZip.java, Downtwo.java, and Zip.java to com.bearmod.loader.component.ota
 */
public class OtaDownloader {
    private static final String TAG = "OtaDownloader";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer for efficient I/O

    /**
     * Process downloaded content from InputStream into memory
     * Handles both raw files and ZIP archives
     */
    public OtaManager.OtaContent processDownloadedContent(InputStream inputStream) throws IOException {
        try {
            Logx.d("Processing downloaded content in memory");

            // Read all data into memory buffer
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(tempBuffer)) != -1) {
                buffer.write(tempBuffer, 0, bytesRead);
            }

            byte[] rawData = buffer.toByteArray();
            String hash = calculateHash(rawData);

            Logx.d("Downloaded " + rawData.length + " bytes, hash: " + hash);

            // Try to extract if it's a ZIP file
            if (isZipFile(rawData)) {
                Logx.d("Detected ZIP file, extracting in memory");
                return extractZipInMemory(rawData, hash);
            } else {
                // Raw file content
                Logx.d("Raw file content detected");
                return new OtaManager.OtaContent(rawData, hash, rawData.length);
            }

        } finally {
            // Ensure input stream is closed
            try {
                inputStream.close();
            } catch (IOException e) {
                Logx.w("Error closing input stream", e);
            }
        }
    }

    /**
     * Extract ZIP file content directly in memory
     */
    private OtaManager.OtaContent extractZipInMemory(byte[] zipData, String zipHash) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new java.io.ByteArrayInputStream(zipData));
        ByteArrayOutputStream extractedBuffer = new ByteArrayOutputStream();
        ZipEntry entry;
        boolean foundContent = false;

        try {
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && isExecutableFile(entry.getName())) {
                    Logx.d("Extracting file: " + entry.getName());

                    // Extract file content to memory
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        extractedBuffer.write(buffer, 0, bytesRead);
                    }

                    foundContent = true;
                    break; // Take first executable file found
                }
                zipInputStream.closeEntry();
            }

            if (!foundContent) {
                throw new IOException("No executable content found in ZIP archive");
            }

            byte[] extractedData = extractedBuffer.toByteArray();
            String extractedHash = calculateHash(extractedData);

            Logx.d("Extracted " + extractedData.length + " bytes from ZIP, hash: " + extractedHash);

            return new OtaManager.OtaContent(extractedData, extractedHash, extractedData.length);

        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                Logx.w("Error closing ZIP input stream", e);
            }
        }
    }

    /**
     * Check if the given data represents a ZIP file
     */
    private boolean isZipFile(byte[] data) {
        if (data.length < 4) return false;

        // ZIP files start with "PK\x03\x04"
        return data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04;
    }

    /**
     * Check if a file name represents an executable file (for OTA purposes)
     */
    private boolean isExecutableFile(String fileName) {
        if (fileName == null) return false;

        String lowerName = fileName.toLowerCase();
        // Common executable extensions for Android
        return lowerName.endsWith(".so") ||
               lowerName.endsWith(".dex") ||
               lowerName.endsWith(".jar") ||
               lowerName.endsWith(".apk") ||
               lowerName.endsWith(".bin");
    }

    /**
     * Calculate SHA-256 hash of byte array
     */
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Logx.e("SHA-256 algorithm not available", e);
            return "ERROR";
        }
    }

    /**
     * Download file from URL into memory (legacy compatibility method)
     * Note: This method is kept for compatibility but should be replaced with Server.downloadFile()
     */
    @Deprecated
    public byte[] downloadToMemory(String url) throws IOException {
        Logx.w("Using deprecated downloadToMemory method - consider using Server.downloadFile() instead");

        java.net.URL fileUrl = new java.net.URL(url);
        try (InputStream inputStream = fileUrl.openStream()) {
            return processDownloadedContent(inputStream).getData();
        }
    }

    /**
     * Extract ZIP from InputStream to memory (legacy compatibility method)
     * Note: This method is kept for compatibility but should be replaced with processDownloadedContent()
     */
    @Deprecated
    public byte[] extractToMemory(InputStream zipInputStream) throws IOException {
        Logx.w("Using deprecated extractToMemory method - consider using processDownloadedContent() instead");

        ZipInputStream zipStream = new ZipInputStream(zipInputStream);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ZipEntry entry;

        try {
            while ((entry = zipStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] tempBuffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = zipStream.read(tempBuffer)) != -1) {
                        buffer.write(tempBuffer, 0, bytesRead);
                    }
                    break; // Take first file
                }
                zipStream.closeEntry();
            }
        } finally {
            zipStream.close();
        }

        return buffer.toByteArray();
    }
}