package com.example.project_oujdashop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.utils.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Handler to delay the start of the next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if user is already logged in
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            Intent intent;

            if (sessionManager.isLoggedIn()) {
                // User is logged in, go to MainActivity
                intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to LoginActivity
                intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Close this activity
        }, SPLASH_TIMEOUT);
    }
} 