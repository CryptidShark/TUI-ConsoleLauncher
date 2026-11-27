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
    private static final String SIDE = "side";
    private static final String SIZE = "size";

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

    public static String getSide(Context context) {
        String side = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(SIDE, "right");
        return "left".equalsIgnoreCase(side) ? "left" : "right";
    }

    public static boolean setSide(Context context, String side) {
        if (side == null || !(side.equalsIgnoreCase("left") || side.equalsIgnoreCase("right"))) return false;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(SIDE, side.toLowerCase(java.util.Locale.ROOT)).apply();
        return true;
    }

    public static String getSize(Context context) {
        String size = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(SIZE, "normal");
        if ("compact".equalsIgnoreCase(size) || "large".equalsIgnoreCase(size)) return size.toLowerCase(java.util.Locale.ROOT);
        return "normal";
    }

    public static boolean setSize(Context context, String size) {
        if (size == null || !(size.equalsIgnoreCase("compact") || size.equalsIgnoreCase("normal") || size.equalsIgnoreCase("large"))) return false;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(SIZE, size.toLowerCase(java.util.Locale.ROOT)).apply();
        return true;
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

    public static JSONObject exportSettings(Context context) {
        JSONObject result = new JSONObject();
        JSONObject icons;
        try {
            icons = new JSONObject(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ICONS, "{}"));
        } catch (JSONException ignored) {
            icons = new JSONObject();
        }
        try {
            result.put("enabled", isEnabled(context));
            result.put("side", getSide(context));
            result.put("size", getSize(context));
            result.put("items", new org.json.JSONArray(getItems(context)));
            result.put("icons", icons);
        } catch (JSONException ignored) { }
        return result;
    }

    public static boolean restoreSettings(Context context, JSONObject settings) {
        if (context == null || settings == null) return false;
        try {
            String side = settings.optString("side", "right");
            String size = settings.optString("size", "normal");
            org.json.JSONArray itemJson = settings.optJSONArray("items");
            JSONObject icons = settings.optJSONObject("icons");
            if (!(side.equals("left") || side.equals("right"))
                    || !(size.equals("compact") || size.equals("normal") || size.equals("large"))
                    || itemJson == null || itemJson.length() > 8 || icons == null) return false;
            List<String> items = new ArrayList<>();
            for (int i = 0; i < itemJson.length(); i++) {
                if (!(itemJson.opt(i) instanceof String)) return false;
                String alias = itemJson.getString(i).trim();
                if (alias.isEmpty() || alias.contains("\n") || alias.contains("\r") || items.contains(alias)) return false;
                items.add(alias);
            }
            java.util.Iterator<String> keys = icons.keys();
            while (keys.hasNext()) {
                String alias = keys.next();
                if (!(icons.opt(alias) instanceof String)) return false;
                String glyph = icons.optString(alias, "");
                if (!items.contains(alias) || glyph.contains("\n") || glyph.contains("\r")
                        || glyph.codePointCount(0, glyph.length()) > 8) return false;
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putBoolean(ENABLED, settings.optBoolean("enabled", false))
                    .putString(SIDE, side)
                    .putString(SIZE, size)
                    .putString(ITEMS, android.text.TextUtils.join("\n", items))
                    .putString(ICONS, icons.toString())
                    .apply();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void save(Context context, List<String> items) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, android.text.TextUtils.join("\n", items)).apply();
    }
}
