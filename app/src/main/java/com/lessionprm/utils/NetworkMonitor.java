package com.lessionprm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkMonitor extends ConnectivityManager.NetworkCallback {
    
    private static NetworkMonitor instance;
    private ConnectivityManager connectivityManager;
    private MutableLiveData<Boolean> networkAvailable = new MutableLiveData<>(false);
    private MutableLiveData<NetworkType> networkType = new MutableLiveData<>(NetworkType.NONE);
    
    public enum NetworkType {
        WIFI, CELLULAR, ETHERNET, NONE
    }
    
    private NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Initial check
        updateNetworkStatus();
        
        // Register for network changes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(this);
        } else {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerNetworkCallback(builder.build(), this);
        }
    }
    
    public static synchronized NetworkMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMonitor(context.getApplicationContext());
        }
        return instance;
    }
    
    public LiveData<Boolean> getNetworkAvailable() {
        return networkAvailable;
    }
    
    public LiveData<NetworkType> getNetworkType() {
        return networkType;
    }
    
    public boolean isNetworkAvailable() {
        return Boolean.TRUE.equals(networkAvailable.getValue());
    }
    
    public NetworkType getCurrentNetworkType() {
        NetworkType current = networkType.getValue();
        return current != null ? current : NetworkType.NONE;
    }
    
    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        updateNetworkStatus();
    }
    
    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        updateNetworkStatus();
    }
    
    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        updateNetworkStatus();
    }
    
    private void updateNetworkStatus() {
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        NetworkType type = getCurrentNetworkTypeInternal();
        
        networkAvailable.postValue(isAvailable);
        networkType.postValue(type);
    }
    
    private NetworkType getCurrentNetworkTypeInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return NetworkType.NONE;
            
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) return NetworkType.NONE;
            
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetworkType.WIFI;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkType.CELLULAR;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return NetworkType.ETHERNET;
            }
        } else {
            // Fallback for older Android versions
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                int type = activeNetworkInfo.getType();
                switch (type) {
                    case ConnectivityManager.TYPE_WIFI:
                        return NetworkType.WIFI;
                    case ConnectivityManager.TYPE_MOBILE:
                        return NetworkType.CELLULAR;
                    case ConnectivityManager.TYPE_ETHERNET:
                        return NetworkType.ETHERNET;
                }
            }
        }
        
        return NetworkType.NONE;
    }
    
    public void unregister() {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(this);
        }
    }
}