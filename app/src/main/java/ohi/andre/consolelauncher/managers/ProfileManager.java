package ohi.andre.consolelauncher.managers;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.EdgeDockManager;
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

            JSONObject notificationsObj = new JSONObject();
            for (Notifications notification : Notifications.values()) {
                notificationsObj.put(notification.label(), XMLPrefsManager.get(notification));
            }
            root.put("notifications", notificationsObj);

            JSONObject suggestionsObj = new JSONObject();
            for (Suggestions suggestion : Suggestions.values()) {
                suggestionsObj.put(suggestion.label(), XMLPrefsManager.get(suggestion));
            }
            root.put("suggestions", suggestionsObj);

            JSONObject appsObj = new JSONObject();
            for (Apps app : Apps.values()) {
                appsObj.put(app.label(), XMLPrefsManager.get(app));
            }
            root.put("apps", appsObj);
            root.put("workspace", WorkspaceManager.getActiveWorkspace(context).name());
            root.put("dock", EdgeDockManager.exportSettings(context));

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

            String validationError = validateSettings(root, "behavior", Behavior.values())
                    + validateSettings(root, "ui", Ui.values())
                    + validateSettings(root, "theme", Theme.values())
                    + validateSettings(root, "cmd", Cmd.values())
                    + validateSettings(root, "notifications", Notifications.values())
                    + validateSettings(root, "suggestions", Suggestions.values())
                    + validateSettings(root, "apps", Apps.values());
            if (!validationError.isEmpty()) return validationError;
            if (root.has("workspace") && WorkspaceManager.fromName(root.optString("workspace")) == null) {
                return "Unknown workspace in profile: " + root.optString("workspace");
            }
            String dockError = validateDockSettings(root.optJSONObject("dock"));
            if (root.has("dock") && root.optJSONObject("dock") == null) return "Invalid dock settings section in profile.";
            if (!dockError.isEmpty()) return dockError;

            String backupName = "profile_before_import_" + System.currentTimeMillis() + ".json";
            String backupResult = exportProfile(context, backupName);
            if (!backupResult.startsWith("Profile exported successfully")) {
                return "Import cancelled because the safety backup could not be created. " + backupResult;
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

            if (root.has("notifications")) {
                JSONObject notificationsObj = root.getJSONObject("notifications");
                for (Notifications notification : Notifications.values()) {
                    if (notificationsObj.has(notification.label())) {
                        notification.parent().write(notification, notificationsObj.getString(notification.label()));
                    }
                }
            }

            if (root.has("suggestions")) {
                JSONObject suggestionsObj = root.getJSONObject("suggestions");
                for (Suggestions suggestion : Suggestions.values()) {
                    if (suggestionsObj.has(suggestion.label())) {
                        suggestion.parent().write(suggestion, suggestionsObj.getString(suggestion.label()));
                    }
                }
            }

            if (root.has("apps")) {
                JSONObject appsObj = root.getJSONObject("apps");
                for (Apps app : Apps.values()) {
                    if (appsObj.has(app.label())) {
                        app.parent().write(app, appsObj.getString(app.label()));
                    }
                }
            }

            if (root.has("workspace")) {
                WorkspaceManager.Workspace workspace = WorkspaceManager.fromName(root.optString("workspace"));
                if (workspace != null) WorkspaceManager.setWorkspace(context, workspace, false);
            }
            if (root.has("dock") && !EdgeDockManager.restoreSettings(context, root.getJSONObject("dock"))) {
                return "Dock settings could not be restored from the profile.";
            }

            if (context instanceof Reloadable) {
                ((Reloadable) context).reload();
            }

            return "Profile successfully imported from: " + file.getName()
                    + "\nPrevious settings backed up as " + backupName + ".";
        } catch (Exception e) {
            return "Error importing profile (malformed file): " + e.getMessage();
        }
    }

    private static String validateSettings(JSONObject root, String sectionName, XMLPrefsSave[] settings) {
        if (!root.has(sectionName)) return "";
        JSONObject section = root.optJSONObject(sectionName);
        if (section == null) return "Invalid profile: '" + sectionName + "' must be an object.";
        for (XMLPrefsSave setting : settings) {
            if (!section.has(setting.label())) continue;
            Object raw = section.opt(setting.label());
            if (!(raw instanceof String)) return "Invalid value for setting '" + setting.label() + "'.";
            String value = (String) raw;
            String type = setting.type();
            try {
                if (XMLPrefsSave.INTEGER.equals(type)) Integer.parseInt(value);
                else if (XMLPrefsSave.BOOLEAN.equals(type)
                        && !("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                    return "Invalid boolean value for setting '" + setting.label() + "'.";
                } else if (XMLPrefsSave.COLOR.equals(type)) android.graphics.Color.parseColor(value);
            } catch (Exception invalidType) {
                return "Invalid " + type + " value for setting '" + setting.label() + "'.";
            }
            String[] invalid = setting.invalidValues();
            if (invalid != null) {
                for (String blocked : invalid) {
                    if (blocked != null && blocked.equals(value)) {
                        return "Unsupported value for setting '" + setting.label() + "'.";
                    }
                }
            }
        }
        return "";
    }

    private static String validateDockSettings(JSONObject dock) {
        if (dock == null) return "";
        String side = dock.optString("side", "right");
        String size = dock.optString("size", "normal");
        org.json.JSONArray items = dock.optJSONArray("items");
        JSONObject icons = dock.optJSONObject("icons");
        if (!(side.equals("left") || side.equals("right"))) return "Invalid edge dock side in profile.";
        if (!(size.equals("compact") || size.equals("normal") || size.equals("large"))) return "Invalid edge dock size in profile.";
        if (items == null || items.length() > 8 || icons == null) return "Invalid edge dock aliases or icons in profile.";
        java.util.Set<String> aliases = new java.util.HashSet<>();
        for (int i = 0; i < items.length(); i++) {
            if (!(items.opt(i) instanceof String)) return "Invalid edge dock alias in profile.";
            String alias = items.optString(i, "").trim();
            if (alias.isEmpty() || alias.contains("\n") || alias.contains("\r") || !aliases.add(alias)) return "Invalid or duplicate edge dock alias in profile.";
        }
        java.util.Iterator<String> keys = icons.keys();
        while (keys.hasNext()) {
            String alias = keys.next();
            if (!(icons.opt(alias) instanceof String)) return "Invalid edge dock icon in profile.";
            String icon = icons.optString(alias, "");
            if (!aliases.contains(alias) || icon.contains("\n") || icon.contains("\r")
                    || icon.codePointCount(0, icon.length()) > 8) return "Invalid edge dock icon in profile.";
        }
        if (dock.has("enabled") && !(dock.opt("enabled") instanceof Boolean)) return "Invalid edge dock enabled state in profile.";
        return "";
    }

    public static String previewProfile(Context context, String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            return "Usage: config -preview_profile [filename.json]";
        }

        File profileFolder = Tuils.getFolder();
        File file = profileFolder == null
                ? new File(context.getFilesDir(), profileName)
                : new File(profileFolder, profileName);
        if (!file.exists()) file = new File(profileName);
        if (!file.exists()) return "Profile file not found: " + profileName;
        if (!file.isFile() || file.length() <= 0 || file.length() > MAX_PROFILE_BYTES) {
            return "Profile must be a non-empty JSON file smaller than 2 MB.";
        }

        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                if (output.size() > MAX_PROFILE_BYTES) return "Profile exceeds the 2 MB import limit.";
            }

            JSONObject root = new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
            int version = root.optInt("version", -1);
            if (version != PROFILE_SCHEMA_VERSION) {
                return "Invalid profile schema version: " + version + " (Expected: " + PROFILE_SCHEMA_VERSION + ")";
            }

            String validationError = validateSettings(root, "behavior", Behavior.values())
                    + validateSettings(root, "ui", Ui.values())
                    + validateSettings(root, "theme", Theme.values())
                    + validateSettings(root, "cmd", Cmd.values())
                    + validateSettings(root, "notifications", Notifications.values())
                    + validateSettings(root, "suggestions", Suggestions.values())
                    + validateSettings(root, "apps", Apps.values());
            if (!validationError.isEmpty()) return "Profile validation failed: " + validationError;
            if (root.has("workspace") && WorkspaceManager.fromName(root.optString("workspace")) == null) {
                return "Profile validation failed: unknown workspace '" + root.optString("workspace") + "'.";
            }
            if (root.has("dock") && root.optJSONObject("dock") == null) return "Profile validation failed: invalid dock section.";
            String dockError = validateDockSettings(root.optJSONObject("dock"));
            if (!dockError.isEmpty()) return "Profile validation failed: " + dockError;

            StringBuilder summary = new StringBuilder("PROFILE PREVIEW: ").append(file.getName());
            String[] sections = {"behavior", "ui", "theme", "cmd", "notifications", "suggestions", "apps", "dock"};
            for (String section : sections) {
                JSONObject values = root.optJSONObject(section);
                if (values != null) summary.append("\n• ").append(section).append(": ").append(values.length()).append(" settings");
            }
            JSONObject behavior = root.optJSONObject("behavior");
            if (behavior != null && behavior.has("theme_preset")) {
                summary.append("\nTheme preset: ").append(behavior.optString("theme_preset"));
            }
            if (root.has("workspace")) summary.append("\nWorkspace: ").append(root.optString("workspace"));
            summary.append("\nRun `config -import_profile ").append(file.getName()).append("` to apply it.");
            return summary.toString();
        } catch (Exception e) {
            return "Error previewing profile: " + e.getMessage();
        }
    }
}
