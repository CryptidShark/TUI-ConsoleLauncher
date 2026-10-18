package ohi.andre.consolelauncher.commands.main.raw;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.search.SearchResult;
import ohi.andre.consolelauncher.search.UniversalSearchEngine;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Search apps, shortcuts, commands, aliases, and settings from one terminal card. */
public class palette implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String query = joinArguments(pack.args);
        if (query.trim().isEmpty()) return "Usage: palette [app, command, alias, or workspace]";

        List<SearchResult> results = UniversalSearchEngine.search(info.context, query,
                info.appsManager, info.aliasManager, info.commandGroup);
        LinearLayout card = new LinearLayout(info.context);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = Tuils.dpToPx(info.context, 12);
        card.setPadding(pad, pad, pad, pad);

        ThemeEngine.Preset preset;
        try { preset = ThemeEngine.Preset.valueOf(XMLPrefsManager.get(Behavior.theme_preset)); }
        catch (Exception ignored) { preset = ThemeEngine.Preset.CLASSIC_TERMINAL; }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);

        TextView heading = new TextView(info.context);
        heading.setText("QUICK FIND  /  " + query.toUpperCase());
        heading.setTextColor(theme.primary);
        heading.setTextSize(13);
        heading.setTypeface(Tuils.getTypeface(info.context));
        heading.setPadding(0, 0, 0, Tuils.dpToPx(info.context, 8));
        card.addView(heading);

        if (results.isEmpty()) {
            TextView empty = new TextView(info.context);
            empty.setText("No matching apps, commands, aliases, or workspaces.");
            empty.setTextColor(theme.textMuted);
            empty.setTypeface(Tuils.getTypeface(info.context));
            card.addView(empty);
        } else {
            int count = Math.min(8, results.size());
            for (int i = 0; i < count; i++) {
                SearchResult result = results.get(i);
                TextView row = new TextView(info.context);
                row.setText(result.getTitle() + "\n" + result.getCategory().name() + "  ·  " + result.getSubtitle());
                row.setTextColor(theme.text);
                row.setTextSize(12);
                row.setTypeface(Tuils.getTypeface(info.context));
                row.setPadding(Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 8),
                        Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 8));
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(theme.surface);
                bg.setCornerRadius(Tuils.dpToPx(info.context, 6));
                bg.setStroke(Tuils.dpToPx(info.context, 1), theme.border);
                row.setBackground(bg);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = Tuils.dpToPx(info.context, 6);
                card.addView(row, lp);
                row.setClickable(true);
                row.setFocusable(true);
                row.setOnClickListener(v -> result.getAction().execute(info.context));
            }
        }
        Tuils.sendOutput(info.context, card);
        return null;
    }

    private static String joinArguments(Object[] args) {
        if (args == null) return "";
        List<String> values = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof List) {
                for (Object value : (List<?>) arg) values.add(String.valueOf(value));
            } else if (arg != null) values.add(String.valueOf(arg));
        }
        return Tuils.toPlanString(values, Tuils.SPACE);
    }

    @Override public int[] argType() { return new int[]{TEXTLIST}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_palette; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return "Usage: palette [query]"; }
}
