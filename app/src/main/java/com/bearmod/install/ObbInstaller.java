package com.bearmod.install;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.bearmod.storage.StorageAccessHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ObbInstaller
 *
 * Purpose:
 * - Silently copy the game's main OBB file into Android/obb/<pkg>/ using SAF when we already have
 *   a persisted tree URI for that target package. If anything is missing, we skip without error.
 *
 * Behavior:
 * - Computes expected OBB name: main.<versionCode>.<packageName>.obb
 * - Source: /storage/emulated/0/Android/obb/<pkg>/<obb>
 * - Destination (SAF): tree uri for Android/obb/<pkg>/
 *
 * This class does not launch SAF. Callers should only use it to attempt a non-intrusive auto-fix.
 */
public final class ObbInstaller {
    private static final String TAG = "ObbInstaller";

    private ObbInstaller() {}

    /** Attempt silent OBB fix; returns true if copied or already exists, false if skipped. */
    public static boolean tryAutoFixObb(Context ctx, String packageName) {
        try {
            if (ctx == null || packageName == null) return false;

            // Ensure we have a persisted OBB tree for this package
            DocumentFile obbDir = StorageAccessHelper.getObbDir(ctx, packageName);
            if (obbDir == null || !obbDir.canWrite()) {
                Log.d(TAG, "No persisted OBB tree or not writeable. Skipping.");
                return false;
            }

            // Continue with auto-fix flow
            PackageInfo pi = getPackageInfo(ctx, packageName);
            if (pi == null) {
                Log.d(TAG, "PackageInfo missing. Skipping OBB fix.");
                return false;
            }
            String obbName = "main." + pi.versionCode + "." + pi.packageName + ".obb";

            // Check source OBB
            File src = new File("/storage/emulated/0/Android/obb/" + packageName, obbName);
            if (!src.exists() || src.length() <= 0) {
                Log.d(TAG, "Source OBB not found: " + src);
                return false;
            }

            // Resolve destination
            DocumentFile existing = findChild(obbDir, obbName);
            if (existing != null && existing.length() == src.length()) {
                Log.d(TAG, "Destination OBB already present and same size. Nothing to do.");
                return true;
            }
            if (existing != null) try { existing.delete(); } catch (Exception ignored) {}

            DocumentFile dest = obbDir.createFile("application/octet-stream", obbName);
            if (dest == null) {
                Log.d(TAG, "Failed to create destination OBB via SAF.");
                return false;
            }

            boolean copied = copyFileToUri(ctx, src, dest.getUri());
            Log.d(TAG, "OBB copy result: " + copied);
            return copied;
        } catch (Throwable t) {
            Log.w(TAG, "tryAutoFixObb failed", t);
            return false;
        }
    }

    /**
     * Copy a user-provided OBB file into Android/obb/<pkg>/ using SAF.
     * This requires that the app has a persisted tree URI for the package's OBB directory
     * (granted by the user via ACTION_OPEN_DOCUMENT_TREE). No patching or modification is performed.
     *
     * @param ctx          Context
     * @param packageName  Target package name (e.g., com.tencent.ig)
     * @param sourceObb    Source OBB file selected or auto-located by the user/app
     * @return true if copy succeeded or the same file already exists; false otherwise
     */
    public static boolean copyFromSource(Context ctx, String packageName, File sourceObb) {
        try {
            if (ctx == null || packageName == null || sourceObb == null) return false;
            if (!sourceObb.exists() || !sourceObb.isFile() || sourceObb.length() <= 0) return false;

            DocumentFile obbDir = StorageAccessHelper.getObbDir(ctx, packageName);
            if (obbDir == null || !obbDir.canWrite()) {
                Log.d(TAG, "Missing SAF write access for OBB dir of " + packageName);
                return false;
            }

            String obbName = sourceObb.getName();
            DocumentFile existing = findChild(obbDir, obbName);
            if (existing != null && existing.length() == sourceObb.length()) {
                Log.d(TAG, "OBB already present with same size: " + obbName);
                return true;
            }
            if (existing != null) {
                try { existing.delete(); } catch (Exception ignored) {}
            }

            DocumentFile dest = obbDir.createFile("application/octet-stream", obbName);
            if (dest == null) {
                Log.w(TAG, "Failed to create destination OBB via SAF for name: " + obbName);
                return false;
            }

            boolean copied = copyFileToUri(ctx, sourceObb, dest.getUri());
            Log.d(TAG, "copyFromSource result for " + obbName + ": " + copied);
            return copied;
        } catch (Throwable t) {
            Log.w(TAG, "copyFromSource failed", t);
            return false;
        }
    }

    @Nullable
    private static PackageInfo getPackageInfo(Context ctx, String pkg) {
        try {
            return ctx.getPackageManager().getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Nullable
    private static DocumentFile findChild(DocumentFile dir, String name) {
        for (DocumentFile f : dir.listFiles()) {
            if (f.getName() != null && f.getName().equals(name)) return f;
        }
        return null;
    }

    private static boolean copyFileToUri(Context ctx, File src, Uri dest) {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = ctx.getContentResolver().openOutputStream(dest, "w")) {
            if (out == null) return false;
            byte[] buf = new byte[1024 * 256];
            int r;
            long total = 0;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
                total += r;
            }
            out.flush();
            return total > 0;
        } catch (IOException e) {
            Log.w(TAG, "copyFileToUri error", e);
            return false;
        }
    }
}
