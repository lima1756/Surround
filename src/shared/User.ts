import UserKind from '../constants/UserKind';
import UserStatus from 'src/constants/UserStatus';

class User {
    private id: string;
    private name: string;
    private kind: UserKind;
    private socket: SocketIO.Socket;
    private roomID: string;
    private status: UserStatus;

    constructor(socket: SocketIO.Socket){
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

    getSocket(): SocketIO.Socket{
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