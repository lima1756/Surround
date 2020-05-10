package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.surround.Controller.ControllerActivity;
import com.example.surround.Speaker.SpeakerMainActivity;

public class MainActivity extends AppCompatActivity {

    TextView controller, desController, speaker, desSpeaker;
    ImageView ivController, ivSpeaker;

    //-----------VIEW LISTENERS--------------------------------------
    View.OnClickListener onClickSpeaker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToSpeaker();
        }
    };

    View.OnClickListener onClickController = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goToController();
        }
    };
    //---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setComponents();
    }

    private void setComponents(){
        controller = findViewById(R.id.tv_controller);
        desController = findViewById(R.id.tv_des_cont);
        desSpeaker = findViewById(R.id.tv_des_sp);
        desSpeaker.setVisibility(View.VISIBLE);
        speaker = findViewById(R.id.tv_speaker);

        ivController = findViewById(R.id.iv_controller);
        ivSpeaker = findViewById(R.id.iv_speaker);

        controller.setOnClickListener(onClickController);
        desController.setOnClickListener(onClickController);
        ivController.setOnClickListener(onClickController);

        ivSpeaker.setOnClickListener(onClickSpeaker);
        desSpeaker.setOnClickListener(onClickSpeaker);
        speaker.setOnClickListener(onClickSpeaker);

    }

    private void goToSpeaker(){
        Intent i = new Intent(this, SpeakerMainActivity.class);
        startActivity(i);
    }

    private void goToController(){
        Intent i = new Intent(this, ControllerActivity.class);
        startActivity(i);
    }
}
