package com.example.test2.data.local.converter

import androidx.room.TypeConverter
import com.example.test2.data.model.TimeCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 时间追踪模块类型转换器
 * 用于在 Room 数据库中存储复杂类型
 */
class TimeTrackerConverter {
    private val gson = Gson()
    
    /**
     * 时间分类枚举转换
     */
    @TypeConverter
    fun fromTimeCategoryToString(category: TimeCategory): String {
        return category.name
    }

    @TypeConverter
    fun toTimeCategory(value: String): TimeCategory {
        return try {
            TimeCategory.valueOf(value)
        } catch (e: Exception) {
            TimeCategory.OTHER
        }
    }

    /**
     * 字符串列表转换
     */
    @TypeConverter
    fun fromListStringToString(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toListStringFromString(string: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(string, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
} 