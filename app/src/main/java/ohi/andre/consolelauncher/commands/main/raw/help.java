package ohi.andre.consolelauncher.commands.main.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.tuils.Tuils;

public class help implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        CommandAbstraction cmd = info.get(CommandAbstraction.class);
        int res = cmd == null ? R.string.output_commandnotfound : cmd.helpRes();
        return "Priority: " + info.cmdPrefs.getPriority(cmd) + Tuils.NEWLINE + info.res.getString(res);
    }

    @Override
    public int helpRes() {
        return R.string.help_help;
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.COMMAND};
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        MainPack info = (MainPack) pack;
        StringBuilder builder = new StringBuilder();
        builder.append("AVAILABLE COMMANDS").append(Tuils.NEWLINE).append(Tuils.NEWLINE);

        String[] names = info.commandGroup.getCommandNames();
        List<String> toPrint = new ArrayList<>(Arrays.asList(names));
        Collections.sort(toPrint, Tuils::alphabeticCompare);

        for (String name : toPrint) {
            try {
                CommandAbstraction cmd = info.commandGroup.getCommandByName(name);
                String desc = "";
                if (cmd != null && cmd.helpRes() != 0) {
                    desc = info.res.getString(cmd.helpRes()).split("\n")[0];
                }
                
                builder.append(String.format("%-12s %s", name, desc)).append(Tuils.NEWLINE);
            } catch (Exception e) {}
        }

        return builder.toString();
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int index) {
        MainPack info = (MainPack) pack;
        return info.res.getString(R.string.output_commandnotfound);
    }

}
