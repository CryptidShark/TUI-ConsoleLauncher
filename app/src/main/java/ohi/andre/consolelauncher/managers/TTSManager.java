package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TTSManager {
    private static TextToSpeech tts;

    public static void init(Context context) {
        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        tts.setLanguage(Locale.getDefault());
                    } catch (Exception ignore) {}
                }
            });
        }
    }

    public static void speak(Context context, String text) {
        init(context);
        if (tts != null) {
            try {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TUI_TTS_ID");
            } catch (Exception ignore) {}
        }
    }

    public static void stop() {
        if (tts != null) {
            try {
                tts.stop();
            } catch (Exception ignore) {}
        }
    }
}
