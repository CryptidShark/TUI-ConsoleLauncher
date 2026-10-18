package ohi.andre.consolelauncher.commands.main.raw;

import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.managers.PrivacyCenter;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Shows optional permissions and links to the relevant Android settings. */
public class privacy implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        ThemeEngine.Preset preset;
        try { preset = ThemeEngine.Preset.valueOf(XMLPrefsManager.get(Behavior.theme_preset)); }
        catch (Exception ignored) { preset = ThemeEngine.Preset.CLASSIC_TERMINAL; }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);
        LinearLayout card = new LinearLayout(pack.context);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = Tuils.dpToPx(pack.context, 12);
        card.setPadding(pad, pad, pad, pad);
        TextView heading = new TextView(pack.context);
        heading.setText("PRIVACY & PERMISSIONS");
        heading.setTextColor(theme.primary);
        heading.setTextSize(14);
        heading.setTypeface(Tuils.getTypeface(pack.context));
        card.addView(heading);

        for (PrivacyCenter.PermissionStatus status : PrivacyCenter.getPrivacyStatus(pack.context)) {
            TextView row = new TextView(pack.context);
            row.setText((status.granted ? "● READY  " : "○ NEEDS SETUP  ") + status.name + "\n" + status.purpose
                    + (status.granted ? "" : "\nTap to open Android settings."));
            row.setTextColor(status.granted ? theme.text : theme.warning);
            row.setTextSize(12);
            row.setTypeface(Tuils.getTypeface(pack.context));
            row.setPadding(pad, pad / 2, pad, pad / 2);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(theme.surface);
            bg.setCornerRadius(Tuils.dpToPx(pack.context, 6));
            bg.setStroke(Tuils.dpToPx(pack.context, 1), theme.border);
            row.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Tuils.dpToPx(pack.context, 6);
            card.addView(row, lp);
            row.setClickable(true);
            row.setOnClickListener(v -> PrivacyCenter.openSettings(pack.context, status.name));
        }
        Tuils.sendOutput(pack.context, card);
        return null;
    }

    @Override public int[] argType() { return new int[0]; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_privacy; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return null; }
}
