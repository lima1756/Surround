package com.example.surround.Controller;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.surround.Common.Song;
import com.example.surround.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {
    public Song currentSong;
    public ImageView songIcon, play, prev, next;
    public TextView songTitle, songArtist, songDuration, songTimeElapsed;
    public MediaPlayer mediaPlayer;
    public SeekBar songPlaying;
    public boolean wasPlaying = false;

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

        Glide.with(getContext())
                .load(currentSong.getImageRes())
                .placeholder(R.drawable.vinil)
                .fitCenter()
                .into(songIcon);
        songTitle.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());
        songDuration.setText(toMinutes(currentSong.getDuration()));



        songPlaying.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                songTimeElapsed.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress / 1000f);

                if (x < 10)
                    songTimeElapsed.setText("0:0" + x);
                else
                    songTimeElapsed.setText("0:" + x);

                double percent = progress / (double) seekBar.getMax();
                int offset = seekBar.getThumbOffset();
                int seekWidth = seekBar.getWidth();
                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                int labelWidth = songTimeElapsed.getWidth();
                songTimeElapsed.setX(offset + seekBar.getX() + val
                        - Math.round(percent * offset)
                        - Math.round(percent * labelWidth / 2));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    play.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));
                    songPlaying.setProgress(0);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }


        });
    }

    public void play(View view){
        try {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                songPlaying.setProgress(0);
                wasPlaying = true;
                play.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));
            }


            if (!wasPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                play.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_pause));

                AssetFileDescriptor descriptor = getContext().getAssets().openFd(currentSong.getPath());
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();

                mediaPlayer.prepare();
                mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setLooping(false);
                songPlaying.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
                new Thread((Runnable) this).start();

            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void previous(View view){

    }

    public void next(View view){

    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void run() {

        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();


        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            songPlaying.setProgress(currentPosition);

        }
    }

    public String toMinutes(int songDuration){

        int minutes = songDuration/60;
        int seconds = songDuration%60;

        if(seconds < 10) return minutes+":0"+seconds;

        return minutes+":"+seconds;
    }
}
