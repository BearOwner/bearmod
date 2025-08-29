package com.bearmod.loader.floating;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.bearmod.loader.utilities.Logx;

/**
 * FloatingMenuManager - Manages menu components (switches, seekbars, combos)
 * Handles the creation and management of floating overlay menu controls
 */
public class FloatingMenuManager {

    private static final String TAG = "FloatingMenuManager";

    private final Context context;

    public FloatingMenuManager(Context context) {
        this.context = context;
        Logx.d("FloatingMenuManager initialized");
    }

    /**
     * Add a switch control to a layout
     */
    public void addSwitch(LinearLayout parent, String englishText, String chineseText,
                         boolean isChecked, CompoundButton.OnCheckedChangeListener listener) {
        try {
            String currentLabel = getLocalizedText(englishText, chineseText);

            // iOS-style container with compact design
            LinearLayout containerLayout = new LinearLayout(context);
            containerLayout.setOrientation(LinearLayout.HORIZONTAL);
            containerLayout.setGravity(Gravity.CENTER_VERTICAL);
            containerLayout.setPadding(convertDpToPx(12), convertDpToPx(8),
                convertDpToPx(12), convertDpToPx(8));

            // iOS-style container background with shadow
            GradientDrawable containerBg = new GradientDrawable();
            containerBg.setColor(Color.parseColor("#2C2C2E")); // iOS secondary background
            containerBg.setCornerRadius(convertDpToPx(12));
            containerBg.setStroke(convertDpToPx(1), Color.parseColor("#38383A"));
            containerLayout.setBackground(containerBg);

            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            containerParams.setMargins(convertDpToPx(8), convertDpToPx(4),
                convertDpToPx(8), convertDpToPx(4));
            containerLayout.setLayoutParams(containerParams);

            // Add elevation for modern depth effect
            containerLayout.setElevation(convertDpToPx(2));

            // Label text
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.weight = 1;

            TextView labelText = new TextView(context);
            labelText.setText(currentLabel);
            labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            labelText.setTextColor(Color.parseColor("#FFFFFF")); // iOS primary text
            labelText.setTypeface(Typeface.DEFAULT);
            labelText.setLayoutParams(textParams);

            // Switch control
            LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            switchParams.gravity = Gravity.END;

            Switch switchButton = new Switch(context);
            switchButton.setChecked(isChecked);
            switchButton.setLayoutParams(switchParams);

            // iOS-style switch colors
            switchButton.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
            switchButton.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#34C759")));

            // Enhanced switch listener with haptic feedback
            switchButton.setOnCheckedChangeListener((buttonView, isCheckedNew) -> {
                triggerHapticFeedback(isCheckedNew ? "SUCCESS" : "LIGHT");

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
                            .start();
                    })
                    .start();

                if (listener != null) {
                    listener.onCheckedChanged(buttonView, isCheckedNew);
                }
            });

            containerLayout.addView(labelText);
            containerLayout.addView(switchButton);
            parent.addView(containerLayout);

            // Add spacing line
            addSpacingLine(parent);

            Logx.d("Switch added: " + currentLabel);

        } catch (Exception e) {
            Logx.e("Error adding switch '" + englishText + "': " + e.getMessage(), e);
        }
    }

    /**
     * Add a seekbar control to a layout
     */
    public void addSeekbar(LinearLayout parent, String englishText, String chineseText,
                          int min, int max, int value, SeekBar.OnSeekBarChangeListener listener) {
        try {
            String currentText = getLocalizedText(englishText, chineseText);

            LinearLayout containerLayout = new LinearLayout(context);
            containerLayout.setOrientation(LinearLayout.VERTICAL);
            containerLayout.setPadding(convertDpToPx(10), convertDpToPx(6),
                convertDpToPx(10), convertDpToPx(6));

            // Label text
            TextView textV = new TextView(context);
            textV.setText(currentText);
            textV.setTextSize(14);
            textV.setTextColor(Color.parseColor("#E0E0E0"));
            textV.setTypeface(Typeface.DEFAULT_BOLD);
            textV.setPadding(convertDpToPx(4), 0, convertDpToPx(4), convertDpToPx(6));

            // Seekbar row
            LinearLayout seekbarRow = new LinearLayout(context);
            seekbarRow.setOrientation(LinearLayout.HORIZONTAL);
            seekbarRow.setGravity(Gravity.CENTER_VERTICAL);
            seekbarRow.setPadding(convertDpToPx(4), 0, convertDpToPx(4), 0);

            // Value display
            final TextView textValue = new TextView(context);
            textValue.setText(String.valueOf(value > 0 ? value : min));
            textValue.setTextSize(13);
            textValue.setTextColor(Color.parseColor("#64B5F6"));
            textValue.setTypeface(Typeface.DEFAULT_BOLD);
            textValue.setGravity(Gravity.CENTER);
            textValue.setMinWidth(convertDpToPx(35));

            // Seekbar
            SeekBar seekBar = new SeekBar(context);
            seekBar.setMax(max);
            seekBar.setMin(min);
            seekBar.setProgress(value > 0 ? value : min);
            seekBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#64B5F6")));

            LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            seekBarParams.setMargins(convertDpToPx(4), 0, convertDpToPx(4), 0);
            seekBar.setLayoutParams(seekBarParams);

            // Enhanced seekbar listener
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < min) {
                        progress = min;
                        seekBar.setProgress(progress);
                    }
                    textValue.setText(String.valueOf(progress));

                    // Animation feedback
                    textValue.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(150)
                        .withEndAction(() -> textValue.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start());

                    if (listener != null) {
                        listener.onProgressChanged(seekBar, progress, fromUser);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        listener.onStartTrackingTouch(seekBar);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (listener != null) {
                        listener.onStopTrackingTouch(seekBar);
                    }
                }
            });

            // Container background
            GradientDrawable containerBg = new GradientDrawable();
            containerBg.setColor(Color.parseColor("#202020"));
            containerBg.setCornerRadius(convertDpToPx(8));
            containerBg.setStroke(convertDpToPx(1), Color.parseColor("#2C2C2C"));
            containerLayout.setBackground(containerBg);

            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            containerParams.setMargins(convertDpToPx(6), convertDpToPx(3),
                convertDpToPx(6), convertDpToPx(3));
            containerLayout.setLayoutParams(containerParams);

            // Add views
            seekbarRow.addView(seekBar);
            seekbarRow.addView(textValue);
            containerLayout.addView(textV);
            containerLayout.addView(seekbarRow);
            parent.addView(containerLayout);

            addSpacing(parent, 3);

            Logx.d("Seekbar added: " + currentText);

        } catch (Exception e) {
            Logx.e("Error adding seekbar '" + englishText + "': " + e.getMessage(), e);
        }
    }

    /**
     * Add a combo box (dropdown) control
     */
    public void addCombo(LinearLayout parent, String englishLabel, String chineseLabel,
                        String[] options, String configKey) {
        try {
            String currentLabel = getLocalizedText(englishLabel, chineseLabel);

            // Container with subtle elevation
            RelativeLayout containerLayout = new RelativeLayout(context);
            containerLayout.setBackground(createBackground());
            containerLayout.setPadding(convertDpToPx(12), convertDpToPx(8),
                convertDpToPx(12), convertDpToPx(8));

            // Horizontal layout
            LinearLayout rowLayout = new LinearLayout(context);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
            rowLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

            // Label text
            TextView labelText = new TextView(context);
            labelText.setText(currentLabel);
            labelText.setTextSize(13);
            labelText.setTypeface(Typeface.DEFAULT_BOLD);
            labelText.setTextColor(Color.parseColor("#383838"));
            labelText.setPadding(convertDpToPx(2), convertDpToPx(2), convertDpToPx(8), convertDpToPx(2));
            labelText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            labelText.setLayoutParams(labelParams);

            // Options will be added here in a scrollable container
            // For now, just add a placeholder
            TextView placeholder = new TextView(context);
            placeholder.setText("Options placeholder");
            placeholder.setTextSize(12);
            placeholder.setTextColor(Color.parseColor("#666666"));

            rowLayout.addView(labelText);
            rowLayout.addView(placeholder);
            containerLayout.addView(rowLayout);

            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            containerParams.setMargins(convertDpToPx(4), convertDpToPx(4),
                convertDpToPx(4), convertDpToPx(4));
            containerLayout.setLayoutParams(containerParams);

            parent.addView(containerLayout);

            Logx.d("Combo added: " + currentLabel);

        } catch (Exception e) {
            Logx.e("Error adding combo '" + englishLabel + "': " + e.getMessage(), e);
        }
    }

    /**
     * Add spacing to layout
     */
    public void addSpacing(LinearLayout parent, int height) {
        View spaceView = new View(context);
        spaceView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, height));
        parent.addView(spaceView);
    }

    /**
     * Add a text label to layout
     */
    public void addText(LinearLayout parent, String text, float size, int color) {
        try {
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextColor(color);
            textView.setShadowLayer(5, 5, 5, Color.BLACK);
            textView.setPadding(15, 15, 15, 15);
            textView.setTextSize(convertDpToPx(size));
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            parent.addView(textView);

            Logx.d("Text added: " + text);

        } catch (Exception e) {
            Logx.e("Error adding text '" + text + "': " + e.getMessage(), e);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            Logx.d("Cleaning up FloatingMenuManager resources...");
            // No specific cleanup needed for this component
            Logx.d("FloatingMenuManager cleanup completed");

        } catch (Exception e) {
            Logx.e("Error during FloatingMenuManager cleanup: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private String getLocalizedText(String englishText, String chineseText) {
        // This would check the current language setting
        // For now, return English by default
        return englishText != null ? englishText : chineseText;
    }

    private void addSpacingLine(LinearLayout parent) {
        View lineView = new View(context);
        lineView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lineParams.setMargins(20, 5, 20, 10);
        lineView.setLayoutParams(lineParams);
        parent.addView(lineView);
    }

    private GradientDrawable createBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(10);
        return drawable;
    }

    private void triggerHapticFeedback(String type) {
        // This would be handled by the visual effects manager
        Logx.d("Haptic feedback triggered: " + type);
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private int convertDpToPx(float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}