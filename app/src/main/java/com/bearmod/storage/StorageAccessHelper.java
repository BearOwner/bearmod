package com.bearmod.storage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

/**
 * StorageAccessHelper
 *
 * Purpose:
 * - Manage persisted Storage Access Framework (SAF) tree URIs for per-package OBB directories.
 * - Expose helpers to check access and get DocumentFile handles without prompting the user.
 *
 * Notes:
 * - This helper does NOT launch SAF pickers. It only uses already-persisted URIs.
 * - If a URI does not exist for a package, callers should skip operations silently.
 */
public final class StorageAccessHelper {
    private static final String PREFS = "bearmod_saf_prefs";
    private static final String KEY_PREFIX_OBB = "obb_uri_";

    private StorageAccessHelper() {}

    /** Persist a tree URI for the OBB directory of the given package. */
    public static void persistObbTreeUri(Context ctx, String pkg, Uri treeUri) {
        if (ctx == null || pkg == null || treeUri == null) return;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PREFIX_OBB + pkg, treeUri.toString()).apply();
        // Also ensure we hold persistable permission
        final int flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            ctx.getContentResolver().takePersistableUriPermission(treeUri, flags);
        } catch (Exception ignored) {}
    }

    /** Retrieve the previously persisted tree URI for the OBB directory of the given package, if any. */
    @Nullable
    public static Uri getPersistedObbTreeUri(Context ctx, String pkg) {
        if (ctx == null || pkg == null) return null;
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = sp.getString(KEY_PREFIX_OBB + pkg, null);
        return s == null ? null : Uri.parse(s);
    }

    /** Return DocumentFile for the OBB directory, or null if not available. */
    @Nullable
    public static DocumentFile getObbDir(Context ctx, String pkg) {
        Uri tree = getPersistedObbTreeUri(ctx, pkg);
        if (tree == null) return null;
        try {
            return DocumentFile.fromTreeUri(ctx, tree);
        } catch (Exception e) {
            return null;
        }
    }

    /** Check if we appear to have write access for the package OBB directory. */
    public static boolean hasWriteAccess(Context ctx, String pkg) {
        DocumentFile dir = getObbDir(ctx, pkg);
        return dir != null && dir.canWrite();
    }
}
