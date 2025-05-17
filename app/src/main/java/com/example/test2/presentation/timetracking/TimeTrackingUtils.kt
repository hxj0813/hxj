package com.example.test2.presentation.timetracking

import androidx.compose.ui.graphics.Color
import com.example.test2.data.model.TimeCategory
import com.example.test2.data.model.TimeEntry

/**
 * 时间追踪工具类
 */
object TimeTrackingUtils {
    
    /**
     * 获取分类的颜色对
     * 
     * @param category 时间分类
     * @return 颜色对（背景色，前景色）
     */
    fun getCategoryColors(category: TimeCategory): Pair<Color, Color> {
        return when (category) {
            TimeCategory.WORK -> Pair(Color(0xFF4285F4), Color(0xFF4285F4))      // 蓝色
            TimeCategory.STUDY -> Pair(Color(0xFF0F9D58), Color(0xFF0F9D58))     // 绿色
            TimeCategory.EXERCISE -> Pair(Color(0xFFF4B400), Color(0xFFF4B400))  // 黄色
            TimeCategory.LEISURE -> Pair(Color(0xFF7986CB), Color(0xFF7986CB))   // 淡紫色
            TimeCategory.FOCUS -> Pair(Color(0xFFDB4437), Color(0xFFDB4437))     // 红色
            TimeCategory.OTHER -> Pair(Color(0xFF9E9E9E), Color(0xFF9E9E9E))     // 灰色
        }
    }
    
    /**
     * 获取分类的主颜色
     * 
     * @param category 时间分类
     * @return 主颜色
     */
    fun getCategoryColor(category: TimeCategory): Color {
        return getCategoryColors(category).first
    }
    
    /**
     * 根据分类名称获取颜色
     * 
     * @param categoryName 分类名称
     * @return 主颜色
     */
    fun getCategoryColor(categoryName: String): Color {
        return try {
            val category = TimeCategory.valueOf(categoryName.uppercase())
            getCategoryColor(category)
        } catch (e: IllegalArgumentException) {
            // 尝试匹配常见标签颜色
            when (categoryName.lowercase()) {
                "学习", "study", "learning" -> Color(0xFF0F9D58)  // 绿色
                "工作", "work", "job" -> Color(0xFF4285F4)        // 蓝色
                "运动", "exercise", "workout" -> Color(0xFFF4B400) // 黄色
                "休闲", "leisure", "relax" -> Color(0xFF7986CB)    // 淡紫色
                "阅读", "reading", "book" -> Color(0xFF9C27B0)     // 紫色
                "创意", "creative" -> Color(0xFFE91E63)            // 粉色
                "个人发展", "personal" -> Color(0xFF3F51B5)        // 靛蓝色
                else -> Color(0xFF9E9E9E)                         // 默认灰色
            }
        }
    }
    
    /**
     * 获取分类的显示名称
     * 
     * @param category 时间分类
     * @return 分类名称
     */
    fun getCategoryName(category: TimeCategory): String {
        return when (category) {
            TimeCategory.WORK -> "工作"
            TimeCategory.STUDY -> "学习"
            TimeCategory.EXERCISE -> "锻炼"
            TimeCategory.LEISURE -> "休闲"
            TimeCategory.FOCUS -> "专注"
            TimeCategory.OTHER -> "其他"
        }
    }
    
    /**
     * 获取时间条目的显示分类
     * 优先使用标签，如果没有标签则使用分类
     * 
     * @param timeEntry 时间条目
     * @return 显示分类名称
     */
    fun getDisplayCategory(timeEntry: TimeEntry): String {
        // 如果有标签，优先使用第一个标签
        if (timeEntry.tags.isNotEmpty()) {
            return timeEntry.tags.first()
        }
        
        // 否则使用分类名称
        return getCategoryName(timeEntry.category)
    }
} 