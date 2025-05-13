package com.example.test2.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

/**
 * 成功提示对话框
 *
 * @param message 提示消息
 * @param onDismiss 关闭回调
 * @param animationRes 动画资源ID
 */
@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit,
    animationRes: String = "success.json"
) {
    var showDialog by remember { mutableStateOf(true) }
    
    // 自动关闭
    LaunchedEffect(showDialog) {
        if (showDialog) {
            delay(1500)
            showDialog = false
            onDismiss()
        }
    }
    
    if (showDialog) {
        Dialog(onDismissRequest = {
            showDialog = false
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lottie动画
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.Asset(animationRes)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = 1
                    )
                    
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 提示消息
                    Text(
                        text = message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
} 