package com.example.test2.presentation.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test2.data.model.Habit
import com.example.test2.data.model.HabitBadge
import com.example.test2.data.model.HabitCategory
import com.example.test2.presentation.habits.components.BadgeDetailDialog
import com.example.test2.presentation.habits.components.CheckInButton
import com.example.test2.presentation.habits.components.HabitBadgeView
import com.example.test2.presentation.habits.components.HabitProgressBar
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

/**
 * 习惯养成主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen() {
    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    var showBadgeDialog by remember { mutableStateOf<HabitBadge?>(null) }
    
    // 模拟加载数据
    LaunchedEffect(Unit) {
        delay(1000)
        habits = generateSampleHabits()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "习惯养成",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("添加习惯") },
                icon = { 
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加习惯"
                    )
                },
                onClick = { /* TODO: 实现添加习惯功能 */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // 加载中
                LoadingView()
            } else if (habits.isEmpty()) {
                // 空状态
                EmptyHabitsView()
            } else {
                // 习惯列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(habits) { habit ->
                        HabitCard(
                            habit = habit,
                            onClick = { selectedHabit = habit },
                            onBadgeClick = { badge -> showBadgeDialog = badge }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            
            // 显示选中的习惯详情
            if (selectedHabit != null) {
                HabitDetailDialog(
                    habit = selectedHabit!!,
                    onCheckIn = {
                        // 更新打卡状态
                        habits = habits.map { 
                            if (it.id == selectedHabit!!.id) {
                                // 模拟打卡，添加今天的日期到打卡记录
                                val newCheckIns = it.checkInRecords + Date()
                                it.copy(
                                    checkInRecords = newCheckIns,
                                    totalCheckIns = newCheckIns.size,
                                    currentStreak = it.currentStreak + 1,
                                    longestStreak = maxOf(it.longestStreak, it.currentStreak + 1),
                                    completionRate = newCheckIns.size.toFloat() / it.targetDays
                                )
                            } else {
                                it
                            }
                        }
                    },
                    onCancelCheckIn = {
                        // 取消今天的打卡
                        habits = habits.map { 
                            if (it.id == selectedHabit!!.id) {
                                // 模拟取消打卡，移除今天的日期
                                val today = Date()
                                val newCheckIns = it.checkInRecords.filterNot { date -> 
                                    Habit.isSameDay(date, today) 
                                }
                                it.copy(
                                    checkInRecords = newCheckIns,
                                    totalCheckIns = newCheckIns.size,
                                    currentStreak = maxOf(0, it.currentStreak - 1),
                                    completionRate = newCheckIns.size.toFloat() / it.targetDays
                                )
                            } else {
                                it
                            }
                        }
                    },
                    onBadgeClick = { badge -> showBadgeDialog = badge },
                    onDismiss = { selectedHabit = null }
                )
            }
            
            // 显示徽章详情对话框
            if (showBadgeDialog != null) {
                Dialog(onDismissRequest = { showBadgeDialog = null }) {
                    BadgeDetailDialog(
                        badge = showBadgeDialog!!,
                        onDismiss = { showBadgeDialog = null }
                    )
                }
            }
        }
    }
}

/**
 * 习惯卡片
 */
@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onBadgeClick: (HabitBadge) -> Unit
) {
    val habitColor = Color(habit.color)
    val progress = habit.calculateProgress()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题栏
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 类别指示器
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(habitColor)
                )
                
                Spacer(modifier = Modifier.padding(8.dp))
                
                // 标题
                Text(
                    text = habit.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 完成比例
                Text(
                    text = "${habit.totalCheckIns}/${habit.targetDays}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            HabitProgressBar(
                progress = progress,
                targetDays = habit.targetDays,
                currentDays = habit.totalCheckIns,
                habitColor = habitColor
            )
            
            // 如果有徽章，显示徽章列表
            if (habit.badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                HabitBadgeView(
                    badges = habit.badges,
                    onBadgeClick = onBadgeClick
                )
            }
        }
    }
}

/**
 * 习惯详情对话框
 */
@Composable
fun HabitDetailDialog(
    habit: Habit,
    onCheckIn: () -> Unit,
    onCancelCheckIn: () -> Unit,
    onBadgeClick: (HabitBadge) -> Unit,
    onDismiss: () -> Unit
) {
    val habitColor = Color(habit.color)
    val progress = habit.calculateProgress()
    val isCheckedIn = habit.isCheckedInToday()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // 顶部关闭按钮
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 习惯名称
                Text(
                    text = habit.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = habitColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 习惯描述
                if (habit.description != null) {
                    Text(
                        text = habit.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 进度条
                HabitProgressBar(
                    progress = progress,
                    targetDays = habit.targetDays,
                    currentDays = habit.totalCheckIns,
                    habitColor = habitColor
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 打卡按钮
                CheckInButton(
                    isCheckedIn = isCheckedIn,
                    currentStreak = habit.currentStreak,
                    habitColor = habitColor,
                    onCheckIn = onCheckIn,
                    onCancelCheckIn = onCancelCheckIn
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 统计信息
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 连续天数
                    StatItem(
                        value = habit.currentStreak.toString(),
                        label = "连续天数",
                        color = habitColor
                    )
                    
                    // 最长连续
                    StatItem(
                        value = habit.longestStreak.toString(),
                        label = "最长记录",
                        color = habitColor
                    )
                    
                    // 总打卡次数
                    StatItem(
                        value = habit.totalCheckIns.toString(),
                        label = "总次数",
                        color = habitColor
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 如果有徽章，显示徽章列表
                if (habit.badges.isNotEmpty()) {
                    HabitBadgeView(
                        badges = habit.badges,
                        onBadgeClick = onBadgeClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 统计项目组件
 */
@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = color
        )
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 加载中视图
 */
@Composable
fun LoadingView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "加载中...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 空习惯视图
 */
@Composable
fun EmptyHabitsView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "没有习惯",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "点击右下角的按钮添加你的第一个习惯",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 生成示例习惯数据
 */
private fun generateSampleHabits(): List<Habit> {
    val calendar = Calendar.getInstance()
    
    // 设置为今天凌晨
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val today = calendar.time
    
    // 创建打卡记录列表
    val readingCheckIns = mutableListOf<Date>()
    val exerciseCheckIns = mutableListOf<Date>()
    val meditationCheckIns = mutableListOf<Date>()
    
    // 为阅读习惯添加连续15天的记录（包括今天）
    for (i in 14 downTo 0) {
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        readingCheckIns.add(calendar.time)
    }
    
    // 为运动习惯添加连续7天的记录（但今天还没打卡）
    for (i in 7 downTo 1) {
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        exerciseCheckIns.add(calendar.time)
    }
    
    // 为冥想习惯添加间断的记录
    for (i in listOf(1, 3, 4, 6, 9, 12)) {
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        meditationCheckIns.add(calendar.time)
    }
    
    // 创建徽章列表
    val readingBadges = listOf(
        HabitBadge(
            type = com.example.test2.data.model.HabitBadgeType.STARTER,
            title = "阅读新手",
            description = "开始了阅读习惯养成之旅",
            iconUrl = "",
            isUnlocked = true,
            unlockedAt = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -14) 
            }.time
        ),
        HabitBadge(
            type = com.example.test2.data.model.HabitBadgeType.PERSISTENT,
            title = "坚持阅读",
            description = "连续阅读7天",
            iconUrl = "",
            isUnlocked = true,
            unlockedAt = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -7) 
            }.time
        ),
        HabitBadge(
            type = com.example.test2.data.model.HabitBadgeType.DEDICATED,
            title = "阅读爱好者",
            description = "连续阅读30天",
            iconUrl = "",
            isUnlocked = false
        )
    )
    
    val exerciseBadges = listOf(
        HabitBadge(
            type = com.example.test2.data.model.HabitBadgeType.STARTER,
            title = "运动新手",
            description = "开始了运动习惯养成之旅",
            iconUrl = "",
            isUnlocked = true,
            unlockedAt = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -7) 
            }.time
        ),
        HabitBadge(
            type = com.example.test2.data.model.HabitBadgeType.EARLY_BIRD,
            title = "早起锻炼",
            description = "在早晨6点前开始运动",
            iconUrl = "",
            isUnlocked = true,
            unlockedAt = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -3) 
            }.time
        )
    )
    
    return listOf(
        Habit(
            title = "每日阅读30分钟",
            description = "培养阅读习惯，每天阅读30分钟",
            category = HabitCategory.READING,
            targetDays = 21,
            startDate = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -14) 
            }.time,
            checkInRecords = readingCheckIns,
            badges = readingBadges,
            color = 0xFF2196F3,
            currentStreak = 15,
            longestStreak = 15,
            totalCheckIns = readingCheckIns.size,
            completionRate = readingCheckIns.size.toFloat() / 21
        ),
        Habit(
            title = "每天锻炼",
            description = "每天进行30分钟的体能训练",
            category = HabitCategory.SPORTS,
            targetDays = 30,
            startDate = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -7) 
            }.time,
            checkInRecords = exerciseCheckIns,
            badges = exerciseBadges,
            color = 0xFF4CAF50,
            currentStreak = 7,
            longestStreak = 7,
            totalCheckIns = exerciseCheckIns.size,
            completionRate = exerciseCheckIns.size.toFloat() / 30
        ),
        Habit(
            title = "冥想练习",
            description = "每天进行15分钟的冥想",
            category = HabitCategory.MEDITATION,
            targetDays = 40,
            startDate = calendar.apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -12) 
            }.time,
            checkInRecords = meditationCheckIns,
            color = 0xFF9C27B0,
            currentStreak = 0,
            longestStreak = 2,
            totalCheckIns = meditationCheckIns.size,
            completionRate = meditationCheckIns.size.toFloat() / 40
        )
    )
} 