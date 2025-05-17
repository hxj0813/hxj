package com.example.test2.presentation.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.entity.BadgeCategory
import com.example.test2.data.local.entity.BadgeEntity
import com.example.test2.data.local.entity.UserBadgeEntity
import com.example.test2.presentation.habits.components.BadgeCollection
import com.example.test2.presentation.habits.components.BadgeDialog
import com.example.test2.presentation.habits.viewmodel.BadgeViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**
 * 徽章收藏页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // 收集状态
    val badgesState by viewModel.badgesState.collectAsState()
    val userBadgesState by viewModel.userBadgesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var selectedBadge by remember { mutableStateOf<BadgeEntity?>(null) }
    var selectedUserBadge by remember { mutableStateOf<UserBadgeEntity?>(null) }
    
    // 初始化和加载数据
    LaunchedEffect(Unit) {
        viewModel.initializeBadges()
        viewModel.loadAllBadges()
    }
    
    // 将徽章与用户徽章配对
    val pairedBadges = remember(badgesState, userBadgesState) {
        badgesState.map { badge ->
            badge to userBadgesState.find { it.badgeId == badge.id }
        }
    }
    
    // 按类别筛选徽章
    var selectedTabIndex by remember { mutableStateOf(0) }
    val badgesByCategory = remember(pairedBadges, selectedTabIndex) {
        if (selectedTabIndex == 0) {
            // "全部"标签显示所有徽章
            pairedBadges
        } else {
            // 其他标签按类别筛选
            val category = BadgeCategory.values()[selectedTabIndex - 1]
            pairedBadges.filter { (badge, _) -> badge.getCategoryEnum() == category }
        }
    }
    
    // 已解锁和未解锁的徽章
    val unlockedBadges = remember(pairedBadges) {
        pairedBadges.filter { (_, userBadge) -> userBadge != null }
    }
    
    val lockedBadges = remember(pairedBadges) {
        pairedBadges.filter { (_, userBadge) -> userBadge == null }
        .filter { (badge, _) -> !badge.isSecret } // 不显示隐藏徽章
    }
    
    // 页面状态
    val pagerState = rememberPagerState(initialPage = 0)
    val pages = listOf("已解锁", "未解锁")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("徽章收藏") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (viewModel.newBadges.collectAsState().value.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllNewBadges() }) {
                            Badge {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "新徽章通知"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计卡片
            BadgeStatsCard(
                totalBadges = badgesState.size,
                unlockedBadges = userBadgesState.size,
                progress = if (badgesState.isNotEmpty()) 
                    userBadgesState.size.toFloat() / badgesState.size 
                else 0f
            )
            
            // 类别选择选项卡
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                divider = { Divider(thickness = 2.dp) }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("全部") }
                )
                
                BadgeCategory.values().forEachIndexed { index, category ->
                    Tab(
                        selected = selectedTabIndex == index + 1,
                        onClick = { selectedTabIndex = index + 1 },
                        text = { Text(getCategoryName(category)) },
                        icon = { Icon(imageVector = getCategoryIcon(category), contentDescription = null) }
                    )
                }
            }
            
            // 已解锁/未解锁切换选项卡
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = { Divider(thickness = 2.dp) }
            ) {
                pages.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { /* 由HorizontalPager处理 */ },
                        text = { Text(title) }
                    )
                }
            }
            
            // 主内容区域
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        // 已解锁徽章页
                        if (unlockedBadges.isEmpty()) {
                            EmptyBadgesView(
                                title = "暂无已解锁徽章",
                                message = "继续坚持完成习惯，解锁更多徽章！"
                            )
                        } else {
                            BadgeCollection(
                                badges = if (selectedTabIndex == 0) unlockedBadges else badgesByCategory.filter { (_, userBadge) -> userBadge != null },
                                onBadgeClick = { badge, userBadge ->
                                    selectedBadge = badge
                                    selectedUserBadge = userBadge
                                }
                            )
                        }
                    }
                    1 -> {
                        // 未解锁徽章页
                        if (lockedBadges.isEmpty()) {
                            EmptyBadgesView(
                                title = "恭喜！",
                                message = "你已经解锁了所有可见徽章"
                            )
                        } else {
                            BadgeCollection(
                                badges = if (selectedTabIndex == 0) lockedBadges else badgesByCategory.filter { (_, userBadge) -> userBadge == null },
                                onBadgeClick = { badge, _ ->
                                    // 未解锁徽章只显示基本信息
                                    selectedBadge = badge
                                    selectedUserBadge = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 徽章详情对话框
    if (selectedBadge != null) {
        BadgeDetailDialog(
            badge = selectedBadge!!,
            userBadge = selectedUserBadge,
            onDismiss = {
                selectedBadge = null
                selectedUserBadge = null
            }
        )
    }
    
    // 加载状态
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // 错误提示
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 显示错误，实际项目中可使用Snackbar
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }
}

/**
 * 徽章统计卡片
 */
@Composable
private fun BadgeStatsCard(
    totalBadges: Int,
    unlockedBadges: Int,
    progress: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "徽章收藏进度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$unlockedBadges / $totalBadges",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${(progress * 100).toInt()}% 完成",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空徽章视图
 */
@Composable
private fun EmptyBadgesView(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 徽章详情对话框
 */
@Composable
private fun BadgeDetailDialog(
    badge: BadgeEntity,
    userBadge: UserBadgeEntity?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(badge.name) },
        text = {
            Column {
                val badgeColor = Color(badge.getColorByRarity())
                
                // 徽章图标和稀有度
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 稀有度指示器
                    Text(
                        text = getRarityText(badge),
                        style = MaterialTheme.typography.bodySmall,
                        color = badgeColor
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 徽章类别
                    Text(
                        text = getCategoryName(badge.getCategoryEnum()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 徽章描述
                Text(text = badge.description)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 解锁条件（如果未解锁）
                if (userBadge == null && !badge.isSecret) {
                    Text(
                        text = "解锁条件: ${badge.condition}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 解锁信息（如果已解锁）
                userBadge?.let {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "解锁时间: ${java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", 
                            java.util.Locale.getDefault()).format(it.unlockedAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // 解锁时的值（如果有）
                    it.valueWhenUnlocked?.let { value ->
                        Text(
                            text = "解锁值: $value",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // 解锁备注（如果有）
                    it.note?.let { note ->
                        Text(
                            text = "备注: $note",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 获取徽章类别名称
 */
@Composable
private fun getCategoryName(category: BadgeCategory): String {
    return when (category) {
        BadgeCategory.STREAK -> "连续打卡"
        BadgeCategory.COMPLETION -> "累计完成"
        BadgeCategory.VARIETY -> "多样性"
        BadgeCategory.ACHIEVEMENT -> "特殊成就"
        BadgeCategory.EVENT -> "活动徽章"
    }
}

/**
 * 获取徽章类别图标
 */
@Composable
private fun getCategoryIcon(category: BadgeCategory) = when (category) {
    BadgeCategory.STREAK -> Icons.Default.Whatshot
    BadgeCategory.COMPLETION -> Icons.Default.CheckCircle
    BadgeCategory.VARIETY -> Icons.Default.Category
    BadgeCategory.ACHIEVEMENT -> Icons.Default.EmojiEvents
    BadgeCategory.EVENT -> Icons.Default.Celebration
}

/**
 * 获取徽章稀有度文本
 */
@Composable
private fun getRarityText(badge: BadgeEntity): String {
    return when (badge.getRarityEnum()) {
        com.example.test2.data.local.entity.BadgeRarity.COMMON -> "普通"
        com.example.test2.data.local.entity.BadgeRarity.UNCOMMON -> "不常见"
        com.example.test2.data.local.entity.BadgeRarity.RARE -> "稀有"
        com.example.test2.data.local.entity.BadgeRarity.EPIC -> "史诗"
        com.example.test2.data.local.entity.BadgeRarity.LEGENDARY -> "传说"
    }
} 