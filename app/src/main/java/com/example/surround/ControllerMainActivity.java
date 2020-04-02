package com.example.surround;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.net.Uri;
import android.os.Bundle;


public class ControllerMainActivity extends AppCompatActivity implements ControllerFragment.OnListFragmentInteractionListener {
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
            // fragments are from different classes,
            // different fragments, must change fragment
            currentCentralFragment = f;
            fragmentTransaction.replace(R.id.controller_fragment_container, f);
            // or ft.add(R.id.your_placeholder, new FooFragment());
            // Complete the changes added above
            fragmentTransaction.commit();
        }else if(currentCentralFragment != null &&
                currentCentralFragment.getClass().equals(ControllerFragment.class)){
                if(currentCentralFragment.getClass().equals(ControllerFragment.class)) {
                    ((ControllerFragment) currentCentralFragment).scrollToStart();
                }

        }
    }


    @Override
    public void onListFragmentInteraction(Song song){

    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }

}
