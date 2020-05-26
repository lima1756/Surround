package com.example.surround.Speaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.surround.Speaker.Utils.SpeakerSocket;
import com.example.surround.MainActivity;
import com.example.surround.R;
import com.example.surround.Utils.Constants;
import com.example.surround.Speaker.Utils.MyEqualizer;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SpeakerPlayingActivity extends AppCompatActivity {

    public static final int ERR_NOT_ABLE_PREPARE_SONG = 2;
    public static final int ERR_NOT_INIT_MEDIA_PLAYER = 1;
    public static final int ERR_ASYNC_PLAY = 3;
    // TODO (quien sea): decidir cuanto tiempo es el mejor para esperar
    public static final int WAIT_TIME_RESPONSE = 2000;

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
    SpeakerSocket app;

    // ERROR COUNTERS ---------------------------
    private Integer[] setMusicErrorCounter = {0};
    private Integer[] playErrorCounter = {0};
    private Integer[] setSpeakerErrorCounter = {0};
    private Integer[] musicIsReadyErrorCounter = {0};
    private Integer[] playClientErrorCounter = {0};
    private Integer[] threadPlayClientErrorCounter = {0};

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
                Log.d("SPEAKER_PLAY", "preparing");
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
                SpeakerPlayingActivity.this.setMusicErrorCounter[0] = 0;
            }catch (JSONException e){
                SpeakerPlayingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SpeakerPlayingActivity.this.setSongMetadata("No artist","Untitled"); //Cambiar controles
                    }
                });
                sendServerError(setMusicErrorCounter, "Music Metadata not found", Constants.SOCKET_EMIT_SET_MUSIC_ERROR);
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
            Log.d("SPEAKER_PLAY", "got signal from server");
            JSONObject data = (JSONObject) args[0];
            try{
                millis = data.getInt(Constants.SOCKET_PARAM_MILLIS_PLAY);
                timestamp = data.getLong(Constants.SOCKET_PARAM_TIMESTAMP_PLAY);
                Log.d("SPEAKER_PLAY", "timestamp:"+ timestamp);
                SpeakerPlayingActivity.this.onPlayInMillisecond(timestamp,millis);
            }catch (JSONException e){
                Log.d("SPEAKER_PLAY", "error on play");
                Log.d("SPEAKER_PLAY", e.getMessage());
                sendServerError(setSpeakerErrorCounter, "", Constants.SOCKET_EMIT_PLAY_ERROR);
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

        app = SpeakerSocket.getInstance();
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
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.e("SPEAKER_PLAY", "error code: " + i);
                Log.e("SPEAKER_PLAY", "error extra: " + i1);
                return false;
            }
        });
        try{
            String url = Constants.SERVER_URL + Constants.SERVER_GET_MUSIC_URL +id;
            mp.setDataSource(url);
            mp.prepareAsync(); // might take long! (for buffering, etc)
        }catch (IOException e){
            Toast.makeText(this, R.string.error_on_play,Toast.LENGTH_LONG).show();
        }
    }

    public void onPlay(long timestamp) {
        onPlayInMillisecond(timestamp, 0);
    }


    public void onPlayInMillisecond(final long timestamp, final int milis) {
        this.lastTimestamp = timestamp;
        this.lastMillis = milis;
        Thread startSong;

        if (mp == null  || !isReady) {
            playClientErrorCounter[0]++;
            if(playClientErrorCounter[0] == 3)
            {
                Toast.makeText(this,R.string.error_on_play,Toast.LENGTH_LONG);
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPlayInMillisecond(timestamp, milis);
                }
            }, WAIT_TIME_RESPONSE);
            playClientErrorCounter[0] = 0;
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
                        threadPlayClientErrorCounter[0]++;
                        if(threadPlayClientErrorCounter[0] == 3)
                        {
                            Toast.makeText(SpeakerPlayingActivity.this,R.string.error_on_play,Toast.LENGTH_LONG).show();
                            return;
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO (alex) decidir como intentar revivir el hilo
                            }
                        }, WAIT_TIME_RESPONSE);
                        threadPlayClientErrorCounter[0] = 0;
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

    public void sendMusicIsReadyToServer(){
        JSONObject params= new JSONObject();
        try{
            params.put(Constants.SOCKET_PARAM_READY,true);
            app.getSocket().emit(Constants.SOCKET_EMIT_READY, params);
        }catch (JSONException e){
            musicIsReadyErrorCounter[0]++;
            if(musicIsReadyErrorCounter[0] == 3)
            {
                Toast.makeText(this,R.string.error_no_sended_to_server,Toast.LENGTH_LONG).show();
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMusicIsReadyToServer();
                }
            }, WAIT_TIME_RESPONSE);
            musicIsReadyErrorCounter[0] = 0;
        }
        //TEST ---------------------------
        //onPlayInMillisecond(System.currentTimeMillis()+20000, 30000);
        //--------------------------------
    }


    private void sendServerError(Integer[] counter, String userMessage, String socketIOEmit){
        JSONObject params= new JSONObject();
        try {
            params.put(Constants.SOCKET_PARAM_READY,true);
            sendServerError(counter, userMessage, socketIOEmit, params);
        } catch (JSONException e) {
            Toast.makeText(this,R.string.error_no_sended_to_server,Toast.LENGTH_LONG).show();
            return;
        }
        counter[0]++;
    }

    private void sendServerError(Integer counter[], String userMessage, String socketIOEmit, JSONObject params){
        counter[0]++;
        if(counter[0] == 3){
            counter[0] = 0;
            Toast.makeText(this,R.string.error_connection_generic,Toast.LENGTH_LONG).show();
            return;
        }
        app.getSocket().emit(socketIOEmit, params);
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
