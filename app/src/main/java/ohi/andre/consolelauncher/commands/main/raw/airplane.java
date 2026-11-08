package ohi.andre.consolelauncher.commands.main.raw;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

/** Opens Android's own controls because regular apps cannot toggle airplane mode directly. */
public class airplane implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        if (intent.resolveActivity(pack.context.getPackageManager()) == null) {
            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        }
        if (!(pack.context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            pack.context.startActivity(intent);
            return pack.context.getString(R.string.output_airplane_open_settings);
        } catch (ActivityNotFoundException | SecurityException e) {
            return pack.context.getString(R.string.output_airplane_settings_failed);
        }
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int helpRes() {
        return R.string.help_airplane;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
