package com.bearmod.auth;

import org.junit.Test;
import static org.junit.Assert.*;

import android.content.Context;

public class SimpleLicenseVerifierTest {

    @Test
    public void testHasValidStoredAuth_DefaultFalse() {
        // With returnDefaultValues enabled, static calls return defaults
        // We just assert the method is callable and returns a boolean.
        Context ctx = null; // not used due to returnDefaultValues
        boolean result = SimpleLicenseVerifier.hasValidStoredAuth(ctx);
        // Expect false by default (no stored session)
        assertFalse(result);
    }
}
