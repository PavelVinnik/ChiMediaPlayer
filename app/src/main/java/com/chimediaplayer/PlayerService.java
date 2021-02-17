package com.chimediaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class PlayerService extends Service {

    public static final String PLAY_ACTION = "playAction";
    public static final String PAUSE_ACTION = "pauseAction";
    public static final String STOP_ACTION = "stopAction";
    public static final String NEXT_ACTION = "nextAction";
    public static final String PREVIOUS_ACTION = "previousAction";

    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.dance_macabre);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case PLAY_ACTION: {
                mMediaPlayer.start();
                break;
            }
            case PAUSE_ACTION: {
                mMediaPlayer.pause();
                break;
            }
            case STOP_ACTION: {
                break;
            }
            case NEXT_ACTION: {
                break;
            }
            case PREVIOUS_ACTION: {
                break;
            }
        }
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
