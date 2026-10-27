package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.TTSManager;

public class tts implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        if (pack.args == null || pack.args.length == 0 || pack.args[0] == null) {
            return "Usage: tts <text> or tts -stop";
        }

        String text = pack.args[0].toString().trim();
        if (text.equalsIgnoreCase("-stop") || text.equalsIgnoreCase("stop")) {
            TTSManager.stop();
            return "TTS playback stopped.";
        }

        if (text.isEmpty()) {
            return "Usage: tts <text> or tts -stop";
        }

        TTSManager.speak(info.context, text);
        return "Speaking: " + text;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        return "Usage: tts <text>";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: tts <text>";
    }
}
