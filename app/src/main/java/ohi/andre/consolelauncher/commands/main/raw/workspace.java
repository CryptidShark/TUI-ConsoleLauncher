package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.managers.WorkspaceManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.R;

/** Switches the pinned cards to a named work layout. */
public class workspace implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) {
        String action = pack.args != null && pack.args.length > 0 ? String.valueOf(pack.args[0]) : "list";
        if (action.equalsIgnoreCase("list")) {
            return "Workspaces: " + Tuils.toPlanString(WorkspaceManager.getAvailableWorkspaces(), ", ")
                    + "\nActive: " + WorkspaceManager.getActiveWorkspace(pack.context).getName()
                    + "\nUsage: workspace set [name]";
        }
        if (action.equalsIgnoreCase("set") && pack.args.length > 1) {
            WorkspaceManager.Workspace selected = WorkspaceManager.fromName(String.valueOf(pack.args[1]));
            if (selected == null) return "Unknown workspace. Try: workspace list";
            WorkspaceManager.setWorkspace(pack.context, selected);
            return "Workspace set to " + selected.getName() + ".";
        }
        return "Usage: workspace [list | set [Minimal|Developer|Privacy|Cyber|Gaming|Custom]]";
    }

    @Override public int[] argType() { return new int[]{PLAIN_TEXT, PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_workspace; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return "Unknown workspace. Try: workspace list"; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return null; }
}
