package com.lessionprm.ui.courses;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.Course;
import com.lessionprm.data.model.CourseDetailResponse;
import com.lessionprm.data.model.EnrollResponse;
import com.lessionprm.ui.payment.PaymentActivity;
import com.lessionprm.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseDetailActivity extends AppCompatActivity {

    private ImageView ivCourseImage;
    private TextView tvCourseTitle, tvCourseInstructor, tvCoursePrice;
    private TextView tvCourseDuration, tvCourseLevel, tvCourseCategory;
    private TextView tvCourseDescription;
    private MaterialButton btnEnroll;
    private ProgressBar progressBar;

    private Course currentCourse;
    private Long courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        
        setupToolbar();
        initViews();
        getCourseId();
        loadCourseDetail();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            // Add toolbar to layout if not present
            setSupportActionBar(null);
        } else {
            setSupportActionBar(toolbar);
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết khóa học");
        }
    }

    private void initViews() {
        ivCourseImage = findViewById(R.id.iv_course_image);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCourseInstructor = findViewById(R.id.tv_course_instructor);
        tvCoursePrice = findViewById(R.id.tv_course_price);
        tvCourseDuration = findViewById(R.id.tv_course_duration);
        tvCourseLevel = findViewById(R.id.tv_course_level);
        tvCourseCategory = findViewById(R.id.tv_course_category);
        tvCourseDescription = findViewById(R.id.tv_course_description);
        btnEnroll = findViewById(R.id.btn_enroll);
        progressBar = findViewById(R.id.progress_bar);

        btnEnroll.setOnClickListener(v -> handleEnrollClick());
    }

    private void getCourseId() {
        courseId = getIntent().getLongExtra("courseId", 0L);
        if (courseId == 0L) {
            Toast.makeText(this, "Lỗi: Không tìm thấy khóa học", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadCourseDetail() {
        if (!ValidationUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        RetrofitClient.getApiService().getCourseDetail(courseId)
                .enqueue(new Callback<CourseDetailResponse>() {
                    @Override
                    public void onResponse(Call<CourseDetailResponse> call, Response<CourseDetailResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            CourseDetailResponse courseResponse = response.body();

                            if (courseResponse.isSuccess() && courseResponse.getCourse() != null) {
                                currentCourse = courseResponse.getCourse();
                                displayCourseDetail();
                            } else {
                                showError(courseResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi tải dữ liệu khóa học");
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseDetailResponse> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void displayCourseDetail() {
        if (currentCourse == null) return;

        // Set course title
        tvCourseTitle.setText(currentCourse.getTitle());

        // Set instructor
        if (currentCourse.getInstructorName() != null) {
            tvCourseInstructor.setText(currentCourse.getInstructorName());
        } else {
            tvCourseInstructor.setVisibility(View.GONE);
        }

        // Set price
        tvCoursePrice.setText(currentCourse.getFormattedPrice());

        // Set duration
        if (currentCourse.getDurationHours() != null) {
            tvCourseDuration.setText(currentCourse.getDurationHours() + " giờ");
        } else {
            tvCourseDuration.setVisibility(View.GONE);
        }

        // Set level
        if (currentCourse.getLevel() != null) {
            tvCourseLevel.setText(currentCourse.getLevel());
        } else {
            tvCourseLevel.setVisibility(View.GONE);
        }

        // Set category
        if (currentCourse.getCategory() != null) {
            tvCourseCategory.setText(currentCourse.getCategory());
        } else {
            tvCourseCategory.setVisibility(View.GONE);
        }

        // Set description
        if (currentCourse.getDescription() != null && !currentCourse.getDescription().isEmpty()) {
            tvCourseDescription.setText(currentCourse.getDescription());
        } else if (currentCourse.getShortDescription() != null) {
            tvCourseDescription.setText(currentCourse.getShortDescription());
        } else {
            tvCourseDescription.setText("Không có mô tả");
        }

        // Load course image
        if (currentCourse.getImageUrl() != null && !currentCourse.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentCourse.getImageUrl())
                    .placeholder(R.drawable.ic_course_placeholder)
                    .error(R.drawable.ic_course_placeholder)
                    .into(ivCourseImage);
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentCourse.getTitle());
        }
    }

    private void handleEnrollClick() {
        if (currentCourse == null) return;

        if (currentCourse.getPrice() != null && currentCourse.getPrice() > 0) {
            // Paid course - go to payment
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("courseId", currentCourse.getId());
            intent.putExtra("amount", currentCourse.getPrice().doubleValue());
            startActivity(intent);
        } else {
            // Free course - enroll directly
            enrollInCourse();
        }
    }

    private void enrollInCourse() {
        if (!ValidationUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        btnEnroll.setEnabled(false);

        RetrofitClient.getApiService().enrollCourse(courseId)
                .enqueue(new Callback<EnrollResponse>() {
                    @Override
                    public void onResponse(Call<EnrollResponse> call, Response<EnrollResponse> response) {
                        showLoading(false);
                        btnEnroll.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            EnrollResponse enrollResponse = response.body();

                            if (enrollResponse.isSuccess()) {
                                Toast.makeText(CourseDetailActivity.this, 
                                    "Đăng ký khóa học thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                showError(enrollResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi đăng ký khóa học");
                        }
                    }

                    @Override
                    public void onFailure(Call<EnrollResponse> call, Throwable t) {
                        showLoading(false);
                        btnEnroll.setEnabled(true);
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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