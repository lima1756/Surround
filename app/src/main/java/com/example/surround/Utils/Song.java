package com.example.surround.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Song implements Parcelable {
    private String id;
    private String imageRes;
    private String title, artist, path = "";
    private int duration;

    public Song(String ID, String img, String tit, String art, int dur){
        this.id = ID;
        this.imageRes = img;
        this.title = tit;
        this.artist = art;
        this.duration = dur;
    }

    private Song() {

    }

    private Song(Parcel in) {
        id = in.readString();
        imageRes = in.readString();
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

    public static final Song CreateFromJSON(JSONObject jsonSong) throws JSONException {
        Song s = new Song();
        s.imageRes = jsonSong.has("image")? jsonSong.getString("image"): "";
        s.duration = jsonSong.has("duration")? jsonSong.getInt("duration"): 100;
        s.id = jsonSong.getString("id");
        s.title = jsonSong.getString("name");
        s.artist = jsonSong.getString("artist");
        return s;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(imageRes);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeInt(duration);
    }

    public String getId() {
        return id;
    }

    public String getImageRes() {
        return imageRes;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public int getDuration() {
        return duration;
    }
}
