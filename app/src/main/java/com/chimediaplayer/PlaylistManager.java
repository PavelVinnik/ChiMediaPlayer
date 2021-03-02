package com.chimediaplayer;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

public class PlaylistManager {

    private static final String TAG = "PlaylistManager";

    private static PlaylistManager INSTANCE;

    private Context mContext;
    private ArrayList<Song> mPlayList;

    private PlaylistManager(Context appContext) {
        mContext = appContext;
    }

    public static PlaylistManager getInstance(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new PlaylistManager(application);
        }
        return INSTANCE;
    }

    public void setPlayList(ArrayList<Song> playList) {
        this.mPlayList = playList;
    }

    public void addSong(Song song) {
        if (mPlayList != null) {
            mPlayList.add(song);
        } else {
            mPlayList = new ArrayList<>();
            mPlayList.add(song);
        }
    }

    public void updatePlaylist() {
        mContext.startService(PlayerService.updatePlaylist(mContext, mPlayList));
    }

    public void requestPlaylistState() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_BROADCAST_PLAYLIST_STATE));
    }

    public void play() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_PLAY));
    }

    public void pause() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_PAUSE));
    }

    public void stop() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_STOP));
    }

    public void next() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_NEXT));
    }

    public void previous() {
        mContext.startService(PlayerService.getActionIntent(mContext, PlayerService.ACTION_PREVIOUS));
    }

    public void seekTo(int milliseconds) {
        mContext.startService(PlayerService.seekToIntent(mContext, milliseconds));
    }
}
