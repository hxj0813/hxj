package com.example.test2.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// 暂时移除未实现的屏幕引用
// import com.example.test2.presentation.home.HomeScreen
// import com.example.test2.presentation.mine.MineScreen
// import com.example.test2.presentation.note.NoteScreen
import com.example.test2.presentation.habits.HabitsScreen
import com.example.test2.presentation.habits.NotesScreen
import com.example.test2.presentation.habits.BadgeScreen
import com.example.test2.presentation.habits.HabitDetailScreen
import com.example.test2.presentation.goals.GoalsScreen
import com.example.test2.presentation.tasks.TaskDetailScreen
import com.example.test2.presentation.tasks.TaskStatisticsScreen
import com.example.test2.presentation.tasks.TasksScreen
import com.example.test2.presentation.timetracking.PomodoroSessionScreen
import com.example.test2.presentation.timetracking.TimeTrackingScreen

/**
 * 应用导航路由
 */
sealed class NavRoute(val route: String) {
    // 主要页面路由
    object Home : NavRoute("home")
    object Tasks : NavRoute("tasks?goalId={goalId}") {
        fun createRoute(goalId: Long? = null): String {
            return if (goalId != null) "tasks?goalId=$goalId" else "tasks"
        }
    }
    object TimeTracking : NavRoute("time_tracking")
    object Goals : NavRoute("goals")
    object Habits : NavRoute("habits")
    object Reflect : NavRoute("reflect")
    object Note : NavRoute("note")
    object Mine : NavRoute("mine")
    
    // 任务相关子路由
    object TaskDetail : NavRoute("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object PomodoroSession : NavRoute("pomodoro_session/{taskId}") {
        fun createRoute(taskId: String) = "pomodoro_session/$taskId"
    }
    object TaskStatistics : NavRoute("task_statistics")
    
    // 时间追踪子路由
    object TimeEntryDetail : NavRoute("time_entry_detail/{entryId}") {
        fun createRoute(entryId: Long) = "time_entry_detail/$entryId"
    }
    
    // 目标相关子路由
    object GoalDetail : NavRoute("goal_detail/{goalId}") {
        fun createRoute(goalId: String) = "goal_detail/$goalId"
    }
    
    // 习惯相关子路由
    object HabitDetail : NavRoute("habit_detail/{habitId}") {
        fun createRoute(habitId: String) = "habit_detail/$habitId"
    }
    
    // 徽章相关子路由
    object Badges : NavRoute("badges") {
        fun createRoute() = "badges"
    }
    
    // 反思笔记子路由
    object ReflectDetail : NavRoute("reflect_detail/{reflectId}") {
        fun createRoute(reflectId: String) = "reflect_detail/$reflectId"
    }
}

/**
 * 底部导航项目
 */
val bottomNavItems = listOf(
    NavRoute.Tasks,
    NavRoute.Habits,
    NavRoute.Goals,
    NavRoute.TimeTracking,
    NavRoute.Reflect
    // 暂时移除未完全实现的导航项
    // NavRoute.Home,
    // NavRoute.Note,
    // NavRoute.Mine
)

/**
 * 应用导航图
 */
@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Tasks.route // 改为已实现的起始页
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                // 避免构建相同目标的多个副本
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { 
                            when (screen) {
                                NavRoute.Tasks -> Icon(Icons.Default.List, contentDescription = "任务")
                                NavRoute.TimeTracking -> Icon(Icons.Default.Timer, contentDescription = "时间")
                                NavRoute.Goals -> Icon(Icons.Default.Star, contentDescription = "目标")
                                NavRoute.Habits -> Icon(Icons.Default.Loop, contentDescription = "习惯")
                                NavRoute.Reflect -> Icon(Icons.Default.Book, contentDescription = "反思")
                                // 暂时移除未实现的图标
                                // NavRoute.Home -> Icon(Icons.Default.Home, contentDescription = "首页")
                                // NavRoute.Note -> Icon(Icons.Default.Note, contentDescription = "笔记")
                                // NavRoute.Mine -> Icon(Icons.Default.Person, contentDescription = "我的")
                                else -> {}
                            }
                        },
                        label = { 
                            when (screen) {
                                NavRoute.Tasks -> Text("任务")
                                NavRoute.TimeTracking -> Text("时间")
                                NavRoute.Goals -> Text("目标")
                                NavRoute.Habits -> Text("习惯")
                                NavRoute.Reflect -> Text("反思")
                                // 暂时移除未实现的标签
                                // NavRoute.Home -> Text("首页")
                                // NavRoute.Note -> Text("笔记")
                                // NavRoute.Mine -> Text("我的")
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(paddingValues)
        ) {
            // 暂时注释掉未实现的页面
            // 主页
            // composable(route = NavRoute.Home.route) {
            //     HomeScreen(navController = navController)
            // }
            
            // 任务列表
            composable(
                route = NavRoute.Tasks.route,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.LongType
                        defaultValue = -1L
                        nullable = false
                    }
                )
            ) { entry ->
                // 获取goalId
                val rawGoalId = entry.arguments?.getLong("goalId") ?: -1L
                
                // 使用NavBackStackEntry的savedStateHandle属性
                val processedKey = "processed_goal_id_$rawGoalId"
                val alreadyProcessed = remember { entry.savedStateHandle.contains(processedKey) }
                val effectiveGoalId = remember {
                    if (alreadyProcessed) {
                        -1L // 已经处理过这个goalId，返回默认值
                    } else if (rawGoalId > 0) {
                        // 标记为已处理
                        entry.savedStateHandle[processedKey] = true
                        rawGoalId
                    } else {
                        -1L
                    }
                }
                
                TasksScreen(
                    onNavigateToDetail = { taskId ->
                        navController.navigate(NavRoute.TaskDetail.createRoute(taskId))
                    },
                    onNavigateToStatistics = {
                        navController.navigate(NavRoute.TaskStatistics.route)
                    },
                    initialGoalId = if (effectiveGoalId > 0) effectiveGoalId else null,
                    onTaskCreated = {
                        // 当任务创建完成时，清除已处理的goalId
                        if (rawGoalId > 0) {
                            // 重置状态
                            entry.savedStateHandle.remove<Boolean>(processedKey)
                        }
                    }
                )
            }
            
            // 任务详情
            composable(
                route = NavRoute.TaskDetail.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val taskId = entry.arguments?.getString("taskId") ?: ""
                
                TaskDetailScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEdit = { task ->
                        navController.popBackStack()
                        // 返回后打开编辑对话框
                    },
                    onStart = { taskId ->
                        navController.navigate(NavRoute.PomodoroSession.createRoute(taskId.toString()))
                    },
                    navBackStackEntry = entry
                )
            }
            
            // 番茄钟会话
            composable(
                route = NavRoute.PomodoroSession.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val taskId = entry.arguments?.getString("taskId") ?: ""
                
                PomodoroSessionScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onFinish = {
                        // 返回并携带成功标记
                        navController.previousBackStackEntry?.savedStateHandle?.set("showPomodoroSuccessMessage", true)
                        navController.popBackStack()
                    }
                )
            }
            
            // 任务统计
            composable(route = NavRoute.TaskStatistics.route) {
                TaskStatisticsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 时间追踪
            composable(route = NavRoute.TimeTracking.route) {
                TimeTrackingScreen(
                    navController = navController
                )
            }
            
            // 习惯养成
            composable(route = NavRoute.Habits.route) {
                HabitsScreen(
                    onNavigateToDetail = { habitId ->
                        navController.navigate(NavRoute.HabitDetail.createRoute(habitId))
                    },
                    onNavigateToBadges = {
                        navController.navigate(NavRoute.Badges.createRoute())
                    }
                )
            }
            
            // 习惯详情
            composable(
                route = NavRoute.HabitDetail.route,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val habitId = entry.arguments?.getString("habitId") ?: ""
                
                HabitDetailScreen(
                    habitId = habitId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditHabit = { habitId ->
                        // 返回到习惯列表并带着编辑标记
                        navController.previousBackStackEntry?.savedStateHandle?.set("habitToEdit", habitId)
                        navController.popBackStack()
                    }
                )
            }
            
            // 徽章收藏页面
            composable(route = NavRoute.Badges.route) {
                BadgeScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 目标管理
            composable(route = NavRoute.Goals.route) {
                GoalsScreen(navController = navController)
            }
            
            // 反思笔记（暂时使用NotesScreen）
            composable(route = NavRoute.Reflect.route) {
                NotesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 暂时注释掉未实现的页面
            // 笔记
            // composable(route = NavRoute.Note.route) {
            //     NoteScreen(navController = navController)
            // }
            
            // 我的
            // composable(route = NavRoute.Mine.route) {
            //     MineScreen(navController = navController)
            // }
            
            // TODO: 添加时间追踪详情页面
            // TODO: 添加目标详情页面
            // TODO: 添加其他必要的页面
        }
    }
} 