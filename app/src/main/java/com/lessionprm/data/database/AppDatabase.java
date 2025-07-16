package com.lessionprm.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.lessionprm.data.model.Course;
import com.lessionprm.data.model.User;

@Database(
        entities = {User.class, Course.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "lessionprm_database";
    private static volatile AppDatabase INSTANCE;
    
    public abstract UserDao userDao();
    public abstract CourseDao courseDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
    
    public static void destroyInstance() {
        INSTANCE = null;
    }
}