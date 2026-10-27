package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Context;
import java.util.Map;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.KeybindManager;

public class keybind implements CommandAbstraction {

    private static KeybindManager instance;

    public static synchronized KeybindManager getInstance(Context context) {
        if (instance == null) {
            instance = new KeybindManager(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        KeybindManager km = getInstance(info.context);

        if (pack.args == null || pack.args.length == 0) {
            return "Usage: keybind [list | <F1-F10> <command> | remove <key>]";
        }

        String argText = pack.args[0] == null ? "" : pack.args[0].toString().trim();
        if (argText.isEmpty()) {
            return "Usage: keybind [list | <F1-F10> <command> | remove <key>]";
        }

        String[] parts = argText.split("\\s+", 2);
        String subCmd = parts[0];

        if (subCmd.equalsIgnoreCase("list")) {
            Map<String, String> all = km.getAll();
            if (all.isEmpty()) return "No keybinds defined. Usage: keybind F1 <command>";
            StringBuilder sb = new StringBuilder("Virtual Keybinds:\n");
            for (Map.Entry<String, String> entry : all.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(" --> ").append(entry.getValue()).append("\n");
            }
            return sb.toString().trim();
        } else if (subCmd.equalsIgnoreCase("remove") || subCmd.equalsIgnoreCase("rm")) {
            if (parts.length < 2) return "Usage: keybind remove <F1-F10>";
            boolean removed = km.remove(parts[1]);
            return removed ? "Keybind " + parts[1].toUpperCase() + " removed." : "Keybind not found.";
        } else if (subCmd.toUpperCase().startsWith("F") && parts.length > 1) {
            String key = subCmd.toUpperCase();
            String cmd = parts[1];
            km.set(key, cmd);
            return "Keybind " + key + " set to: " + cmd;
        }

        return "Usage: keybind [list | <F1-F10> <command> | remove <key>]";
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
        return "Usage: keybind [list | <F1-F10> <command> | remove <key>]";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: keybind [list | <F1-F10> <command> | remove <key>]";
    }
}
