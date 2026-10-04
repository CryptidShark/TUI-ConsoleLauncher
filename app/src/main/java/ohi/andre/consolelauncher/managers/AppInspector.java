package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class AppInspector {

    public static class AppDetails {
        public final String packageName;
        public final String versionName;
        public final long versionCode;
        public final int targetSdkVersion;
        public final int minSdkVersion;

        public AppDetails(String packageName, String versionName, long versionCode, int targetSdkVersion, int minSdkVersion) {
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.targetSdkVersion = targetSdkVersion;
            this.minSdkVersion = minSdkVersion;
        }
    }

    public static AppDetails inspect(Context context, String packageName) {
        if (context == null || packageName == null) return null;

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, 0);

            long versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                    info.getLongVersionCode() : info.versionCode;

            int targetSdk = info.applicationInfo != null ? info.applicationInfo.targetSdkVersion : 0;
            int minSdk = info.applicationInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                    info.applicationInfo.minSdkVersion : 0;

            return new AppDetails(packageName, info.versionName, versionCode, targetSdk, minSdk);
        } catch (Exception e) {
            return null;
        }
    }
}
