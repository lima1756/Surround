package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.surround.App.AppSocket;
import com.example.surround.Utils.Constants;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class SpeakerMainActivity extends AppCompatActivity {

    EditText etToken, etName;
    TextView tvName, tvToken, tvConnecting;
    AppSocket app;
    String sessionToken, name;

    TextView.OnEditorActionListener  onEnterToken = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                //do what you want on the press of 'done'
                return onEnterPressedToken();
            }
            return false;
        }
    };

    TextView.OnEditorActionListener  onEnterName = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                //do what you want on the press of 'done'
                return onEnterPressedName();
            }
            return false;
        }
    };


    public boolean onEnterPressedToken(){
        String token ="";
        token=etToken.getText().toString();
        Log.d("TOKEN_GET",token);
        if(token.equals("")){
            Toast.makeText(getApplicationContext(), "Insert a valid token.", Toast.LENGTH_LONG);
            return false;
        }
        SpeakerMainActivity.this.sessionToken = token;
        etToken.setText("");
        changeView();
        return true;
    }

    public boolean onEnterPressedName(){
        String name ="";
        name=etName.getText().toString();
        Log.d("NAME_GET",name);
        if(name.equals("")){
            Toast.makeText(getApplicationContext(), "Insert a name!.", Toast.LENGTH_LONG);
            return false;
        }
        etName.setText("");
        logIn(name);
        return true;
    }

    private Emitter.Listener socketOnLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("LOGIN", "answered");
            int typeOfSpeaker;
            try {
                typeOfSpeaker = data.getInt(Constants.SOCKET_PARAM_TYPE_SPEAKER);
                if(app==null) app = AppSocket.getInstance();
                app.setName(SpeakerMainActivity.this.name);
                app.setSessionToken(SpeakerMainActivity.this.sessionToken);
                app.setTypeOfSpeaker(typeOfSpeaker);
            } catch (JSONException e) {
                Log.d("LOGIN", "login failed ");
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
        app.getSocket().on(Constants.SOCKET_ON_LOGIN_RESPONSE, socketOnLogin);
        if(!app.getSocket().connected()) app.getSocket().connect();
    }

    public void setComponents(){
        tvConnecting = findViewById(R.id.tv_connecting);
        tvConnecting.setVisibility(View.GONE);

        etToken = findViewById(R.id.et_token);
        etToken.setOnEditorActionListener(onEnterToken);
        etToken.setVisibility(View.VISIBLE);

        etName = findViewById(R.id.et_name);
        etName.setVisibility(View.GONE);
        etName.setOnEditorActionListener(onEnterName);

        tvName = findViewById(R.id.tv_insert_name);
        tvName.setVisibility(View.GONE);

        tvToken = findViewById(R.id.tv_insert_token);
        tvToken.setVisibility(View.VISIBLE);
    }

    private void changeView(){

        etToken.setVisibility(View.GONE);
        etName.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.VISIBLE);
        tvToken.setVisibility(View.GONE);
        tvConnecting.setVisibility(View.GONE);
    }

    private void waitingView(){
        tvConnecting.setVisibility(View.VISIBLE);
        etToken.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);
        tvName.setVisibility(View.GONE);
        tvToken.setVisibility(View.GONE);
    }

    private void logIn(String name){
        waitingView();
        if(sessionToken==null) return;
        if(sessionToken.equals("")) return;
        if(name==null) return;
        if(name.equals("")) return;
        this.name = name;
        JSONObject jsonObject = new JSONObject();
        Log.d("SOCKET",""+app.getSocket().toString());
        try {
            Log.d("TOKEN_SEND","name: "+name +" token: "+sessionToken);
            jsonObject.put(Constants.SOCKET_PARAM_TOKEN, sessionToken);
            jsonObject.put(Constants.SOCKET_PARAM_NAME, name);
            app.getSocket().emit(Constants.SOCKET_EMIT_LOGIN_SPEAKER, jsonObject);
        }catch (JSONException e){
            Toast.makeText(getApplicationContext(), "Error while sending token and name.", Toast.LENGTH_LONG);
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        setComponents();
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        setComponents();
    }
    @Override
    protected void onDestroy() {
        app.getSocket().off("login", socketOnLogin);
        super.onDestroy();

    }
}
