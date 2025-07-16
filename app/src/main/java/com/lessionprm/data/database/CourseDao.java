package com.lessionprm.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lessionprm.data.model.Course;

import java.util.List;

@Dao
public interface CourseDao {
    
    @Query("SELECT * FROM courses ORDER BY id DESC")
    List<Course> getAllCourses();
    
    @Query("SELECT * FROM courses WHERE id = :courseId")
    Course getCourseById(Long courseId);
    
    @Query("SELECT * FROM courses WHERE title LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    List<Course> searchCourses(String searchQuery);
    
    @Query("SELECT * FROM courses WHERE category = :category ORDER BY id DESC")
    List<Course> getCoursesByCategory(String category);
    
    @Query("SELECT * FROM courses WHERE isActive = 1 ORDER BY id DESC")
    List<Course> getActiveCourses();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(Course course);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourses(List<Course> courses);
    
    @Update
    void updateCourse(Course course);
    
    @Delete
    void deleteCourse(Course course);
    
    @Query("DELETE FROM courses")
    void deleteAllCourses();
    
    @Query("SELECT COUNT(*) FROM courses")
    int getCoursesCount();
}