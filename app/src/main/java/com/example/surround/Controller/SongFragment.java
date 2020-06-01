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
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.Song;
import com.example.surround.R;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment implements SongListener {
    private Song currentSong;
    private ImageView songIcon, playBtn, prevBtn, nextBtn;
    private TextView songTitle, songArtist, songDuration, songTimeElapsed;
    private MediaPlayer mediaPlayer;
    private SeekBar sbSongPlaying;
    private boolean isPlaying = false;
    private ControllerSocket app;
    private int playEmitterErrorCounter = 0;
    private int milisSong;
    private ArrayList<Song> songs;
    private int currentSongIndex;

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

        setLayoutComponents(view);

        app.getSocket().on(Constants.SOCKET_ON_PLAY_START, socketOnControllerPlayStart);
        app.getSocket().on(Constants.SOCKET_ON_PLAY_MUSIC_RESPONSE, socketOnPlayResponse);

        app.getSocket().on(Constants.SOCKET_ON_STOP_MUSIC_RESPONSE,socketOnStopResponse);
        app.getSocket().connect();

        updateSongMedia();

        emmitPlay(0);
        isPlaying = true;
        playBtn.setImageResource( R.drawable.ic_pause_circle);


        //Setting Listeners
        playBtn.setOnClickListener(playListener);
        sbSongPlaying.setMax(Constants.MAX_PROGRESS_SEEKBAR);
        sbSongPlaying.setOnSeekBarChangeListener(seekBarListener);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSongIndex = (currentSongIndex+1) % songs.size();
                currentSong = songs.get(currentSongIndex);
                updateSongMedia();
                emmitPause();
                emmitPlay(0);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
                isPlaying = true;
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSongIndex = (currentSongIndex-1) % songs.size();
                currentSong = songs.get(currentSongIndex);
                updateSongMedia();
                emmitPause();
                emmitPlay(0);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
                isPlaying = true;
            }
        });
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
        updateTime();
    }

    private void updateTime(){
        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
        Log.d("URI", Uri.parse(Constants.SERVER_URL + Constants.SERVER_GET_MUSIC_URL + currentSong.getId()).toString());
        mRetriever.setDataSource(Constants.SERVER_URL + Constants.SERVER_GET_MUSIC_URL + currentSong.getId(), new HashMap<String, String>());
        String s = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int minutes = (int)Math.floor(Long.parseLong(s)/60000.0);
        int seconds = (int)Math.floor(Long.parseLong(s)/1000)-(minutes*60);
        songDuration.setText(minutes+":"+seconds);
        mRetriever.release();
    }


   private void setLayoutComponents(View view){
       songIcon = view.findViewById(R.id.songImageIV);
       songTitle = view.findViewById(R.id.songTitleTV);
       songArtist = view.findViewById(R.id.songArtistTV);
       songDuration = view.findViewById(R.id.durationTV);
       songTimeElapsed = view.findViewById(R.id.timeElapsedTV);
       sbSongPlaying = view.findViewById(R.id.songSB);

       prevBtn = view.findViewById(R.id.prevIV);
       nextBtn = view.findViewById(R.id.nextIV);
       playBtn = view.findViewById(R.id.playIV);

       songTimeElapsed.setVisibility(View.VISIBLE);
   }


    private void updateMillisSong() {
        if (sbSongPlaying == null) return;
        milisSong = (currentSong.getDuration() * sbSongPlaying.getProgress()) / Constants.MAX_PROGRESS_SEEKBAR;
    }

    private String toMinutes(int songDuration){

        int minutes = songDuration/60;
        int seconds = songDuration%60;

        if(seconds < 10) return minutes+":0"+seconds;

        return minutes+":"+seconds;
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
                updateMillisSong();
                emmitPlay(milisSong);
                playBtn.setImageResource( R.drawable.ic_pause_circle);
            }
            isPlaying = !isPlaying;
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser) return;
            songTimeElapsed.setText(toMinutes(progress));
            updateMillisSong();
            if(isPlaying){
                emmitPlay(milisSong);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //TODO(alex) is it necessary something here?
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //TODO(alex) is it necessary something here?
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
            JSONObject data = (JSONObject) args[0];
            try {
                Toast.makeText(getContext(), "User: " + data.getString("name")+ "Connected", Toast.LENGTH_LONG).show();

            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                Toast.makeText(getContext(),R.string.error_disconnect_speaker,Toast.LENGTH_LONG).show();
            }
        }
    };

    private Emitter.Listener socketOnControllerPlayStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.e("SONG_FRAGMENT" ,data.toString());
            try {
                final long timeToStartSeek = data.getLong("timestamp");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long wait = timeToStartSeek-System.currentTimeMillis();
                            Thread.sleep(wait);
                            // TODO(alex): iniciar seekbar para que avance
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                Toast.makeText(getContext(),R.string.error_disconnect_speaker,Toast.LENGTH_LONG).show();
            }
        }
    };


    //SOCKETS-EMITS ------------------------------------------


    private void emmitPlay(final int milis){
        JSONObject params= new JSONObject();
        try{
            params.put(Constants.SOCKET_PARAM_SONG_ID,currentSong.getId());
            params.put(Constants.SOCKET_PARAM_MILLIS_PLAY, milis);
            app.getSocket().emit(Constants.SOCKET_EMIT_PLAY, params);
        }catch (JSONException e){
            Log.e("SONG_FRAGMENT" ,e.toString());
            playEmitterErrorCounter++;
            if(playEmitterErrorCounter == 3)
            {
                Toast.makeText(getContext(),R.string.error_connection_generic,Toast.LENGTH_LONG).show();
                return;
            }
            // TODO (ivan): decidir cuanto tiempo es el mejor para esperar
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emmitPlay(milis);
                }
            }, 3000);
            playEmitterErrorCounter = 0;
        }

    }


    private void emmitPause(){
        app.getSocket().emit(Constants.SOCKET_EMIT_STOP, new JSONObject());
    }

    @Override
    public void updateSongs(ArrayList<Song> songs) {
        Log.d("FRAGMENT", ""+songs.size());
        this.songs = songs;
    }
}
