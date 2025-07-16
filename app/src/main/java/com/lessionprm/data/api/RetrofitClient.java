package com.lessionprm.data.api;

import android.content.Context;
import com.lessionprm.data.model.LoginResponse;
import com.lessionprm.data.model.RefreshRequest;
import com.lessionprm.utils.ConfigManager;
import com.lessionprm.utils.PrefsHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Context context;
    
    public static void init(Context ctx) {
        context = ctx;
        ConfigManager.init(ctx);
    }
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            
            // Add auth interceptor with token refresh
            httpClient.addInterceptor(new AuthInterceptor());
            
            // Add logging interceptor (only for debug builds)
            if (ConfigManager.isDebuggingEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(loggingInterceptor);
            }
            
            // Set timeouts based on environment
            int timeout = ConfigManager.getNetworkTimeout();
            httpClient.connectTimeout(timeout, TimeUnit.SECONDS);
            httpClient.readTimeout(timeout, TimeUnit.SECONDS);
            httpClient.writeTimeout(timeout, TimeUnit.SECONDS);
            
            // Add retry mechanism
            httpClient.addInterceptor(new RetryInterceptor());
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(ConfigManager.getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    // Reset client when environment changes
    public static void resetClient() {
        retrofit = null;
    }
    
    private static class AuthInterceptor implements okhttp3.Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            
            if (context != null) {
                String token = PrefsHelper.getAuthToken(context);
                if (token != null && !token.isEmpty()) {
                    Request authenticatedRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    
                    Response response = chain.proceed(authenticatedRequest);
                    
                    // Check if token expired (401 Unauthorized)
                    if (response.code() == 401) {
                        response.close();
                        
                        // Try to refresh token
                        String refreshToken = PrefsHelper.getRefreshToken(context);
                        if (refreshToken != null && !refreshToken.isEmpty()) {
                            String newToken = refreshAccessToken(refreshToken);
                            if (newToken != null) {
                                // Retry with new token
                                Request newRequest = originalRequest.newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        }
                        
                        // If refresh failed, clear auth data
                        PrefsHelper.clearAuthData(context);
                    }
                    
                    return response;
                }
            }
            
            return chain.proceed(originalRequest);
        }
        
        private String refreshAccessToken(String refreshToken) {
            try {
                // Create a separate Retrofit instance for refresh to avoid interceptor loop
                Retrofit refreshRetrofit = new Retrofit.Builder()
                        .baseUrl(ConfigManager.getBaseUrl())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                
                ApiService refreshService = refreshRetrofit.create(ApiService.class);
                RefreshRequest request = new RefreshRequest(refreshToken);
                
                Call<LoginResponse> call = refreshService.refreshToken(request);
                retrofit2.Response<LoginResponse> response = call.execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess() && loginResponse.getToken() != null) {
                        // Save new tokens
                        PrefsHelper.saveAuthToken(context, loginResponse.getToken());
                        if (loginResponse.getRefreshToken() != null) {
                            PrefsHelper.saveRefreshToken(context, loginResponse.getRefreshToken());
                        }
                        return loginResponse.getToken();
                    }
                }
            } catch (Exception e) {
                if (ConfigManager.isDebuggingEnabled()) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    
    private static class RetryInterceptor implements okhttp3.Interceptor {
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;
            
            int maxRetries = ConfigManager.getRetryCount();
            
            for (int i = 0; i < maxRetries; i++) {
                try {
                    response = chain.proceed(request);
                    
                    // If response is successful or client error (4xx), don't retry
                    if (response.isSuccessful() || (response.code() >= 400 && response.code() < 500)) {
                        return response;
                    }
                    
                    // Close response before retry
                    if (response != null) {
                        response.close();
                    }
                    
                    // Wait before retry (exponential backoff)
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep((long) Math.pow(2, i) * 1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    
                } catch (IOException e) {
                    exception = e;
                    
                    // Wait before retry
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep((long) Math.pow(2, i) * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            // If we get here, all retries failed
            if (exception != null) {
                throw exception;
            }
            
            return response;
        }
    }
}