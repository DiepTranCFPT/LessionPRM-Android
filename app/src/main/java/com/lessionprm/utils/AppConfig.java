package com.lessionprm.utils;

import com.lessionprm.BuildConfig;

public class AppConfig {
    
    // Environment types
    public enum Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
    
    // Current environment - can be changed based on build variant
    private static final Environment CURRENT_ENVIRONMENT = Environment.DEVELOPMENT;
    
    // Base URLs for different environments
    private static final String DEV_BASE_URL = "http://10.0.2.2:8080/api/";
    private static final String STAGING_BASE_URL = "https://staging-api.lessionprm.com/api/";
    private static final String PROD_BASE_URL = "https://api.lessionprm.com/api/";
    
    // Network timeouts (in seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    
    // Token refresh settings
    public static final long TOKEN_REFRESH_THRESHOLD_MINUTES = 5; // Refresh token 5 minutes before expiry
    
    // Debug settings
    public static final boolean ENABLE_NETWORK_LOGGING = BuildConfig.DEBUG;
    public static final boolean ENABLE_CRASHLYTICS = !BuildConfig.DEBUG;
    
    /**
     * Get the base URL for the current environment
     */
    public static String getBaseUrl() {
        switch (CURRENT_ENVIRONMENT) {
            case STAGING:
                return STAGING_BASE_URL;
            case PRODUCTION:
                return PROD_BASE_URL;
            case DEVELOPMENT:
            default:
                return DEV_BASE_URL;
        }
    }
    
    /**
     * Get the current environment
     */
    public static Environment getCurrentEnvironment() {
        return CURRENT_ENVIRONMENT;
    }
    
    /**
     * Check if we're in debug mode
     */
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }
    
    /**
     * Check if network logging should be enabled
     */
    public static boolean isNetworkLoggingEnabled() {
        return ENABLE_NETWORK_LOGGING;
    }
    
    /**
     * Get the user agent string for API requests
     */
    public static String getUserAgent() {
        return "LessionPRM-Android/" + BuildConfig.VERSION_NAME + 
               " (" + BuildConfig.VERSION_CODE + ") " +
               "Environment:" + CURRENT_ENVIRONMENT.name();
    }
    
    /**
     * Get MoMo payment configuration based on environment
     */
    public static class MoMoConfig {
        public static final String DEV_PARTNER_CODE = "MOMO_DEV_PARTNER";
        public static final String STAGING_PARTNER_CODE = "MOMO_STAGING_PARTNER";
        public static final String PROD_PARTNER_CODE = "MOMO_PROD_PARTNER";
        
        public static String getPartnerCode() {
            switch (CURRENT_ENVIRONMENT) {
                case STAGING:
                    return STAGING_PARTNER_CODE;
                case PRODUCTION:
                    return PROD_PARTNER_CODE;
                case DEVELOPMENT:
                default:
                    return DEV_PARTNER_CODE;
            }
        }
        
        public static boolean isTestMode() {
            return CURRENT_ENVIRONMENT != Environment.PRODUCTION;
        }
    }
}