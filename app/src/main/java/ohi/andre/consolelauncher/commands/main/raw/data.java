package ohi.andre.consolelauncher.commands.main.raw;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

/** Opens Android's connectivity controls; regular apps cannot toggle mobile data directly. */
public class data implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        Intent intent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                : new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        if (intent.resolveActivity(pack.context.getPackageManager()) == null) {
            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        }
        if (intent.resolveActivity(pack.context.getPackageManager()) == null) {
            intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        }
        if (!(pack.context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            pack.context.startActivity(intent);
            return pack.context.getString(R.string.output_data_open_settings);
        } catch (ActivityNotFoundException | SecurityException e) {
            return pack.context.getString(R.string.output_data_settings_failed);
        }
    }

    @Override
    public int helpRes() {
        return R.string.help_data;
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
