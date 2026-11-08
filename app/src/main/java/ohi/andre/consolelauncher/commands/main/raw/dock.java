package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.EdgeDockManager;
import ohi.andre.consolelauncher.tuils.Tuils;

/** Configures the optional thumb-operated edge dock. */
public class dock implements CommandAbstraction {
    @Override public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        String input = pack.args == null || pack.args.length == 0 ? "help" : String.valueOf(pack.args[0]).trim();
        String[] parts = input.split("\\s+", 2);
        String action = parts[0].toLowerCase();
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
        if (action.equals("list")) return "Edge dock: " + Tuils.toPlanString(EdgeDockManager.getItems(info.context), ", ")
                + "\nState: " + (EdgeDockManager.isEnabled(info.context) ? "enabled" : "disabled")
                + "\nUsage: dock add <alias> | dock enable | dock disable | dock remove <alias>";
        return "Usage: dock [list | add <alias> | remove <alias> | enable | disable]";
    }

    private static void refresh(MainPack info) {
        if (info.context instanceof LauncherActivity) ((LauncherActivity) info.context).refreshEdgeDock();
    }
    @Override public int[] argType() { return new int[]{PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return 0; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
}
