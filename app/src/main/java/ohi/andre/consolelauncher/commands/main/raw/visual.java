package ohi.andre.consolelauncher.commands.main.raw;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.NotesManager;
import ohi.andre.consolelauncher.managers.PrivacyCenter;
import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.Tuils;

public class visual implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;

        String arg = "";
        boolean fixed = false;

        if (pack.args != null && pack.args.length > 0 && pack.args[0] != null) {
            Object obj = pack.args[0];
            List<String> list = new ArrayList<>();
            if (obj instanceof List) {
                list = (List<String>) obj;
            } else if (obj instanceof String) {
                String[] split = ((String) obj).split("\\s+");
                Collections.addAll(list, split);
            }

            if (pack.args.length > 1 && pack.args[1] != null) {
                list.add(pack.args[1].toString());
            }

            if (!list.isEmpty()) {
                arg = list.get(0).trim();
                for (int i = 1; i < list.size(); i++) {
                    String item = list.get(i).trim();
                    if (item.equalsIgnoreCase("-fixed") || item.equalsIgnoreCase("fixed")) {
                        fixed = true;
                    }
                }
            }

            if (arg.toLowerCase().endsWith("-fixed")) {
                arg = arg.substring(0, arg.length() - 6);
                fixed = true;
            }

            if (arg.equalsIgnoreCase("battery")) {
                showBatteryCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("system")) {
                showSystemCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("music")) {
                showMusicCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("notes")) {
                showNotesCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("shortcuts")) {
                showShortcutsCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("weather")) {
                showWeatherCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("termux")) {
                showTermuxCard(info, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("dashboard")) {
                showDashboard(info);
                return null;
            } else if (arg.equalsIgnoreCase("clear")) {
                Behavior.persistent_system_card.parent().write(Behavior.persistent_system_card, "false");
                Behavior.persistent_battery_card.parent().write(Behavior.persistent_battery_card, "false");
                Behavior.persistent_notes_card.parent().write(Behavior.persistent_notes_card, "false");
                Behavior.persistent_shortcuts_card.parent().write(Behavior.persistent_shortcuts_card, "false");
                Behavior.persistent_music_card.parent().write(Behavior.persistent_music_card, "false");
                Behavior.persistent_weather_card.parent().write(Behavior.persistent_weather_card, "false");
                Behavior.persistent_termux_card.parent().write(Behavior.persistent_termux_card, "false");

                if (info.context instanceof LauncherActivity) {
                    ((LauncherActivity) info.context).runOnUiThread(() -> {
                        ViewGroup pc = ((LauncherActivity) info.context).findViewById(R.id.persistent_container);
                        if (pc != null) {
                            pc.removeAllViews();
                            ((LauncherActivity) info.context).updatePersistentAreaVisibility();
                        }
                    });
                }
                return "Persistent area cleared.";
            }
        }

        return "Usage: visual [battery | system | notes | shortcuts | music | weather | termux | dashboard] [-fixed] or visual clear";
    }



    private void showSystemCard(MainPack info, boolean fixed) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_system, null);

        // Styling (reuse existing logic but wrap for output)
        TextView title = card.findViewById(R.id.system_title);
        TextView systemInfo = card.findViewById(R.id.system_info);
        TextView ramInfo = card.findViewById(R.id.ram_info);
        ProgressBar ramProgress = card.findViewById(R.id.ram_progress);

        title.setTypeface(Tuils.getTypeface(info.context));
        systemInfo.setTypeface(Tuils.getTypeface(info.context));
        ramInfo.setTypeface(Tuils.getTypeface(info.context));

        ActivityManager am = (ActivityManager) info.context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

        long totalRam = Tuils.totalRam() / 1024; // MB
        long freeRam = (long) (Tuils.freeRam(am, mi) / (1024 * 1024)); // MB
        long usedRam = totalRam - freeRam;
        int pct = totalRam > 0 ? Math.max(0, Math.min(100, (int) ((usedRam / (float) totalRam) * 100))) : 0;

        String device = Build.MANUFACTURER + " " + Build.MODEL;
        String androidVer = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        String kernel = System.getProperty("os.version");
        
        long uptimeMillis = SystemClock.elapsedRealtime();
        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis / (1000 * 60)) % 60;
        String uptime = hours + " hours, " + minutes + " minutes";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-14s : %s\n", "Phone Model", device));
        sb.append(String.format("%-14s : %s\n", "OS Type", "Android " + androidVer));
        sb.append(String.format("%-14s : %s\n", "Kernel", kernel));
        sb.append(String.format("%-14s : %s\n", "Uptime", uptime));
        sb.append(String.format("%-14s : %d / %d MB (%d%%)", "Memory", usedRam, totalRam, pct));

        systemInfo.setText(sb.toString());
        ramInfo.setVisibility(View.GONE);
        ramProgress.setProgress(pct);
        ThemeEngine.DesignTokens theme = currentTheme();
        ramProgress.setProgressTintList(ColorStateList.valueOf(theme.primary));

        if (fixed && info.context instanceof LauncherActivity) {
            Behavior.persistent_system_card.parent().write(Behavior.persistent_system_card, "true");
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showBatteryCard(MainPack info, boolean fixed) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_battery, null);

        TextView title = card.findViewById(R.id.battery_title);
        TextView percentage = card.findViewById(R.id.battery_percentage);
        TextView status = card.findViewById(R.id.battery_status);
        ProgressBar progress = card.findViewById(R.id.battery_progress);

        title.setTypeface(Tuils.getTypeface(info.context));
        percentage.setTypeface(Tuils.getTypeface(info.context));
        status.setTypeface(Tuils.getTypeface(info.context));

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = info.context.registerReceiver(null, ifilter);
        
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        int statusInt = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;

        int pct = (scale > 0 && level >= 0) ? Math.max(0, Math.min(100, (int) ((level / (float) scale) * 100))) : 0;
        
        StringBuilder batteryAscii = new StringBuilder("[");
        int segments = 20;
        int filled = (int) (pct / (100f / segments));
        for (int i = 0; i < segments; i++) {
            if (i < filled) batteryAscii.append("■");
            else batteryAscii.append(" ");
        }
        batteryAscii.append("] ").append(pct).append("%");

        percentage.setText(batteryAscii.toString());
        progress.setProgress(pct);

        boolean isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING || statusInt == BatteryManager.BATTERY_STATUS_FULL;
        status.setText(isCharging ? "CHARGING  /  POWER CONNECTED" : "ON BATTERY  /  DISCHARGING");

        ThemeEngine.DesignTokens theme = currentTheme();
        int color = pct <= 20 ? theme.error : (isCharging ? theme.success : theme.primary);
        title.setTextColor(theme.primary);
        percentage.setTextColor(color);
        status.setTextColor(theme.textMuted);
        progress.setProgressTintList(ColorStateList.valueOf(color));

        if (fixed && info.context instanceof LauncherActivity) {
            Behavior.persistent_battery_card.parent().write(Behavior.persistent_battery_card, "true");
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
            if (info.context instanceof LauncherActivity) {
                ((LauncherActivity) info.context).runOnUiThread(() -> {
                    ViewGroup pc = ((LauncherActivity) info.context).findViewById(R.id.persistent_container);
                    if (pc != null && pc.getChildCount() == 0) {
                        ((LauncherActivity) info.context).onPersistentOutput(card);
                    }
                });
            }
        }
    }

    private void showMusicCard(MainPack info, boolean fixed) {
        if (fixed) {
            Behavior.persistent_music_card.parent().write(Behavior.persistent_music_card, "true");
        }
        
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_music, null);

        TextView title = card.findViewById(R.id.music_title);
        TextView songName = card.findViewById(R.id.song_name);
        TextView visualizer = card.findViewById(R.id.ascii_visualizer);
        TextView btnPrev = card.findViewById(R.id.btn_prev);
        TextView btnPlay = card.findViewById(R.id.btn_play_pause);
        TextView btnNext = card.findViewById(R.id.btn_next);
        TextView btnSet = card.findViewById(R.id.music_settings);

        title.setTypeface(Tuils.getTypeface(info.context));
        songName.setTypeface(Tuils.getTypeface(info.context));
        visualizer.setTypeface(Tuils.getTypeface(info.context));
        btnPrev.setTypeface(Tuils.getTypeface(info.context));
        btnPlay.setTypeface(Tuils.getTypeface(info.context));
        btnNext.setTypeface(Tuils.getTypeface(info.context));
        btnSet.setTypeface(Tuils.getTypeface(info.context));

        btnSet.setOnClickListener(v -> PrivacyCenter.openSettings(info.context, "Notification access"));

        MediaSessionManager msm = (MediaSessionManager) info.context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        
        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);
        styleMusicControl(btnPrev, theme);
        styleMusicControl(btnPlay, theme);
        styleMusicControl(btnNext, theme);
        styleMusicControl(btnSet, theme);

        final Handler handler = new Handler(Looper.getMainLooper());
        final MediaController[] activeController = new MediaController[1];
        final boolean[] listening = {false};
        final String[] bars = {"▂", "▃", "▄", "▅", "▆", "▇", "█"};
        final Runnable[] renderRef = new Runnable[1];

        Runnable animation = new Runnable() {
            @Override public void run() {
                MediaController controller = activeController[0];
                PlaybackState state = controller == null ? null : controller.getPlaybackState();
                if (!card.isAttachedToWindow() || state == null || state.getState() != PlaybackState.STATE_PLAYING) return;
                StringBuilder barsText = new StringBuilder();
                for (int i = 0; i < 18; i++) barsText.append(bars[(int) (Math.random() * bars.length)]).append(' ');
                visualizer.setText(barsText.toString());
                handler.postDelayed(this, 650);
            }
        };

        MediaController.Callback controllerCallback = new MediaController.Callback() {
            @Override public void onMetadataChanged(MediaMetadata metadata) {
                if (renderRef[0] != null) renderRef[0].run();
            }
            @Override public void onPlaybackStateChanged(PlaybackState state) {
                if (renderRef[0] != null) renderRef[0].run();
            }
            @Override public void onSessionDestroyed() {
                if (activeController[0] != null) activeController[0].unregisterCallback(this);
                activeController[0] = null;
                if (renderRef[0] != null) renderRef[0].run();
            }
        };

        renderRef[0] = () -> {
            MediaController controller = activeController[0];
            if (controller == null) {
                songName.setText(PrivacyCenter.hasNotificationAccess(info.context)
                        ? "NO_ACTIVE_SESSION  /  START A MUSIC APP"
                        : "GRANT NOTIFICATION ACCESS TO CONNECT A PLAYER");
                btnPlay.setText("[ PLAY ]");
                btnPlay.setEnabled(false);
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
                visualizer.setText("-- -- -- -- -- -- -- -- -- --");
                title.setOnClickListener(v -> {
                    String target = XMLPrefsManager.get(Behavior.music_target_app);
                    if (target != null && !target.isEmpty()) {
                        Intent launch = info.context.getPackageManager().getLaunchIntentForPackage(target);
                        if (launch != null) {
                            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            info.context.startActivity(launch);
                        } else Tuils.sendOutput(info.context, "Target app not found: " + target);
                    }
                });
                handler.removeCallbacks(animation);
                return;
            }

            MediaMetadata metadata = controller.getMetadata();
            String artist = metadata == null ? null : metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            String track = metadata == null ? null : metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            if ((artist == null || artist.isEmpty()) && metadata != null) artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            songName.setText(((artist == null || artist.isEmpty()) ? "UNKNOWN ARTIST" : artist)
                    + "  /  " + ((track == null || track.isEmpty()) ? "UNKNOWN TRACK" : track));

            PlaybackState state = controller.getPlaybackState();
            boolean playing = state != null && state.getState() == PlaybackState.STATE_PLAYING;
            long actions = state == null ? 0 : state.getActions();
            btnPlay.setText(playing ? "[ PAUSE ]" : "[ PLAY ]");
            btnPlay.setContentDescription(playing ? "Pause music" : "Play music");
            btnPlay.setEnabled((actions & (playing ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY)) != 0);
            btnPrev.setEnabled((actions & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0);
            btnNext.setEnabled((actions & PlaybackState.ACTION_SKIP_TO_NEXT) != 0);
            visualizer.setText(playing ? "SESSION ACTIVE  /  ANIMATED DISPLAY"
                    : "SESSION PAUSED  /  MEDIA CONTROLS READY");

            btnPlay.setOnClickListener(v -> {
                PlaybackState latest = controller.getPlaybackState();
                if (latest != null && latest.getState() == PlaybackState.STATE_PLAYING) controller.getTransportControls().pause();
                else controller.getTransportControls().play();
            });
            btnPrev.setOnClickListener(v -> controller.getTransportControls().skipToPrevious());
            btnNext.setOnClickListener(v -> controller.getTransportControls().skipToNext());
            title.setOnClickListener(v -> {
                Intent launch = info.context.getPackageManager().getLaunchIntentForPackage(controller.getPackageName());
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    info.context.startActivity(launch);
                }
            });
            handler.removeCallbacks(animation);
            if (playing && card.isAttachedToWindow()) handler.post(animation);
        };

        MediaSessionManager.OnActiveSessionsChangedListener sessionsListener = controllers -> handler.post(() -> {
            MediaController selected = chooseMusicController(controllers);
            if (selected != activeController[0]) {
                if (activeController[0] != null) activeController[0].unregisterCallback(controllerCallback);
                activeController[0] = selected;
                if (selected != null) selected.registerCallback(controllerCallback, handler);
            }
            renderRef[0].run();
        });

        card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {
                if (msm == null || listening[0]) return;
                try {
                    msm.addOnActiveSessionsChangedListener(sessionsListener,
                            new ComponentName(info.context, NotificationService.class), handler);
                    listening[0] = true;
                } catch (SecurityException e) {
                    Tuils.log("Media session access is not enabled", e);
                }
                if (!listening[0]) {
                    renderRef[0].run();
                    return;
                }
                try {
                    List<MediaController> active = msm.getActiveSessions(
                            new ComponentName(info.context, NotificationService.class));
                    sessionsListener.onActiveSessionsChanged(active);
                } catch (SecurityException e) {
                    songName.setText("ENABLE NOTIFICATION ACCESS TO CONNECT MUSIC");
                }
            }
            @Override public void onViewDetachedFromWindow(View v) {
                handler.removeCallbacks(animation);
                if (activeController[0] != null) {
                    activeController[0].unregisterCallback(controllerCallback);
                    activeController[0] = null;
                }
                if (msm != null && listening[0]) {
                    try { msm.removeOnActiveSessionsChangedListener(sessionsListener); }
                    catch (Exception ignored) {}
                }
                listening[0] = false;
            }
        });

        if (fixed && info.context instanceof LauncherActivity) {
            if (!XMLPrefsManager.getBoolean(Behavior.persistent_music_card)) {
                Behavior.persistent_music_card.parent().write(Behavior.persistent_music_card, "true");
            }
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
            if (info.context instanceof LauncherActivity) {
                ((LauncherActivity) info.context).runOnUiThread(() -> {
                    ViewGroup pc = ((LauncherActivity) info.context).findViewById(R.id.persistent_container);
                    if (pc != null && pc.getChildCount() == 0) {
                        ((LauncherActivity) info.context).onPersistentOutput(card);
                    }
                });
            }
        }

    }

    private static MediaController chooseMusicController(List<MediaController> controllers) {
        if (controllers == null || controllers.isEmpty()) return null;
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            if (state != null && state.getState() == PlaybackState.STATE_PLAYING) return controller;
        }
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            if (state != null && state.getState() != PlaybackState.STATE_STOPPED
                    && state.getState() != PlaybackState.STATE_NONE) return controller;
        }
        return null;
    }

    private static void styleMusicControl(TextView control, ThemeEngine.DesignTokens theme) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(theme.surface);
        background.setCornerRadius(Tuils.dpToPx(control.getContext(), 8));
        background.setStroke(Tuils.dpToPx(control.getContext(), 1), theme.primary);
        control.setBackground(background);
        control.setTextColor(theme.primary);
        control.setPadding(Tuils.dpToPx(control.getContext(), 12), Tuils.dpToPx(control.getContext(), 9),
                Tuils.dpToPx(control.getContext(), 12), Tuils.dpToPx(control.getContext(), 9));
        control.setTypeface(Tuils.getTypeface(control.getContext()));
    }

    private static ThemeEngine.DesignTokens currentTheme() {
        try {
            return ThemeEngine.getPreset(ThemeEngine.Preset.valueOf(XMLPrefsManager.get(Behavior.theme_preset)));
        } catch (Exception ignored) {
            return ThemeEngine.getPreset(ThemeEngine.Preset.CLASSIC_TERMINAL);
        }
    }

    private void showNotesCard(MainPack info, boolean fixed) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_notes, null);

        TextView title = card.findViewById(R.id.notes_title);
        TextView content = card.findViewById(R.id.notes_content);

        title.setTypeface(Tuils.getTypeface(info.context));
        content.setTypeface(Tuils.getTypeface(info.context));

        final Runnable updateNotes = () -> {
            if (info.context instanceof LauncherActivity) {
                NotesManager nm = ((LauncherActivity) info.context).getUIManager().getNotesManager();
                if (nm != null) {
                    content.setText(nm.getNotes());
                }
            }
        };

        updateNotes.run();

        // Optional: Listen for note changes via broadcast if NotesManager sends one
        // For now, manual update on create is fine for a widget.

        if (fixed && info.context instanceof LauncherActivity) {
            if (!XMLPrefsManager.getBoolean(Behavior.persistent_notes_card)) {
                Behavior.persistent_notes_card.parent().write(Behavior.persistent_notes_card, "true");
            }
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showShortcutsCard(MainPack info, boolean fixed) {
        LayoutInflater inflater = LayoutInflater.from(info.context);
        View card = inflater.inflate(R.layout.card_shortcuts, null);

        TextView title = card.findViewById(R.id.shortcuts_title);
        LinearLayout container = card.findViewById(R.id.shortcuts_container);

        title.setTypeface(Tuils.getTypeface(info.context));

        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);

        AppsManager.LaunchInfo[] suggested = info.appsManager.getSuggestedApps();
        if (suggested != null) {
            for (int i = 0; i < Math.min(5, suggested.length); i++) {
                final AppsManager.LaunchInfo li = suggested[i];
                if (li == null) continue;

                TextView btn = new TextView(info.context);
                btn.setText("[" + li.publicLabel.substring(0, Math.min(3, li.publicLabel.length())).toUpperCase() + "]");
                btn.setPadding(Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 5), Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 5));
                btn.setTypeface(Tuils.getTypeface(info.context));
                btn.setTextColor(theme.primary);
                
                // Add a border to buttons
                GradientDrawable b = new GradientDrawable();
                b.setStroke(Tuils.dpToPx(info.context, 1), theme.border);
                b.setCornerRadius(Tuils.dpToPx(info.context, 4));
                btn.setBackground(b);
                
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(Tuils.dpToPx(info.context, 5), 0, Tuils.dpToPx(info.context, 5), 0);
                btn.setLayoutParams(lp);

                btn.setOnClickListener(v -> {
                    Intent intent = info.appsManager.getIntent(li);
                    if (intent != null) info.context.startActivity(intent);
                });

                container.addView(btn);
            }
        }

        if (fixed && info.context instanceof LauncherActivity) {
            Behavior.persistent_shortcuts_card.parent().write(Behavior.persistent_shortcuts_card, "true");
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showWeatherCard(MainPack info, boolean fixed) {
        if (fixed) {
            Behavior.persistent_weather_card.parent().write(Behavior.persistent_weather_card, "true");
        }
        
        LinearLayout card = new LinearLayout(info.context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10));

        TextView title = new TextView(info.context);
        title.setText("● [ WEATHER_MODULE ]");
        title.setTypeface(Tuils.getTypeface(info.context));
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView status = new TextView(info.context);
        status.setTypeface(Tuils.getTypeface(info.context));
        status.setPadding(0, Tuils.dpToPx(info.context, 6), 0, 0);

        boolean enabled = XMLPrefsManager.getBoolean(Ui.show_weather);
        if (enabled) {
            status.setText("Status: ACTIVE // Location Weather Provider Connected\nUse: 'tuiweather -update' to refresh");
        } else {
            status.setText("Status: INACTIVE // Weather is currently disabled\nUse: 'tuiweather -enable' to activate");
        }
        card.addView(status);

        if (fixed && info.context instanceof LauncherActivity) {
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showTermuxCard(MainPack info, boolean fixed) {
        if (fixed) {
            Behavior.persistent_termux_card.parent().write(Behavior.persistent_termux_card, "true");
        }

        LinearLayout card = new LinearLayout(info.context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10));

        TextView title = new TextView(info.context);
        title.setText("● [ TERMUX_INTEGRATION_MODULE ]");
        title.setTypeface(Tuils.getTypeface(info.context));
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView status = new TextView(info.context);
        status.setTypeface(Tuils.getTypeface(info.context));
        status.setPadding(0, Tuils.dpToPx(info.context, 6), 0, Tuils.dpToPx(info.context, 6));

        final String TERMUX_PKG = "com.termux";
        Intent termuxIntent = info.context.getPackageManager().getLaunchIntentForPackage(TERMUX_PKG);
        if (termuxIntent != null) {
            status.setText("Status: INSTALLED // Package: " + TERMUX_PKG + "\nIntegration: READY [CLICK_TO_LAUNCH]");
            card.setOnClickListener(v -> {
                termuxIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                info.context.startActivity(termuxIntent);
            });
        } else {
            status.setText("Status: NOT_DETECTED\nTermux is not installed on this device.");
        }
        card.addView(status);

        if (fixed && info.context instanceof LauncherActivity) {
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showDashboard(MainPack info) {
        showSystemCard(info, false);
        showBatteryCard(info, false);
        showWeatherCard(info, false);
        showTermuxCard(info, false);
        showShortcutsCard(info, false);
    }

    @Override
    public int[] argType() {
        // PLAIN_TEXT consumes the complete remaining command line. Parse its
        // subcommands and flags inside exec() instead of asking the command
        // parser for multiple PLAIN_TEXT arguments (which it cannot split).
        return new int[] {CommandAbstraction.PLAIN_TEXT};
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
    public String onArgNotFound(ExecutePack info, int index) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack info, int nArgs) {
        return null;
    }
}
