package ohi.andre.consolelauncher.managers;

import android.graphics.Color;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import java.util.HashMap;
import java.util.Map;

import ohi.andre.consolelauncher.managers.xml.options.Theme;

public class ThemeEngine {

    public enum Preset {
        CLASSIC_TERMINAL,
        WINDOWS_TERMINAL,
        MODERN_TERMINAL,
        CYBER_TERMINAL,
        NEO_TERMINAL,
        AMBER_TERMINAL,
        NORD_TERMINAL,
        DRACULA_TERMINAL,
        AMOLED_TERMINAL,
        WATCHDOGS_CYBER,
        GHOST_SHELL,
        CATPPUCCIN_MOCHA,
        TOKYO_NIGHT,
        GRUVBOX_HARD,
        CATPPUCCIN_LATTE
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

        // Windows Terminal inspired: quiet charcoal surfaces and a clear blue accent.
        DesignTokens windows = new DesignTokens();
        windows.background = Color.parseColor("#FF101214");
        windows.surface = Color.parseColor("#FF1C1E22");
        windows.primary = Color.parseColor("#FF4FC1FF");
        windows.secondary = Color.parseColor("#FF8AB4F8");
        windows.accent = Color.parseColor("#FF0078D4");
        windows.text = Color.parseColor("#FFE6E6E6");
        windows.textMuted = Color.parseColor("#FF9A9DA3");
        windows.success = Color.parseColor("#FF6CCB5F");
        windows.warning = Color.parseColor("#FFFFC857");
        windows.error = Color.parseColor("#FFFF6B6B");
        windows.info = Color.parseColor("#FF4FC1FF");
        windows.border = Color.parseColor("#FF35383E");
        windows.cursor = Color.parseColor("#FF4FC1FF");
        windows.selection = Color.parseColor("#554FC1FF");
        windows.borderRadius = 10;
        windows.borderWidth = 1;
        windows.useShadows = true;
        presets.put(Preset.WINDOWS_TERMINAL, windows);

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

        // WATCHDOGS_CYBER (Watch Dogs ctOS Minimalist Cyberpunk)
        DesignTokens watchdogs = new DesignTokens();
        watchdogs.background = Color.parseColor("#E60B0E14"); // Deep CtOS Slate / Cyber Dark
        watchdogs.surface = Color.parseColor("#141A22");    // Metallic Surface
        watchdogs.primary = Color.parseColor("#00E5FF");    // Electric Cyan
        watchdogs.secondary = Color.parseColor("#BD93F9");  // Cyber Violet
        watchdogs.accent = Color.parseColor("#FF79C6");     // Neon Magenta
        watchdogs.text = Color.parseColor("#F0F4F8");       // Crisp Tech White
        watchdogs.textMuted = Color.parseColor("#5A6978");  // Muted Tech Metallic Grey
        watchdogs.success = Color.parseColor("#00FF9F");    // Cyber Lime
        watchdogs.warning = Color.parseColor("#FFD600");    // Tech Yellow
        watchdogs.error = Color.parseColor("#FF2A54");      // Glitch Crimson
        watchdogs.info = Color.parseColor("#00E5FF");       // Electric Cyan
        watchdogs.border = Color.parseColor("#00E5FF");     // Electric Cyan Border
        watchdogs.cursor = Color.parseColor("#00E5FF");     // Electric Cyan Cursor
        watchdogs.selection = Color.parseColor("#3300E5FF");
        watchdogs.borderRadius = 2;
        watchdogs.borderWidth = 1;
        watchdogs.useShadows = true;
        presets.put(Preset.WATCHDOGS_CYBER, watchdogs);

        // GHOST_SHELL (Section 9 Cyber-Tactical Neural Interface)
        DesignTokens ghostShell = new DesignTokens();
        ghostShell.background = Color.parseColor("#E6040C0E"); // Tactical Stealth Obsidian
        ghostShell.surface = Color.parseColor("#0E171B");    // Deep Neural Surface
        ghostShell.primary = Color.parseColor("#00FFD1");    // Section 9 Neon Cyan
        ghostShell.secondary = Color.parseColor("#7D5FFF");  // Deep Cobalt
        ghostShell.accent = Color.parseColor("#E056FD");     // Holographic Violet
        ghostShell.text = Color.parseColor("#E8F8F5");       // Phosphor Tech White
        ghostShell.textMuted = Color.parseColor("#4A6572");  // Stealth Grey
        ghostShell.success = Color.parseColor("#00FF9D");    // Emerald Signal
        ghostShell.warning = Color.parseColor("#FFD000");    // Tech Amber
        ghostShell.error = Color.parseColor("#FF3B5C");      // Tactical Crimson
        ghostShell.info = Color.parseColor("#00FFD1");       // Section 9 Cyan
        ghostShell.border = Color.parseColor("#00FFD1");     // Neon Cyan Border
        ghostShell.cursor = Color.parseColor("#00FFD1");     // Neon Cyan Cursor
        ghostShell.selection = Color.parseColor("#3300FFD1");
        ghostShell.borderRadius = 2;
        ghostShell.borderWidth = 1;
        ghostShell.useShadows = true;
        presets.put(Preset.GHOST_SHELL, ghostShell);

        // CATPPUCCIN_MOCHA
        DesignTokens catppuccinMocha = new DesignTokens();
        catppuccinMocha.background = Color.parseColor("#E61E1E2E");
        catppuccinMocha.surface = Color.parseColor("#313244");
        catppuccinMocha.primary = Color.parseColor("#89b4fa");
        catppuccinMocha.secondary = Color.parseColor("#cba6f7");
        catppuccinMocha.accent = Color.parseColor("#f5c2e7");
        catppuccinMocha.text = Color.parseColor("#cdd6f4");
        catppuccinMocha.textMuted = Color.parseColor("#6c7086");
        catppuccinMocha.success = Color.parseColor("#a6e3a1");
        catppuccinMocha.warning = Color.parseColor("#f9e2af");
        catppuccinMocha.error = Color.parseColor("#f38ba8");
        catppuccinMocha.info = Color.parseColor("#89dceb");
        catppuccinMocha.border = Color.parseColor("#45475a");
        catppuccinMocha.cursor = Color.parseColor("#f5c2e7");
        catppuccinMocha.selection = Color.parseColor("#3389b4fa");
        catppuccinMocha.borderRadius = 6;
        catppuccinMocha.borderWidth = 1;
        catppuccinMocha.useShadows = true;
        presets.put(Preset.CATPPUCCIN_MOCHA, catppuccinMocha);

        // TOKYO_NIGHT
        DesignTokens tokyoNight = new DesignTokens();
        tokyoNight.background = Color.parseColor("#E61A1B26");
        tokyoNight.surface = Color.parseColor("#24283b");
        tokyoNight.primary = Color.parseColor("#7aa2f7");
        tokyoNight.secondary = Color.parseColor("#bb9af7");
        tokyoNight.accent = Color.parseColor("#7dcfff");
        tokyoNight.text = Color.parseColor("#c0caf5");
        tokyoNight.textMuted = Color.parseColor("#565f89");
        tokyoNight.success = Color.parseColor("#9ece6a");
        tokyoNight.warning = Color.parseColor("#e0af68");
        tokyoNight.error = Color.parseColor("#f7768e");
        tokyoNight.info = Color.parseColor("#2ac3de");
        tokyoNight.border = Color.parseColor("#414868");
        tokyoNight.cursor = Color.parseColor("#c0caf5");
        tokyoNight.selection = Color.parseColor("#337aa2f7");
        tokyoNight.borderRadius = 6;
        tokyoNight.borderWidth = 1;
        tokyoNight.useShadows = true;
        presets.put(Preset.TOKYO_NIGHT, tokyoNight);

        // GRUVBOX_HARD
        DesignTokens gruvboxHard = new DesignTokens();
        gruvboxHard.background = Color.parseColor("#E61D2021");
        gruvboxHard.surface = Color.parseColor("#282828");
        gruvboxHard.primary = Color.parseColor("#fabd2f");
        gruvboxHard.secondary = Color.parseColor("#fe8019");
        gruvboxHard.accent = Color.parseColor("#b8bb26");
        gruvboxHard.text = Color.parseColor("#ebdbb2");
        gruvboxHard.textMuted = Color.parseColor("#928374");
        gruvboxHard.success = Color.parseColor("#b8bb26");
        gruvboxHard.warning = Color.parseColor("#fabd2f");
        gruvboxHard.error = Color.parseColor("#fb4934");
        gruvboxHard.info = Color.parseColor("#83a598");
        gruvboxHard.border = Color.parseColor("#504945");
        gruvboxHard.cursor = Color.parseColor("#fabd2f");
        gruvboxHard.selection = Color.parseColor("#33fabd2f");
        gruvboxHard.borderRadius = 4;
        gruvboxHard.borderWidth = 1;
        gruvboxHard.useShadows = true;
        presets.put(Preset.GRUVBOX_HARD, gruvboxHard);

        // CATPPUCCIN_LATTE (Light Mode)
        DesignTokens catppuccinLatte = new DesignTokens();
        catppuccinLatte.background = Color.parseColor("#E6eff1f5");
        catppuccinLatte.surface = Color.parseColor("#e6e9ef");
        catppuccinLatte.primary = Color.parseColor("#1e66f5");
        catppuccinLatte.secondary = Color.parseColor("#8839ef");
        catppuccinLatte.accent = Color.parseColor("#ea76cb");
        catppuccinLatte.text = Color.parseColor("#4c4f69");
        catppuccinLatte.textMuted = Color.parseColor("#9ca0b0");
        catppuccinLatte.success = Color.parseColor("#40a02b");
        catppuccinLatte.warning = Color.parseColor("#df8e1d");
        catppuccinLatte.error = Color.parseColor("#d20f39");
        catppuccinLatte.info = Color.parseColor("#04a5e5");
        catppuccinLatte.border = Color.parseColor("#ccd0da");
        catppuccinLatte.cursor = Color.parseColor("#ea76cb");
        catppuccinLatte.selection = Color.parseColor("#331e66f5");
        catppuccinLatte.borderRadius = 6;
        catppuccinLatte.borderWidth = 1;
        catppuccinLatte.useShadows = true;
        presets.put(Preset.CATPPUCCIN_LATTE, catppuccinLatte);
    }

    public static DesignTokens getPreset(Preset preset) {
        DesignTokens source = presets.get(preset);
        if (source == null) source = presets.get(Preset.CLASSIC_TERMINAL);
        DesignTokens tokens = copy(source);
        
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
            int defaultBgColor = Color.parseColor(Theme.bg_color.defaultValue());
            if (xmlBgColor != defaultBgColor && xmlBgColor != 0) {
                tokens.background = xmlBgColor;
            }
        } catch (Exception ignored) {}

        return tokens;
    }

    private static DesignTokens copy(DesignTokens source) {
        DesignTokens tokens = new DesignTokens();
        tokens.background = source.background;
        tokens.surface = source.surface;
        tokens.primary = source.primary;
        tokens.secondary = source.secondary;
        tokens.accent = source.accent;
        tokens.text = source.text;
        tokens.textMuted = source.textMuted;
        tokens.success = source.success;
        tokens.warning = source.warning;
        tokens.error = source.error;
        tokens.info = source.info;
        tokens.border = source.border;
        tokens.cursor = source.cursor;
        tokens.selection = source.selection;
        tokens.borderRadius = source.borderRadius;
        tokens.borderWidth = source.borderWidth;
        tokens.useShadows = source.useShadows;
        return tokens;
    }
}
