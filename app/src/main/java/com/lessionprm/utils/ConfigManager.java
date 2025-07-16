package com.lessionprm.utils;

import android.content.Context;
import com.lessionprm.BuildConfig;

public class ConfigManager {
    
    public enum Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
    
    private static Environment currentEnvironment = Environment.DEVELOPMENT;
    
    // Base URLs for different environments
    private static final String DEV_BASE_URL = "http://10.0.2.2:8080/api/";
    private static final String STAGING_BASE_URL = "https://staging-api.lessionprm.com/api/";
    private static final String PROD_BASE_URL = "https://api.lessionprm.com/api/";
    
    public static void init(Context context) {
        // Determine environment based on build type or configuration
        if (BuildConfig.DEBUG) {
            currentEnvironment = Environment.DEVELOPMENT;
        } else {
            // Could be configured via build variants or remote config
            currentEnvironment = Environment.PRODUCTION;
        }
    }
    
    public static String getBaseUrl() {
        switch (currentEnvironment) {
            case STAGING:
                return STAGING_BASE_URL;
            case PRODUCTION:
                return PROD_BASE_URL;
            case DEVELOPMENT:
            default:
                return DEV_BASE_URL;
        }
    }
    
    public static Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    public static void setEnvironment(Environment environment) {
        currentEnvironment = environment;
    }
    
    public static boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }
    
    public static boolean isProduction() {
        return currentEnvironment == Environment.PRODUCTION;
    }
    
    public static boolean isDebuggingEnabled() {
        return isDebugMode() && currentEnvironment != Environment.PRODUCTION;
    }
    
    // Network configuration
    public static int getNetworkTimeout() {
        return isProduction() ? 60 : 30; // seconds
    }
    
    public static int getRetryCount() {
        return isProduction() ? 3 : 2;
    }
    
    // Security configuration
    public static boolean isSSLValidationEnabled() {
        return isProduction();
    }
    
    // Feature flags
    public static boolean isOfflineModeEnabled() {
        return true; // Always enabled for better user experience
    }
    
    public static boolean isAnalyticsEnabled() {
        return isProduction();
    }
    
    public static boolean isCrashReportingEnabled() {
        return !isDebugMode();
    }
}