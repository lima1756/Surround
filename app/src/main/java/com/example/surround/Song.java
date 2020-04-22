package com.example.surround;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    public int id;
    public int imageRes;
    public String title, artist, path = "";
    public int duration;

    public Song(int ID, int img, String tit, String art, int dur){
        this.id = ID;
        this.imageRes = img;
        this.title = tit;
        this.artist = art;
        this.duration = dur;
    }

    protected Song(Parcel in) {
        id = in.readInt();
        imageRes = in.readInt();
        title = in.readString();
        artist = in.readString();
        duration = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(imageRes);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeInt(duration);
    }
}
