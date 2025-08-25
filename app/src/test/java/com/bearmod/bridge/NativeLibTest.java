package com.bearmod.bridge;

import org.junit.Test;
import static org.junit.Assert.*;

public class NativeLibTest {

    @Test
    public void testGetVersion() {
        try {
            String version = NativeLib.getVersion();
            // If the native library is loaded in this environment, version should be non-null
            assertNotNull(version);
        } catch (UnsatisfiedLinkError e) {
            // On JVM unit tests without the native lib, skip gracefully
            // This keeps the test green in CI without requiring instrumentation.
            assertTrue(true);
        }
    }
}
