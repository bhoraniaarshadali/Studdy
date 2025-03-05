package com.example.studdy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Check if it's the first launch using SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (isFirstLaunch) {
                    intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isFirstLaunch", false);
                    editor.apply();
                } else {
                    // Go to LoginActivity instead of MainActivity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, SPLASH_DURATION);
    }
}