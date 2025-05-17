package com.example.test2

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test2.data.model.Goal
import com.example.test2.domain.usecase.GoalUseCases
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * 应用程序类
 * 使用@HiltAndroidApp注解启用Hilt依赖注入
 */
@HiltAndroidApp
class SelfManagementApp : Application() {
    
    @Inject
    lateinit var goalUseCases: GoalUseCases
    
    private lateinit var preferences: SharedPreferences
    
    override fun onCreate() {
        super.onCreate()
        // 可以在这里进行全局初始化
        
        preferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        // 检查是否是首次启动
        if (isFirstLaunch()) {
            initializeSampleData()
        }
    }
    
    /**
     * 判断是否是首次启动
     */
    private fun isFirstLaunch(): Boolean {
        val isFirstLaunch = preferences.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            preferences.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirstLaunch
    }
    
    /**
     * 初始化示例数据
     */
    private fun initializeSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            // 创建一些示例目标
            val sampleGoals = listOf(
                Goal(
                    title = "学习Kotlin和Jetpack Compose",
                    description = "掌握Kotlin编程语言和Jetpack Compose UI框架，能够熟练开发Android应用",
                    isLongTerm = true,
                    isImportant = true,
                    progress = 0.6f,
                    deadline = Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000)
                ),
                Goal(
                    title = "坚持每日运动",
                    description = "每天保持30分钟以上的运动，提高身体素质和健康水平",
                    isLongTerm = true,
                    isImportant = true,
                    progress = 0.3f,
                    deadline = Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000)
                ),
                Goal(
                    title = "完成个人成长管理系统开发",
                    description = "设计并实现一个完整的个人成长管理系统，包括目标管理、任务规划、习惯形成等功能",
                    isLongTerm = false,
                    isImportant = true,
                    progress = 0.2f,
                    deadline = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000)
                ),
                Goal(
                    title = "阅读《原子习惯》",
                    description = "阅读完成《原子习惯》这本书，并做好读书笔记",
                    isLongTerm = false,
                    isImportant = false,
                    progress = 0.8f,
                    deadline = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
                ),
                Goal(
                    title = "学习设计模式",
                    description = "掌握常用的软件设计模式，并能在实际项目中应用",
                    isLongTerm = true,
                    isImportant = false,
                    progress = 1.0f,
                    deadline = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000),
                    isCompleted = true
                )
            )

            // 保存到数据库
            goalUseCases.saveGoals(sampleGoals)
        }
    }
} 

/**
 * 主应用界面
 * @param onLogout 登出回调
 */
@Composable
fun SelfManagementApp(onLogout: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    // 这里保留原来的主应用内容，并添加登出按钮
    // 仅作为示例，实际应用中应该将此按钮集成到应用工具栏或设置中
    
    // 添加登出按钮
    IconButton(
        onClick = { showMenu = true }
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "退出登录"
        )
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.padding(8.dp)
        ) {
            DropdownMenuItem(
                text = { Text("退出登录") },
                onClick = {
                    showMenu = false
                    onLogout()
                }
            )
        }
    }
    
    // 这里应该是原有的应用主界面内容
    // 省略原有代码...
} 