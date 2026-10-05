package com.example.smartautofiller.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartautofiller.R;
import com.example.smartautofiller.security.PinManager;

/**
 * Launcher activity that checks user setup, PIN authentication, and routes to MainActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNextScreen, SPLASH_DELAY_MS);
    }

    private void routeNextScreen() {
        SharedPreferences prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean("onboarding_done", false);

        Intent intent;
        if (!onboardingDone) {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
