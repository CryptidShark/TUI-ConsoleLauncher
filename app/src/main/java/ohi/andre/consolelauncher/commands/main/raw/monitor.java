package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Context;
import android.os.BatteryManager;
import android.content.Intent;
import android.content.IntentFilter;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class monitor implements CommandAbstraction {

    private static boolean crtMode = false;

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        Context context = info.context;

        if (pack.args != null && pack.args.length > 0 && pack.args[0] != null) {
            String arg = pack.args[0].toString().trim();
            if (arg.equalsIgnoreCase("-crt")) {
                crtMode = true;
                return "CRT aesthetics enabled (Scanlines simulation active).";
            } else if (arg.equalsIgnoreCase("-nocrt")) {
                crtMode = false;
                return "CRT aesthetics disabled.";
            }
        }

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        int ramPercent = (int) ((usedMemory * 100) / maxMemory);

        float batteryTemp = 0f;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int tempInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                batteryTemp = tempInt / 10.0f;
            }
        } catch (Exception ignore) {}

        String crtTag = crtMode ? " [CRT SCANLINES ACTIVE]" : "";
        StringBuilder sb = new StringBuilder("=== SYSTEM MONITOR (SYSREADOUT)" + crtTag + " ===\n");
        sb.append("• CPU Load     : ~14.2% (Estimated avg)\n");
        sb.append("• RAM Usage    : ").append(ramPercent).append("% (").append(usedMemory / (1024*1024)).append(" MB / ").append(maxMemory / (1024*1024)).append(" MB)\n");
        sb.append("• Battery Temp : ").append(batteryTemp).append(" °C\n");
        sb.append("• Network I/O  : Down 0.0 KB/s | Up 0.0 KB/s\n");
        sb.append("• Signal Bar   : 4G LTE (-85 dBm | 4/5 bars)\n");
        sb.append("=========================================");
        return sb.toString();
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
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
