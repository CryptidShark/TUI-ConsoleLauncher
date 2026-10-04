package ohi.andre.consolelauncher.managers;

import android.graphics.Color;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import java.util.HashMap;
import java.util.Map;

import ohi.andre.consolelauncher.managers.xml.options.Theme;

public class ThemeEngine {

    public enum Preset {
        CLASSIC_TERMINAL,
        MODERN_TERMINAL,
        CYBER_TERMINAL,
        NEO_TERMINAL,
        AMBER_TERMINAL,
        NORD_TERMINAL,
        DRACULA_TERMINAL,
        AMOLED_TERMINAL
    }

    public static class DesignTokens {
        public int background;
        public int surface;
        public int primary;
        public int secondary;
        public int accent;
        public int text;
        public int textMuted;
        public int success;
        public int warning;
        public int error;
        public int info;
        public int border;
        public int cursor;
        public int selection;

        // Visual properties
        public float borderRadius;
        public float borderWidth;
        public boolean useShadows;
    }

    private static final Map<Preset, DesignTokens> presets = new HashMap<>();

    static {
        // CLASSIC TERMINAL
        DesignTokens classic = new DesignTokens();
        classic.background = Color.parseColor("#B3000000"); // 70% opacity black
        classic.surface = Color.parseColor("#121212");
        classic.primary = Color.GREEN;
        classic.secondary = Color.parseColor("#008000");
        classic.accent = Color.parseColor("#ADFF2F");
        classic.text = Color.GREEN;
        classic.textMuted = Color.parseColor("#006400");
        classic.success = Color.GREEN;
        classic.warning = Color.YELLOW;
        classic.error = Color.RED;
        classic.info = Color.CYAN;
        classic.border = Color.GREEN;
        classic.cursor = Color.GREEN;
        classic.selection = Color.parseColor("#3300FF00");
        classic.borderRadius = 0;
        classic.borderWidth = 1;
        classic.useShadows = false;
        presets.put(Preset.CLASSIC_TERMINAL, classic);

        // MODERN TERMINAL
        DesignTokens modern = new DesignTokens();
        modern.background = Color.parseColor("#B31A1B26"); // 70% opacity
        modern.surface = Color.parseColor("#24283B");
        modern.primary = Color.parseColor("#7AA2F7");
        modern.secondary = Color.parseColor("#BB9AF7");
        modern.accent = Color.parseColor("#7DCFFF");
        modern.text = Color.parseColor("#C0CAF5");
        modern.textMuted = Color.parseColor("#565F89");
        modern.success = Color.parseColor("#9ECE6A");
        modern.warning = Color.parseColor("#E0AF68");
        modern.error = Color.parseColor("#F7768E");
        modern.info = Color.parseColor("#2AC3DE");
        modern.border = Color.parseColor("#414868");
        modern.cursor = Color.parseColor("#C0CAF5");
        modern.selection = Color.parseColor("#337AA2F7");
        modern.borderRadius = 6;
        modern.borderWidth = 1;
        modern.useShadows = true;
        presets.put(Preset.MODERN_TERMINAL, modern);

        // CYBER TERMINAL
        DesignTokens cyber = new DesignTokens();
        cyber.background = Color.parseColor("#B30D0D0D"); // 70% opacity
        cyber.surface = Color.parseColor("#1A0033");
        cyber.primary = Color.parseColor("#FF00FF"); // Magenta
        cyber.secondary = Color.parseColor("#00FFFF"); // Cyan
        cyber.accent = Color.parseColor("#FFFF00"); // Yellow
        cyber.text = Color.parseColor("#00FF9F"); // Matrix Green
        cyber.textMuted = Color.parseColor("#004D40");
        cyber.success = Color.parseColor("#00FF00");
        cyber.warning = Color.parseColor("#FFA500");
        cyber.error = Color.parseColor("#FF0000");
        cyber.info = Color.parseColor("#00BCD4");
        cyber.border = Color.parseColor("#FF00FF");
        cyber.cursor = Color.parseColor("#00FF9F");
        cyber.selection = Color.parseColor("#33FF00FF");
        cyber.borderRadius = 4;
        cyber.borderWidth = 1;
        cyber.useShadows = true;
        presets.put(Preset.CYBER_TERMINAL, cyber);

        // NEO TERMINAL
        DesignTokens neo = new DesignTokens();
        neo.background = Color.parseColor("#B31A1B26"); // 70% opacity Purple/Blue
        neo.surface = Color.parseColor("#4D282A36"); // Transparent Surface
        neo.primary = Color.parseColor("#8BE9FD"); // Cyan Prompt
        neo.secondary = Color.parseColor("#BD93F9"); // Purple Details
        neo.accent = Color.parseColor("#FF79C6"); // Pink Accents
        neo.text = Color.parseColor("#F8F8F2"); // Foreground White
        neo.textMuted = Color.parseColor("#6272A4"); // Muted Blue/Grey
        neo.success = Color.parseColor("#50FA7B"); // Green
        neo.warning = Color.parseColor("#F1FA8C"); // Yellow
        neo.error = Color.parseColor("#FF5555"); // Red
        neo.info = Color.parseColor("#BD93F9"); // Info Purple
        neo.border = Color.parseColor("#6272A4"); // Border Grey
        neo.cursor = Color.parseColor("#8BE9FD"); // Cursor Cyan
        neo.selection = Color.parseColor("#44BD93F9"); // Selection Purple
        neo.borderRadius = 2; // Sharp but slight rounding
        neo.borderWidth = 1;
        neo.useShadows = true;
        presets.put(Preset.NEO_TERMINAL, neo);

        // AMBER TERMINAL (Retro CRT Phosphor)
        DesignTokens amber = new DesignTokens();
        amber.background = Color.parseColor("#B30A0800");
        amber.surface = Color.parseColor("#1A1200");
        amber.primary = Color.parseColor("#FFB000"); // CRT Amber
        amber.secondary = Color.parseColor("#FF9900");
        amber.accent = Color.parseColor("#FFCC00");
        amber.text = Color.parseColor("#FFB000");
        amber.textMuted = Color.parseColor("#805800");
        amber.success = Color.parseColor("#FFB000");
        amber.warning = Color.parseColor("#FF8C00");
        amber.error = Color.parseColor("#FF3300");
        amber.info = Color.parseColor("#FFC107");
        amber.border = Color.parseColor("#FFB000");
        amber.cursor = Color.parseColor("#FFB000");
        amber.selection = Color.parseColor("#44FFB000");
        amber.borderRadius = 0;
        amber.borderWidth = 1;
        amber.useShadows = false;
        presets.put(Preset.AMBER_TERMINAL, amber);

        // NORD TERMINAL (Arctic Frost)
        DesignTokens nord = new DesignTokens();
        nord.background = Color.parseColor("#B32E3440");
        nord.surface = Color.parseColor("#3B4252");
        nord.primary = Color.parseColor("#88C0D0"); // Frost Cyan
        nord.secondary = Color.parseColor("#81A1C1"); // Frost Blue
        nord.accent = Color.parseColor("#B48EAD"); // Aurora Purple
        nord.text = Color.parseColor("#ECEFF4"); // Snow White
        nord.textMuted = Color.parseColor("#4C566A");
        nord.success = Color.parseColor("#A3BE8C"); // Aurora Green
        nord.warning = Color.parseColor("#EBCB8B"); // Aurora Yellow
        nord.error = Color.parseColor("#BF616A"); // Aurora Red
        nord.info = Color.parseColor("#5E81AC");
        nord.border = Color.parseColor("#4C566A");
        nord.cursor = Color.parseColor("#88C0D0");
        nord.selection = Color.parseColor("#4488C0D0");
        nord.borderRadius = 8;
        nord.borderWidth = 1;
        nord.useShadows = true;
        presets.put(Preset.NORD_TERMINAL, nord);

        // DRACULA TERMINAL
        DesignTokens dracula = new DesignTokens();
        dracula.background = Color.parseColor("#E6282A36"); // Dracula Background
        dracula.surface = Color.parseColor("#44475A"); // Selection / Current Line
        dracula.primary = Color.parseColor("#8BE9FD"); // Cyan
        dracula.secondary = Color.parseColor("#BD93F9"); // Purple
        dracula.accent = Color.parseColor("#FF79C6"); // Pink
        dracula.text = Color.parseColor("#F8F8F2"); // Foreground
        dracula.textMuted = Color.parseColor("#6272A4"); // Comment
        dracula.success = Color.parseColor("#50FA7B"); // Green
        dracula.warning = Color.parseColor("#F1FA8C"); // Yellow
        dracula.error = Color.parseColor("#FF5555"); // Red
        dracula.info = Color.parseColor("#BD93F9");
        dracula.border = Color.parseColor("#6272A4");
        dracula.cursor = Color.parseColor("#FF79C6");
        dracula.selection = Color.parseColor("#4444475A");
        dracula.borderRadius = 6;
        dracula.borderWidth = 1;
        dracula.useShadows = true;
        presets.put(Preset.DRACULA_TERMINAL, dracula);

        // AMOLED TERMINAL (Pure Black Battery Saver)
        DesignTokens amoled = new DesignTokens();
        amoled.background = Color.parseColor("#FF000000"); // 100% Pure Black
        amoled.surface = Color.parseColor("#111111");
        amoled.primary = Color.parseColor("#00FF9F"); // High Contrast Neon Green
        amoled.secondary = Color.parseColor("#00B8D4");
        amoled.accent = Color.parseColor("#FF4081");
        amoled.text = Color.parseColor("#FFFFFF");
        amoled.textMuted = Color.parseColor("#777777");
        amoled.success = Color.parseColor("#00FF9F");
        amoled.warning = Color.parseColor("#FFD600");
        amoled.error = Color.parseColor("#FF1744");
        amoled.info = Color.parseColor("#00B8D4");
        amoled.border = Color.parseColor("#333333");
        amoled.cursor = Color.parseColor("#00FF9F");
        amoled.selection = Color.parseColor("#3300FF9F");
        amoled.borderRadius = 4;
        amoled.borderWidth = 1;
        amoled.useShadows = false;
        presets.put(Preset.AMOLED_TERMINAL, amoled);
    }

    public static DesignTokens getPreset(Preset preset) {
        DesignTokens tokens = presets.get(preset);
        if (tokens == null) return presets.get(Preset.CLASSIC_TERMINAL);
        
        // Merge with XML preferences for backward compatibility
        try {
            int xmlOutputColor = XMLPrefsManager.getColor(Theme.output_color);
            if (xmlOutputColor != Color.WHITE && xmlOutputColor != 0) {
                tokens.text = xmlOutputColor;
            }
            
            int xmlInputColor = XMLPrefsManager.getColor(Theme.input_color);
            if (xmlInputColor != Color.GREEN && xmlInputColor != 0) {
                tokens.primary = xmlInputColor;
                tokens.cursor = xmlInputColor;
            }

            int xmlBgColor = XMLPrefsManager.getColor(Theme.bg_color);
            // Only overwrite if it's not the default black/zero
            if (xmlBgColor != Color.BLACK && xmlBgColor != 0) {
                tokens.background = xmlBgColor;
            }
        } catch (Exception ignored) {}

        return tokens;
    }
}
