package com.example.surround.Controller;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.example.surround.Speaker.SpeakerPlayingActivity;
import com.example.surround.Utils.Constants;
import com.example.surround.Utils.Song;
import com.example.surround.R;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {
    private Song currentSong;
    private ImageView songIcon, playBtn, prevBtn, nextBtn;
    private TextView songTitle, songArtist, songDuration, songTimeElapsed;
    private MediaPlayer mediaPlayer;
    private SeekBar songPlaying;
    private boolean isPlaying = false;
    private ControllerSocket app;
    private int playEmitterErrorCounter = 0;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        songIcon = view.findViewById(R.id.songImageIV);
        songTitle = view.findViewById(R.id.songTitleTV);
        songArtist = view.findViewById(R.id.songArtistTV);
        songDuration = view.findViewById(R.id.durationTV);
        songTimeElapsed = view.findViewById(R.id.timeElapsedTV);
        songPlaying = view.findViewById(R.id.songSB);

        prevBtn = view.findViewById(R.id.prevIV);
        nextBtn = view.findViewById(R.id.nextIV);
        playBtn = view.findViewById(R.id.playIV);

        Glide.with(getContext())
                .load(currentSong.getImageRes())
                .placeholder(R.drawable.vinil)
                .fitCenter()
                .into(songIcon);
        songTitle.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());
        songDuration.setText(toMinutes(currentSong.getDuration()));

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    emmitPause();
                    playBtn.setImageResource(R.drawable.ic_play_circle);
                }
                else {
                    emmitPlay();
                    playBtn.setImageResource( R.drawable.ic_pause_circle);
                }
                isPlaying = !isPlaying;
            }
        });

        // TODO: enable this by changing the socketIO api to let the controller obtain the song
//        songPlaying.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//                songTimeElapsed.setVisibility(View.VISIBLE);
//                int x = (int) Math.ceil(progress / 1000f);
//
//                if (x < 10)
//                    songTimeElapsed.setText("0:0" + x);
//                else
//                    songTimeElapsed.setText("0:" + x);
//
//                double percent = progress / (double) seekBar.getMax();
//                int offset = seekBar.getThumbOffset();
//                int seekWidth = seekBar.getWidth();
//                int val = (int) Math.round(percent * (seekWidth - 2 * offset));
//                int labelWidth = songTimeElapsed.getWidth();
//                songTimeElapsed.setX(offset + seekBar.getX() + val
//                        - Math.round(percent * offset)
//                        - Math.round(percent * labelWidth / 2));
//
//                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
//                    clearMediaPlayer();
//                    play.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));
//                    songPlaying.setProgress(0);
//                }
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                    mediaPlayer.seekTo(seekBar.getProgress());
//                }
//            }
//
//
//        });
    }

    public void play(View view){
        try {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                songPlaying.setProgress(0);
                isPlaying = true;
                playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_play));
            }


            if (!isPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), android.R.drawable.ic_media_pause));

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

            isPlaying = false;
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

    private String toMinutes(int songDuration){

        int minutes = songDuration/60;
        int seconds = songDuration%60;

        if(seconds < 10) return minutes+":0"+seconds;

        return minutes+":"+seconds;
    }


    //SOCKET-IO LISTENERS .........................................
    private Emitter.Listener socketOnStopResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                if(data.has("error")){
                    String error = data.getString(Constants.SOCKET_PARAM_ERROR);
                    Log.e("SongFragment" ,error);
                    // TODO (quien sea): decirle al usuario el error
                }

            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                // TODO (quien sea): decirle al usuario que hubo un error
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
                    // TODO (quien sea): decirle al usuario el error
                }

            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                // TODO (quien sea): decirle al usuario que hubo un error
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
                // TODO (quien sea): decirle al usuario que hubo un error
            }
        }
    };

    private Emitter.Listener socketOnSpeakerDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                Toast.makeText(getContext(), "User: " + data.getString("name")+ "Disconnected", Toast.LENGTH_LONG).show();
            }catch (JSONException e){
                Log.e("SongFragment" ,e.toString());
                // TODO (quien sea): decirle al usuario que hubo un error
            }
        }
    };


    //SOCKETS-EMITS ------------------------------------------


    private void emmitPlay(){
        JSONObject params= new JSONObject();
        try{
            params.put(Constants.SOCKET_PARAM_SONG_ID,currentSong.getId());
            app.getSocket().emit(Constants.SOCKET_EMIT_PLAY, params);
        }catch (JSONException e){
            Log.e("SongFragment" ,e.toString());
            playEmitterErrorCounter++;
            if(playEmitterErrorCounter == 3)
            {
                // TODO (quien sea): decirle al usuario que hubo un error
                return;
            }
            // TODO (quien sea): decidir cuanto tiempo es el mejor para esperar
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    emmitPlay();
                }
            }, 2000);
            playEmitterErrorCounter = 0;
        }

    }

    private void emmitPause(){
        app.getSocket().emit(Constants.SOCKET_EMIT_STOP, new JSONObject());
    }
}
