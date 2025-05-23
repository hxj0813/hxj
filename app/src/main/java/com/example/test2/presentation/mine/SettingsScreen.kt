package com.example.test2.presentation.mine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test2.data.local.prefs.ThemeMode
import com.example.test2.presentation.mine.viewmodel.SettingsViewModel

/**
 * 设置页面
 * 
 * @param onNavigateBack 返回回调
 * @param viewModel 设置ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var selectedThemeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
    
    // 初始化主题模式
    LaunchedEffect(Unit) {
        selectedThemeMode = viewModel.getThemeMode()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    // 主题模式选项
                    ListItem(
                        headlineContent = { Text("主题设置") },
                        supportingContent = { Text("选择应用的外观主题") },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Outlined.DarkMode,
                                contentDescription = "主题设置"
                            )
                        }
                    )
                    
                    Divider()
                    
                    // 主题选项单选按钮
                    Column(
                        modifier = Modifier
                            .selectableGroup()
                            .padding(16.dp)
                    ) {
                        ThemeMode.values().forEach { themeMode ->
                            val isSelected = selectedThemeMode == themeMode
                            val themeName = when(themeMode) {
                                ThemeMode.LIGHT -> "浅色"
                                ThemeMode.DARK -> "深色"
                            }
                            
                            ListItem(
                                headlineContent = { Text(themeName) },
                                leadingContent = {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null
                                    )
                                },
                                modifier = Modifier
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            selectedThemeMode = themeMode
                                            viewModel.setThemeMode(themeMode)
                                        }
                                    )
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 