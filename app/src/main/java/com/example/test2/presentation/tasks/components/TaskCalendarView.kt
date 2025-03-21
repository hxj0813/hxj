package com.example.test2.presentation.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import com.example.test2.presentation.theme.PrimaryDark
import com.example.test2.presentation.theme.PrimaryLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

/**
 * 获取一个月的天数矩阵
 * 
 * @param year 年份
 * @param month 月份 (0-11)
 * @return 包含该月所有日期的二维数组，包括上月和下月的补充日期
 */
private fun getDaysInMonth(year: Int, month: Int): List<List<CalendarDay>> {
    val result = mutableListOf<List<CalendarDay>>()
    val calendar = Calendar.getInstance()
    
    // 设置为当月1号
    calendar.set(year, month, 1)
    
    // 获取当月1号是星期几
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    
    // 获取当月的天数
    val numDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // 创建上个月的日期填充
    val daysFromPrevMonth = mutableListOf<CalendarDay>()
    if (firstDayOfWeek > 0) {
        val prevCalendar = calendar.clone() as Calendar
        prevCalendar.add(Calendar.MONTH, -1)
        val numDaysInPrevMonth = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (i in numDaysInPrevMonth - firstDayOfWeek + 1..numDaysInPrevMonth) {
            prevCalendar.set(Calendar.DAY_OF_MONTH, i)
            daysFromPrevMonth.add(CalendarDay(
                date = prevCalendar.time,
                isCurrentMonth = false
            ))
        }
    }
    
    // 创建当月的日期
    val daysInCurrentMonth = mutableListOf<CalendarDay>()
    for (i in 1..numDaysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, i)
        daysInCurrentMonth.add(CalendarDay(
            date = calendar.time,
            isCurrentMonth = true
        ))
    }
    
    // 合并并按周分组
    val allDays = daysFromPrevMonth + daysInCurrentMonth
    val weeks = allDays.chunked(7)
    
    // 如果最后一周不足7天，添加下个月的日期
    val lastWeek = weeks.last()
    if (lastWeek.size < 7) {
        val nextCalendar = calendar.clone() as Calendar
        nextCalendar.add(Calendar.MONTH, 1)
        
        val fillerDays = mutableListOf<CalendarDay>()
        for (i in 1..(7 - lastWeek.size)) {
            nextCalendar.set(Calendar.DAY_OF_MONTH, i)
            fillerDays.add(CalendarDay(
                date = nextCalendar.time,
                isCurrentMonth = false
            ))
        }
        
        result.addAll(weeks.dropLast(1))
        result.add(lastWeek + fillerDays)
    } else {
        result.addAll(weeks)
    }
    
    return result
}

/**
 * 日历日期数据类
 */
data class CalendarDay(
    val date: Date,
    val isCurrentMonth: Boolean
)

/**
 * 任务日历视图组件 - 显示两周的日期选择器
 */
@Composable
fun TaskCalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    tasksPerDay: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    // 生成两周的日期列表
    val dates = remember {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // 从一周前开始
        
        List(14) { index ->
            val date = calendar.time.clone() as Date
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            date
        }
    }
    
    val dayFormatter = remember { SimpleDateFormat("d", Locale.getDefault()) }
    val weekDayFormatter = remember { SimpleDateFormat("E", Locale.getDefault()) }
    val dateKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    val today = remember { Date() }
    val todayStr = remember { dateKeyFormatter.format(today) }
    
    val listState = rememberLazyListState()
    
    // 找到今天在列表中的位置
    val todayIndex = dates.indexOfFirst { 
        dateKeyFormatter.format(it) == todayStr 
    }.coerceAtLeast(0)
    
    // 滚动到今天的位置
    LaunchedEffect(Unit) {
        listState.scrollToItem(todayIndex.coerceAtMost(dates.size - 1))
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "日历",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dates) { date ->
                    val isSelected = remember(selectedDate, date) {
                        val cal1 = Calendar.getInstance().apply { time = selectedDate }
                        val cal2 = Calendar.getInstance().apply { time = date }
                        
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                        cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                    }
                    
                    val isToday = remember(date) {
                        val cal1 = Calendar.getInstance()
                        val cal2 = Calendar.getInstance().apply { time = date }
                        
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                        cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                    }
                    
                    val dateKey = dateKeyFormatter.format(date)
                    val taskCount = tasksPerDay[dateKey] ?: 0
                    
                    CalendarDay(
                        date = date,
                        dayText = dayFormatter.format(date),
                        weekDayText = weekDayFormatter.format(date),
                        isSelected = isSelected,
                        isToday = isToday,
                        taskCount = taskCount,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

/**
 * 日历天组件
 */
@Composable
private fun CalendarDay(
    date: Date,
    dayText: String,
    weekDayText: String,
    isSelected: Boolean,
    isToday: Boolean,
    taskCount: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> PrimaryLight
        isToday -> PrimaryLight.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color.White
        else -> Color.DarkGray
    }
    
    val weekDayColor = when {
        isSelected -> Color.White.copy(alpha = 0.8f)
        else -> Color.Gray
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(45.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, PrimaryLight, CircleShape)
                } else {
                    Modifier
                }
            )
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = weekDayText,
            color = weekDayColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = dayText,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // 任务指示器
        if (taskCount > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.3f)
                        else PrimaryDark.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = min(taskCount, 9).toString(),
                    color = if (isSelected) Color.White else PrimaryDark,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 