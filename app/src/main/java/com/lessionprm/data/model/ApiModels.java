package com.lessionprm.data.model;

// Missing classes for API completeness

public class UserProfileResponse {
    private User user;
    private boolean success;
    private String message;
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    
    public UpdateProfileRequest(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

public class UpdateProfileResponse {
    private User user;
    private boolean success;
    private String message;
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

// Payment models
public class CreatePaymentRequest {
    private Long courseId;
    private Double amount;
    private String description;
    
    public CreatePaymentRequest(Long courseId, Double amount, String description) {
        this.courseId = courseId;
        this.amount = amount;
        this.description = description;
    }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

public class MoMoPaymentResponse {
    private String payUrl;
    private String orderId;
    private boolean success;
    private String message;
    
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

public class PaymentStatusResponse {
    private String status;
    private String orderId;
    private Double amount;
    private boolean success;
    private String message;
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

// Admin models
public class StatisticsResponse {
    private int totalUsers;
    private int totalCourses;
    private int totalEnrollments;
    private double totalRevenue;
    private boolean success;
    private String message;
    
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    
    public int getTotalCourses() { return totalCourses; }
    public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
    
    public int getTotalEnrollments() { return totalEnrollments; }
    public void setTotalEnrollments(int totalEnrollments) { this.totalEnrollments = totalEnrollments; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

public class MonthlyRevenueResponse {
    private java.util.List<MonthlyRevenue> data;
    private boolean success;
    private String message;
    
    public java.util.List<MonthlyRevenue> getData() { return data; }
    public void setData(java.util.List<MonthlyRevenue> data) { this.data = data; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

public class MonthlyRevenue {
    private int month;
    private int year;
    private double revenue;
    
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
}

public class CreateCourseRequest {
    private String title;
    private String description;
    private String shortDescription;
    private Double price;
    private Integer durationHours;
    private String category;
    private String level;
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getDurationHours() { return durationHours; }
    public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}

public class UpdateCourseRequest extends CreateCourseRequest {
    // Same fields as CreateCourseRequest
}