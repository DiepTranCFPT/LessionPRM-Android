package com.lessionprm.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "courses")
public class Course {
    @PrimaryKey
    private Long id;
    
    private String title;
    
    private String description;
    
    @SerializedName("short_description")
    private String shortDescription;
    
    private Double price;
    
    @SerializedName("duration_hours")
    private Integer durationHours;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("instructor_name")
    private String instructorName;
    
    private String category;
    
    private String level;
    
    @SerializedName("student_count")
    private Integer studentCount;
    
    private Double rating;
    
    @SerializedName("is_active")
    private Boolean isActive;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    // Constructors
    public Course() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public String getFormattedPrice() {
        if (price == null) return "Miễn phí";
        return String.format("%,.0f VND", price);
    }
}