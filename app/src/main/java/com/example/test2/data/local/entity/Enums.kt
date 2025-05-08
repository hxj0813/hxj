package com.example.test2.data.local.entity

/**
 * 任务类型枚举
 */
enum class TaskType {
    CHECK_IN,    // 打卡任务
    POMODORO;    // 番茄钟任务
    
    companion object {
        fun fromInt(value: Int): TaskType = values().getOrElse(value) { CHECK_IN }
    }
}

/**
 * 任务优先级枚举
 */
enum class TaskPriority {
    HIGH,       // 高优先级
    MEDIUM,     // 中优先级
    LOW;        // 低优先级
    
    companion object {
        fun fromInt(value: Int): TaskPriority = values().getOrElse(value) { MEDIUM }
    }
}

/**
 * 频率类型枚举
 */
enum class FrequencyType {
    DAILY,      // 每日打卡
    WEEKLY,     // 每周打卡
    MONTHLY,    // 每月打卡
    CUSTOM;     // 自定义
    
    companion object {
        fun fromInt(value: Int): FrequencyType = values().getOrElse(value) { DAILY }
    }
}

/**
 * 标签分类枚举
 */
enum class TagCategory {
    WORK,       // 工作
    STUDY,      // 学习
    EXERCISE,   // 运动
    READING,    // 阅读
    CREATIVE,   // 创意
    PERSONAL,   // 个人发展
    OTHER,      // 其他
    CUSTOM;     // 自定义
    
    companion object {
        fun fromInt(value: Int): TagCategory = values().getOrElse(value) { OTHER }
    }
} 