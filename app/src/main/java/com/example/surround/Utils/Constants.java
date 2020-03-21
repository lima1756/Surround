package com.example.surround.Utils;


public class Constants {
    public static final String CHAT_SERVER_URL = "https://socket-io-chat.now.sh/";

    public static final int EQUALIZER_SUBWOOFER_SPEAKER =3;
    public static final int EQUALIZER_CENTER_SPEAKER =2;
    public static final int EQUALIZER_FRONT_LEFT_SPEAKER =0;
    public static final int EQUALIZER_LEFT_SPEAKER =4;
    public static final int EQUALIZER_BACK_LEFT_SPEAKER  =6;
    public static final int EQUALIZER_FRONT_RIGHT_SPEAKER  =1;
    public static final int EQUALIZER_RIGHT_SPEAKER =5;
    public static final int EQUALIZER_BACK_RIGHT_SPEAKER =7;

    //SOCKETS PARAMS
    public static final String SOCKET_PARAM_TYPE_SPEAKER = "type_speaker";
    public static final String SOCKET_PARAM_SONG_URL = "url_song";
    public static final String SOCKET_PARAM_SONG_ARTIST= "artist_song";
    public static final String SOCKET_PARAM_SONG_NAME = "name_song";
    public static final String SOCKET_PARAM_MILLIS_PLAY = "millis_play";
    public static final String SOCKET_PARAM_TIMESTAMP_PLAY = "timestamp_play";

    public static final String SOCKET_EMIT_LOGIN_SPEAKER = "login_speaker";
    public static final String SOCKET_EMIT_READY = "speaker_ready";

    public static final String SOCKET_ON_PLAY = "on_play";
    public static final String SOCKET_ON_SET_MUSIC = "on_set_music";
    public static final String SOCKET_PARAM_TOKEN = "session_token";
    public static final String SOCKET_PARAM_ID="id";
    public static final String SOCKET_ON_STOP_SONG = "on_stop_song";
    public static final String SOCKET_ON_SET_SPEAKER = "on_set_speaker";


}