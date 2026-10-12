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
}
