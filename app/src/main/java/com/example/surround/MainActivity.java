package com.example.surround;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    TextView controller, desController, speaker, desSpeaker;
    ImageView ivController, ivSpeaker;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setComponents();
    }

    private void setComponents(){
        controller = findViewById(R.id.tv_controller);
        desController = findViewById(R.id.tv_des_cont);
        desSpeaker = findViewById(R.id.tv_des_speaker);
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
        Intent i = new Intent(this, ControllerMainActivity.class);
        startActivity(i);
    }
}
