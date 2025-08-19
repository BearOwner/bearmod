package com.bearmod.mundo.internal;

import android.content.Context;
import java.io.File;

/**
 * AssetGuard - prepares for asset-to-memory migration by preventing direct file access.
 * NOTE: Stub implementation; integrate with SecureScriptManager in a follow-up.
 */
public final class AssetGuard {
    private AssetGuard() {}
    public static boolean isPathAllowed(File f) {
        // Deny direct access to known sensitive dirs; allow others for now.
        String p = f.getAbsolutePath();
        return !(p.contains("/Download/") || p.contains("/Android/data/") || p.contains("/bearmod/assets"));
    }
    public static void install(Context ctx) { /* no-op stub for now */ }
}

