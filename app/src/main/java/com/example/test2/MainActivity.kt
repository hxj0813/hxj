package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.presentation.common.components.BottomNavigationBar
import com.example.test2.presentation.common.navigation.BottomNavItem
import com.example.test2.presentation.goals.GoalsScreen
import com.example.test2.presentation.habits.HabitScreen
import com.example.test2.presentation.habits.NotesScreen
import com.example.test2.presentation.tasks.TasksScreen
import com.example.test2.presentation.timetracking.TimeTrackingScreen
import com.example.test2.ui.theme.Test2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

/**
 * 应用主界面组合函数
 */
@Composable
fun MainApp() {
    val navController = rememberNavController()
    // 底部导航栏可见性状态
    val bottomBarVisible = remember { mutableStateOf(true) }
    // 底部导航项列表
    val bottomNavItems = listOf(
        BottomNavItem.HABITS,
        BottomNavItem.TASKS,
        BottomNavItem.GOALS,
        BottomNavItem.TIME_TRACKING,
        BottomNavItem.NOTES
    )
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems,
                isVisible = bottomBarVisible
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = BottomNavItem.HABITS.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 习惯模块
            composable(BottomNavItem.HABITS.route) {
                HabitScreen(
                    onNavigateToNotes = {
                        navController.navigate("notes")
                    },
                    onNavigateToHabitNotes = { habitId ->
                        navController.navigate("notes/$habitId")
                    }
                )
            }
            
            // 任务模块
            composable(BottomNavItem.TASKS.route) {
                TasksScreen()
            }
            
            // 目标模块
            composable(BottomNavItem.GOALS.route) {
                GoalsScreen()
            }
            
            // 时间追踪模块
            composable(BottomNavItem.TIME_TRACKING.route) {
                TimeTrackingScreen()
            }
            
            // 笔记模块
            composable(BottomNavItem.NOTES.route) {
                NotesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 习惯相关笔记详情
            composable(
                route = "notes/{habitId}",
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                // 进入详情页时隐藏底部导航栏
                bottomBarVisible.value = false
                
                val habitId = backStackEntry.arguments?.getString("habitId")
                NotesScreen(
                    habitId = habitId,
                    onNavigateBack = {
                        // 返回时显示底部导航栏
                        bottomBarVisible.value = true
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}