package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
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

import com.example.surround.App.AppSocket;
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.MyEqualizer;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

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
    int typeOfSpeaker;
    //...............................

    ImageView ivStopBtn, ivDisk;
    MediaPlayer mp;
    TextView tvArtist, tvNameSong;
    boolean isPlaying, isReady;
    long lastTimestamp ;
    int lastMillis;

    MyEqualizer myEq;
    AppSocket app;
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
            sendMusicIsReadyToServer();
        }
    };

    //SOCKET-IO LISTENERS .........................................
    private Emitter.Listener socketOnSetMusic = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            String url, artistSong, nameSong;
            try {
                url = data.getString(Constants.SOCKET_PARAM_SONG_URL);
                artistSong = data.getString(Constants.SOCKET_PARAM_SONG_ARTIST);
                nameSong = data.getString(Constants.SOCKET_PARAM_SONG_NAME);

                SpeakerPlayingActivity.this.onSetMusic( url);
                SpeakerPlayingActivity.this.setSongMetadata(artistSong,nameSong);
            }catch (JSONException e){
                //TODO send error to socket?
                //TODO or send to slave?
                SpeakerPlayingActivity.this.setSongMetadata("No artist","Untitled");
            }
        }
    };

    private Emitter.Listener socketOnSetSpeaker = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            int tSpk;
            try {
                tSpk = data.getInt(Constants.SOCKET_PARAM_TYPE_SPEAKER);
                SpeakerPlayingActivity.this.onSetSpeaker( tSpk);
            }catch (JSONException e){
                //TODO send error to socket?
                //TODO or send to slave?
                SpeakerPlayingActivity.this.onSetSpeaker(Constants.EQUALIZER_CENTER_SPEAKER); //Default
            }
        }
    };

    private Emitter.Listener socketOnStopSong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            SpeakerPlayingActivity.this.onStopSong();
        }
    };

    private Emitter.Listener socketOnPlay = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            int millis;long timestamp;
            JSONObject data = (JSONObject) args[0];
            try{
                millis = data.getInt(Constants.SOCKET_PARAM_MILLIS_PLAY);
                timestamp = data.getLong(Constants.SOCKET_PARAM_TIMESTAMP_PLAY);
                SpeakerPlayingActivity.this.onPlayInMillisecond(timestamp,millis);
            }catch (JSONException e){
                //TODO send error to socket?
                //TODO or send to slave?
            }
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent i = new Intent(SpeakerPlayingActivity.this, MainActivity.class);
            startActivity(i);
            //TODO
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //TODO
        }
    };



    //.............................................................



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_playing);
        setLayoutComponents();
        setSongMetadata("Massive Attack", "Inertia Creeps");//TODO FROM SOCKETS
        isPlaying = false;
        isReady = false;
        typeOfSpeaker= Constants.EQUALIZER_FRONT_LEFT_SPEAKER;
        app = AppSocket.getInstance();

        //SOCKET LISTENERS --------------------------------------

        app.getSocket().on(Constants.SOCKET_ON_SET_SPEAKER, socketOnSetSpeaker);
        app.getSocket().on(Constants.SOCKET_ON_STOP_SONG, socketOnStopSong);
        app.getSocket().on(Constants.SOCKET_ON_SET_MUSIC, socketOnSetMusic);
        app.getSocket().on(Constants.SOCKET_ON_PLAY, socketOnPlay);
        app.getSocket().on(Socket.EVENT_DISCONNECT,onDisconnect);
        app.getSocket().on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        app.getSocket().on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

        //--------------------------------------------------------

        if(!app.getSocket().connected()){
            app.getSocket().connect();
        }
        tests();


    }


    private void tests(){
        String url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/Checkie_Brown/hey/Checkie_Brown_-_09_-_Mary_Roose_CB_36.mp3";
        onSetMusic(url);
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
    public void onSetSpeaker(int typeOfSpeaker){
        this.typeOfSpeaker = typeOfSpeaker;
        if(myEq==null){
            myEq = new MyEqualizer(null,this.typeOfSpeaker);
        }else{
            myEq.setTypeSpeaker(typeOfSpeaker);
        }
    }

    public void onStopSong(){
        if(mp != null){
            mp.stop(); //TODO decidir si podremos volvernos a conectar al server a media canción o no.

        }
        isPlaying = false;
    }

    public void onSetMusic(String url){
        Log.d("SPEAKER_PLAY", "on get m Is playing: "+ isPlaying + "is ready "+ isReady);
        if(isPlaying)onStopSong();
        if(isReady && currentSongUrl.equals(url)) return;
        isReady = false;
        currentSongUrl = url;

        mp = new MediaPlayer();

        if(myEq==null)myEq = new MyEqualizer(mp,typeOfSpeaker);
        else myEq.setMp(mp);

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(readyToPlay);
        try{
            mp.setDataSource(url);
            mp.prepareAsync(); // might take long! (for buffering, etc)
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
                    myEq.getEq().setEnabled(true);
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
        app.getSocket().disconnect();
        //TODO APP  remove ALL LISTENERS
        app.getSocket().off("onSetSpeaker", socketOnSetSpeaker);
        app.getSocket().off("onStopSong", socketOnStopSong);
        app.getSocket().off("onSetMusic", socketOnSetMusic);
        app.getSocket().off("onPlay", socketOnPlay);
        app.getSocket().off(Socket.EVENT_DISCONNECT,onDisconnect);
        app.getSocket().off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        app.getSocket().off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

    }

    public void sendMusicIsReadyToServer(){
        //TODO SOCKETS;
        app.getSocket().emit(Constants.SOCKET_EMIT_READY);
        //TEST ---------------------------
        //onPlayInMillisecond(System.currentTimeMillis()+20000, 30000);
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
