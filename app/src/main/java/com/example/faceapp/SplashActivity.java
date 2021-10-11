package com.example.faceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {


    //     <---- Creating a Splash Screen Activity Here !!! ---->

    public static int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//     <---- Using Intent here for moving into another activity !!! ---->

        new Handler().postDelayed(() ->{
            Intent homeIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }, SPLASH_TIME_OUT);


    }
}