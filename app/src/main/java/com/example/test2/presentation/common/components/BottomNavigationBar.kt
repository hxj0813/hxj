package com.example.test2.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.test2.presentation.common.navigation.BottomNavItem

/**
 * 底部导航栏组件
 * 
 * @param navController 导航控制器
 * @param items 导航项列表
 * @param isVisible 控制导航栏可见性的状态
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>,
    isVisible: MutableState<Boolean>
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    
    // 使用动画来显示/隐藏底部导航栏
    AnimatedVisibility(
        visible = isVisible.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 8.dp,
            modifier = Modifier.height(60.dp)
        ) {
            items.forEach { item ->
                // 确定当前项是否被选中
                val selected = currentRoute == item.route
                
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    },
                    selected = selected,
                    onClick = {
                        // 避免重复导航到当前位置
                        if (currentRoute != item.route) {
                            // 导航到选中的项，清除之前的后退栈
                            navController.navigate(item.route) {
                                // 避免创建重复的后退栈
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // 恢复状态并避免重复导航到同一项
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        }
    }
} 