package com.lessionprm.data.api;

import android.content.Context;
import com.lessionprm.data.model.LoginResponse;
import com.lessionprm.data.model.RefreshRequest;
import com.lessionprm.utils.AppConfig;
import com.lessionprm.utils.PrefsHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Context context;
    
    public static void init(Context ctx) {
        context = ctx;
    }
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            
            // Add User-Agent header
            httpClient.addInterceptor(chain -> {
                Request originalRequest = chain.request();
                Request newRequest = originalRequest.newBuilder()
                        .header("User-Agent", AppConfig.getUserAgent())
                        .build();
                return chain.proceed(newRequest);
            });
            
            // Add auth interceptor
            httpClient.addInterceptor(new AuthInterceptor());
            
            // Add logging interceptor for debug builds
            if (AppConfig.isNetworkLoggingEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(loggingInterceptor);
            }
            
            // Set timeouts from config
            httpClient.connectTimeout(AppConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS);
            httpClient.readTimeout(AppConfig.READ_TIMEOUT, TimeUnit.SECONDS);
            httpClient.writeTimeout(AppConfig.WRITE_TIMEOUT, TimeUnit.SECONDS);
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    private static class AuthInterceptor implements okhttp3.Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            
            if (context != null) {
                String token = PrefsHelper.getAuthToken(context);
                if (token != null && !token.isEmpty()) {
                    Request authenticatedRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    
                    okhttp3.Response response = chain.proceed(authenticatedRequest);
                    
                    // If we get 401 (Unauthorized), try to refresh the token
                    if (response.code() == 401 && !originalRequest.url().toString().contains("/auth/")) {
                        response.close();
                        
                        if (refreshToken()) {
                            // Retry the request with the new token
                            String newToken = PrefsHelper.getAuthToken(context);
                            if (newToken != null && !newToken.isEmpty()) {
                                Request newRequest = originalRequest.newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        }
                        
                        // If refresh failed, clear auth data and return 401 response
                        PrefsHelper.clearAuthData(context);
                        return response;
                    }
                    
                    return response;
                }
            }
            
            return chain.proceed(originalRequest);
        }
        
        private boolean refreshToken() {
            try {
                String refreshTokenValue = PrefsHelper.getRefreshToken(context);
                if (refreshTokenValue == null || refreshTokenValue.isEmpty()) {
                    return false;
                }
                
                // Create a separate retrofit instance for token refresh to avoid infinite loop
                OkHttpClient simpleClient = new OkHttpClient.Builder()
                        .connectTimeout(AppConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(AppConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(AppConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                        .build();
                
                Retrofit refreshRetrofit = new Retrofit.Builder()
                        .baseUrl(AppConfig.getBaseUrl())
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(simpleClient)
                        .build();
                
                ApiService refreshService = refreshRetrofit.create(ApiService.class);
                retrofit2.Response<LoginResponse> response = refreshService
                        .refreshToken(new RefreshRequest(refreshTokenValue))
                        .execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        PrefsHelper.saveAuthToken(context, loginResponse.getToken());
                        if (loginResponse.getRefreshToken() != null) {
                            PrefsHelper.saveRefreshToken(context, loginResponse.getRefreshToken());
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}