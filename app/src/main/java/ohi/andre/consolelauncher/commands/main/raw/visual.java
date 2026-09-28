package ohi.andre.consolelauncher.commands.main.raw;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.NotesManager;
import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.Tuils;

public class visual implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) {
        MainPack info = (MainPack) pack;

        if (pack.args.length > 0) {
            String fullArg = pack.args[0].toString();
            String[] split = fullArg.split("\\s+");
            String arg = split[0];
            
            boolean fixed = (pack.args.length > 1 && pack.args[1].toString().equalsIgnoreCase("-fixed"))
                    || (split.length > 1 && split[1].equalsIgnoreCase("-fixed"));

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
            } else if (arg.equalsIgnoreCase("clear")) {
                Behavior.persistent_system_card.parent().write(Behavior.persistent_system_card, "false");
                Behavior.persistent_battery_card.parent().write(Behavior.persistent_battery_card, "false");
                Behavior.persistent_notes_card.parent().write(Behavior.persistent_notes_card, "false");
                Behavior.persistent_shortcuts_card.parent().write(Behavior.persistent_shortcuts_card, "false");
                Behavior.persistent_music_card.parent().write(Behavior.persistent_music_card, "false");

                if (info.context instanceof LauncherActivity) {
                    ((LauncherActivity) info.context).runOnUiThread(() -> {
                        ViewGroup pc = ((LauncherActivity) info.context).findViewById(R.id.persistent_container);
                        if (pc != null) pc.removeAllViews();
                    });
                }
                return "Persistent area cleared.";
            }
        }

        return "Usage: visual [battery | system | notes | shortcuts] [-fixed] or visual clear";
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
        int pct = (int) ((usedRam / (float) totalRam) * 100);

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

        int pct = (scale > 0) ? (int) ((level / (float) scale) * 100) : 0;
        
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
        status.setText(isCharging ? "⚡ CHARGING_PROTOCOL: ACTIVE" : "🔋 DISCHARGING_MODE: ENABLED");

        int color = isCharging ? Color.CYAN : (pct > 20 ? Color.GREEN : Color.RED);
        title.setTextColor(color);
        percentage.setTextColor(color);
        status.setTextColor(color);
        progress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (fixed && info.context instanceof LauncherActivity) {
            Behavior.persistent_battery_card.parent().write(Behavior.persistent_battery_card, "true");
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
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

        btnSet.setOnClickListener(v -> Tuils.sendOutput(info.context, "To set the default music app, use: config -set music_target_app [package_name]\nExample: com.spotify.music"));

        MediaSessionManager msm = (MediaSessionManager) info.context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        
        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable updateTask = new Runnable() {
            final String[] bars = {" ", "▂", "▃", "▄", "▅", "▆", "▇", "█"};
            
            @Override
            public void run() {
                if (card.getWindowToken() == null && !fixed) return;
                if (fixed && card.getParent() == null) return;

                try {
                    List<MediaController> controllers = null;
                    try {
                        controllers = msm.getActiveSessions(new ComponentName(info.context, NotificationService.class));
                    } catch (SecurityException e) {
                        Tuils.log("MediaSession SecurityException", e);
                    }

                    if (controllers == null || controllers.isEmpty()) {
                        try {
                            controllers = msm.getActiveSessions(null);
                        } catch (Exception ignore) {}
                    }

                    if (controllers != null && !controllers.isEmpty()) {
                        MediaController mc = controllers.get(0);
                        MediaMetadata metadata = mc.getMetadata();
                        PlaybackState state = mc.getPlaybackState();

                        if (metadata != null) {
                            String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                            String track = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                            songName.setText((artist != null ? artist : "UNKNOWN") + " - " + (track != null ? track : "UNKNOWN"));
                        } else {
                            songName.setText("ACTIVE_SESSION [NO_METADATA]");
                        }

                        boolean isPlaying = state != null && state.getState() == PlaybackState.STATE_PLAYING;
                        btnPlay.setText(isPlaying ? "[ PAUSE ]" : "[ PLAY ]");

                        if (isPlaying) {
                            StringBuilder ascii = new StringBuilder();
                            for (int j = 0; j < 10; j++) {
                                int h = (int) (1 + Math.random() * (bars.length - 1));
                                ascii.append(bars[h]).append(" ");
                            }
                            ascii.append("\n");
                            for (int j = 0; j < 10; j++) {
                                int h = (int) (Math.random() * 3);
                                ascii.append(bars[h]).append(" ");
                            }
                            visualizer.setText(ascii.toString());
                        } else {
                            visualizer.setText("|| || || || || || || || || ||\n.. .. .. .. .. .. .. .. .. ..");
                        }

                        btnPlay.setOnClickListener(v -> {
                            if (isPlaying) mc.getTransportControls().pause();
                            else mc.getTransportControls().play();
                        });
                        btnPrev.setOnClickListener(v -> mc.getTransportControls().skipToPrevious());
                        btnNext.setOnClickListener(v -> mc.getTransportControls().skipToNext());
                        
                        title.setOnClickListener(v -> {
                            String pkg = mc.getPackageName();
                            Intent launch = info.context.getPackageManager().getLaunchIntentForPackage(pkg);
                            if (launch != null) {
                                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                info.context.startActivity(launch);
                            }
                        });
                    } else {
                        songName.setText("NO_ACTIVE_SESSION");
                        visualizer.setText("-- -- -- -- -- -- -- -- --");
                        
                        title.setOnClickListener(v -> {
                            String target = XMLPrefsManager.get(Behavior.music_target_app);
                            if (target != null && !target.isEmpty()) {
                                Intent launch = info.context.getPackageManager().getLaunchIntentForPackage(target);
                                if (launch != null) {
                                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    info.context.startActivity(launch);
                                }
                                else Tuils.sendOutput(info.context, "Target app not found: " + target);
                            } else {
                                Tuils.sendOutput(info.context, "Set target app with: config -set music_target_app [pkg]");
                            }
                        });
                    }
                } catch (Exception e) {
                    songName.setText("ERROR: " + e.getMessage());
                }

                String listeners = Settings.Secure.getString(info.context.getContentResolver(), "enabled_notification_listeners");
                boolean hasAccess = listeners != null && listeners.contains(info.context.getPackageName());

                if (!hasAccess) {
                    songName.setText("GRANT_NOTIFICATION_ACCESS_REQUIRED [CLICK_TO_FIX]");
                    songName.setOnClickListener(v -> {
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        info.context.startActivity(intent);
                    });
                }

                handler.postDelayed(this, 150);
            }
        };

        handler.post(updateTask);

        if (fixed && info.context instanceof LauncherActivity) {
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
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

    @Override
    public int[] argType() {
        return new int[] {CommandAbstraction.PLAIN_TEXT, CommandAbstraction.PLAIN_TEXT};
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
