package com.example.surround.Controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.surround.Controller.Utils.ControllerSocket;
import com.example.surround.MainActivity;
import com.example.surround.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Stack;


public class ControllerActivity extends AppCompatActivity {
    private Stack<Fragment> fragmentHistory = new Stack<>();
    private Fragment currentFragment;

    private MaterialToolbar topAppBar;
    private ControllerSocket app;
    private FrameLayout fragmentContainer;
    private CoordinatorLayout.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        app = ControllerSocket.getInstance();
        fragmentContainer =  findViewById(R.id.controller_fragment_container);
        params = (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
        currentFragment = new AllMusicFragment();
        replaceFragment();

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
        topAppBar.setTitle("Room: " + ControllerSocket.getInstance().getRoomToken());
    }

    public void pushFragment(Fragment f){
        fragmentHistory.push(currentFragment);
        currentFragment = f;
        marginControl();
        replaceFragment();
    }

    public void popFragment(){
        currentFragment = fragmentHistory.pop();
        marginControl();
        replaceFragment();
    }

    private void replaceFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.controller_fragment_container, currentFragment);
        fragmentTransaction.commit();
    }

    public void marginControl(){
        if(currentFragment instanceof AllMusicFragment){
            params.setMargins(0,0,0,0);
        }
        else {
            ((AppBarLayout)findViewById(R.id.appBarLayout)).setExpanded(true);
            params.setMargins(0,0,0, getSoftButtonsBarSizePort(this));
        }
        fragmentContainer.setLayoutParams(params);
    }

    //Hardware Back Button method
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && !fragmentHistory.isEmpty()) {
            popFragment();
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK && currentFragment instanceof AllMusicFragment){
            ((AllMusicFragment) currentFragment).scrollToStart();
        }
        return false;
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
}
