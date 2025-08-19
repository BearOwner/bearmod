# API + Mock Skeleton

Interface + Mock + Unit Test pattern for JVM-friendly testing.

```java
// Interface
package com.bearmod.sample.api;

import android.content.Context;

/**
 * SampleAPI
 * Purpose: public, mockable facade.
 * Responsibilities: business contract; no transport details.
 * Lifecycle: stateless or injected.
 */
public interface SampleAPI {
    boolean doWork(Context context, String input);
}
```

```java
// Mock implementation
package com.bearmod.sample.api;

import android.content.Context;

/**
 * MockSampleAPI
 * Deterministic behavior for unit tests; no network/JNI.
 */
public final class MockSampleAPI implements SampleAPI {
    @Override public boolean doWork(Context context, String input) {
        return input != null && input.trim().equalsIgnoreCase("OK");
    }
}
```

```java
// Unit test
package com.bearmod.sample.api;

import static org.junit.Assert.*;
import android.content.Context;
import org.junit.Test;

public class MockSampleAPITest {
    @Test public void ok_returnsTrue() {
        SampleAPI api = new MockSampleAPI();
        Context ctx = null;
        assertTrue(api.doWork(ctx, "OK"));
    }
    @Test public void other_returnsFalse() {
        SampleAPI api = new MockSampleAPI();
        Context ctx = null;
        assertFalse(api.doWork(ctx, "NOPE"));
    }
}
```

Notes:
- Keep interfaces small and synchronous for JVM tests when possible.
- Real implementations can delegate to async/network layers; instrumentation tests can cover them.
