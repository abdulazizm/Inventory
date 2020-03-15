package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;


public class BottomNav extends AppCompatActivity implements  BottomNavigationView.OnNavigationItemSelectedListener {

    private FrameLayout frameLayout;
    private Boolean exit = false;
    HomeFragment t = new HomeFragment();


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);


        frameLayout = findViewById(R.id.fragmentContainer);
        BottomNavigationView navigation = findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(this);

        //to call home frag first
        load(t);
    }

    boolean isLocationPermissionEnable(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);
            return false;


        }
        return true;

    }

    public boolean load(Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment f = null;
        switch (menuItem.getItemId()) {

            case R.id.navigation_home:
                f = new HomeFragment();
                break;
            case R.id.navigation_timer:
                f = new OrdersFragment();
                break;
            case R.id.navigation_settings:
                f = new SettingsFragment();
                break;
            case R.id.navigation_graph:
                f = new Graph();
                break;


        }
        return load(f);
    }

    @Override
    public void onBackPressed(){
        if(exit){
            finish();
        }
        else{
            Toast.makeText(this,"Press back again to Exit",Toast.LENGTH_SHORT).show();
            exit=true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit=false;
                }
            },3*1000);
        }
    }
}
