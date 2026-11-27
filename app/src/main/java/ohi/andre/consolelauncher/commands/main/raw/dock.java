package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.PermanentSuggestionCommand;
import ohi.andre.consolelauncher.managers.EdgeDockManager;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Configures the optional thumb-operated edge dock. */
public class dock implements PermanentSuggestionCommand {
    @Override public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = pack.args == null || pack.args.length == 0 ? "help" : String.valueOf(pack.args[0]).trim();
        String[] parts = input.split("\\s+", 2);
        String action = parts[0].toLowerCase(java.util.Locale.ROOT);
        String alias = parts.length > 1 ? parts[1].trim() : "";
        if (action.equals("enable") || action.equals("on")) {
            EdgeDockManager.setEnabled(info.context, true);
            refresh(info);
            return "Edge dock enabled. Swipe along the right edge to choose an alias.";
        }
        if (action.equals("disable") || action.equals("off")) {
            EdgeDockManager.setEnabled(info.context, false);
            refresh(info);
            return "Edge dock disabled.";
        }
        if (action.equals("add")) {
            if (alias.isEmpty()) return "Usage: dock add <alias> (create it first with `alias add`).";
            if (info.aliasManager.getAlias(alias, false)[0] == null) return "Alias not found. Create it first with: alias add " + alias + " <command>";
            if (!EdgeDockManager.add(info.context, alias)) return "Dock is full (maximum 8 aliases) or alias is invalid.";
            refresh(info);
            return "Added [" + alias + "] to the edge dock.";
        }
        if (action.equals("remove") || action.equals("rm")) {
            if (!EdgeDockManager.remove(info.context, alias)) return "Alias is not in the edge dock.";
            refresh(info);
            return "Removed [" + alias + "] from the edge dock.";
        }
        if (action.equals("icon")) {
            String[] iconArgs = alias.split("\\s+", 2);
            if (iconArgs.length < 2) return "Usage: dock icon <alias> <emoji|reset>";
            String targetAlias = iconArgs[0];
            String icon = iconArgs[1].trim();
            if (!EdgeDockManager.getItems(info.context).contains(targetAlias)) return "Add the alias to the dock first: dock add " + targetAlias;
            if (icon.equalsIgnoreCase("reset") || icon.equalsIgnoreCase("none")) icon = "";
            if (!EdgeDockManager.setIcon(info.context, targetAlias, icon)) return "Icon is invalid. Use a short symbol or emoji (up to 8 characters).";
            refresh(info);
            return icon.isEmpty() ? "Reset icon for [" + targetAlias + "]." : "Set icon " + icon + " for [" + targetAlias + "].";
        }
        if (action.equals("list")) {
            java.util.List<String> items = EdgeDockManager.getItems(info.context);
            java.util.List<String> shown = new java.util.ArrayList<>();
            for (String item : items) {
                String icon = EdgeDockManager.getIcon(info.context, item);
                shown.add((icon.isEmpty() ? "[" + item + "]" : icon + " [" + item + "]"));
            }
            return "Edge dock: " + Tuils.toPlanString(shown, ", ")
                + "\nState: " + (EdgeDockManager.isEnabled(info.context) ? "enabled" : "disabled")
                + "\nUsage: dock add <alias> | dock icon <alias> <emoji|reset> | dock enable | dock disable | dock remove <alias>";
        }
        return "Edge dock commands:\n"
            + "  dock add <alias>       Add an existing alias to the dock\n"
            + "  dock icon <alias> <emoji>  Set its icon, e.g. dock icon w 📱\n"
            + "  dock icon <alias> reset   Restore the [alias] label\n"
            + "  dock remove <alias>    Remove an alias\n"
            + "  dock list              Show dock items and state\n"
            + "  dock enable | disable  Turn the edge dock on or off\n"
            + "First create an alias: alias add w WhatsApp. Then run: dock add w\n"
            + "Type `dock ` (dock + space) to see command suggestions.";
    }

    private static void refresh(MainPack info) {
        if (info.context instanceof LauncherActivity) ((LauncherActivity) info.context).refreshEdgeDock();
    }
    @Override public int[] argType() { return new int[]{PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return 0; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }

    @Override public String[] permanentSuggestions() {
        return new String[]{"list", "add ", "icon ", "remove ", "enable", "disable"};
    }
}
