package com.bearmod.loader.floating;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingUIManager - Manages UI layout creation and iOS-style theming
 * Handles the creation and management of floating overlay interface components
 */
public class FloatingUIManager {

    private static final String TAG = "FloatingUIManager";

    private final Context context;
    private final FloatingTabManager tabManager;
    private final FloatingMenuManager menuManager;

    // UI Components
    private RelativeLayout iconLayout;
    private LinearLayout mainLayout;
    private LinearLayout bodyLayout;
    private TextView textTitle;
    private ImageView iconImg;
    private ScrollView scrollView;
    private LinearLayout container;
    private LinearLayout layoutBypasss;
    private LinearLayout layoutEsp;
    private LinearLayout layoutCheat;
    private LinearLayout layoutSet;

    // Layout parameters
    private int screenWidth, screenHeight;
    private int layoutWidth = 450;
    private int layoutHeight = 330;
    private int iconSize = 64;
    private int menuButtonSize = 30;

    // Colors and styling
    private final int c_Background = Color.argb(255, 242, 241, 247);
    private final int c_Text = Color.BLACK;
    private final int c_Child = Color.WHITE;
    private final int c_Text2 = Color.argb(240, 143, 143, 142);
    private final int c_Line = Color.argb(80, 150, 150, 150);
    private final int c_Combo = Color.WHITE;

    public FloatingUIManager(Context context, FloatingTabManager tabManager, FloatingMenuManager menuManager) {
        this.context = context;
        this.tabManager = tabManager;
        this.menuManager = menuManager;
        Logx.d("FloatingUIManager initialized");
    }

    /**
     * Create the main floating layout
     */
    public void createMainLayout() {
        try {
            Logx.d("Creating main floating layout...");

            // Create main layout with iOS-style background
            mainLayout = new LinearLayout(context);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                convertDpToPx(layoutWidth), convertDpToPx(layoutHeight)));

            // iOS-inspired main layout background
            GradientDrawable mainBg = new GradientDrawable();
            mainBg.setColor(Color.parseColor("#1C1C1E")); // iOS dark background
            mainBg.setStroke(convertDpToPx(1), Color.parseColor("#38383A")); // iOS border
            mainBg.setCornerRadius(convertDpToPx(16)); // iOS corner radius
            mainLayout.setBackground(mainBg);

            // Create header layout
            createHeaderLayout();

            // Create content area
            createContentArea();

            // Create floating home icon
            createFloatingHomeIcon();

            Logx.d("Main floating layout created successfully");

        } catch (Exception e) {
            Logx.e("Error creating main layout: " + e.getMessage(), e);
        }
    }

    /**
     * Create the icon layout for minimized state
     */
    public void createIconLayout() {
        try {
            Logx.d("Creating icon layout...");

            iconLayout = new RelativeLayout(context);
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            iconLayout.setLayoutParams(iconParams);

            // Create icon image view
            iconImg = new ImageView(context);
            ViewGroup.LayoutParams iconImgParams = new ViewGroup.LayoutParams(
                convertDpToPx(iconSize), convertDpToPx(iconSize));
            iconImg.setLayoutParams(iconImgParams);

            // Load icon from base64
            loadIconFromBase64();

            iconLayout.addView(iconImg);
            Logx.d("Icon layout created successfully");

        } catch (Exception e) {
            Logx.e("Error creating icon layout: " + e.getMessage(), e);
        }
    }

    /**
     * Show the floating interface
     */
    public void showInterface() {
        if (mainLayout != null) {
            mainLayout.setVisibility(View.VISIBLE);
        }
        if (iconLayout != null) {
            iconLayout.setVisibility(View.GONE);
        }
        Logx.d("Floating interface shown");
    }

    /**
     * Hide the floating interface
     */
    public void hideInterface() {
        if (mainLayout != null) {
            mainLayout.setVisibility(View.GONE);
        }
        if (iconLayout != null) {
            iconLayout.setVisibility(View.VISIBLE);
        }
        Logx.d("Floating interface hidden");
    }

    /**
     * Get the main layout
     */
    public LinearLayout getMainLayout() {
        return mainLayout;
    }

    /**
     * Get the icon layout
     */
    public RelativeLayout getIconLayout() {
        return iconLayout;
    }

    /**
     * Get the ESP layout
     */
    public LinearLayout getEspLayout() {
        return layoutEsp;
    }

    /**
     * Get the aim layout
     */
    public LinearLayout getAimLayout() {
        return layoutCheat;
    }

    /**
     * Get the skin layout
     */
    public LinearLayout getSkinLayout() {
        return layoutSet;
    }

    /**
     * Get the main tab layout
     */
    public LinearLayout getMainTabLayout() {
        return layoutBypasss;
    }

    /**
     * Update screen dimensions
     */
    public void updateScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        Logx.d("Screen dimensions updated: " + width + "x" + height);
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingUIManager resources...");

            // Clear references
            iconLayout = null;
            mainLayout = null;
            bodyLayout = null;
            textTitle = null;
            iconImg = null;
            scrollView = null;
            container = null;
            layoutBypasss = null;
            layoutEsp = null;
            layoutCheat = null;
            layoutSet = null;

            Logx.d("FloatingUIManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingUIManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void createHeaderLayout() {
        // Create header layout with title and language switcher
        RelativeLayout headerLayout = new RelativeLayout(context);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPx(40)));
        headerLayout.setPadding(convertDpToPx(16), convertDpToPx(12),
            convertDpToPx(16), convertDpToPx(12));

        // iOS-style header background with gradient
        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColors(new int[]{Color.parseColor("#2C2C2E"), Color.parseColor("#1C1C1E")});
        headerBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        headerBg.setCornerRadii(new float[]{
            convertDpToPx(16), convertDpToPx(16), // top-left
            convertDpToPx(16), convertDpToPx(16), // top-right
            0, 0, // bottom-right
            0, 0  // bottom-left
        });
        headerLayout.setBackground(headerBg);

        // Create title text view
        textTitle = new TextView(context);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        textTitle.setLayoutParams(titleParams);
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textTitle.setText("BearMod"); // Default title
        textTitle.setTextColor(Color.parseColor("#FFFFFF"));
        textTitle.setTypeface(Typeface.DEFAULT_BOLD);

        headerLayout.addView(textTitle);
        mainLayout.addView(headerLayout);
    }

    private void createContentArea() {
        // Create content root as FrameLayout to support overlayed elements
        final FrameLayout main = new FrameLayout(context);
        main.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        main.setClipToPadding(false);
        main.setClipChildren(false);

        // Create scrollable content area
        createScrollableContent(main);

        mainLayout.addView(main);
    }

    private void createScrollableContent(FrameLayout parent) {
        // Create vertical bar for content
        final LinearLayout vbar = new LinearLayout(context);
        vbar.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        vbar.setOrientation(LinearLayout.VERTICAL);

        // Create scroll view
        scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);

        // Create container for tab content
        container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);

        // Create tab layouts
        createTabLayouts();

        scrollView.addView(container);
        vbar.addView(scrollView);
        parent.addView(vbar);
    }

    private void createTabLayouts() {
        // Create main tab layout
        layoutBypasss = new LinearLayout(context);
        layoutBypasss.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutBypasss.setOrientation(LinearLayout.VERTICAL);
        layoutBypasss.setPadding(15, 15, 15, 15);

        // Create ESP tab layout
        layoutEsp = new LinearLayout(context);
        layoutEsp.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutEsp.setOrientation(LinearLayout.VERTICAL);
        layoutEsp.setPadding(15, 15, 15, 15);

        // Create aim tab layout
        layoutCheat = new LinearLayout(context);
        layoutCheat.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutCheat.setOrientation(LinearLayout.VERTICAL);
        layoutCheat.setPadding(15, 15, 15, 15);

        // Create skin tab layout
        layoutSet = new LinearLayout(context);
        layoutSet.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutSet.setOrientation(LinearLayout.VERTICAL);
        layoutSet.setPadding(15, 15, 15, 15);

        // Set main tab as default
        container.addView(layoutBypasss);
    }

    private void createFloatingHomeIcon() {
        // Create floating home icon container
        RelativeLayout floatingIconContainer = new RelativeLayout(context);
        FrameLayout.LayoutParams floatingContainerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        floatingContainerParams.gravity = Gravity.BOTTOM | Gravity.START;
        floatingContainerParams.setMargins(convertDpToPx(16), 0, convertDpToPx(16), convertDpToPx(16));
        floatingIconContainer.setLayoutParams(floatingContainerParams);

        // Create floating home icon button
        Button floatingHomeIcon = new Button(context);
        RelativeLayout.LayoutParams homeIconParams = new RelativeLayout.LayoutParams(
            convertDpToPx(48), convertDpToPx(48));
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

        floatingIconContainer.addView(floatingHomeIcon);

        // Add to main layout (this would be added to the FrameLayout parent)
        // parent.addView(floatingIconContainer);
    }

    private void loadIconFromBase64() {
        try {
            // This would load the icon from a native method
            // For now, just set a placeholder
            Logx.d("Icon loading placeholder - would load from native method");
        } catch (Exception e) {
            Logx.e("Error loading icon: " + e.getMessage(), e);
        }
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}