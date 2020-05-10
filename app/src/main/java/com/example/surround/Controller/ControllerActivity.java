package com.example.surround.Controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import com.example.surround.R;


public class ControllerActivity extends AppCompatActivity {
    public Fragment previousFragment;
    public Fragment currentCentralFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        replaceFragment(new AllMusicFragment());
    }

    public void replaceFragment(Fragment f){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(currentCentralFragment == null || !currentCentralFragment.getClass().equals(f.getClass())){
            previousFragment = currentCentralFragment;
            currentCentralFragment = f;
            fragmentTransaction.replace(R.id.controller_fragment_container, f);
            fragmentTransaction.commit();
        } else if(currentCentralFragment instanceof AllMusicFragment){
            previousFragment = currentCentralFragment;
            currentCentralFragment = f;
            fragmentTransaction.replace(R.id.song_fragment_container, f);
            fragmentTransaction.commit();
        } else if(currentCentralFragment != null &&
                currentCentralFragment.getClass().equals(AllMusicFragment.class)){
            ((AllMusicFragment) currentCentralFragment).scrollToStart();
        }
    }

    //Hardware Back Button method
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            replaceFragment(previousFragment);
            return true;
        }

        return false;
    }
}
