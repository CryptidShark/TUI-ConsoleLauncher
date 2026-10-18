package ohi.andre.consolelauncher.commands.main.raw;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.PrivacyCenter;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.BusyBoxInstaller;

/** Diagnoses common setup blockers without changing device settings. */
public class doctor implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        StringBuilder report = new StringBuilder("CYTHERNOIR DIAGNOSTICS\n")
                .append("Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");
        add(report, "Default home app", info.widgetManager != null && info.widgetManager.isDefaultLauncher(), "Set Cythernoir/T-UI as default Home in Android settings.");
        add(report, "Notification listener", PrivacyCenter.hasNotificationAccess(pack.context), "Run privacy and enable Notification access for music controls and notification features.");
        add(report, "Music library", PrivacyCenter.hasAudioPermission(pack.context), "Run privacy and grant Music library access to scan local audio.");
        boolean notifications = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(pack.context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        add(report, "Post notifications", notifications, "Run privacy and allow notifications in app settings.");
        add(report, "Contacts", ContextCompat.checkSelfPermission(pack.context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED, "Grant Contacts only if you use contact commands.");
        add(report, "Music player module", XMLPrefsManager.getBoolean(Behavior.enable_music), "Enable the integrated player in settings, or control an external media session.");
        add(report, "BusyBox", BusyBoxInstaller.isInstalled(pack.context), "Optional: run bbman -install. Requires Internet.");
        int widgets = info.widgetManager == null ? 0 : info.widgetManager.getActiveWidgetIds().size();
        report.append("Widgets: ").append(widgets).append(" saved\n");
        report.append("\nRun `privacy` to review permissions or `help` to list commands.");
        return report.toString();
    }

    private static void add(StringBuilder report, String label, boolean ready, String fix) {
        report.append(ready ? "[OK]   " : "[TODO] ").append(label);
        if (!ready) report.append(" — ").append(fix);
        report.append('\n');
    }

    @Override public int[] argType() { return new int[0]; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_doctor; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return null; }
}
