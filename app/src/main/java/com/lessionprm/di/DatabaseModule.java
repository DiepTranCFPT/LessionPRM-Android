package com.lessionprm.di;

import android.content.Context;

import com.lessionprm.data.api.ApiService;
import com.lessionprm.data.api.RetrofitClient;
import com.lessionprm.data.database.AppDatabase;
import com.lessionprm.data.database.CourseDao;
import com.lessionprm.data.database.UserDao;
import com.lessionprm.data.repository.CourseRepository;
import com.lessionprm.data.repository.UserRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    
    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }
    
    @Provides
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }
    
    @Provides
    public CourseDao provideCourseDao(AppDatabase database) {
        return database.courseDao();
    }
    
    @Provides
    @Singleton
    public ApiService provideApiService() {
        return RetrofitClient.getApiService();
    }
    
    @Provides
    @Singleton
    public UserRepository provideUserRepository(@ApplicationContext Context context) {
        return new UserRepository(context);
    }
    
    @Provides
    @Singleton
    public CourseRepository provideCourseRepository(@ApplicationContext Context context) {
        return new CourseRepository(context);
    }
}