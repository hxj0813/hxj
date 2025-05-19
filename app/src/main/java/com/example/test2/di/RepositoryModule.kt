package com.example.test2.di

import android.content.Context
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepository
import com.example.test2.data.local.AppDatabase
import com.example.test2.data.local.dao.BadgeDao
import com.example.test2.data.local.dao.GoalDao
import com.example.test2.data.local.dao.NoteDao
import com.example.test2.data.local.dao.UserBadgeDao
import com.example.test2.data.repository.BadgeRepository
import com.example.test2.data.repository.BadgeRepositoryImpl
import com.example.test2.data.repository.GoalRepository
import com.example.test2.data.repository.HybridNoteRepository
import com.example.test2.data.repository.NoteRepository
import com.example.test2.data.repository.TimeEntryRepository
import com.example.test2.data.repository.TimeTrackingRepository
import com.example.test2.data.repository.impl.GoalRepositoryImpl
import com.example.test2.data.repository.impl.HybridNoteRepositoryImpl
import com.example.test2.data.repository.impl.NoteRepositoryImpl
import com.example.test2.data.repository.impl.TimeEntryRepositoryImpl
import com.example.test2.util.NoteImageManager
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库模块
 * 提供各种仓库的依赖注入
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
    fun provideNoteRepository(
        noteDao: NoteDao,
        @ApplicationContext context: Context,
        imageManager: NoteImageManager
    ): NoteRepository {
        return NoteRepositoryImpl(noteDao, context, imageManager)
    }
    
    /**
     * 提供HybridNoteRepository实例
     * 集成本地和云端存储
     */
    @Provides
    @Singleton
    fun provideHybridNoteRepository(
        localRepository: NoteRepository,
        firebaseRepository: FirebaseHabitNoteRepository,
        auth: FirebaseAuth,
        @ApplicationContext context: Context
    ): HybridNoteRepository {
        return HybridNoteRepositoryImpl(localRepository, firebaseRepository, auth, context)
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
    
    @Provides
    @Singleton
    fun provideTimeTrackingRepository(db: AppDatabase): TimeTrackingRepository {
        return TimeTrackingRepository(
            timeTrackingDao = db.timeTrackingDao()
        )
    }
    
    /**
     * 提供BadgeRepository实例
     * 使用BadgeRepositoryImpl作为具体实现
     */
    @Provides
    @Singleton
    fun provideBadgeRepository(
        badgeDao: BadgeDao,
        userBadgeDao: UserBadgeDao
    ): BadgeRepository {
        return BadgeRepositoryImpl(badgeDao, userBadgeDao)
    }
    
    // 注意：TaskRepository, CheckInTaskRepository, PomodoroTaskRepository,
    // TaskTagRepository, 和 TaskLogRepository 使用@Inject构造器直接注入，
    // 不需要在这里显式提供
} 