package com.example.surround.Controller.Utils;

import com.example.surround.Utils.AppSocket;

public class ControllerSocket extends AppSocket {
    private static ControllerSocket app;

    public  static ControllerSocket getInstance() {
        if (app==null) app = new ControllerSocket();
        return app;
    }

    private ControllerSocket() {
        super();
    }

}
