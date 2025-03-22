package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.presentation.goals.GoalsScreen
import com.example.test2.presentation.goals.GoalsViewModel
import com.example.test2.ui.theme.Test2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 显示目标管理模块界面
                    GoalsScreen(
                        viewModel = viewModel()
                    )

                }
            }
        }
    }
}

// 保留这些预览函数以便于开发时预览
@Preview(showBackground = true)
@Composable
fun GoalsScreenPreview() {
    Test2Theme {
        GoalsScreen(
            viewModel = viewModel()
        )
    }
}