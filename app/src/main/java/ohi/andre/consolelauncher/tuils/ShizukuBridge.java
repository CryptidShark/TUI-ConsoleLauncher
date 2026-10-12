package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.content.pm.PackageManager;

public class ShizukuBridge {

    public static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";
    public static final String SHIZUKU_ALT_PACKAGE = "rikka.shizuku";

    public static boolean isShizukuInstalled(Context context) {
        if (context == null) return false;
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(SHIZUKU_PACKAGE, 0);
            return true;
        } catch (Exception e1) {
            try {
                pm.getPackageInfo(SHIZUKU_ALT_PACKAGE, 0);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    public static String getShizukuStatus(Context context) {
        if (!isShizukuInstalled(context)) {
            return "Shizuku: NOT_DETECTED (Optional ADB integration service)";
        }
        return "Shizuku: INSTALLED (Optional system bridge available upon user grant)";
    }
}
