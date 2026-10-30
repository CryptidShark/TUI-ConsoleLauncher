package ohi.andre.consolelauncher.commands.main.raw;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.tuils.TermuxBridge;

public class termux implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        Context context = info.context;

        if (!TermuxBridge.isTermuxInstalled(context)) {
            return "Termux is not installed.\nPlease install Termux from F-Droid or GitHub (avoid Google Play Store version as it is deprecated), and configure ~/.termux/termux.properties with 'allow-external-apps=true'.";
        }

        if (pack.args == null || pack.args.length == 0) {
            return "Usage: termux [run <script> | tmux | status | help] or termux <command>";
        }

        String argText = pack.args[0] == null ? "" : pack.args[0].toString().trim();
        if (argText.isEmpty() || argText.equalsIgnoreCase("help")) {
            return "Termux Integration Help:\n" +
                   "• termux run <script> - Execute a Termux script\n" +
                   "• termux <command>    - Execute any command (e.g. termux ping 8.8.8.8)\n" +
                   "• termux tmux         - Connect to tmux session\n" +
                   "• termux status       - Check installation and RUN_COMMAND permission";
        }

        String[] parts = argText.split("\\s+", 2);
        String subCmd = parts[0];
        String param = parts.length > 1 ? parts[1] : "";

        if (subCmd.equalsIgnoreCase("status")) {
            return TermuxBridge.getTermuxStatus(context);
        } else if (subCmd.equalsIgnoreCase("tmux")) {
            try {
                Intent intent = new Intent();
                intent.setAction("com.termux.RUN_COMMAND");
                intent.putExtra("com.termux.intent.extra.EXECUTABLE", "/data/data/com.termux/files/usr/bin/tmux");
                intent.putExtra("com.termux.intent.extra.ARGUMENTS", new String[]{"attach"});
                intent.putExtra("com.termux.intent.extra.WORKDIR", "/data/data/com.termux/files/home");

                boolean executed = false;
                try {
                    intent.setComponent(new ComponentName("com.termux", "com.termux.app.RunCommandService"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                    executed = true;
                } catch (Exception exService) {
                    try {
                        intent.setComponent(new ComponentName("com.termux", "com.termux.app.RunCommandBroadcastReceiver"));
                        context.sendBroadcast(intent);
                        executed = true;
                    } catch (Exception exBroadcast) {
                        TermuxBridge.launchTermux(context);
                    }
                }
                return executed ? "Connecting to tmux session in Termux..." : "Launched Termux app.";
            } catch (Exception e) {
                TermuxBridge.launchTermux(context);
                return "Launched Termux (tmux start failed: " + e.getMessage() + ")";
            }
        } else {
            String commandToRun = subCmd.equalsIgnoreCase("run") ? param : argText;
            if (commandToRun.isEmpty()) {
                return "Usage: termux run <script> or termux <command>";
            }

            try {
                Intent intent = new Intent();
                intent.setAction("com.termux.RUN_COMMAND");
                intent.putExtra("com.termux.intent.extra.EXECUTABLE", "/data/data/com.termux/files/usr/bin/bash");
                intent.putExtra("com.termux.intent.extra.ARGUMENTS", new String[]{"-c", commandToRun});
                intent.putExtra("com.termux.intent.extra.WORKDIR", "/data/data/com.termux/files/home");

                boolean executed = false;
                try {
                    intent.setComponent(new ComponentName("com.termux", "com.termux.app.RunCommandService"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                    executed = true;
                } catch (Exception exService) {
                    try {
                        intent.setComponent(new ComponentName("com.termux", "com.termux.app.RunCommandBroadcastReceiver"));
                        context.sendBroadcast(intent);
                        executed = true;
                    } catch (Exception exBroadcast) {
                        TermuxBridge.launchTermux(context);
                    }
                }
                return executed ? "Executing in Termux: " + commandToRun : "Launched Termux app.";
            } catch (Exception e) {
                return "Failed to execute Termux command: " + e.getMessage() + "\nEnsure permission 'com.termux.permission.RUN_COMMAND' is granted in Android settings and 'allow-external-apps=true' is set in ~/.termux/termux.properties.";
            }
        }
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
        return "Usage: termux [run <script> | tmux | status]";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: termux [run <script> | tmux | status]";
    }
}
