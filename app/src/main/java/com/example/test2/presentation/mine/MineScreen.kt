package com.example.test2.presentation.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.test2.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    navController: NavController,
    onNavigateToNotes: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // 从AuthViewModel获取登录状态
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn = authState.user != null
    
    // 注销对话框状态
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLoggedIn) {
                // 未登录状态显示登录按钮
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "登录",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("登录/注册")
                }
            }
            
            // 个人信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            if (isLoggedIn) 
                                authState.user?.displayName?.takeIf { it.isNotBlank() } ?: "用户"
                            else 
                                "游客模式"
                        ) 
                    },
                    supportingContent = { 
                        Text(
                            if (isLoggedIn) 
                                authState.user?.email ?: ""
                            else 
                                "登录后可查看个人资料"
                        ) 
                    },
                    leadingContent = { 
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "个人资料"
                        )
                    },
                    modifier = if (!isLoggedIn) Modifier.clickable(onClick = onNavigateToLogin) else Modifier
                )
            }
            
            // 功能列表
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 笔记功能
                ListItem(
                    headlineContent = { Text("我的笔记") },
                    supportingContent = { Text("查看和管理个人笔记") },
                    leadingContent = { 
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "笔记"
                        )
                    },
                    modifier = Modifier.clickable { onNavigateToNotes() }
                )
                
                // 其他设置项
                ListItem(
                    headlineContent = { Text("设置") },
                    supportingContent = { Text("应用设置和偏好") },
                    leadingContent = { 
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                )
                
                // 如果已登录，显示退出登录选项
                if (isLoggedIn) {
                    ListItem(
                        headlineContent = { Text("退出登录") },
                        supportingContent = { Text("清除当前登录状态") },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "退出登录"
                            )
                        },
                        modifier = Modifier.clickable { showLogoutDialog = true }
                    )
                }
            }
        }
        
        // 退出登录确认对话框
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("退出登录") },
                text = { Text("确定要退出当前账号吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            showLogoutDialog = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
} 