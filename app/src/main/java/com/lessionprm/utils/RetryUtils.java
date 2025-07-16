package com.lessionprm.utils;

import android.os.Handler;
import android.os.Looper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class for handling API call retries with exponential backoff
 */
public class RetryUtils {
    
    public static class RetryableCallback<T> implements Callback<T> {
        private final Callback<T> originalCallback;
        private final Call<T> call;
        private final int maxRetries;
        private int currentAttempt = 0;
        private final Handler handler = new Handler(Looper.getMainLooper());
        
        public RetryableCallback(Call<T> call, Callback<T> originalCallback, int maxRetries) {
            this.call = call;
            this.originalCallback = originalCallback;
            this.maxRetries = maxRetries;
        }
        
        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful() || !NetworkUtils.shouldRetry(response.code()) || currentAttempt >= maxRetries) {
                // Success or non-retryable error or max retries reached
                originalCallback.onResponse(call, response);
            } else {
                // Retry the request
                retryRequest();
            }
        }
        
        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (currentAttempt >= maxRetries) {
                // Max retries reached
                originalCallback.onFailure(call, t);
            } else {
                // Retry the request
                retryRequest();
            }
        }
        
        private void retryRequest() {
            currentAttempt++;
            long delay = NetworkUtils.getRetryDelay(currentAttempt);
            
            handler.postDelayed(() -> {
                Call<T> newCall = call.clone();
                newCall.enqueue(this);
            }, delay);
        }
    }
    
    /**
     * Create a retryable callback wrapper
     */
    public static <T> Callback<T> createRetryableCallback(Call<T> call, Callback<T> originalCallback, int maxRetries) {
        return new RetryableCallback<>(call, originalCallback, maxRetries);
    }
    
    /**
     * Create a retryable callback wrapper with default 3 retries
     */
    public static <T> Callback<T> createRetryableCallback(Call<T> call, Callback<T> originalCallback) {
        return createRetryableCallback(call, originalCallback, 3);
    }
}