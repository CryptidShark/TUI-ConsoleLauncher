package ohi.andre.consolelauncher.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PrivacyCenter {

    public static class PermissionStatus {
        public final String name;
        public final boolean granted;

        public PermissionStatus(String name, boolean granted) {
            this.name = name;
            this.granted = granted;
        }
    }

    public static PermissionStatus[] getPrivacyStatus(Context context) {
        if (context == null) return new PermissionStatus[0];

        boolean notifGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;

        return new PermissionStatus[] {
                new PermissionStatus("Notification Access", notifGranted),
                new PermissionStatus("Contacts Access", ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED),
                new PermissionStatus("Location Access", ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        };
    }
}
