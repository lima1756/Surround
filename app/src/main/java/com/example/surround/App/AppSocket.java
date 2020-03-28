package com.example.surround.App;

import com.example.surround.Utils.Constants;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.app.Application;

import java.net.URISyntaxException;

public class AppSocket extends Application {
    private  Socket mSocket;
    private static AppSocket app;
    private int sessionToken;

    private int hashId;
    private int typeOfSpeaker;

    public int getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(int sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getHashId() {
        return hashId;
    }

    public void setHashId(int hashId) {
        this.hashId = hashId;
    }

    public int getTypeOfSpeaker() {
        return typeOfSpeaker;
    }

    public void setTypeOfSpeaker(int typeOfSpeaker) {
        this.typeOfSpeaker = typeOfSpeaker;
    }


    public  static AppSocket getInstance() {
        if (app==null) {
            app = new AppSocket();
        }
        return app;
    }
    private AppSocket(){
        try {
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
