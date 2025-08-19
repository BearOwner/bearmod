package com.bearmod;

import android.annotation.SuppressLint;
import com.bearmod.auth.SimpleLicenseVerifier;
import com.bearmod.auth.HWID;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.TypedValue;
import android.content.Context;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.util.Base64;
import android.view.Display;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.android.material.tabs.TabLayout;

import android.util.Log;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.graphics.Typeface;
import android.widget.Button;

public class Floating extends Service {

    private static WindowManager windowManager;
    private View floatingView;

    private TabLayout tabLayout;
    static Map<String, String> configMap = new HashMap<>(); // Use generics
    public static int REQUEST_OVERLAY_PERMISSION = 5469;

    // Add missing typeface constants
    private static final Typeface DEFAULT_BOLD = Typeface.DEFAULT_BOLD;
    private static final Typeface DEFAULT = Typeface.DEFAULT;

    int screenWidth, screenHeight, type, CheckAttY = 0;
    boolean EnableFakeRecord = false;
    GradientDrawable gdMenuBody, gdAnimation = new GradientDrawable();
    LayoutParams layoutParams;

    public static LayoutParams iconLayoutParams, mainLayoutParams, canvasLayoutParams;
    public static LayoutParams vParams;
    //  public static  View vTouch;

    private String TimeHari;
    private String TimeJam;
    private String TimeMenit;
    private String TimeDetik;

    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout iconLayout;
    public static LinearLayout mainLayout, bodyLayout;
    public static ESPView canvasLayout;

    TextView textTitle;
    RelativeLayout closeLayout, maximizeLayout, minimizeLayout;
    RelativeLayout.LayoutParams closeLayoutParams, maximizeLayoutParams, minimizeLayoutParams;

    //  private int EngChIndex = 0;//1
    private int EspMenuTextthi = 0;
    private static final int currentLanguageIndex = 0;
    private LinearLayout contentLayout;


    private native String ChannelName();

    private native String FeedBackName();

    private native String channellink();

    private native String feedbacklink();

    private native String onlinename();

    public native void Switch(int i);

    public native boolean IsESPActive();

    public static native void DrawOn(ESPView espView, Canvas canvas);


    ImageView iconImg;

    private native String iconenc();

    private final List<LinearLayout> scrollBarContainers = new ArrayList<>();
    private final List<TextView> textViewList = new ArrayList<>();
    private final List<TextView> textViewList2 = new ArrayList<>();
    public static List<TextView> textViewList3 = new ArrayList<>();
    private final List<View> LineViewList = new ArrayList<>();
    private final List<GradientDrawable> comboViewList = new ArrayList<>();

    public static boolean DarkMode;
    private ScrollView scrollView;
    private LinearLayout pageLayout;
    private final int c_Background = Color.argb(255, 242, 241, 247);
    private final int c_Text = Color.BLACK;
    private final int c_Child = Color.WHITE;
    private final int c_Text2 = Color.argb(240, 143, 143, 142);
    private final int c_Line = Color.argb(80, 150, 150, 150);
    public static int c_WidgetsText = Color.BLACK;
    private final int c_Combo = Color.WHITE;

    private static final int MENU_WIDTH_DP = 300;
    private static final int MENU_HEIGHT_DP = 400;
    private static final int ICON_SIZE_DP = 64;


    public static boolean LanguageTest;
    //public static boolean MENULANGUAGE;


    String[] listTab = {"ESP", "ITEMS", "AIM", "SKIN"};
    LinearLayout[] pageLayouts = new LinearLayout[listTab.length];
    LayoutParams params;

    int lastSelectedPage = 0;
    static boolean isBullet;
    boolean CheckAtt;
    SharedPreferences configPrefs;


    int ToggleON = Color.WHITE;
    int ToggleOFF = Color.LTGRAY;
    boolean isMaximized = false;
    int lastMaximizedX = 0, lastMaximizedY = 0;
    int lastMaximizedW = 0, lastMaximizedH = 0;
    int action;
    int layoutWidth;
    int layoutHeight;
    int iconSize;
    int iconSize2;
    int menuButtonSize;
    int tabWidth;
    int tabHeight;

    int MENU_TEXT_COLOR = Color.parseColor("#FFFFFFFF");
    int MENU_LIST_STROKE = Color.argb(255, 200, 100, 0);

    private native boolean IsHideEsp();

    private boolean SaveKey;
    int RadioColor = Color.parseColor("#FFFF9700");
    int MENU_BG_COLOR = Color.parseColor("#fff7f7f7"); // #AARRGGBB

    static boolean isHIDE;
    int Storage_Permission = 142;
    TextView mTitle;

    float mediumSize = 5.0f;

    private native String cfg();

    private native void onSendConfig(String s, String v);

    private Thread thread;
    private boolean isRunning;
    private Paint paint;
    private long startTime;
    private int frames;

    long days;
    long hours;
    long minutes;
    long seconds;

    @SuppressLint("StaticFieldLeak")
    public static Context g_context;
    private final int fpsViewId = View.generateViewId();
    private final long lastTime = 0;
    private final int frameCount = 0;
    private TextView fpsTextView;

    // Add fields for dynamic UI references for language switching
    private TextView[] dynamicTextViews;
    private Button[] dynamicButtons;
    private final boolean[] stealthModeActive = {false};

    // Track current tab for proper restoration when showing interface
    private int currentTabIndex = 0;

    // Layout references for proper tab navigation
    private LinearLayout container;
    private LinearLayout layoutBypasss;
    private LinearLayout layoutEsp;
    private LinearLayout layoutCheat;
    private LinearLayout layoutSet;
    private Button[] tabButtons;
    private Button floatingHomeIcon;

    // Language switching system - track all translatable elements
    private Button espToggleButton;
    private final List<TranslatableElement> translatableElements = new ArrayList<>();

    // Helper class to track translatable UI elements
        private record TranslatableElement(TextView textView, String englishText, String chineseText) {
    }

    /**
     * Initialize service state for patch integration
     */
    private void initializeServiceState() {
        try {
            Log.d("Floating", "Initializing floating service state");

            // Verify authentication before starting service features
            if (!isUserAuthenticated()) {
                Log.w("Floating", "User not authenticated, limiting service functionality");
                return;
            }

            // Synchronize stealth mode state between Java and C++
            synchronizeESPState();

            // Update visual state of tabs based on stealth mode
            updateTabsVisualState();

            // Service is ready for patch integration
            Log.d("Floating", "Floating service initialized and ready for patches");

        } catch (Exception e) {
            Log.e("Floating", "Error initializing service state", e);
        }
    }

    /**
     * Check if stealth mode is active and mod features should be enabled
     */
    public boolean isStealthModeActive() {
        return stealthModeActive[0] && isUserAuthenticated();
    }

    /**
     * Disable all tab functionality when stealth mode is off
     */
    private void enforceStealthModeRestrictions() {
        if (!isStealthModeActive()) {
            // Show message that stealth mode must be enabled first
            Toast.makeText(this, getEspMenuText("Enable Stealth Mode first to access mod features", "请先启用隐身模式以访问功能"), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update visual state of all tabs based on stealth mode status
     */
    private void updateTabsVisualState() {
        boolean stealthActive = isStealthModeActive();

        // Update all page layouts to reflect stealth mode state
        for (LinearLayout layout : pageLayouts) {
            if (layout != null) {
                // Set alpha to indicate disabled state
                layout.setAlpha(stealthActive ? 1.0f : 0.5f);

                // Disable/enable all child views in the tab
                setTabContentEnabled(layout, stealthActive);
            }
        }
    }

    /**
     * Recursively enable/disable all views in a tab layout
     */
    private void setTabContentEnabled(ViewGroup viewGroup, boolean enabled) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            // Skip the stealth mode button itself
            if (child == espToggleButton) {
                continue;
            }

            if (child instanceof ViewGroup) {
                setTabContentEnabled((ViewGroup) child, enabled);
            } else {
                child.setEnabled(enabled);
                child.setAlpha(enabled ? 1.0f : 0.5f);
            }
        }
    }

    /**
     * Synchronize stealth mode state between Java and C++
     */
    private void synchronizeESPState() {
        try {
            if (!isUserAuthenticated()) {
                // Clear stealth mode state if not authenticated
                stealthModeActive[0] = false;
                return;
            }

            // Check C++ stealth mode state and synchronize with Java
            boolean cppStealthActive = IsESPActive(); // This will be updated to check all mod features
            stealthModeActive[0] = cppStealthActive;

            Log.d("Floating", "Stealth mode state synchronized - Active: " + stealthModeActive[0]);

        } catch (Exception e) {
            Log.e("Floating", "Error synchronizing ESP state", e);
            // Default to inactive on error
            stealthModeActive[0] = false;
        }
    }

    /**
     * Check if user is authenticated and mod features should be enabled
     * Simple check using stored license key
     */
    private boolean isUserAuthenticated() {
        try {
            return com.bearmod.activity.LoginActivity.hasValidKey(this);
        } catch (Exception e) {
            Log.e("Floating", "Authentication check error", e);
            return false;
        }
    }
    public static void hideesp() {
        RecorderFakeUtils.setFakeRecorderWindowLayoutParams(mainLayoutParams, iconLayoutParams, canvasLayoutParams, windowManager, mainLayout, iconLayout, canvasLayout, g_context);

    }

    public static void stopHideesp() {
        RecorderFakeUtils.unsetFakeRecorderWindowLayoutParams(mainLayoutParams, iconLayoutParams, canvasLayoutParams, windowManager, mainLayout, iconLayout, canvasLayout, g_context);

    }

    private Boolean GetBoolean(String str) {
        boolean z = configMap.get(str) != null && Integer.parseInt(Objects.requireNonNull(configMap.get(str))) == 1;
        return z;
    }

    private Integer GetInteger(String str) {
        return configMap.get(str) != null ? Integer.parseInt(Objects.requireNonNull(configMap.get(str))) : 0;
    }

    Date time;
    SimpleDateFormat formatter;
    SimpleDateFormat formatter2;

    void CreateCanvas() {
        canvasLayoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                getLayoutType(),
                LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        canvasLayoutParams.gravity = Gravity.TOP | Gravity.START;
        canvasLayoutParams.x = 0;
        canvasLayoutParams.y = 0;
        if (Build.VERSION.SDK_INT >= 30) {
            canvasLayoutParams.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        canvasLayout = new ESPView(this);
        windowManager.addView(canvasLayout, canvasLayoutParams);
    }

    public int getLayoutType() {
        int LAYOUT_FLAG;

        LAYOUT_FLAG = LayoutParams.TYPE_APPLICATION_OVERLAY;
        return LAYOUT_FLAG;
    }

    private boolean isNotInGame() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    private void Thread() {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (isNotInGame()) {
            try {
                if (windowManager != null) {
                    windowManager.removeView(mainLayout);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            mainLayoutParams = new LayoutParams(layoutWidth, layoutHeight, LayoutParams.TYPE_APPLICATION_OVERLAY, LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

            try {
                windowManager.addView(mainLayout, mainLayoutParams);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    private void LoadConfiguration(/*String customPat*/) {
        try {
            File file;

            file = new File(getFilesDir(), "NRG_SaveFile.cfg");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                String[] split = readLine.split(" = ");
                if (split.length == 2) {
                    configMap.put(split[0], split[1]);
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSendConfig(String split, Object parseInt) {
        configMap.put(split, parseInt.toString());
        // onSendConfig(split, parseInt.toString());
        parseInt.toString();
        configMap.put(split, String.valueOf(parseInt));
    }

    private void SaveConfiguration(/*String customPath*/) {
        try {
            File file;

            file = new File(getFilesDir(), "NRG_SaveFile.cfg");
            //    }

            PrintWriter printWriter = new PrintWriter(new FileOutputStream(file), true);
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                printWriter.println(entry.getKey() + " = " + entry.getValue());
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateConfiguration2(String s, Object v) {
        try {
            configMap.put(s, v.toString());
            onSendConfig(s, v.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void UpdateConfiguration(String s, Object v) {
        try {
            configMap.put(s, v.toString()); // Update local configMap
            onSendConfig(s, v.toString()); // Send to native if needed

            SharedPreferences.Editor configEditor = configPrefs.edit();
            configEditor.putString(s, v.toString());
            configEditor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class CanvasView extends View {
        public CanvasView(Context context) {
            super(context);

            g_context = context;

        }


    }

    private int seconds2 = 0;

    @Override
    public void onDestroy() {
        GetBoolean("RECORDER_HIDE");

        if (floatingView != null && floatingView.getParent() != null) {
            windowManager.removeView(floatingView);
        }

        if (mUpdateCanvas.isAlive()) {
            mUpdateCanvas.interrupt();
        }
        if (mUpdateThread.isAlive()) {
            mUpdateThread.interrupt();
        }

        if (iconLayout != null && iconLayout.getParent() != null) {
            windowManager.removeView(iconLayout);
        }
        if (mainLayout != null && mainLayout.getParent() != null) {
            windowManager.removeView(mainLayout);
        }
        if (canvasLayout != null && canvasLayout.getParent() != null) {
            windowManager.removeView(canvasLayout);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    public static Floating instance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Hard gate: do not run service if not authenticated
        if (!com.bearmod.activity.LoginActivity.hasValidKey(this)) {
            Log.w("Floating", "Unauthorized service start attempt - stopping service");
            stopSelf();
            return;
        }

        instance = this;
        configPrefs = getSharedPreferences("config", MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Initialize service state for patch integration
        initializeServiceState();

        time = new Date();
        formatter = new SimpleDateFormat(" HH:mm:ss", Locale.getDefault());
        formatter2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Modern API 30+ only: get screen size
        android.view.WindowMetrics metrics = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            metrics = windowManager.getCurrentWindowMetrics();
        }
        android.graphics.Rect bounds = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bounds = metrics.getBounds();
        }
        assert bounds != null;
        screenWidth = bounds.width();
        screenHeight = bounds.height();

        layoutWidth = convertSizeToDp(450);
        layoutHeight = convertSizeToDp(330);
        iconSize = convertSizeToDp(64);
        iconSize2 = 150;

        menuButtonSize = convertSizeToDp(30);
        tabWidth = convertSizeToDp(0);
        tabHeight = convertSizeToDp(0);
        type = 2038;

        GetBoolean("RECORDER_HIDE");

        CreateCanvas();
        // Set overlay FPS based on device refresh rate
        int deviceFps = GetDeviceMaxFps();
        if (canvasLayout != null) {
            canvasLayout.setTargetFps(deviceFps);
        }
        // Optionally, check for problematic ROMs and enable compatibility mode
        if (!RecorderFakeUtils.isEmui() && !RecorderFakeUtils.isVivo() && !RecorderFakeUtils.isOppo() && !RecorderFakeUtils.isMiui()) {
            RecorderFakeUtils.check("Blackshark");
        }
        CreateLayout();
        CreateIcon();

        // Ensure mainLayout is not already added
        if (mainLayout.getParent() != null) {
            windowManager.removeView(mainLayout);
        }
        windowManager.addView(mainLayout, mainLayoutParams);
        mainLayout.setVisibility(View.GONE); // Start hidden

        // 6. Configuration defaults
        initDefaultConfigurations();
        // setLanguageBasedOnSystemLocale(); // REMOVED: Language is now user-controlled only
    }

    private void initDefaultConfigurations() {
        UpdateConfiguration("AIM::TRIGGER1", (byte) 1);
        UpdateConfiguration("AIM::TARGET1", (byte) 1);
        UpdateConfiguration("ESP::BOXTYPE1", (byte) 1);
        UpdateConfiguration("AIM_MOD1", (byte) 1);
        UpdateConfiguration("SMOOT::HNESS1", (byte) 1);
        UpdateConfiguration("RADAR::SIZE", (byte) 60);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // setLanguageBasedOnSystemLocale(); // REMOVED: Language is now user-controlled only
        // translateMenuElements(); // No need to auto-translate on config change
    }

    private String GetString(String key, String defaultValue) {
        String value = configMap.get(key);
        return value != null ? value : defaultValue;
    }

    String getEspMenuText(String engText, String cnText) {
        return switch (EspMenuTextthi) {
            case 1 -> cnText;
            default -> engText;
        };
    }

    // Helper method to register translatable elements
    private void registerTranslatableElement(TextView textView, String englishText, String chineseText) {
        if (textView != null) {
            translatableElements.add(new TranslatableElement(textView, englishText, chineseText));
        }
    }

    private void translateMenuElements() {
        // Update main title
        if (textTitle != null) {
            textTitle.setText(getEspMenuText("BearMod", "熊模组"));
        }

        // Update ESP toggle button with authentication status and C++ state sync
        if (espToggleButton != null) {
            boolean isAuthenticated = isUserAuthenticated();

            // Synchronize stealth mode state with C++ if authenticated
            if (isAuthenticated) {
                try {
                    boolean cppStealthActive = IsESPActive(); // Will be updated to check all mod features
                    stealthModeActive[0] = cppStealthActive;
                } catch (Exception e) {
                    Log.e("Floating", "Error checking C++ stealth mode state in translateMenuElements", e);
                }
            }

            if (stealthModeActive[0]) {
                espToggleButton.setText(getEspMenuText("STOP STEALTH MODE", "关闭隐身模式"));
            } else {
                if (isAuthenticated) {
                    espToggleButton.setText(getEspMenuText("START STEALTH MODE", "启动隐身模式"));
                } else {
                    espToggleButton.setText(getEspMenuText("AUTHENTICATION REQUIRED", "需要身份验证"));
                }
            }
        }

        // Update all registered translatable elements (switches, labels, etc.)
        for (TranslatableElement element : translatableElements) {
            if (element.textView != null) {
                String newText = getEspMenuText(element.englishText, element.chineseText);
                element.textView.setText(newText);
            }
        }
    }


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    void CreateLayout() {
        LoadConfiguration();

        int windowType = LayoutParams.TYPE_APPLICATION_OVERLAY;
        mainLayoutParams = new LayoutParams(
                layoutWidth,
                layoutHeight,
                windowType,
                LayoutParams.FLAG_NOT_FOCUSABLE |
                        LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );


        String[] languages = {"English", "中国人"};
        if (configMap.containsKey("SETTING_MENU")) {
            switch (GetInteger("SETTING_MENU")) {
                case 0:
                    EspMenuTextthi = 0;

                    break;
                case 1:
                    EspMenuTextthi = 1;


                    break;
            }
        }

        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        for (int i = 0; i < languages.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(languages[i]);
            radioButton.setId(View.generateViewId());
            radioButton.setChecked(GetInteger("SETTING_MENU") == i);
            radioGroup.addView(radioButton);
        }

        mainLayoutParams = new WindowManager.LayoutParams(layoutWidth, layoutHeight, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.RGBA_8888);
        mainLayoutParams.x = 150;
        mainLayoutParams.y = 150;
        mainLayoutParams.gravity = Gravity.START | Gravity.TOP;

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // iOS-inspired main layout background
        GradientDrawable mainBg = new GradientDrawable();
        mainBg.setColor(Color.parseColor("#1C1C1E")); // iOS dark background
        mainBg.setStroke(convertSizeToDp(1), Color.parseColor("#38383A")); // iOS border
        mainBg.setCornerRadius(convertSizeToDp(16)); // iOS corner radius
        mainLayout.setBackground(mainBg);

        // Use RelativeLayout for header to properly position title and language switcher
        RelativeLayout headerLayout = new RelativeLayout(this);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, this.dp(40)));
        headerLayout.setPadding(convertSizeToDp(16), convertSizeToDp(12), convertSizeToDp(16), convertSizeToDp(12));
        headerLayout.setClickable(true);
        headerLayout.setFocusable(true);
        headerLayout.setFocusableInTouchMode(true);

        // iOS-style header background with gradient
        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColors(new int[]{Color.parseColor("#2C2C2E"), Color.parseColor("#1C1C1E")});
        headerBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        headerBg.setCornerRadii(new float[]{
            convertSizeToDp(16), convertSizeToDp(16), // top-left
            convertSizeToDp(16), convertSizeToDp(16), // top-right
            0, 0, // bottom-right
            0, 0  // bottom-left
        });
        headerLayout.setBackground(headerBg);

        mainLayout.addView(headerLayout);

        new GestureDetector(Floating.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                mainLayout.setVisibility(View.GONE);
                iconLayout.setVisibility(View.VISIBLE);
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                Toast.makeText(Floating.this, "Double tap to close the menu.", Toast.LENGTH_LONG).show();
                return super.onSingleTapConfirmed(e);
            }
        });
        // Modern iOS-inspired touch listener with haptic feedback and smooth interactions
        View.OnTouchListener onTitleListener = new ModernIOSTouchListener();

        TextView textView = new TextView(this);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textView.setLayoutParams(titleParams);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setText(onlinename());
        textView.setTextColor(Color.parseColor("#FFFFFF")); // iOS white
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setShadowLayer(convertSizeToDp(2), 0, convertSizeToDp(1), Color.parseColor("#000000"));
        headerLayout.addView(textView);
        headerLayout.setOnTouchListener(onTitleListener);
        textView.setOnTouchListener(onTitleListener);

        // Assign to textTitle field for translation support
        textTitle = textView;

        // Compact segmented language control in header (right-aligned)
        LinearLayout languageSegment = createLanguageSegment(new String[]{"English", "中国人"}, GetInteger("SETTING_MENU"), index -> {
            UpdateConfiguration2("SETTING_MENU", index);
            SaveConfiguration();
            EspMenuTextthi = index;
            translateMenuElements();
        });
        RelativeLayout.LayoutParams langParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        langParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        langParams.addRule(RelativeLayout.CENTER_VERTICAL);
        languageSegment.setLayoutParams(langParams);
        headerLayout.addView(languageSegment);

        // Content root as FrameLayout to support overlayed floating elements
        final FrameLayout main = new FrameLayout(this);
        main.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        main.setClipToPadding(false);
        main.setClipChildren(false);

        // Create floating home icon container (overlay) - positioned at bottom
        RelativeLayout floatingIconContainer = new RelativeLayout(this);
        FrameLayout.LayoutParams floatingContainerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        floatingContainerParams.gravity = Gravity.BOTTOM | Gravity.START;
        floatingContainerParams.setMargins(convertSizeToDp(16), 0, convertSizeToDp(16), convertSizeToDp(16));
        floatingIconContainer.setLayoutParams(floatingContainerParams);
        floatingIconContainer.setClipToPadding(false);
        floatingIconContainer.setClipChildren(false);

        // Floating home icon button
        floatingHomeIcon = new Button(this);
        RelativeLayout.LayoutParams homeIconParams = new RelativeLayout.LayoutParams(
            convertSizeToDp(48), convertSizeToDp(48));
        homeIconParams.setMargins(convertSizeToDp(16), 0, 0, convertSizeToDp(16));
        homeIconParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        homeIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        floatingHomeIcon.setLayoutParams(homeIconParams);

        // iOS-style floating icon design
        GradientDrawable homeIconBg = new GradientDrawable();
        homeIconBg.setShape(GradientDrawable.OVAL);
        homeIconBg.setColors(new int[]{Color.parseColor("#007AFF"), Color.parseColor("#0051D5")});
        homeIconBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        floatingHomeIcon.setBackground(homeIconBg);
        floatingHomeIcon.setText("☰");
        floatingHomeIcon.setTextColor(Color.WHITE);
        floatingHomeIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        floatingHomeIcon.setTypeface(Typeface.DEFAULT_BOLD);
        floatingHomeIcon.setElevation(convertSizeToDp(4));

        // Backdrop behind expanded tab menu (subtle shadow/blur-like)
        final View tabBackdrop = new View(this);
        FrameLayout.LayoutParams backdropParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, convertSizeToDp(64));
        backdropParams.gravity = Gravity.BOTTOM | Gravity.START;
        backdropParams.setMargins(convertSizeToDp(8), 0, convertSizeToDp(8), convertSizeToDp(8));
        tabBackdrop.setLayoutParams(backdropParams);
        tabBackdrop.setBackgroundColor(Color.parseColor("#26000000")); // ~15% black
        tabBackdrop.setVisibility(View.GONE);

        // Create expandable tab menu (initially hidden) as overlay row
        final LinearLayout expandableTabMenu = new LinearLayout(this);
        RelativeLayout.LayoutParams tabMenuParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, convertSizeToDp(48));
        tabMenuParams.setMargins(convertSizeToDp(64), 0, 0, convertSizeToDp(16));
        tabMenuParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        expandableTabMenu.setLayoutParams(tabMenuParams);
        expandableTabMenu.setOrientation(LinearLayout.HORIZONTAL);
        expandableTabMenu.setVisibility(View.GONE);
        expandableTabMenu.setPadding(convertSizeToDp(8), 0, convertSizeToDp(8), 0);
        expandableTabMenu.setClipToPadding(false);
        expandableTabMenu.setClipChildren(false);

        // Create tab icons (removed Hide tab - now handled by floating home icon)
        String[] tabIcons = {"⌂", "👁", "🎯", "👕"};
        String[] tabNames = {"Main", "ESP", "Aim", "Skin"};
        tabButtons = new Button[4];

        for (int i = 0; i < tabIcons.length; i++) {
            Button tabIcon = new Button(this);
            LinearLayout.LayoutParams tabIconParams = new LinearLayout.LayoutParams(
                convertSizeToDp(40), convertSizeToDp(40));
            tabIconParams.setMargins(convertSizeToDp(4), 0, convertSizeToDp(4), 0);
            tabIcon.setLayoutParams(tabIconParams);

            // iOS-style tab icon design
            GradientDrawable tabIconBg = new GradientDrawable();
            tabIconBg.setShape(GradientDrawable.OVAL);
            tabIconBg.setColor(Color.parseColor("#3A3A3C"));
            tabIconBg.setStroke(convertSizeToDp(1), Color.parseColor("#48484A"));
            tabIcon.setBackground(tabIconBg);
            tabIcon.setText(tabIcons[i]);
            tabIcon.setTextColor(Color.parseColor("#8E8E93"));
            tabIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tabIcon.setTag(i); // Store index for click handling
            tabIcon.setElevation(convertSizeToDp(2));

            tabButtons[i] = tabIcon;
            expandableTabMenu.addView(tabIcon);
        }

        floatingIconContainer.addView(floatingHomeIcon);
        floatingIconContainer.addView(expandableTabMenu);

        // Initialize tab selection to show Main tab as selected
        updateFloatingTabSelection(tabButtons, 0);

        // Content column occupies full size under header
        final LinearLayout vbar = new LinearLayout(this);
        vbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vbar.setOrientation(LinearLayout.VERTICAL);

        final ScrollView sc = new ScrollView(this);
        sc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sc.setFillViewport(true);

        container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);

        layoutBypasss = new LinearLayout(instance);
        layoutBypasss.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutBypasss.setOrientation(LinearLayout.VERTICAL);
        layoutBypasss.setPadding(15, 15, 15, 15);

        layoutEsp = new LinearLayout(this);
        layoutEsp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutEsp.setOrientation(LinearLayout.VERTICAL);
        layoutEsp.setPadding(15, 15, 15, 15);

        LayoutParams tabparam2 = new LayoutParams(dp(100), dp(40));

        layoutCheat = new LinearLayout(this);
        layoutCheat.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutCheat.setOrientation(LinearLayout.VERTICAL);
        layoutCheat.setPadding(15, 15, 15, 15);

        layoutSet = new LinearLayout(this);
        layoutSet.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutSet.setOrientation(LinearLayout.VERTICAL);
        layoutSet.setPadding(15, 15, 15, 15);

        final LinearLayout layoutAcc = new LinearLayout(this);
        layoutAcc.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutAcc.setOrientation(LinearLayout.VERTICAL);
        layoutAcc.setPadding(15, 15, 15, 15);

        mainLayout.addView(main);
        // Add content first so overlay floats above
        main.addView(vbar);
        vbar.addView(sc);
        sc.addView(container);
        container.addView(layoutBypasss);
        // Overlay elements added last for higher Z order
        main.addView(tabBackdrop);
        main.addView(floatingIconContainer);

        // Floating menu functionality
        final boolean[] isMenuExpanded = {false};

        // Floating home icon click listener with dynamic state and hide/show functionality
        floatingHomeIcon.setOnClickListener(view -> {
            triggerHapticFeedback(HapticFeedbackType.MEDIUM);
            animateTabSelection(view);

            // Check if interface is currently hidden
            if (mainLayout.getVisibility() == View.GONE) {
                // Show interface and return to current tab
                mainLayout.setVisibility(View.VISIBLE);
                iconLayout.setVisibility(View.GONE);

                // Reset icon to hamburger menu and ensure menu is collapsed
                floatingHomeIcon.setText("☰");
                isMenuExpanded[0] = false;

                // Restore the current tab content
                container.removeAllViews();
                switch (currentTabIndex) {
                    case 0: // Main
                        container.addView(layoutBypasss);
                        break;
                    case 1: // ESP
                        container.addView(layoutEsp);
                        break;
                    case 2: // Aim
                        container.addView(layoutCheat);
                        break;
                    case 3: // Skin
                        container.addView(layoutSet);
                        break;
                }
                updateFloatingTabSelection(tabButtons, currentTabIndex);
                return;
            }

            if (!isMenuExpanded[0]) {
                // Expand menu with backdrop and change icon to X
                floatingHomeIcon.setText("✕");

                tabBackdrop.setVisibility(View.VISIBLE);
                tabBackdrop.setAlpha(0f);
                tabBackdrop.animate().alpha(1f).setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator()).start();

                expandableTabMenu.setVisibility(View.VISIBLE);
                expandableTabMenu.setAlpha(0f);
                expandableTabMenu.setTranslationX(-convertSizeToDp(100));
                expandableTabMenu.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
                isMenuExpanded[0] = true;
            } else {
                // When showing X icon, clicking should hide the interface
                mainLayout.setVisibility(View.GONE);
                iconLayout.setVisibility(View.VISIBLE);

                // Reset icon state for next time
                floatingHomeIcon.setText("☰");
                isMenuExpanded[0] = false;

                // Hide menu elements
                tabBackdrop.setVisibility(View.GONE);
                expandableTabMenu.setVisibility(View.GONE);
            }
        });



        // Tab icon click listeners
        for (int i = 0; i < tabButtons.length; i++) {
            final int tabIndex = i;
            tabButtons[i].setOnClickListener(view -> {
                triggerHapticFeedback(HapticFeedbackType.LIGHT);
                animateTabSelection(view);

                // Update tab icon appearance
                updateFloatingTabSelection(tabButtons, tabIndex);

                // Collapse menu after selection and reset icon to hamburger
                floatingHomeIcon.setText("☰");

                tabBackdrop.animate().alpha(0f).setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> tabBackdrop.setVisibility(View.GONE)).start();

                expandableTabMenu.animate()
                    .alpha(0f)
                    .translationX(-convertSizeToDp(100))
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> expandableTabMenu.setVisibility(View.GONE))
                    .start();
                isMenuExpanded[0] = false;

                // Handle tab navigation and track current tab
                currentTabIndex = tabIndex;
                container.removeAllViews();
                switch (tabIndex) {
                    case 0: // Main
                        container.addView(layoutBypasss);
                        break;
                    case 1: // ESP
                        container.addView(layoutEsp);
                        break;
                    case 2: // Aim
                        container.addView(layoutCheat);
                        break;
                    case 3: // Skin
                        container.addView(layoutSet);
                        break;
                }

                if (contentLayout != null) {
                    contentLayout.removeAllViews();
                }
            });
        }

        // Old tab listeners removed - using new floating menu system


        windowManager.addView(mainLayout, mainLayoutParams);

        // iOS-style ESP toggle button (compact)
        espToggleButton = new Button(this);
        espToggleButton.setText(getEspMenuText("START ESP OVERLAY", "启动透视悬浮窗"));
        espToggleButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        espToggleButton.setTextColor(Color.WHITE);
        espToggleButton.setTypeface(Typeface.DEFAULT_BOLD);
        espToggleButton.setAllCaps(false); // iOS doesn't use all caps

        // iOS-style button background (subtle)
        GradientDrawable espButtonBg = new GradientDrawable();
        espButtonBg.setColor(Color.parseColor("#FF3B30")); // iOS system red
        espButtonBg.setCornerRadius(convertSizeToDp(12));
        espButtonBg.setStroke(convertSizeToDp(1), Color.parseColor("#D70015"));
        espToggleButton.setBackground(espButtonBg);

        LinearLayout.LayoutParams espBtnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertSizeToDp(40));
        espBtnParams.setMargins(convertSizeToDp(8), convertSizeToDp(8), convertSizeToDp(8), convertSizeToDp(8));
        espToggleButton.setLayoutParams(espBtnParams);

        espToggleButton.setElevation(convertSizeToDp(2));

        // Track ESP overlay state (using class field)

        // iOS-style button state updater with authentication status
        Runnable updateEspButton = () -> {
            GradientDrawable buttonBg = new GradientDrawable();
            buttonBg.setCornerRadius(convertSizeToDp(12));

            // Check authentication status for button appearance
            boolean isAuthenticated = isUserAuthenticated();

            // Synchronize stealth mode state with C++ if authenticated
            if (isAuthenticated) {
                try {
                    boolean cppStealthActive = IsESPActive(); // Will be updated to check all mod features
                    stealthModeActive[0] = cppStealthActive;
                } catch (Exception e) {
                    Log.e("Floating", "Error checking C++ stealth mode state", e);
                }
            }

            if (stealthModeActive[0]) {
                espToggleButton.setText(getEspMenuText("STOP STEALTH MODE", "关闭隐身模式"));
                buttonBg.setColor(Color.parseColor("#34C759")); // iOS system green
                buttonBg.setStroke(convertSizeToDp(1), Color.parseColor("#248A3D"));
            } else {
                if (isAuthenticated) {
                    espToggleButton.setText(getEspMenuText("START STEALTH MODE", "启动隐身模式"));
                    buttonBg.setColor(Color.parseColor("#FF3B30")); // iOS system red
                    buttonBg.setStroke(convertSizeToDp(1), Color.parseColor("#D70015"));
                } else {
                    espToggleButton.setText(getEspMenuText("AUTHENTICATION REQUIRED", "需要身份验证"));
                    buttonBg.setColor(Color.parseColor("#8E8E93")); // iOS system gray (disabled)
                    buttonBg.setStroke(convertSizeToDp(1), Color.parseColor("#6D6D70"));
                }
            }
            espToggleButton.setBackground(buttonBg);
        };

        espToggleButton.setOnClickListener(v -> {
            // iOS-style button press animation
            triggerHapticFeedback(HapticFeedbackType.MEDIUM);
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
                })
                .start();

            // AUTHENTICATION GATING: Check if user is authenticated before allowing ESP features
            if (!isUserAuthenticated()) {
                triggerHapticFeedback(HapticFeedbackType.ERROR);
                Toast.makeText(this, getEspMenuText(
                    "Authentication Required! Please restart app and enter valid license key.",
                    "需要身份验证！请重启应用并输入有效许可证密钥。"
                ), Toast.LENGTH_LONG).show();

                Log.w("Floating", "ESP toggle blocked - user not authenticated");
                return; // Block ESP activation
            }

            // Check authentication before allowing stealth mode toggle
            if (!isUserAuthenticated()) {
                triggerHapticFeedback(HapticFeedbackType.ERROR);
                Toast.makeText(this, getEspMenuText("Authentication required to use stealth mode", "需要身份验证才能使用隐身模式"), Toast.LENGTH_LONG).show();
                return;
            }

            // Define all mod feature keys across all tabs
            String[] espKeys = new String[]{
                "ESP_LINE", "ESP_BONE", "ESP_INFO", "ESP_WEAPON", "ESP_WARNING", "ESP_ALERT", "ESP_RADAR", "ESP_IGNOREBOTS"
            };
            String[] aimKeys = new String[]{
                "NRG_AIMBOT", "AIM_MOD1", "AIM_MOD2", "AIM_MOD3", "AIM_VISCHECK", "AIM_KNOCKED", "AIM_IGNOREBOTS", "AIM::TRIGGER1"
            };
            String[] skinKeys = new String[]{
                "SKIN_ENABLE"
            };

            if (!stealthModeActive[0]) {
                // Enable ALL mod functionality (stealth mode ON)

                // Enable ESP features
                for (String key : espKeys) {
                    UpdateConfiguration2(key, 1);
                }

                // Enable core AIM features (user can fine-tune individual settings)
                UpdateConfiguration2("NRG_AIMBOT", 1);
                UpdateConfiguration2("AIM_MOD1", 1);
                UpdateConfiguration2("AIM_VISCHECK", 1);
                UpdateConfiguration2("AIM_IGNOREBOTS", 1);
                // Keep knocked players disabled by default
                UpdateConfiguration2("AIM_KNOCKED", 0);

                // Enable SKIN features
                UpdateConfiguration2("SKIN_ENABLE", 1);

                SaveConfiguration();

                // Call C++ stealth mode switch to enable all features
                Switch(1);

                translateMenuElements();
                updateTabsVisualState(); // Update visual state of all tabs
                triggerHapticFeedback(HapticFeedbackType.SUCCESS);
                Toast.makeText(this, getEspMenuText("Stealth Mode Started! All mod features enabled.", "隐身模式已启动！所有功能已启用。"), Toast.LENGTH_SHORT).show();
                stealthModeActive[0] = true;
            } else {
                // Disable ALL mod functionality (stealth mode OFF)

                // Disable ESP features
                for (String key : espKeys) {
                    UpdateConfiguration2(key, 0);
                }

                // Disable AIM features
                for (String key : aimKeys) {
                    UpdateConfiguration2(key, 0);
                }

                // Disable SKIN features
                for (String key : skinKeys) {
                    UpdateConfiguration2(key, 0);
                }

                SaveConfiguration();

                // Call C++ stealth mode switch to disable all features
                Switch(0);

                translateMenuElements();
                updateTabsVisualState(); // Update visual state of all tabs
                triggerHapticFeedback(HapticFeedbackType.WARNING);
                Toast.makeText(this, getEspMenuText("Stealth Mode Stopped! All mod features disabled.", "隐身模式已关闭！所有功能已禁用。"), Toast.LENGTH_SHORT).show();
                stealthModeActive[0] = false;
            }
            updateEspButton.run();
        });

        // Initialize button state
        updateEspButton.run();




        addCheckboxWithSwitch(layoutBypasss, (buttonView, isChecked) -> {
            if (isChecked) {
                RecorderFakeUtils.setFakeRecorderWindowLayoutParams(mainLayoutParams, iconLayoutParams, canvasLayoutParams, windowManager, mainLayout, iconLayout, canvasLayout, Floating.this);
            } else {
                RecorderFakeUtils.unsetFakeRecorderWindowLayoutParams(mainLayoutParams, iconLayoutParams, canvasLayoutParams, windowManager, mainLayout, iconLayout, canvasLayout, Floating.this);
            }
        });

        // Add ESP button at the bottom of the main tab content to avoid overlay conflicts
        layoutBypasss.addView(espToggleButton);



      addSwitch2("Line", "线",
      GetBoolean("ESP_LINE"),
      (buttonView, isChecked) -> UpdateConfiguration2("ESP_LINE", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutEsp);


addSwitch2("Bone", "骨骼",
      GetBoolean("ESP_BONE"),
      (buttonView, isChecked) -> UpdateConfiguration2("ESP_BONE", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutEsp);


addSwitch2("Info", "玩家信息",
      GetBoolean("ESP_INFO"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_INFO", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutEsp);


addSwitch2("Weapon", "玩家武器",
      GetBoolean("ESP_WEAPON"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_WEAPON", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutEsp);


addSwitch2("Grenade Warning", "手雷预警",
      GetBoolean("ESP_WARNING"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_WARNING", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutEsp);


addSwitch2("360° Alert", "360° 警报",

      GetBoolean("ESP_ALERT"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_ALERT", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutEsp);

addSwitch2("Radar MAP", "雷达地图",

      GetBoolean("ESP_RADAR"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_RADAR", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutEsp);

addSwitch2("IgnoreBot-ESP", "忽略机器人",

      GetBoolean("ESP_IGNOREBOTS"), (buttonView, isChecked) -> UpdateConfiguration2("ESP_IGNOREBOTS", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutEsp);


AddSeekbar2("RadarMAP-Size", "雷达地图大小", 60, 350, GetInteger("RADAR_SIZE"), new SeekBar.OnSeekBarChangeListener() {
  @Override
  //   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
  public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
      UpdateConfiguration2("RADAR_SIZE", i);
      //  UpdateConfiguration2("RADAR_SIZE", progress);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutEsp);


addSwitch2("AimBot", "自瞄",
      GetBoolean("NRG_AIMBOT"), (buttonView, isChecked) -> UpdateConfiguration2("NRG_AIMBOT", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },

      layoutCheat);

addCombo("Aim-Target", "瞄准目标", new String[]{getEspMenuText("Head", "头"), getEspMenuText("Body", "身体")}, "AIM_TARGET", layoutCheat);
addCombo("Aim-Trigger", "瞄准扳机", new String[]{getEspMenuText("Shoot", "射击"), getEspMenuText("Scope", "开镜"), getEspMenuText("Both", "两个都")}, "AIM_TRIGGER", layoutCheat);


addSwitch2("IgnoreBot", "瞄准-忽略机器人",
      GetBoolean("AIM_IGNOREBOTS"), (buttonView, isChecked) -> UpdateConfiguration2("AIM_IGNOREBOTS", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutCheat);


addSwitch2("Aim-Knocked", "瞄准击倒",
      GetBoolean("AIM_KNOCKED"), (buttonView, isChecked) -> UpdateConfiguration2("AIM_KNOCKED", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutCheat);


addSwitch2("iPad View", "iPad 视图",

      GetBoolean("MEMORY_WIDEVIEW"), (buttonView, isChecked) -> UpdateConfiguration2("MEMORY_WIDEVIEW", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutCheat);


addSwitch2("VisiCheck", "掩体预判",

      GetBoolean("AIM_VISCHECK"), (buttonView, isChecked) -> UpdateConfiguration2("AIM_VISCHECK", isChecked ? 1 : 0),


      view -> {
          SaveConfiguration();
      },
      layoutCheat);


addSwitch2("Aim-Recoil", "自动瞄准控制后坐力",

      GetBoolean("RECOI_LCOMPARISON"), (buttonView, isChecked) -> UpdateConfiguration2("RECOI_LCOMPARISON", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutCheat);


AddSeekbar2("RecoilSize", "自瞄控制后坐力大小", 0, 5, GetInteger("RECOIL_SIZE") == 0 ? 1 : GetInteger("RECOIL_SIZE"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("RECOIL_SIZE", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutCheat);

AddSeekbar2("Aim-Dist", "瞄准距离", 0, 180, GetInteger("AIM_DISTANCE") == 0 ? 1 : GetInteger("AIM_DISTANCE"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("AIM_DISTANCE", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutCheat);

AddSeekbar2("Fov-Aim", "视野瞄准", 50, 350, GetInteger("AIM_SIZE") == 0 ? 1 : GetInteger("AIM_SIZE"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("AIM_SIZE", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutCheat);


addSwitch2("Skin-Enable", "Skin-Enable",
      GetBoolean("SKIN_ENABLE"), (buttonView, isChecked) -> UpdateConfiguration2("SKIN_ENABLE", Integer.valueOf(isChecked ? 1 : 0)),

      view -> {
          SaveConfiguration();
      },
      layoutSet);


addSwitch2("DeadBox (Ingame for open)", "DeadBox",
      GetBoolean("SKIN_BOXENABLE"), (buttonView, isChecked) -> UpdateConfiguration2("SKIN_BOXENABLE", isChecked ? 1 : 0),

      view -> {
          SaveConfiguration();
      },
      layoutSet);

AddSeekbar2("X-suit", 0, 13, GetInteger("SKIN_XSUIT"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_XSUIT", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("Set", 0, 72, GetInteger("SKIN_SET"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_SET", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("Skin-BackPack", 0, 16, GetInteger("SKIN_BACKPACK"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_BACKPACK", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
AddSeekbar2("Skin-Helmet", 0, 10, GetInteger("SKIN_HELMET"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_HELMET", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

//   AddText("Skin-Gun", layoutSet);

AddSeekbar2("M416", 0, 11, GetInteger("SKIN_M416"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_M416", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("AKM", 0, 10, GetInteger("SKIN_AKM"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_AKM", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      //

      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("SCAR-L", 0, 7, GetInteger("SKIN_SCARL"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_SCARL", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
AddSeekbar2("M762", 0, 9, GetInteger("SKIN_M762"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_M762", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("M16A4", 0, 5, GetInteger("SKIN_M16A4"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_M16A4", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("GROZAR", 0, 7, GetInteger("SKIN_GROZAR"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_GROZAR", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("AUG", 0, 5, GetInteger("SKIN_AUG"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_AUG", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
AddSeekbar2("ACE32", 0, 3, GetInteger("SKIN_ACE32"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_ACE32", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
AddSeekbar2("M249", 0, 4, GetInteger("SKIN_M249"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_M249", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
AddSeekbar2("DP28", 0, 4, GetInteger("SKIN_DP28"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_DP28", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("MG3", 0, 1, GetInteger("SKIN_MG3"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_MG3", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);
//  AddText("SMG", layoutSet);

AddSeekbar2("P90", 0, 1, GetInteger("SKIN_P90"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_P90", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("UZI", 0, 6, GetInteger("SKIN_UZI"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_UZI", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("UMP45", 0, 8, GetInteger("SKIN_UMP45"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_UMP45", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("VECTOR", 0, 4, GetInteger("SKIN_VECTOR"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_VECTOR", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("THOMPSON", 0, 4, GetInteger("SKIN_THOMPSON"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_THOMPSON", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


//  AddText("Sniper", layoutSet);
AddSeekbar2("M24", 0, 5, GetInteger("SKIN_M24"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_M24", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("KAR98K", 0, 6, GetInteger("SKIN_KAR98K"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_KAR98K", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("AWM", 0, 7, GetInteger("SKIN_AWM"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_AWM", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("AMR", 0, 1, GetInteger("SKIN_AMR"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_AMR", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("MK14", 0, 2, GetInteger("SKIN_MK14"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_MK14", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

//   AddText("Skin-Vehicle", layoutSet);

AddSeekbar2("Dacia", 0, 23, GetInteger("SKIN_DACIA"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_DACIA", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("CoupeRP", 0, 35, GetInteger("SKIN_COUPERP"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_COUPERP", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


AddSeekbar2("UAZ", 0, 13, GetInteger("SKIN_UAZ"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_UAZ", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);

AddSeekbar2("Moto", 0, 8, GetInteger("SKIN_MOTO"), new SeekBar.OnSeekBarChangeListener() {

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      UpdateConfiguration2("SKIN_MOTO", progress);

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
      SaveConfiguration();
  }
}, layoutSet);


mainLayout.addView(radioGroup);

        // Optionally, add logic to re-enable the button if overlay is stopped elsewhere
        // (e.g., via a callback or broadcast)

        // --- Store references to all dynamic UI elements for language switching ---
        // Main tab labels
      //  final TextView[] dynamicTextViews = new TextView[] { txtMain, txtEsp, txtAim, txtSkin, txtHide };
        //final Button[] dynamicButtons = new Button[] { espToggleButton };
        // Add more as needed (e.g., for switches, combos, etc.)
        // ---

        // Update translateMenuElements to update all these elements
       // this.dynamicTextViews = dynamicTextViews;
       // this.dynamicButtons = dynamicButtons;

}


    void AddText(Object data, String text, float size, int color) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setShadowLayer(5, 5, 5, Color.BLACK);
        textView.setPadding(15, 15, 15, 15);
        textView.setTextSize(convertSizeToDp(size));
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(textView);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(textView);
    }

    int convertSizeToDp(float size) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics);
        return Math.round(fpixels);
    }


    int dp(int i) {
        return dpToPx(i);
    }

    int dpi(float dp) {
        return (int) (dp * this.getResources().getDisplayMetrics().density + 0.5f);
    }

    int convertSizeToDp22() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 2.5, metrics);
        return Math.round(fpixels);
    }


    public void addSwitch2(String label, boolean isChecked,
                           CompoundButton.OnCheckedChangeListener listener,
                           View.OnClickListener longClickListener,
                           LinearLayout parent) {
        // Extract English and Chinese text from the label if it was created with getEspMenuText
        // We'll create an overloaded method for new switches
        addSwitch2Internal(label, null, null, isChecked, listener, longClickListener, parent);
    }

    // New method that accepts English and Chinese text separately for proper language switching
    public void addSwitch2(String englishText, String chineseText, boolean isChecked,
                           CompoundButton.OnCheckedChangeListener listener,
                           View.OnClickListener longClickListener,
                           LinearLayout parent) {
        String currentLabel = getEspMenuText(englishText, chineseText);
        addSwitch2Internal(currentLabel, englishText, chineseText, isChecked, listener, longClickListener, parent);
    }

    private void addSwitch2Internal(String label, String englishText, String chineseText, boolean isChecked,
                           CompoundButton.OnCheckedChangeListener listener,
                           View.OnClickListener longClickListener,
                           LinearLayout parent) {
        // iOS-style container with compact design
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.HORIZONTAL);
        containerLayout.setGravity(Gravity.CENTER_VERTICAL);
        containerLayout.setPadding(convertSizeToDp(12), convertSizeToDp(8), convertSizeToDp(12), convertSizeToDp(8));

        // iOS-style container background with shadow
        GradientDrawable containerBg = new GradientDrawable();
        containerBg.setColor(Color.parseColor("#2C2C2E")); // iOS secondary background
        containerBg.setCornerRadius(convertSizeToDp(12));
        containerBg.setStroke(convertSizeToDp(1), Color.parseColor("#38383A"));
        containerLayout.setBackground(containerBg);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(convertSizeToDp(8), convertSizeToDp(4), convertSizeToDp(8), convertSizeToDp(4));
        containerLayout.setLayoutParams(containerParams);

        // Add elevation for modern depth effect
        containerLayout.setElevation(convertSizeToDp(2));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.weight = 1;

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labelText.setTextColor(Color.parseColor("#FFFFFF")); // iOS primary text
        labelText.setTypeface(Typeface.DEFAULT);
        labelText.setLayoutParams(textParams);

        // Register for language updates if English and Chinese text are provided
        if (englishText != null && chineseText != null) {
            registerTranslatableElement(labelText, englishText, chineseText);
        }

        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchParams.gravity = Gravity.END;

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchButton = new Switch(this);
        switchButton.setChecked(isChecked);
        switchButton.setLayoutParams(switchParams);

        // iOS-style switch colors
        ColorStateList thumbStates = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.WHITE, // White thumb for both states
                        Color.WHITE
                }
        );

        ColorStateList trackStates = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.parseColor("#34C759"), // iOS system green for ON
                        Color.parseColor("#39393D")  // iOS gray for OFF
                }
        );

        switchButton.setThumbTintList(thumbStates);
        switchButton.setTrackTintList(trackStates);

        // Enhanced switch listener with haptic feedback and authentication gating
        switchButton.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
            // AUTHENTICATION GATING: Check if user is authenticated before allowing mod features
            if (isCheckedNew && !isUserAuthenticated()) {
                // Block the switch activation and revert to unchecked state
                buttonView.setChecked(false);
                triggerHapticFeedback(HapticFeedbackType.ERROR);
                Toast.makeText(this, getEspMenuText(
                    "Authentication Required! Please restart app and enter valid license key.",
                    "需要身份验证！请重启应用并输入有效许可证密钥。"
                ), Toast.LENGTH_SHORT).show();

                Log.w("Floating", "Mod feature blocked - user not authenticated");
                return; // Block feature activation
            }

            triggerHapticFeedback(isCheckedNew ? HapticFeedbackType.SUCCESS : HapticFeedbackType.LIGHT);

            // iOS-style switch animation
            buttonView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    buttonView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
                })
                .start();

            if (listener != null) {
                listener.onCheckedChanged(buttonView, isCheckedNew);
            }
        });

        switchButton.setOnLongClickListener(v -> {
            triggerHapticFeedback(HapticFeedbackType.HEAVY);
            if (longClickListener != null) {
                longClickListener.onClick(v);
            }
            return true;
        });

        containerLayout.addView(labelText);
        containerLayout.addView(switchButton);
        parent.addView(containerLayout);

        View lineView = new View(this);
        lineView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lineParams.setMargins(20, 5, 20, 10);
        lineView.setLayoutParams(lineParams);
        parent.addView(lineView);
        addSpacing(parent, 5);
    }

    @SuppressLint("SetTextI18n")
    public void addCheckboxWithSwitch(LinearLayout parent, CompoundButton.OnCheckedChangeListener listener) {
        // Compact iOS-style container
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.HORIZONTAL);
        containerLayout.setGravity(Gravity.CENTER_VERTICAL);
        containerLayout.setPadding(convertSizeToDp(12), convertSizeToDp(8), convertSizeToDp(12), convertSizeToDp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2C2C2E"));
        bg.setCornerRadius(convertSizeToDp(12));
        bg.setStroke(convertSizeToDp(1), Color.parseColor("#38383A"));
        containerLayout.setBackground(bg);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(convertSizeToDp(8), convertSizeToDp(4), convertSizeToDp(8), convertSizeToDp(4));
        containerLayout.setLayoutParams(containerParams);
        containerLayout.setElevation(convertSizeToDp(2));

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.weight = 1; // Takes all remaining space

        TextView labelText = new TextView(this);
        labelText.setText(getEspMenuText("LiveStreamMode(Recording Hide)", "隐藏模式"));
        labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelText.setTextColor(Color.WHITE);
        labelText.setLayoutParams(textParams);

        // Register for language updates
        registerTranslatableElement(labelText, "LiveStreamMode(Recording Hide)", "隐藏模式");

        // Create layout params for the switch
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        switchParams.gravity = Gravity.END;

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch switchButton = new Switch(this);
        switchButton.setChecked(GetBoolean("RECORDER_HIDE"));
        switchButton.setLayoutParams(switchParams);

        // iOS color accents
        switchButton.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
        switchButton.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#34C759")));
        switchButton.setOnCheckedChangeListener(listener);

        containerLayout.addView(labelText);
        containerLayout.addView(switchButton);
        parent.addView(containerLayout);
        addSpacing(parent, 5);
    }


    // Define the missing AddCheckbox method
    @SuppressLint("SetTextI18n")
    private void AddCheckbox(LinearLayout parent, CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(10, 5, 10, 5);

        TextView textView = new TextView(this);
        textView.setText("LiveStreamMode(Recording Hide)");
        textView.setTextSize(10.0f);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(20, 0, 20, 0);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchBtn = new Switch(this);
        switchBtn.setChecked(false);
        switchBtn.setOnCheckedChangeListener(listener);

        layout.addView(textView);
        layout.addView(switchBtn);
        parent.addView(layout);

        View lineView = new View(this);
        lineView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lineParams.setMargins(20, 0, 20, 10);
        lineView.setLayoutParams(lineParams);
        parent.addView(lineView);
        addSpacing(parent, 5);
    }

    @SuppressLint("RtlHardcoded")
    void addRadioButton(Object object, String[] list, int value, RadioGroup.OnCheckedChangeListener listener) {
        // Deprecated in favor of compact header segment; keep for compatibility
        createIOSStyleRadioGroup(object, list, value, listener);
    }

    // Create compact segmented control for language selection (header)
    private LinearLayout createLanguageSegment(String[] options, int selected, java.util.function.IntConsumer onSelect) {
        LinearLayout segment = new LinearLayout(this);
        segment.setOrientation(LinearLayout.HORIZONTAL);
        segment.setPadding(convertSizeToDp(4), convertSizeToDp(4), convertSizeToDp(4), convertSizeToDp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2C2C2E"));
        bg.setCornerRadius(convertSizeToDp(12));
        bg.setStroke(convertSizeToDp(1), Color.parseColor("#38383A"));
        segment.setBackground(bg);
        segment.setElevation(convertSizeToDp(2));

        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            TextView chip = new TextView(this);
            chip.setText(options[i]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setPadding(convertSizeToDp(12), convertSizeToDp(6), convertSizeToDp(12), convertSizeToDp(6));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) p.setMargins(convertSizeToDp(4), 0, 0, 0);
            chip.setLayoutParams(p);
            styleLanguageChip(chip, idx == selected);
            chip.setOnClickListener(v -> {
                triggerHapticFeedback(HapticFeedbackType.LIGHT);
                onSelect.accept(idx);
                // Update visuals
                for (int c = 0; c < segment.getChildCount(); c++) {
                    styleLanguageChip((TextView) segment.getChildAt(c), c == idx);
                }
            });
            segment.addView(chip);
        }
        return segment;
    }

    private void styleLanguageChip(TextView chip, boolean selected) {
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setCornerRadius(convertSizeToDp(10));
        if (selected) {
            chipBg.setColor(Color.parseColor("#007AFF"));
            chipBg.setStroke(convertSizeToDp(1), Color.parseColor("#0051D5"));
            chip.setTextColor(Color.WHITE);
        } else {
            chipBg.setColor(Color.parseColor("#3A3A3C"));
            chipBg.setStroke(convertSizeToDp(1), Color.parseColor("#48484A"));
            chip.setTextColor(Color.parseColor("#E0E0E0"));
        }
        chip.setBackground(chipBg);
    }


    // Overloaded method for language-aware seekbars
    @SuppressLint("SetTextI18n")
    void AddSeekbar2(String englishText, String chineseText, final int min, int max, int value, final SeekBar.OnSeekBarChangeListener listener, LinearLayout subWindow) {
        String currentText = getEspMenuText(englishText, chineseText);
        AddSeekbar2Internal(currentText, englishText, chineseText, min, max, value, listener, subWindow);
    }

    @SuppressLint("SetTextI18n")
    void AddSeekbar2(String text, final int min, int max, int value, final SeekBar.OnSeekBarChangeListener listener, LinearLayout subWindow) {
        AddSeekbar2Internal(text, null, null, min, max, value, listener, subWindow);
    }

    @SuppressLint("SetTextI18n")
    private void AddSeekbar2Internal(String text, String englishText, String chineseText, final int min, int max, int value, final SeekBar.OnSeekBarChangeListener listener, LinearLayout subWindow) {
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setPadding(dpi(10), dpi(6), dpi(10), dpi(6));

        // Label text with new style
        TextView textV = new TextView(this);
        textV.setText(text);
        textV.setTextSize(14);
        textV.setTextColor(Color.parseColor("#E0E0E0"));
        textV.setTypeface(DEFAULT_BOLD);
        textV.setPadding(dpi(4), 0, dpi(4), dpi(6));

        // Register for language updates if English and Chinese text are provided
        if (englishText != null && chineseText != null) {
            registerTranslatableElement(textV, englishText, chineseText);
        }

        // Seekbar row
        LinearLayout seekbarRow = new LinearLayout(this);
        seekbarRow.setOrientation(LinearLayout.HORIZONTAL);
        seekbarRow.setGravity(Gravity.CENTER_VERTICAL);
        seekbarRow.setPadding(dpi(4), 0, dpi(4), 0);

        // Value display with new color
        final TextView textValue = new TextView(this);
        textValue.setText(String.valueOf(value > 0 ? value : min));
        textValue.setTextSize(13);
        textValue.setTextColor(Color.parseColor("#64B5F6")); // New blue accent
        textValue.setTypeface(DEFAULT_BOLD);
        textValue.setGravity(Gravity.CENTER);
        textValue.setMinWidth(dpi(35));

        // Modern seekbar track
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setColor(Color.parseColor("#1E1E1E")); // Darker background
        trackBg.setCornerRadius(dpi(5));

        // Seekbar with updated colors
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        seekBar.setMin(min);
        seekBar.setProgress(value > 0 ? value : min);
        seekBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#64B5F6"))); // New blue accent
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF5722"))); // Darker track
        seekBar.setBackground(trackBg);

        // Modern thumb design
        GradientDrawable thumbDrawable = new GradientDrawable();
        thumbDrawable.setShape(GradientDrawable.OVAL);
        thumbDrawable.setColor(Color.parseColor("#64B5F6")); // New blue accent
        thumbDrawable.setSize(dpi(14), dpi(14)); // Slightly smaller thumb
        seekBar.setThumb(thumbDrawable);

        // Layout parameters
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        seekBarParams.setMargins(dpi(4), 0, dpi(4), 0);
        seekBar.setLayoutParams(seekBarParams);

        // Enhanced seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < min) {
                    progress = min;
                    seekBar.setProgress(progress);
                }
                if (listener != null) {
                    listener.onProgressChanged(seekBar, progress, fromUser);
                }
                textValue.setText(String.valueOf(progress));

                // Smoother animation
                textValue.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(150)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .withEndAction(() -> textValue.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(150)
                                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                .start());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStartTrackingTouch(seekBar);
                }
                thumbDrawable.setColor(Color.parseColor("#90CAF9")); // Lighter blue when pressed
                seekBar.setThumb(thumbDrawable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) {
                    listener.onStopTrackingTouch(seekBar);
                }
                thumbDrawable.setColor(Color.parseColor("#64B5F6")); // Return to normal blue
                seekBar.setThumb(thumbDrawable);
                SaveConfiguration();
            }
        });

        // Refined container background
        GradientDrawable containerBg = new GradientDrawable();
        containerBg.setColor(Color.parseColor("#202020")); // Darker background
        containerBg.setCornerRadius(dpi(8));
        containerBg.setStroke(dpi(1), Color.parseColor("#2C2C2C")); // Subtle border
        containerLayout.setBackground(containerBg);

        // Add views
        seekbarRow.addView(seekBar);
        seekbarRow.addView(textValue);
        containerLayout.addView(textV);
        containerLayout.addView(seekbarRow);

        // Compact margins
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(dpi(6), dpi(3), dpi(6), dpi(3));
        containerLayout.setLayoutParams(containerParams);

        subWindow.addView(containerLayout);
        addSpacing(subWindow, 3);
    }

    private int convertSizeToDp(int size) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float fpixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics);
        return Math.round(fpixels);
    }

    // Overloaded method for language-aware combo boxes
    private void addCombo(String englishLabel, String chineseLabel, String[] options, final String configKey, LinearLayout subWindow) {
        String currentLabel = getEspMenuText(englishLabel, chineseLabel);
        addComboInternal(currentLabel, englishLabel, chineseLabel, options, configKey, subWindow);
    }

    private void addCombo(String label, String[] options, final String configKey, LinearLayout subWindow) {
        addComboInternal(label, null, null, options, configKey, subWindow);
    }

    private void addComboInternal(String label, String englishLabel, String chineseLabel, String[] options, final String configKey, LinearLayout subWindow) {
        // Create container with subtle elevation
        RelativeLayout containerLayout = new RelativeLayout(this);
        containerLayout.setBackground(selectedBackground());
        containerLayout.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        // Horizontal layout with reduced size
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.END); // Align to end
        rowLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // Smaller label text
        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextSize(13);
        labelText.setTypeface(DEFAULT_BOLD);
        labelText.setTextColor(Color.parseColor("#383838"));
        labelText.setPadding(dpToPx(2), dpToPx(2), dpToPx(8), dpToPx(2));
        labelText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        // Register for language updates if English and Chinese labels are provided
        if (englishLabel != null && chineseLabel != null) {
            registerTranslatableElement(labelText, englishLabel, chineseLabel);
        }

        // Label takes left portion
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f);
        labelText.setLayoutParams(labelParams);

        // Options scroll view aligned to end
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);

        LinearLayout optionsLayout = new LinearLayout(this);
        optionsLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionsLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        optionsLayout.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));

        String selectedOption = configMap.get(configKey);
        int selectedIndex = selectedOption != null ? Integer.parseInt(selectedOption) : 0;
        List<TextView> optionViews = new ArrayList<>();


        for (int i = 0; i < options.length; i++) {
            TextView optionView = new TextView(this);
            optionView.setText(options[i]);
            optionView.setTextSize(12);
            optionView.setTypeface(DEFAULT);
            optionView.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
            optionView.setGravity(Gravity.CENTER);

            GradientDrawable normalBg = new GradientDrawable();
            normalBg.setColor(Color.parseColor("#383838"));
            normalBg.setCornerRadius(dpToPx(15));

            GradientDrawable selectedBg = new GradientDrawable();
            selectedBg.setColor(Color.parseColor("#4CAF50"));
            selectedBg.setCornerRadius(dpToPx(15));

            final int index = i;
            optionView.setOnClickListener(v -> {
                for (TextView view : optionViews) {
                    view.setBackground(normalBg);
                    view.setTextColor(Color.parseColor("#FFFFFF"));
                }
                optionView.setBackground(selectedBg);
                optionView.setTextColor(Color.parseColor("#FFFFFF"));
                UpdateConfiguration2(configKey, index);
            });

            optionView.setBackground(i == selectedIndex ? selectedBg : normalBg);
            optionView.setTextColor(Color.parseColor("#FFFFFF"));

            if (i > 0) {
                Space space = new Space(this);
                space.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(4), 0));
                optionsLayout.addView(space);
            }

            optionsLayout.addView(optionView);
            optionViews.add(optionView);
        }

        scrollView.addView(optionsLayout);
        rowLayout.addView(labelText);
        rowLayout.addView(scrollView);
        containerLayout.addView(rowLayout);

        // Reduced margins
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        containerLayout.setLayoutParams(containerParams);

        subWindow.addView(containerLayout);
    }


    private GradientDrawable selectedBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(10);
        return drawable;
    }

    private int dpToPxLine() {
        return Math.round((float) 0.5 * getResources().getDisplayMetrics().density);
    }


    public int dpToPx(int i) {
        return Math.round(((float) i) * getResources().getDisplayMetrics().density);
    }



    void addRadioButtonGroup(String label, String option1, String option2, String option3, /*boolean isOption1Checked, boolean isOption2Checked,*/
                             final View.OnClickListener onClickOption1, final View.OnClickListener onClickOption2, final View.OnClickListener onClickOption3,
                             LinearLayout subWindow) {
        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.HORIZONTAL);
        containerLayout.setGravity(Gravity.CENTER_VERTICAL);
        containerLayout.setPadding(10, 0, 10, 10);
        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextSize(10.0f);
        labelText.setTextColor(Color.BLACK);
        labelText.setPadding(20, 0, 20, 10);
        containerLayout.addView(labelText);
        RelativeLayout backgroundLayout = getRelativeLayout();
        final TextView radioButton1 = new TextView(this);
        final TextView radioButton2 = new TextView(this);
        final TextView radioButton3 = new TextView(this);
        radioButton1.setText(option1);
        radioButton1.setTextSize(10.0f);
        radioButton1.measure(0, 0);
        radioButton1.setPadding(20, 12, 20, 10);
        radioButton1.setGravity(Gravity.CENTER);
        radioButton1.setTextColor(Color.BLACK);
        radioButton1.setBackground(null);

        if (onClickOption1 != null) {
            radioButton1.setTextColor(Color.BLACK);
            radioButton2.setTextColor(Color.BLACK);
            radioButton3.setTextColor(Color.BLACK);
            radioButton1.setBackground(createWhiteBackground());
            radioButton2.setBackground(null);
            radioButton3.setBackground(null);
        }


        radioButton1.setOnClickListener(v -> {
            radioButton1.setTextColor(Color.BLACK);
            radioButton2.setTextColor(Color.BLACK);
            radioButton3.setTextColor(Color.BLACK);
            radioButton1.setBackground(createWhiteBackground());
            radioButton2.setBackground(null);
            radioButton3.setBackground(null);
            if (onClickOption1 != null) {
                onClickOption1.onClick(v);
            }
        });
        radioButton2.setText(option2);
        radioButton2.setTextSize(10.0f);
        radioButton2.measure(0, 0);
        radioButton2.setPadding(20, 12, 20, 10);
        radioButton2.setGravity(Gravity.CENTER);
        radioButton2.setTextColor(Color.BLACK);
        radioButton2.setBackground(null);
        radioButton2.setOnClickListener(v -> {
            radioButton1.setTextColor(Color.BLACK);
            radioButton2.setTextColor(Color.BLACK);
            radioButton3.setTextColor(Color.BLACK);
            radioButton2.setBackground(createWhiteBackground());
            radioButton1.setBackground(null);
            radioButton3.setBackground(null);
            if (onClickOption2 != null) {
                onClickOption2.onClick(v);
            }
        });
        radioButton3.setText(option3);
        radioButton3.setTextSize(10.0f);
        radioButton3.measure(0, 0);
        radioButton3.setPadding(20, 12, 20, 10);
        radioButton3.setGravity(Gravity.CENTER);
        radioButton3.setTextColor(Color.BLACK);
        radioButton3.setBackground(null);
        radioButton3.setOnClickListener(v -> {
            radioButton1.setTextColor(Color.BLACK);
            radioButton2.setTextColor(Color.BLACK);
            radioButton3.setTextColor(Color.BLACK);
            radioButton3.setBackground(createWhiteBackground());
            radioButton1.setBackground(null);
            radioButton2.setBackground(null);
            if (onClickOption3 != null) {
                onClickOption3.onClick(v);
            }
        });
        RelativeLayout.LayoutParams radioButtonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        radioButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        radioButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        radioButtonParams.setMargins(0, 10, 10, 5);
        radioButton1.setLayoutParams(radioButtonParams);
        radioButton2.setLayoutParams(radioButtonParams);
        radioButton3.setLayoutParams(radioButtonParams);
        LinearLayout radioGroupLayout = new LinearLayout(this);
        radioGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
        radioGroupLayout.setGravity(Gravity.CENTER);
        radioGroupLayout.setPadding(10, 0, 10, 10);
        radioGroupLayout.addView(radioButton1);
        radioGroupLayout.addView(radioButton2);
        radioGroupLayout.addView(radioButton3);
        backgroundLayout.addView(radioGroupLayout);
        containerLayout.addView(backgroundLayout);
        subWindow.addView(containerLayout);
        View lineView = new View(this);
        lineView.setBackgroundColor(0xFF000000);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lineParams.setMargins(20, 0, 20, 10);
        lineView.setLayoutParams(lineParams);
        subWindow.addView(lineView);
        addSpacing(subWindow, 5);
    }

    @NonNull
    private RelativeLayout getRelativeLayout() {
        RelativeLayout backgroundLayout = new RelativeLayout(this);
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(0xFFEEEEEE);
        backgroundDrawable.setCornerRadius(15);
        backgroundLayout.setBackground(backgroundDrawable);
        RelativeLayout.LayoutParams backgroundLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        backgroundLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        backgroundLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        backgroundLayoutParams.setMargins(160, 10, 10, 3);
        backgroundLayout.setLayoutParams(backgroundLayoutParams);
        return backgroundLayout;
    }


    private Drawable createWhiteBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(10);
        return drawable;
    }

    void addLogoText(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(0xFFFFFFFF);


        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setGravity(Gravity.CENTER);

        textView.setOnClickListener(v -> {
            mainLayout.setVisibility(View.GONE);
            iconLayout.setVisibility(View.VISIBLE);
        });
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        bodyLayout.addView(textView);
    }

    void addSpacing(ViewGroup parentLayout, int i) {
        View spaceView = new View(parentLayout.getContext());
        spaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5));
        parentLayout.addView(spaceView);
    }


      @SuppressLint("ClickableViewAccessibility")
    void CreateIcon() {
        iconLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        iconLayout.setLayoutParams(iconParams);

        iconImg = new ImageView(this);
        ViewGroup.LayoutParams iconImgParams = new ViewGroup.LayoutParams(120, 120);
        iconImg.setLayoutParams(iconImgParams);
        GetBoolean("RECORDER_HIDE");
        iconLayout.addView(iconImg);

        try {
            String iconBase64 = iconenc();
            byte[] iconData = Base64.decode(iconBase64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
            iconImg.setImageBitmap(bmp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        iconLayoutParams = new WindowManager.LayoutParams();
        iconLayoutParams.width = iconSize;
        iconLayoutParams.height = iconSize;
        iconLayoutParams.type = type;
        iconLayoutParams.format = PixelFormat.RGBA_8888;
        iconLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        iconLayoutParams.gravity = Gravity.START | Gravity.TOP;
        iconLayoutParams.x = 0;
        iconLayoutParams.y = 0;
        iconLayoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        iconLayout.setVisibility(View.VISIBLE);
        iconLayout.setOnTouchListener(new View.OnTouchListener() {
            int lastX;
            int lastY;
            float pressedX;
            float pressedY;
            float deltaX;
            float deltaY;
            float newX;
            float newY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = iconLayoutParams.x;
                        lastY = iconLayoutParams.y;
                        deltaX = lastX - event.getRawX();
                        deltaY = lastY - event.getRawY();

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        // Unified API 28+ vibration call
                        Vibrator vibrator = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
                            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                            if (vm != null) {
                                vibrator = vm.getDefaultVibrator();
                            }
                        } else { // API 28–30
                            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        }
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(VibrationEffect.createOneShot(
                                    50,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                            ));
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - pressedX);
                        int Ydiff = (int) (event.getRawY() - pressedY);
                        if (Xdiff == 0 && Ydiff == 0) {
                            // Show main layout and ensure we return to Main tab (index 0)
                            mainLayout.setVisibility(View.VISIBLE);
                            iconLayout.setVisibility(View.GONE);

                            // Reset to Main tab to prevent blank screen
                            currentTabIndex = 0;
                            if (container != null && layoutBypasss != null) {
                                container.removeAllViews();
                                container.addView(layoutBypasss);
                            }

                            // Reset floating home icon to hamburger menu state
                            if (floatingHomeIcon != null) {
                                floatingHomeIcon.setText("☰");
                            }

                            // Update floating tab selection to show Main tab as selected
                            if (tabButtons != null && tabButtons.length > 0) {
                                updateFloatingTabSelection(tabButtons, 0);
                            }
                        }
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        newX = event.getRawX() + deltaX;
                        newY = event.getRawY() + deltaY;

                        float maxX = screenWidth - v.getWidth();
                        float maxY = screenHeight - v.getHeight();

                        if (newX < 0) newX = 0;
                        if (newX > maxX) newX = maxX;
                        if (newY < 0) newY = 0;
                        if (newY > maxY) newY = maxY;

                        iconLayoutParams.x = (int) newX;
                        iconLayoutParams.y = (int) newY;

                        mainLayoutParams.x = iconLayoutParams.x;
                        mainLayoutParams.y = iconLayoutParams.y;
                        windowManager.updateViewLayout(mainLayout, mainLayoutParams);
                        windowManager.updateViewLayout(iconLayout, iconLayoutParams);
                        break;
                }
                return false;
            }
        });

        windowManager.addView(iconLayout, iconLayoutParams);
    }


    LinearLayout CreateHolder(Object data) {
        RelativeLayout parentHolder = new RelativeLayout(this);
        parentHolder.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout childHolder = new LinearLayout(this);
        childHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        childHolder.setOrientation(LinearLayout.HORIZONTAL);
        parentHolder.addView(childHolder);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(parentHolder);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(parentHolder);

        return childHolder;
    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(android.os.Looper.getMainLooper()) {
        public void handleMessage() {
            handleMessage(null);
            try {
                // Modern API 30+ only: get screen size
                android.view.WindowMetrics metrics = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    metrics = windowManager.getCurrentWindowMetrics();
                }
                android.graphics.Rect bounds = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bounds = metrics.getBounds();
                }
                assert bounds != null;
                screenWidth = bounds.width();
                screenHeight = bounds.height();
                mainLayoutParams.width = layoutWidth;
                mainLayoutParams.height = layoutHeight;
                windowManager.updateViewLayout(mainLayout, mainLayoutParams);
                canvasLayoutParams.width = screenWidth;
                canvasLayoutParams.height = screenHeight;
                windowManager.updateViewLayout(canvasLayout, canvasLayoutParams);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //}
    };

    public int GetDeviceMaxFps() {
        // Use application context for system services
        final Context ctx = getApplicationContext();
        if (ctx == null) {
            // Fallback/default value
            return 60;
        }

        final WindowManager wm =
                (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // API 30+
            if (wm != null) {
                final android.util.DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
                float refreshRate = displayMetrics.densityDpi; // fallback

                // WindowMetrics does not provide refresh rate directly, so fallback to DisplayManager
                final android.hardware.display.DisplayManager dm =
                        (android.hardware.display.DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
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
            // For API 17+, try DisplayManager for all displays
            final android.hardware.display.DisplayManager dm =
                    (android.hardware.display.DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            if (dm != null) {
                final android.view.Display[] displays = dm.getDisplays();
                if (displays.length > 0) {
                    return (int) displays[0].getRefreshRate();
                }
            }
        }

        // Fallback/default value
        return 60;
    }
    Thread mUpdateCanvas = new Thread() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            while (isAlive() && !isInterrupted()) {
                try {
                    long t1 = System.currentTimeMillis();
                    canvasLayout.postInvalidate();
                    long td = System.currentTimeMillis() - t1;

                    long sleepTime = 1000 / GetDeviceMaxFps();
                    Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    @SuppressWarnings("deprecation") // For getDefaultDisplay() & getRealSize()
    Thread mUpdateThread = new Thread() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            while (isAlive() && !isInterrupted()) {
                try {
                    long t1 = System.currentTimeMillis();
                    Point screenSize = new Point();
                    Display display = windowManager.getDefaultDisplay(); // Deprecated in API 30+
                    display.getRealSize(screenSize); // Deprecated in API 30+

                    if (screenWidth != screenSize.x || screenHeight != screenSize.y) {
                        handler.sendEmptyMessage(0);
                    }

                    long td = System.currentTimeMillis() - t1;
                    long sleepTime = 1000 / GetDeviceMaxFps();

                    // Ensure we don't pass a negative value to Thread.sleep()
                    long adjustedSleep = Math.max(0, sleepTime - td);
                    Thread.sleep(adjustedSleep);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    // ========== iOS-INSPIRED MODERN UI COMPONENTS ==========

    /**
     * Modern iOS-style touch listener with haptic feedback and smooth interactions
     * Provides enhanced user experience with iOS-like interaction patterns
     */
    private class ModernIOSTouchListener implements View.OnTouchListener {
        private float pressedX, pressedY;
        private float deltaX, deltaY;
        private float newX, newY;
        private float maxX, maxY;
        private boolean isDragging = false;
        private long touchStartTime = 0;
        private static final long LONG_PRESS_THRESHOLD = 500; // ms

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartTime = System.currentTimeMillis();
                    pressedX = event.getRawX();
                    pressedY = event.getRawY();
                    deltaX = mainLayoutParams.x;
                    deltaY = mainLayoutParams.y;
                    maxX = screenWidth - mainLayout.getWidth();
                    maxY = screenHeight - mainLayout.getHeight();
                    isDragging = false;

                    // iOS-style haptic feedback on touch down
                    triggerHapticFeedback(HapticFeedbackType.LIGHT);

                    // Subtle scale animation on press
                    v.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(100)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float deltaMove = Math.abs(event.getRawX() - pressedX) + Math.abs(event.getRawY() - pressedY);
                    if (deltaMove > 10) { // Start dragging threshold
                        isDragging = true;
                        newX = event.getRawX() - pressedX + deltaX;
                        newY = event.getRawY() - pressedY + deltaY;

                        // Smooth boundary constraints with iOS-style elastic effect
                        mainLayoutParams.x = (int) Math.max(-20, Math.min(newX, maxX + 20));
                        mainLayoutParams.y = (int) Math.max(-20, Math.min(newY, maxY + 20));

                        windowManager.updateViewLayout(mainLayout, mainLayoutParams);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Restore scale with iOS-style bounce
                    v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator(1.2f))
                        .start();

                    // Snap back to boundaries if outside
                    if (mainLayoutParams.x < 0 || mainLayoutParams.x > maxX ||
                        mainLayoutParams.y < 0 || mainLayoutParams.y > maxY) {

                        mainLayoutParams.x = (int) Math.max(0, Math.min(mainLayoutParams.x, maxX));
                        mainLayoutParams.y = (int) Math.max(0, Math.min(mainLayoutParams.y, maxY));

                        // Smooth snap-back animation
                        ValueAnimator snapAnimator = ValueAnimator.ofFloat(0f, 1f);
                        snapAnimator.setDuration(300);
                        snapAnimator.setInterpolator(new OvershootInterpolator(0.8f));
                        snapAnimator.addUpdateListener(animation ->
                            windowManager.updateViewLayout(mainLayout, mainLayoutParams));
                        snapAnimator.start();

                        triggerHapticFeedback(HapticFeedbackType.MEDIUM);
                    }

                    // Handle long press for additional functionality
                    if (!isDragging && (System.currentTimeMillis() - touchStartTime) > LONG_PRESS_THRESHOLD) {
                        triggerHapticFeedback(HapticFeedbackType.HEAVY);
                        // Could add long press functionality here
                    }
                    break;
            }
            return true;
        }
    }

    /**
     * Enhanced haptic feedback system with iOS-style patterns
     */
    private enum HapticFeedbackType {
        LIGHT, MEDIUM, HEAVY, SUCCESS, WARNING, ERROR
    }

    private void triggerHapticFeedback(HapticFeedbackType type) {
        try {
            Vibrator vibrator = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vibrator = vm.getDefaultVibrator();
                }
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = switch (type) {
                    case LIGHT -> VibrationEffect.createOneShot(25, 50);
                    case MEDIUM -> VibrationEffect.createOneShot(50, 100);
                    case HEAVY -> VibrationEffect.createOneShot(75, 150);
                    case SUCCESS -> VibrationEffect.createWaveform(new long[]{0, 50, 50, 50}, -1);
                    case WARNING ->
                            VibrationEffect.createWaveform(new long[]{0, 100, 100, 100}, -1);
                    case ERROR -> VibrationEffect.createWaveform(new long[]{0, 150, 50, 150}, -1);
                    default -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
                };
                vibrator.vibrate(effect);
            }
        } catch (Exception e) {
            // Fallback for older devices or permission issues
            Log.w("HapticFeedback", "Could not trigger haptic feedback: " + e.getMessage());
        }
    }

    /**
     * Creates iOS-style radio button group with modern design and smooth animations
     * Features circular buttons, iOS color scheme, and elegant transitions
     */
    private void createIOSStyleRadioGroup(Object object, String[] list, int value, RadioGroup.OnCheckedChangeListener listener) {
        // Container with iOS-style background
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(convertSizeToDp(16), convertSizeToDp(12), convertSizeToDp(16), convertSizeToDp(12));

        // iOS-style container background
        GradientDrawable containerBg = new GradientDrawable();
        containerBg.setColor(Color.parseColor("#F2F2F7")); // iOS light gray
        containerBg.setCornerRadius(convertSizeToDp(12));
        containerBg.setStroke(convertSizeToDp(1), Color.parseColor("#E5E5EA")); // iOS border
        container.setBackground(containerBg);

        // Create custom radio group
        LinearLayout radioContainer = new LinearLayout(this);
        radioContainer.setOrientation(LinearLayout.HORIZONTAL);
        radioContainer.setGravity(Gravity.CENTER);

        // iOS-style radio buttons
        for (int i = 0; i < list.length; i++) {
            LinearLayout radioItem = createIOSRadioButton(list[i], i == value, i, listener);
            radioContainer.addView(radioItem);

            // Add spacing between buttons
            if (i < list.length - 1) {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    convertSizeToDp(12), ViewGroup.LayoutParams.MATCH_PARENT);
                spacer.setLayoutParams(spacerParams);
                radioContainer.addView(spacer);
            }
        }

        container.addView(radioContainer);

        // Add to parent
        if (object instanceof Integer) {
            this.pageLayouts[(Integer) object].addView(container);
        } else if (object instanceof ViewGroup) {
            ((ViewGroup) object).addView(container);
        }
    }

    /**
     * Creates individual iOS-style radio button with modern design
     */
    private LinearLayout createIOSRadioButton(String text, boolean isSelected, int index, RadioGroup.OnCheckedChangeListener listener) {
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(Gravity.CENTER_VERTICAL);
        buttonContainer.setPadding(convertSizeToDp(12), convertSizeToDp(8), convertSizeToDp(12), convertSizeToDp(8));

        // iOS-style circular indicator
        View radioCircle = new View(this);
        int circleSize = convertSizeToDp(20);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(circleSize, circleSize);
        circleParams.setMargins(0, 0, convertSizeToDp(8), 0);
        radioCircle.setLayoutParams(circleParams);

        // Create iOS-style radio button appearance
        updateIOSRadioAppearance(radioCircle, isSelected);

        // Text label with iOS typography
        TextView textLabel = new TextView(this);
        textLabel.setText(text);
        textLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textLabel.setTextColor(Color.parseColor("#1C1C1E")); // iOS primary text
        textLabel.setTypeface(Typeface.DEFAULT);

        buttonContainer.addView(radioCircle);
        buttonContainer.addView(textLabel);

        // iOS-style button background
        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(isSelected ? Color.parseColor("#007AFF") : Color.parseColor("#FFFFFF")); // iOS blue or white
        buttonBg.setCornerRadius(convertSizeToDp(8));
        buttonBg.setStroke(convertSizeToDp(1), isSelected ? Color.parseColor("#007AFF") : Color.parseColor("#E5E5EA"));
        buttonContainer.setBackground(buttonBg);

        // Update text color based on selection
        textLabel.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#1C1C1E"));

        // Modern click listener with animations and haptic feedback
        buttonContainer.setOnClickListener(v -> {
            // Trigger haptic feedback
            triggerHapticFeedback(HapticFeedbackType.LIGHT);

            // iOS-style press animation
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
                })
                .start();

            // Update selection state with smooth animation
            updateRadioGroupSelection((ViewGroup) buttonContainer.getParent(), index);

            // Call the original listener
            if (listener != null) {
                listener.onCheckedChanged(null, index);
            }
        });

        return buttonContainer;
    }

    /**
     * Updates iOS-style radio button appearance with smooth animations
     */
    private void updateIOSRadioAppearance(View radioCircle, boolean isSelected) {
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);

        if (isSelected) {
            // Selected state: iOS blue with white checkmark effect
            circleDrawable.setColor(Color.parseColor("#007AFF"));
            circleDrawable.setStroke(convertSizeToDp(2), Color.parseColor("#007AFF"));

            // Add inner white circle for checkmark effect
            GradientDrawable innerCircle = new GradientDrawable();
            innerCircle.setShape(GradientDrawable.OVAL);
            innerCircle.setColor(Color.WHITE);

            // Create layered drawable for the checkmark effect
            android.graphics.drawable.LayerDrawable layeredDrawable = new android.graphics.drawable.LayerDrawable(
                new Drawable[]{circleDrawable, innerCircle}
            );
            int inset = convertSizeToDp(6);
            layeredDrawable.setLayerInset(1, inset, inset, inset, inset);
            radioCircle.setBackground(layeredDrawable);
        } else {
            // Unselected state: white with gray border
            circleDrawable.setColor(Color.WHITE);
            circleDrawable.setStroke(convertSizeToDp(2), Color.parseColor("#C7C7CC"));
            radioCircle.setBackground(circleDrawable);
        }

        // Add subtle shadow for depth (iOS-style)
        radioCircle.setElevation(isSelected ? convertSizeToDp(2) : convertSizeToDp(1));
    }

    /**
     * Updates radio group selection with smooth iOS-style animations
     */
    private void updateRadioGroupSelection(ViewGroup radioGroup, int selectedIndex) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof LinearLayout buttonContainer) {
                boolean isSelected = (i / 2) == selectedIndex; // Account for spacers

                // Update button background with animation
                GradientDrawable buttonBg = new GradientDrawable();
                buttonBg.setColor(isSelected ? Color.parseColor("#007AFF") : Color.parseColor("#FFFFFF"));
                buttonBg.setCornerRadius(convertSizeToDp(8));
                buttonBg.setStroke(convertSizeToDp(1), isSelected ? Color.parseColor("#007AFF") : Color.parseColor("#E5E5EA"));

                // Animate background change
                ValueAnimator colorAnimator = ValueAnimator.ofArgb(
                    isSelected ? Color.WHITE : Color.parseColor("#007AFF"),
                    isSelected ? Color.parseColor("#007AFF") : Color.WHITE
                );
                colorAnimator.setDuration(200);
                colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                colorAnimator.addUpdateListener(animation -> {
                    buttonBg.setColor((Integer) animation.getAnimatedValue());
                    buttonContainer.setBackground(buttonBg);
                });
                colorAnimator.start();

                // Update radio circle and text
                if (buttonContainer.getChildCount() >= 2) {
                    View radioCircle = buttonContainer.getChildAt(0);
                    TextView textLabel = (TextView) buttonContainer.getChildAt(1);

                    // Animate radio circle
                    updateIOSRadioAppearance(radioCircle, isSelected);

                    // Animate text color
                    ValueAnimator textColorAnimator = ValueAnimator.ofArgb(
                        isSelected ? Color.parseColor("#1C1C1E") : Color.WHITE,
                        isSelected ? Color.WHITE : Color.parseColor("#1C1C1E")
                    );
                    textColorAnimator.setDuration(200);
                    textColorAnimator.addUpdateListener(animation ->
                        textLabel.setTextColor((Integer) animation.getAnimatedValue()));
                    textColorAnimator.start();

                    // Scale animation for selection feedback
                    if (isSelected) {
                        radioCircle.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator(1.2f))
                            .withEndAction(() -> {
                                radioCircle.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(150)
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .start();
                            })
                            .start();
                    }
                }
            }
        }
    }

    /**
     * iOS-style tab selection animation
     */
    private void animateTabSelection(View tabView) {
        // Scale animation for iOS-style feedback
        tabView.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                tabView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
            })
            .start();
    }

    /**
     * Updates floating tab icon appearance based on selection state
     */
    private void updateFloatingTabSelection(Button[] tabButtons, int selectedIndex) {
        for (int i = 0; i < tabButtons.length; i++) {
            Button tabButton = tabButtons[i];
            GradientDrawable tabBg = new GradientDrawable();
            tabBg.setShape(GradientDrawable.OVAL);

            if (i == selectedIndex) {
                // Selected tab - iOS blue
                tabBg.setColor(Color.parseColor("#007AFF"));
                tabBg.setStroke(convertSizeToDp(1), Color.parseColor("#0051D5"));
                tabButton.setTextColor(Color.WHITE);
                tabButton.setElevation(convertSizeToDp(4));
            } else {
                // Unselected tab - iOS gray
                tabBg.setColor(Color.parseColor("#3A3A3C"));
                tabBg.setStroke(convertSizeToDp(1), Color.parseColor("#48484A"));
                tabButton.setTextColor(Color.parseColor("#8E8E93"));
                tabButton.setElevation(convertSizeToDp(2));
            }

            // Animate background change
            ValueAnimator colorAnimator = ValueAnimator.ofArgb(
                i == selectedIndex ? Color.parseColor("#3A3A3C") : Color.parseColor("#007AFF"),
                i == selectedIndex ? Color.parseColor("#007AFF") : Color.parseColor("#3A3A3C")
            );
            colorAnimator.setDuration(200);
            colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            final Button finalTabButton = tabButton;
            final GradientDrawable finalTabBg = tabBg;
            colorAnimator.addUpdateListener(animation -> {
                finalTabBg.setColor((Integer) animation.getAnimatedValue());
                finalTabButton.setBackground(finalTabBg);
            });
            colorAnimator.start();
        }
    }

}
