package com.travellight.camera.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.travellight.camera.R

/**
 * 亮度控制组件
 * 提供滑动条来调节补光亮度
 */
@Composable
fun BrightnessControl(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.brightness_control),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 亮度滑动条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 低亮度图标
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Low Brightness",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp)
                )
                
                // 滑动条
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    valueRange = 0.1f..1.0f,
                    steps = 9 // 10个档位
                )
                
                // 高亮度图标
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "High Brightness",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 亮度百分比显示
            Text(
                text = "${(brightness * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
    }
}

/**
 * 简化版亮度控制（仅滑动条）
 */
@Composable
fun SimpleBrightnessControl(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Low Brightness",
            tint = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (enabled) 1f else 0.38f
            ),
            modifier = Modifier.size(16.dp)
        )
        
        Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            valueRange = 0.1f..1.0f
        )
        
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "High Brightness",
            tint = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (enabled) 1f else 0.38f
            ),
            modifier = Modifier.size(16.dp)
        )
        
        Text(
            text = "${(brightness * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (enabled) 1f else 0.38f
            ),
            modifier = Modifier.width(40.dp)
        )
    }
}