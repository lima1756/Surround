package com.example.surround.Controller;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.surround.Controller.Utils.ControllerSocket;
import com.example.surround.Controller.Utils.SongListener;
import com.example.surround.Speaker.SpeakerPlayingActivity;
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.Song;
import com.example.surround.R;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment implements SongListener {
    private Song currentSong;
    private ImageView songIcon, playBtn, prevBtn, nextBtn;
    private TextView songTitle, songArtist, songDuration, songTimeElapsed;
    private SeekBar sbSongPlaying;
    private boolean isPlaying = false;
    private ControllerSocket app;
    private int playEmitterErrorCounter = 0;
    private int secondsSong;
    private ArrayList<Song> songs;
    private int currentSongIndex;
    public static final int SLEEP_TIME = 500; //milliseconds.
    Thread threadPlaySong;
    private volatile boolean threadPlaySongIsBeenUsed;
    int lastMilis;

    SongFragment(Song song) { this.currentSong = song; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ControllerSocket.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLayoutComponents(view);
        app.getSocket().on(Constants.SOCKET_ON_PLAY_START, socketOnControllerPlayStart);
        app.getSocket().on(Constants.SOCKET_ON_PLAY_MUSIC_RESPONSE, socketOnPlayResponse);
        app.getSocket().on(Constants.SOCKET_ON_SPEAKER_CONNECTED, socketOnSpeakerConnected);
        app.getSocket().on(Constants.SOCKET_ON_STOP_MUSIC_RESPONSE,socketOnStopResponse);
        app.getSocket().connect();

        songs = ((ControllerActivity)getActivity()).getSongs();

        if(songs.size() == 0){
            ((ControllerActivity)getActivity()).retrieveMusic(this);
        }
        for(int i = 0; i < songs.size(); i++){
            if(songs.get(i).getId().equals(currentSong.getId()))
            {
                this.currentSongIndex = i;

                break;
            }
        }
        sbSongPlaying.setMax(currentSong.getDuration());
        sbSongPlaying.setProgress(0);
        updateSongMedia();

        emmitPlay(lastMilis);

        isPlaying = true;
        playBtn.setImageResource( R.drawable.ic_pause_circle);




        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSongIndex = (currentSongIndex+1) % songs.size();
                setNewSong();
                emmitPause();
                emmitPlay(0);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
                isPlaying = true;
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSongIndex = (currentSongIndex-1+songs.size()) % songs.size();
                setNewSong();
                emmitPause();
                emmitPlay(0);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
                isPlaying = true;
            }
        });
   }

   private void setNewSong(){
       currentSong = songs.get(currentSongIndex);
       sbSongPlaying.setMax(currentSong.getDuration());
       sbSongPlaying.setProgress(0);
       songTimeElapsed.setText(toMinutes(0));
       updateSongMedia();
   }


    private void updateSongMedia(){
        Glide.with(getContext())
                .load(currentSong.getImageRes())
                .placeholder(R.drawable.vinil)
                .fitCenter()
                .into(songIcon);
        songTitle.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());
        songDuration.setText(toMinutes(currentSong.getDuration()));
    }


   private void setLayoutComponents(View view){
       songIcon = view.findViewById(R.id.songImageIV);
       songTitle = view.findViewById(R.id.songTitleTV);
       songArtist = view.findViewById(R.id.songArtistTV);
       songDuration = view.findViewById(R.id.durationTV);
       songTimeElapsed = view.findViewById(R.id.timeElapsedTV);

       sbSongPlaying = view.findViewById(R.id.songSB);
       sbSongPlaying.setOnSeekBarChangeListener(seekBarListener);

       prevBtn = view.findViewById(R.id.prevIV);
       nextBtn = view.findViewById(R.id.nextIV);

       playBtn = view.findViewById(R.id.playIV);
       playBtn.setOnClickListener(playListener);

       songTimeElapsed.setVisibility(View.VISIBLE);
   }

    private String toMinutes(int songSeconds){

        int minutes = songSeconds/60;
        int seconds = songSeconds%60;

        if(seconds < 10) return minutes+":0"+seconds;

        return String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
    }

    public void onPlayInMillisecond(final long timestamp, final int millis) {
        threadPlaySong = new Thread(){
            public void run(){
                threadPlaySongIsBeenUsed = true;
                int  progressBar = 0;
                long now;
                Log.d("ZZZ", "ABOUT TO PLAY THE SONG");
                while(progressBar < (sbSongPlaying.getMax()-1) && threadPlaySongIsBeenUsed){
                    now = System.currentTimeMillis() ; //TODO agregar factor de correccion según server?
                    if(timestamp <= now ){
                        progressBar = (((int)(now - timestamp)) + millis)/1000;
                        final int finalProgressBar = progressBar;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                songTimeElapsed.setText(toMinutes(finalProgressBar));
                                sbSongPlaying.setProgress(finalProgressBar);
                            }
                        });
                    }
                    try {
                        Thread.sleep(SLEEP_TIME); // waiting to play sync.
                    }catch (Exception e){
                        //TODO que agregar aqui?
                    }
                }
            }
        };
        threadPlaySong.start();
    }


    //LISTENERS ----------------------------------------------
    View.OnClickListener playListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isPlaying){
                emmitPause();
                playBtn.setImageResource(R.drawable.ic_play_circle);
            }
            else {
                secondsSong =  sbSongPlaying.getProgress() ;
                emmitPlay(secondsSong*1000);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
            }
            isPlaying = !isPlaying;
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) return;
            threadPlaySongIsBeenUsed = false; // Stops the SB playing song thread.
            songTimeElapsed.setText(toMinutes(progress));
            secondsSong = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //TODO(alex) is it necessary something here?
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(isPlaying){
                emmitPause();
                emmitPlay(secondsSong*1000);
            }
        }
    };

    //SOCKET-IO LISTENERS .........................................
    private Emitter.Listener socketOnStopResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                if(data.has("error")){
                    String error = data.getString(Constants.SOCKET_PARAM_ERROR);
                    Log.e("SongFragment" ,error);
                    Toast.makeText(getContext(),R.string.error_on_stop_response,Toast.LENGTH_LONG).show();
                }
                Log.d("SONG_FRAGMENT" ,data.toString());

            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                Toast.makeText(getContext(),R.string.error_on_stop_response,Toast.LENGTH_LONG).show();
            }
        }
    };

    private Emitter.Listener socketOnPlayResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                if(data.has("error")){
                    String error = data.getString(Constants.SOCKET_PARAM_ERROR);
                    Log.e("SongFragment" ,error);
                    Toast.makeText(getContext(),R.string.error_connection_generic,Toast.LENGTH_LONG).show();
                }

            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                Toast.makeText(getContext(),R.string.error_connection_generic,Toast.LENGTH_LONG).show();
            }
        }
    };

    private Emitter.Listener socketOnSpeakerConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(getContext(), "User: " + data.getString("name")+ "Connected", Toast.LENGTH_LONG).show();
                    }catch (JSONException e) {
                        Log.e("SongFragment", e.toString());
                        Toast.makeText(getContext(), R.string.some_user, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    };

    private Emitter.Listener socketOnControllerPlayStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.e("SONG_FRAGMENT" ,data.toString());
            try {
                final long timestamp = data.getLong("timestamp");
                SongFragment.this.onPlayInMillisecond(timestamp,lastMilis);
            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                Toast.makeText(getContext(),R.string.error_disconnect_speaker,Toast.LENGTH_LONG).show();
            }
        }
    };


    //SOCKETS-EMITS ------------------------------------------


    private void emmitPlay(final int millis){
        lastMilis = millis;
        JSONObject params= new JSONObject();
        try{
            params.put(Constants.SOCKET_PARAM_SONG_ID,currentSong.getId());
            params.put(Constants.SOCKET_PARAM_MILLIS_PLAY, millis);
            app.getSocket().emit(Constants.SOCKET_EMIT_PLAY, params);
        }catch (JSONException e){
            Log.e("SONG_FRAGMENT" ,e.toString());
            playEmitterErrorCounter++;
            if(playEmitterErrorCounter == 3)
            {
                Toast.makeText(getContext(),R.string.error_connection_generic,Toast.LENGTH_LONG).show();
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emmitPlay(millis);
                }
            }, 1000);
            playEmitterErrorCounter = 0;
        }

    }


    private void emmitPause(){
        threadPlaySongIsBeenUsed = false; // Stops the SB playing song thread.
        app.getSocket().emit(Constants.SOCKET_EMIT_STOP, new JSONObject());
    }

    @Override
    public void updateSongs(ArrayList<Song> songs) {
        Log.d("FRAGMENT", ""+songs.size());
        this.songs = songs;
    }
}
