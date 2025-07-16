package com.lessionprm.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lessionprm.data.model.User;

@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(Long userId);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);
    
    @Update
    void updateUser(User user);
    
    @Delete
    void deleteUser(User user);
    
    @Query("DELETE FROM users")
    void deleteAllUsers();
}