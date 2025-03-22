package com.example.test2.presentation.timetracking

import androidx.compose.ui.graphics.Color
import com.example.test2.data.model.TimeCategory

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
            TimeCategory.REST -> Pair(Color(0xFF7986CB), Color(0xFF7986CB))      // 淡紫色
            TimeCategory.ENTERTAIN -> Pair(Color(0xFFDB4437), Color(0xFFDB4437)) // 红色
            TimeCategory.OTHER -> Pair(Color(0xFF9E9E9E), Color(0xFF9E9E9E))     // 灰色
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
            TimeCategory.REST -> "休息"
            TimeCategory.ENTERTAIN -> "娱乐"
            TimeCategory.OTHER -> "其他"
        }
    }
} 