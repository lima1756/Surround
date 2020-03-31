import Equalizer from 'src/constants/Equalizer.enum';

export interface ControllerLoginRequest{
    name: string
}

export interface ControllerConfigurationRequest{
    speaker_id: string;
    type: Equalizer;
}

export interface ControllerPlayRequest{
    song_id: string;
    millis_play: number;
}

export interface ControllerLoginResponse {
    id: string;
    room: string;
}

export interface ControllerSpeakerConnected {
    id: string,
    name: string,
    type: Equalizer
}

export interface ControllerSpeakerDisconnected {
    id: string,
    name: string
}