package com.example.test2.di

import android.content.Context
import com.example.test2.util.NoteImageManager
import com.example.test2.data.local.prefs.PreferencesHelper
import com.example.test2.ui.theme.ThemeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 工具模块
 * 提供应用中需要的工具类依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    /**
     * 提供NoteImageManager实例
     * 用于管理笔记图片的保存、加载和删除
     */
    @Provides
    @Singleton
    fun provideNoteImageManager(
        @ApplicationContext context: Context
    ): NoteImageManager {
        return NoteImageManager(context)
    }
    
    /**
     * 提供PreferencesHelper实例
     * 用于管理应用的SharedPreferences数据
     */
    @Provides
    @Singleton
    fun providePreferencesHelper(
        @ApplicationContext context: Context
    ): PreferencesHelper {
        return PreferencesHelper(context)
    }
    
    /**
     * 提供ThemeDataStore实例
     * 用于管理应用的主题状态
     */
    @Provides
    @Singleton
    fun provideThemeDataStore(
        preferencesHelper: PreferencesHelper
    ): ThemeDataStore {
        return ThemeDataStore(preferencesHelper)
    }
    
    // 随着应用扩展，可以在此添加更多工具类的Provider方法
} 