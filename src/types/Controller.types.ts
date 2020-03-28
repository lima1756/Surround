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
}

export interface ControllerLoginResponse {
    id: string;
    room: string;
}