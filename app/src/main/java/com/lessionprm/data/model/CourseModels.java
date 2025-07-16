package com.lessionprm.data.model;

import java.util.List;

public class CourseListResponse {
    private List<Course> content;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;
    private boolean first;
    private boolean last;
    private boolean success;
    private String message;
    
    // Getters and Setters
    public List<Course> getContent() { return content; }
    public void setContent(List<Course> content) { this.content = content; }
    
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class CourseDetailResponse {
    private Course course;
    private boolean success;
    private String message;
    
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

class EnrollResponse {
    private boolean success;
    private String message;
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}