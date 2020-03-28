enum ControllerSignals {
    // envio
    LOGIN_RESPONSE = 'login_response',
    CONFIGURE_SPEAKER_RESPONSE = 'configure_speaker_response',
    PLAY_MUSIC_RESPONSE = 'play_response',
    STOP_MUSIC_RESPONSE = 'stop_response',
    // recibo    
    LOGIN = 'login_controller',
    CONFIGURE_SPEAKER='configure_speaker',
    PLAY_MUSIC='play',
    STOP_MUSIC='stop',
}
export default ControllerSignals;