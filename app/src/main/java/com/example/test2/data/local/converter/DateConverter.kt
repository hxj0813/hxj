package com.example.test2.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room数据库Date类型转换器
 * 
 * 由于SQLite不直接支持Date类型，此转换器用于：
 * 1. 将Date对象转换为Long类型的时间戳存储到数据库
 * 2. 将从数据库读取的Long类型时间戳转换回Date对象
 */
class DateConverter {
    /**
     * 将Long时间戳转换为Date对象
     * 
     * @param value 毫秒时间戳
     * @return 转换后的Date对象，如果输入为null则返回null
     */

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * 将Date对象转换为Long时间戳
     * 
     * @param date Date对象
     * @return 毫秒时间戳，如果输入为null则返回null
     */
     
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}