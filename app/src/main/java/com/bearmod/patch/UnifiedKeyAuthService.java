package com.bearmod.patch;

import android.content.Context;

import java.util.concurrent.CompletableFuture;

/**
 * Minimal UnifiedKeyAuthService stub used by FridaPatchManager for downloads.
 * Always returns success to keep flow functional without external dependencies.
 */
public class UnifiedKeyAuthService {
    private static volatile UnifiedKeyAuthService instance;
    private final Context appContext;

    private UnifiedKeyAuthService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static UnifiedKeyAuthService getInstance(Context context) {
        if (instance == null) {
            synchronized (UnifiedKeyAuthService.class) {
                if (instance == null) {
                    instance = new UnifiedKeyAuthService(context);
                }
            }
        }
        return instance;
    }

    public CompletableFuture<DownloadResult> downloadFile(String name) {
        // Immediately complete successfully
        return CompletableFuture.completedFuture(DownloadResult.success());
    }

    public static class DownloadResult {
        private final boolean success;
        private final String message;

        private DownloadResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static DownloadResult success() { return new DownloadResult(true, "ok"); }
        public static DownloadResult failure(String msg) { return new DownloadResult(false, msg); }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
