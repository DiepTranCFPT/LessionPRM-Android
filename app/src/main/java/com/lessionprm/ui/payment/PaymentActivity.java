package com.lessionprm.ui.payment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.lessionprm.R;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvCourseName, tvAmount;
    private MaterialButton btnMomo;
    private ProgressBar progressBar;

    private Long courseId;
    private Float amount;

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
        amount = getIntent().getFloatExtra("amount", 0f);

        if (courseId == 0L || amount == 0f) {
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
        showLoading(true);
        
        // TODO: Implement MoMo payment integration
        // For now, simulate a successful payment after 2 seconds
        btnMomo.postDelayed(() -> {
            showLoading(false);
            Toast.makeText(this, "Tích hợp MoMo sẽ được hoàn thiện sớm", Toast.LENGTH_LONG).show();
            
            // Simulate successful payment
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }, 2000);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnMomo.setEnabled(!show);
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