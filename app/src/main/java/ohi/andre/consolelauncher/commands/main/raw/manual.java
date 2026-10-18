package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

/** In-app quick guide kept aligned with commands implemented by this fork. */
public class manual implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        return "--- CYTHERNOIR LAUNCHER / QUICK GUIDE ---\n\n"
                + "START HERE\n"
                + "  help                       List commands\n"
                + "  doctor                     Check common setup issues\n"
                + "  privacy                    Review optional Android access\n"
                + "  palette [query]            Find apps, commands, aliases, and workspaces\n\n"
                + "PINNED CARDS\n"
                + "  visual battery -fixed      Pin battery status\n"
                + "  visual system -fixed       Pin system information\n"
                + "  visual music -fixed        Pin external media controls\n"
                + "  visual clear               Remove pinned visual cards\n\n"
                + "MUSIC\n"
                + "  The integrated player uses music commands and local audio access.\n"
                + "  The visual card controls other players through Android media sessions.\n"
                + "  Enable Notification access for T-UI in Android settings to connect them.\n"
                + "  Its ASCII animation is decorative, not an audio spectrum.\n\n"
                + "ANDROID WIDGETS\n"
                + "  widget add | list | move [ID] up|down | height [ID] [dp]\n"
                + "  widget remove [ID] | clear\n"
                + "  Some devices require Cythernoir/T-UI as the default Home app.\n\n"
                + "WORKSPACES\n"
                + "  workspace list\n"
                + "  workspace set [Minimal|Developer|Privacy|Cyber|Gaming|Custom]\n\n"
                + "BACKUP\n"
                + "  config -export_profile [name.json]\n"
                + "  config -import_profile [name.json]\n"
                + "  Profiles store launcher settings; keep a copy before importing.\n\n"
                + "THEMES & SETTINGS\n"
                + "  theme -preset [blue|red|green|pink|bw|cyberpunk|watchdogs]\n"
                + "  settings / config -get / config -set [option] [value]\n\n"
                + "Run `help [command]` for command-specific usage. Android permissions and widget support vary by device.";
    }

    @Override public int[] argType() { return new int[0]; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_manual; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return null; }
}
