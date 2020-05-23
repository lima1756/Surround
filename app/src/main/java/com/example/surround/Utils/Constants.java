package com.example.surround.Utils;


public class Constants {
    public static final String SERVER_URL = "https://surround-music.herokuapp.com";
    public static final String SERVER_GET_ALL_MUSIC = "/api/music/all";
    public static final String SERVER_GET_MUSIC_URL ="/api/music/song/";

    //SEEKBAR CONSTANTS, USED BY BOTH SIDES
    public static final int MAX_PROGRESS_SEEKBAR = 1000;
    //                      Min  progress seekbar value is 0 by default. resetting it would be a problem.
    public static final int EQUALIZER_SUBWOOFER_SPEAKER =3;
    public static final int EQUALIZER_CENTER_SPEAKER =2;
    public static final int EQUALIZER_FRONT_LEFT_SPEAKER =0;
    public static final int EQUALIZER_LEFT_SPEAKER =4;
    public static final int EQUALIZER_BACK_LEFT_SPEAKER  =6;
    public static final int EQUALIZER_FRONT_RIGHT_SPEAKER  =1;
    public static final int EQUALIZER_RIGHT_SPEAKER =5;
    public static final int EQUALIZER_BACK_RIGHT_SPEAKER =7;

    //SOCKETS SPEAKER
    public static final String SOCKET_PARAM_TYPE_SPEAKER = "type_speaker";
    public static final String SOCKET_PARAM_SONG_ID = "song_id";
    public static final String SOCKET_PARAM_SONG_ARTIST= "song_artist";
    public static final String SOCKET_PARAM_SONG_NAME = "song_name";
    public static final String SOCKET_PARAM_MILLIS_PLAY = "millis_play";
    public static final String SOCKET_PARAM_TIMESTAMP_PLAY = "timestamp";

    public static final String SOCKET_EMIT_LOGIN_SPEAKER = "login_speaker";
    public static final String SOCKET_EMIT_READY = "speaker_ready";
    public static final String SOCKET_EMIT_SET_MUSIC_ERROR = "set_music_error";
    public static final String SOCKET_EMIT_SET_SPEAKER_ERROR = "set_speaker_error";
    public static final String SOCKET_EMIT_PLAY_ERROR = "play_error";

    public static final String SOCKET_ON_PLAY = "play";
    public static final String SOCKET_ON_SET_MUSIC = "set_music";
    public static final String SOCKET_ON_LOGIN_RESPONSE = "login_response";
    public static final String SOCKET_ON_STOP_SONG = "stop_song";
    public static final String SOCKET_ON_SET_SPEAKER = "set_speaker";

    public static final String SOCKET_PARAM_TOKEN = "room";
    public static final String SOCKET_PARAM_NAME = "name";
    public static final String SOCKET_PARAM_ID="id";
    public static final String SOCKET_PARAM_READY="ready";

    //SOCKETS CONTROLLER
    public static final String SOCKET_EMIT_LOGIN_CONTROLLER = "login_controller";
    public static final String SOCKET_EMIT_PLAY = "play";
    public static final String SOCKET_EMIT_STOP = "stop";

    public static final String SOCKET_ON_STOP_MUSIC_RESPONSE = "stop_response";
    public static final String SOCKET_ON_PLAY_MUSIC_RESPONSE = "play_response";
    public static final String SOCKET_ON_CONFIGURE_SPEAKER_RESPONSE = "configure_speaker_response";
    public static final String SOCKET_ON_SPEAKER_DISCONNECTED = "speaker_disconnected";
    public static final String SOCKET_ON_SPEAKER_CONNECTED = "speaker_connected";
    public static final String SOCKET_ON_PLAY_START = "play_start";

    public static final String SOCKET_PARAM_ROOM = "room";
    public static final String SOCKET_PARAM_OK = "ok";
    public static final String SOCKET_PARAM_ERROR = "error";
}