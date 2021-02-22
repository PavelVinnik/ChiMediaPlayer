package com.chimediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "PlayerService";

    public static final String PLAY_ACTION = "playAction";
    public static final String PAUSE_ACTION = "pauseAction";
    public static final String STOP_ACTION = "stopAction";
    public static final String NEXT_ACTION = "nextAction";
    public static final String PREVIOUS_ACTION = "previousAction";
    public static final String SEEK_TO_ACTION = "seekToAction";

    public static final String ACTION_SEND_TRACK_DURATION = "actionSendTrackDuration";
    public static final String ACTION_SEND_TRACK_POSITION = "actionSendTrackPosition";

    public static final String DURATION_EXTRA = "durationExtra";
    public static final String POSITION_EXTRA = "positionExtra";

    private static final String SEEK_TO_EXTRA = "seekToExtra";

    private static final String NOTIFICATION_CHANEL_ID = "myNotificationChannel";

    private MediaPlayer mMediaPlayer;
    private LocalBroadcastManager mLocalBroadcastManager;
    private List<Integer> mPlayList;
    private int mCurrentTrackIndex;
    private PositionUpdateThread mPositionUpdateThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mCurrentTrackIndex = 0;
        mPlayList = getRawIdList();
        mMediaPlayer = MediaPlayer.create(getBaseContext(), mPlayList.get(mCurrentTrackIndex));
        mMediaPlayer.setOnCompletionListener(this);
        mPositionUpdateThread = new PositionUpdateThread();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case PLAY_ACTION: {
                    if (!mMediaPlayer.isPlaying()) {
                        startForeground(startId, createNotification());
                        mMediaPlayer.start();
                        sendDuration();
                        if (!mPositionUpdateThread.isAlive()) {
                            mPositionUpdateThread = new PositionUpdateThread();
                            mPositionUpdateThread.start();
                        }
                    }
                    break;
                }
                case PAUSE_ACTION: {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                    break;
                }
                case STOP_ACTION: {
                    mMediaPlayer.release();
                    mCurrentTrackIndex = 0;
                    mMediaPlayer = MediaPlayer.create(getBaseContext(), mPlayList.get(mCurrentTrackIndex));
                    resetUi();
                    break;
                }
                case NEXT_ACTION: {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        if (mCurrentTrackIndex + 1 < mPlayList.size()) {
                            mMediaPlayer.release();
                            mCurrentTrackIndex++;
                            resetUi();
                            mMediaPlayer = MediaPlayer.create(getBaseContext(), mPlayList.get(mCurrentTrackIndex));
                            mMediaPlayer.start();
                            mMediaPlayer.setOnCompletionListener(this);
                            sendDuration();
                        }
                    }
                    break;
                }
                case PREVIOUS_ACTION: {
                    if (mCurrentTrackIndex - 1 >= 0) {
                        mMediaPlayer.release();
                        mCurrentTrackIndex--;
                        resetUi();
                        mMediaPlayer = MediaPlayer.create(getBaseContext(), mPlayList.get(mCurrentTrackIndex));
                        mMediaPlayer.start();
                        mMediaPlayer.setOnCompletionListener(this);
                        sendDuration();
                    }
                    break;
                }
                case SEEK_TO_ACTION: {
                    mMediaPlayer.seekTo(intent.getIntExtra(SEEK_TO_EXTRA, 0));
                    break;
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        resetUi();
        mCurrentTrackIndex++;
        if (mCurrentTrackIndex >= mPlayList.size()) {
            mCurrentTrackIndex = 0;
        }
        mMediaPlayer = MediaPlayer.create(getBaseContext(), mPlayList.get(mCurrentTrackIndex));
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.start();
        sendDuration();
    }

    private void sendDuration() {
        Intent sendDurationIntent = new Intent(ACTION_SEND_TRACK_DURATION);
        sendDurationIntent.putExtra(DURATION_EXTRA, mMediaPlayer.getDuration());
        mLocalBroadcastManager.sendBroadcast(sendDurationIntent);
    }

    private void resetUi() {
        Intent sendDurationIntent = new Intent(ACTION_SEND_TRACK_DURATION);
        sendDurationIntent.putExtra(DURATION_EXTRA, 0);
        mLocalBroadcastManager.sendBroadcast(sendDurationIntent);
        Intent sendPositionIntent = new Intent(ACTION_SEND_TRACK_POSITION);
        sendPositionIntent.putExtra(POSITION_EXTRA, 0);
        mLocalBroadcastManager.sendBroadcast(sendPositionIntent);
    }

    private List<Integer> getRawIdList() {
        Field[] field = R.raw.class.getDeclaredFields();
        List<Integer> rawIdList = new ArrayList<>();
        for (Field item : field) {
            try {
                rawIdList.add(item.getInt(item));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return rawIdList;
    }

    public static Intent seekToIntent(Context context, int seekTo) {
        Intent seekToIntent = new Intent(context, PlayerService.class);
        seekToIntent.setAction(SEEK_TO_ACTION);
        seekToIntent.putExtra(SEEK_TO_EXTRA, seekTo);
        return seekToIntent;
    }

    private Notification createNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANEL_ID,
                    getResources().getString(R.string.notification_channel_text), NotificationManager.IMPORTANCE_NONE);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("dsfd")
                .setContentText("dfs");
        return builder.build();
    }

    private class PositionUpdateThread extends Thread {

        @Override
        public void run() {
            Intent sendPositionIntent = new Intent(ACTION_SEND_TRACK_POSITION);
            while (mPlayList != null && mMediaPlayer.isPlaying()) {
                sendPositionIntent.putExtra(POSITION_EXTRA, mMediaPlayer.getCurrentPosition());
                mLocalBroadcastManager.sendBroadcast(sendPositionIntent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
