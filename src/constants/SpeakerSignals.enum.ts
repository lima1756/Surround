enum SpeakerSignals {
    // envio
    LOGIN_RESPONSE = 'login_response',
    PLAY = 'play',
    SET_MUSIC = 'set_music',
    STOP_SONG = 'stop_song',
    SET_TYPE = 'set_speaker',
    // recibo
    LOGIN = 'login_speaker',
    READY = 'speaker_ready',
    // recibo errores
    SET_MUSIC_ERROR = 'set_music_error',
    SET_SPEAKER_ERROR = "set_speaker_error",
    PLAY_ERROR = "play_error",
}
export default SpeakerSignals;