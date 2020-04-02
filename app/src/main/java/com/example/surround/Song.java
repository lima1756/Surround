package com.example.surround;

import android.graphics.drawable.Icon;

public class Song {
    public int id;
    public int imageRes;
    public String title, artist;
    public int duration;

    public Song(int ID, int img, String tit, String art, int dur){
        this.id = ID;
        this.imageRes = img;
        this.title = tit;
        this.artist = art;
        this.duration = dur;
    }
}
