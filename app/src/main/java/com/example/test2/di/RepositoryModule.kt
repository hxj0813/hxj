package com.example.test2.di

import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.repository.GoalRepository
import com.example.test2.data.repository.NoteRepository
import com.example.test2.data.repository.impl.GoalRepositoryImpl
import com.example.test2.data.repository.impl.NoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库依赖注入模块
 * 使用Hilt提供仓库实例
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * 提供GoalRepository实例
     * 使用GoalRepositoryImpl作为具体实现
     */
    @Provides
    @Singleton
    fun provideGoalRepository(goalDao: GoalDao): GoalRepository {
        return GoalRepositoryImpl(goalDao)
    }
    
    /**
     * 提供NoteRepository实例
     * 使用NoteRepositoryImpl作为具体实现
     */
    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }
    
    // 随着应用扩展，可以在此添加更多Repository的Provider方法
} 