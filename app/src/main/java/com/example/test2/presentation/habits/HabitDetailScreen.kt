package com.example.test2.presentation.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.FrequencyType
import com.example.test2.data.local.entity.HabitCategory
import com.example.test2.data.local.entity.HabitEntity
import com.example.test2.data.local.entity.UserBadgeEntity
import com.example.test2.presentation.habits.components.BadgeList
import com.example.test2.presentation.habits.viewmodel.HabitDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.res.stringResource

/**
 * 分类信息数据类
 */
private data class HabitDetailCategoryInfo(
    val name: String,
    val icon: ImageVector
)

/**
 * 获取习惯分类信息
 */
private fun getCategoryInfo(category: com.example.test2.data.local.entity.HabitCategory): HabitDetailCategoryInfo {
    return when (category) {
        com.example.test2.data.local.entity.HabitCategory.HEALTH -> HabitDetailCategoryInfo("健康", Icons.Default.Favorite)
        com.example.test2.data.local.entity.HabitCategory.EXERCISE -> HabitDetailCategoryInfo("运动", Icons.Default.DirectionsRun)
        com.example.test2.data.local.entity.HabitCategory.STUDY -> HabitDetailCategoryInfo("学习", Icons.Default.School)
        com.example.test2.data.local.entity.HabitCategory.WORK -> HabitDetailCategoryInfo("工作", Icons.Default.Work)
        com.example.test2.data.local.entity.HabitCategory.MINDFULNESS -> HabitDetailCategoryInfo("冥想", Icons.Default.SelfImprovement)
        com.example.test2.data.local.entity.HabitCategory.SKILL -> HabitDetailCategoryInfo("技能", Icons.Default.Psychology)
        com.example.test2.data.local.entity.HabitCategory.SOCIAL -> HabitDetailCategoryInfo("社交", Icons.Default.People)
        com.example.test2.data.local.entity.HabitCategory.OTHER -> HabitDetailCategoryInfo("其他", Icons.Default.MoreHoriz)
    }
}

/**
 * 习惯详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onNavigateBack: () -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    // 加载习惯数据
    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
        viewModel.loadHabitBadges(habitId)
    }
    
    val habit by viewModel.habit.collectAsState()
    val userBadges by viewModel.userBadges.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.title ?: "习惯详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    habit?.let {
                        IconButton(onClick = { onEditHabit(it.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑习惯"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            habit?.let { habitEntity ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 习惯头部信息
                    HabitHeader(habit = habitEntity)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 习惯统计卡片
                    HabitStatsCard(habit = habitEntity)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 习惯详细信息
                    HabitInfoCard(habit = habitEntity)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 徽章部分
                    if (userBadges.isNotEmpty()) {
                        Text(
                            text = "已获得的徽章",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        BadgeList(
                            userBadges = userBadges,
                            allBadges = allBadges,
                            onBadgeClick = { badge, userBadge ->
                                viewModel.selectBadge(badge, userBadge)
                            }
                        )
                    } else {
                        // 未获得徽章时的提示
                        UnlockedBadgesPlaceholder()
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 下一个可解锁的徽章
                    NextBadgeToUnlock(
                        habit = habitEntity,
                        badges = allBadges.filter { !userBadges.any { ub -> ub.badgeId == it.id } }
                    )
                }
            } ?: run {
                // 加载中或未找到习惯
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    
    // 选中徽章的对话框
    BadgeDialog(viewModel)
}

@Composable
private fun BadgeDialog(viewModel: HabitDetailViewModel) {
    val selectedBadge by viewModel.selectedBadge.collectAsState()
    val selectedUserBadge by viewModel.selectedUserBadge.collectAsState()
    
    if (selectedBadge != null && selectedUserBadge != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedBadge() },
            title = { Text(text = selectedBadge!!.name) },
            text = {
                Column {
                    // 徽章图标
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally)
                            .background(
                                Color(selectedBadge!!.getColorByRarity()),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = selectedBadge!!.name,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 徽章描述
                    Text(text = selectedBadge!!.description)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 解锁时间
                    Text(
                        text = "解锁时间: ${SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(selectedUserBadge!!.unlockedAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // 解锁时的值（如果有）
                    selectedUserBadge!!.valueWhenUnlocked?.let { value ->
                        Text(
                            text = "解锁值: $value",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // 解锁备注（如果有）
                    selectedUserBadge!!.note?.let { note ->
                        Text(
                            text = "备注: $note",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSelectedBadge() }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun HabitHeader(habit: HabitEntity) {
    val categoryInfo = getCategoryInfo(habit.getCategoryEnum())
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(habit.color.toInt()), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryInfo.icon,
                contentDescription = habit.title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标题
        Text(
            text = habit.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        // 描述
        habit.description?.let {
            if (it.isNotBlank()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 类别标签
        AssistChip(
            onClick = { },
            label = { Text(categoryInfo.name) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun HabitStatsCard(habit: HabitEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HabitDetailStatItem(
                    label = "当前连续",
                    value = "${habit.currentStreak} 天",
                    icon = Icons.Default.Bolt
                )
                
                HabitDetailStatItem(
                    label = "最长连续",
                    value = "${habit.bestStreak} 天",
                    icon = Icons.Default.Timeline
                )
                
                HabitDetailStatItem(
                    label = "累计完成",
                    value = "${habit.totalCompletions} 次",
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
private fun HabitDetailStatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HabitInfoCard(habit: HabitEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "详细信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 频率信息
            InfoRow(
                icon = Icons.Default.Repeat,
                title = "频率",
                value = getHabitFrequencyText(habit)
            )
            
            // 优先级
            InfoRow(
                icon = Icons.Default.Flag,
                title = "优先级",
                value = when (habit.getPriorityEnum()) {
                    com.example.test2.data.local.entity.HabitPriority.LOW -> "低"
                    com.example.test2.data.local.entity.HabitPriority.MEDIUM -> "中"
                    com.example.test2.data.local.entity.HabitPriority.HIGH -> "高"
                }
            )
            
            // 开始日期
            InfoRow(
                icon = Icons.Default.CalendarToday,
                title = "开始日期",
                value = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(habit.startDate)
            )
            
            // 最后完成日期
            habit.lastCompletedDate?.let {
                InfoRow(
                    icon = Icons.Default.Event,
                    title = "上次完成",
                    value = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(it)
                )
            }
            
            // 提醒信息
            if (habit.reminder && habit.reminderTime != null) {
                InfoRow(
                    icon = Icons.Default.Notifications,
                    title = "提醒时间",
                    value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(habit.reminderTime)
                )
            }
            
            // 标签
            val tags = habit.getTagsList()
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "标签:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "$title:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun UnlockedBadgesPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "尚未获得徽章",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "继续坚持完成习惯，解锁成就徽章！",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NextBadgeToUnlock(
    habit: HabitEntity,
    badges: List<BadgeEntity>
) {
    // 筛选出连续天数和完成次数相关的徽章
    val streakBadges = badges.filter { 
        it.getCategoryEnum() == com.example.test2.data.local.entity.BadgeCategory.STREAK &&
        it.thresholdValue > habit.currentStreak
    }.sortedBy { it.thresholdValue }
    
    val completionBadges = badges.filter {
        it.getCategoryEnum() == com.example.test2.data.local.entity.BadgeCategory.COMPLETION &&
        it.thresholdValue > habit.totalCompletions
    }.sortedBy { it.thresholdValue }
    
    if (streakBadges.isEmpty() && completionBadges.isEmpty()) {
        return
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "下一个可解锁的徽章",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 连续天数下一个徽章
            streakBadges.firstOrNull()?.let { nextStreakBadge ->
                NextBadgeItem(
                    badge = nextStreakBadge,
                    currentValue = habit.currentStreak,
                    unit = "天"
                )
            }
            
            // 完成次数下一个徽章
            completionBadges.firstOrNull()?.let { nextCompletionBadge ->
                if (streakBadges.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                NextBadgeItem(
                    badge = nextCompletionBadge,
                    currentValue = habit.totalCompletions,
                    unit = "次"
                )
            }
        }
    }
}

@Composable
private fun NextBadgeItem(
    badge: BadgeEntity,
    currentValue: Int,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 徽章图标
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(badge.getColorByRarity()).copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = badge.name,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = badge.name,
                style = MaterialTheme.typography.titleSmall
            )
            
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = currentValue.toFloat() / badge.thresholdValue,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "$currentValue / ${badge.thresholdValue} $unit",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * 获取习惯频率文本描述
 */
private fun getHabitFrequencyText(habit: com.example.test2.data.local.entity.HabitEntity): String {
    return when (habit.getFrequencyTypeEnum()) {
        com.example.test2.data.local.entity.FrequencyType.DAILY -> "每日"
        com.example.test2.data.local.entity.FrequencyType.WEEKLY -> {
            val days = habit.getFrequencyDaysList()
            if (days.isEmpty()) "每周" else "每周${days.size}天"
        }
        com.example.test2.data.local.entity.FrequencyType.MONTHLY -> {
            val days = habit.getFrequencyDaysList()
            if (days.isEmpty()) "每月" else "每月${days.size}天"
        }
        com.example.test2.data.local.entity.FrequencyType.CUSTOM -> "每${habit.frequencyCount}天"
    }
} 