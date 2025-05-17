package com.example.test2.presentation.auth

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraphBuilder

/**
 * 认证导航路由
 */
sealed class AuthRoute(val route: String) {
    /**
     * 登录页面
     */
    object Login : AuthRoute("login")
    
    /**
     * 注册页面
     */
    object Register : AuthRoute("register")
    
    /**
     * 忘记密码页面
     */
    object ForgotPassword : AuthRoute("forgot_password")
}

/**
 * 认证导航图
 * @param navController 导航控制器
 * @param onAuthSuccess 认证成功回调
 */
@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoute.Login.route
    ) {
        // 登录页面
        composable(AuthRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AuthRoute.ForgotPassword.route)
                }
            )
        }
        
        // 注册页面
        composable(AuthRoute.Register.route) {
            RegisterScreen(
                onRegistrationSuccess = onAuthSuccess,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 忘记密码页面
        composable(AuthRoute.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 添加认证导航图
 * @param onAuthSuccess 认证成功回调
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    // 登录页面
    composable(AuthRoute.Login.route) {
        LoginScreen(
            onLoginSuccess = onAuthSuccess,
            onNavigateToRegister = {
                navController.navigate(AuthRoute.Register.route)
            },
            onNavigateToForgotPassword = {
                navController.navigate(AuthRoute.ForgotPassword.route)
            }
        )
    }
    
    // 注册页面
    composable(AuthRoute.Register.route) {
        RegisterScreen(
            onRegistrationSuccess = onAuthSuccess,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
    
    // 忘记密码页面
    composable(AuthRoute.ForgotPassword.route) {
        ForgotPasswordScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
} 