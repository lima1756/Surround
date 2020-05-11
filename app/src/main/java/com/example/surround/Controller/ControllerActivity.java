package com.example.surround.Controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.example.surround.Controller.Utils.ControllerSocket;
import com.example.surround.MainActivity;
import com.example.surround.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;


public class ControllerActivity extends AppCompatActivity {
    private Fragment previousFragment;
    private Fragment currentCentralFragment;

    private MaterialToolbar topAppBar;
    private ControllerSocket app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        replaceFragment(new AllMusicFragment());
        app = ControllerSocket.getInstance();

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.close){
                    app.getSocket().disconnect();
                    Intent intent = new Intent(ControllerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
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
