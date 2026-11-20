package ohi.andre.consolelauncher.widgets.hud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

/** Lightweight, real palette preview rendered inside the launcher output stream. */
public class ThemePreviewCard extends LinearLayout {
    public ThemePreviewCard(Context context) {
        this(context, ThemeEngine.Preset.CLASSIC_TERMINAL);
    }

    public ThemePreviewCard(Context context, AttributeSet attrs) {
        this(context, ThemeEngine.Preset.CLASSIC_TERMINAL);
    }

    public ThemePreviewCard(Context context, ThemeEngine.Preset preset) {
        super(context);
        ThemeEngine.DesignTokens colors = ThemeEngine.getBasePreset(preset);
        int pad = Math.round(Tuils.dpToPx(context, 14));
        setOrientation(VERTICAL);
        setPadding(pad, pad, pad, pad);
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(colors.background);
        panel.setCornerRadius(Tuils.dpToPx(context, Math.max(2, colors.borderRadius)));
        panel.setStroke(Math.max(1, Math.round(Tuils.dpToPx(context, colors.borderWidth))), colors.border);
        setBackground(panel);

        TextView heading = line(context, "╭─ " + preset.name().replace('_', ' ') + " ─────────╮", colors.secondary, 16, true);
        addView(heading);
        addView(line(context, "> status: READY   battery: 87%", colors.success, 13, false));
        addView(line(context, "> network: CONNECTED  ·  rx/tx: active", colors.info, 13, false));
        addView(line(context, "> prompt: ~/workspace  $ _", colors.primary, 13, true));

        LinearLayout swatches = new LinearLayout(context);
        swatches.setOrientation(HORIZONTAL);
        swatches.setGravity(Gravity.CENTER_VERTICAL);
        int[] sample = {colors.primary, colors.secondary, colors.accent, colors.success, colors.warning, colors.error};
        for (int color : sample) {
            TextView swatch = new TextView(context);
            swatch.setText("  ");
            GradientDrawable dot = new GradientDrawable();
            dot.setColor(color);
            dot.setCornerRadius(Tuils.dpToPx(context, 4));
            swatch.setBackground(dot);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.round(Tuils.dpToPx(context, 8)), 1f);
            lp.setMargins(0, pad / 2, pad / 3, 0);
            swatches.addView(swatch, lp);
        }
        addView(swatches);

        Button apply = new Button(context);
        apply.setAllCaps(false);
        apply.setText("Apply " + preset.name().replace('_', ' '));
        apply.setTextColor(colors.text);
        apply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colors.surface));
        LayoutParams buttonParams = new LayoutParams(-1, Math.round(Tuils.dpToPx(context, 44)));
        buttonParams.topMargin = pad / 2;
        addView(apply, buttonParams);
        apply.setOnClickListener(v -> {
            ThemeEngine.applyPreset(preset);
            if (context instanceof Reloadable) ((Reloadable) context).reload();
        });
    }

    private static TextView line(Context context, String text, int color, int size, boolean bold) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(Tuils.getTypeface(context), bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        view.setPadding(0, 0, 0, Math.round(Tuils.dpToPx(context, 4)));
        return view;
    }
}
