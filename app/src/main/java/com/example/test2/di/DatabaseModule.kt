package com.example.test2.di

import android.content.Context
import androidx.room.Room
import com.example.test2.data.local.AppDatabase
import com.example.test2.data.local.dao.BadgeDao
import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.HabitDao
import com.example.test2.data.local.dao.HabitLogDao
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.local.dao.UserBadgeDao
import com.example.test2.data.local.dao.TaskDao
import com.example.test2.data.local.dao.CheckInTaskDao
import com.example.test2.data.local.dao.PomodoroTaskDao
import com.example.test2.data.local.dao.TaskTagDao
import com.example.test2.data.local.dao.TaskLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供应用中需要的数据库相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供AppDatabase实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    /**
     * 提供GoalDao实例
     */
    @Provides
    @Singleton
    fun provideGoalDao(database: AppDatabase): GoalDao {
        return database.goalDao()
    }
    
    /**
     * 提供NoteDao实例
     */
    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }
    
    /**
     * 提供HabitDao实例
     */
    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao {
        return database.habitDao()
    }
    
    /**
     * 提供HabitLogDao实例
     */
    @Provides
    @Singleton
    fun provideHabitLogDao(database: AppDatabase): HabitLogDao {
        return database.habitLogDao()
    }
    
    /**
     * 提供BadgeDao实例
     */
    @Provides
    @Singleton
    fun provideBadgeDao(database: AppDatabase): BadgeDao {
        return database.badgeDao()
    }
    
    /**
     * 提供UserBadgeDao实例
     */
    @Provides
    @Singleton
    fun provideUserBadgeDao(database: AppDatabase): UserBadgeDao {
        return database.userBadgeDao()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao {
        return appDatabase.taskDao()
    }
    
    @Provides
    @Singleton
    fun provideCheckInTaskDao(appDatabase: AppDatabase): CheckInTaskDao {
        return appDatabase.checkInTaskDao()
    }
    
    @Provides
    @Singleton
    fun providePomodoroTaskDao(appDatabase: AppDatabase): PomodoroTaskDao {
        return appDatabase.pomodoroTaskDao()
    }
    
    @Provides
    @Singleton
    fun provideTaskTagDao(appDatabase: AppDatabase): TaskTagDao {
        return appDatabase.taskTagDao()
    }
    
    @Provides
    @Singleton
    fun provideTaskLogDao(appDatabase: AppDatabase): TaskLogDao {
        return appDatabase.taskLogDao()
    }
    
    // 随着应用扩展，可以在此添加更多DAO的Provider方法
} 