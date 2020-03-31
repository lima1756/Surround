import User from "./User";
import RoomStatus from '../constants/RoomStatus.enum';
import UserStatus from '../constants/UserStatus.enum';
import SpeakerSignals from '../constants/SpeakerSignals.enum';
import { Disconnect } from '../types/Shared.types';
import { SpeakerPlaySignal, SpeakerPrepareSignal, SpekerStopSignal } from 'src/types/Speaker.types';
import { Logger } from '@overnightjs/logger';

class Room{
    private controller: User;
    private speakers: { [id:string]:User } = {};
    private id: string;
    private static KEYS= ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
    private status: RoomStatus

    
    constructor(controller: User, id: string){
        this.controller = controller;
        this.id = id;
        this.status = RoomStatus.CREATED;
    }

    static genID(): string{
        let id: string = "";
        for(let i = 0; i < 7; i++){
            id = id + Room.KEYS[Math.floor(Math.random()*(Room.KEYS.length))];
        }
        return id;
    }

    public getID(): string{
        return this.id;
    }

    public getController(): User{
        return this.controller;
    }

    public getSpeaker(id: string): User{
        return this.speakers[id];
    }

    public addSpeaker(speaker: User){
        if(this.speakers[speaker.getID()]){
            Logger.Warn("Speaker already in room");
            return;
        }
        this.speakers[speaker.getID()] = speaker;
    }

    private disconnectSpeaker(id: string, disconnectResponse: Disconnect | null){
        if(!this.speakers[id]){
            throw new Error("speaker doesn't exist");
        }
        const user = this.speakers[id]
        if(disconnectResponse)
            user.getSocket().emit("disconnect", disconnectResponse);
        user.getSocket().disconnect();
        delete(this.speakers[id]);
    }

    public removeSpeaker(id: string, reason: string|null){
        this.disconnectSpeaker(id, null);
    }

    public removeAllSpeakers(reason: string)  {
        for(const [id, _user] of Object.entries(this.speakers)){
            this.disconnectSpeaker(id, {"disconnection": reason});
        }
    }

    public speakersReady(): boolean {
        for(const [_id, user] of Object.entries(this.speakers)){
            if(user.getStatus() != UserStatus.READY){
                return false;
            }
        }
        return true;
    }

    public playSpeakers() {
        for(const [_id, user] of Object.entries(this.speakers)){
            user.getSocket().emit<SpeakerPlaySignal>(SpeakerSignals.PLAY, {"timestamp": Date.now()+3000})
        }
    }

    public prepareSpeakers(song_id: string) {
        // TODO: send real data retrieved from DB
        for(const [_id, user] of Object.entries(this.speakers)){
            user.getSocket().emit<SpeakerPrepareSignal>(SpeakerSignals.SET_MUSIC, {"song_id": song_id, "song_artist": "artista", "song_name":"cancion"})
        }
    }

    public stopSpeakers() {
        for(const [_id, user] of Object.entries(this.speakers)){
            user.getSocket().emit<SpekerStopSignal>(SpeakerSignals.STOP_SONG, {"stop":true})
        }
    }
}

export default Room;