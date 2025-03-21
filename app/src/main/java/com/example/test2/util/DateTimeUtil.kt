package com.example.test2.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 日期时间工具类
 */
object DateTimeUtil {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val monthDayFormatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
    
    /**
     * 格式化日期为字符串：yyyy-MM-dd
     */
    fun formatDate(date: Date?): String {
        return date?.let { dateFormatter.format(it) } ?: ""
    }
    
    /**
     * 格式化日期时间为字符串：yyyy-MM-dd HH:mm
     */
    fun formatDateTime(date: Date?): String {
        return date?.let { dateTimeFormatter.format(it) } ?: ""
    }
    
    /**
     * 格式化日期为更友好的格式：MM月dd日
     */
    fun formatDateFriendly(date: Date?): String {
        if (date == null) return ""
        
        val today = Date()
        val tomorrow = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        
        return when {
            isSameDay(date, today) -> "今天"
            isSameDay(date, tomorrow) -> "明天"
            else -> monthDayFormatter.format(date)
        }
    }
    
    /**
     * 获取与deadline的相差天数
     */
    fun getRemainingDays(deadline: Date): Int {
        val now = Date()
        val diffInMillis = deadline.time - now.time
        return TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
    }
    
    /**
     * 将剩余时间格式化为友好字符串
     */
    fun formatRemainingTime(deadline: Date): String {
        val days = getRemainingDays(deadline)
        
        return when {
            days < 0 -> "已逾期${-days}天"
            days == 0 -> "今天到期"
            days == 1 -> "明天到期"
            days <= 3 -> "还剩${days}天"
            else -> formatDateFriendly(deadline)
        }
    }
    
    /**
     * 检查日期是否即将到期（3天内）
     */
    fun isUpcoming(deadline: Date): Boolean {
        val days = getRemainingDays(deadline)
        return days in 0..3
    }
    
    /**
     * 检查日期是否已过期
     */
    fun isOverdue(deadline: Date): Boolean {
        return getRemainingDays(deadline) < 0
    }
    
    /**
     * 判断两个日期是否是同一天
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
    
    /**
     * 获取今天的开始时间 (00:00:00)
     */
    fun getStartOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    /**
     * 获取今天的结束时间 (23:59:59)
     */
    fun getEndOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
    
    /**
     * 获取一周后的日期
     */
    fun getOneWeekLater(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }.time
    }
    
    /**
     * 格式化为日期键 yyyy-MM-dd
     */
    fun formatDateKey(date: Date): String {
        return dateFormatter.format(date)
    }
} 