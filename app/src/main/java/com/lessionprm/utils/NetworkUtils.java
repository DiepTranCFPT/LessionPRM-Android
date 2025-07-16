package com.lessionprm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import retrofit2.Response;

public class NetworkUtils {
    
    /**
     * Check if network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Get user-friendly error message from HTTP status code
     */
    public static String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Yêu cầu không hợp lệ";
            case 401:
                return "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại";
            case 403:
                return "Bạn không có quyền thực hiện thao tác này";
            case 404:
                return "Không tìm thấy dữ liệu";
            case 409:
                return "Dữ liệu đã tồn tại";
            case 422:
                return "Dữ liệu không hợp lệ";
            case 429:
                return "Quá nhiều yêu cầu. Vui lòng thử lại sau";
            case 500:
                return "Lỗi máy chủ. Vui lòng thử lại sau";
            case 502:
            case 503:
            case 504:
                return "Máy chủ đang bảo trì. Vui lòng thử lại sau";
            default:
                return "Có lỗi xảy ra. Vui lòng thử lại";
        }
    }
    
    /**
     * Get error message from throwable (network errors)
     */
    public static String getNetworkErrorMessage(Throwable throwable) {
        if (throwable instanceof java.net.UnknownHostException) {
            return "Không thể kết nối tới máy chủ. Kiểm tra kết nối mạng";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            return "Kết nối bị timeout. Vui lòng thử lại";
        } else if (throwable instanceof java.net.ConnectException) {
            return "Không thể kết nối tới máy chủ";
        } else if (throwable instanceof javax.net.ssl.SSLException) {
            return "Lỗi bảo mật kết nối";
        } else {
            return "Kết nối mạng bị lỗi. Vui lòng thử lại";
        }
    }
    
    /**
     * Check if response is successful and has data
     */
    public static <T> boolean isResponseSuccessful(Response<T> response) {
        return response.isSuccessful() && response.body() != null;
    }
    
    /**
     * Check if we should retry a failed request based on status code
     */
    public static boolean shouldRetry(int statusCode) {
        // Retry on server errors (5xx) and some client errors
        return statusCode >= 500 || statusCode == 429 || statusCode == 408;
    }
    
    /**
     * Check if error is authentication related
     */
    public static boolean isAuthError(int statusCode) {
        return statusCode == 401 || statusCode == 403;
    }
    
    /**
     * Get retry delay in milliseconds based on attempt number
     */
    public static long getRetryDelay(int attemptNumber) {
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s (max)
        return Math.min(1000L * (1L << attemptNumber), 16000L);
    }
    
    /**
     * Check if we're running on emulator (for localhost connections)
     */
    public static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
               android.os.Build.FINGERPRINT.startsWith("unknown") ||
               android.os.Build.MODEL.contains("google_sdk") ||
               android.os.Build.MODEL.contains("Emulator") ||
               android.os.Build.MODEL.contains("Android SDK built for x86") ||
               android.os.Build.MANUFACTURER.contains("Genymotion") ||
               (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
               "google_sdk".equals(android.os.Build.PRODUCT);
    }
}