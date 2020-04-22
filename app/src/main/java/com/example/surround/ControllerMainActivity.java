package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;


public class ControllerMainActivity extends AppCompatActivity implements ControllerFragment.OnListFragmentInteractionListener {
    public Fragment previousFragment;
    public Fragment currentCentralFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_main);

        replaceFragment(new ControllerFragment());
    }

    public void replaceFragment(Fragment f){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(currentCentralFragment == null || !currentCentralFragment.getClass().equals(f.getClass())){
            previousFragment = currentCentralFragment;
            currentCentralFragment = f;
            fragmentTransaction.replace(R.id.controller_fragment_container, f);
            fragmentTransaction.commit();
        } else if(currentCentralFragment instanceof ControllerFragment){
            previousFragment = currentCentralFragment;
            currentCentralFragment = f;
            fragmentTransaction.replace(R.id.song_fragment_container, f);
            fragmentTransaction.commit();
        } else if(currentCentralFragment != null &&
                currentCentralFragment.getClass().equals(ControllerFragment.class)){
            ((ControllerFragment) currentCentralFragment).scrollToStart();
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

    @Override
    public void onListFragmentInteraction(Song song){

    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }

}
