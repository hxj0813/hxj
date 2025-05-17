package com.example.test2.di

import com.example.test2.data.firebase.repository.FirebaseAuthRepository
import com.example.test2.data.firebase.repository.FirebaseAuthRepositoryImpl
import com.example.test2.data.firebase.repository.FirebaseNoteRepository
import com.example.test2.data.firebase.repository.FirebaseNoteRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase依赖注入模块
 * 提供Firebase服务的实例
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * 提供Firebase认证服务实例
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    /**
     * 提供Firebase云存储服务实例
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    /**
     * 提供Firebase存储服务实例
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage
}

/**
 * Firebase存储库绑定模块
 * 将接口绑定到具体实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseRepositoryModule {

    /**
     * 绑定Firebase认证存储库
     */
    @Binds
    @Singleton
    abstract fun bindFirebaseAuthRepository(
        repository: FirebaseAuthRepositoryImpl
    ): FirebaseAuthRepository

    /**
     * 绑定Firebase笔记存储库
     */
    @Binds
    @Singleton
    abstract fun bindFirebaseNoteRepository(
        repository: FirebaseNoteRepositoryImpl
    ): FirebaseNoteRepository
} 