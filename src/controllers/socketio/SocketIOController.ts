import {Logger} from '@overnightjs/logger';
import Room from '../../shared/Room';
import User from '../../shared/User'
import UserKind from '../../constants/UserKind.enum';
import UserStatus from '../../constants/UserStatus.enum';
import SpeakerSignals from '../../constants/SpeakerSignals.enum';
import ControllerSignals from '../../constants/ControllerSignals.enum';
import { SpeakerLoginRequest, SpeakerLoginResponse, SpeakerSetTypeSignal, SpeakerReadyRequest } from '../../types/Speaker.types';
import { ControllerConfigurationRequest, ControllerPlayRequest, ControllerLoginResponse, ControllerLoginRequest, ControllerSpeakerConnected } from '../../types/Controller.types';
import { ErrorResponse, OkResponse } from 'src/types/Shared.types';
import { Socket } from 'src/types/Socket.type';
import Equalizer from '../../constants/Equalizer.enum';


class SocketIOController {

    private io: SocketIO.Server;
    private rooms: {[id:string]: Room}

    constructor(io: SocketIO.Server) {
        Logger.Info("Configuring Socket io");
        this.rooms = {}
        this.io = io;
    }

    public socketEvents() {
        this.io.on('connection', (socket: Socket) => {
            Logger.Info("new connection");
            let user = new User(socket);
            

            // Speaker listeners
            socket.on(SpeakerSignals.LOGIN, (data: SpeakerLoginRequest) => {
                if(this.rooms[data.room]) {
                    user.setName(data.name);
                    user.setKind(UserKind.SPEAKER)
                    user.setRoomID(data.room);
                    this.rooms[data.room].addSpeaker(user);
                    Logger.Info("Speaker connecting to room");
                    socket.emit<SpeakerLoginResponse>(SpeakerSignals.LOGIN_RESPONSE, {"type_speaker": 2});
                    this.rooms[data.room].getController().getSocket()
                        .emit<ControllerSpeakerConnected>(ControllerSignals.CONNECTED_SPEAKER,
                            {
                                "name":data.name,
                                "id":user.getID(),
                                "type":Equalizer.CENTER_SPEAKER
                            });
                }
                else {
                    socket.emit<ErrorResponse>(SpeakerSignals.LOGIN_RESPONSE, {"error": "Selected room doesn't exist."});
                }
            });

            socket.on(SpeakerSignals.READY, (data : SpeakerReadyRequest) => {
                user.setStatus(UserStatus.READY);
                Logger.Info("Speaker ready: " + user.getName());
                const room = this.rooms[user.getRoomID()];
                if(room && room.speakersReady()){
                    Logger.Info("Speaker ready: "+user.getName());
                    room.playSpeakers();
                }
            });

            // Controller listeners
            socket.on(ControllerSignals.LOGIN, (data: ControllerLoginRequest) => {
                let id = Room.genID();
                while(this.rooms[id]){
                    id = Room.genID();
                }
                user.setName(data.name);
                user.setKind(UserKind.CONTROLLER);
                let room = new Room(user, id);
                user.setRoomID(id);
                this.rooms[id] = room;
                Logger.Info("New Room");
                socket.emit<ControllerLoginResponse>(ControllerSignals.LOGIN_RESPONSE, {"id":user.getID(),"room":room.getID()});
            });

            socket.on(ControllerSignals.CONFIGURE_SPEAKER, (data: ControllerConfigurationRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit<ErrorResponse>(ControllerSignals.CONFIGURE_SPEAKER_RESPONSE, {"error": "Selected room doesn't exist."});
                    return;
                }
                const speaker = room.getSpeaker(data.speaker_id);
                if(!speaker){
                    socket.emit<ErrorResponse>(ControllerSignals.CONFIGURE_SPEAKER_RESPONSE, {"error": "User doesn't exist in room."});
                    return;
                }
                speaker.getSocket().emit<SpeakerSetTypeSignal>(SpeakerSignals.SET_TYPE,  {"type_speaker": data.type});
                socket.emit<OkResponse>(ControllerSignals.CONFIGURE_SPEAKER_RESPONSE,  {"ok":true});
            })

            socket.on(ControllerSignals.STOP_MUSIC, (data: ControllerConfigurationRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit(ControllerSignals.CONFIGURE_SPEAKER, {"error": "Selected room doesn't exist."});
                    return;
                }
                room.stopSpeakers();
                socket.emit<OkResponse>(ControllerSignals.STOP_MUSIC_RESPONSE, {"ok": true})
            })

            socket.on(ControllerSignals.PLAY_MUSIC, (data: ControllerPlayRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit(ControllerSignals.PLAY_MUSIC_RESPONSE, {"error": "Selected room doesn't exist."});
                    return;
                }
                room.prepareSpeakers(data.song_id, data.millis_play);
                socket.emit<OkResponse>(ControllerSignals.PLAY_MUSIC_RESPONSE, {"ok": true})
            })



            socket.on('disconnect', () => {
                Logger.Info("user: " + user.getName() + " disconnected from room: " + user.getRoomID());
                if(!this.rooms[user.getRoomID()]){
                    return;
                }
                if(user.getKind() == UserKind.CONTROLLER){
                    this.rooms[user.getRoomID()].removeAllSpeakers("Controller abandoned the room");
                    delete(this.rooms[user.getRoomID()]);
                }
                else {
                    this.rooms[user.getRoomID()].removeSpeaker(user.getID(), null);
                }
            });

        });
    }

}

export default SocketIOController;