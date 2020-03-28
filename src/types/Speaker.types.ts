export interface SpeakerLoginRequest{
    room: string,
    name: string,
}

export interface SpeakerReadyRequest{
    ready: boolean
}

export interface SpeakerLoginResponse {
    type_speaker: number,
}

export interface SpeakerSetTypeSignal {
    type_speaker: number
}

export interface SpeakerPlaySignal{
    timestamp:number
}

export interface SpeakerPrepareSignal{
    song_id:string,
    song_artist: string,
    song_name: string
}

export interface SpekerStopSignal{
    stop:boolean
}