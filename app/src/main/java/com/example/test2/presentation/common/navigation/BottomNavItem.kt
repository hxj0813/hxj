package com.example.test2.presentation.common.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项枚举
 * 定义应用中的主要导航目标
 */
enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    HABITS("habits", Icons.Default.Home, "习惯"),
    TASKS("tasks", Icons.Default.Task, "任务"),
    GOALS("goals", Icons.Default.Flag, "目标"),
    TIME_TRACKING("time_tracking", Icons.Default.AccessTime, "时间"),
    NOTES("notes", Icons.Default.Notes, "笔记")
} 