package com.lessionprm.data.repository;

import android.content.Context;

import com.lessionprm.data.api.ApiService;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.database.AppDatabase;
import com.lessionprm.data.database.CourseDao;
import com.lessionprm.data.model.Course;
import com.lessionprm.data.model.CourseDetailResponse;
import com.lessionprm.data.model.CourseListResponse;
import com.lessionprm.data.model.EnrollResponse;
import com.lessionprm.utils.NetworkUtils;
import com.lessionprm.utils.ValidationUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {
    
    private final ApiService apiService;
    private final CourseDao courseDao;
    private final Context context;
    private final Executor executor;
    
    public CourseRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService();
        this.courseDao = AppDatabase.getInstance(context).courseDao();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public interface CourseListCallback {
        void onSuccess(List<Course> courses, boolean fromCache);
        void onError(String errorMessage);
    }
    
    public interface CourseDetailCallback {
        void onSuccess(Course course, boolean fromCache);
        void onError(String errorMessage);
    }
    
    public interface EnrollCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    /**
     * Get courses with caching support
     */
    public void getCourses(int page, int size, String search, CourseListCallback callback) {
        // If no network, return cached data
        if (!ValidationUtils.isNetworkAvailable(context)) {
            loadCoursesFromCache(search, callback);
            return;
        }
        
        // Try to get fresh data from API
        apiService.getCourses(page, size, search).enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(Call<CourseListResponse> call, Response<CourseListResponse> response) {
                if (NetworkUtils.isResponseSuccessful(response)) {
                    CourseListResponse courseResponse = response.body();
                    
                    if (courseResponse.isSuccess() && courseResponse.getContent() != null) {
                        List<Course> courses = courseResponse.getContent();
                        
                        // Cache the courses in background
                        if (page == 0) { // Only cache first page for simplicity
                            executor.execute(() -> {
                                try {
                                    courseDao.deleteAllCourses();
                                    courseDao.insertCourses(courses);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        
                        callback.onSuccess(courses, false);
                    } else {
                        // API error, try to load from cache
                        loadCoursesFromCache(search, callback);
                    }
                } else {
                    // HTTP error, try to load from cache
                    loadCoursesFromCache(search, callback);
                }
            }
            
            @Override
            public void onFailure(Call<CourseListResponse> call, Throwable t) {
                // Network error, try to load from cache
                loadCoursesFromCache(search, callback);
            }
        });
    }
    
    /**
     * Get course detail with caching support
     */
    public void getCourseDetail(Long courseId, CourseDetailCallback callback) {
        // If no network, return cached data
        if (!ValidationUtils.isNetworkAvailable(context)) {
            loadCourseFromCache(courseId, callback);
            return;
        }
        
        // Try to get fresh data from API
        apiService.getCourseDetail(courseId).enqueue(new Callback<CourseDetailResponse>() {
            @Override
            public void onResponse(Call<CourseDetailResponse> call, Response<CourseDetailResponse> response) {
                if (NetworkUtils.isResponseSuccessful(response)) {
                    CourseDetailResponse courseResponse = response.body();
                    
                    if (courseResponse.isSuccess() && courseResponse.getCourse() != null) {
                        Course course = courseResponse.getCourse();
                        
                        // Cache the course in background
                        executor.execute(() -> {
                            try {
                                courseDao.insertCourse(course);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        
                        callback.onSuccess(course, false);
                    } else {
                        // API error, try to load from cache
                        loadCourseFromCache(courseId, callback);
                    }
                } else {
                    // HTTP error, try to load from cache
                    loadCourseFromCache(courseId, callback);
                }
            }
            
            @Override
            public void onFailure(Call<CourseDetailResponse> call, Throwable t) {
                // Network error, try to load from cache
                loadCourseFromCache(courseId, callback);
            }
        });
    }
    
    /**
     * Enroll in course (requires network)
     */
    public void enrollInCourse(Long courseId, EnrollCallback callback) {
        if (!ValidationUtils.isNetworkAvailable(context)) {
            callback.onError("Cần kết nối mạng để đăng ký khóa học");
            return;
        }
        
        apiService.enrollCourse(courseId).enqueue(new Callback<EnrollResponse>() {
            @Override
            public void onResponse(Call<EnrollResponse> call, Response<EnrollResponse> response) {
                if (NetworkUtils.isResponseSuccessful(response)) {
                    EnrollResponse enrollResponse = response.body();
                    
                    if (enrollResponse.isSuccess()) {
                        callback.onSuccess();
                    } else {
                        callback.onError(enrollResponse.getMessage());
                    }
                } else {
                    callback.onError(NetworkUtils.getErrorMessage(response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<EnrollResponse> call, Throwable t) {
                callback.onError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }
    
    private void loadCoursesFromCache(String search, CourseListCallback callback) {
        executor.execute(() -> {
            try {
                List<Course> cachedCourses;
                if (search != null && !search.trim().isEmpty()) {
                    cachedCourses = courseDao.searchCourses(search.trim());
                } else {
                    cachedCourses = courseDao.getAllCourses();
                }
                
                // Post result back to main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (cachedCourses.isEmpty()) {
                            callback.onError("Không có dữ liệu");
                        } else {
                            callback.onSuccess(cachedCourses, true);
                        }
                    });
                }
            } catch (Exception e) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onError("Lỗi đọc dữ liệu cục bộ"));
                }
            }
        });
    }
    
    private void loadCourseFromCache(Long courseId, CourseDetailCallback callback) {
        executor.execute(() -> {
            try {
                Course cachedCourse = courseDao.getCourseById(courseId);
                
                // Post result back to main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (cachedCourse == null) {
                            callback.onError("Không tìm thấy dữ liệu");
                        } else {
                            callback.onSuccess(cachedCourse, true);
                        }
                    });
                }
            } catch (Exception e) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onError("Lỗi đọc dữ liệu cục bộ"));
                }
            }
        });
    }
}