package com.example.test2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.test2.presentation.tasks.TaskDetailScreen
import com.example.test2.presentation.tasks.TaskStatisticsScreen
import com.example.test2.presentation.tasks.TasksScreen
import com.example.test2.presentation.timetracking.PomodoroSessionScreen

/**
 * 应用导航路由
 */
sealed class NavRoute(val route: String) {
    object Tasks : NavRoute("tasks")
    object TaskDetail : NavRoute("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object PomodoroSession : NavRoute("pomodoro_session/{taskId}") {
        fun createRoute(taskId: String) = "pomodoro_session/$taskId"
    }
    object TaskStatistics : NavRoute("task_statistics")
}

/**
 * 应用导航图
 */
@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Tasks.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 任务列表
        composable(route = NavRoute.Tasks.route) {
            TasksScreen(
                onNavigateToDetail = { taskId ->
                    navController.navigate(NavRoute.TaskDetail.createRoute(taskId))
                },
                onNavigateToStatistics = {
                    navController.navigate(NavRoute.TaskStatistics.route)
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
                onComplete = { taskId ->
                    // 完成任务
                    navController.popBackStack()
                },
                onDelete = { taskId ->
                    // 删除任务
                    navController.popBackStack()
                }
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
    }
} 