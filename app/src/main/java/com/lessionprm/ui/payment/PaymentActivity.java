package com.lessionprm.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.CreatePaymentRequest;
import com.lessionprm.data.model.MoMoPaymentResponse;
import com.lessionprm.data.model.PaymentStatusResponse;
import com.lessionprm.utils.AppConfig;
import com.lessionprm.utils.NetworkUtils;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvCourseName, tvAmount;
    private MaterialButton btnMomo;
    private ProgressBar progressBar;

    private Long courseId;
    private Double amount;
    private String currentOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        setupToolbar();
        initViews();
        getPaymentData();
        setupClickListeners();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
    }

    private void initViews() {
        tvCourseName = findViewById(R.id.tv_course_name);
        tvAmount = findViewById(R.id.tv_amount);
        btnMomo = findViewById(R.id.btn_momo);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void getPaymentData() {
        courseId = getIntent().getLongExtra("courseId", 0L);
        amount = getIntent().getDoubleExtra("amount", 0.0);

        if (courseId == 0L || amount == 0.0) {
            Toast.makeText(this, "Lỗi: Thông tin thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display payment info
        tvCourseName.setText("Khóa học ID: " + courseId);
        tvAmount.setText(String.format("Số tiền: %,.0f VND", amount));
    }

    private void setupClickListeners() {
        btnMomo.setOnClickListener(v -> handleMoMoPayment());
    }

    private void handleMoMoPayment() {
        if (!ValidationUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Create payment request
        String description = "Thanh toán khóa học #" + courseId;
        CreatePaymentRequest request = new CreatePaymentRequest(courseId, amount, description);

        RetrofitClient.getApiService().createPayment(request)
                .enqueue(new Callback<MoMoPaymentResponse>() {
                    @Override
                    public void onResponse(Call<MoMoPaymentResponse> call, Response<MoMoPaymentResponse> response) {
                        showLoading(false);

                        if (NetworkUtils.isResponseSuccessful(response)) {
                            MoMoPaymentResponse paymentResponse = response.body();

                            if (paymentResponse.isSuccess()) {
                                currentOrderId = paymentResponse.getOrderId();
                                openMoMoPayment(paymentResponse.getPayUrl());
                            } else {
                                showError(paymentResponse.getMessage());
                            }
                        } else {
                            handlePaymentError(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<MoMoPaymentResponse> call, Throwable t) {
                        showLoading(false);
                        showError(NetworkUtils.getNetworkErrorMessage(t));
                    }
                });
    }

    private void openMoMoPayment(String payUrl) {
        try {
            if (AppConfig.MoMoConfig.isTestMode()) {
                // In test mode, simulate payment success after a delay
                simulatePaymentSuccess();
            } else {
                // Open MoMo app or web browser for payment
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                startActivity(intent);
                
                // Start polling for payment status
                startPaymentStatusPolling();
            }
        } catch (Exception e) {
            showError("Không thể mở ứng dụng MoMo. Vui lòng cài đặt ứng dụng MoMo.");
        }
    }

    private void simulatePaymentSuccess() {
        Toast.makeText(this, "Chế độ test - Mô phỏng thanh toán thành công", Toast.LENGTH_SHORT).show();
        
        btnMomo.postDelayed(() -> {
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, 2000);
    }

    private void startPaymentStatusPolling() {
        if (currentOrderId == null) return;

        // Poll payment status every 3 seconds for up to 5 minutes
        checkPaymentStatus(0, 100); // max 100 attempts = 5 minutes
    }

    private void checkPaymentStatus(int attempt, int maxAttempts) {
        if (attempt >= maxAttempts) {
            showError("Timeout kiểm tra trạng thái thanh toán. Vui lòng kiểm tra lại sau.");
            return;
        }

        RetrofitClient.getApiService().getPaymentStatus(currentOrderId)
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(Call<PaymentStatusResponse> call, Response<PaymentStatusResponse> response) {
                        if (NetworkUtils.isResponseSuccessful(response)) {
                            PaymentStatusResponse statusResponse = response.body();

                            if (statusResponse.isSuccess()) {
                                String status = statusResponse.getStatus();
                                
                                if ("COMPLETED".equalsIgnoreCase(status)) {
                                    // Payment successful
                                    Toast.makeText(PaymentActivity.this, 
                                        "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                } else if ("FAILED".equalsIgnoreCase(status) || 
                                          "CANCELLED".equalsIgnoreCase(status)) {
                                    // Payment failed
                                    showError("Thanh toán thất bại hoặc bị hủy");
                                } else {
                                    // Still pending, continue polling
                                    btnMomo.postDelayed(() -> 
                                        checkPaymentStatus(attempt + 1, maxAttempts), 3000);
                                }
                            } else {
                                // Continue polling if API call succeeded but payment still pending
                                btnMomo.postDelayed(() -> 
                                    checkPaymentStatus(attempt + 1, maxAttempts), 3000);
                            }
                        } else {
                            // Continue polling on API errors (might be temporary)
                            btnMomo.postDelayed(() -> 
                                checkPaymentStatus(attempt + 1, maxAttempts), 3000);
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentStatusResponse> call, Throwable t) {
                        // Continue polling on network errors
                        btnMomo.postDelayed(() -> 
                            checkPaymentStatus(attempt + 1, maxAttempts), 3000);
                    }
                });
    }

    private void handlePaymentError(int statusCode) {
        String errorMessage = NetworkUtils.getErrorMessage(statusCode);
        showError(errorMessage);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnMomo.setEnabled(!show);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When user returns from MoMo app, check payment status if we have an order ID
        if (currentOrderId != null) {
            checkPaymentStatus(0, 1); // Single check when returning from payment
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}