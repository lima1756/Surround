import UserKind from '../constants/UserKind.enum';
import UserStatus from '../constants/UserStatus.enum';
import { Socket } from '../types/Socket.type';
import Equalizer from '../constants/Equalizer.enum';

class User {
    private id: string;
    private name: string;
    private kind: UserKind;
    private socket: Socket;
    private roomID: string;
    private status: UserStatus;
    private equalizerType: Equalizer;

    constructor(socket: Socket){
        this.id = Date.now().toString();
        this.socket = socket;
        this.roomID = "";
        this.kind = UserKind.UNDEFINED;
        this.name = "";
        this.status = UserStatus.UNDEFINED;
        this.equalizerType = Equalizer.NONE;
    }

    setKind(kind: UserKind){
        this.kind = kind;
    }

    setName(name: string){
        this.name = name;
    }

    setRoomID(id: string){
        this.roomID = id;
    }

    setStatus(status: UserStatus){
        this.status = status;
    }

    setEqualizerType(type: Equalizer){
        this.equalizerType = type;
    }

    getRoomID():string{
        return this.roomID;
    }

    getSocket(): Socket{
        return this.socket;
    }

    getName(): string{
        return this.name;
    }

    getKind(): UserKind{
        return this.kind;
    }

    getID(): string{
        return this.id;
    }

    getStatus(): UserStatus{
        return this.status;
    }

    getEqualizerType(): Equalizer{
        return this.equalizerType;
    }
}

export default User;