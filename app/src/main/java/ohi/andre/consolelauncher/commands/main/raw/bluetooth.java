package ohi.andre.consolelauncher.commands.main.raw;

import android.bluetooth.BluetoothAdapter;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class bluetooth implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter == null) return info.context.getString(R.string.output_bluetooth_unavailable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(info.context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            if (info.context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) info.context,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, LauncherActivity.COMMAND_REQUEST_PERMISSION);
                return info.context.getString(R.string.output_waitingpermission);
            }
            return "Grant Nearby devices permission to control Bluetooth.";
        }

        try {
            if(adapter.isEnabled()) {
                adapter.disable();
                return info.context.getString(R.string.output_bluetooth) + " false";
            } else {
                adapter.enable();
                return info.context.getString(R.string.output_bluetooth) + " true";
            }
        } catch (SecurityException e) {
            return "Bluetooth permission was denied. Grant Nearby devices permission and try again.";
        }
    }

    @Override
    public int helpRes() {
        return R.string.help_bluetooth;
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
