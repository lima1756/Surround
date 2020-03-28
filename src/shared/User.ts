import UserKind from '../constants/UserKind.enum';
import UserStatus from '../constants/UserStatus.enum';
import { Socket } from 'src/types/Socket.type';

class User {
    private id: string;
    private name: string;
    private kind: UserKind;
    private socket: Socket;
    private roomID: string;
    private status: UserStatus;

    constructor(socket: Socket){
        this.id = Date.now().toString();
        this.socket = socket;
        this.roomID = "";
        this.kind = UserKind.UNDEFINED;
        this.name = "";
        this.status = UserStatus.UNDEFINED;
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
}

export default User;