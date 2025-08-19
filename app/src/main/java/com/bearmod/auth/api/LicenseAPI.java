package com.bearmod.auth.api;

import android.content.Context;

/**
 * LicenseAPI
 *
 * Purpose:
 * Public facade for license verification. Designed for easy mocking in unit tests
 * and flexible backend implementations (e.g., KeyAuth, offline, mock).
 *
 * Responsibilities:
 * - Provide a synchronous verification entrypoint for JVM tests.
 * - Optionally expose metadata (e.g., expiration) for UI.
 *
 * Lifecycle:
 * - Created per usage or injected via DI; stateless implementations are recommended.
 */
public interface LicenseAPI {

    /**
     * Verify a license key.
     *
     * @param context Android context; may be unused by mock implementations.
     * @param licenseKey license string to verify
     * @return true if valid, false otherwise
     */
    boolean verify(Context context, String licenseKey);

    /**
     * Optional: expiration string if available, null otherwise.
     */
    default String getExpiration() { return null; }
}
