package com.example.surround.Speaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.surround.R;
import com.example.surround.Speaker.Utils.SpeakerSocket;
import com.example.surround.Utils.Constants;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class SpeakerMainActivity extends AppCompatActivity {

    EditText etToken, etName;
    TextView tvName, tvToken, tvConnecting;
    SpeakerSocket app;
    String sessionToken, name;
    Button btnConnect;

    View.OnClickListener onEnter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String token ="";
            token=etToken.getText().toString();
            Log.d("TOKEN_GET",token);
            if(token.equals("")){
                Toast.makeText(getApplicationContext(), "Insert a valid token.", Toast.LENGTH_LONG);
                return;
            }
            SpeakerMainActivity.this.sessionToken = token;
            etToken.setText("");

            String name ="";
            name=etName.getText().toString();
            Log.d("NAME_GET",name);
            if(name.equals("")){
                Toast.makeText(getApplicationContext(), "Insert a name!.", Toast.LENGTH_LONG);
                return;
            }
            etName.setText("");
            logIn(name);
        }
    };

    private Emitter.Listener socketOnLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("LOGIN", "answered");
            int typeOfSpeaker;
            try {
                typeOfSpeaker = data.getInt(Constants.SOCKET_PARAM_TYPE_SPEAKER);
                if(app==null) app = SpeakerSocket.getInstance();
                app.setName(SpeakerMainActivity.this.name);
                app.setRoomToken(SpeakerMainActivity.this.sessionToken);
                app.setTypeOfSpeaker(typeOfSpeaker);
            } catch (JSONException e) {
                Log.d("LOGIN", "login failed ");
                SpeakerMainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Selected Room doesn't exist", Toast.LENGTH_LONG ).show();
                        initialView();
                        etName.setText(SpeakerMainActivity.this.name);
                        etToken.setText("");
                    }
                });

                return;
            }

            Intent intent = new Intent(SpeakerMainActivity.this, SpeakerPlayingActivity.class);
            setResult(RESULT_OK, intent);
            startActivity(intent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaker_main);
        app = SpeakerSocket.getInstance();
        setComponents();
        app.getSocket().on(Constants.SOCKET_ON_LOGIN_RESPONSE, socketOnLogin);
        if(!app.getSocket().connected()) app.getSocket().connect();
    }

    public void setComponents(){
        tvConnecting = findViewById(R.id.tv_connecting);

        etToken = findViewById(R.id.et_token);
        etName = findViewById(R.id.et_name);

        tvName = findViewById(R.id.tv_insert_name);
        tvToken = findViewById(R.id.tv_insert_token);

        btnConnect = findViewById(R.id.btn_enter_room);
        btnConnect.setOnClickListener(onEnter);
        initialView();
    }

    private void initialView(){
        tvConnecting.setVisibility(View.GONE);

        etToken.setVisibility(View.VISIBLE);
        etName.setVisibility(View.VISIBLE);

        tvName.setVisibility(View.VISIBLE);
        tvToken.setVisibility(View.VISIBLE);

        btnConnect.setVisibility(View.VISIBLE);
    }

    private void waitingView(){
        tvConnecting.setVisibility(View.VISIBLE);

        etToken.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);

        tvName.setVisibility(View.GONE);
        tvToken.setVisibility(View.GONE);

        btnConnect.setVisibility(View.GONE);
    }

    private void logIn(String name){
        waitingView();
        if(sessionToken==null || name==null) {
            Toast.makeText(getApplicationContext(), "Error inserting values", Toast.LENGTH_LONG);
            initialView();
            return;
        }
        if(sessionToken.equals("") || name.equals("")){
            Toast.makeText(getApplicationContext(), "Error inserting values", Toast.LENGTH_LONG);
            initialView();
            return;
        }

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
