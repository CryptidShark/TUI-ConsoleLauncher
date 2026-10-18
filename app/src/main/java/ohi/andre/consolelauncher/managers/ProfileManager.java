package ohi.andre.consolelauncher.managers;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.managers.xml.options.Apps;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Cmd;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;
import ohi.andre.consolelauncher.managers.xml.options.Suggestions;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class ProfileManager {

    private static final int PROFILE_SCHEMA_VERSION = 1;
    private static final int MAX_PROFILE_BYTES = 2 * 1024 * 1024;

    public static String exportProfile(Context context, String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            profileName = "default_profile.json";
        }
        // Keep exports inside the app's configuration directory even if the
        // caller supplies a path or traversal segments.
        profileName = new File(profileName.trim()).getName();
        if (profileName.isEmpty() || profileName.equals(".") || profileName.equals("..")) {
            return "Invalid profile name.";
        }
        if (!profileName.toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            profileName += ".json";
        }

        try {
            JSONObject root = new JSONObject();
            root.put("version", PROFILE_SCHEMA_VERSION);
            root.put("exportedAt", System.currentTimeMillis());

            JSONObject behaviorObj = new JSONObject();
            for (Behavior b : Behavior.values()) {
                behaviorObj.put(b.label(), XMLPrefsManager.get(b));
            }
            root.put("behavior", behaviorObj);

            JSONObject uiObj = new JSONObject();
            for (Ui u : Ui.values()) {
                uiObj.put(u.label(), XMLPrefsManager.get(u));
            }
            root.put("ui", uiObj);

            JSONObject themeObj = new JSONObject();
            for (Theme t : Theme.values()) {
                themeObj.put(t.label(), XMLPrefsManager.get(t));
            }
            root.put("theme", themeObj);

            JSONObject cmdObj = new JSONObject();
            for (Cmd c : Cmd.values()) {
                cmdObj.put(c.label(), XMLPrefsManager.get(c));
            }
            root.put("cmd", cmdObj);
            root.put("workspace", WorkspaceManager.getActiveWorkspace(context).name());

            File folder = Tuils.getFolder();
            if (folder == null && context != null) folder = context.getFilesDir();
            if (folder == null || (!folder.exists() && !folder.mkdirs())) return "Unable to create the profile folder.";
            File dest = new File(folder, profileName);
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }

            return "Profile exported successfully to: " + dest.getAbsolutePath();
        } catch (Exception e) {
            return "Error exporting profile: " + e.getMessage();
        }
    }

    public static String importProfile(Context context, String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            return "Usage: config -import_profile [filename.json]";
        }

        File profileFolder = Tuils.getFolder();
        File file = profileFolder == null
                ? new File(context.getFilesDir(), profileName)
                : new File(profileFolder, profileName);
        if (!file.exists()) {
            file = new File(profileName);
        }
        if (!file.exists()) {
            return "Profile file not found: " + profileName;
        }
        if (!file.isFile() || file.length() <= 0 || file.length() > MAX_PROFILE_BYTES) {
            return "Profile must be a non-empty JSON file smaller than 2 MB.";
        }

        try {
            byte[] data;
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                    if (output.size() > MAX_PROFILE_BYTES) return "Profile exceeds the 2 MB import limit.";
                }
                data = output.toByteArray();
            }

            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(jsonStr);

            int version = root.optInt("version", -1);
            if (version != PROFILE_SCHEMA_VERSION) {
                return "Invalid profile schema version: " + version + " (Expected: " + PROFILE_SCHEMA_VERSION + ")";
            }

            if (root.has("behavior")) {
                JSONObject behaviorObj = root.getJSONObject("behavior");
                for (Behavior b : Behavior.values()) {
                    if (behaviorObj.has(b.label())) {
                        b.parent().write(b, behaviorObj.getString(b.label()));
                    }
                }
            }

            if (root.has("ui")) {
                JSONObject uiObj = root.getJSONObject("ui");
                for (Ui u : Ui.values()) {
                    if (uiObj.has(u.label())) {
                        u.parent().write(u, uiObj.getString(u.label()));
                    }
                }
            }

            if (root.has("theme")) {
                JSONObject themeObj = root.getJSONObject("theme");
                for (Theme t : Theme.values()) {
                    if (themeObj.has(t.label())) {
                        t.parent().write(t, themeObj.getString(t.label()));
                    }
                }
            }

            if (root.has("cmd")) {
                JSONObject cmdObj = root.getJSONObject("cmd");
                for (Cmd c : Cmd.values()) {
                    if (cmdObj.has(c.label())) {
                        c.parent().write(c, cmdObj.getString(c.label()));
                    }
                }
            }

            if (root.has("workspace")) {
                WorkspaceManager.Workspace workspace = WorkspaceManager.fromName(root.optString("workspace"));
                if (workspace != null) WorkspaceManager.setWorkspace(context, workspace, false);
            }

            if (context instanceof Reloadable) {
                ((Reloadable) context).reload();
            }

            return "Profile successfully imported from: " + file.getName();
        } catch (Exception e) {
            return "Error importing profile (malformed file): " + e.getMessage();
        }
    }
}
