package com.bearmod.auth;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assume;

@RunWith(AndroidJUnit4.class)
public class SimpleLicenseVerifierIT {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        // Point the verifier at the mock server for both init and license calls
        SimpleLicenseVerifier.setApiBaseForTesting(server.url("/").toString());
    }

    @After
    public void tearDown() throws Exception {
        SimpleLicenseVerifier.clearApiBaseForTesting();
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void verifyLicense_success_withMockServer() throws Exception {
        // 1) init response (success + sessionid)
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"success\":true,\"sessionid\":\"sess123\"}"));
        // 2) license response (success)
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"success\":true,\"expiry\":\"2099-01-01 00:00:00\"}"));

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        Context ctx = ApplicationProvider.getApplicationContext();
        SimpleLicenseVerifier.verifyLicense(ctx, "TEST-KEY", new SimpleLicenseVerifier.LicenseCallback() {
            @Override
            public void onSuccess(String message) {
                success[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                latch.countDown();
            }
        });

        assertTrue("Callback not invoked in time", latch.await(2, TimeUnit.SECONDS));
        assertTrue("Expected success from mocked response", success[0]);

        // Request assertions for success path
        RecordedRequest initReq = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest licReq = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull("Expected init request", initReq);
        assertNotNull("Expected license request", licReq);
        assertEquals("/", initReq.getPath());
        assertEquals("/", licReq.getPath());
        assertEquals("POST", initReq.getMethod());
        assertEquals("POST", licReq.getMethod());
        String initBody = initReq.getBody().readUtf8();
        String licBody = licReq.getBody().readUtf8();
        // Init must contain KeyAuth init fields
        assertTrue(initBody.contains("type=init"));
        assertTrue(initBody.contains("name="));
        assertTrue(initBody.contains("ownerid="));
        assertTrue(initBody.contains("ver="));
        assertTrue(initBody.contains("hash="));
        // License must contain required fields and session id propagated from init (sess123)
        assertTrue(licBody.contains("type=license"));
        assertTrue(licBody.contains("key="));
        assertTrue(licBody.contains("hwid="));
        assertTrue(licBody.contains("sessionid=sess123"));
        assertTrue(licBody.contains("name="));
        assertTrue(licBody.contains("ownerid="));
        assertTrue(licBody.contains("ver="));
    }

    @Test
    public void verifyLicense_failure_withMockServer() throws Exception {
        // 1) init response (success + sessionid)
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"success\":true,\"sessionid\":\"sess456\"}"));
        // 2) license response (explicit failure)
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"success\":false,\"message\":\"invalid key\"}"));

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] failed = {false};

        Context ctx = ApplicationProvider.getApplicationContext();
        SimpleLicenseVerifier.verifyLicense(ctx, "BAD-KEY", new SimpleLicenseVerifier.LicenseCallback() {
            @Override
            public void onSuccess(String message) {
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                failed[0] = true;
                latch.countDown();
            }
        });

        assertTrue("Callback not invoked in time", latch.await(2, TimeUnit.SECONDS));
        assertTrue("Expected failure from mocked response", failed[0]);

        // Request assertions for failure path
        RecordedRequest r1 = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest r2 = server.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull("Expected first request", r1);
        assertNotNull("Expected second request", r2);
        assertEquals("/", r1.getPath());
        assertEquals("/", r2.getPath());
        assertEquals("POST", r1.getMethod());
        assertEquals("POST", r2.getMethod());
        String body1 = r1.getBody().readUtf8();
        String body2 = r2.getBody().readUtf8();
        assertTrue(body1.contains("type=init"));
        assertTrue(body2.contains("type=license"));
        assertTrue(body2.contains("sessionid=sess456"));
        // Content-Type should be form-encoded at least for init
        String ct1 = r1.getHeader("Content-Type");
        if (ct1 != null) {
            assertTrue(ct1.contains("application/x-www-form-urlencoded"));
        }
        // No third request should be present quickly
        RecordedRequest r3 = server.takeRequest(200, TimeUnit.MILLISECONDS);
        assertNull("Did not expect a third request", r3);
    }
}
