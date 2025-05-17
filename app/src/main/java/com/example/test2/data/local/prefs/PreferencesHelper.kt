package com.example.test2.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 偏好设置帮助类
 * 用于管理应用的SharedPreferences数据
 */
@Singleton
class PreferencesHelper @Inject constructor(
    context: Context
) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * 保存目标最后更新日期
     */
    fun saveGoalLastUpdateDate(goalId: Long, date: Date) {
        preferences.edit().putLong(PREF_GOAL_LAST_UPDATE_PREFIX + goalId, date.time).apply()
    }
    
    /**
     * 获取目标最后更新日期
     * 如果不存在则返回null
     */
    fun getGoalLastUpdateDate(goalId: Long): Date? {
        val timestamp = preferences.getLong(PREF_GOAL_LAST_UPDATE_PREFIX + goalId, -1)
        return if (timestamp != -1L) Date(timestamp) else null
    }
    
    /**
     * 清除所有目标更新日期记录
     */
    fun clearAllGoalUpdateDates() {
        val allPrefs = preferences.all
        val editor = preferences.edit()
        
        allPrefs.keys.filter { it.startsWith(PREF_GOAL_LAST_UPDATE_PREFIX) }.forEach {
            editor.remove(it)
        }
        
        editor.apply()
    }
    
    companion object {
        private const val PREF_NAME = "app_preferences"
        private const val PREF_GOAL_LAST_UPDATE_PREFIX = "goal_last_update_"
    }
} 