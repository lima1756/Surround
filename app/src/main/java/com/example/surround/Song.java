package com.example.surround;

import android.graphics.drawable.Icon;

public class Song {
    public Icon image;
    public String title, artist;
    public int duration;

    public Song(Icon img, String tit, String art, int dur){
        this.image = img;
        this.title = tit;
        this.artist = art;
        this.duration = dur;
    }
}
