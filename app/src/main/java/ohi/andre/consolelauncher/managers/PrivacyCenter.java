package ohi.andre.consolelauncher.managers;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.managers.notifications.NotificationService;

/** Reads the grants that unlock optional launcher features. */
public final class PrivacyCenter {
    private PrivacyCenter() {}

    public static class PermissionStatus {
        public final String name;
        public final boolean granted;
        public final String purpose;

        public PermissionStatus(String name, boolean granted, String purpose) {
            this.name = name;
            this.granted = granted;
            this.purpose = purpose;
        }
    }

    public static PermissionStatus[] getPrivacyStatus(Context context) {
        if (context == null) return new PermissionStatus[0];
        return new PermissionStatus[]{
                new PermissionStatus("Default launcher", isDefaultLauncher(context), "Needed for the complete home-screen and widget experience."),
                new PermissionStatus("Notification access", hasNotificationAccess(context), "Reads notification and media-session information; required to control external music sessions."),
                new PermissionStatus("Post notifications", Build.VERSION.SDK_INT < 33 || granted(context, Manifest.permission.POST_NOTIFICATIONS), "Allows the launcher to show its own notifications."),
                new PermissionStatus("Music library", hasAudioPermission(context), "Allows the integrated player to scan local audio."),
                new PermissionStatus("Contacts", granted(context, Manifest.permission.READ_CONTACTS), "Used only by contact-related commands."),
                new PermissionStatus("Location", granted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        || granted(context, Manifest.permission.ACCESS_COARSE_LOCATION), "Used by location and weather features.")
        };
    }

    public static boolean hasNotificationAccess(Context context) {
        String listeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (listeners == null) return false;
        for (String component : listeners.split(":")) {
            ComponentName name = ComponentName.unflattenFromString(component);
            if (name != null && context.getPackageName().equals(name.getPackageName())
                    && NotificationService.class.getName().equals(name.getClassName())) return true;
        }
        return false;
    }

    public static boolean hasAudioPermission(Context context) {
        String permission = Build.VERSION.SDK_INT >= 33 ? "android.permission.READ_MEDIA_AUDIO" : Manifest.permission.READ_EXTERNAL_STORAGE;
        return granted(context, permission);
    }

    private static boolean granted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isDefaultLauncher(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roles = context.getSystemService(RoleManager.class);
                return roles != null && roles.isRoleHeld(RoleManager.ROLE_HOME);
            }
            Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            return context.getPackageName().equals(context.getPackageManager()
                    .resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName);
        } catch (Exception ignored) { return false; }
    }

    public static void openSettings(Context context, String name) {
        Intent intent;
        if ("Notification access".equals(name)) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        } else if ("Default launcher".equals(name)) {
            intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { context.startActivity(intent); }
        catch (Exception ignored) {
            Intent fallback = new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallback);
        }
    }
}
