package com.bearmod.auth.api;

import static org.junit.Assert.*;

import android.content.Context;
import org.junit.Test;

public class MockLicenseAPITest {

    @Test
    public void verify_validLicense_returnsTrue() {
        LicenseAPI api = new MockLicenseAPI();
        Context ctx = null; // returnDefaultValues=true, context not used by mock
        assertTrue(api.verify(ctx, "VALID"));
        assertTrue(api.verify(ctx, "valid"));
        assertTrue(api.verify(ctx, "  VALID  "));
    }

    @Test
    public void verify_invalidLicense_returnsFalse() {
        LicenseAPI api = new MockLicenseAPI();
        Context ctx = null;
        assertFalse(api.verify(ctx, null));
        assertFalse(api.verify(ctx, ""));
        assertFalse(api.verify(ctx, "nope"));
    }
}
