export interface Socket extends SocketIO.Socket{
    emit<T>(event: string | symbol, response:T):boolean;
}