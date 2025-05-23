package com.example.test2.presentation.mine.viewmodel

import androidx.lifecycle.ViewModel
import com.example.test2.data.local.prefs.PreferencesHelper
import com.example.test2.data.local.prefs.ThemeMode
import com.example.test2.ui.theme.ThemeDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 设置ViewModel
 * 管理应用设置相关的数据和操作
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeDataStore: ThemeDataStore
) : ViewModel() {
    
    /**
     * 获取当前主题模式
     */
    fun getThemeMode(): ThemeMode {
        return themeDataStore.getThemeMode()
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(themeMode: ThemeMode) {
        themeDataStore.setThemeMode(themeMode)
    }
} 