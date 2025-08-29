package com.bearmod.loader.floating;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingTabManager - Manages tab navigation and content switching
 * Handles the creation and management of floating overlay tabs
 */
public class FloatingTabManager {

    private static final String TAG = "FloatingTabManager";

    private final Context context;

    // Tab configuration
    private static final String[] TAB_NAMES = {"Main", "ESP", "Aim", "Skin"};
    private static final String[] TAB_ICONS = {"⌂", "👁", "🎯", "👕"};

    // Tab components
    private Button[] tabButtons;
    private LinearLayout[] tabLayouts;
    private LinearLayout container;
    private LinearLayout expandableTabMenu;
    private View tabBackdrop;
    private Button floatingHomeIcon;

    // State management
    private int currentTabIndex = 0;
    private boolean isMenuExpanded = false;

    public FloatingTabManager(Context context) {
        this.context = context;
        Logx.d("FloatingTabManager initialized");
    }

    /**
     * Initialize tab system with layouts
     */
    public void initialize(LinearLayout container, LinearLayout[] tabLayouts) {
        this.container = container;
        this.tabLayouts = tabLayouts;

        createTabButtons();
        createExpandableTabMenu();
        createTabBackdrop();

        // Set initial tab
        switchToTab(0);
        Logx.d("FloatingTabManager initialized with " + TAB_NAMES.length + " tabs");
    }

    /**
     * Switch to specific tab
     */
    public void switchToTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= TAB_NAMES.length) {
            Logx.w("Invalid tab index: " + tabIndex);
            return;
        }

        try {
            // Update current tab index
            currentTabIndex = tabIndex;

            // Clear container and add selected tab layout
            if (container != null) {
                container.removeAllViews();
                if (tabLayouts != null && tabIndex < tabLayouts.length && tabLayouts[tabIndex] != null) {
                    container.addView(tabLayouts[tabIndex]);
                }
            }

            // Update tab button selection
            updateTabSelection(tabIndex);

            Logx.d("Switched to tab: " + TAB_NAMES[tabIndex] + " (index: " + tabIndex + ")");

        } catch (Exception e) {
            Logx.e("Error switching to tab " + tabIndex + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get current tab index
     */
    public int getCurrentTabIndex() {
        return currentTabIndex;
    }

    /**
     * Get tab name by index
     */
    public String getTabName(int index) {
        if (index >= 0 && index < TAB_NAMES.length) {
            return TAB_NAMES[index];
        }
        return "Unknown";
    }

    /**
     * Get tab icon by index
     */
    public String getTabIcon(int index) {
        if (index >= 0 && index < TAB_ICONS.length) {
            return TAB_ICONS[index];
        }
        return "•";
    }

    /**
     * Set floating home icon reference
     */
    public void setFloatingHomeIcon(Button floatingHomeIcon) {
        this.floatingHomeIcon = floatingHomeIcon;
        setupHomeIconClickListener();
    }

    /**
     * Handle tab button click
     */
    public void onTabButtonClick(int tabIndex) {
        Logx.d("Tab button clicked: " + getTabName(tabIndex));

        // Trigger haptic feedback
        triggerHapticFeedback("LIGHT");

        // Switch to tab
        switchToTab(tabIndex);

        // Collapse menu after selection
        collapseTabMenu();
    }

    /**
     * Toggle expandable tab menu
     */
    public void toggleTabMenu() {
        if (isMenuExpanded) {
            collapseTabMenu();
        } else {
            expandTabMenu();
        }
    }

    /**
     * Expand tab menu with animation
     */
    public void expandTabMenu() {
        if (isMenuExpanded || tabBackdrop == null || expandableTabMenu == null) {
            return;
        }

        try {
            Logx.d("Expanding tab menu");

            // Update home icon
            if (floatingHomeIcon != null) {
                floatingHomeIcon.setText("✕");
            }

            // Show backdrop with animation
            tabBackdrop.setVisibility(View.VISIBLE);
            tabBackdrop.setAlpha(0f);
            tabBackdrop.animate()
                .alpha(1f)
                .setDuration(200)
                .start();

            // Show expandable menu with animation
            expandableTabMenu.setVisibility(View.VISIBLE);
            expandableTabMenu.setAlpha(0f);
            expandableTabMenu.setTranslationX(-convertDpToPx(100));
            expandableTabMenu.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(200)
                .start();

            isMenuExpanded = true;

        } catch (Exception e) {
            Logx.e("Error expanding tab menu: " + e.getMessage(), e);
        }
    }

    /**
     * Collapse tab menu with animation
     */
    public void collapseTabMenu() {
        if (!isMenuExpanded || tabBackdrop == null || expandableTabMenu == null) {
            return;
        }

        try {
            Logx.d("Collapsing tab menu");

            // Update home icon
            if (floatingHomeIcon != null) {
                floatingHomeIcon.setText("☰");
            }

            // Hide backdrop with animation
            tabBackdrop.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> tabBackdrop.setVisibility(View.GONE))
                .start();

            // Hide expandable menu with animation
            expandableTabMenu.animate()
                .alpha(0f)
                .translationX(-convertDpToPx(100))
                .setDuration(200)
                .withEndAction(() -> expandableTabMenu.setVisibility(View.GONE))
                .start();

            isMenuExpanded = false;

        } catch (Exception e) {
            Logx.e("Error collapsing tab menu: " + e.getMessage(), e);
        }
    }

    /**
     * Check if menu is expanded
     */
    public boolean isMenuExpanded() {
        return isMenuExpanded;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingTabManager resources...");

            // Clear references
            tabButtons = null;
            tabLayouts = null;
            container = null;
            expandableTabMenu = null;
            tabBackdrop = null;
            floatingHomeIcon = null;

            Logx.d("FloatingTabManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingTabManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void createTabButtons() {
        tabButtons = new Button[TAB_NAMES.length];

        for (int i = 0; i < TAB_NAMES.length; i++) {
            tabButtons[i] = createTabButton(i);
        }
    }

    private Button createTabButton(int index) {
        Button tabButton = new Button(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            convertDpToPx(40), convertDpToPx(40));
        params.setMargins(convertDpToPx(4), 0, convertDpToPx(4), 0);
        tabButton.setLayoutParams(params);

        // iOS-style tab button design
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#3A3A3C"));
        bg.setStroke(convertDpToPx(1), Color.parseColor("#48484A"));
        tabButton.setBackground(bg);

        tabButton.setText(getTabIcon(index));
        tabButton.setTextColor(Color.parseColor("#8E8E93"));
        tabButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tabButton.setTag(index);

        // Set click listener
        final int tabIndex = index;
        tabButton.setOnClickListener(v -> onTabButtonClick(tabIndex));

        return tabButton;
    }

    private void createExpandableTabMenu() {
        expandableTabMenu = new LinearLayout(context);
        expandableTabMenu.setOrientation(LinearLayout.HORIZONTAL);
        expandableTabMenu.setVisibility(View.GONE);
        expandableTabMenu.setPadding(convertDpToPx(8), 0, convertDpToPx(8), 0);

        // Add tab buttons to expandable menu
        for (Button tabButton : tabButtons) {
            expandableTabMenu.addView(tabButton);
        }
    }

    private void createTabBackdrop() {
        // This would be created in the UI manager and passed here
        // For now, just initialize as null
        tabBackdrop = null;
    }

    private void setupHomeIconClickListener() {
        if (floatingHomeIcon != null) {
            floatingHomeIcon.setOnClickListener(v -> {
                triggerHapticFeedback("MEDIUM");
                toggleTabMenu();
            });
        }
    }

    private void updateTabSelection(int selectedIndex) {
        if (tabButtons == null) {
            return;
        }

        for (int i = 0; i < tabButtons.length; i++) {
            updateTabButtonAppearance(tabButtons[i], i == selectedIndex);
        }
    }

    private void updateTabButtonAppearance(Button tabButton, boolean isSelected) {
        if (tabButton == null) {
            return;
        }

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        if (isSelected) {
            // Selected tab - iOS blue
            bg.setColor(Color.parseColor("#007AFF"));
            bg.setStroke(convertDpToPx(1), Color.parseColor("#0051D5"));
            tabButton.setTextColor(Color.WHITE);
        } else {
            // Unselected tab - iOS gray
            bg.setColor(Color.parseColor("#3A3A3C"));
            bg.setStroke(convertDpToPx(1), Color.parseColor("#48484A"));
            tabButton.setTextColor(Color.parseColor("#8E8E93"));
        }

        tabButton.setBackground(bg);
    }

    private void triggerHapticFeedback(String type) {
        // This would be handled by the visual effects manager
        // For now, just log
        Logx.d("Haptic feedback triggered: " + type);
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}