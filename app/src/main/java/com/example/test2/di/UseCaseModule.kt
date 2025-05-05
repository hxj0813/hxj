package com.example.test2.di

import com.example.test2.data.repository.GoalRepository
import com.example.test2.data.repository.NoteRepository
import com.example.test2.domain.usecase.GoalUseCases
import com.example.test2.domain.usecase.NoteUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 用例依赖注入模块
 * 使用Hilt提供用例实例
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    /**
     * 提供GoalUseCases实例
     */
    @Provides
    @Singleton
    fun provideGoalUseCases(repository: GoalRepository): GoalUseCases {
        return GoalUseCases(repository)
    }
    
    /**
     * 提供NoteUseCases实例
     */
    @Provides
    @Singleton
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(repository)
    }
    
    // 随着应用扩展，可以在此添加更多UseCase的Provider方法
} 