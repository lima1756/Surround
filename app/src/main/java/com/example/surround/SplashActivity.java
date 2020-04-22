package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
        private long ms=0;
        private long splashTime=5000;
        private long sleepTime=100;
        private boolean splashActive = true;
        private boolean paused=false;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);
            animation();
            Thread mythread = new Thread() {
                public void run() {
                    try {
                        while (splashActive && ms < splashTime) {
                            if(!paused)  ms+=sleepTime;
                            sleep(sleepTime);
                        }
                    } catch(Exception e) {}
                    finally {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            };
            mythread.start();
        }

        private void animation(){
            ConstraintLayout cl = findViewById(R.id.bkg_splash);
            AnimationDrawable ad = (AnimationDrawable) cl.getBackground();
            ad.setEnterFadeDuration(400);
            ad.setExitFadeDuration(200);
            ad.mutate();
            ad.start();
        }
    }
