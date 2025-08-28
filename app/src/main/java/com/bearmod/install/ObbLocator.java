package com.bearmod.install;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ObbLocator
 *
 * Purpose:
 * - Locate candidate OBB files that users may have downloaded manually
 *   (e.g., via Telegram or Browser) so we can assist moving them to
 *   Android/obb/<pkg>/ using SAF.
 *
 * Strategy:
 * - Search a small set of common public directories on primary storage:
 *   - /storage/emulated/0/Download
 *   - /storage/emulated/0/Telegram/Telegram Files
 *   - /storage/emulated/0/Android/org.telegram.messenger/files/Telegram/Telegram Files
 * - Filter by filename pattern: main.<versionCode>.<package>.obb
 */
public final class ObbLocator {
    private static final String TAG = "ObbLocator";

    private ObbLocator() {}

    /** Return candidate OBB files matching main.<versionCode>.<pkg>.obb. */
    public static List<File> findCandidates(String packageName, int versionCode) {
        List<File> results = new ArrayList<>();
        if (TextUtils.isEmpty(packageName) || versionCode <= 0) return results;
        String fileName = "main." + versionCode + "." + packageName + ".obb";

        List<File> roots = new ArrayList<>();
        File external = Environment.getExternalStorageDirectory();
        if (external != null) {
            roots.add(new File(external, "Download"));
            roots.add(new File(external, "Telegram/Telegram Files"));
            roots.add(new File(external, "Android/org.telegram.messenger/files/Telegram/Telegram Files"));
        }

        for (File dir : roots) {
            try {
                if (dir != null && dir.exists() && dir.isDirectory()) {
                    File f = new File(dir, fileName);
                    if (f.exists() && f.isFile() && f.length() > 0) {
                        results.add(f);
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "scan error: " + dir, t);
            }
        }
        return results;
    }
}
