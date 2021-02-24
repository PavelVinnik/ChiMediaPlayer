package com.chimediaplayer;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment implements PlaylistViewHolder.Callback {

    private static final String TAG = "PlaylistFragment";

    private RecyclerView mRecyclerView;
    private PlaylistAdapter mAdapter;

    private List<Integer> mPlayList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPlayList = getRawIdList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mRecyclerView = view.findViewById(R.id.playListRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new PlaylistAdapter(this, mPlayList);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMenuItem: {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void deleteFromPlaylist(int position) {
        mPlayList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void holderClick(int position) {
        getContext().startService(PlayerService.playSongIntent(getContext(), mPlayList.get(position)));
    }
}
