package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.surround.App.AppSocket;
import com.example.surround.Utils.Constants;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class SpeakerMainActivity extends AppCompatActivity {

    EditText etToken;
    AppSocket app;

    View.OnKeyListener onEnterKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                int token =-1;
                token= Integer.parseInt(((EditText)v).getText().toString());
                ((EditText)v).setText("");
                logIn(token);
                return true;
            }
            return false;
        }
    };

    private Emitter.Listener socketOnLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int typeOfSpeaker, id, tokenSession;
            try {
                typeOfSpeaker = data.getInt(Constants.SOCKET_PARAM_TOKEN);
                id =data.getInt(Constants.SOCKET_PARAM_ID);
                tokenSession =data.getInt(Constants.SOCKET_PARAM_TOKEN);
                if(app==null) app = AppSocket.getInstance();
                app.setHashId(id);
                app.setSessionToken(tokenSession);
                app.setTypeOfSpeaker(typeOfSpeaker);
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent(SpeakerMainActivity.this,SpeakerPlayingActivity.class);
            setResult(RESULT_OK, intent);
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_main);
        app = AppSocket.getInstance();
        setComponents();
        app.getSocket().on("login", socketOnLogin);
    }

    public void setComponents(){
        etToken = findViewById(R.id.et_token);
        etToken.setOnKeyListener(onEnterKey);
    }

    private void logIn(int token){
        if(token!=-1){
            app.getSocket().emit(Constants.SOCKET_EMIT_LOGIN_SPEAKER, token);
        }
    }

    @Override
    protected void onDestroy() {
        app.getSocket().off("login", socketOnLogin);
        super.onDestroy();

    }
}
