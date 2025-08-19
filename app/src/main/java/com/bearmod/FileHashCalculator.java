package com.bearmod;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.pm.ApplicationInfo; // For getting APK path if needed
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.logging.Level;
import java.util.logging.Logger; // Using java.util.logging. For Android, consider android.util.Log
import java.io.FileNotFoundException; // Using java.util.logging. For Android, consider android.util.Log

/**
 * FileHashCalculator provides utility methods to calculate MD5, SHA-1, SHA-256
 * hashes and CRC32 checksum directly from an InputStream. This allows calculating
 * hashes for files residing in the app's 'assets' folder without needing to copy them
 * to internal storage first.
 *
 * IMPORTANT: Hashes are DERIVED from file content; they cannot be "edited".
 * This class calculates hashes based on the actual file data.
 */
public class FileHashCalculator {

    private static final Logger LOGGER = Logger.getLogger(FileHashCalculator.class.getName());

    static {
        LOGGER.setLevel(Level.ALL); // Set logger level to ALL for debugging
    }

    /**
     * Calculates the hash of data from an InputStream using a specified hashing algorithm.
     * This is the core method for calculating hashes from any stream (e.g., assets).
     *
     * @param is The InputStream from which to read data for hashing. The stream will be closed by this method.
     * @param algorithm The hashing algorithm to use (e.g., "MD5", "SHA-1", "SHA-256").
     * @return The hexadecimal string representation of the hash, or null if an error occurs.
     */
    private static String calculateHashFromStream(InputStream is, String algorithm) {
        if (is == null) {
            LOGGER.warning("Input stream is null for " + algorithm + " calculation.");
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192]; // Use a larger buffer for efficiency
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHexString(hashBytes); // Call helper to convert bytes to hex string
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Hashing algorithm not found: " + algorithm + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null instead of throwing RuntimeException directly
        } catch (IOException e) {
            LOGGER.severe("IOException while calculating " + algorithm + " from stream. Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Stream management is handled by the caller using try-with-resources.
            // If this method takes ownership of closing, it should be in a finally block.
            // For now, removing `is.close()` from here as public methods will use try-with-resources.
        }
    }

    /**
     * Calculates the MD5 hash of a file directly from assets.
     * @param context The application context.
     * @param assetPath The path to the file within the 'assets' folder (e.g., "SignatureKiller/origin.apk").
     * @return The MD5 hash as a hexadecimal string, or null on error.
     */
    public static String calculateMD5FromAsset(Context context, String assetPath) {
        LOGGER.info("Calculating MD5 from asset: " + assetPath);
        try {
            try (InputStream is = context.getAssets().open(assetPath)) { // try-with-resources ensures stream is closed
                return calculateHashFromStream(is, "MD5");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to open asset for MD5 calculation: " + assetPath + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculates the SHA-1 hash of a file directly from assets.
     * @param context The application context.
     * @param assetPath The path to the file within the 'assets' folder.
     * @return The SHA-1 hash as a hexadecimal string, or null on error.
     */
    public static String calculateSHA1FromAsset(Context context, String assetPath) {
        LOGGER.info("Calculating SHA-1 from asset: " + assetPath);
        try {
            try (InputStream is = context.getAssets().open(assetPath)) { // try-with-resources ensures stream is closed
                return calculateHashFromStream(is, "SHA-1");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to open asset for SHA-1 calculation: " + assetPath + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculates the SHA-256 hash of a file directly from assets.
     * @param context The application context.
     * @param assetPath The path to the file within the 'assets' folder.
     * @return The SHA-256 hash as a hexadecimal string, or null on error.
     */
    public static String calculateSHA256FromAsset(Context context, String assetPath) {
        LOGGER.info("Calculating SHA-256 from asset: " + assetPath);
        try {
            try (InputStream is = context.getAssets().open(assetPath)) { // try-with-resources ensures stream is closed
                return calculateHashFromStream(is, "SHA-256");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to open asset for SHA-256 calculation: " + assetPath + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculates the CRC32 checksum of a file directly from assets.
     * @param context The application context.
     * @param assetPath The path to the file within the 'assets' folder.
     * @return The CRC32 checksum as an uppercase hexadecimal string, or null on error.
     */
    public static String calculateCRC32FromAsset(Context context, String assetPath) {
        LOGGER.info("Calculating CRC32 from asset: " + assetPath);
        try {
            try (InputStream is = context.getAssets().open(assetPath)) { // try-with-resources ensures stream is closed
                CRC32 crc = new CRC32();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    crc.update(buffer, 0, bytesRead);
                }
                return String.format("%08X", crc.getValue()); // %08X for 8 hex digits, uppercase
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to open asset for CRC32 calculation: " + assetPath + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to convert a byte array to a hexadecimal string representation.
     * (No spaces, uppercase for consistency with hash conventions)
     */
    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)); // Use %02X for uppercase hex, 2 digits
        }
        return sb.toString(); // Already uppercase from %02X
    }

    /**
     * Gets the absolute path of the APK file for the current application.
     * (This is separate as it gets the hash of the *running* APK, not an asset).
     * @param context The application context.
     * @return The absolute path to the APK file, or null if not found.
     */
    public static String getApkFilePath(Context context) {
        try {
            ApplicationInfo appInfo = context.getApplicationInfo();
            if (appInfo != null && appInfo.sourceDir != null) {
                File apkFile = new File(appInfo.sourceDir);
                if (apkFile.exists() && apkFile.isFile() && apkFile.canRead()) {
                    LOGGER.info("APK file path found: " + apkFile.getAbsolutePath());
                    return apkFile.getAbsolutePath();
                } else {
                    LOGGER.warning("APK file path found, but file does not exist, is not a file, or cannot be read: " + apkFile.getAbsolutePath());
                    return null;
                }
            } else {
                LOGGER.severe("ApplicationInfo or sourceDir is null. Cannot get APK path.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.severe("Error getting APK file path: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // --- You can still use the file-based hash calculation methods if needed for other files ---
    // These methods accept a java.io.File object, useful for files on disk (not assets).
    public static String calculateMD5(File file) {
        LOGGER.info("Calculating MD5 for file: " + file.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateHashFromStream(fis, "MD5");
        } catch (IOException e) {
            LOGGER.severe("Failed to open file for MD5 calculation: " + file.getAbsolutePath() + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // Similarly, you can define calculateSHA1(File), calculateSHA256(File), calculateCRC32(File)
    // by adapting the methods to accept FileInputStream and use calculateHashFromStream.
    public static String calculateSHA1(File file) {
        LOGGER.info("Calculating SHA-1 for file: " + file.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateHashFromStream(fis, "SHA-1");
        } catch (IOException e) {
            LOGGER.severe("Failed to open file for SHA-1 calculation: " + file.getAbsolutePath() + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String calculateSHA256(File file) {
        LOGGER.info("Calculating SHA-256 for file: " + file.getAbsolutePath());
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateHashFromStream(fis, "SHA-256");
        } catch (IOException e) {
            LOGGER.severe("Failed to open file for SHA-256 calculation: " + file.getAbsolutePath() + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String calculateCRC32(File file) {
        LOGGER.info("Calculating CRC32 for file: " + file.getAbsolutePath());
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            LOGGER.warning("File does not exist, is not a file, or cannot be read for CRC32: " + file.getAbsolutePath());
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            CRC32 crc = new CRC32();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
            return String.format("%08X", crc.getValue());
        } catch (IOException e) {
            LOGGER.severe("IOException while calculating CRC32 for file " + file.getAbsolutePath() + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Example implementation showing proper usage of FileHashCalculator
     */
    public static class HashResult {
        private final String md5;
        private final String sha1;
        private final String sha256;
        private final String crc32;
        private final String filePath;
        private final boolean isSuccess;
        private final String errorMessage;

        private HashResult(Builder builder) {
            this.md5 = builder.md5;
            this.sha1 = builder.sha1;
            this.sha256 = builder.sha256;
            this.crc32 = builder.crc32;
            this.filePath = builder.filePath;
            this.isSuccess = builder.isSuccess;
            this.errorMessage = builder.errorMessage;
        }

        public String getMd5() { return md5; }
        public String getSha1() { return sha1; }
        public String getSha256() { return sha256; }
        public String getCrc32() { return crc32; }
        public String getFilePath() { return filePath; }
        public boolean isSuccess() { return isSuccess; }
        public String getErrorMessage() { return errorMessage; }

        public static class Builder {
            private String md5;
            private String sha1;
            private String sha256;
            private String crc32;
            private String filePath;
            private boolean isSuccess = true;
            private String errorMessage = "";

            public Builder setMd5(String md5) { this.md5 = md5; return this; }
            public Builder setSha1(String sha1) { this.sha1 = sha1; return this; }
            public Builder setSha256(String sha256) { this.sha256 = sha256; return this; }
            public Builder setCrc32(String crc32) { this.crc32 = crc32; return this; }
            public Builder setFilePath(String filePath) { this.filePath = filePath; return this; }
            public Builder setError(String error) { 
                this.isSuccess = false;
                this.errorMessage = error;
                return this;
            }

            public HashResult build() {
                return new HashResult(this);
            }
        }
    }

    /**
     * Calculates all hashes for a file in assets.
     * @param context The application context
     * @param assetPath The path to the file in assets
     * @return HashResult containing all calculated hashes
     */
    public static HashResult calculateAssetHashes(Context context, String assetPath) {
        HashResult.Builder builder = new HashResult.Builder().setFilePath(assetPath);
        
        try {
            String md5 = calculateMD5FromAsset(context, assetPath);
            String sha1 = calculateSHA1FromAsset(context, assetPath);
            String sha256 = calculateSHA256FromAsset(context, assetPath);
            String crc32 = calculateCRC32FromAsset(context, assetPath);

            if (md5 == null || sha1 == null || sha256 == null || crc32 == null) {
                return builder.setError("Failed to calculate one or more hashes").build();
            }

            return builder
                .setMd5(md5)
                .setSha1(sha1)
                .setSha256(sha256)
                .setCrc32(crc32)
                .build();
        } catch (Exception e) {
            LOGGER.severe("Error calculating hashes for asset: " + assetPath + ". " + e.getMessage());
            return builder.setError("Exception while calculating hashes: " + e.getMessage()).build();
        }
    }

    /**
     * Calculates all hashes for the current application's APK
     * @param context The application context
     * @return HashResult containing all calculated hashes
     */
    public static HashResult calculateCurrentApkHashes(Context context) {
        String apkPath = getApkFilePath(context);
        HashResult.Builder builder = new HashResult.Builder().setFilePath(apkPath);
        
        if (apkPath == null) {
            return builder.setError("Could not get APK file path").build();
        }

        try {
            File apkFile = new File(apkPath);
            String md5 = calculateMD5(apkFile);
            String sha1 = calculateSHA1(apkFile);
            String sha256 = calculateSHA256(apkFile);
            String crc32 = calculateCRC32(apkFile);

            if (md5 == null || sha1 == null || sha256 == null || crc32 == null) {
                return builder.setError("Failed to calculate one or more hashes").build();
            }

            return builder
                .setMd5(md5)
                .setSha1(sha1)
                .setSha256(sha256)
                .setCrc32(crc32)
                .build();
        } catch (Exception e) {
            LOGGER.severe("Error calculating hashes for APK: " + apkPath + ". " + e.getMessage());
            return builder.setError("Exception while calculating hashes: " + e.getMessage()).build();
        }
    }

    /*
     // Place this code within an Android Context (e.g., your Application class's onCreate
     // or your main Activity's onCreate method).
     // Make sure to import android.util.Log if you're using Log.i/e etc.

     public class MyAndroidApp extends Application { // Or public class MyMainActivity extends AppCompatActivity {
     @Override
     public void onCreate() {
     super.onCreate(); // If in an Activity, call super.onCreate(savedInstanceState);
     // import android.util.Log; // Make sure this is imported if you use Log.i/e

     Log.i("MyAndroidApp", "Starting APK Hash Calculation directly from assets...");

     // --- 1. Calculate Hashes for 'origin.apk' directly from assets ---
     // Ensure you have 'origin.apk' located at app/src/main/assets/SignatureKiller/origin.apk
     String assetPathToOriginApk = "SignatureKiller/origin.apk"; // The path within your assets folder

     String md5FromAsset = FileHashCalculator.calculateMD5FromAsset(getApplicationContext(), assetPathToOriginApk);
     String sha1FromAsset = FileHashCalculator.calculateSHA1FromAsset(getApplicationContext(), assetPathToOriginApk);
     String sha256FromAsset = FileHashCalculator.calculateSHA256FromAsset(getApplicationContext(), assetPathToOriginApk);
     String crc32FromAsset = FileHashCalculator.calculateCRC32FromAsset(getApplicationContext(), assetPathToOriginApk);

     // --- 2. Use the calculated hashes ---
     Log.i("MyAndroidApp", "--- Hashes for 'origin.apk' (read directly from assets) ---");
     Log.i("MyAndroidApp", "Asset Path: " + assetPathToOriginApk);
     Log.i("MyAndroidApp", "MD5:     " + (md5FromAsset != null ? md5FromAsset : "Failed"));
     Log.i("MyAndroidApp", "SHA-1:   " + (sha1FromAsset != null ? sha1FromAsset : "Failed"));
     Log.i("MyAndroidApp", "SHA-256: " + (sha256FromAsset != null ? sha256FromAsset : "Failed"));
     Log.i("MyAndroidApp", "CRC32:   " + (crc32FromAsset != null ? crc32FromAsset : "Failed"));

     // Example: Compare with an expected hash (if you know it)
     // String expectedMd5ForOriginApk = "YOUR_EXPECTED_MD5_FOR_ASSET_ORIGIN_APK";
     // if (md5FromAsset != null && md5FromAsset.equalsIgnoreCase(expectedMd5ForOriginApk)) {
     //     Log.i("MyAndroidApp", "MD5 verification passed for origin.apk from assets!");
     // } else {
     //     Log.w("MyAndroidApp", "MD5 verification failed for origin.apk from assets!");
     // }

     // --- Optional: Calculate hashes for your OWN App's APK (the one currently running) ---
     // This part still works on the installed APK file path.
     Log.i("MyAndroidApp", "\n--- Hashes for YOUR APP's APK (the one running now) ---");
     String myApkPath = FileHashCalculator.getApkFilePath(getApplicationContext());
     if (myApkPath != null) {
     File myApkFile = new File(myApkPath);
     Log.i("MyAndroidApp", "My APK File: " + myApkPath);
     Log.i("MyAndroidApp", "My APK MD5:     " + (FileHashCalculator.calculateMD5(myApkFile)));
     Log.i("MyAndroidApp", "My APK SHA-1:   " + (FileHashCalculator.calculateSHA1(myApkFile)));
     Log.i("MyAndroidApp", "My APK SHA-256: " + (FileHashCalculator.calculateSHA256(myApkFile)));
     Log.i("MyAndroidApp", "My APK CRC32:   " + (FileHashCalculator.calculateCRC32(myApkFile)));
     } else {
     Log.e("MyAndroidApp", "Could not get your own APK file path.");
     }
     }
     }
     */
} 