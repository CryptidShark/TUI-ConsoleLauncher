package ohi.andre.consolelauncher;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.raw.visual;
import ohi.andre.consolelauncher.commands.tuixt.TuixtActivity;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.RegexManager;
import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.TimeManager;
import ohi.andre.consolelauncher.managers.TuiLocationManager;
import ohi.andre.consolelauncher.managers.notifications.KeeperService;
import ohi.andre.consolelauncher.managers.notifications.NotificationManager;
import ohi.andre.consolelauncher.managers.notifications.NotificationMonitorService;
import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.suggestions.SuggestionsManager;
import ohi.andre.consolelauncher.managers.TuiWidgetManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Assist;
import ohi.andre.consolelauncher.tuils.BusyBoxInstaller;
import ohi.andre.consolelauncher.tuils.CustomExceptionHandler;
import ohi.andre.consolelauncher.tuils.LongClickableSpan;
import ohi.andre.consolelauncher.tuils.PrivateIOReceiver;
import ohi.andre.consolelauncher.tuils.PublicIOReceiver;
import ohi.andre.consolelauncher.tuils.SimpleMutableEntry;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.Inputable;
import ohi.andre.consolelauncher.tuils.interfaces.Outputable;
import ohi.andre.consolelauncher.tuils.interfaces.Reloadable;

public class LauncherActivity extends AppCompatActivity implements Reloadable {

    public static final int COMMAND_REQUEST_PERMISSION = 10;
    public static final int STARTING_PERMISSION = 11;
    public static final int COMMAND_SUGGESTION_REQUEST_PERMISSION = 12;
    public static final int LOCATION_REQUEST_PERMISSION = 13;

    public static final int TUIXT_REQUEST = 10;
    public static final int STORAGE_MANAGER_REQUEST = 1000;

    private UIManager ui;
    private MainManager main;
    private TuiWidgetManager widgetManager;
    private ViewGroup persistentContainer;

    private PrivateIOReceiver privateIOReceiver;
    private PublicIOReceiver publicIOReceiver;

    private boolean openKeyboardOnStart, canApplyTheme, backButtonEnabled;

    private Set<ReloadMessageCategory> categories;
    private Runnable stopActivity = () -> {
            dispose();
            finish();

            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);

            CharSequence reloadMessage = Tuils.EMPTYSTRING;
            for (ReloadMessageCategory c : categories) {
                reloadMessage = TextUtils.concat(reloadMessage, Tuils.NEWLINE, c.text());
            }
            startMain.putExtra(Reloadable.MESSAGE, reloadMessage);

            startActivity(startMain);
    };

    private Inputable in = new Inputable() {

        @Override
        public void in(String s) {
            if(ui != null) ui.setInput(s);
        }

        @Override
        public void changeHint(final String s) {
            runOnUiThread(() -> ui.setHint(s));
        }

        @Override
        public void resetHint() {
            runOnUiThread(() -> ui.resetHint());
        }
    };

    private Outputable out = new Outputable() {

        private final int DELAY = 500;

        Queue<SimpleMutableEntry<CharSequence,Integer>> textColor = new LinkedList<>();
        Queue<SimpleMutableEntry<CharSequence,Integer>> textCategory = new LinkedList<>();

        boolean charged = false;
        Handler handler = new Handler();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(ui == null) {
                    handler.postDelayed(this, DELAY);
                    return;
                }

                SimpleMutableEntry<CharSequence,Integer> sm;
                while ((sm = textCategory.poll()) != null) {
                    ui.setOutput(sm.getKey(), sm.getValue());
                }

                while ((sm = textColor.poll()) != null) {
                    ui.setOutput(sm.getValue(), sm.getKey());
                }

                textCategory = null;
                textColor = null;
                handler = null;
                r = null;
            }
        };

        @Override
        public void onOutput(CharSequence output) {
            if(ui != null) ui.setOutput(output, TerminalManager.CATEGORY_OUTPUT);
            else {
                textCategory.add(new SimpleMutableEntry<>(output, TerminalManager.CATEGORY_OUTPUT));

                if(!charged) {
                    charged = true;
                    handler.postDelayed(r, DELAY);
                }
            }
        }

        @Override
        public void onOutput(CharSequence output, int category) {
            if(ui != null) ui.setOutput(output, category);
            else {
                textCategory.add(new SimpleMutableEntry<>(output, category));

                if(!charged) {
                    charged = true;
                    handler.postDelayed(r, DELAY);
                }
            }
        }

        @Override
        public void onOutput(int color, CharSequence output) {
            if(ui != null) ui.setOutput(color, output);
            else {
                textColor.add(new SimpleMutableEntry<>(output, color));

                if(!charged) {
                    charged = true;
                    handler.postDelayed(r, DELAY);
                }
            }
        }

        @Override
        public void onOutput(View view) {
            if(ui != null) ui.setOutput(view);
        }

        @Override
        public void dispose() {
            if(handler != null) handler.removeCallbacksAndMessages(null);
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        XMLPrefsManager.loadCommons(newBase);
        String lang = XMLPrefsManager.get(Behavior.language);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            XMLPrefsManager.loadCommons(this);
        } catch (Exception e) {
            Tuils.toFile(e);
        }

        boolean fullscreen = XMLPrefsManager.getBoolean(Ui.fullscreen);
        if(fullscreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        boolean useSystemWP = XMLPrefsManager.getBoolean(Ui.system_wallpaper);
        if (useSystemWP) {
            setTheme(R.style.Custom_SystemWP);
        } else {
            setTheme(R.style.Custom_Solid);
        }

        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
        } else {
            overridePendingTransition(0, 0);
        }

        if (isFinishing()) {
            return;
        }

        List<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 13+ (API 33+) doesn't use READ/WRITE_EXTERNAL_STORAGE for general files
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }

        // Special check for MANAGE_EXTERNAL_STORAGE (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, STORAGE_MANAGER_REQUEST);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, STORAGE_MANAGER_REQUEST);
                }
                Toast.makeText(this, "Please grant storage permissions to T-UI", Toast.LENGTH_LONG).show();
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), LauncherActivity.STARTING_PERMISSION);
        } else {
            canApplyTheme = true;
            finishOnCreate(savedInstanceState);
        }
    }

    private void finishOnCreate(Bundle savedInstanceState) {

        Thread.currentThread().setUncaughtExceptionHandler(new CustomExceptionHandler());

        XMLPrefsManager.loadCommons(this);
        new RegexManager(LauncherActivity.this);
        new TimeManager(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PrivateIOReceiver.ACTION_INPUT);
        filter.addAction(PrivateIOReceiver.ACTION_OUTPUT);
        filter.addAction(PrivateIOReceiver.ACTION_REPLY);

        privateIOReceiver = new PrivateIOReceiver(this, out, in);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(privateIOReceiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(PublicIOReceiver.ACTION_CMD);
        filter1.addAction(PublicIOReceiver.ACTION_OUTPUT);

        publicIOReceiver = new PublicIOReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplicationContext().registerReceiver(publicIOReceiver, filter1, "ohi.andre.consolelauncher.permission.RECEIVE_CMD", null, Context.RECEIVER_EXPORTED);
        } else {
            getApplicationContext().registerReceiver(publicIOReceiver, filter1, "ohi.andre.consolelauncher.permission.RECEIVE_CMD", null);
        }

        int requestedOrientation = XMLPrefsManager.getInt(Behavior.orientation);
        if(requestedOrientation >= 0 && requestedOrientation != 2) {
            int orientation = getResources().getConfiguration().orientation;
            if(orientation != requestedOrientation) setRequestedOrientation(requestedOrientation);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !XMLPrefsManager.getBoolean(Ui.ignore_bar_color)) {
            Window window = getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(XMLPrefsManager.getColor(Theme.statusbar_color));
            window.setNavigationBarColor(XMLPrefsManager.getColor(Theme.navigationbar_color));
        }

        backButtonEnabled = XMLPrefsManager.getBoolean(Behavior.back_button_enabled);

        boolean showNotification = XMLPrefsManager.getBoolean(Behavior.tui_notification);
        Intent keeperIntent = new Intent(this, KeeperService.class);
        if (showNotification) {
            keeperIntent.putExtra(KeeperService.PATH_KEY, XMLPrefsManager.get(Behavior.home_path));
            startService(keeperIntent);
        } else {
            try {
                stopService(keeperIntent);
            } catch (Exception e) {}
        }

        try {
            NotificationManager.create(this);
        } catch (Exception e) {
            Tuils.toFile(e);
        }

        boolean notifications = XMLPrefsManager.getBoolean(Notifications.show_notifications) || XMLPrefsManager.get(Notifications.show_notifications).equalsIgnoreCase("enabled");
        if(notifications) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                try {
                    ComponentName notificationComponent = new ComponentName(this, NotificationService.class);
                    PackageManager pm = getPackageManager();
                    pm.setComponentEnabledSetting(notificationComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    if (!Tuils.hasNotificationAccess(this)) {
                        Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        if (i.resolveActivity(getPackageManager()) == null) {
                            Toast.makeText(this, R.string.no_notification_access, Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(i);
                        }
                    }

                    Intent monitor = new Intent(this, NotificationMonitorService.class);
                    startService(monitor);

                    Intent notificationIntent = new Intent(this, NotificationService.class);
                    startService(notificationIntent);
                } catch (NoClassDefFoundError er) {
                    Intent intent = new Intent(PrivateIOReceiver.ACTION_OUTPUT);
                    intent.putExtra(PrivateIOReceiver.TEXT, getString(R.string.output_notification_error) + Tuils.SPACE + er.toString());
                }
            } else {
                Tuils.sendOutput(Color.RED, this, R.string.notification_low_api);
            }
        }

        LongClickableSpan.longPressVibrateDuration = XMLPrefsManager.getInt(Behavior.long_click_vibration_duration);

        openKeyboardOnStart = XMLPrefsManager.getBoolean(Behavior.auto_show_keyboard);
        if (!openKeyboardOnStart) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        setContentView(R.layout.base_view);

        if(XMLPrefsManager.getBoolean(Ui.show_restart_message)) {
            CharSequence s = getIntent().getCharSequenceExtra(Reloadable.MESSAGE);
            if(s != null) out.onOutput(Tuils.span(s, XMLPrefsManager.getColor(Theme.restart_message_color)));
        }

        categories = new HashSet<>();

        main = new MainManager(this);

        ViewGroup mainView = (ViewGroup) findViewById(R.id.mainview);
        persistentContainer = (ViewGroup) findViewById(R.id.persistent_container);

        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !XMLPrefsManager.getBoolean(Ui.ignore_bar_color) && !XMLPrefsManager.getBoolean(Ui.statusbar_light_icons)) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), mainView);
            controller.setAppearanceLightStatusBars(true);
        }

        ui = new UIManager(this, mainView, main.getMainPack(), canApplyTheme, main.executer());
        widgetManager = new TuiWidgetManager(this);
        widgetManager.restoreState(savedInstanceState);
        main.setWidgetManager(widgetManager);

        main.setRedirectionListener(ui.buildRedirectionListener());
        ui.pack = main.getMainPack();

        in.in(Tuils.EMPTYSTRING);
        ui.focusTerminal();

        if(XMLPrefsManager.getBoolean(Ui.fullscreen)) Assist.assistActivity(this);

        if (XMLPrefsManager.getBoolean(Behavior.persistent_system_card)) {
            loadFixedVisual("system");
        }

        if (XMLPrefsManager.getBoolean(Behavior.persistent_battery_card)) {
            loadFixedVisual("battery");
        }

        if (XMLPrefsManager.getBoolean(Behavior.persistent_notes_card)) {
            loadFixedVisual("notes");
        }

        if (XMLPrefsManager.getBoolean(Behavior.persistent_shortcuts_card)) {
            loadFixedVisual("shortcuts");
        }

        if (XMLPrefsManager.getBoolean(Behavior.persistent_music_card)) {
            loadFixedVisual("music");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        });

        if (XMLPrefsManager.getBoolean(Ui.fullscreen)) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), mainView);
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        System.gc();
    }

    private void loadFixedVisual(String type) {
        MainPack pack = main.getMainPack();
        pack.set(new String[]{type, "-fixed"});
        try {
            new visual().exec(pack);
        } catch (Exception ignore) {}
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ui != null) ui.onStart(openKeyboardOnStart);
        if (widgetManager != null) widgetManager.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (widgetManager != null) widgetManager.stopListening();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (widgetManager != null) widgetManager.saveState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(new Intent(UIManager.ACTION_UPDATE_SUGGESTIONS));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ui != null && main != null) {
            ui.pause();
            main.dispose();
        }
    }

    private boolean disposed = false;
    private void dispose() {
        if(disposed) return;

        try {
            LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(privateIOReceiver);
            getApplicationContext().unregisterReceiver(publicIOReceiver);
        } catch (Exception e) {}

        try {
            stopService(new Intent(this, NotificationMonitorService.class));
        } catch (NoClassDefFoundError | Exception e) {
            Tuils.log(e);
        }

        try {
            stopService(new Intent(this, KeeperService.class));
        } catch (NoClassDefFoundError | Exception e) {
            Tuils.log(e);
        }

        try {
            Intent notificationIntent = new Intent(this, NotificationService.class);
            notificationIntent.putExtra(NotificationService.DESTROY, true);
            startService(notificationIntent);
        } catch (Throwable e) {
            Tuils.log(e);
        }

        overridePendingTransition(0,0);

        if(main != null) main.destroy();
        if(ui != null) ui.dispose();

        XMLPrefsManager.dispose();
        RegexManager.instance.dispose();
        TimeManager.instance.dispose();

        disposed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dispose();
    }

    @Override
    public void onBackPressed() {
        if (backButtonEnabled && main != null) {
            if (ui != null) {
                String currentInput = ui.getInput();
                if (currentInput != null && !currentInput.isEmpty()) {
                    ui.setInput(Tuils.EMPTYSTRING);
                } else {
                    ui.onBackPressed();
                }
            }
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK)
            return super.onKeyLongPress(keyCode, event);

        if (main != null)
            main.onLongBack();
        return true;
    }

    @Override
    public void reload() {
        runOnUiThread(stopActivity);
    }

    @Override
    public void addMessage(String header, String message) {
        for(ReloadMessageCategory cs : categories) {
            Tuils.log(cs.header, header);
            if(cs.header.equals(header)) {
                cs.lines.add(message);
                return;
            }
        }

        ReloadMessageCategory c = new ReloadMessageCategory(header);
        if(message != null) c.lines.add(message);
        categories.add(c);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && ui != null) {
            ui.focusTerminal();
        }
    }

    SuggestionsManager.Suggestion suggestion;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        suggestion = (SuggestionsManager.Suggestion) v.getTag(R.id.suggestion_id);

        if(suggestion.type == SuggestionsManager.Suggestion.TYPE_CONTACT) {
            ContactManager.Contact contact = (ContactManager.Contact) suggestion.object;

            menu.setHeaderTitle(contact.name);
            for(int count = 0; count < contact.numbers.size(); count++) {
                menu.add(0, count, count, contact.numbers.get(count));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(suggestion != null) {
            if(suggestion.type == SuggestionsManager.Suggestion.TYPE_CONTACT) {
                ContactManager.Contact contact = (ContactManager.Contact) suggestion.object;
                contact.setSelectedNumber(item.getItemId());

                Tuils.sendInput(this, suggestion.getText());

                return true;
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TuiWidgetManager.REQUEST_PICK_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                Tuils.sendOutput(this, "Widget selected, configuring...");
                widgetManager.configureWidget(this, data);
            } else {
                Tuils.sendOutput(this, "Widget pick cancelled (code: " + resultCode + ")");
                if (data != null) {
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    if (appWidgetId != -1) {
                        widgetManager.deleteWidgetId(appWidgetId);
                    }
                }
            }
        } else if (requestCode == TuiWidgetManager.REQUEST_CREATE_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                Tuils.sendOutput(this, "Widget configuration success.");
                widgetManager.createWidgetFromIntent(this, data);
            } else {
                Tuils.sendOutput(this, "Widget configuration failed or cancelled.");
            }
        } else if (requestCode == TuiWidgetManager.REQUEST_BIND_APPWIDGET) {
            if (resultCode == RESULT_OK) {
                Tuils.sendOutput(this, "Widget bind success.");
                widgetManager.createWidgetFromIntent(this, data);
            } else {
                // Final strategy for Samsung/Modern Android: 
                // If bind fails but user says they have permission, try to create anyway
                Tuils.sendOutput(this, "Widget bind returned " + resultCode + ". Attempting direct creation...");
                widgetManager.createWidgetFromIntent(this, data);
            }
        }

        if (requestCode == STORAGE_MANAGER_REQUEST) {
            // Re-init folder and reload
            Tuils.reinit(this);
            reload();
        }

        if(requestCode == TUIXT_REQUEST && resultCode != 0) {
            if(resultCode == TuixtActivity.BACK_PRESSED) {
                Tuils.sendOutput(this, R.string.tuixt_back_pressed);
            } else {
                Tuils.sendOutput(this, data.getStringExtra(TuixtActivity.ERROR_KEY));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(permissions.length > 0 && permissions[0].equals(Manifest.permission.READ_CONTACTS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(new Intent(ContactManager.ACTION_REFRESH));
        }

        try {
            switch (requestCode) {
                case COMMAND_REQUEST_PERMISSION:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        MainPack info = main.getMainPack();
                        main.onCommand(info.lastCommand, (String) null, false);
                    } else {
                        ui.setOutput(getString(R.string.output_nopermissions), TerminalManager.CATEGORY_OUTPUT);
                        main.sendPermissionNotGrantedWarning();
                    }
                    break;
                case STARTING_PERMISSION:
                    int count = 0;
                    while(count < permissions.length && count < grantResults.length) {
                        if(grantResults[count] == PackageManager.PERMISSION_DENIED) {
                            Toast.makeText(this, R.string.permissions_toast, Toast.LENGTH_LONG).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();

                                    try {
                                        sleep(2000);
                                    } catch (InterruptedException e) {}

                                    runOnUiThread(stopActivity);
                                }
                            }.start();
                            return;
                        }
                        count++;
                    }
                    canApplyTheme = false;
                    finishOnCreate(null);
                    break;
                case COMMAND_SUGGESTION_REQUEST_PERMISSION:
                    if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        ui.setOutput(getString(R.string.output_nopermissions), TerminalManager.CATEGORY_OUTPUT);
                    }
                    break;
                case LOCATION_REQUEST_PERMISSION:
                    if (grantResults.length > 0) {
                        Intent i = new Intent(TuiLocationManager.ACTION_GOT_PERMISSION);
                        i.putExtra(XMLPrefsManager.VALUE_ATTRIBUTE, grantResults[0]);
                        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(i);
                    }
                    break;
            }
        } catch (Exception e) {}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String cmd = intent.getStringExtra(PrivateIOReceiver.TEXT);
        if(cmd != null) {
            Intent i = new Intent(MainManager.ACTION_EXEC);
            i.putExtra(MainManager.CMD_COUNT, MainManager.commandCount);
            i.putExtra(MainManager.CMD, cmd);
            i.putExtra(MainManager.NEED_WRITE_INPUT, true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onOutput(View view) {
        if (out != null) out.onOutput(view);
    }

    public UIManager getUIManager() {
        return ui;
    }

    public void onPersistentOutput(View view) {
        if (persistentContainer != null) {
            runOnUiThread(() -> {
                String presetName = XMLPrefsManager.get(Behavior.theme_preset);
                ThemeEngine.Preset preset;
                try {
                    preset = ThemeEngine.Preset.valueOf(presetName);
                } catch (Exception e) {
                    preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
                }
                ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);

                if (view instanceof AppWidgetHostView) {
                    AppWidgetHostView hostView = (AppWidgetHostView) view;
                    AppWidgetProviderInfo info = hostView.getAppWidgetInfo();
                    
                    LinearLayout wrapper = new LinearLayout(this);
                    wrapper.setTag("widget_container");
                    wrapper.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    wrapperParams.setMargins(0, Tuils.dpToPx(this, 6), 0, Tuils.dpToPx(this, 6));
                    wrapper.setLayoutParams(wrapperParams);

                    GradientDrawable cardBg = new GradientDrawable();
                    cardBg.setColor(theme.surface);
                    cardBg.setCornerRadius(Tuils.dpToPx(this, (int) theme.borderRadius > 0 ? (int) theme.borderRadius : 6));
                    cardBg.setStroke((int) Tuils.dpToPx(this, (int) theme.borderWidth > 0 ? (int) theme.borderWidth : 1), theme.border);
                    wrapper.setBackground(cardBg);

                    String titleText = "EXTERNAL_MODULE";
                    if (info != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            titleText = info.loadLabel(getPackageManager());
                        } else {
                            titleText = info.label;
                        }
                    }

                    TextView header = new TextView(this);
                    header.setText("● [ " + (titleText != null ? titleText.toUpperCase() : "EXTERNAL_MODULE") + " ]");
                    header.setTextColor(theme.primary);
                    header.setTextSize(11);
                    header.setTypeface(Tuils.getTypeface(this));
                    header.setPadding(Tuils.dpToPx(this, 12), Tuils.dpToPx(this, 8), Tuils.dpToPx(this, 12), Tuils.dpToPx(this, 6));
                    wrapper.addView(header);

                    View line = new View(this);
                    line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Tuils.dpToPx(this, 1)));
                    line.setBackgroundColor(theme.border);
                    line.setAlpha(0.35f);
                    wrapper.addView(line);

                    hostView.setPadding(0, 0, 0, 0);
                    LinearLayout.LayoutParams hostParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    hostParams.setMargins(Tuils.dpToPx(this, 4), Tuils.dpToPx(this, 4), Tuils.dpToPx(this, 4), Tuils.dpToPx(this, 4));
                    hostView.setLayoutParams(hostParams);
                    wrapper.addView(hostView);
                    
                    TextView footer = new TextView(this);
                    footer.setText("<< MODULE_ID: " + hostView.getAppWidgetId() + " // STATUS: ONLINE");
                    footer.setTextColor(theme.textMuted);
                    footer.setTextSize(9);
                    footer.setTypeface(Tuils.getTypeface(this));
                    footer.setPadding(Tuils.dpToPx(this, 12), Tuils.dpToPx(this, 2), Tuils.dpToPx(this, 12), Tuils.dpToPx(this, 8));
                    wrapper.addView(footer);

                    persistentContainer.addView(wrapper);
                    applyThemeRecursively(wrapper);
                } else {
                    persistentContainer.addView(view);
                    applyThemeRecursively(view);
                }
            });
        }
    }

    private void applyThemeRecursively(View view) {
        if (ui == null || ui.pack == null) return;
        
        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(theme.text);
            ((TextView) view).setTypeface(Tuils.getTypeface(this));
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            
            // Critical Fix: Also apply background style to containers
            if (group.getId() == R.id.card_container || group.getParent() == persistentContainer) {
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(theme.surface);
                gd.setCornerRadius(Tuils.dpToPx(this, (int)theme.borderRadius));
                gd.setStroke((int)Tuils.dpToPx(this, (int)theme.borderWidth), theme.border);
                group.setBackground(gd);
            }

            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeRecursively(group.getChildAt(i));
            }
        }
    }
}
