package com.travellight.camera.ui.screens.main

import androidx.compose.foundation.background
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travellight.camera.R
import com.travellight.camera.data.model.LightType
import com.travellight.camera.ui.theme.*
import com.travellight.camera.ui.components.LightControlPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecommendation: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // 应用标题
            Text(
                text = "旅游补光相机",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "智能引导，完美拍摄",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 智能场景分析卡片
            SceneAnalysisCard(
                modifier = Modifier.fillMaxWidth(),
                lightType = uiState.selectedLightType,
                brightness = uiState.brightness
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 补光控制面板
            LightControlPanel(
                isLightOn = uiState.isLightOn,
                selectedLightType = uiState.selectedLightType,
                brightness = uiState.brightness,
                onToggleLight = { viewModel.toggleLight() },
                onSelectLightType = { viewModel.selectLightType(it) },
                onBrightnessChanged = { viewModel.setBrightness(it) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 主拍照按钮
            MainCameraButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToCamera()
                },
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 智能引导提示
            SmartGuidanceHint()
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部功能按钮
            BottomActionBar(
                onRecommendationClick = onNavigateToRecommendation,
                onSettingsClick = onNavigateToSettings
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SceneAnalysisCard(
    modifier: Modifier = Modifier,
    lightType: LightType,
    brightness: Float
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "智能场景分析",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalysisItem(
                    icon = Icons.Default.Star,
                    label = "光线类型",
                    value = when(lightType) {
                        LightType.NATURAL -> "自然光"
                        LightType.WARM -> "暖光"
                        LightType.COOL -> "冷光"
                        LightType.SOFT -> "柔光"
                    }
                )
                
                AnalysisItem(
                    icon = Icons.Default.Add,
                    label = "亮度指数",
                    value = "${(brightness * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
fun AnalysisItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MainCameraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2E7D32)
                    )
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "拍照",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "拍照",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SmartGuidanceHint() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "点击拍照按钮获取智能拍摄建议",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun BottomActionBar(
    onRecommendationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            icon = Icons.Default.Star,
            label = "智能推荐",
            onClick = onRecommendationClick
        )
        
        ActionButton(
            icon = Icons.Default.Settings,
            label = "设置",
            onClick = onSettingsClick
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}