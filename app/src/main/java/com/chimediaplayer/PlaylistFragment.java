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

    private PlaylistAdapter mAdapter;

    private BroadcastReceiver mPlaylistReceiver;

    private ArrayList<Song> mFragmentPlaylist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFragmentPlaylist = Song.generateSongArrayList();

        mPlaylistReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case PlayerService.BROADCAST_PLAYLIST_STATE: {
                        mFragmentPlaylist = new ArrayList<>(intent.getParcelableArrayListExtra(PlayerService.EXTRA_PLAYLIST));
                        mAdapter.submitList(mFragmentPlaylist);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        };
        updatePlaylist(mFragmentPlaylist);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.playListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PlaylistAdapter(new PlaylistAdapter.SongDiff(), this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.submitList(mFragmentPlaylist);

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
                mFragmentPlaylist.add(Song.getRandomSong());
                mAdapter.submitList(mFragmentPlaylist);
                updatePlaylist(mFragmentPlaylist);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlaylist(ArrayList<Song> playlistToService) {
        getContext().startService(PlayerService.getUpdatePlaylistIntent(getContext(), playlistToService));
    }

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void deleteClick(int position) {
        if (position != -1) {
            mFragmentPlaylist.remove(position);
            mAdapter.submitList(mFragmentPlaylist);
            updatePlaylist(mFragmentPlaylist);
        }
    }

    @Override
    public void viewClick(int position) {
        getContext().startService(PlayerService.getPlaySongIntent(getContext(), position));
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.BROADCAST_PLAYLIST_STATE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mPlaylistReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mPlaylistReceiver);
    }
}
