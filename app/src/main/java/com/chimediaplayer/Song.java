package com.chimediaplayer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Song implements Serializable {

    int mId;
    String mTitle;

    public Song(int id, String title) {
        this.mId = id;
        this.mTitle = title;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public static ArrayList<Song> generateSongArrayList() {
        Field[] field = R.raw.class.getDeclaredFields();
        ArrayList<Song> songList = new ArrayList<>();
        for (Field item : field) {
            try {
                songList.add(new Song(item.getInt(item), item.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return songList;
    }

    public static Song getRandomSong() {
        Song song;
        Field[] field = R.raw.class.getDeclaredFields();
        int i = (int) (field.length * Math.random());
        try {
            song = new Song(field[i].getInt(field[i]), field[i].getName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            song = new Song(0, "Bad song");
        }
        return song;
    }
}
