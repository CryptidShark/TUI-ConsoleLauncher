package ohi.andre.consolelauncher.commands.main.raw;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.managers.MindfulManager;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Reports Android's per-app foreground time for today (requires Usage Access). */
public class stats implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        Context context = pack.context;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return "Usage statistics require Android 5.1 or later.";
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps == null ? AppOpsManager.MODE_IGNORED : appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            try {
                context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return "Enable Usage access for Cythernoir Launcher, then run `stats` again.";
            } catch (Exception e) {
                return "Grant Usage access to Cythernoir Launcher in Android Settings, then run `stats` again.";
            }
        }

        android.app.usage.UsageStatsManager manager = (android.app.usage.UsageStatsManager)
                context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return "Android Usage Stats service is unavailable.";
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long now = System.currentTimeMillis();
        List<UsageStats> entries = manager.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                start.getTimeInMillis(), now);
        long totalMs = 0;
        UsageStats top = null;
        if (entries != null) {
            for (UsageStats entry : entries) {
                long foreground = entry.getTotalTimeInForeground();
                totalMs += foreground;
                if (top == null || foreground > top.getTotalTimeInForeground()) top = entry;
            }
        }
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.getDefault());
        StringBuilder report = new StringBuilder("Screen time today (since ")
                .append(time.format(new Date(start.getTimeInMillis()))).append("): ")
                .append(formatDuration(totalMs));
        if (top != null && top.getTotalTimeInForeground() > 0) {
            String label = top.getPackageName();
            try {
                label = context.getPackageManager().getApplicationLabel(
                        context.getPackageManager().getApplicationInfo(top.getPackageName(), 0)).toString();
            } catch (Exception ignored) {}
            report.append("\nMost used: ").append(label).append(" — ")
                    .append(formatDuration(top.getTotalTimeInForeground()));
        }
        report.append("\nMindful confirmations requested: ").append(MindfulManager.getBlockCount(context)).append(" (all time).");
        return report.toString();
    }

    private static String formatDuration(long millis) {
        long minutes = Math.max(0, millis / 60_000L);
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    @Override public int[] argType() { return new int[0]; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return 0; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
}
