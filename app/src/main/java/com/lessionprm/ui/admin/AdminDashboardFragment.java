package com.lessionprm.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.MonthlyRevenue;
import com.lessionprm.data.model.MonthlyRevenueResponse;
import com.lessionprm.data.model.StatisticsResponse;
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalCourses, tvTotalEnrollments, tvTotalRevenue;
    private LineChart chartRevenue;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutStats, layoutError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if user is admin
        if (!PrefsHelper.isAdmin(requireContext())) {
            showError("Bạn không có quyền truy cập trang này");
            return;
        }

        initViews(view);
        setupSwipeRefresh();
        loadDashboardData();
    }

    private void initViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvTotalCourses = view.findViewById(R.id.tv_total_courses);
        tvTotalEnrollments = view.findViewById(R.id.tv_total_enrollments);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        
        chartRevenue = view.findViewById(R.id.chart_revenue);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutStats = view.findViewById(R.id.layout_stats);
        layoutError = view.findViewById(R.id.layout_error);
        
        setupChart();
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void setupChart() {
        chartRevenue.setTouchEnabled(true);
        chartRevenue.setDragEnabled(true);
        chartRevenue.setScaleEnabled(true);
        chartRevenue.setPinchZoom(true);
        
        Description description = new Description();
        description.setText("Doanh thu theo tháng");
        chartRevenue.setDescription(description);
    }

    private void loadDashboardData() {
        if (!ValidationUtils.isNetworkAvailable(requireContext())) {
            showError("Không có kết nối mạng");
            return;
        }

        showLoading(true);
        layoutError.setVisibility(View.GONE);

        // Load statistics
        loadStatistics();
        
        // Load revenue chart data
        loadRevenueData();
    }

    private void loadStatistics() {
        RetrofitClient.getApiService().getStatistics()
                .enqueue(new Callback<StatisticsResponse>() {
                    @Override
                    public void onResponse(Call<StatisticsResponse> call, Response<StatisticsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StatisticsResponse stats = response.body();
                            
                            if (stats.isSuccess()) {
                                updateStatistics(stats);
                                layoutStats.setVisibility(View.VISIBLE);
                            } else {
                                showError(stats.getMessage());
                            }
                        } else {
                            showError("Lỗi tải thống kê");
                        }
                    }

                    @Override
                    public void onFailure(Call<StatisticsResponse> call, Throwable t) {
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void loadRevenueData() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        RetrofitClient.getApiService().getMonthlyRevenue(currentYear)
                .enqueue(new Callback<MonthlyRevenueResponse>() {
                    @Override
                    public void onResponse(Call<MonthlyRevenueResponse> call, Response<MonthlyRevenueResponse> response) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            MonthlyRevenueResponse revenueResponse = response.body();
                            
                            if (revenueResponse.isSuccess() && revenueResponse.getData() != null) {
                                updateRevenueChart(revenueResponse.getData());
                            } else {
                                showError(revenueResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi tải dữ liệu doanh thu");
                        }
                    }

                    @Override
                    public void onFailure(Call<MonthlyRevenueResponse> call, Throwable t) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        showError("Lỗi kết nối mạng");
                    }
                });
    }

    private void updateStatistics(StatisticsResponse stats) {
        tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
        tvTotalCourses.setText(String.valueOf(stats.getTotalCourses()));
        tvTotalEnrollments.setText(String.valueOf(stats.getTotalEnrollments()));
        tvTotalRevenue.setText(String.format("%,.0f VND", stats.getTotalRevenue()));
    }

    private void updateRevenueChart(List<MonthlyRevenue> revenueData) {
        List<Entry> entries = new ArrayList<>();
        
        for (MonthlyRevenue revenue : revenueData) {
            entries.add(new Entry(revenue.getMonth(), (float) revenue.getRevenue()));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setValueTextColor(getResources().getColor(R.color.text_primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getResources().getColor(R.color.primary));
        
        LineData lineData = new LineData(dataSet);
        chartRevenue.setData(lineData);
        chartRevenue.invalidate(); // refresh chart
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        
        if (layoutError != null) {
            layoutError.setVisibility(View.VISIBLE);
        }
        
        if (layoutStats != null) {
            layoutStats.setVisibility(View.GONE);
        }
    }
}