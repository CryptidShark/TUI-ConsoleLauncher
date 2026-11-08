package ohi.andre.consolelauncher.commands.main.raw;

import android.os.Handler;
import android.os.Looper;

import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.MindfulManager;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Opt-in intention pause for selected apps. */
public class mindful implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = pack.args == null || pack.args.length == 0 ? "help" : String.valueOf(pack.args[0]).trim();
        String[] pieces = input.split("\\s+", 2);
        String action = pieces[0].toLowerCase();
        String appName = pieces.length > 1 ? pieces[1].trim() : "";

        if (action.equals("list")) {
            if (MindfulManager.getMarked(info.context).isEmpty()) return "No apps marked. Use: mindful mark <app>";
            return "Mindful apps (package names):\n" + Tuils.toPlanString(MindfulManager.getMarked(info.context), "\n");
        }
        if (action.equals("mark") || action.equals("unmark")) {
            if (appName.isEmpty()) return "Usage: mindful " + action + " <app name or package>";
            AppsManager.LaunchInfo app = info.appsManager.findLaunchInfoWithLabel(appName, AppsManager.SHOWN_APPS);
            String packageName = app != null ? app.componentName.getPackageName() : appName;
            if (app == null) {
                try { info.context.getPackageManager().getApplicationInfo(packageName, 0); }
                catch (Exception e) { return "App not found. Try its launcher name or package id."; }
            }
            boolean mark = action.equals("mark");
            MindfulManager.setMarked(info.context, packageName, mark);
            return (mark ? "Mindful pause enabled for " : "Mindful pause removed for ") + (app != null ? app.publicLabel : packageName) + ".";
        }
        if (action.equals("continue")) {
            if (info.pendingMindfulLaunch == null && info.pendingMindfulAction == null) return "There is no launch waiting for confirmation.";
            MainManager manager = info.context instanceof ohi.andre.consolelauncher.LauncherActivity
                    ? ((ohi.andre.consolelauncher.LauncherActivity) info.context).getMainManager() : null;
            if (manager == null || !manager.continueMindfulLaunch(info)) return "Could not resume the pending app.";
            return null;
        }
        if (action.equals("cancel")) {
            info.pendingMindfulLaunch = null;
            info.pendingMindfulAction = null;
            info.mindfulRequestVersion++;
            return "Pending app launch cancelled.";
        }
        if (action.equals("breathe")) {
            if (info.pendingMindfulLaunch == null && info.pendingMindfulAction == null) return "There is no launch waiting. Launch a marked app first.";
            AppsManager.LaunchInfo breathingApp = info.pendingMindfulLaunch;
            int requestVersion = info.mindfulRequestVersion;
            Tuils.sendOutput(info.context, "Breathe in… breathe out… continuing in 10 seconds. Type `mindful cancel` to stop.");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                MainManager manager = info.context instanceof ohi.andre.consolelauncher.LauncherActivity
                        ? ((ohi.andre.consolelauncher.LauncherActivity) info.context).getMainManager() : null;
                if (manager != null && (info.pendingMindfulLaunch == breathingApp || info.pendingMindfulAction != null)
                        && info.mindfulRequestVersion == requestVersion) manager.continueMindfulLaunch(info);
            }, 10_000L);
            return null;
        }
        return "Mindful commands:\n  mindful mark <app>\n  mindful unmark <app>\n  mindful list\n  mindful continue | breathe | cancel";
    }

    @Override public int[] argType() { return new int[]{PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return 0; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
}
