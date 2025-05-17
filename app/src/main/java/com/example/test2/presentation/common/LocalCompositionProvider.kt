package com.example.test2.presentation.common

import androidx.compose.runtime.compositionLocalOf
import com.example.test2.presentation.timetracking.TimeTrackingViewModel

/**
 * 本地组合提供者
 * 用于在整个组合树中访问 ViewModel
 */

/**
 * 时间追踪ViewModel的本地提供者
 */
val LocalTimeTrackingViewModel = compositionLocalOf<TimeTrackingViewModel> { 
    error("No TimeTrackingViewModel provided") 
} 