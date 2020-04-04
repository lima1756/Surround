package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.surround.App.AppSocket;
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.MyEqualizer;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SpeakerPlayingActivity extends AppCompatActivity {

    public static final int ERR_NOT_ABLE_PREPARE_SONG = 2;
    public static final int ERR_NOT_INIT_MEDIA_PLAYER = 1;
    public static final int ERR_ASYNC_PLAY = 3;


    public static final int SLEEP_TIME = 50; //milliseconds.
    //Minimum async delta = 2 * SLEEP_TIME

    //SONG PARAM .............................
    String currentSongId, artistSong, nameSong;
    //...............................

    ImageView ivStopBtn, ivDisk, ivWait;
    Button btnTestStart;
    MediaPlayer mp;
    TextView tvArtist, tvNameSong, tvWait;
    boolean isPlaying, isReady;
    long lastTimestamp ;
    int lastMillis;
    MyEqualizer myEq;
    AppSocket app;

    /*---------------------- TEST------------------------
    View.OnClickListener testStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // For test propouses.
            tests();
        }
    };
    ---------------------------------------------------*/

    //-------------------- View listeners
    View.OnClickListener onStopBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setVisibility(View.GONE);
            //TODO ADD PLAY BTN?
            onStopSong();
        }
    };

    // --------------------MediaPlayer Listeners
    MediaPlayer.OnPreparedListener readyToPlay = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            isReady = true;
            sendMusicIsReadyToServer();
        }
    };

    // TODO (@lima1756): revisar que catches se envian al servidor como una solicitud para reintentar (cuales llevan contador) y cuales solo se informa al usuario
    //SOCKET-IO LISTENERS .........................................
    private Emitter.Listener socketOnSetMusic = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            final String id, artistSong, nameSong;
            SpeakerPlayingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() { setLayoutComponentsPlay(); //Cambiar controles
                }
            });
            try {
                id = data.getString(Constants.SOCKET_PARAM_SONG_ID);
                artistSong = data.getString(Constants.SOCKET_PARAM_SONG_ARTIST);
                nameSong = data.getString(Constants.SOCKET_PARAM_SONG_NAME);

                SpeakerPlayingActivity.this.onSetMusic( id);
                SpeakerPlayingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SpeakerPlayingActivity.this.setSongMetadata(artistSong, nameSong); //Cambiar controles
                    }
                });
            }catch (JSONException e){
                // TODO: send an error to the server requesting again for the data to prepare agregar un contador de un maximo numero de intentos
                SpeakerPlayingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SpeakerPlayingActivity.this.setSongMetadata("No artist","Untitled"); //Cambiar controles
                    }
                });
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
                //TODO send error to server, requesting again for the data
                SpeakerPlayingActivity.this.onSetSpeaker(Constants.EQUALIZER_CENTER_SPEAKER); //Default
            }
        }
    };

    private Emitter.Listener socketOnStopSong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) { SpeakerPlayingActivity.this.onStopSong(); }
    };

    private Emitter.Listener socketOnPlay = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            int millis;long timestamp;
            Log.d("ON_PLAY", "got signal from server");
            JSONObject data = (JSONObject) args[0];
            try{
                millis = data.getInt(Constants.SOCKET_PARAM_MILLIS_PLAY);
                timestamp = data.getLong(Constants.SOCKET_PARAM_TIMESTAMP_PLAY);
                Log.d("ON_PLAY", "timestamp:"+ timestamp);
                SpeakerPlayingActivity.this.onPlayInMillisecond(timestamp,millis);
            }catch (JSONException e){
                Log.d("PLAY", "error on play");
                Log.d("PLAY", e.getMessage());
                //TODO send error to socket? or send to slave?
            }
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent i = new Intent(SpeakerPlayingActivity.this, MainActivity.class);
            startActivity(i);
            Toast.makeText(getApplicationContext(),"Connection was lost.",Toast.LENGTH_LONG);
        }
    };




    //.............................................................


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_playing);
        setLayoutComponentsInit();
        isPlaying = false;
        isReady = false;

        app = AppSocket.getInstance();
        //SOCKET LISTENERS --------------------------------------

        app.getSocket().on(Constants.SOCKET_ON_SET_SPEAKER, socketOnSetSpeaker);
        app.getSocket().on(Constants.SOCKET_ON_STOP_SONG, socketOnStopSong);
        app.getSocket().on(Constants.SOCKET_ON_SET_MUSIC, socketOnSetMusic);
        app.getSocket().on(Constants.SOCKET_ON_PLAY, socketOnPlay);
        app.getSocket().on(Socket.EVENT_DISCONNECT,onDisconnect);

        //--------------------------------------------------------

        app.getSocket().connect();

        //---------------------------TEST--------------------------------
        /*isReady= true;
        btnTestStart = findViewById(R.id.btn_test_start);
        btnTestStart.setOnClickListener(testStart*/
        //---------------------------------------------------------------


    }


    private void tests(){
        Log.d("SPEAKER_PLAY", ""+app.getSocket().connected());
        String url = Constants.SERVER_URL + Constants.SERVER_GET_MUSIC_URL + "101";
        Log.d("SPEAKER_PLAY", url);
        onSetMusic("101");
    }

    public void setSongMetadata(String artist, String nameSong){
        tvArtist.setText(artist);
        tvNameSong.setText(nameSong);
        this.nameSong = nameSong;
        this.artistSong = artist;
    }

    public void setLayoutComponentsPlay(){
        //WAITING
        ivWait.setVisibility(View.GONE);
        tvWait.setVisibility(View.GONE);
        //PLAYING
        ivStopBtn.setVisibility(View.VISIBLE);
        tvArtist.setVisibility(View.VISIBLE);
        tvNameSong.setVisibility(View.VISIBLE);
        ivDisk.setVisibility(View.VISIBLE);
    }

    public void setLayoutComponentsInit(){
        //wait
        ivWait = findViewById(R.id.iv_wait);
        ivWait.setVisibility(View.VISIBLE);
        tvWait = findViewById(R.id.tv_wait);
        tvWait.setVisibility(View.VISIBLE);

        //playing
        ivStopBtn  = findViewById(R.id.iv_stop);
        ivStopBtn.setVisibility(View.GONE);
        ivStopBtn.setOnClickListener(onStopBtn);

        tvArtist = findViewById(R.id.tv_artist_song);
        tvArtist.setVisibility(View.GONE);
        tvNameSong = findViewById(R.id.tv_name_song);
        tvNameSong.setVisibility(View.GONE);

        ivDisk = findViewById(R.id.iv_disc);
        ivDisk.setVisibility(View.GONE);

        setSongMetadata("No Artist", "Untitled");
    }

    private void releaseMediaPlayer(){
        if (mp!=null){
            mp.release();
            mp = null;
        }
    }

    //SOCKETS-LISTENERS -----------------------------------
    public void onSetSpeaker(int typeOfSpeaker){
        app.setTypeOfSpeaker(typeOfSpeaker);
        if(myEq==null) {myEq = new MyEqualizer(null,typeOfSpeaker);}
        else { myEq.setTypeSpeaker(typeOfSpeaker);}
    }

    public void onStopSong(){
        if(mp != null)mp.stop(); //TODO decidir si podremos volvernos a conectar al server a media canción o no.
        isPlaying = false;
    }

    public void onSetMusic(String id){
        Log.d("SPEAKER_PLAY", "on get m Is playing: "+ isPlaying + "is ready "+ isReady);
        if(isPlaying)onStopSong();
        if(id==null) return;
        if(isReady && id.equals(currentSongId)) return;
        isReady = false;
        currentSongId = id;

        mp = new MediaPlayer();

        if(myEq==null)myEq = new MyEqualizer(mp,app.getTypeOfSpeaker());
        else myEq.setMp(mp);

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(readyToPlay);
        try{
            String url = Constants.SERVER_URL + Constants.SERVER_GET_MUSIC_URL +id;
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
        Thread startSong;

        if (mp == null) {
            sendServerError(ERR_NOT_INIT_MEDIA_PLAYER, "ERR_NOT_INIT_MEDIA_PLAYER");
            return;
        }
        if(!isReady){
            sendServerError(ERR_NOT_ABLE_PREPARE_SONG, "ERR_NOT_ABLE_PREPARE_SONG");
            return;
        }
        Log.d("SPEAKER_PLAY", "HOLA");
        startSong = new Thread(){
            public void run(){
                boolean done= false;
                long now;
                Log.d("ZZZ", "ABOUT TO PLAY THE SONG");
                while(!done){
                    now = System.currentTimeMillis() ; //TODO agregar factor de correccion según server?
                    if(SpeakerPlayingActivity.this.lastTimestamp <= now ){
                        mp.seekTo(SpeakerPlayingActivity.this.lastMillis);
                        mp.start();
                        myEq.getEq().setEnabled(true);
                        done = true;
                    }
                    try {
                        Thread.sleep(SLEEP_TIME); // waiting to play sync.
                    }catch (Exception e){
                        sendServerError(ERR_ASYNC_PLAY,"ERR_ASYNC_PLAY");
                        //TODO
                        done = true;
                    }
                }
            }
        };
        startSong.start();
    }

    //SOCKETS-SEND ------------------------------------------

    private void sendDisconnect(){
        releaseMediaPlayer();
        app.getSocket().disconnect();
        app.getSocket().off("onSetSpeaker", socketOnSetSpeaker);
        app.getSocket().off("onStopSong", socketOnStopSong);
        app.getSocket().off("onSetMusic", socketOnSetMusic);
        app.getSocket().off("onPlay", socketOnPlay);
        app.getSocket().off(Socket.EVENT_DISCONNECT,onDisconnect);
    }

    // TODO (@lima1756): same as SOCKET-IO LISTENERS
    public void sendMusicIsReadyToServer(){
        JSONObject params= new JSONObject();
        try{
            params.put(Constants.SOCKET_PARAM_READY,true);
            app.getSocket().emit(Constants.SOCKET_EMIT_READY, params);
        }catch (JSONException e){
            //TODO
        }
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
    public void onRestart(){
        super.onRestart();
        setLayoutComponentsInit();
    }
    @Override
    public void onResume(){
        super.onResume();
        setLayoutComponentsInit();
    }
    @Override
    public void onBackPressed(){
        sendDisconnect();
        super.onBackPressed();
    }
}
