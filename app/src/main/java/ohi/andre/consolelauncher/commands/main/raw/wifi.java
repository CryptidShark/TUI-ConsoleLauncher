package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class wifi implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        if (info.wifi == null)
            info.wifi = (WifiManager) info.context.getSystemService(Context.WIFI_SERVICE);
        if (info.wifi == null) return info.res.getString(R.string.output_wifi_unavailable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ rejects WifiManager.setWifiEnabled() for regular apps.
            // Let the user change the radio in the system-owned Wi-Fi panel instead.
            Intent panel = new Intent(Settings.Panel.ACTION_WIFI);
            if (panel.resolveActivity(info.context.getPackageManager()) == null) {
                panel = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
            }
            try {
                if (panel.resolveActivity(info.context.getPackageManager()) == null) {
                    panel = new Intent(Settings.ACTION_WIFI_SETTINGS);
                }
                if (!(info.context instanceof android.app.Activity)) {
                    panel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                info.context.startActivity(panel);
                return info.res.getString(R.string.output_wifi_open_settings);
            } catch (ActivityNotFoundException | SecurityException e) {
                return info.res.getString(R.string.output_wifi_settings_failed);
            }
        }

        try {
            boolean active = !info.wifi.isWifiEnabled();
            if (!info.wifi.setWifiEnabled(active)) {
                return info.res.getString(R.string.output_wifi_request_failed);
            }
            return info.res.getString(R.string.output_wifi_request_sent) + " " + active;
        } catch (SecurityException e) {
            return info.res.getString(R.string.output_wifi_request_failed);
        }
    }

    @Override
    public int helpRes() {
        return R.string.help_wifi;
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

}
