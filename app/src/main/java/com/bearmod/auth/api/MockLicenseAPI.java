package com.bearmod.auth.api;

import android.content.Context;

/**
 * MockLicenseAPI
 *
 * Purpose:
 * Deterministic, dependency-free mock for unit tests. Avoids network/JNI.
 *
 * Behavior:
 * - Any license equal to "VALID" (case-insensitive) returns true.
 * - Any other input returns false.
 */
public final class MockLicenseAPI implements LicenseAPI {

    @Override
    public boolean verify(Context context, String licenseKey) {
        if (licenseKey == null) return false;
        return "VALID".equalsIgnoreCase(licenseKey.trim());
    }
}
