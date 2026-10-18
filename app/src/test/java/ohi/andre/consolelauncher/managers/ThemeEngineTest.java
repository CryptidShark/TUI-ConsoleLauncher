package ohi.andre.consolelauncher.managers;

import android.graphics.Color;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ThemeEngineTest {

    @Test
    public void testWatchdogsCyberPreset() {
        ThemeEngine.DesignTokens tokens = ThemeEngine.getPreset(ThemeEngine.Preset.WATCHDOGS_CYBER);
        assertNotNull(tokens);
        assertEquals(2.0f, tokens.borderRadius, 0.01f);
        assertEquals(1.0f, tokens.borderWidth, 0.01f);
    }

    @Test
    public void windowsTerminalPresetUsesModernPalette() {
        ThemeEngine.DesignTokens tokens = ThemeEngine.getPreset(ThemeEngine.Preset.WINDOWS_TERMINAL);
        assertNotNull(tokens);
        assertEquals(Color.parseColor("#FF101214"), tokens.background);
        assertEquals(Color.parseColor("#FF4FC1FF"), tokens.primary);
        assertEquals(10.0f, tokens.borderRadius, 0.01f);
    }

    @Test
    public void returnedTokensDoNotMutateSharedPreset() {
        ThemeEngine.DesignTokens first = ThemeEngine.getPreset(ThemeEngine.Preset.WINDOWS_TERMINAL);
        first.primary = Color.RED;

        ThemeEngine.DesignTokens second = ThemeEngine.getPreset(ThemeEngine.Preset.WINDOWS_TERMINAL);
        assertEquals(Color.parseColor("#FF4FC1FF"), second.primary);
    }
}
