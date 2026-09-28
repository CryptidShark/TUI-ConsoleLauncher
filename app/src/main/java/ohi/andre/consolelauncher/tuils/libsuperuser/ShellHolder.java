package ohi.andre.consolelauncher.tuils.libsuperuser;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.BusyBoxInstaller;
import ohi.andre.consolelauncher.tuils.Tuils;

public class ShellHolder {

    private Context context;

    public ShellHolder(Context context) {
        this.context = context;
    }

    Pattern p = Pattern.compile("^\\n");

    public Shell.Interactive build() {
        Shell.Interactive interactive = new Shell.Builder()
                .setOnSTDOUTLineListener(line -> {
                    line = p.matcher(line).replaceAll(Tuils.EMPTYSTRING);
                    Tuils.sendOutput(context, line, TerminalManager.CATEGORY_OUTPUT);
                })
                .setOnSTDERRLineListener(line -> {
                    line = p.matcher(line).replaceAll(Tuils.EMPTYSTRING);
                    Tuils.sendOutput(context, line, TerminalManager.CATEGORY_OUTPUT);
                })
                .open();
        
        setupBusyBox(interactive);
        interactive.addCommand("cd " + XMLPrefsManager.get(File.class, Behavior.home_path));
        return interactive;
    }

    private void setupBusyBox(Shell.Interactive interactive) {
        if (BusyBoxInstaller.isInstalled(context)) {
            String bbPath = BusyBoxInstaller.getBusyboxPath(context);
            if (bbPath != null) {
                // On modern Android (10+), executing from filesDir is blocked by SELinux.
                // We add the alias but we don't force standard commands to use it by default
                // unless the user explicitly uses 'busybox' command.
                interactive.addCommand("alias busybox='" + bbPath + "'");

                // We only alias commands that are likely MISSING from native Android toybox/toolbox
                String[] extraApplets = {
                    "vi", "vim", "less", "more", "wget", "curl", "nc", "telnet", 
                    "ftpget", "ftpput", "ssh", "scp"
                };

                for (String applet : extraApplets) {
                    interactive.addCommand("alias " + applet + "='busybox " + applet + "'");
                }
            }
        }
    }
}
