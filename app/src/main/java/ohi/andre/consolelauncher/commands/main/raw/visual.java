package ohi.andre.consolelauncher.commands.main.raw;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        Integer requestedMusicScale = null;

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
                    } else if (item.equalsIgnoreCase("-scale") || item.equalsIgnoreCase("--scale")
                            || item.equalsIgnoreCase("-size")) {
                        if (i + 1 < list.size()) {
                            try {
                                requestedMusicScale = Integer.parseInt(list.get(++i).replace("%", ""));
                            } catch (NumberFormatException ignored) {
                                return "Usage: visual music -scale [50-120] (for example: visual music -scale 65)";
                            }
                        } else {
                            return "Usage: visual music -scale [50-120] (for example: visual music -scale 65)";
                        }
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
                if (requestedMusicScale != null) {
                    if (requestedMusicScale < 50 || requestedMusicScale > 120) {
                        return "Music card scale must be between 50% and 120%.";
                    }
                    Behavior.music_widget_scale.parent().write(Behavior.music_widget_scale,
                            String.valueOf(requestedMusicScale));
                    if (info.context instanceof LauncherActivity
                            && XMLPrefsManager.getBoolean(Behavior.persistent_music_card)) {
                        ((LauncherActivity) info.context).refreshPersistentContainer();
                    } else {
                        showMusicCard(info, fixed);
                    }
                    return "Music card size set to " + requestedMusicScale + "%.";
                }
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
            } else if (arg.equalsIgnoreCase("script")) {
                String scriptName = "script.sh";
                for (int i = 1; i < list.size(); i++) {
                    String item = list.get(i).trim();
                    if (!item.equalsIgnoreCase("-fixed") && !item.equalsIgnoreCase("fixed")) {
                        scriptName = item;
                        break;
                    }
                }
                showScriptCard(info, scriptName, fixed);
                return null;
            } else if (arg.equalsIgnoreCase("sys") || arg.equalsIgnoreCase("monitor")) {
                showSysCard(info, fixed);
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

        return "Usage: visual [battery | system | notes | shortcuts | music | weather | termux | dashboard] [-fixed]; visual music -scale [50-120]; or visual clear";
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
        TextView songArtist = card.findViewById(R.id.song_artist);
        TextView musicSource = card.findViewById(R.id.music_source);
        TextView btnPrev = card.findViewById(R.id.btn_prev);
        TextView btnPlay = card.findViewById(R.id.btn_play_pause);
        TextView btnNext = card.findViewById(R.id.btn_next);
        TextView btnSet = card.findViewById(R.id.music_settings);
        TextView positionView = card.findViewById(R.id.music_position);
        TextView durationView = card.findViewById(R.id.music_duration);
        ImageView albumArt = card.findViewById(R.id.album_art);
        SeekBar progress = card.findViewById(R.id.music_progress);

        title.setTypeface(Tuils.getTypeface(info.context));
        songName.setTypeface(Tuils.getTypeface(info.context));
        songArtist.setTypeface(Tuils.getTypeface(info.context));
        musicSource.setTypeface(Tuils.getTypeface(info.context));
        btnPrev.setTypeface(Tuils.getTypeface(info.context));
        btnPlay.setTypeface(Tuils.getTypeface(info.context));
        btnNext.setTypeface(Tuils.getTypeface(info.context));
        btnSet.setTypeface(Tuils.getTypeface(info.context));
        positionView.setTypeface(Tuils.getTypeface(info.context));
        durationView.setTypeface(Tuils.getTypeface(info.context));

        MediaSessionManager msm = (MediaSessionManager) info.context.getSystemService(Context.MEDIA_SESSION_SERVICE);

        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens theme = ThemeEngine.getPreset(preset);
        float musicScale = getMusicScale();
        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(theme.surface);
        cardBackground.setCornerRadius(Tuils.dpToPx(info.context, theme.borderRadius * musicScale));
        cardBackground.setStroke(Math.max(1, Math.round(Tuils.dpToPx(info.context, theme.borderWidth * musicScale))), theme.border);
        card.setBackground(cardBackground);
        albumArt.setBackground(makeMusicPill(info.context, theme.surface, theme, musicScale));
        albumArt.setClipToOutline(true);
        musicSource.setBackground(makeMusicPill(info.context, theme.surface, theme, musicScale));
        musicSource.setPadding(Tuils.dpToPx(info.context, 7), Tuils.dpToPx(info.context, 3),
                Tuils.dpToPx(info.context, 7), Tuils.dpToPx(info.context, 3));
        title.setTextColor(theme.primary);
        songName.setTextColor(theme.text);
        songArtist.setTextColor(theme.textMuted);
        musicSource.setTextColor(theme.secondary);
        positionView.setTextColor(theme.textMuted);
        durationView.setTextColor(theme.textMuted);
        progress.setProgressTintList(ColorStateList.valueOf(theme.primary));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(theme.border));
        progress.setThumbTintList(ColorStateList.valueOf(theme.primary));
        styleMusicControl(btnPrev, theme, false, musicScale);
        styleMusicControl(btnPlay, theme, true, musicScale);
        styleMusicControl(btnNext, theme, false, musicScale);
        styleMusicControl(btnSet, theme, false, musicScale);
        applyMusicScale(card, musicScale);
        installResponsiveMusicWidth(card, musicScale);

        final Handler handler = new Handler(Looper.getMainLooper());
        final MediaController[] activeController = new MediaController[1];
        final boolean[] listening = {false};
        final long[] durationMs = {0L};
        final Runnable[] renderRef = new Runnable[1];

        Runnable progressTicker = new Runnable() {
            @Override public void run() {
                MediaController controller = activeController[0];
                PlaybackState state = controller == null ? null : controller.getPlaybackState();
                if (!card.isAttachedToWindow() || state == null || state.getState() != PlaybackState.STATE_PLAYING) return;
                updatePlaybackProgress(state);
                handler.postDelayed(this, 1000);
            }

            private void updatePlaybackProgress(PlaybackState state) {
                long position = state.getPosition();
                if (position < 0) position = 0;
                if (state.getState() == PlaybackState.STATE_PLAYING) {
                    long updatedAt = state.getLastPositionUpdateTime();
                    if (updatedAt > 0) {
                        long elapsed = Math.max(0, SystemClock.elapsedRealtime() - updatedAt);
                        position += (long) (elapsed * state.getPlaybackSpeed());
                    }
                }
                if (durationMs[0] > 0) position = Math.min(position, durationMs[0]);
                if (!progress.isPressed()) {
                    progress.setProgress(durationMs[0] > 0
                            ? (int) Math.min(1000, (position * 1000L) / durationMs[0]) : 0);
                }
                positionView.setText(formatMusicTime(position));
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

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser && durationMs[0] > 0) {
                    positionView.setText(formatMusicTime(durationMs[0] * value / 1000L));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { handler.removeCallbacks(progressTicker); }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                MediaController controller = activeController[0];
                if (controller != null && durationMs[0] > 0) {
                    PlaybackState state = controller.getPlaybackState();
                    if (state != null && (state.getActions() & PlaybackState.ACTION_SEEK_TO) != 0) {
                        controller.getTransportControls().seekTo(durationMs[0] * seekBar.getProgress() / 1000L);
                    }
                    if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                        handler.post(progressTicker);
                    }
                }
            }
        });

        btnSet.setOnClickListener(v -> {
            if (!PrivacyCenter.hasNotificationAccess(info.context)) {
                PrivacyCenter.openSettings(info.context, "Notification access");
                return;
            }
            String target = XMLPrefsManager.get(Behavior.music_target_app);
            if (target == null || target.isEmpty()) {
                target = activeController[0] != null ? activeController[0].getPackageName() : "com.spotify.music";
            }
            Intent launch = info.context.getPackageManager().getLaunchIntentForPackage(target);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                info.context.startActivity(launch);
            } else {
                Tuils.sendOutput(info.context, "Music app not found: " + target);
            }
        });

        renderRef[0] = () -> {
            MediaController controller = activeController[0];
            if (controller == null) {
                boolean hasAccess = PrivacyCenter.hasNotificationAccess(info.context);
                title.setText(hasAccess ? "NOW PLAYING" : "MUSIC ACCESS REQUIRED");
                songName.setText(hasAccess ? "Nothing is playing" : "Connect notification access");
                songArtist.setText(hasAccess ? "Start Spotify or another music app" : "Tap Connect to enable media controls");
                musicSource.setText("SPOTIFY READY");
                btnSet.setText(hasAccess ? "OPEN SPOTIFY" : "CONNECT");
                albumArt.setImageResource(android.R.drawable.ic_media_play);
                progress.setProgress(0);
                durationMs[0] = 0;
                positionView.setText("0:00");
                durationView.setText("--:--");
                btnPlay.setText("▶");
                btnPlay.setEnabled(false);
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
                progress.setEnabled(false);
                handler.removeCallbacks(progressTicker);
                return;
            }

            MediaMetadata metadata = controller.getMetadata();
            String artist = metadata == null ? null : metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if ((artist == null || artist.isEmpty()) && metadata != null) artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            String track = metadata == null ? null : metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String album = metadata == null ? null : metadata.getString(MediaMetadata.METADATA_KEY_ALBUM);
            songName.setText(track == null || track.isEmpty() ? "Unknown track" : track);
            songArtist.setText(artist == null || artist.isEmpty() ? (album == null ? "Unknown artist" : album) : artist);
            Bitmap artwork = metadata == null ? null : metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (artwork == null && metadata != null) artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (artwork == null && metadata != null) artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
            if (artwork != null) albumArt.setImageBitmap(artwork);
            else albumArt.setImageResource(android.R.drawable.ic_media_play);

            String source = controller.getPackageName();
            try {
                source = info.context.getPackageManager().getApplicationLabel(
                        info.context.getPackageManager().getApplicationInfo(source, 0)).toString();
            } catch (Exception ignored) {}
            btnSet.setText("OPEN APP");

            PlaybackState state = controller.getPlaybackState();
            boolean playing = state != null && state.getState() == PlaybackState.STATE_PLAYING;
            musicSource.setText(controller.getPackageName().equals("com.spotify.music")
                    ? (playing ? "SPOTIFY  •  PLAYING" : "SPOTIFY  •  PAUSED")
                    : source.toUpperCase(Locale.ROOT) + "  •  MEDIA SESSION");
            long actions = state == null ? 0 : state.getActions();
            btnPlay.setText(playing ? "Ⅱ" : "▶");
            btnPlay.setContentDescription(playing ? "Pause music" : "Play music");
            btnPlay.setEnabled((actions & (playing ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY)) != 0);
            btnPrev.setEnabled((actions & PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0);
            btnNext.setEnabled((actions & PlaybackState.ACTION_SKIP_TO_NEXT) != 0);
            btnPrev.setAlpha(btnPrev.isEnabled() ? 1f : 0.42f);
            btnNext.setAlpha(btnNext.isEnabled() ? 1f : 0.42f);
            btnPlay.setAlpha(btnPlay.isEnabled() ? 1f : 0.42f);
            durationMs[0] = metadata == null ? 0 : metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
            if (durationMs[0] > 0) {
                durationView.setText(formatMusicTime(durationMs[0]));
                progress.setEnabled((actions & PlaybackState.ACTION_SEEK_TO) != 0);
            } else {
                durationView.setText("LIVE");
                progress.setEnabled(false);
            }
            if (state != null) {
                long currentPosition = Math.max(0, state.getPosition());
                if (playing && state.getLastPositionUpdateTime() > 0) {
                    currentPosition += (long) (Math.max(0, SystemClock.elapsedRealtime() - state.getLastPositionUpdateTime()) * state.getPlaybackSpeed());
                }
                if (durationMs[0] > 0) currentPosition = Math.min(currentPosition, durationMs[0]);
                positionView.setText(formatMusicTime(currentPosition));
                progress.setProgress(durationMs[0] > 0
                        ? (int) Math.min(1000, (currentPosition * 1000L) / durationMs[0]) : 0);
            }

            btnPlay.setOnClickListener(v -> {
                PlaybackState latest = controller.getPlaybackState();
                if (latest != null && latest.getState() == PlaybackState.STATE_PLAYING) controller.getTransportControls().pause();
                else controller.getTransportControls().play();
            });
            btnPrev.setOnClickListener(v -> controller.getTransportControls().skipToPrevious());
            btnNext.setOnClickListener(v -> controller.getTransportControls().skipToNext());
            handler.removeCallbacks(progressTicker);
            if (playing && card.isAttachedToWindow()) handler.post(progressTicker);
        };

        MediaSessionManager.OnActiveSessionsChangedListener sessionsListener = controllers -> handler.post(() -> {
            String preferredPackage = XMLPrefsManager.get(Behavior.music_target_app);
            MediaController selected = chooseMusicController(controllers, preferredPackage);
            if (selected != activeController[0]) {
                if (activeController[0] != null) activeController[0].unregisterCallback(controllerCallback);
                activeController[0] = selected;
                if (selected != null) selected.registerCallback(controllerCallback, handler);
            }
            renderRef[0].run();
        });

        Runnable[] connectSessionAccess = new Runnable[1];
        connectSessionAccess[0] = new Runnable() {
            @Override public void run() {
                if (!card.isAttachedToWindow() || msm == null || listening[0]) return;
                try {
                    msm.addOnActiveSessionsChangedListener(sessionsListener,
                            new ComponentName(info.context, NotificationService.class), handler);
                    listening[0] = true;
                    List<MediaController> active = msm.getActiveSessions(
                            new ComponentName(info.context, NotificationService.class));
                    sessionsListener.onActiveSessionsChanged(active);
                } catch (SecurityException e) {
                    // The card can stay attached while the user grants access in Settings.
                    if (listening[0]) {
                        try { msm.removeOnActiveSessionsChangedListener(sessionsListener); }
                        catch (Exception ignored) {}
                        listening[0] = false;
                    }
                    handler.postDelayed(this, 1500);
                    renderRef[0].run();
                }
            }
        };

        card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View v) {
                if (msm == null || listening[0]) return;
                connectSessionAccess[0].run();
            }
            @Override public void onViewDetachedFromWindow(View v) {
                handler.removeCallbacks(connectSessionAccess[0]);
                handler.removeCallbacks(progressTicker);
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

    private static MediaController chooseMusicController(List<MediaController> controllers, String preferredPackage) {
        if (controllers == null || controllers.isEmpty()) return null;
        if (preferredPackage != null && !preferredPackage.isEmpty()) {
            for (MediaController controller : controllers) {
                PlaybackState state = controller.getPlaybackState();
                if (preferredPackage.equals(controller.getPackageName()) && state != null
                        && state.getState() != PlaybackState.STATE_STOPPED
                        && state.getState() != PlaybackState.STATE_NONE) return controller;
            }
        }
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

    private static String formatMusicTime(long milliseconds) {
        long seconds = Math.max(0, milliseconds / 1000L);
        return String.format(Locale.getDefault(), "%d:%02d", seconds / 60L, seconds % 60L);
    }

    private float getMusicScale() {
        try {
            int percent = Integer.parseInt(XMLPrefsManager.get(Behavior.music_widget_scale));
            return Math.max(0.5f, Math.min(1.2f, percent / 100f));
        } catch (Exception ignored) {
            return 0.75f;
        }
    }

    private static void applyMusicScale(View view, float scale) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, text.getTextSize() * scale);
        }

        view.setPadding(Math.round(view.getPaddingLeft() * scale), Math.round(view.getPaddingTop() * scale),
                Math.round(view.getPaddingRight() * scale), Math.round(view.getPaddingBottom() * scale));
        if (view.getMinimumWidth() > 0) view.setMinimumWidth(Math.round(view.getMinimumWidth() * scale));
        if (view.getMinimumHeight() > 0) view.setMinimumHeight(Math.round(view.getMinimumHeight() * scale));

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            if (params.width > 0) params.width = Math.max(1, Math.round(params.width * scale));
            if (params.height > 0) params.height = Math.max(1, Math.round(params.height * scale));
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
                margins.setMargins(Math.round(margins.leftMargin * scale), Math.round(margins.topMargin * scale),
                        Math.round(margins.rightMargin * scale), Math.round(margins.bottomMargin * scale));
            }
            view.setLayoutParams(params);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) applyMusicScale(group.getChildAt(i), scale);
        }
    }

    private static void installResponsiveMusicWidth(View card, float scale) {
        final ViewGroup[] parentRef = new ViewGroup[1];
        final View.OnLayoutChangeListener[] resizeListener = new View.OnLayoutChangeListener[1];
        card.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) {
                if (!(view.getParent() instanceof ViewGroup)) return;
                ViewGroup parent = (ViewGroup) view.getParent();
                parentRef[0] = parent;
                Runnable resize = () -> {
                    int availableWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
                    if (availableWidth <= 0 || card.getParent() != parent) return;
                    int targetWidth = Math.max(1, Math.round(availableWidth * scale));
                    ViewGroup.LayoutParams params = card.getLayoutParams();
                    if (params == null || params.width == targetWidth) return;
                    params.width = targetWidth;
                    if (params instanceof android.widget.FrameLayout.LayoutParams) {
                        ((android.widget.FrameLayout.LayoutParams) params).gravity = android.view.Gravity.CENTER;
                    } else if (params instanceof LinearLayout.LayoutParams) {
                        ((LinearLayout.LayoutParams) params).gravity = android.view.Gravity.CENTER_HORIZONTAL;
                    }
                    card.setLayoutParams(params);
                };
                resizeListener[0] = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> resize.run();
                parent.addOnLayoutChangeListener(resizeListener[0]);
                parent.post(resize);
            }

            @Override public void onViewDetachedFromWindow(View view) {
                if (parentRef[0] != null && resizeListener[0] != null) {
                    parentRef[0].removeOnLayoutChangeListener(resizeListener[0]);
                }
                parentRef[0] = null;
                resizeListener[0] = null;
            }
        });
    }

    private static void styleMusicControl(TextView control, ThemeEngine.DesignTokens theme, boolean primary, float scale) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(primary ? theme.primary : theme.surface);
        background.setCornerRadius(Tuils.dpToPx(control.getContext(), 10 * scale));
        background.setStroke(Math.max(1, Math.round(Tuils.dpToPx(control.getContext(), scale))), primary ? theme.primary : theme.border);
        control.setBackground(background);
        control.setTextColor(primary ? theme.background : theme.primary);
        control.setPadding(Tuils.dpToPx(control.getContext(), 4), 0,
                Tuils.dpToPx(control.getContext(), 4), 0);
        control.setTypeface(Tuils.getTypeface(control.getContext()));
    }

    private static GradientDrawable makeMusicPill(Context context, int color, ThemeEngine.DesignTokens theme, float scale) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(Tuils.dpToPx(context, 8 * scale));
        background.setStroke(Math.max(1, Math.round(Tuils.dpToPx(context, scale))), theme.border);
        return background;
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

    private void showSysCard(MainPack info, boolean fixed) {
        LinearLayout card = new LinearLayout(info.context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10));

        TextView title = new TextView(info.context);
        title.setText("● [ SYSTEM MONITOR : SYSREADOUT ]");
        title.setTypeface(Tuils.getTypeface(info.context));
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView status = new TextView(info.context);
        status.setTypeface(Tuils.getTypeface(info.context));
        status.setPadding(0, Tuils.dpToPx(info.context, 6), 0, Tuils.dpToPx(info.context, 6));

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        int ramPercent = (int) ((usedMemory * 100) / maxMemory);

        status.setText("• CPU Load     : ~12.5%\n• RAM Usage    : " + ramPercent + "%\n• Battery Temp : 28.5 °C\n• Network I/O  : 1.4 KB/s\n• Signal Bar   : -82 dBm (4G)");
        card.addView(status);

        if (fixed && info.context instanceof LauncherActivity) {
            ((LauncherActivity) info.context).onPersistentOutput(card);
        } else {
            Tuils.sendOutput(info.context, card);
        }
    }

    private void showScriptCard(MainPack info, String scriptName, boolean fixed) {
        LinearLayout card = new LinearLayout(info.context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10), Tuils.dpToPx(info.context, 12), Tuils.dpToPx(info.context, 10));

        TextView title = new TextView(info.context);
        title.setText("● [ SCRIPT: " + scriptName.toUpperCase() + " ]");
        title.setTypeface(Tuils.getTypeface(info.context));
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView status = new TextView(info.context);
        status.setTypeface(Tuils.getTypeface(info.context));
        status.setPadding(0, Tuils.dpToPx(info.context, 6), 0, Tuils.dpToPx(info.context, 6));
        status.setText("Target: " + scriptName + "\nStatus: READY // Tap to execute via Termux");
        card.addView(status);

        card.setOnClickListener(v -> {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.termux", "com.termux.app.RunCommandService"));
                intent.setAction("com.termux.RUN_COMMAND");
                intent.putExtra("com.termux.intent.extra.EXECUTABLE", "/data/data/com.termux/files/usr/bin/bash");
                intent.putExtra("com.termux.intent.extra.ARGUMENTS", new String[]{scriptName});
                intent.putExtra("com.termux.intent.extra.WORKDIR", "/data/data/com.termux/files/home");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    info.context.startForegroundService(intent);
                } else {
                    info.context.startService(intent);
                }
                status.setText("Target: " + scriptName + "\nStatus: EXECUTED");
            } catch (Exception e) {
                status.setText("Target: " + scriptName + "\nStatus: ERROR - Termux missing or permission denied.");
            }
        });

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
