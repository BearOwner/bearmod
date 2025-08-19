# MockWebServer Sample

This snippet shows how to use OkHttp MockWebServer in Android instrumentation tests to replace backend calls.

## Setup
- Add dependency in `app/build.gradle` (androidTest):
```
androidTestImplementation "com.squareup.okhttp3:mockwebserver:5.0.0-alpha.14"
```

## Example
```java
@RunWith(AndroidJUnit4.class)
public class ExampleIT {
  private MockWebServer server;

  @Before public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    // Redirect API calls to the mock server
    SimpleLicenseVerifier.setApiBaseForTesting(server.url("/").toString());
  }

  @After public void tearDown() throws Exception {
    SimpleLicenseVerifier.clearApiBaseForTesting();
    server.shutdown();
  }

  @Test public void happyPath() throws Exception {
    // Enqueue init
    server.enqueue(new MockResponse().setResponseCode(200)
      .setBody("{\"success\":true,\"sessionid\":\"sess\"}"));
    // Enqueue license
    server.enqueue(new MockResponse().setResponseCode(200)
      .setBody("{\"success\":true,\"expiry\":\"2099-01-01 00:00:00\"}"));

    CountDownLatch latch = new CountDownLatch(1);
    final boolean[] ok = {false};
    Context ctx = ApplicationProvider.getApplicationContext();
    SimpleLicenseVerifier.verifyLicense(ctx, "TEST-KEY", new SimpleLicenseVerifier.LicenseCallback() {
      @Override public void onSuccess(String message) { ok[0] = true; latch.countDown(); }
      @Override public void onFailure(String error) { latch.countDown(); }
    });

    assertTrue(latch.await(2, TimeUnit.SECONDS));
    assertTrue(ok[0]);
  }
}
```

## Tips
- Always clear the override in @After.
- Keep timeouts small; prefer IdlingResource for UI flows.
- Use `server.takeRequest()` to assert path, method, and form fields.
