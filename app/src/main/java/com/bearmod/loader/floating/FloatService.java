package com.bearmod.loader.floating;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * FloatService - Modular floating service wrapper
 *
 * This service provides a clean interface for managing the floating overlay
 * functionality, replacing direct Floating.class references.
 */
public class FloatService extends Service {
    private static final String TAG = "FloatService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "FloatService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "FloatService started");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "FloatService destroyed");
        super.onDestroy();
    }

    /**
     * Start the floating service
     */
    public static void startService(Context context) {
        try {
            Intent intent = new Intent(context, FloatService.class);
            context.startService(intent);
            Log.d(TAG, "FloatService started via static method");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start FloatService", e);
            throw new RuntimeException("Failed to start floating service", e);
        }
    }

    /**
     * Stop the floating service
     */
    public static void stopService(Context context) {
        try {
            Intent intent = new Intent(context, FloatService.class);
            context.stopService(intent);
            Log.d(TAG, "FloatService stopped via static method");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop FloatService", e);
            throw new RuntimeException("Failed to stop floating service", e);
        }
    }

    /**
     * Bind to the floating service
     */
    public static void bindService(Context context, ServiceConnection connection) {
        try {
            Intent intent = new Intent(context, FloatService.class);
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "FloatService bound via static method");
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind FloatService", e);
            throw new RuntimeException("Failed to bind floating service", e);
        }
    }
}