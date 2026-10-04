package ohi.andre.consolelauncher.managers;

import android.content.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkspaceManager {

    public enum Workspace {
        MINIMAL("Minimal", Arrays.asList("Clock", "Search", "Apps")),
        DEVELOPER("Developer", Arrays.asList("Clock", "Search", "Terminal", "CPU", "RAM", "Network", "Battery")),
        PRIVACY("Privacy", Arrays.asList("Clock", "Search", "Apps")),
        CYBER("Cyber", Arrays.asList("Clock", "Search", "Terminal", "System", "Media", "Widgets")),
        GAMING("Gaming", Arrays.asList("Clock", "Search", "Performance", "Battery")),
        CUSTOM("Custom", Arrays.asList("Clock", "Search", "Apps", "Widgets"));

        private final String name;
        private final List<String> modules;

        Workspace(String name, List<String> modules) {
            this.name = name;
            this.modules = modules;
        }

        public String getName() {
            return name;
        }

        public List<String> getModules() {
            return modules;
        }
    }

    private Workspace activeWorkspace = Workspace.MINIMAL;
    private final Context context;

    public WorkspaceManager(Context context) {
        this.context = context;
    }

    public Workspace getActiveWorkspace() {
        return activeWorkspace;
    }

    public void setWorkspace(Workspace workspace) {
        if (workspace != null) {
            this.activeWorkspace = workspace;
        }
    }

    public List<String> getAvailableWorkspaces() {
        List<String> list = new ArrayList<>();
        for (Workspace w : Workspace.values()) {
            list.add(w.getName());
        }
        return list;
    }
}
