package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Context;
import java.util.Map;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.CommandManager;

public class command implements CommandAbstraction {

    private static CommandManager instance;

    public static synchronized CommandManager getInstance(Context context) {
        if (instance == null) {
            instance = new CommandManager(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        CommandManager cm = getInstance(info.context);

        if (pack.args == null || pack.args.length == 0) {
            return "Usage: command [create <name> <action> | list | remove <name>]";
        }

        String argText = pack.args[0] == null ? "" : pack.args[0].toString().trim();
        if (argText.isEmpty()) {
            return "Usage: command [create <name> <action> | list | remove <name>]";
        }

        String[] parts = argText.split("\\s+", 3);
        String subCmd = parts[0];

        if (subCmd.equalsIgnoreCase("list")) {
            Map<String, String> all = cm.getAll();
            if (all.isEmpty()) return "No custom commands defined. Use `command create <name> <action>` to add one.";
            StringBuilder sb = new StringBuilder("Custom Commands:\n");
            for (Map.Entry<String, String> entry : all.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(" --> ").append(entry.getValue()).append("\n");
            }
            return sb.toString().trim();
        } else if (subCmd.equalsIgnoreCase("remove") || subCmd.equalsIgnoreCase("rm")) {
            if (parts.length < 2) return "Usage: command remove <name>";
            String name = parts[1];
            boolean removed = cm.remove(name);
            return removed ? "Custom command '" + name + "' removed." : "Custom command '" + name + "' not found.";
        } else if (subCmd.equalsIgnoreCase("create") || subCmd.equalsIgnoreCase("add")) {
            if (parts.length < 3) return "Usage: command create <name> <action>";
            String name = parts[1];
            String action = parts[2];
            boolean created = cm.create(name, action);
            return created ? "Custom command '" + name + "' created successfully: " + action : "Failed to create custom command.";
        }

        return "Usage: command [create <name> <action> | list | remove <name>]";
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
        return "Usage: command [create <name> <action> | list | remove <name>]";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: command [create <name> <action> | list | remove <name>]";
    }
}
