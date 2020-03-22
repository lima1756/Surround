import * as path from 'path';
import * as express from 'express';
import * as bodyParser from 'body-parser';
import * as controllers from './controllers';
import { Server } from '@overnightjs/core';
import { Logger } from '@overnightjs/logger';
import * as socketIO from 'socket.io'
import SocketIOController from './controllers/socketio/SocketIOController';
import * as cors from 'cors';
import * as http from 'http';
import fileUpload = require('express-fileupload');

interface ProcessEnv {
    [key: string]: string | undefined
}

class SorroundServer extends Server {

    private port: number | string;
    private static readonly PORT: number = 3001;
    private static readonly SERVER_START_MSG: string = 'Server started on port: ';
    private static readonly DEV_MSG: string = 'Express Server is running in development mode. ' + 
        'No front-end content is being served.';
    private static readonly PATH: string = path.join(__dirname, 'public', 'messaging-react');

    constructor() {
        super(true);
        this.app.use((req, res, next) => {
            res.header('Access-Control-Allow-Origin', req.get('Origin') || '*');
            res.header('Access-Control-Allow-Credentials', 'true');
            res.header('Access-Control-Allow-Methods', 'GET,HEAD,PUT,PATCH,POST,DELETE');
            res.header('Access-Control-Expose-Headers', 'Content-Length');
            res.header('Access-Control-Allow-Headers', 'Accept, Authorization, Content-Type, X-Requested-With, Range');
            return next();
        })
        this.app.use(bodyParser.json());
        this.app.use(bodyParser.urlencoded({extended: true}));
        this.app.use(express.static(SorroundServer.PATH));
        this.app.use(fileUpload())
        this.port = process.env.PORT || SorroundServer.PORT;
        
        this.setupControllers();
        if (process.env.NODE_ENV !== 'production'){
            this.app.get('/', (req, res) => res.send(SorroundServer.DEV_MSG));
        }
        else{
            this.app.get('/', (req, res) => res.sendFile(path.join(SorroundServer.PATH, 'index.html' )));
        }
    }

    private setupControllers(): void {
        const ctlrInstances =  [];
        for (const name in controllers) {
            if (controllers.hasOwnProperty(name)) { 
                let Controller = (controllers as any)[name];
                ctlrInstances.push(new Controller());
            }
        }
        super.addControllers(ctlrInstances);
    }

    public start(): void {
        const server = new http.Server(this.app);
        const io = new SocketIOController(socketIO.listen(server));
        server.listen(this.port, () => {
            Logger.Imp(SorroundServer.SERVER_START_MSG + this.port);
        });
        io.socketEvents();
    }
}

export default SorroundServer;