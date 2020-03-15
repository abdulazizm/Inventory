package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

    LocalDb db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        db = new LocalDb(this);

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                //Toast.makeText(getApplicationContext(),""+db.checkUserLoggedin(),Toast.LENGTH_SHORT).show();
                if(!db.alreadyRegistered()){
                    Intent register = new Intent(getApplicationContext(),RegisterActivity.class);
                    startActivity(register);
                    finish();
                }
                else if(!db.checkUserLoggedin()){
                    Intent login = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(login);
                    finish();
                }
                else {
                    Intent home = new Intent(getApplicationContext(),BottomNav.class);
                    startActivity(home);
                    finish();
                }
            }
        }, 2000);
    }
}
