package com.lessionprm;

import android.app.Application;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.utils.NetworkMonitor;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class LessionPRMApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Retrofit client
        RetrofitClient.init(this);
        
        // Initialize network monitor
        NetworkMonitor.getInstance(this);
    }
}