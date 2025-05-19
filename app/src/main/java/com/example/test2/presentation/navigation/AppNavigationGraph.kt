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
 * @param startDestination 起始目的地（现在默认为主应用，允许不登录直接使用）
 */
@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    startDestination: String = AppDestination.Main.route // 默认进入主应用，不要求登录
) {
    val authState by authViewModel.authState.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                onLoginRequest = {
                    // 当用户希望登录时导航到认证界面
                    navController.navigate(AppDestination.Auth.route)
                },
                onLogout = {
                    authViewModel.logout()
                    // 退出登录后仍然留在主应用，而不是回到登录界面
                }
            )
        }
    }
} 