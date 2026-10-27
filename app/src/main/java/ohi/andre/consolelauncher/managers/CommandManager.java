package ohi.andre.consolelauncher.managers;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import ohi.andre.consolelauncher.tuils.Tuils;

public class CommandManager {
    public static final String FILENAME = "commands.json";
    private Map<String, String> customCommands = new HashMap<>();
    private File file;

    public CommandManager(Context context) {
        this.file = new File(context.getFilesDir(), FILENAME);
        load();
    }

    public void load() {
        customCommands.clear();
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                customCommands.put(key, json.getString(key));
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public void save() {
        try {
            JSONObject json = new JSONObject(customCommands);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public boolean create(String name, String action) {
        if (name == null || name.trim().isEmpty() || action == null || action.trim().isEmpty()) {
            return false;
        }
        customCommands.put(name.trim().toLowerCase(), action.trim());
        save();
        return true;
    }

    public boolean remove(String name) {
        if (name == null) return false;
        String key = name.trim().toLowerCase();
        if (customCommands.containsKey(key)) {
            customCommands.remove(key);
            save();
            return true;
        }
        return false;
    }

    public String get(String name) {
        if (name == null) return null;
        return customCommands.get(name.trim().toLowerCase());
    }

    public Map<String, String> getAll() {
        return customCommands;
    }
}
