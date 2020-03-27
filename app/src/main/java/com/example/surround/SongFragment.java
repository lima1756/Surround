package com.example.surround;

import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {
    public Song currentSong;
    public ImageView songIcon, play, prev, next;
    public TextView songTitle, songArtist, songDuration;
    public MediaPlayer mediaPlayer;

    public SongFragment(Song song) {
        this.currentSong = song;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songIcon = (ImageView)view.findViewById(R.id.songImageIV);
        songTitle = (TextView)view.findViewById(R.id.songTitleTV);
        songArtist = (TextView)view.findViewById(R.id.songArtistTV);
        //songDuration = (TextView)view.findViewById(R.id.);
        //mediaPlayer = MediaPlayer.create(getContext(), );

        songIcon.setImageIcon(currentSong.image);
        songTitle.setText(currentSong.title);
        songArtist.setText(currentSong.artist);
        //songDuration.setText(currentSong.duration);
    }

    public void play(View view){

    }

    public void previous(View view){

    }

    public void next(View view){

    }
}
