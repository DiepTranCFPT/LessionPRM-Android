package com.lessionprm.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.ui.auth.LoginActivity;
import com.lessionprm.utils.PrefsHelper;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Retrofit client with context
        RetrofitClient.init(this);
        
        // Check login status after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DELAY);
    }
    
    private void checkLoginStatus() {
        Intent intent;
        
        if (PrefsHelper.isLoggedIn(this)) {
            // User is logged in, go to main activity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User is not logged in, go to login activity
            intent = new Intent(this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}