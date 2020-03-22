import {Logger} from '@overnightjs/logger';
import Room from '../../shared/Room';
import User from '../../shared/User'
import UserKind from 'src/constants/UserKind';
import UserStatus from 'src/constants/UserStatus';
import SpeakerSocket from 'src/constants/SpeakerSocket';
import ControllerSocket from 'src/constants/ControllerSocket';
import { SpeakerLoginRequest } from 'src/types/Speaker.types';
import { ControllerConfigurationRequest, ControllerPlayRequest } from 'src/types/Controller.types';


class SocketIOController {

    private io: SocketIO.Server;
    private rooms: {[id:string]: Room}

    constructor(io: SocketIO.Server) {
        Logger.Info("Configuring Socket io");
        this.rooms = {}
        this.io = io;
    }

    public socketEvents() {
        this.io.on('connection', (socket) => {
            Logger.Info("new connection");
            let user = new User(socket);

            // Speaker listeners
            socket.on(SpeakerSocket.LOGIN, (data: SpeakerLoginRequest) => {
                if(this.rooms[data.room]) {
                    user.setName(data.name);
                    user.setKind(UserKind.SPEAKER)
                    user.setRoomID(data.room);
                    this.rooms[data.room].addSpeaker(user);
                    socket.emit("login", {"typeOfSpeaker": 2});
                }
                else {
                    socket.emit("login", {"error": "Selected room doesn't exist."});
                }
            });

            socket.on(SpeakerSocket.READY, () => {
                user.setStatus(UserStatus.READY);
                const room = this.rooms[user.getRoomID()];
                if(room.speakersReady()){
                    room.playSpeakers();
                }
            });

            // Controller listeners
            socket.on(ControllerSocket.LOGIN, (data) => {
                let id = Room.genID();
                while(this.rooms[id]){
                    id = Room.genID();
                }
                user.setName(data.name);
                user.setKind(UserKind.CONTROLLER);
                let room = new Room(user, id);
                user.setRoomID(id);
                this.rooms[id] = room;
                socket.emit("")
            });

            socket.on(ControllerSocket.CONFIGURE_SPEAKER, (data: ControllerConfigurationRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit(ControllerSocket.CONFIGURE_SPEAKER, {"error": "Selected room doesn't exist."});
                    return;
                }
                const speaker = room.getSpeaker(data.speaker_id);
                if(!speaker){
                    socket.emit(ControllerSocket.CONFIGURE_SPEAKER, {"error": "User doesn't exist in room."});
                    return;
                }
                speaker.getSocket().emit("onSetSpeaker", {"typeOfSpeaker": data.type})
            })

            socket.on(ControllerSocket.STOP_MUSIC, (data: ControllerConfigurationRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit(ControllerSocket.CONFIGURE_SPEAKER, {"error": "Selected room doesn't exist."});
                    return;
                }
                room.stopSpeakers();
            })

            socket.on(ControllerSocket.PLAY_MUSIC, (data: ControllerPlayRequest) => {
                const room = this.rooms[user.getRoomID()];
                if(!room){
                    socket.emit(ControllerSocket.CONFIGURE_SPEAKER, {"error": "Selected room doesn't exist."});
                    return;
                }
                room.prepareSpeakers(data.song_id);
            })



            socket.on('disconnect', () => {
                Logger.Info("user: " + user.getName + " disconnected from room: " + user.getRoomID);
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