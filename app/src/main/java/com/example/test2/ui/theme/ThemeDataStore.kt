package com.example.test2.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.test2.data.local.prefs.PreferencesHelper
import com.example.test2.data.local.prefs.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题数据存储
 * 管理应用的主题状态并提供给整个应用使用
 */
@Singleton
class ThemeDataStore @Inject constructor(
    private val preferencesHelper: PreferencesHelper
) {
    // 暗色主题状态
    private val _isDarkTheme = mutableStateOf(preferencesHelper.isDarkMode(false))
    val isDarkTheme: State<Boolean> = _isDarkTheme
    
    // 初始化主题
    init {
        updateTheme()
    }
    
    /**
     * 更新主题
     * 从PreferencesHelper读取最新的主题设置
     */
    fun updateTheme() {
        _isDarkTheme.value = preferencesHelper.isDarkMode(false)
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(themeMode: ThemeMode) {
        preferencesHelper.setThemeMode(themeMode)
        updateTheme()
    }
    
    /**
     * 切换主题
     */
    fun toggleTheme() {
        val newThemeMode = if (_isDarkTheme.value) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(newThemeMode)
    }
    
    /**
     * 获取当前主题模式
     */
    fun getThemeMode(): ThemeMode {
        return preferencesHelper.getThemeMode()
    }
} 