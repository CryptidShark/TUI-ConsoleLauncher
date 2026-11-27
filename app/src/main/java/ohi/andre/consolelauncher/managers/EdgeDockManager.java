package ohi.andre.consolelauncher.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONException;

/** User-configured short aliases shown in the optional in-app edge dock. */
public final class EdgeDockManager {
    private static final String PREFS = "edge_dock";
    private static final String ITEMS = "items";
    private static final String ENABLED = "enabled";
    private static final String ICONS = "icons";

    private EdgeDockManager() {}

    public static List<String> getItems(Context context) {
        String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ITEMS, "");
        List<String> items = new ArrayList<>();
        if (value != null && !value.trim().isEmpty()) {
            for (String item : value.split("\\n")) if (!item.trim().isEmpty()) items.add(item.trim());
        }
        return items;
    }

    public static boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ENABLED, enabled).apply();
    }

    /** Optional Unicode glyph/emoji shown instead of the alias initials. */
    public static String getIcon(Context context, String alias) {
        try {
            String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ICONS, "{}");
            return new JSONObject(json == null ? "{}" : json).optString(alias, "");
        } catch (JSONException ignored) {
            return "";
        }
    }

    public static boolean setIcon(Context context, String alias, String icon) {
        if (alias == null || alias.trim().isEmpty()) return false;
        String glyph = icon == null ? "" : icon.trim();
        if (glyph.contains("\n") || glyph.contains("\r") || glyph.codePointCount(0, glyph.length()) > 8) return false;
        try {
            String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ICONS, "{}");
            JSONObject icons = new JSONObject(json == null ? "{}" : json);
            if (glyph.isEmpty()) icons.remove(alias);
            else icons.put(alias, glyph);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ICONS, icons.toString()).apply();
            return true;
        } catch (JSONException ignored) {
            return false;
        }
    }

    public static boolean add(Context context, String alias) {
        if (alias == null || alias.trim().isEmpty() || alias.contains("\n")) return false;
        List<String> items = getItems(context);
        if (items.contains(alias.trim())) return true;
        if (items.size() >= 8) return false;
        items.add(alias.trim());
        save(context, items);
        return true;
    }

    public static boolean remove(Context context, String alias) {
        List<String> items = getItems(context);
        boolean removed = items.remove(alias);
        if (removed) {
            save(context, items);
            setIcon(context, alias, "");
        }
        return removed;
    }

    private static void save(Context context, List<String> items) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, android.text.TextUtils.join("\n", items)).apply();
    }
}
