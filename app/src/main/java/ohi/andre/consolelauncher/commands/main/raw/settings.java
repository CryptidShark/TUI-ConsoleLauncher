package ohi.andre.consolelauncher.commands.main.raw;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class settings implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        
        if (pack.args.length > 0 && pack.args[0].toString().equalsIgnoreCase("live")) {
            return showLiveCustomizer(info);
        }

        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_settings, null);

        TextView title = card.findViewById(R.id.settings_title);
        title.setText("SETTINGS");
        title.setTypeface(Tuils.getTypeface(info.context));

        Button btnEn = card.findViewById(R.id.btn_lang_en);
        Button btnEs = card.findViewById(R.id.btn_lang_es);
        Button btnClassic = card.findViewById(R.id.btn_theme_classic);
        Button btnModern = card.findViewById(R.id.btn_theme_modern);
        Button btnCyber = card.findViewById(R.id.btn_theme_cyber);
        Button btnNeo = card.findViewById(R.id.btn_theme_neo);

        btnEn.setOnClickListener(v -> {
            Behavior.language.parent().write(Behavior.language, "en");
            ((Reloadable) info.context).reload();
        });

        btnEs.setOnClickListener(v -> {
            Behavior.language.parent().write(Behavior.language, "es");
            ((Reloadable) info.context).reload();
        });

        btnClassic.setOnClickListener(v -> {
            Behavior.theme_preset.parent().write(Behavior.theme_preset, "CLASSIC_TERMINAL");
            ((Reloadable) info.context).reload();
        });

        btnModern.setOnClickListener(v -> {
            Behavior.theme_preset.parent().write(Behavior.theme_preset, "MODERN_TERMINAL");
            ((Reloadable) info.context).reload();
        });

        btnCyber.setOnClickListener(v -> {
            Behavior.theme_preset.parent().write(Behavior.theme_preset, "CYBER_TERMINAL");
            ((Reloadable) info.context).reload();
        });

        if (btnNeo != null) {
            btnNeo.setOnClickListener(v -> {
                Behavior.theme_preset.parent().write(Behavior.theme_preset, "NEO_TERMINAL");
                ((Reloadable) info.context).reload();
            });
        }

        Tuils.sendOutput(info.context, card);
        return null;
    }

    private String showLiveCustomizer(MainPack info) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_customizer, null);

        TextView title = card.findViewById(R.id.customizer_title);
        if (XMLPrefsManager.get(Behavior.language).equals("es")) {
            title.setText("PERSONALIZACIÓN EN VIVO");
        }
        title.setTypeface(Tuils.getTypeface(info.context));

        final TextView tvSize = card.findViewById(R.id.tv_font_size);
        int currentSize = XMLPrefsManager.getInt(Ui.input_output_size);
        tvSize.setText(String.valueOf(currentSize));

        card.findViewById(R.id.btn_font_plus).setOnClickListener(v -> {
            int newSize = XMLPrefsManager.getInt(Ui.input_output_size) + 1;
            Ui.input_output_size.parent().write(Ui.input_output_size, String.valueOf(newSize));
            tvSize.setText(String.valueOf(newSize));
            ((Reloadable) info.context).reload();
        });

        card.findViewById(R.id.btn_font_minus).setOnClickListener(v -> {
            int newSize = Math.max(8, XMLPrefsManager.getInt(Ui.input_output_size) - 1);
            Ui.input_output_size.parent().write(Ui.input_output_size, String.valueOf(newSize));
            tvSize.setText(String.valueOf(newSize));
            ((Reloadable) info.context).reload();
        });

        setupPinButton(card, R.id.btn_pin_system, Behavior.persistent_system_card, info);
        setupPinButton(card, R.id.btn_pin_battery, Behavior.persistent_battery_card, info);
        setupPinButton(card, R.id.btn_pin_shortcuts, Behavior.persistent_shortcuts_card, info);

        card.findViewById(R.id.btn_toggle_wallpaper).setOnClickListener(v -> {
            boolean current = XMLPrefsManager.getBoolean(Ui.system_wallpaper);
            Ui.system_wallpaper.parent().write(Ui.system_wallpaper, String.valueOf(!current));
            ((Reloadable) info.context).reload();
        });
        setupPinButton(card, R.id.btn_pin_shortcuts, Behavior.persistent_shortcuts_card, info);

        card.findViewById(R.id.btn_clear_persistent).setOnClickListener(v -> {
            MainPack pack = info;
            pack.set(new String[]{"clear"});
            try {
                new visual().exec(pack);
            } catch (Exception ignore) {}
            ((Reloadable) info.context).reload();
        });

        card.findViewById(R.id.btn_reset_customizer).setOnClickListener(v -> {
            Ui.input_output_size.parent().write(Ui.input_output_size, Ui.input_output_size.defaultValue());
            Theme.input_color.parent().write(Theme.input_color, Theme.input_color.defaultValue());
            Behavior.persistent_battery_card.parent().write(Behavior.persistent_battery_card, Behavior.persistent_battery_card.defaultValue());
            Behavior.persistent_notes_card.parent().write(Behavior.persistent_notes_card, Behavior.persistent_notes_card.defaultValue());
            Behavior.persistent_shortcuts_card.parent().write(Behavior.persistent_shortcuts_card, Behavior.persistent_shortcuts_card.defaultValue());
            ((Reloadable) info.context).reload();
        });

        setupColorButton(card, R.id.color_cyan, "#00FFFF", info);
        setupColorButton(card, R.id.color_magenta, "#FF00FF", info);
        setupColorButton(card, R.id.color_yellow, "#FFFF00", info);
        setupColorButton(card, R.id.color_green, "#00FF00", info);

        Tuils.sendOutput(info.context, card);
        return null;
    }

    private void setupColorButton(View card, int viewId, String colorHex, MainPack info) {
        card.findViewById(viewId).setOnClickListener(v -> {
            Theme.input_color.parent().write(Theme.input_color, colorHex);
            ((Reloadable) info.context).reload();
        });
    }

    private void setupPinButton(View card, int viewId, XMLPrefsSave pref, MainPack info) {
        card.findViewById(viewId).setOnClickListener(v -> {
            boolean current = XMLPrefsManager.getBoolean(pref);
            pref.parent().write(pref, String.valueOf(!current));
            ((Reloadable) info.context).reload();
        });
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public int helpRes() {
        return R.string.help_settings;
    }

    @Override
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
