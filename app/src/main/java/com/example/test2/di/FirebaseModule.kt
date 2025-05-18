package com.example.test2.di

import com.example.test2.data.firebase.repository.FirebaseAuthRepository
import com.example.test2.data.firebase.repository.FirebaseAuthRepositoryImpl
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepository
import com.example.test2.data.firebase.repository.FirebaseHabitNoteRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase依赖注入模块
 * 提供Firebase相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    /**
     * 提供Firebase认证实例
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    /**
     * 提供Firebase Firestore实例
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    /**
     * 提供Firebase Storage实例
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    /**
     * 提供Firebase认证存储库
     */
    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseAuthRepository {
        return FirebaseAuthRepositoryImpl(auth, firestore)
    }
    
    /**
     * 提供Firebase习惯笔记存储库
     */
    @Provides
    @Singleton
    fun provideFirebaseHabitNoteRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirebaseHabitNoteRepository {
        return FirebaseHabitNoteRepositoryImpl(auth, firestore, storage)
    }
} 