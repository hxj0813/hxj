package com.example.test2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test2.presentation.auth.AuthNavigation
import com.example.test2.presentation.auth.AuthViewModel
import com.example.test2.SelfManagementApp

/**
 * 应用主导航节点
 */
sealed class AppDestination(val route: String) {
    /**
     * 认证流程（登录/注册）
     */
    object Auth : AppDestination("auth")
    
    /**
     * 主应用
     */
    object Main : AppDestination("main")
}

/**
 * 应用导航图
 * 处理应用主要导航逻辑，包括认证和主应用流程
 * 
 * @param modifier Modifier修饰符
 * @param navController 导航控制器
 * @param authViewModel 认证视图模型
 * @param startDestination 起始目的地
 */
@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    startDestination: String = AppDestination.Auth.route
) {
    val authState by authViewModel.authState.collectAsState()
    
    // 根据登录状态决定起始路由
    val currentStartDestination = if (authState.user != null) {
        AppDestination.Main.route
    } else {
        startDestination
    }
    
    NavHost(
        navController = navController,
        startDestination = currentStartDestination,
        modifier = modifier
    ) {
        // 嵌套认证导航图
        composable(AppDestination.Auth.route) {
            AuthNavigation(
                onAuthSuccess = {
                    navController.navigate(AppDestination.Main.route) {
                        popUpTo(AppDestination.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 主应用
        composable(AppDestination.Main.route) {
            SelfManagementApp(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppDestination.Auth.route) {
                        popUpTo(AppDestination.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
} 