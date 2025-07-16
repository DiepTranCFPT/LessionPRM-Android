package com.lessionprm.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.RegisterRequest;
import com.lessionprm.data.model.RegisterResponse;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Clear previous errors
        clearErrors();

        // Validate inputs
        boolean isValid = validateInputs(fullName, email, phone, password, confirmPassword);

        if (!isValid) return;

        // Check network connectivity
        if (!ValidationUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading and perform registration
        showLoading(true);
        performRegister(fullName, email, password, phone);
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private boolean validateInputs(String fullName, String email, String phone, String password, String confirmPassword) {
        boolean isValid = true;

        String nameError = ValidationUtils.getNameError(fullName);
        if (nameError != null) {
            tilFullName.setError(nameError);
            isValid = false;
        }

        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }

        String phoneError = ValidationUtils.getPhoneError(phone);
        if (phoneError != null) {
            tilPhone.setError(phoneError);
            isValid = false;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }

        String confirmPasswordError = ValidationUtils.getConfirmPasswordError(password, confirmPassword);
        if (confirmPasswordError != null) {
            tilConfirmPassword.setError(confirmPasswordError);
            isValid = false;
        }

        return isValid;
    }

    private void performRegister(String fullName, String email, String password, String phone) {
        RegisterRequest request = new RegisterRequest(fullName, email, password, phone);
        
        RetrofitClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    
                    if (registerResponse.isSuccess()) {
                        handleRegisterSuccess();
                    } else {
                        showError(registerResponse.getMessage());
                    }
                } else {
                    handleRegisterError(response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                showError("Kết nối mạng bị lỗi. Vui lòng thử lại.");
            }
        });
    }

    private void handleRegisterSuccess() {
        Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
        
        // Return to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void handleRegisterError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 400:
                errorMessage = "Thông tin đăng ký không hợp lệ";
                break;
            case 409:
                errorMessage = "Email này đã được sử dụng";
                break;
            case 500:
                errorMessage = "Lỗi máy chủ. Vui lòng thử lại sau";
                break;
            default:
                errorMessage = "Đăng ký thất bại. Vui lòng thử lại";
        }
        showError(errorMessage);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        etFullName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPhone.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }
}