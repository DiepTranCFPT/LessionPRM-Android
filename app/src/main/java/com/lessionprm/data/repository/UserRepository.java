package com.lessionprm.data.repository;

import android.content.Context;

import com.lessionprm.data.api.ApiService;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.database.AppDatabase;
import com.lessionprm.data.database.UserDao;
import com.lessionprm.data.model.UpdateProfileRequest;
import com.lessionprm.data.model.UpdateProfileResponse;
import com.lessionprm.data.model.User;
import com.lessionprm.data.model.UserProfileResponse;
import com.lessionprm.utils.NetworkUtils;
import com.lessionprm.utils.PrefsHelper;
import com.lessionprm.utils.ValidationUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    
    private final ApiService apiService;
    private final UserDao userDao;
    private final Context context;
    private final Executor executor;
    
    public UserRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService();
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    public interface UserProfileCallback {
        void onSuccess(User user, boolean fromCache);
        void onError(String errorMessage);
        void onAuthError();
    }
    
    public interface UpdateProfileCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
        void onAuthError();
    }
    
    /**
     * Get user profile with caching support
     */
    public void getUserProfile(UserProfileCallback callback) {
        // First, try to return cached user if available
        Long userId = PrefsHelper.getUserId(context);
        if (userId > 0) {
            loadUserFromCache(userId, callback);
        }
        
        // If no network, only return cached data
        if (!ValidationUtils.isNetworkAvailable(context)) {
            if (userId == 0) {
                callback.onError("Không có kết nối mạng");
            }
            return;
        }
        
        // Try to get fresh data from API
        apiService.getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (NetworkUtils.isResponseSuccessful(response)) {
                    UserProfileResponse profileResponse = response.body();
                    
                    if (profileResponse.isSuccess() && profileResponse.getUser() != null) {
                        User user = profileResponse.getUser();
                        
                        // Cache the user in background
                        executor.execute(() -> {
                            try {
                                userDao.insertUser(user);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        
                        // Update cached user info
                        PrefsHelper.saveUserInfo(context,
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole());
                        
                        callback.onSuccess(user, false);
                    } else {
                        callback.onError(profileResponse.getMessage());
                    }
                } else if (NetworkUtils.isAuthError(response.code())) {
                    callback.onAuthError();
                } else {
                    callback.onError(NetworkUtils.getErrorMessage(response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                callback.onError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }
    
    /**
     * Update user profile (requires network)
     */
    public void updateUserProfile(String fullName, String phone, UpdateProfileCallback callback) {
        if (!ValidationUtils.isNetworkAvailable(context)) {
            callback.onError("Cần kết nối mạng để cập nhật thông tin");
            return;
        }
        
        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);
        
        apiService.updateProfile(request).enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (NetworkUtils.isResponseSuccessful(response)) {
                    UpdateProfileResponse updateResponse = response.body();
                    
                    if (updateResponse.isSuccess() && updateResponse.getUser() != null) {
                        User user = updateResponse.getUser();
                        
                        // Cache the updated user in background
                        executor.execute(() -> {
                            try {
                                userDao.insertUser(user);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        
                        // Update cached user info
                        PrefsHelper.saveUserInfo(context,
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole());
                        
                        callback.onSuccess(user);
                    } else {
                        callback.onError(updateResponse.getMessage());
                    }
                } else if (NetworkUtils.isAuthError(response.code())) {
                    callback.onAuthError();
                } else {
                    callback.onError(NetworkUtils.getErrorMessage(response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                callback.onError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }
    
    /**
     * Get cached user profile
     */
    public void getCachedUserProfile(UserProfileCallback callback) {
        Long userId = PrefsHelper.getUserId(context);
        if (userId > 0) {
            loadUserFromCache(userId, callback);
        } else {
            callback.onError("Không có dữ liệu người dùng");
        }
    }
    
    /**
     * Clear cached user data
     */
    public void clearUserCache() {
        executor.execute(() -> {
            try {
                userDao.deleteAllUsers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        PrefsHelper.clearAuthData(context);
    }
    
    private void loadUserFromCache(Long userId, UserProfileCallback callback) {
        executor.execute(() -> {
            try {
                User cachedUser = userDao.getUserById(userId);
                
                // Post result back to main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        if (cachedUser != null) {
                            callback.onSuccess(cachedUser, true);
                        }
                        // Don't call error here, let the API call handle it
                    });
                }
            } catch (Exception e) {
                // Silently fail for cache reads
                e.printStackTrace();
            }
        });
    }
}