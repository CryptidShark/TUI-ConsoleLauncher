package ohi.andre.consolelauncher.commands.main.raw;

import android.content.Intent;
import android.graphics.Color;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.ThemeManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Suggestions;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class theme extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {

        preview {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String exec(ExecutePack pack) {
                String name = pack.getString();
                if (name == null || name.trim().isEmpty()) {
                    return "Usage: theme preview <name>\nAvailable: catppuccin_mocha, tokyo_night, gruvbox_hard, catppuccin_latte, watchdogs, ghost_shell, cyberpunk";
                }
                name = name.toLowerCase().trim();
                return "=== THEME PREVIEW: " + name.toUpperCase() + " ===\n" +
                       "• Primary Accent : Active / Prompt\n" +
                       "• Background     : Terminal Canvas\n" +
                       "• Status         : Preset ready to apply\n" +
                       "Run `theme preset " + name + "` to apply this theme permanently.";
            }
        },
        apply {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String exec(ExecutePack pack) {
                Intent intent = new Intent(ThemeManager.ACTION_APPLY);
                intent.putExtra(ThemeManager.NAME, pack.getString());
                LocalBroadcastManager.getInstance(pack.context.getApplicationContext()).sendBroadcast(intent);
                return null;
            }
        },
        set {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.CONFIG_ENTRY, CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String exec(ExecutePack pack) {
                Object o = pack.get();
                if (!(o instanceof Theme)) {
                    return "Invalid theme element.";
                }
                Theme element = (Theme) o;
                String color = pack.getString();

                try {
                    Color.parseColor(color);
                } catch (Exception e) {
                    return "Invalid color format. Use #RRGGBB or #AARRGGBB";
                }

                XMLPrefsManager.XMLPrefsRoot.THEME.write(element, color);

                try {
                    if (pack.context instanceof Reloadable) {
                        ((Reloadable) pack.context).reload();
                    }
                } catch (Exception e) {}

                return element.label() + " updated to " + color;
            }
        },
        preset {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.THEME_PRESET};
            }

            @Override
            public String exec(ExecutePack pack) {
                String name = pack.getString().toLowerCase();
                
                Map<Theme, String> colors = new HashMap<>();
                Map<Suggestions, String> suggestionColors = new HashMap<>();
                
                boolean isTransparent = XMLPrefsManager.getBoolean(Ui.system_wallpaper);
                Theme backgroundTarget = isTransparent ? Theme.overlay_color : Theme.bg_color;
                String transPrefix = isTransparent ? "#00" : "#FF";

                switch(name) {
                    case "watchdogs_cyber":
                    case "watchdogs":
                        colors.put(backgroundTarget, isTransparent ? "#000B0E14" : "#E60B0E14");
                        colors.put(Theme.input_color, "#00E5FF");
                        colors.put(Theme.output_color, "#F0F4F8");
                        colors.put(Theme.time_color, "#00E5FF");
                        colors.put(Theme.device_color, "#FFB86C");
                        colors.put(Theme.battery_color_high, "#00FF9F");
                        colors.put(Theme.battery_color_medium, "#FFD600");
                        colors.put(Theme.battery_color_low, "#FF2A54");
                        colors.put(Theme.storage_color, "#BD93F9");
                        colors.put(Theme.ram_color, "#FF2A54");
                        colors.put(Theme.network_info_color, "#00FF9F");
                        colors.put(Theme.weather_color, "#FF79C6");
                        colors.put(Theme.notes_color, "#00E5FF");
                        colors.put(Theme.notes_locked_color, "#BD93F9");
                        colors.put(Theme.cursor_color, "#00E5FF");
                        colors.put(Theme.enter_color, "#00E5FF");
                        colors.put(Theme.toolbar_color, "#00E5FF");
                        colors.put(Theme.alias_content_color, "#00FF9F");
                        colors.put(Theme.hint_color, "#00FF9F");
                        colors.put(Theme.statusbar_color, "#0B0E14");
                        colors.put(Theme.navigationbar_color, "#0B0E14");

                        suggestionColors.put(Suggestions.apps_bg_color, "#141A22");
                        suggestionColors.put(Suggestions.alias_bg_color, "#BD93F9");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#00E5FF");
                        suggestionColors.put(Suggestions.file_bg_color, "#5A6978");
                        suggestionColors.put(Suggestions.song_bg_color, "#FF79C6");
                        
                        suggestionColors.put(Suggestions.apps_text_color, "#F0F4F8");
                        suggestionColors.put(Suggestions.alias_text_color, "#000000");
                        suggestionColors.put(Suggestions.cmd_text_color, "#000000");
                        suggestionColors.put(Suggestions.file_text_color, "#FFFFFF");
                        suggestionColors.put(Suggestions.song_text_color, "#000000");

                        try {
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.time_index, "0");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.device_index, "1");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.battery_index, "2");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.storage_index, "3");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.ram_index, "4");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.network_index, "5");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.weather_index, "6");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.notes_index, "7");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.status_lines_alignment, "0,0,0,0,0,0,0,0,0");
                        } catch (Exception ignore) {}

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.WATCHDOGS_CYBER.name());
                        break;
                    case "blue":
                        colors.put(backgroundTarget, transPrefix + "001221");
                        colors.put(Theme.input_color, "#00BFFF");
                        colors.put(Theme.output_color, "#E0FFFF");
                        colors.put(Theme.device_color, "#1E90FF");
                        colors.put(Theme.enter_color, "#00BFFF");
                        colors.put(Theme.toolbar_color, "#00BFFF");
                        colors.put(Theme.time_color, "#87CEFA");

                        suggestionColors.put(Suggestions.apps_bg_color, "#0000FF");
                        suggestionColors.put(Suggestions.alias_bg_color, "#4169E1");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#00BFFF");
                        suggestionColors.put(Suggestions.file_bg_color, "#87CEFA");
                        suggestionColors.put(Suggestions.song_bg_color, "#1E90FF");
                        break;
                    case "red":
                        colors.put(backgroundTarget, transPrefix + "210000");
                        colors.put(Theme.input_color, "#FF4500");
                        colors.put(Theme.output_color, "#FFEBEE");
                        colors.put(Theme.device_color, "#B71C1C");
                        colors.put(Theme.enter_color, "#FF0000");
                        colors.put(Theme.toolbar_color, "#FF5252");
                        colors.put(Theme.time_color, "#FF8A80");

                        suggestionColors.put(Suggestions.apps_bg_color, "#FF0000");
                        suggestionColors.put(Suggestions.alias_bg_color, "#DC143C");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#FF4500");
                        suggestionColors.put(Suggestions.file_bg_color, "#FA8072");
                        suggestionColors.put(Suggestions.song_bg_color, "#B22222");
                        break;
                    case "green":
                        colors.put(backgroundTarget, transPrefix + "001B00");
                        colors.put(Theme.input_color, "#00FF41");
                        colors.put(Theme.output_color, "#D5F5E3");
                        colors.put(Theme.device_color, "#2ECC71");
                        colors.put(Theme.enter_color, "#00FF41");
                        colors.put(Theme.toolbar_color, "#27AE60");
                        colors.put(Theme.time_color, "#A9DFBF");

                        suggestionColors.put(Suggestions.apps_bg_color, "#00FF00");
                        suggestionColors.put(Suggestions.alias_bg_color, "#32CD32");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#00FF41");
                        suggestionColors.put(Suggestions.file_bg_color, "#90EE90");
                        suggestionColors.put(Suggestions.song_bg_color, "#228B22");
                        break;
                    case "pink":
                        colors.put(backgroundTarget, transPrefix + "1A0010");
                        colors.put(Theme.input_color, "#FF69B4");
                        colors.put(Theme.output_color, "#FCE4EC");
                        colors.put(Theme.device_color, "#AD1457");
                        colors.put(Theme.enter_color, "#FF1493");
                        colors.put(Theme.toolbar_color, "#F06292");
                        colors.put(Theme.time_color, "#F8BBD0");

                        suggestionColors.put(Suggestions.apps_bg_color, "#FF69B4");
                        suggestionColors.put(Suggestions.alias_bg_color, "#FF1493");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#FFB6C1");
                        suggestionColors.put(Suggestions.file_bg_color, "#FFC0CB");
                        suggestionColors.put(Suggestions.song_bg_color, "#C71585");
                        break;
                    case "bw":
                        colors.put(backgroundTarget, transPrefix + "000000");
                        colors.put(Theme.input_color, "#FFFFFF");
                        colors.put(Theme.output_color, "#CCCCCC");
                        colors.put(Theme.device_color, "#AAAAAA");
                        colors.put(Theme.enter_color, "#FFFFFF");
                        colors.put(Theme.toolbar_color, "#FFFFFF");
                        colors.put(Theme.time_color, "#FFFFFF");

                        suggestionColors.put(Suggestions.apps_bg_color, "#FFFFFF");
                        suggestionColors.put(Suggestions.alias_bg_color, "#EEEEEE");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#DDDDDD");
                        suggestionColors.put(Suggestions.file_bg_color, "#CCCCCC");
                        suggestionColors.put(Suggestions.song_bg_color, "#BBBBBB");
                        
                        suggestionColors.put(Suggestions.apps_text_color, "#000000");
                        suggestionColors.put(Suggestions.alias_text_color, "#000000");
                        suggestionColors.put(Suggestions.cmd_text_color, "#000000");
                        suggestionColors.put(Suggestions.file_text_color, "#000000");
                        suggestionColors.put(Suggestions.song_text_color, "#000000");
                        break;
                    case "cyberpunk":
                        colors.put(backgroundTarget, transPrefix + "0D0615");
                        colors.put(Theme.input_color, "#FCEE09"); 
                        colors.put(Theme.output_color, "#00F0FF"); 
                        colors.put(Theme.device_color, "#FF003C"); 
                        colors.put(Theme.enter_color, "#FCEE09");
                        colors.put(Theme.toolbar_color, "#39FF14"); 
                        colors.put(Theme.time_color, "#00F0FF");

                        suggestionColors.put(Suggestions.apps_bg_color, "#FF003C");
                        suggestionColors.put(Suggestions.alias_bg_color, "#FCEE09");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#00F0FF");
                        suggestionColors.put(Suggestions.file_bg_color, "#39FF14");
                        suggestionColors.put(Suggestions.song_bg_color, "#BC00FF");
                        
                        suggestionColors.put(Suggestions.alias_text_color, "#000000");
                        break;
                    case "ghost_shell":
                    case "ghost":
                    case "section9":
                        colors.put(backgroundTarget, isTransparent ? "#00040C0E" : "#E6040C0E");
                        colors.put(Theme.input_color, "#00FFD1");
                        colors.put(Theme.output_color, "#E8F8F5");
                        colors.put(Theme.time_color, "#00FFD1");
                        colors.put(Theme.device_color, "#FFB000");
                        colors.put(Theme.battery_color_high, "#00FF9D");
                        colors.put(Theme.battery_color_medium, "#FFD000");
                        colors.put(Theme.battery_color_low, "#FF3B5C");
                        colors.put(Theme.storage_color, "#7D5FFF");
                        colors.put(Theme.ram_color, "#FF3B5C");
                        colors.put(Theme.network_info_color, "#00E5FF");
                        colors.put(Theme.weather_color, "#E056FD");
                        colors.put(Theme.notes_color, "#00FFD1");
                        colors.put(Theme.notes_locked_color, "#7D5FFF");
                        colors.put(Theme.cursor_color, "#00FFD1");
                        colors.put(Theme.enter_color, "#00FFD1");
                        colors.put(Theme.toolbar_color, "#00FFD1");
                        colors.put(Theme.alias_content_color, "#00FF9D");
                        colors.put(Theme.hint_color, "#00FFD1");
                        colors.put(Theme.statusbar_color, "#040C0E");
                        colors.put(Theme.navigationbar_color, "#040C0E");

                        suggestionColors.put(Suggestions.apps_bg_color, "#0E171B");
                        suggestionColors.put(Suggestions.alias_bg_color, "#7D5FFF");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#00FFD1");
                        suggestionColors.put(Suggestions.file_bg_color, "#4A6572");
                        suggestionColors.put(Suggestions.song_bg_color, "#E056FD");
                        
                        suggestionColors.put(Suggestions.apps_text_color, "#E8F8F5");
                        suggestionColors.put(Suggestions.alias_text_color, "#FFFFFF");
                        suggestionColors.put(Suggestions.cmd_text_color, "#000000");
                        suggestionColors.put(Suggestions.file_text_color, "#FFFFFF");
                        suggestionColors.put(Suggestions.song_text_color, "#FFFFFF");

                        try {
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.time_index, "0");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.device_index, "1");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.battery_index, "2");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.storage_index, "3");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.ram_index, "4");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.network_index, "5");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.weather_index, "6");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.notes_index, "7");
                            XMLPrefsManager.XMLPrefsRoot.UI.write(Ui.status_lines_alignment, "0,0,0,0,0,0,0,0,0");
                        } catch (Exception ignore) {}

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.GHOST_SHELL.name());
                        break;
                    case "catppuccin_mocha":
                        colors.put(backgroundTarget, isTransparent ? "#001e1e2e" : "#E61e1e2e");
                        colors.put(Theme.input_color, "#89b4fa");
                        colors.put(Theme.output_color, "#cdd6f4");
                        colors.put(Theme.time_color, "#f5c2e7");
                        colors.put(Theme.device_color, "#cba6f7");
                        colors.put(Theme.battery_color_high, "#a6e3a1");
                        colors.put(Theme.battery_color_medium, "#f9e2af");
                        colors.put(Theme.battery_color_low, "#f38ba8");
                        colors.put(Theme.storage_color, "#cba6f7");
                        colors.put(Theme.ram_color, "#f38ba8");
                        colors.put(Theme.network_info_color, "#a6e3a1");
                        colors.put(Theme.weather_color, "#89dceb");
                        colors.put(Theme.notes_color, "#89b4fa");
                        colors.put(Theme.notes_locked_color, "#cba6f7");
                        colors.put(Theme.cursor_color, "#f5c2e7");
                        colors.put(Theme.enter_color, "#89b4fa");
                        colors.put(Theme.toolbar_color, "#89b4fa");
                        colors.put(Theme.alias_content_color, "#a6e3a1");
                        colors.put(Theme.hint_color, "#a6e3a1");
                        colors.put(Theme.statusbar_color, "#1e1e2e");
                        colors.put(Theme.navigationbar_color, "#1e1e2e");

                        suggestionColors.put(Suggestions.apps_bg_color, "#313244");
                        suggestionColors.put(Suggestions.alias_bg_color, "#cba6f7");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#89b4fa");
                        suggestionColors.put(Suggestions.file_bg_color, "#6c7086");
                        suggestionColors.put(Suggestions.song_bg_color, "#f5c2e7");
                        suggestionColors.put(Suggestions.apps_text_color, "#cdd6f4");

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.CATPPUCCIN_MOCHA.name());
                        break;
                    case "tokyo_night":
                        colors.put(backgroundTarget, isTransparent ? "#001a1b26" : "#E61a1b26");
                        colors.put(Theme.input_color, "#7aa2f7");
                        colors.put(Theme.output_color, "#c0caf5");
                        colors.put(Theme.time_color, "#7dcfff");
                        colors.put(Theme.device_color, "#bb9af7");
                        colors.put(Theme.battery_color_high, "#9ece6a");
                        colors.put(Theme.battery_color_medium, "#e0af68");
                        colors.put(Theme.battery_color_low, "#f7768e");
                        colors.put(Theme.storage_color, "#bb9af7");
                        colors.put(Theme.ram_color, "#f7768e");
                        colors.put(Theme.network_info_color, "#9ece6a");
                        colors.put(Theme.weather_color, "#2ac3de");
                        colors.put(Theme.notes_color, "#7aa2f7");
                        colors.put(Theme.notes_locked_color, "#bb9af7");
                        colors.put(Theme.cursor_color, "#c0caf5");
                        colors.put(Theme.enter_color, "#7aa2f7");
                        colors.put(Theme.toolbar_color, "#7aa2f7");
                        colors.put(Theme.alias_content_color, "#9ece6a");
                        colors.put(Theme.hint_color, "#9ece6a");
                        colors.put(Theme.statusbar_color, "#1a1b26");
                        colors.put(Theme.navigationbar_color, "#1a1b26");

                        suggestionColors.put(Suggestions.apps_bg_color, "#24283b");
                        suggestionColors.put(Suggestions.alias_bg_color, "#bb9af7");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#7aa2f7");
                        suggestionColors.put(Suggestions.file_bg_color, "#565f89");
                        suggestionColors.put(Suggestions.song_bg_color, "#7dcfff");
                        suggestionColors.put(Suggestions.apps_text_color, "#c0caf5");

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.TOKYO_NIGHT.name());
                        break;
                    case "gruvbox_hard":
                    case "gruvbox":
                        colors.put(backgroundTarget, isTransparent ? "#001d2021" : "#E61d2021");
                        colors.put(Theme.input_color, "#fabd2f");
                        colors.put(Theme.output_color, "#ebdbb2");
                        colors.put(Theme.time_color, "#fe8019");
                        colors.put(Theme.device_color, "#b8bb26");
                        colors.put(Theme.battery_color_high, "#b8bb26");
                        colors.put(Theme.battery_color_medium, "#fabd2f");
                        colors.put(Theme.battery_color_low, "#fb4934");
                        colors.put(Theme.storage_color, "#83a598");
                        colors.put(Theme.ram_color, "#fb4934");
                        colors.put(Theme.network_info_color, "#b8bb26");
                        colors.put(Theme.weather_color, "#fabd2f");
                        colors.put(Theme.notes_color, "#fabd2f");
                        colors.put(Theme.notes_locked_color, "#83a598");
                        colors.put(Theme.cursor_color, "#fabd2f");
                        colors.put(Theme.enter_color, "#fabd2f");
                        colors.put(Theme.toolbar_color, "#fabd2f");
                        colors.put(Theme.alias_content_color, "#b8bb26");
                        colors.put(Theme.hint_color, "#b8bb26");
                        colors.put(Theme.statusbar_color, "#1d2021");
                        colors.put(Theme.navigationbar_color, "#1d2021");

                        suggestionColors.put(Suggestions.apps_bg_color, "#282828");
                        suggestionColors.put(Suggestions.alias_bg_color, "#83a598");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#fabd2f");
                        suggestionColors.put(Suggestions.file_bg_color, "#928374");
                        suggestionColors.put(Suggestions.song_bg_color, "#fe8019");
                        suggestionColors.put(Suggestions.apps_text_color, "#ebdbb2");

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.GRUVBOX_HARD.name());
                        break;
                    case "catppuccin_latte":
                        colors.put(backgroundTarget, isTransparent ? "#00eff1f5" : "#E6eff1f5");
                        colors.put(Theme.input_color, "#1e66f5");
                        colors.put(Theme.output_color, "#4c4f69");
                        colors.put(Theme.time_color, "#ea76cb");
                        colors.put(Theme.device_color, "#8839ef");
                        colors.put(Theme.battery_color_high, "#40a02b");
                        colors.put(Theme.battery_color_medium, "#df8e1d");
                        colors.put(Theme.battery_color_low, "#d20f39");
                        colors.put(Theme.storage_color, "#8839ef");
                        colors.put(Theme.ram_color, "#d20f39");
                        colors.put(Theme.network_info_color, "#40a02b");
                        colors.put(Theme.weather_color, "#04a5e5");
                        colors.put(Theme.notes_color, "#1e66f5");
                        colors.put(Theme.notes_locked_color, "#8839ef");
                        colors.put(Theme.cursor_color, "#ea76cb");
                        colors.put(Theme.enter_color, "#1e66f5");
                        colors.put(Theme.toolbar_color, "#1e66f5");
                        colors.put(Theme.alias_content_color, "#40a02b");
                        colors.put(Theme.hint_color, "#40a02b");
                        colors.put(Theme.statusbar_color, "#eff1f5");
                        colors.put(Theme.navigationbar_color, "#eff1f5");

                        suggestionColors.put(Suggestions.apps_bg_color, "#e6e9ef");
                        suggestionColors.put(Suggestions.alias_bg_color, "#8839ef");
                        suggestionColors.put(Suggestions.cmd_bg_color, "#1e66f5");
                        suggestionColors.put(Suggestions.file_bg_color, "#9ca0b0");
                        suggestionColors.put(Suggestions.song_bg_color, "#ea76cb");
                        suggestionColors.put(Suggestions.apps_text_color, "#4c4f69");

                        Behavior.theme_preset.parent().write(Behavior.theme_preset, ThemeEngine.Preset.CATPPUCCIN_LATTE.name());
                        break;
                    default:
                        return "Unknown preset. Available: catppuccin_mocha, tokyo_night, gruvbox_hard, catppuccin_latte, watchdogs, ghost_shell, blue, red, green, pink, bw, cyberpunk";
                }

                colors.put(Theme.toolbar_bg, "#00000000");

                for (Map.Entry<Theme, String> entry : colors.entrySet()) {
                    XMLPrefsManager.XMLPrefsRoot.THEME.write(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<Suggestions, String> entry : suggestionColors.entrySet()) {
                    XMLPrefsManager.XMLPrefsRoot.SUGGESTIONS.write(entry.getKey(), entry.getValue());
                }

                try {
                    if (pack.context instanceof Reloadable) {
                        ((Reloadable) pack.context).reload();
                    }
                } catch (Exception e) {}

                return "Applied " + name + " preset!";
            }
        },
        export {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String exec(ExecutePack pack) {
                String filename = pack.getString();
                if (filename == null || filename.trim().isEmpty()) {
                    filename = "theme_export.json";
                }
                if (!filename.endsWith(".json")) {
                    filename += ".json";
                }

                try {
                    JSONObject json = new JSONObject();
                    json.put("version", 1);
                    json.put("themeName", XMLPrefsManager.get(Behavior.theme_preset));
                    json.put("input_color", XMLPrefsManager.get(Theme.input_color));
                    json.put("output_color", XMLPrefsManager.get(Theme.output_color));
                    json.put("bg_color", XMLPrefsManager.get(Theme.bg_color));
                    json.put("device_color", XMLPrefsManager.get(Theme.device_color));
                    json.put("enter_color", XMLPrefsManager.get(Theme.enter_color));
                    json.put("toolbar_color", XMLPrefsManager.get(Theme.toolbar_color));
                    json.put("time_color", XMLPrefsManager.get(Theme.time_color));

                    File folder = Tuils.getFolder();
                    File dest = new File(folder, filename);
                    FileOutputStream fos = new FileOutputStream(dest);
                    fos.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
                    fos.flush();
                    fos.close();

                    return "Theme exported to " + dest.getAbsolutePath();
                } catch (Exception e) {
                    return "Error exporting theme: " + e.getMessage();
                }
            }
        },
        import_theme {
            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.PLAIN_TEXT};
            }

            @Override
            public String exec(ExecutePack pack) {
                String filename = pack.getString();
                if (filename == null || filename.trim().isEmpty()) {
                    return "Usage: theme -import_theme [filename.json]";
                }

                File file = new File(Tuils.getFolder(), filename);
                if (!file.exists()) {
                    file = new File(filename);
                }
                if (!file.exists()) {
                    return "Theme file not found: " + filename;
                }

                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();

                    String jsonStr = new String(data, StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(jsonStr);

                    int version = json.optInt("version", -1);
                    if (version != 1) {
                        return "Invalid or unsupported theme schema version: " + version;
                    }

                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (key.equals("version") || key.equals("themeName")) continue;

                        String colorVal = json.getString(key);
                        try {
                            Color.parseColor(colorVal);
                            for (Theme t : Theme.values()) {
                                if (t.label().equalsIgnoreCase(key)) {
                                    XMLPrefsManager.XMLPrefsRoot.THEME.write(t, colorVal);
                                    break;
                                }
                            }
                        } catch (Exception invalidColor) {
                            Tuils.log("Skipping invalid color: " + key + "=" + colorVal);
                        }
                    }

                    if (pack.context instanceof Reloadable) {
                        ((Reloadable) pack.context).reload();
                    }

                    return "Theme successfully imported from " + file.getName();
                } catch (Exception e) {
                    return "Error importing theme (malformed file): " + e.getMessage();
                }
            }
        },
        standard {
            @Override
            public int[] args() {
                return new int[] {};
            }

            @Override
            public String exec(ExecutePack pack) {
                LocalBroadcastManager.getInstance(pack.context.getApplicationContext()).sendBroadcast(new Intent(ThemeManager.ACTION_STANDARD));
                return null;
            }
        },
        old {
            @Override
            public String exec(ExecutePack pack) {
                LocalBroadcastManager.getInstance(pack.context.getApplicationContext()).sendBroadcast(new Intent(ThemeManager.ACTION_REVERT));
                return null;
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];

            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }

            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_theme);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return null;
        }

        @Override
        public int[] args() {
            return new int[0];
        }
    }

    @Override
    public String[] params() {
        return Param.labels();
    };

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public int helpRes() {
        return R.string.help_theme;
    }

    @Override
    protected String doThings(ExecutePack pack) {
        return null;
    }
}
