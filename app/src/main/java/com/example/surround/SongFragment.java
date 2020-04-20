package com.example.surround;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {
    public Song currentSong;
    public ImageView songIcon, play, prev, next;
    public TextView songTitle, songArtist, songDuration, songTimeElapsed;
    public MediaPlayer mediaPlayer;
    public SeekBar songPlaying;
    public boolean isPlaying = false;

    public SongFragment(Song song) { this.currentSong = song; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        songDuration = (TextView)view.findViewById(R.id.durationTV);
        songTimeElapsed = (TextView)view.findViewById(R.id.timeElapsedTV);
        songPlaying = (SeekBar)view.findViewById(R.id.songSB);

        //mediaPlayer = MediaPlayer.create(getContext(), );
        prev = (ImageView)view.findViewById(R.id.prevIV);
        next = (ImageView)view.findViewById(R.id.nextIV);
        play = (ImageView)view.findViewById(R.id.playIV);

        songIcon.setImageResource(currentSong.imageRes);
        songTitle.setText(currentSong.title);
        songArtist.setText(currentSong.artist);
        songDuration.setText(toMinutes(currentSong.duration));
    }

    public String toMinutes(int songDuration){

        int minutes = songDuration/60;
        int seconds = songDuration%60;

        if(seconds < 10) return minutes+":0"+seconds;

        return minutes+":"+seconds;
    }

    public void play(View view){

    }

    public void previous(View view){

    }

    public void next(View view){

    }
}
