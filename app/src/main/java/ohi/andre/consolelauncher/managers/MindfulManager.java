package ohi.andre.consolelauncher.managers;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

/** Small opt-in list of apps that should pause before launching. */
public final class MindfulManager {
    private static final String PREFS = "mindful_apps";
    private static final String KEY_PACKAGES = "packages";
    private static final String KEY_BLOCK_COUNT = "block_count";

    private MindfulManager() {}

    public static boolean isMarked(Context context, String packageName) {
        return context != null && packageName != null
                && context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_PACKAGES, new HashSet<>()).contains(packageName);
    }

    public static Set<String> getMarked(Context context) {
        if (context == null) return new HashSet<>();
        return new HashSet<>(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_PACKAGES, new HashSet<>()));
    }

    public static void setMarked(Context context, String packageName, boolean marked) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return;
        Set<String> packages = getMarked(context);
        if (marked) packages.add(packageName);
        else packages.remove(packageName);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putStringSet(KEY_PACKAGES, packages).apply();
    }

    public static void recordBlock(Context context) {
        if (context == null) return;
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_BLOCK_COUNT, prefs.getInt(KEY_BLOCK_COUNT, 0) + 1).apply();
    }

    public static int getBlockCount(Context context) {
        return context == null ? 0 : context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_BLOCK_COUNT, 0);
    }
}
