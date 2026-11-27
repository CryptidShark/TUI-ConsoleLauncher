package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.specific.PermanentSuggestionCommand;
import ohi.andre.consolelauncher.managers.WorkspaceManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.R;

/** Switches the pinned cards to a named work layout. */
public class workspace implements PermanentSuggestionCommand {
    @Override
    public String exec(ExecutePack pack) {
        String input = pack.args != null && pack.args.length > 0 ? String.valueOf(pack.args[0]).trim() : "";
        String[] parts = input.isEmpty() ? new String[]{"list"} : input.split("\\s+", 2);
        String action = parts[0];
        if (action.equalsIgnoreCase("list")) {
            return "Workspaces: " + Tuils.toPlanString(WorkspaceManager.getAvailableWorkspaces(), ", ")
                    + "\nActive: " + WorkspaceManager.getActiveWorkspace(pack.context).getName()
                    + "\nUsage: workspace set [minimal|developer|privacy|cyber|gaming|web_dev|authorized_lab|custom]";
        }
        if (action.equalsIgnoreCase("set") && parts.length > 1) {
            WorkspaceManager.Workspace selected = WorkspaceManager.fromName(parts[1].trim());
            if (selected == null) return "Unknown workspace. Try: workspace list";
            WorkspaceManager.setWorkspace(pack.context, selected);
            return "Workspace set to " + selected.getName() + ".";
        }
        return "Usage: workspace [list | set [minimal|developer|privacy|cyber|gaming|web_dev|authorized_lab|custom]]";
    }

    @Override public int[] argType() { return new int[]{PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return R.string.help_workspace; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return "Unknown workspace. Try: workspace list"; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String[] permanentSuggestions() {
        return new String[]{"list", "set minimal", "set developer", "set privacy", "set cyber", "set gaming", "set web_dev", "set authorized_lab", "set custom"};
    }
}
