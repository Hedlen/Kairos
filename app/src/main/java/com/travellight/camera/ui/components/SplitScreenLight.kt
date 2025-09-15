package com.travellight.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.travellight.camera.R
import com.travellight.camera.data.model.LightType
import com.travellight.camera.ui.theme.*

/**
 * 分屏模式枚举
 */
enum class SplitMode {
    SINGLE,     // 单屏模式
    VERTICAL,   // 垂直分屏
    HORIZONTAL  // 水平分屏
}

/**
 * 分屏补光数据类
 */
data class SplitLightConfig(
    val mode: SplitMode = SplitMode.SINGLE,
    val leftTopType: LightType = LightType.NATURAL,
    val rightBottomType: LightType = LightType.WARM,
    val leftTopBrightness: Float = 0.8f,
    val rightBottomBrightness: Float = 0.8f,
    val isEnabled: Boolean = false
)

/**
 * 分屏补光组件
 */
@Composable
fun SplitScreenLight(
    config: SplitLightConfig,
    onConfigChange: (SplitLightConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和模式切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分屏补光",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // 模式切换按钮
                Row {
                    IconButton(
                        onClick = {
                            onConfigChange(config.copy(mode = SplitMode.SINGLE))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "单屏模式",
                            tint = if (config.mode == SplitMode.SINGLE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            onConfigChange(config.copy(mode = SplitMode.VERTICAL))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "垂直分屏",
                            tint = if (config.mode == SplitMode.VERTICAL) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 分屏预览
            SplitScreenPreview(
                config = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            
            if (config.mode != SplitMode.SINGLE) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分屏控制
                SplitScreenControls(
                    config = config,
                    onConfigChange = onConfigChange
                )
            }
        }
    }
}

/**
 * 分屏预览组件
 */
@Composable
fun SplitScreenPreview(
    config: SplitLightConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
    ) {
        when (config.mode) {
            SplitMode.SINGLE -> {
                // 单屏模式
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (config.isEnabled) {
                                getLightColor(config.leftTopType).copy(
                                    alpha = config.leftTopBrightness
                                )
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "单屏模式",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            SplitMode.VERTICAL -> {
                // 垂直分屏
                Row(modifier = Modifier.fillMaxSize()) {
                    // 左侧
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (config.isEnabled) {
                                    getLightColor(config.leftTopType).copy(
                                        alpha = config.leftTopBrightness
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "左侧",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // 分割线
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                    )
                    
                    // 右侧
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (config.isEnabled) {
                                    getLightColor(config.rightBottomType).copy(
                                        alpha = config.rightBottomBrightness
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "右侧",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            SplitMode.HORIZONTAL -> {
                // 水平分屏
                Column(modifier = Modifier.fillMaxSize()) {
                    // 上侧
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                if (config.isEnabled) {
                                    getLightColor(config.leftTopType).copy(
                                        alpha = config.leftTopBrightness
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "上侧",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // 分割线
                    HorizontalDivider()
                    
                    // 下侧
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                if (config.isEnabled) {
                                    getLightColor(config.rightBottomType).copy(
                                        alpha = config.rightBottomBrightness
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "下侧",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 分屏控制组件
 */
@Composable
fun SplitScreenControls(
    config: SplitLightConfig,
    onConfigChange: (SplitLightConfig) -> Unit
) {
    Column {
        // 左侧/上侧控制
        Text(
            text = if (config.mode == SplitMode.VERTICAL) "左侧设置" else "上侧设置",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LightTypeSelector(
            selectedType = config.leftTopType,
            onTypeSelected = { type ->
                onConfigChange(config.copy(leftTopType = type))
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SimpleBrightnessControl(
            brightness = config.leftTopBrightness,
            onBrightnessChange = { brightness ->
                onConfigChange(config.copy(leftTopBrightness = brightness))
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 右侧/下侧控制
        Text(
            text = if (config.mode == SplitMode.VERTICAL) "右侧设置" else "下侧设置",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LightTypeSelector(
            selectedType = config.rightBottomType,
            onTypeSelected = { type ->
                onConfigChange(config.copy(rightBottomType = type))
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SimpleBrightnessControl(
            brightness = config.rightBottomBrightness,
            onBrightnessChange = { brightness ->
                onConfigChange(config.copy(rightBottomBrightness = brightness))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 获取补光颜色
 */
private fun getLightColor(lightType: LightType): Color {
    return when (lightType) {
        LightType.WARM -> WarmLight
        LightType.COOL -> CoolLight
        LightType.NATURAL -> NaturalLight
        LightType.SOFT -> SoftLight
    }
}