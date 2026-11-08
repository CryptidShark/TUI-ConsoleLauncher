package ohi.andre.consolelauncher.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/** User-configured short aliases shown in the optional in-app edge dock. */
public final class EdgeDockManager {
    private static final String PREFS = "edge_dock";
    private static final String ITEMS = "items";
    private static final String ENABLED = "enabled";

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
        if (removed) save(context, items);
        return removed;
    }

    private static void save(Context context, List<String> items) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, android.text.TextUtils.join("\n", items)).apply();
    }
}
