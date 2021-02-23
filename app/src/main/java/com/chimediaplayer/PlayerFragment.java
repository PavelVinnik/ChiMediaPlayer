package com.chimediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";

    private Intent mPlayerServiceIntent;

    private TextView mPositionTextView;
    private TextView mDurationTextView;
    private SeekBar mPlayerSeekBar;

    private BroadcastReceiver mPlayerReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerServiceIntent = new Intent(getContext(), PlayerService.class);
        mPlayerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case PlayerService.ACTION_BRODCAST_TRACK_DURATION: {
                        mPlayerSeekBar.setMax(intent.getIntExtra(PlayerService.DURATION_EXTRA, 0));
                        int duration = intent.getIntExtra(PlayerService.DURATION_EXTRA, 0);
                        mDurationTextView.setText(formatMills(duration));
                        break;
                    }
                    case PlayerService.ACTION_BRODCAST_TRACK_POSITION: {
                        mPlayerSeekBar.setProgress(intent.getIntExtra(PlayerService.POSITION_EXTRA, 0));
                        int position = intent.getIntExtra(PlayerService.POSITION_EXTRA, 0);
                        mPositionTextView.setText(formatMills(position));
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mPositionTextView = view.findViewById(R.id.positionTextView);
        mPositionTextView.setText(formatMills(0));
        mDurationTextView = view.findViewById(R.id.durationTextView);
        mDurationTextView.setText(formatMills(0));
        mPlayerSeekBar = view.findViewById(R.id.playerSeekBar);
        mPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPositionTextView.setText(formatMills(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                unregisterReceiver();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mPositionTextView.setText(formatMills(progress));
                getContext().startService(PlayerService.seekToIntent(getContext(), progress));
                registerReceiver();
            }
        });

        Button playButton = view.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerServiceIntent.setAction(PlayerService.PLAY_ACTION);
                getContext().startService(mPlayerServiceIntent);
            }
        });

        Button pauseButton = view.findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerServiceIntent.setAction(PlayerService.PAUSE_ACTION);
                getContext().startService(mPlayerServiceIntent);
            }
        });

        Button stopButton = view.findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerServiceIntent.setAction(PlayerService.STOP_ACTION);
                getContext().startService(mPlayerServiceIntent);
            }
        });

        Button nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerServiceIntent.setAction(PlayerService.NEXT_ACTION);
                getContext().startService(mPlayerServiceIntent);
            }
        });

        Button previousButton = view.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerServiceIntent.setAction(PlayerService.PREVIOUS_ACTION);
                getContext().startService(mPlayerServiceIntent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_BRODCAST_TRACK_DURATION);
        intentFilter.addAction(PlayerService.ACTION_BRODCAST_TRACK_POSITION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mPlayerReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mPlayerReceiver);
    }

    private String formatMills(int mills) {
        return String.format("%d : %d",
                TimeUnit.MILLISECONDS.toMinutes(mills),
                TimeUnit.MILLISECONDS.toSeconds(mills) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mills))
        );
    }

    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }
}
