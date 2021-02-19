package com.chimediaplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PlayerService extends Service {

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

    private MediaPlayer mMediaPlayer;
    private LocalBroadcastManager mLocalBroadcastManager;

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
        mMediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.dance_macabre);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case PLAY_ACTION: {
                mMediaPlayer.start();
                Intent sendDurationIntent = new Intent(ACTION_SEND_TRACK_DURATION);
                sendDurationIntent.putExtra(DURATION_EXTRA, mMediaPlayer.getDuration());
                mLocalBroadcastManager.sendBroadcast(sendDurationIntent);
                Intent sendPositionIntent = new Intent(ACTION_SEND_TRACK_POSITION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (mMediaPlayer.isPlaying()) {
                            sendPositionIntent.putExtra(POSITION_EXTRA, mMediaPlayer.getCurrentPosition());
                            mLocalBroadcastManager.sendBroadcast(sendPositionIntent);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;
            }
            case PAUSE_ACTION: {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                break;
            }
            case STOP_ACTION: {
                mMediaPlayer.stop();
                mMediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.dance_macabre);
                break;
            }
            case NEXT_ACTION: {
                break;
            }
            case PREVIOUS_ACTION: {
                break;
            }
            case SEEK_TO_ACTION: {
                mMediaPlayer.seekTo(intent.getIntExtra(SEEK_TO_EXTRA, 0));
                break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        Log.d(TAG, "onDestroy: ");
    }

    public static Intent seekToIntent(Context context, int seekTo) {
        Intent seekToIntent = new Intent(context, PlayerService.class);
        seekToIntent.setAction(SEEK_TO_ACTION);
        seekToIntent.putExtra(SEEK_TO_EXTRA, seekTo);
        return seekToIntent;
    }
}
