package com.example.surround.Speaker.Utils;

import com.example.surround.Utils.AppSocket;
import com.example.surround.Utils.Constants;

public class SpeakerSocket extends AppSocket {

    private static SpeakerSocket app;
    private int typeOfSpeaker;

    public int getTypeOfSpeaker() {
        return typeOfSpeaker;
    }

    public void setTypeOfSpeaker(int typeOfSpeaker) {
        this.typeOfSpeaker = typeOfSpeaker;
    }


    public  static SpeakerSocket getInstance() {
        if (app==null) app = new SpeakerSocket();
        return app;
    }

    private SpeakerSocket() {
        super();
        typeOfSpeaker = Constants.EQUALIZER_CENTER_SPEAKER;
    }
}
