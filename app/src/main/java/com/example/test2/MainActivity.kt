package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.test2.data.local.prefs.PreferencesHelper
import com.example.test2.presentation.navigation.AppNavigationGraph
import com.example.test2.ui.theme.Test2Theme
import com.example.test2.ui.theme.ThemeDataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themeDataStore: ThemeDataStore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 使用ThemeDataStore中的isDarkTheme状态
            val isDarkTheme by themeDataStore.isDarkTheme
            
            Test2Theme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 使用重构后的单一导航系统
                    AppNavigationGraph(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}