package com.bearmod.loader.floating;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.view.WindowManager;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingServiceManager - Manages service lifecycle and threading
 * Handles background threads, service operations, and resource management
 */
public class FloatingServiceManager {

    private static final String TAG = "FloatingServiceManager";

    private final Context context;

    // Thread management
    private Thread updateCanvasThread;
    private Thread updateThread;
    private boolean isCanvasRunning = false;
    private boolean isUpdateRunning = false;

    // Service state
    private boolean isServiceRunning = false;
    private Handler mainHandler;

    // Window management
    private WindowManager windowManager;
    private int screenWidth = 0;
    private int screenHeight = 0;

    public FloatingServiceManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        Logx.d("FloatingServiceManager initialized");
    }

    /**
     * Start service operations
     */
    public void startService() {
        if (isServiceRunning) {
            Logx.w("Service already running");
            return;
        }

        try {
            Logx.d("Starting floating service operations...");

            // Initialize window manager
            initializeWindowManager();

            // Start background threads
            startCanvasUpdateThread();
            startScreenUpdateThread();

            isServiceRunning = true;
            Logx.d("Floating service operations started successfully");

        } catch (Exception e) {
            Logx.e("Error starting floating service: " + e.getMessage(), e);
        }
    }

    /**
     * Stop service operations
     */
    public void stopService() {
        if (!isServiceRunning) {
            Logx.w("Service not running");
            return;
        }

        try {
            Logx.d("Stopping floating service operations...");

            // Stop background threads
            stopCanvasUpdateThread();
            stopScreenUpdateThread();

            isServiceRunning = false;
            Logx.d("Floating service operations stopped successfully");

        } catch (Exception e) {
            Logx.e("Error stopping floating service: " + e.getMessage(), e);
        }
    }

    /**
     * Handle service intent
     */
    public void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        try {
            String action = intent.getAction();
            Logx.d("Handling service intent: " + action);

            // Handle different intent actions here
            if ("START_FLOATING".equals(action)) {
                startService();
            } else if ("STOP_FLOATING".equals(action)) {
                stopService();
            } else if ("UPDATE_SCREEN_SIZE".equals(action)) {
                updateScreenDimensions();
            }

        } catch (Exception e) {
            Logx.e("Error handling service intent: " + e.getMessage(), e);
        }
    }

    /**
     * Check if service is running
     */
    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * Get screen width
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Get screen height
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Update screen dimensions
     */
    public void updateScreenDimensions() {
        try {
            if (windowManager != null) {
                // Get current window metrics
                android.view.WindowMetrics metrics = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    metrics = windowManager.getCurrentWindowMetrics();
                }

                if (metrics != null) {
                    android.graphics.Rect bounds = metrics.getBounds();
                    int newWidth = bounds.width();
                    int newHeight = bounds.height();

                    if (newWidth != screenWidth || newHeight != screenHeight) {
                        screenWidth = newWidth;
                        screenHeight = newHeight;
                        Logx.d("Screen dimensions updated: " + screenWidth + "x" + screenHeight);
                    }
                }
            }
        } catch (Exception e) {
            Logx.e("Error updating screen dimensions: " + e.getMessage(), e);
        }
    }

    /**
     * Get device FPS for optimal rendering
     */
    public int getDeviceMaxFps() {
        try {
            if (context == null) {
                return 60; // Default fallback
            }

            final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (wm != null) {
                    final android.util.DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                    float refreshRate = displayMetrics.densityDpi; // fallback

                    // Try to get refresh rate from DisplayManager
                    final android.hardware.display.DisplayManager dm =
                        (android.hardware.display.DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
                    if (dm != null) {
                        final android.view.Display[] displays = dm.getDisplays();
                        if (displays.length > 0) {
                            refreshRate = displays[0].getRefreshRate();
                        }
                    }
                    return (int) refreshRate;
                }
            } else {
                if (wm != null) {
                    final android.view.Display display = wm.getDefaultDisplay();
                    if (display != null) {
                        return (int) display.getRefreshRate();
                    }
                }
            }
        } catch (Exception e) {
            Logx.e("Error getting device FPS: " + e.getMessage(), e);
        }

        return 60; // Default fallback
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingServiceManager resources...");

            // Stop all operations
            stopService();

            // Clear references
            updateCanvasThread = null;
            updateThread = null;
            windowManager = null;
            mainHandler = null;

            Logx.d("FloatingServiceManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingServiceManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void initializeWindowManager() {
        try {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            updateScreenDimensions();
            Logx.d("Window manager initialized");
        } catch (Exception e) {
            Logx.e("Error initializing window manager: " + e.getMessage(), e);
        }
    }

    private void startCanvasUpdateThread() {
        if (isCanvasRunning) {
            return;
        }

        updateCanvasThread = new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            isCanvasRunning = true;

            Logx.d("Canvas update thread started");

            while (isCanvasRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    long startTime = System.currentTimeMillis();

                    // Update canvas on main thread
                    mainHandler.post(() -> {
                        // Canvas update logic would go here
                        // This would typically invalidate the ESP view
                    });

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int targetFps = getDeviceMaxFps();
                    long sleepTime = 1000 / targetFps;

                    // Sleep for remaining time, but don't go negative
                    long adjustedSleep = Math.max(0, sleepTime - elapsedTime);
                    Thread.sleep(adjustedSleep);

                } catch (InterruptedException e) {
                    Logx.d("Canvas update thread interrupted");
                    break;
                } catch (Exception e) {
                    Logx.e("Error in canvas update thread: " + e.getMessage(), e);
                }
            }

            isCanvasRunning = false;
            Logx.d("Canvas update thread stopped");
        });

        updateCanvasThread.setName("FloatingCanvasUpdate");
        updateCanvasThread.start();
    }

    private void stopCanvasUpdateThread() {
        if (updateCanvasThread != null && updateCanvasThread.isAlive()) {
            try {
                isCanvasRunning = false;
                updateCanvasThread.interrupt();
                updateCanvasThread.join(1000); // Wait up to 1 second
                Logx.d("Canvas update thread stopped");
            } catch (Exception e) {
                Logx.e("Error stopping canvas update thread: " + e.getMessage(), e);
            }
        }
    }

    private void startScreenUpdateThread() {
        if (isUpdateRunning) {
            return;
        }

        updateThread = new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            isUpdateRunning = true;

            Logx.d("Screen update thread started");

            while (isUpdateRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    long startTime = System.currentTimeMillis();

                    // Check for screen size changes
                    updateScreenDimensions();

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    int targetFps = getDeviceMaxFps();
                    long sleepTime = 1000 / targetFps;

                    // Sleep for remaining time, but don't go negative
                    long adjustedSleep = Math.max(0, sleepTime - elapsedTime);
                    Thread.sleep(adjustedSleep);

                } catch (InterruptedException e) {
                    Logx.d("Screen update thread interrupted");
                    break;
                } catch (Exception e) {
                    Logx.e("Error in screen update thread: " + e.getMessage(), e);
                }
            }

            isUpdateRunning = false;
            Logx.d("Screen update thread stopped");
        });

        updateThread.setName("FloatingScreenUpdate");
        updateThread.start();
    }

    private void stopScreenUpdateThread() {
        if (updateThread != null && updateThread.isAlive()) {
            try {
                isUpdateRunning = false;
                updateThread.interrupt();
                updateThread.join(1000); // Wait up to 1 second
                Logx.d("Screen update thread stopped");
            } catch (Exception e) {
                Logx.e("Error stopping screen update thread: " + e.getMessage(), e);
            }
        }
    }
}