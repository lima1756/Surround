import Equalizer from 'src/constants/Equalizer';

export interface ControllerConfigurationRequest{
    speaker_id: string;
    type: Equalizer;
}

export interface ControllerPlayRequest{
    song_id: string;
}