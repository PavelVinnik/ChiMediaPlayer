package com.chimediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlayerService";

    public static final String ACTION_PLAY = "playAction";
    public static final String ACTION_PAUSE = "pauseAction";
    public static final String ACTION_STOP = "stopAction";
    public static final String ACTION_NEXT = "nextAction";
    public static final String ACTION_PREVIOUS = "previousAction";
    public static final String ACTION_SEEK_TO = "seekToAction";
    public static final String ACTION_PLAY_SONG = "playSongAction";
    public static final String ACTION_UPDATE_PLAYLIST = "updatePlaylistAction";
    public static final String ACTION_REPEAT_BROADCAST_INFO = "repeatBroadcastInfo";
    public static final String ACTION_BROADCAST_PLAYLIST_STATE = "actionBroadcastPlaylistState";

    public static final String BROADCAST_TRACK_DURATION = "broadcastTrackDuration";
    public static final String BROADCAST_TRACK_POSITION = "broadcastTrackPosition";
    public static final String BROADCAST_PLAYLIST_STATE = "broadcastPlaylistState";

    public static final String EXTRA_DURATION = "durationExtra";
    public static final String EXTRA_POSITION = "positionExtra";
    public static final String EXTRA_PLAYLIST = "playlistExtra";

    private static final String EXTRA_SEEK_TO = "seekToExtra";
    private static final String EXTRA_SONG_POSITION = "songExtra";

    private static final int NOTIFICATION_ID = 2342;
    private static final String NOTIFICATION_CHANEL_ID = "myNotificationChannel";

    private MediaPlayer mMediaPlayer;
    private LocalBroadcastManager mLocalBroadcastManager;
    private broadcastPositionThread mBroadcastPositionThread;

    private ArrayList<Song> mServicePlaylist;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mMediaPlayer = null;
        mServicePlaylist = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY: {
                    startForeground(NOTIFICATION_ID, createNotification());
                    if (mServicePlaylist != null && !mServicePlaylist.isEmpty()) {
                        if (mMediaPlayer == null) {
                            mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
                            mMediaPlayer.setOnCompletionListener(this);
                        }
                        mMediaPlayer.start();
                        beginBroadcastInfo();
                    }
                    break;
                }
                case ACTION_PAUSE: {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                    break;
                }
                case ACTION_STOP: {
                    releaseAndNullMediaPlayer();
                    stopForeground(false);
                    break;
                }
                case ACTION_NEXT: {
                    if (mServicePlaylist.size() > 1) {
                        releaseAndNullMediaPlayer();
                        Song tempSong = mServicePlaylist.get(0);
                        mServicePlaylist.remove(0);
                        mServicePlaylist.add(tempSong);
                        mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
                        mMediaPlayer.setOnCompletionListener(this);
                        mMediaPlayer.start();
                        beginBroadcastInfo();
                        broadcastPlaylistState(mServicePlaylist);
                    }
                    break;
                }
                case ACTION_PREVIOUS: {
                    if (mServicePlaylist.size() > 1) {
                        releaseAndNullMediaPlayer();
                        mServicePlaylist.add(0, mServicePlaylist.get(mServicePlaylist.size() - 1));
                        mServicePlaylist.remove(mServicePlaylist.size() - 1);
                        mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
                        mMediaPlayer.setOnCompletionListener(this);
                        mMediaPlayer.start();
                        beginBroadcastInfo();
                        broadcastPlaylistState(mServicePlaylist);
                    }
                    break;
                }
                case ACTION_SEEK_TO: {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.seekTo(intent.getIntExtra(EXTRA_SEEK_TO, 0));
                    }
                    break;
                }
                case ACTION_PLAY_SONG: {
                    startForeground(NOTIFICATION_ID, createNotification());
                    releaseAndNullMediaPlayer();
                    int position = intent.getIntExtra(EXTRA_SONG_POSITION, -1);
                    if (position > 0) {
                        Collections.swap(mServicePlaylist, 0, position);
                        Song song = mServicePlaylist.get(position);
                        mServicePlaylist.remove(position);
                        mServicePlaylist.add(song);
                    }
                    mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
                    mMediaPlayer.setOnCompletionListener(this);
                    mMediaPlayer.start();
                    beginBroadcastInfo();
                    broadcastPlaylistState(mServicePlaylist);
                    break;
                }
                case ACTION_REPEAT_BROADCAST_INFO: {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        beginBroadcastInfo();
                    }
                    break;
                }
                case ACTION_BROADCAST_PLAYLIST_STATE: {
                    if (mServicePlaylist != null) {
                        broadcastPlaylistState(mServicePlaylist);
                    }
                    break;
                }
                case ACTION_UPDATE_PLAYLIST: {
                    ArrayList<Song> receivedPlaylist = intent.getParcelableArrayListExtra(EXTRA_PLAYLIST);
                    if (receivedPlaylist.isEmpty()) {
                        mServicePlaylist = receivedPlaylist;
                        releaseAndNullMediaPlayer();
                    }
                    if (mServicePlaylist == null) {
                        mServicePlaylist = receivedPlaylist;
                    }
                    if (!mServicePlaylist.isEmpty() && mServicePlaylist.get(0).getId() != receivedPlaylist.get(0).getId()) {
                        releaseAndNullMediaPlayer();
                        mServicePlaylist = receivedPlaylist;
                        mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
                        mMediaPlayer.setOnCompletionListener(this);
                        mMediaPlayer.start();
                        beginBroadcastInfo();
                    } else {
                        mServicePlaylist = receivedPlaylist;
                    }
                    broadcastPlaylistState(mServicePlaylist);
                    break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAndNullMediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        releaseAndNullMediaPlayer();
        if (mServicePlaylist.size() > 1) {
            Song tempSong = mServicePlaylist.get(0);
            mServicePlaylist.remove(0);
            mServicePlaylist.add(tempSong);
            mMediaPlayer = MediaPlayer.create(this, mServicePlaylist.get(0).getId());
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.start();
            beginBroadcastInfo();
            broadcastPlaylistState(mServicePlaylist);
        }
    }

    private void releaseAndNullMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            broadcastResetUi();
        }
    }

    private void broadcastDuration() {
        Intent sendDurationIntent = new Intent(BROADCAST_TRACK_DURATION);
        sendDurationIntent.putExtra(EXTRA_DURATION, mMediaPlayer.getDuration());
        mLocalBroadcastManager.sendBroadcast(sendDurationIntent);
    }

    private void broadcastPlaylistState(ArrayList<Song> playlistToFragment) {
        Intent playlistStateIntent = new Intent(BROADCAST_PLAYLIST_STATE);
        playlistStateIntent.putParcelableArrayListExtra(EXTRA_PLAYLIST, playlistToFragment);
        mLocalBroadcastManager.sendBroadcast(playlistStateIntent);
    }

    private void beginBroadcastInfo() {
        broadcastDuration();
        if (mBroadcastPositionThread == null || !mBroadcastPositionThread.isAlive()) {
            mBroadcastPositionThread = new broadcastPositionThread();
            mBroadcastPositionThread.start();
        }
    }

    private void broadcastResetUi() {
        Intent sendDurationIntent = new Intent(BROADCAST_TRACK_DURATION);
        sendDurationIntent.putExtra(EXTRA_DURATION, 0);
        mLocalBroadcastManager.sendBroadcast(sendDurationIntent);
        Intent sendPositionIntent = new Intent(BROADCAST_TRACK_POSITION);
        sendPositionIntent.putExtra(EXTRA_POSITION, 0);
        mLocalBroadcastManager.sendBroadcast(sendPositionIntent);
    }

    public static Intent getActionIntent(Context context, String action) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(action);
        return intent;
    }

    public static Intent getUpdatePlaylistIntent(Context context, ArrayList<Song> intentPlaylist) {
        Intent updatePlaylistIntent = new Intent(context, PlayerService.class);
        updatePlaylistIntent.setAction(ACTION_UPDATE_PLAYLIST);
        updatePlaylistIntent.putParcelableArrayListExtra(EXTRA_PLAYLIST, intentPlaylist);
        return updatePlaylistIntent;
    }

    public static Intent getSeekToIntent(Context context, int seekTo) {
        Intent seekToIntent = new Intent(context, PlayerService.class);
        seekToIntent.setAction(ACTION_SEEK_TO);
        seekToIntent.putExtra(EXTRA_SEEK_TO, seekTo);
        return seekToIntent;
    }

    public static Intent getPlaySongIntent(Context context, int position) {
        Intent playSongIntent = new Intent(context, PlayerService.class);
        playSongIntent.setAction(ACTION_PLAY_SONG);
        playSongIntent.putExtra(EXTRA_SONG_POSITION, position);
        return playSongIntent;
    }

    private Notification createNotification() {
        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.notification_small);
        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(ACTION_PLAY);
        Intent testIntent = new Intent(ACTION_PLAY, null, this, PlayerService.class);
        PendingIntent playPending = PendingIntent.getService(this, 32, testIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.playNotificationButton, playPending);

        Intent pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePending = PendingIntent.getService(this, 32, pauseIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.pauseNotificationButton, pausePending);

        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent previousPending = PendingIntent.getService(this, 32, previousIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.previousNotificationButton, previousPending);

        Intent stopIntent = new Intent(this, PlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 32, stopIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.stopNotificationButton, stopPending);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getService(this, 32, nextIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.nextNotificationButton, nextPending);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANEL_ID,
                    getResources().getString(R.string.notification_channel_text), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationView)
                .setAutoCancel(false)
                .build();
        return notification;
    }

    private class broadcastPositionThread extends Thread {

        @Override
        public void run() {
            Intent sendPositionIntent = new Intent(BROADCAST_TRACK_POSITION);
            try {
                while (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    sendPositionIntent.putExtra(EXTRA_POSITION, mMediaPlayer.getCurrentPosition());
                    mLocalBroadcastManager.sendBroadcast(sendPositionIntent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "run: ", e);
                mBroadcastPositionThread = null;
            }
        }
    }
}
