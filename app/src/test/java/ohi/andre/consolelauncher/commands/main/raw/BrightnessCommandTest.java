package ohi.andre.consolelauncher.commands.main.raw;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrightnessCommandTest {

    @Test
    public void windowBrightnessUsesAndroidZeroToOneRange() {
        assertEquals(0f, brightness.toWindowBrightness(0), 0.001f);
        assertEquals(0.5f, brightness.toWindowBrightness(50), 0.001f);
        assertEquals(1f, brightness.toWindowBrightness(100), 0.001f);
    }

    @Test
    public void windowBrightnessClampsPercentInput() {
        assertEquals(0f, brightness.toWindowBrightness(-10), 0.001f);
        assertEquals(1f, brightness.toWindowBrightness(150), 0.001f);
    }
}
