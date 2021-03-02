package com.chimediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment implements PlaylistViewHolder.Callback {

    private static final String TAG = "PlaylistFragment";

    private static final String SHARED_PREF = "SharedPref";
    private static final String IS_FIRST_LAUNCH = "isFirstLaunch";

    private RecyclerView mRecyclerView;
    private PlaylistAdapter mAdapter;

    private ArrayList<Song> mFragmentPlayList;

    private BroadcastReceiver mPlaylistReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFragmentPlayList = Song.generateSongArrayList();

        mPlaylistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case PlayerService.BROADCAST_PLAYLIST_STATE: {
                        mFragmentPlayList = new ArrayList<>((ArrayList<Song>) intent.getSerializableExtra(PlayerService.EXTRA_PLAYLIST));
                        mAdapter.submitList(mFragmentPlayList);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        };
        updatePlaylist(mFragmentPlayList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mRecyclerView = view.findViewById(R.id.playListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PlaylistAdapter(new PlaylistAdapter.SongDiff(), this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.submitList(mFragmentPlayList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        getContext().startService(PlayerService.getActionIntent(getContext(), PlayerService.ACTION_BROADCAST_PLAYLIST_STATE));
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMenuItem: {
                mFragmentPlayList.add(Song.getRandomSong());
                mAdapter.submitList(mFragmentPlayList);
                updatePlaylist(mFragmentPlayList);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlaylist(ArrayList<Song> playlistToService) {
        getContext().startService(PlayerService.updatePlaylist(getContext(), playlistToService));
    }

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void deleteClick(int position) {
        if (position != -1) {
            mFragmentPlayList.remove(position);
            mAdapter.submitList(mFragmentPlayList);
            updatePlaylist(mFragmentPlayList);
        }
    }

    @Override
    public void viewClick(int position) {
        getContext().startService(PlayerService.playSongIntent(getContext(), position));
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.BROADCAST_PLAYLIST_STATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mPlaylistReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mPlaylistReceiver);
    }
}
