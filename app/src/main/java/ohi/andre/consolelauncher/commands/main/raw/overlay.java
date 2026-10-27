package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.os.Build;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.services.TuiOverlayService;

public class overlay implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        Context context = info.context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ignore) {}
            return "Please grant 'Draw over other apps' permission in Android Settings, then run `overlay show` again.";
        }

        if (pack.args == null || pack.args.length == 0 || pack.args[0] == null) {
            return "Usage: overlay [show | hide]";
        }

        String arg = pack.args[0].toString().trim();
        if (arg.equalsIgnoreCase("show")) {
            Intent serviceIntent = new Intent(context, TuiOverlayService.class);
            context.startService(serviceIntent);
            return "Quick assistant overlay activated. Floating '>_' button displayed.";
        } else if (arg.equalsIgnoreCase("hide")) {
            Intent serviceIntent = new Intent(context, TuiOverlayService.class);
            context.stopService(serviceIntent);
            return "Quick assistant overlay deactivated.";
        }

        return "Usage: overlay [show | hide]";
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return "Usage: overlay [show | hide]";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: overlay [show | hide]";
    }
}
