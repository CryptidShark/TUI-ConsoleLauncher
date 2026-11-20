package ohi.andre.consolelauncher.commands.main.raw;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.WorkspaceManager;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class settings implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;
        
        if (pack.args.length > 0 && pack.args[0].toString().equalsIgnoreCase("live")) {
            return showLiveCustomizer(info);
        }

        if (pack.args.length > 0 && pack.args[0].toString().equalsIgnoreCase("gestures")) {
            return showGesturesCard(info);
        }

        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_settings, null);
        boolean spanish = "es".equalsIgnoreCase(XMLPrefsManager.get(Behavior.language));

        TextView title = card.findViewById(R.id.settings_title);
        title.setText(spanish ? "CYTHERNOIR / AJUSTES" : "CYTHERNOIR / SETTINGS");
        title.setTypeface(Tuils.getTypeface(info.context));
        ((TextView) card.findViewById(R.id.settings_themes_label)).setText(spanish ? "SISTEMAS DE COLOR" : "COLOR SYSTEMS");
        ((TextView) card.findViewById(R.id.settings_workspaces_label)).setText(spanish ? "PERFILES DE PANTALLA" : "WORKSPACE PROFILES");

        Button btnEn = card.findViewById(R.id.btn_lang_en);
        Button btnEs = card.findViewById(R.id.btn_lang_es);

        btnEn.setOnClickListener(v -> {
            Behavior.language.parent().write(Behavior.language, "en");
            ((Reloadable) info.context).reload();
        });

        btnEs.setOnClickListener(v -> {
            Behavior.language.parent().write(Behavior.language, "es");
            ((Reloadable) info.context).reload();
        });

        LinearLayout themeGrid = card.findViewById(R.id.settings_theme_grid);
        ThemeEngine.Preset[] presets = ThemeEngine.Preset.values();
        String currentPreset = XMLPrefsManager.get(Behavior.theme_preset);
        for (int i = 0; i < presets.length; i += 3) {
            LinearLayout row = new LinearLayout(info.context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            themeGrid.addView(row, new LinearLayout.LayoutParams(-1, -2));
            for (int j = i; j < Math.min(i + 3, presets.length); j++) {
                ThemeEngine.Preset preset = presets[j];
                ThemeEngine.DesignTokens tokens = ThemeEngine.getBasePreset(preset);
                String label = preset.name().replace("_TERMINAL", "").replace("_CYBER", "").replace('_', ' ');
                Button button = new Button(info.context);
                button.setAllCaps(false);
                button.setText(preset.name().equals(currentPreset) ? "✓ " + label : label);
                button.setTextSize(10);
                button.setTextColor(tokens.text);
                button.setTypeface(Tuils.getTypeface(info.context));
                button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tokens.surface));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Tuils.dpToPx(info.context, 42), 1f);
                params.setMargins(Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2));
                row.addView(button, params);
                button.setOnClickListener(v -> {
                    ThemeEngine.applyPreset(preset);
                    ((Reloadable) info.context).reload();
                });
            }
        }

        LinearLayout workspaceGrid = card.findViewById(R.id.settings_workspace_grid);
        WorkspaceManager.Workspace[] workspaces = WorkspaceManager.Workspace.values();
        WorkspaceManager.Workspace activeWorkspace = WorkspaceManager.getActiveWorkspace(info.context);
        for (int i = 0; i < workspaces.length; i += 3) {
            LinearLayout row = new LinearLayout(info.context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            workspaceGrid.addView(row, new LinearLayout.LayoutParams(-1, -2));
            for (int j = i; j < Math.min(i + 3, workspaces.length); j++) {
                WorkspaceManager.Workspace workspace = workspaces[j];
                Button button = new Button(info.context);
                button.setAllCaps(false);
                String workspaceName = workspace.getName();
                if (spanish) {
                    if (workspace == WorkspaceManager.Workspace.DEVELOPER) workspaceName = "Desarrollo";
                    else if (workspace == WorkspaceManager.Workspace.PRIVACY) workspaceName = "Privacidad";
                    else if (workspace == WorkspaceManager.Workspace.CYBER) workspaceName = "Ciber";
                    else if (workspace == WorkspaceManager.Workspace.GAMING) workspaceName = "Juegos";
                    else if (workspace == WorkspaceManager.Workspace.WEB_DEV) workspaceName = "Web Dev";
                    else if (workspace == WorkspaceManager.Workspace.AUTHORIZED_LAB) workspaceName = "Lab autorizado";
                    else if (workspace == WorkspaceManager.Workspace.CUSTOM) workspaceName = "Personalizado";
                }
                button.setText(workspace == activeWorkspace ? "✓ " + workspaceName : workspaceName);
                button.setTextSize(10);
                button.setTypeface(Tuils.getTypeface(info.context));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Tuils.dpToPx(info.context, 42), 1f);
                params.setMargins(Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2), Tuils.dpToPx(info.context, 2));
                row.addView(button, params);
                button.setOnClickListener(v -> {
                    WorkspaceManager.setWorkspace(info.context, workspace);
                    ((Reloadable) info.context).reload();
                });
            }
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

    private String showGesturesCard(MainPack info) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_gestures, null);

        TextView title = card.findViewById(R.id.gestures_title);
        title.setTypeface(Tuils.getTypeface(info.context));

        TextView tvSwipeUp = card.findViewById(R.id.tv_swipe_up_action);
        TextView tvSwipeDown = card.findViewById(R.id.tv_swipe_down_action);
        TextView tvSwipeLeft = card.findViewById(R.id.tv_swipe_left_action);
        TextView tvSwipeRight = card.findViewById(R.id.tv_swipe_right_action);
        TextView tvDoubleTap = card.findViewById(R.id.tv_double_tap_action);
        TextView tvLongPress = card.findViewById(R.id.tv_long_press_action);

        String swipeUp = XMLPrefsManager.get(Behavior.swipe_up_cmd);
        String swipeDown = XMLPrefsManager.get(Behavior.swipe_down_cmd);
        String swipeLeft = XMLPrefsManager.get(Behavior.swipe_left_cmd);
        String swipeRight = XMLPrefsManager.get(Behavior.swipe_right_cmd);
        String doubleTap = XMLPrefsManager.get(Behavior.double_tap_cmd);
        boolean doubleTapLock = XMLPrefsManager.getBoolean(Behavior.double_tap_lock);

        tvSwipeUp.setText("Action: " + (swipeUp != null && !swipeUp.trim().isEmpty() ? swipeUp : "None"));
        tvSwipeDown.setText("Action: " + (swipeDown != null && !swipeDown.trim().isEmpty() ? swipeDown : "None"));
        if (tvSwipeLeft != null) tvSwipeLeft.setText("Action: " + (swipeLeft != null && !swipeLeft.trim().isEmpty() ? swipeLeft : "None"));
        if (tvSwipeRight != null) tvSwipeRight.setText("Action: " + (swipeRight != null && !swipeRight.trim().isEmpty() ? swipeRight : "None"));
        tvDoubleTap.setText("Action: " + (doubleTapLock ? "Lock Device (Screen Lock)" : (doubleTap != null && !doubleTap.isEmpty() ? doubleTap : "None")));
        tvLongPress.setText("Action: Launcher Settings / Context Options");

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
