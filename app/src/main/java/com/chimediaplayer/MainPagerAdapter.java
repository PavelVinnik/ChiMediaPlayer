package com.chimediaplayer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public static final String TAG = "MainPagerAdapter";

    private static final int[] TAB_TITLES = new int[]{R.string.tab_1_text, R.string.tab_2_text};
    private final Context mContext;

    public MainPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
        super(fm, behavior);
        mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return PlayerFragment.newInstance();
            }
            case 1: {
                return PlaylistFragment.newInstance();
            }
            default: {
                return new Fragment();
            }
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}
