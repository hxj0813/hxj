package com.example.test2.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.util.PasswordValidator
import kotlinx.coroutines.delay

/**
 * 登录界面
 * 
 * @param onLoginSuccess 登录成功回调
 * @param onNavigateToRegister 导航到注册页面
 * @param onNavigateToForgotPassword 导航到忘记密码页面
 * @param viewModel 认证视图模型
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // 密码验证状态
    val passwordValidation by remember(password) {
        derivedStateOf { PasswordValidator.validatePassword(password) }
    }
    
    // 显示密码要求
    var showPasswordRequirements by remember { mutableStateOf(false) }
    
    // 网络错误相关状态
    var showNetworkErrorDialog by remember { mutableStateOf(false) }
    var networkErrorMessage by remember { mutableStateOf("") }
    var isRetrying by remember { mutableStateOf(false) }
    
    // 检测网络错误
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            if (error.contains("SSL") || error.contains("网络") || error.contains("连接")) {
                networkErrorMessage = error
                showNetworkErrorDialog = true
            }
        }
    }
    
    // 自动重试指示器
    LaunchedEffect(isRetrying) {
        if (isRetrying) {
            delay(3000) // 3秒后自动重试
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            }
            isRetrying = false
        }
    }
    
    // 登录成功时的处理
    LaunchedEffect(authState.isLoginSuccess) {
        if (authState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.resetSuccessState()
        }
    }
    
    // 重置错误信息
    LaunchedEffect(email, password) {
        viewModel.resetError()
    }
    
    // 网络错误对话框
    if (showNetworkErrorDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkErrorDialog = false },
            title = { Text("网络连接问题") },
            text = { 
                Column {
                    Text(networkErrorMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("建议: 请检查网络连接，切换到更稳定的网络，或稍后重试。")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showNetworkErrorDialog = false
                        isRetrying = true
                    }
                ) {
                    Text("重试")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNetworkErrorDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 应用标题
            Text(
                text = "自我管理应用",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "登录账号",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 错误提示 (只显示非网络错误)
            AnimatedVisibility(visible = authState.error != null && 
                                      !(authState.error?.contains("SSL") == true || 
                                        authState.error?.contains("网络") == true || 
                                        authState.error?.contains("连接") == true)) {
                authState.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
            
            // 自动重试指示器
            AnimatedVisibility(visible = isRetrying) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在重新连接...",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 邮箱输入框
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱地址") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "邮箱图标"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isError = authState.error?.contains("邮箱") == true
            )
            
            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    if (!showPasswordRequirements && password.isNotEmpty()) {
                        showPasswordRequirements = true
                    }
                },
                label = { Text("密码") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "密码图标"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        }
                    }
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isError = authState.error?.contains("密码") == true
            )
            
            // 密码要求提示
            AnimatedVisibility(visible = showPasswordRequirements && !passwordValidation.isValid) {
                Text(
                    text = passwordValidation.errorMessage ?: PasswordValidator.getPasswordRequirements(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
            
            // 忘记密码
            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 4.dp)
            ) {
                Text("忘记密码?")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 登录按钮
            Button(
                onClick = { viewModel.login(email, password) },
                enabled = !authState.isLoading && !isRetrying && 
                         email.isNotBlank() && password.isNotBlank() && 
                         passwordValidation.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("登录")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 注册入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("还没有账号?")
                TextButton(onClick = onNavigateToRegister) {
                    Text("立即注册")
                }
            }
        }
    }
} 