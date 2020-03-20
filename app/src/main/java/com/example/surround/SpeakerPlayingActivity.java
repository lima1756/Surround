package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class SpeakerPlayingActivity extends AppCompatActivity {

    //TODO THREADS ON PLAY, ON PLAY MILISECONDS, TO GET SYNC.
    public static final int ERR_NOT_ABLE_PREPARE_SONG = 2;
    public static final int ERR_NOT_INIT_MEDIA_PLAYER = 1;
    public static final int ERR_ASYNC_PLAY = 3;


    public static final int SLEEP_TIME = 50; //milliseconds.
    //Minimum async delta = 2 * SLEEP_TIME
    //SONG PARAM .............................
    String currentSongUrl;
    // hash number music  ?
    String artistSong, nameSong;
    //...............................

    ImageView ivStopBtn, ivDisk;
    MediaPlayer mp;
    TextView tvArtist, tvNameSong;
    boolean isPlaying, isReady;
    long lastTimestamp ;
    int lastMillis;

    View.OnClickListener onStopBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setVisibility(View.GONE);
            //TODO ADD PLAY BTN?
            onStopSong();
        }
    };


    MediaPlayer.OnPreparedListener readyToPlay = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            isReady = true;
            Log.d("SPEAKER_PLAY", "IS READY");
            sendMusicIsReadyToServer();
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_playing);
        setLayoutComponents();
        setSongMetadata("Massive Attack", "Inertia Creeps");//TODO FROM SOCKETS
        isPlaying = false;
        isReady = false;
        tests();


    }


    private void tests(){
        String url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Checkie_Brown/hey/Checkie_Brown_-_09_-_Mary_Roose_CB_36.mp3";
        onGetMusic(url);
    }

    public void setSongMetadata(String artist, String nameSong){
        tvArtist.setText(artist);
        tvNameSong.setText(nameSong);
        this.nameSong = nameSong;
        this.artistSong = artist;
    }

    private void setLayoutComponents(){

        ivStopBtn  = findViewById(R.id.iv_stop);
        ivStopBtn.setOnClickListener(onStopBtn);
        tvArtist = findViewById(R.id.tv_artist_song);
        tvNameSong = findViewById(R.id.tv_name_song);

        ivDisk = findViewById(R.id.iv_disc);


    }

    private void releaseMediaPlayer(){
        if (mp!=null){
            mp.release();
            mp = null;
        }
    }

    //SOCKETS-LISTENERS -----------------------------------
    public void onStopSong(){
        if(mp != null){
            mp.stop(); //TODO decidir si podremos volvernos a conectar al server a media canción o no.

        }
        isPlaying = false;
    }



    public void onGetMusic(String url){
        Log.d("SPEAKER_PLAY", "on get m Is playing: "+ isPlaying + "is ready "+ isReady);
        if(isPlaying){
            onStopSong();
        }
        if(isReady && currentSongUrl.equals(url)) return;
        isReady = false;
        currentSongUrl = url;
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(readyToPlay);
        try{
            mp.setDataSource(url);
            mp.prepare(); // might take long! (for buffering, etc)


        }catch (IOException e){
            Log.e("SPEAKER_PLAY", e.getMessage()+"hola");
            sendServerError(ERR_NOT_ABLE_PREPARE_SONG,"ERR_NOT_ABLE_PREPARE_SONG");
        }
    }

    public void onPlay(long timestamp) {
        onPlayInMillisecond(timestamp, 0);
    }

    public void onPlayInMillisecond(long timestamp, int milis) {
        this.lastTimestamp = timestamp;
        this.lastMillis = milis;

        if (mp == null) {
            sendServerError(ERR_NOT_INIT_MEDIA_PLAYER, "ERR_NOT_INIT_MEDIA_PLAYER");
            return;
        }
        if(!isReady){
            sendServerError(ERR_NOT_ABLE_PREPARE_SONG, "ERR_NOT_ABLE_PREPARE_SONG");
            return;
        }
        Log.d("SPEAKER_PLAY", "HOLA");
        startSong.start();
    }

    Thread startSong = new Thread(){
        public void run(){
            boolean done= false;
            while(!done){
                long now = System.currentTimeMillis() ; //TODO agregar factor de correccion según server?
                if(SpeakerPlayingActivity.this.lastTimestamp <= now ){
                    mp.seekTo(SpeakerPlayingActivity.this.lastMillis);
                    mp.start();
                    done = true;
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                    Log.d("","+"+SpeakerPlayingActivity.this.lastTimestamp+" ?>= "+now);
                }catch (Exception e){
                    sendServerError(ERR_ASYNC_PLAY,"ERR_ASYNC_PLAY");
                    mp.seekTo(SpeakerPlayingActivity.this.lastMillis);
                    mp.start();
                    done = true;
                }
            }
        }
    };



    //SOCKETS-SEND ------------------------------------------

    private void sendDisconnect(){
        releaseMediaPlayer();
        //TODO SOCKETS
    }

    public void sendMusicIsReadyToServer(){
        //TODO SOCKETS;
        //TEST ---------------------------
        onPlayInMillisecond(System.currentTimeMillis()+20000, 30000);
        //--------------------------------
    }

    public void sendServerError( int errCode,String err){
        //TODO SOCKETS
        Log.e("SPEAKER_PLAY",err);
    }

    // -----------------------------------------------

    @Override
    public void onBackPressed(){
        sendDisconnect();
        super.onBackPressed();
    }
}
