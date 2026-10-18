package ohi.andre.consolelauncher.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;

/** Preset sets of pinned cards for common launcher workflows. */
public class WorkspaceManager {

    private static final String PREFS = "launcher_workspaces";
    private static final String ACTIVE_KEY = "active_workspace";

    public enum Workspace {
        MINIMAL("Minimal", new Behavior[0]),
        DEVELOPER("Developer", new Behavior[]{Behavior.persistent_system_card, Behavior.persistent_battery_card, Behavior.persistent_shortcuts_card}),
        PRIVACY("Privacy", new Behavior[]{Behavior.persistent_system_card, Behavior.persistent_notes_card}),
        CYBER("Cyber", new Behavior[]{Behavior.persistent_system_card, Behavior.persistent_music_card, Behavior.persistent_shortcuts_card}),
        GAMING("Gaming", new Behavior[]{Behavior.persistent_battery_card, Behavior.persistent_music_card}),
        CUSTOM("Custom", null);

        private final String label;
        private final Behavior[] cards;

        Workspace(String label, Behavior[] cards) {
            this.label = label;
            this.cards = cards;
        }

        public String getName() { return label; }
    }

    private WorkspaceManager() {}

    public static Workspace getActiveWorkspace(Context context) {
        String name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(ACTIVE_KEY, Workspace.CUSTOM.name());
        try { return Workspace.valueOf(name); }
        catch (Exception ignored) { return Workspace.CUSTOM; }
    }

    public static boolean setWorkspace(Context context, Workspace workspace) {
        return setWorkspace(context, workspace, true);
    }

    public static boolean setWorkspace(Context context, Workspace workspace, boolean applyCards) {
        if (context == null || workspace == null) return false;
        if (applyCards && workspace.cards != null) {
            for (Behavior card : cardOptions()) {
                boolean enabled = Arrays.asList(workspace.cards).contains(card);
                card.parent().write(card, Boolean.toString(enabled));
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(ACTIVE_KEY, workspace.name()).apply();
        if (applyCards && context instanceof LauncherActivity) {
            ((LauncherActivity) context).refreshPersistentContainer();
        }
        return true;
    }

    public static Workspace fromName(String name) {
        if (name == null) return null;
        for (Workspace workspace : Workspace.values()) {
            if (workspace.name().equalsIgnoreCase(name) || workspace.label.equalsIgnoreCase(name)) return workspace;
        }
        return null;
    }

    public static List<String> getAvailableWorkspaces() {
        List<String> names = new ArrayList<>();
        for (Workspace workspace : Workspace.values()) names.add(workspace.label);
        return names;
    }

    private static List<Behavior> cardOptions() {
        return Arrays.asList(Behavior.persistent_system_card, Behavior.persistent_battery_card,
                Behavior.persistent_notes_card, Behavior.persistent_shortcuts_card,
                Behavior.persistent_music_card, Behavior.persistent_weather_card,
                Behavior.persistent_termux_card);
    }
}
