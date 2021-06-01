package com.chimediaplayer;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;


public class PlaylistAdapter extends ListAdapter<Song, PlaylistViewHolder> {

    private PlaylistViewHolder.Callback mCallback;

    public PlaylistAdapter(DiffUtil.ItemCallback<Song> diffCallback, PlaylistViewHolder.Callback callback) {
        super(diffCallback);
        mCallback = callback;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PlaylistViewHolder.create(parent, mCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.onBind(getItem(position).getTitle());
    }

    static class SongDiff extends DiffUtil.ItemCallback<Song> {

        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }
    }
}
