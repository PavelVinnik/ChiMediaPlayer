package com.chimediaplayer;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {

    private PlaylistViewHolder.Callback mCallback;

    private List<Integer> mPlayList;

    public PlaylistAdapter(PlaylistViewHolder.Callback callback, List<Integer> playList) {
        mCallback = callback;
        mPlayList = playList;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PlaylistViewHolder.create(parent, mCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.onBind(mPlayList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return mPlayList.size();
    }
}
