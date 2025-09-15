package com.travellight.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import com.travellight.camera.R
import com.travellight.camera.data.model.LightType

/**
 * 相机预览组件
 */
@Composable
fun CameraPreview(
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
    )
}

/**
 * 补光类型选择器
 */
@Composable
fun LightTypeSelector(
    selectedType: LightType,
    onTypeSelected: (LightType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LightType.values().forEach { type ->
            LightTypeItem(
                lightType = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

/**
 * 补光类型选项
 */
@Composable
fun LightTypeItem(
    lightType: LightType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = getLightTypeIcon(lightType),
            contentDescription = getLightTypeName(lightType),
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = getLightTypeName(lightType),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 亮度控制滑块
 */
@Composable
fun BrightnessSlider(
    brightness: Float,
    onBrightnessChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.brightness),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = stringResource(R.string.brightness),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${brightness.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = brightness,
                onValueChange = onBrightnessChanged,
                valueRange = 0f..100f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 拍照按钮
 */
@Composable
fun CaptureButton(
    onClick: () -> Unit,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isEnabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isEnabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(buttonColor)
            .border(
                width = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.take_photo),
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * 功能按钮
 */
@Composable
fun FeatureButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = modifier
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * 补光开关
 */
@Composable
fun LightSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.Star else Icons.Default.Add,
                    contentDescription = stringResource(R.string.light_switch),
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = stringResource(R.string.light_switch),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (isEnabled) {
                            stringResource(R.string.light_on)
                        } else {
                            stringResource(R.string.light_off)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * 状态指示器
 */
@Composable
fun StatusIndicator(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 获取补光类型图标
 */
fun getLightTypeIcon(lightType: LightType): ImageVector {
    return when (lightType) {
        LightType.WARM -> Icons.Default.Star
        LightType.COOL -> Icons.Default.Add
        LightType.NATURAL -> Icons.Default.Settings
        LightType.SOFT -> Icons.Default.ArrowBack
    }
}

/**
 * 获取补光类型名称
 */
fun getLightTypeName(lightType: LightType): String {
    return when (lightType) {
        LightType.WARM -> "暖光"
        LightType.COOL -> "冷光"
        LightType.NATURAL -> "自然光"
        LightType.SOFT -> "柔光"
    }
}