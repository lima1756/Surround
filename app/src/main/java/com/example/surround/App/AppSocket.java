package com.example.surround.App;

import com.example.surround.Utils.Constants;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.app.Application;

import java.net.URISyntaxException;

public class AppSocket extends Application {
    private  Socket mSocket;
    private static AppSocket app;

    private String sessionToken;
    private String name;
    private int typeOfSpeaker;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTypeOfSpeaker() {
        return typeOfSpeaker;
    }

    public void setTypeOfSpeaker(int typeOfSpeaker) {
        this.typeOfSpeaker = typeOfSpeaker;
    }


    public  static AppSocket getInstance() {
        if (app==null) app = new AppSocket();
        return app;
    }
    private AppSocket(){
        typeOfSpeaker = Constants.EQUALIZER_CENTER_SPEAKER;
        try {
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() { return mSocket; }
}
