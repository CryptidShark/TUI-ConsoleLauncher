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

public class KeybindManager {
    public static final String FILENAME = "keybinds.json";
    private Map<String, String> keybinds = new HashMap<>();
    private File file;

    public KeybindManager(Context context) {
        this.file = new File(context.getFilesDir(), FILENAME);
        load();
    }

    public void load() {
        keybinds.clear();
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                keybinds.put(key.toUpperCase(), json.getString(key));
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public void save() {
        try {
            JSONObject json = new JSONObject(keybinds);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public boolean set(String key, String command) {
        if (key == null || command == null) return false;
        keybinds.put(key.trim().toUpperCase(), command.trim());
        save();
        return true;
    }

    public boolean remove(String key) {
        if (key == null) return false;
        String k = key.trim().toUpperCase();
        if (keybinds.containsKey(k)) {
            keybinds.remove(k);
            save();
            return true;
        }
        return false;
    }

    public String get(String key) {
        if (key == null) return null;
        return keybinds.get(key.trim().toUpperCase());
    }

    public Map<String, String> getAll() {
        return keybinds;
    }
}
