package com.lessionprm.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.LoginResponse;
import com.lessionprm.data.model.RefreshRequest;
import com.lessionprm.ui.auth.LoginActivity;
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        if (!ValidationUtils.isNetworkAvailable(this)) {
            // No network - proceed with stored auth status
            navigateBasedOnAuthStatus();
            return;
        }
        
        if (PrefsHelper.isLoggedIn(this) && PrefsHelper.getAuthToken(this) != null) {
            // Try to validate token with a simple API call or refresh if needed
            validateTokenAndNavigate();
        } else {
            // User is not logged in, go to login activity
            navigateToLogin();
        }
    }
    
    private void validateTokenAndNavigate() {
        String refreshToken = PrefsHelper.getRefreshToken(this);
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // Try to refresh token to validate it's still valid
            RefreshRequest request = new RefreshRequest(refreshToken);
            
            RetrofitClient.getApiService().refreshToken(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Token is valid, update it and go to main activity
                        LoginResponse loginResponse = response.body();
                        PrefsHelper.saveAuthToken(SplashActivity.this, loginResponse.getToken());
                        if (loginResponse.getRefreshToken() != null) {
                            PrefsHelper.saveRefreshToken(SplashActivity.this, loginResponse.getRefreshToken());
                        }
                        navigateToMain();
                    } else {
                        // Token is invalid, clear data and go to login
                        PrefsHelper.clearAuthData(SplashActivity.this);
                        navigateToLogin();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    // Network error or server error - proceed with stored auth status
                    navigateBasedOnAuthStatus();
                }
            });
        } else {
            // No refresh token, clear auth data and go to login
            PrefsHelper.clearAuthData(this);
            navigateToLogin();
        }
    }
    
    private void navigateBasedOnAuthStatus() {
        if (PrefsHelper.isLoggedIn(this)) {
            navigateToMain();
        } else {
            navigateToLogin();
        }
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}