package ohi.andre.consolelauncher.commands.main.raw;

import java.util.List;
import java.util.Locale;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.specific.PermanentSuggestionCommand;
import ohi.andre.consolelauncher.managers.CommandHistory;

/** Lists, searches and recalls recent CLI commands. */
public class history implements PermanentSuggestionCommand {
    @Override
    public String exec(ExecutePack pack) {
        String input = pack.args == null || pack.args.length == 0 || pack.args[0] == null
                ? "list" : pack.args[0].toString().trim();
        String[] parts = input.split("\\s+", 2);
        String action = parts[0].toLowerCase(Locale.ROOT);
        List<String> items = CommandHistory.get(pack.context);

        if (action.equals("clear")) {
            CommandHistory.clear(pack.context);
            return "Saved command history cleared.";
        }
        if (action.equals("run") || action.equals("recall")) {
            if (parts.length < 2) return "Usage: history run <number from history list>";
            try {
                int index = Integer.parseInt(parts[1].trim()) - 1;
                if (index < 0 || index >= items.size()) return "History number out of range. Use `history list`.";
                if (pack.context instanceof LauncherActivity) {
                    ohi.andre.consolelauncher.UIManager ui = ((LauncherActivity) pack.context).getUIManager();
                    if (ui == null) return "Terminal is not ready to recall a command.";
                    String selectedCommand = items.get(index);
                    ((LauncherActivity) pack.context).runOnUiThread(() -> ui.setInput(selectedCommand));
                    return "Command recalled to the prompt. Press Enter to run it.";
                }
                return items.get(index);
            } catch (NumberFormatException e) {
                return "Usage: history run <number from history list>";
            }
        }

        if (action.equals("find") || action.equals("search")) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) return "Usage: history find <text>";
            String query = parts[1].trim().toLowerCase(Locale.ROOT);
            StringBuilder result = new StringBuilder("Matching command history:");
            int matches = 0;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).toLowerCase(Locale.ROOT).contains(query)) {
                    result.append("\n").append(i + 1).append("  ").append(items.get(i));
                    matches++;
                }
            }
            if (matches == 0) return "No saved commands match: " + parts[1].trim();
            return result.toString();
        }

        if (!action.equals("list") && !action.equals("ls")) return "Usage: history [list | find <text> | run <number> | clear]";
        if (items.isEmpty()) return "Command history is empty. Incognito mode disables saved history.";
        StringBuilder result = new StringBuilder("Recent commands (newest first):");
        for (int i = 0; i < Math.min(items.size(), 30); i++) {
            result.append("\n").append(i + 1).append("  ").append(items.get(i));
        }
        result.append("\nUse `history run <number>` to place a command in the prompt; press Enter to execute.");
        return result.toString();
    }

    @Override public int[] argType() { return new int[]{CommandAbstraction.PLAIN_TEXT}; }
    @Override public int priority() { return 5; }
    @Override public int helpRes() { return 0; }
    @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    @Override public String onNotArgEnough(ExecutePack pack, int nArgs) { return exec(pack); }
    @Override public String[] permanentSuggestions() { return new String[]{"list", "find ", "run ", "clear"}; }
}
