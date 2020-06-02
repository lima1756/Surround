import User from "./User";
import RoomStatus from '../constants/RoomStatus.enum';
import UserStatus from '../constants/UserStatus.enum';
import SpeakerSignals from '../constants/SpeakerSignals.enum';
import { Disconnect } from '../types/Shared.types';
import { SpeakerPlaySignal, SpeakerPrepareSignal, SpekerStopSignal } from 'src/types/Speaker.types';
import { Logger } from '@overnightjs/logger';
import ControllerSignals from '../constants/ControllerSignals.enum';
import { ControllerSpeakerDisconnected } from 'src/types/Controller.types';
import { SongHelper } from '../models/Song.model';
import {ControllerPlaySignal} from 'src/types/Controller.types';

class Room{
    private controller: User;
    private speakers: { [id:string]:User } = {};
    private id: string;
    private static KEYS= ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
    private status: RoomStatus;
    private songStart?: number;
    private songID?: string;
    private songName?: string;
    private timeStart?: number;

    
    constructor(controller: User, id: string){
        this.controller = controller;
        this.id = id;
        this.status = RoomStatus.CREATED;
        this.songStart = undefined;
        this.songID = undefined;
        this.songName = undefined;
    }

    static genID(): string{
        let id: string = "";
        for(let i = 0; i < 4; i++){
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
        this.controller.getSocket().emit<ControllerSpeakerDisconnected>(ControllerSignals.SPEAKER_DISCONNECTED,{"id": id, "name": user.getName()})
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
                Logger.Info("SPEAKERS READY(): Speakers NOT ready: " + user.getName());
                return false;
            }
            Logger.Info("SPEAKERS READY(): Speaker ready: " + user.getName());
        }
        Logger.Info("SPEAKERS READY(): Speakers ready: TRUE");
        return true;
    }

    public playSpeakers() {
        Logger.Info("Playing music in room: " + this.id);
        this.timeStart = Date.now() + 3000;
        for(const [_id, user] of Object.entries(this.speakers)){
            Logger.Info("Playing music: " + user.getName());
            user.getSocket().emit<SpeakerPlaySignal>(SpeakerSignals.PLAY, {"timestamp": this.timeStart, "millis_play": (this.songStart?this.songStart:0)})
        }
        this.controller.getSocket().emit<ControllerPlaySignal>(ControllerSignals.PLAY_START, {"timestamp": this.timeStart});
        Logger.Info("Signal sent to controller: " + ControllerSignals.PLAY_START);
    }

    public prepareSpeakers(song_id: string, songStartTime: number) {
        this.status = RoomStatus.PLAYING;
        this.songID = song_id;
        this.songStart = songStartTime;
        this.songName = "";
        SongHelper.findById(song_id).then(song=>{
            if(!song){
                return;
            }
            Logger.Info("Preparing speakers");
            for(const [_id, user] of Object.entries(this.speakers)){
                user.getSocket().emit<SpeakerPrepareSignal>(SpeakerSignals.SET_MUSIC, {"song_id": song.id, "song_artist": song.artist, "song_name":song.name})
                Logger.Info("(prepareSpeakers): Prepared: " + user.getName());
            }  
        })
    }

    public stopSpeakers() {
        this.status = RoomStatus.PAUSE;
        for(const [_id, user] of Object.entries(this.speakers)){
            user.setStatus(UserStatus.PAUSED);
            user.getSocket().emit<SpekerStopSignal>(SpeakerSignals.STOP_SONG, {"stop":true})
        }
    }

    public getSongID(){
        return this.songID;
    }

    public getTimeStart(){
        return this.timeStart;
    }

    public getSongStart(){
        return this.songStart;
    }
}

export default Room;
