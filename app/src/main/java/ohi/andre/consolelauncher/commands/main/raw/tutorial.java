package ohi.andre.consolelauncher.commands.main.raw;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.tuils.Tuils;

public class tutorial implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        showStep(info, 0);
        return null;
    }

    private void showStep(MainPack info, int step) {
        String[] steps = info.res.getStringArray(R.array.tutorial_steps);
        
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_tutorial, null);

        TextView title = card.findViewById(R.id.tutorial_title);
        TextView content = card.findViewById(R.id.tutorial_step);
        Button btnNext = card.findViewById(R.id.btn_tutorial_next);

        title.setTypeface(Tuils.getTypeface(info.context));
        content.setTypeface(Tuils.getTypeface(info.context));
        content.setText(steps[step]);

        if (step >= steps.length - 1) {
            btnNext.setText(info.context.getString(android.R.string.ok));
        }

        btnNext.setOnClickListener(v -> {
            if (step < steps.length - 1) {
                showStep(info, step + 1);
            } else {
                Tuils.sendOutput(info.context, "Tutorial completed!");
            }
        });

        Tuils.sendOutput(info.context, card);
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public int helpRes() {
        return R.string.help_tutorial;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
