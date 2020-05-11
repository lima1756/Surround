package com.example.surround.Utils;

import com.example.surround.Utils.Constants;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.app.Application;

import java.net.URISyntaxException;

public class AppSocket extends Application {
    private  Socket mSocket;


    private String roomToken;
    private String name;


    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String sessionToken) {
        this.roomToken = roomToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    protected AppSocket(){

        try {
            mSocket = IO.socket(Constants.SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() { return mSocket; }
}
