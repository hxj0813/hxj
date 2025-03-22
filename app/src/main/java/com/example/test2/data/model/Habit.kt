package com.example.test2.data.model

import java.util.Date
import java.util.UUID

/**
 * 涔犳儻鍏绘垚棰戠巼绫诲瀷
 */
enum class HabitFrequency {
    DAILY,      // 姣忓ぉ
    WEEKDAYS,   // 宸ヤ綔鏃?
    WEEKLY,     // 姣忓懆
    MONTHLY     // 姣忔湀
}

/**
 * 涔犳儻绫诲瀷
 */
enum class HabitCategory {
    HEALTH,     // 鍋ュ悍
    STUDY,      // 瀛︿範
    WORK,       // 宸ヤ綔
    SPORTS,     // 杩愬姩
    READING,    // 闃呰
    MEDITATION, // 鍐ユ兂
    OTHER       // 鍏朵粬
}

/**
 * 涔犳儻鎴愬氨寰界珷绫诲瀷
 */
enum class HabitBadgeType {
    STARTER,        // 鍒濆鑰咃紙寮€濮嬩竴涓柊涔犳儻锛?
    PERSISTENT,     // 鍧氭寔鑰咃紙杩炵画7澶╋級
    DEDICATED,      // 涓撴敞鑰咃紙杩炵画30澶╋級
    MASTER,         // 澶у笀锛堣繛缁?00澶╋級
    COMEBACK,       // 鍥炲綊鑰咃紙涓柇鍚庨噸鏂板紑濮嬶級
    CONSISTENT,     // 绋冲畾鑰咃紙瀹屾垚鐜?0%浠ヤ笂锛?
    EARLY_BIRD,     // 鏃╄捣楦燂紙娓呮櫒瀹屾垚涔犳儻锛?
    NIGHT_OWL,      // 澶滅尗瀛愶紙鏅氫笂瀹屾垚涔犳儻锛?
    SOCIAL,         // 绀句氦杈句汉锛堝垎浜範鎯級
    MILESTONE       // 閲岀▼纰戯紙鑷畾涔夋垚灏憋級
}

/**
 * 涔犳儻鎴愬氨寰界珷鏁版嵁绫?
 */
data class HabitBadge(
    val id: String = UUID.randomUUID().toString(),
    val type: HabitBadgeType,
    val title: String,
    val description: String,
    val iconUrl: String,
    val unlockedAt: Date? = null,
    val isUnlocked: Boolean = false
)

/**
 * 涔犳儻鏁版嵁妯″瀷
 */
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,                              // 涔犳儻鏍囬
    val description: String? = null,                // 涔犳儻鎻忚堪
    val category: HabitCategory = HabitCategory.OTHER, // 涔犳儻绫诲埆
    val frequency: HabitFrequency = HabitFrequency.DAILY, // 鎵撳崱棰戠巼
    val targetDays: Int = 21,                       // 鐩爣澶╂暟锛堥粯璁?1澶╁舰鎴愪範鎯級
    val isRemindable: Boolean = false,              // 鏄惁闇€瑕佹彁閱?
    val reminderTime: Date? = null,                 // 鎻愰啋鏃堕棿
    val startDate: Date = Date(),                   // 寮€濮嬫棩鏈?
    val checkInRecords: List<Date> = emptyList(),   // 鎵撳崱璁板綍
    val badges: List<HabitBadge> = emptyList(),     // 鑾峰緱鐨勬垚灏卞窘绔?
    val color: Long = 0xFF4A90E2,                   // 涔犳儻棰滆壊锛堥粯璁よ摑鑹诧級
    val icon: String = "default_habit",             // 涔犳儻鍥炬爣
    val currentStreak: Int = 0,                     // 褰撳墠杩炵画鎵撳崱澶╂暟
    val longestStreak: Int = 0,                     // 鏈€闀胯繛缁墦鍗″ぉ鏁?
    val totalCheckIns: Int = 0,                     // 鎬绘墦鍗℃鏁?
    val completionRate: Float = 0f,                 // 瀹屾垚鐜?
    val notes: List<HabitNote> = emptyList(),       // 涔犳儻绗旇
    val isArchived: Boolean = false,                // 鏄惁褰掓。
    val createdAt: Date = Date(),                   // 鍒涘缓鏃堕棿
    val updatedAt: Date = Date()                    // 鏇存柊鏃堕棿
) {
    /**
     * 浠婂ぉ鏄惁宸叉墦鍗?
     */
    fun isCheckedInToday(): Boolean {
        val today = Date()
        return checkInRecords.any { record ->
            isSameDay(record, today)
        }
    }
    
    /**
     * 璁＄畻杩涘害鐧惧垎姣?
     */
    fun calculateProgress(): Float {
        return (totalCheckIns.toFloat() / targetDays).coerceIn(0f, 1f)
    }
    
    /**
     * 鍒ゆ柇鏄惁宸插畬鎴愮洰鏍囧ぉ鏁?
     */
    fun isCompleted(): Boolean {
        return totalCheckIns >= targetDays
    }
    
    /**
     * 璁＄畻涔犳儻宸插紑濮嬬殑澶╂暟
     */
    fun daysSinceStart(): Int {
        val today = Date()
        val diffTime = today.time - startDate.time
        return (diffTime / (24 * 60 * 60 * 1000)).toInt()
    }
    
    companion object {
        /**
         * 鍒ゆ柇涓や釜鏃ユ湡鏄惁鏄悓涓€澶?
         */
        fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
            val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
            return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                   cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
        }
    }
} 
