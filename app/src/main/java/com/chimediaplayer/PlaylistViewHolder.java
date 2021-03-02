package com.chimediaplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    private TextView mSongTextView;
    private ImageButton mDeleteImageButton;

    private Callback mCallback;

    public PlaylistViewHolder(@NonNull View itemView, Callback callback) {
        super(itemView);
        mCallback = callback;
        mSongTextView = itemView.findViewById(R.id.songTextView);
        mDeleteImageButton = itemView.findViewById(R.id.deleteImageButton);
        mDeleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.deleteClick(getAdapterPosition());
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.viewClick(getAdapterPosition());
            }
        });
    }

    public void onBind(String song) {
        mSongTextView.setText(song);
    }

    public static PlaylistViewHolder create(ViewGroup parent, Callback callback) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_playlist_item, parent, false);
        return new PlaylistViewHolder(view, callback);
    }

    public interface Callback {
        public void deleteClick(int position);

        public void viewClick(int position);
    }
}
