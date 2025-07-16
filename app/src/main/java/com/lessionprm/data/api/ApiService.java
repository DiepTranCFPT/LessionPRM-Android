package com.lessionprm.data.api;

import com.lessionprm.data.model.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Authentication
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    
    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshRequest request);
    
    // Courses
    @GET("courses")
    Call<CourseListResponse> getCourses(
        @Query("page") int page, 
        @Query("size") int size, 
        @Query("search") String search
    );
    
    @GET("courses/{id}")
    Call<CourseDetailResponse> getCourseDetail(@Path("id") Long id);
    
    @POST("courses/{id}/enroll")
    Call<EnrollResponse> enrollCourse(@Path("id") Long id);
    
    @GET("courses/my-courses")
    Call<CourseListResponse> getMyCourses();
    
    // Payment
    @POST("payment/momo/create")
    Call<MoMoPaymentResponse> createPayment(@Body CreatePaymentRequest request);
    
    @GET("payment/momo/status/{orderId}")
    Call<PaymentStatusResponse> getPaymentStatus(@Path("orderId") String orderId);
    
    // User Profile
    @GET("users/profile")
    Call<UserProfileResponse> getProfile();
    
    @PUT("users/profile")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request);
    
    // Admin APIs
    @GET("statistics/overview")
    Call<StatisticsResponse> getStatistics();
    
    @GET("revenue/monthly")
    Call<MonthlyRevenueResponse> getMonthlyRevenue(@Query("year") int year);
    
    @GET("admin/courses")
    Call<CourseListResponse> getAdminCourses(@Query("page") int page, @Query("size") int size);
    
    @POST("admin/courses")
    Call<CourseDetailResponse> createCourse(@Body CreateCourseRequest request);
    
    @PUT("admin/courses/{id}")
    Call<CourseDetailResponse> updateCourse(@Path("id") Long id, @Body UpdateCourseRequest request);
    
    @DELETE("admin/courses/{id}")
    Call<Void> deleteCourse(@Path("id") Long id);
}