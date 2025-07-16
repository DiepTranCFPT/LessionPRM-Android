package com.lessionprm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.LoginRequest;
import com.lessionprm.data.model.LoginResponse;
import com.lessionprm.ui.main.MainActivity;
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private CheckBox cbRemember;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        cbRemember = findViewById(R.id.cb_remember);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> openRegisterActivity());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        boolean isValid = true;

        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }

        if (!isValid) return;

        // Check network connectivity
        if (!ValidationUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading and perform login
        showLoading(true);
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        
        RetrofitClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.isSuccess()) {
                        handleLoginSuccess(loginResponse);
                    } else {
                        showError(loginResponse.getMessage());
                    }
                } else {
                    handleLoginError(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                showError("Kết nối mạng bị lỗi. Vui lòng thử lại.");
            }
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        // Save auth data
        PrefsHelper.saveAuthToken(this, response.getToken());
        PrefsHelper.saveRefreshToken(this, response.getRefreshToken());
        
        if (response.getUser() != null) {
            PrefsHelper.saveUserInfo(this, 
                response.getUser().getId(),
                response.getUser().getEmail(),
                response.getUser().getFullName(),
                response.getUser().getRole());
        }

        // Show success message
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 401:
                errorMessage = "Email hoặc mật khẩu không chính xác";
                break;
            case 403:
                errorMessage = "Tài khoản đã bị khóa";
                break;
            case 500:
                errorMessage = "Lỗi máy chủ. Vui lòng thử lại sau";
                break;
            default:
                errorMessage = "Đăng nhập thất bại. Vui lòng thử lại";
        }
        showError(errorMessage);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleForgotPassword() {
        // TODO: Implement forgot password functionality
        Toast.makeText(this, "Tính năng quên mật khẩu sẽ được cập nhật sớm", Toast.LENGTH_SHORT).show();
    }
}