package com.lessionprm.ui.payment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvCourseName, tvAmount, tvDescription;
    private MaterialButton btnMomo;
    private ProgressBar progressBar;

    private Long courseId;
    private Double amount;
    private String currentOrderId;
    private Handler statusHandler = new Handler(Looper.getMainLooper());
    private Runnable statusCheckRunnable;

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
        tvDescription = findViewById(R.id.tv_description);
        btnMomo = findViewById(R.id.btn_momo);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void getPaymentData() {
        courseId = getIntent().getLongExtra("courseId", 0L);
        amount = getIntent().getDoubleExtra("amount", 0.0);
        String courseName = getIntent().getStringExtra("courseName");

        if (courseId == 0L || amount == 0.0) {
            Toast.makeText(this, "Lỗi: Thông tin thanh toán không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display payment info
        if (courseName != null && !courseName.isEmpty()) {
            tvCourseName.setText(courseName);
        } else {
            tvCourseName.setText("Khóa học ID: " + courseId);
        }
        
        tvAmount.setText(String.format("Số tiền: %,.0f VND", amount));
        tvDescription.setText("Thanh toán khóa học qua MoMo");
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
        
        String description = "Thanh toán khóa học ID: " + courseId;
        CreatePaymentRequest request = new CreatePaymentRequest(courseId, amount, description);
        
        RetrofitClient.getApiService().createPayment(request)
                .enqueue(new Callback<MoMoPaymentResponse>() {
                    @Override
                    public void onResponse(Call<MoMoPaymentResponse> call, Response<MoMoPaymentResponse> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            MoMoPaymentResponse paymentResponse = response.body();
                            
                            if (paymentResponse.isSuccess() && paymentResponse.getPayUrl() != null) {
                                currentOrderId = paymentResponse.getOrderId();
                                openMoMoPayment(paymentResponse.getPayUrl());
                            } else {
                                showError(paymentResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi tạo thanh toán MoMo");
                        }
                    }

                    @Override
                    public void onFailure(Call<MoMoPaymentResponse> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void openMoMoPayment(String payUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
            startActivity(intent);
            
            // Start checking payment status after opening MoMo
            startPaymentStatusCheck();
        } catch (Exception e) {
            showError("Lỗi mở ứng dụng MoMo. Vui lòng cài đặt MoMo hoặc thanh toán qua web.");
        }
    }

    private void startPaymentStatusCheck() {
        if (currentOrderId == null) return;
        
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkPaymentStatus();
                statusHandler.postDelayed(this, 3000); // Check every 3 seconds
            }
        };
        
        statusHandler.postDelayed(statusCheckRunnable, 3000);
    }

    private void checkPaymentStatus() {
        if (currentOrderId == null) return;
        
        RetrofitClient.getApiService().getPaymentStatus(currentOrderId)
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(Call<PaymentStatusResponse> call, Response<PaymentStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentStatusResponse statusResponse = response.body();
                            
                            if (statusResponse.isSuccess()) {
                                String status = statusResponse.getStatus();
                                
                                if ("COMPLETED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
                                    stopPaymentStatusCheck();
                                    handlePaymentSuccess();
                                } else if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                                    stopPaymentStatusCheck();
                                    handlePaymentFailure();
                                }
                                // If status is PENDING, continue checking
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentStatusResponse> call, Throwable t) {
                        // Continue checking on failure - might be temporary network issue
                    }
                });
    }

    private void stopPaymentStatusCheck() {
        if (statusHandler != null && statusCheckRunnable != null) {
            statusHandler.removeCallbacks(statusCheckRunnable);
        }
    }

    private void handlePaymentSuccess() {
        Toast.makeText(this, "Thanh toán thành công! Bạn đã được ghi danh vào khóa học.", Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    private void handlePaymentFailure() {
        Toast.makeText(this, "Thanh toán thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnMomo.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume payment status checking when user returns from MoMo
        if (currentOrderId != null && statusCheckRunnable == null) {
            startPaymentStatusCheck();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause status checking when activity is not visible
        stopPaymentStatusCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentStatusCheck();
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