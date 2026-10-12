package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class TermuxBridge {

    public static final String TERMUX_PACKAGE = "com.termux";
    public static final String TERMUX_RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND";
    public static final String ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND";

    public static boolean isTermuxInstalled(Context context) {
        if (context == null) return false;
        try {
            context.getPackageManager().getPackageInfo(TERMUX_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean hasRunCommandPermission(Context context) {
        if (context == null) return false;
        return context.checkCallingOrSelfPermission(TERMUX_RUN_COMMAND_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static String getTermuxStatus(Context context) {
        if (!isTermuxInstalled(context)) {
            return "Termux: NOT_INSTALLED";
        }
        boolean hasPerm = hasRunCommandPermission(context);
        return "Termux: INSTALLED // RunCommandPermission: " + (hasPerm ? "GRANTED" : "NOT_GRANTED");
    }

    public static void launchTermux(Context context) {
        if (context != null && isTermuxInstalled(context)) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(TERMUX_PACKAGE);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
