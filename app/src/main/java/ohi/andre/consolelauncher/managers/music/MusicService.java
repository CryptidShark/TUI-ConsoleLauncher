package ohi.andre.consolelauncher.managers.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.tuils.PrivateIOReceiver;
import ohi.andre.consolelauncher.tuils.PublicIOReceiver;
import ohi.andre.consolelauncher.tuils.Tuils;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final int NOTIFY_ID=100001;

    private MediaPlayer player;
    private List<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = Tuils.EMPTYSTRING;
    private boolean shuffle=false;

    private long lastNotificationChange;

//    do not touch the song playback from here

    public void onCreate(){
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();

        lastNotificationChange = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(System.currentTimeMillis() - lastNotificationChange < 500 || songTitle == null || songTitle.length() == 0) return super.onStartCommand(intent, flags, startId);

        lastNotificationChange = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFY_ID, buildNotification(this.getApplicationContext(), songTitle), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFY_ID, buildNotification(this.getApplicationContext(), songTitle));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(songTitle == null || songTitle.length() == 0) return;

        lastNotificationChange = System.currentTimeMillis();

        mp.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFY_ID, buildNotification(this.getApplicationContext(), songTitle), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFY_ID, buildNotification(this.getApplicationContext(), songTitle));
        }
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(List<Song> theSongs) {
        songs = theSongs;
        if(shuffle) Collections.shuffle(songs);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    public String playSong(){
        if (player == null || songs == null || songs.isEmpty() || songPosn < 0 || songPosn >= songs.size()) {
            return getString(R.string.no_songs);
        }
        try {
            player.reset();
        } catch (Exception e) {
//            no need to log this error, as this will occur everytime
            Tuils.log(e);
        }

        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();

        long id = playSong.getID();
        if(id == -1) {
            String path = playSong.getPath();
            try {
                player.setDataSource(path);
            } catch (IOException e) {
                Tuils.log(e);
                Tuils.toFile(e);
                return null;
            }
        } else {
            long currSong = playSong.getID();
            Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
            try {
                player.setDataSource(getApplicationContext(), trackUri);
            }
            catch(Exception e) {
                Tuils.log(e);
                Tuils.toFile(e);
                return null;
            }
        }
        player.prepareAsync();

        return playSong.getTitle();
    }

    public void setSong(int songIndex){
        if (songs != null && songIndex >= 0 && songIndex < songs.size()) songPosn = songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player != null) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        try { mp.reset(); } catch (Exception ignored) {}
        return false;
    }

    public static Notification buildNotification(Context context, String songTitle) {
        String channelId = "music_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Music", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent notIntent = new Intent(context, LauncherActivity.class);
        PendingIntent pendInt = PendingIntent.getActivity(context, 0, notIntent, Tuils.pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));

        Notification not;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String label = "cmd";
            RemoteInput remoteInput = new RemoteInput.Builder(PrivateIOReceiver.TEXT)
                    .setLabel(label)
                    .build();

            Intent i = new Intent(PublicIOReceiver.ACTION_CMD);
            i.putExtra(MainManager.MUSIC_SERVICE, true);

            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, label,
                    PendingIntent.getBroadcast(context.getApplicationContext(), 10, i, Tuils.pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)))
                    .addRemoteInput(remoteInput)
                    .build();

            builder.addAction(action);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) not = builder.build();
        else not = builder.getNotification();

        return not;
    }

    public int getPosn(){
        try { return player == null ? 0 : player.getCurrentPosition(); }
        catch (Exception ignored) { return 0; }
    }

    public int getDur(){
        try { return player == null ? 0 : player.getDuration(); }
        catch (Exception ignored) { return 0; }
    }

    public boolean isPng(){
        try { return player != null && player.isPlaying(); }
        catch (Exception ignored) { return false; }
    }

    public void pausePlayer(){
        try { if (player != null && player.isPlaying()) player.pause(); }
        catch (Exception ignored) {}
    }

    public void stop() {
        try {
            if (player != null) player.stop();
        } catch (Exception e) {}

        try {
            if (player != null) player.release();
        } catch (Exception e) {}
        player = null;

        setSong(0);
    }

    public void playPlayer() {
        try { if (player != null) player.start(); }
        catch (Exception ignored) {}
    }

    public void seek(int posn){
        try { if (player != null) player.seekTo(posn); }
        catch (Exception ignored) {}
    }

    public void go(){
        try { if (player != null) player.start(); }
        catch (Exception ignored) {}
    }

    public String playPrev(){
        if(songs == null || songs.isEmpty()) return getString(R.string.no_songs);
        songPosn = previous();
        return playSong();
    }

    public String playNext() {
        if(songs == null || songs.isEmpty()) return getString(R.string.no_songs);
        songPosn = next();
        return playSong();
    }

    private int next() {
        int pos = songPosn + 1;
        if(pos == songs.size()) pos = 0;
        return pos;
    }

    private int previous() {
        int pos = songPosn - 1;
        if(pos < 0) pos = songs.size() - 1;
        return pos;
    }

    public int getSongIndex() {
        return songPosn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null) {
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }
        if (songs != null) songs.clear();

        stopForeground(true);
    }

    public void setShuffle(boolean shuffle){
        this.shuffle = shuffle;
    }

}
