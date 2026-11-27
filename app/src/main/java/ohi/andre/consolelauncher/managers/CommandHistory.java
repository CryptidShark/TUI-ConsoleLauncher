package ohi.andre.consolelauncher.managers;

import android.content.Context;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;

/** Small, bounded command history. Persistent history is disabled in incognito mode. */
public final class CommandHistory {
    private static final String PREFS = "cli_command_history";
    private static final String ITEMS = "items";
    private static final int MAX_ITEMS = 100;

    private CommandHistory() {}

    public static synchronized void record(Context context, String command) {
        if (context == null || command == null || command.trim().isEmpty()
                || XMLPrefsManager.getBoolean(Behavior.incognito_mode)) return;
        List<String> items = get(context);
        String normalized = command.trim();
        if (normalized.equalsIgnoreCase("history") || normalized.regionMatches(true, 0, "history ", 0, 8)) return;
        if (!items.isEmpty() && normalized.equals(items.get(0))) return;
        items.add(0, normalized);
        while (items.size() > MAX_ITEMS) items.remove(items.size() - 1);
        save(context, items);
    }

    public static synchronized List<String> get(Context context) {
        List<String> items = new ArrayList<>();
        if (context == null || XMLPrefsManager.getBoolean(Behavior.incognito_mode)) return items;
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ITEMS, "[]");
        try {
            JSONArray json = new JSONArray(raw == null ? "[]" : raw);
            for (int i = 0; i < json.length() && items.size() < MAX_ITEMS; i++) {
                String command = json.optString(i, "").trim();
                if (!command.isEmpty()) items.add(command);
            }
        } catch (Exception ignored) { }
        return items;
    }

    public static synchronized void clear(Context context) {
        if (context != null) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(ITEMS).apply();
    }

    private static void save(Context context, List<String> items) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ITEMS, new JSONArray(items).toString()).apply();
    }
}
