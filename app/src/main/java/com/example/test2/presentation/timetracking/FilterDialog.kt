package com.example.test2.presentation.timetracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.test2.data.model.TimeCategory
import com.example.test2.presentation.timetracking.TimeTrackingUtils.getCategoryColor
import com.example.test2.presentation.timetracking.TimeTrackingUtils.getCategoryName

/**
 * 筛选对话框
 */
@Composable
fun FilterDialog(
    selectedCategory: TimeCategory?,
    onDismiss: () -> Unit,
    onFilter: (TimeCategory?) -> Unit
) {
    var currentCategory by remember { mutableStateOf(selectedCategory) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "筛选时间条目",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 分类筛选
                Text(
                    text = "按分类筛选",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // 显示所有分类选项
                CategoryFilterOptions(
                    selectedCategory = currentCategory,
                    onSelectCategory = { category -> 
                        currentCategory = category
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { 
                            currentCategory = null
                            onFilter(null)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除筛选",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "清除筛选",
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    
                    Button(
                        onClick = { onFilter(currentCategory) }
                    ) {
                        Text("应用筛选")
                    }
                }
            }
        }
    }
}

/**
 * 分类筛选选项
 */
@Composable
private fun CategoryFilterOptions(
    selectedCategory: TimeCategory?,
    onSelectCategory: (TimeCategory?) -> Unit
) {
    TimeCategory.values().forEach { category ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCategory(category) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) }
            )
            
            // 分类颜色指示器
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(TimeTrackingUtils.getCategoryColor(category), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            
            Text(
                text = TimeTrackingUtils.getCategoryName(category),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
} 