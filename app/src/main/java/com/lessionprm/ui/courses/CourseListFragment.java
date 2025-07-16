package com.lessionprm.ui.courses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.lessionprm.R;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.model.Course;
import com.lessionprm.data.model.CourseListResponse;
import com.lessionprm.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseListFragment extends Fragment implements CourseAdapter.OnCourseClickListener {

    private RecyclerView rvCourses;
    private CourseAdapter courseAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextInputEditText etSearch;

    private List<Course> courses = new ArrayList<>();
    private String currentSearch = "";
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupSwipeRefresh();
        
        loadCourses();
    }

    private void initViews(View view) {
        rvCourses = view.findViewById(R.id.rv_courses);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        etSearch = view.findViewById(R.id.et_search);
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseAdapter(courses, this);
        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCourses.setAdapter(courseAdapter);
        
        // Setup pagination
        rvCourses.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreData) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    
                    if (pastVisibleItems + visibleItemCount >= totalItemCount - 5) {
                        loadMoreCourses();
                    }
                }
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.equals(currentSearch)) {
                        currentSearch = query;
                        refreshCourses();
                    }
                };
                
                searchHandler.postDelayed(searchRunnable, 500); // Debounce search
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::refreshCourses);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadCourses() {
        if (isLoading) return;
        
        if (currentPage == 0) {
            showLoading(true);
        }
        
        if (!ValidationUtils.isNetworkAvailable(requireContext())) {
            showError("Không có kết nối mạng");
            return;
        }
        
        isLoading = true;
        
        RetrofitClient.getApiService().getCourses(currentPage, 10, currentSearch.isEmpty() ? null : currentSearch)
                .enqueue(new Callback<CourseListResponse>() {
                    @Override
                    public void onResponse(Call<CourseListResponse> call, Response<CourseListResponse> response) {
                        isLoading = false;
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            CourseListResponse courseResponse = response.body();
                            
                            if (courseResponse.isSuccess() && courseResponse.getContent() != null) {
                                handleCoursesLoaded(courseResponse);
                            } else {
                                showError(courseResponse.getMessage());
                            }
                        } else {
                            showError("Lỗi tải dữ liệu");
                        }
                        
                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(Call<CourseListResponse> call, Throwable t) {
                        isLoading = false;
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        showError("Lỗi kết nối mạng");
                        updateEmptyState();
                    }
                });
    }

    private void loadMoreCourses() {
        if (hasMoreData) {
            currentPage++;
            loadCourses();
        }
    }

    private void refreshCourses() {
        currentPage = 0;
        hasMoreData = true;
        courses.clear();
        courseAdapter.notifyDataSetChanged();
        loadCourses();
    }

    private void handleCoursesLoaded(CourseListResponse response) {
        List<Course> newCourses = response.getContent();
        
        if (currentPage == 0) {
            courses.clear();
        }
        
        courses.addAll(newCourses);
        courseAdapter.notifyDataSetChanged();
        
        hasMoreData = !response.isLast();
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
    }

    private void updateEmptyState() {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra("courseId", course.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}