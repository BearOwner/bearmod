package com.bearmod.loader.server;

/**
 * FileInfo - Simple data class for file information from KeyAuth server
 * Used by OTA update system to track file versions and hashes
 */
public class FileInfo {
    private final String fileId;
    private final String hash;
    private final String version;

    public FileInfo(String fileId, String hash, String version) {
        this.fileId = fileId;
        this.hash = hash;
        this.version = version;
    }

    public String getFileId() {
        return fileId;
    }

    public String getHash() {
        return hash;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileId='" + fileId + '\'' +
                ", hash='" + hash + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}