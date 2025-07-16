package com.lessionprm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Patterns;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 10 && phone.matches("\\d+");
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }
    
    public static boolean isNetworkAvailable(Context context) {
        return NetworkUtils.isNetworkAvailable(context);
    }
    
    public static String getEmailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        return null;
    }
    
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }
        if (!isValidPassword(password)) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null;
    }
    
    public static String getNameError(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Họ tên không được để trống";
        }
        if (!isValidName(name)) {
            return "Họ tên phải có ít nhất 2 ký tự";
        }
        return null;
    }
    
    public static String getPhoneError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Số điện thoại không được để trống";
        }
        if (!isValidPhone(phone)) {
            return "Số điện thoại không hợp lệ";
        }
        return null;
    }
    
    public static String getConfirmPasswordError(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Xác nhận mật khẩu không được để trống";
        }
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp";
        }
        return null;
    }
}