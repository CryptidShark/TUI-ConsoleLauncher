package ohi.andre.consolelauncher.managers;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

public class UsageStatsManager {

    private static final Map<String, Integer> sAppLaunchCounts = new HashMap<>();

    public static void recordLaunch(String packageName) {
        if (packageName == null) return;
        int count = sAppLaunchCounts.containsKey(packageName) ? sAppLaunchCounts.get(packageName) : 0;
        sAppLaunchCounts.put(packageName, count + 1);
    }

    public static int getLaunchCount(String packageName) {
        if (packageName == null) return 0;
        return sAppLaunchCounts.containsKey(packageName) ? sAppLaunchCounts.get(packageName) : 0;
    }

    public static void clearStats() {
        sAppLaunchCounts.clear();
    }
}
