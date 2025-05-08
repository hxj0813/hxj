package com.example.test2.di

import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.repository.GoalRepository
import com.example.test2.data.repository.NoteRepository
import com.example.test2.data.repository.TimeEntryRepository
import com.example.test2.data.repository.impl.GoalRepositoryImpl
import com.example.test2.data.repository.impl.NoteRepositoryImpl
import com.example.test2.data.repository.impl.TimeEntryRepositoryImpl
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
    
    /**
     * 提供TimeEntryRepository实例
     * 使用TimeEntryRepositoryImpl作为具体实现
     */
    @Provides
    @Singleton
    fun provideTimeEntryRepository(): TimeEntryRepository {
        return TimeEntryRepositoryImpl()
    }
    
    // 注意：TaskRepository, CheckInTaskRepository, PomodoroTaskRepository,
    // TaskTagRepository, 和 TaskLogRepository 使用@Inject构造器直接注入，
    // 不需要在这里显式提供
} 