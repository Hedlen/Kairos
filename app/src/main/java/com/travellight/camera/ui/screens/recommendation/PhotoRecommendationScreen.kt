package com.travellight.camera.ui.screens.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travellight.camera.R
import com.travellight.camera.domain.service.AngleRecommendation
import com.travellight.camera.domain.service.PoseRecommendation
import com.travellight.camera.domain.service.LightAnalysis
import com.travellight.camera.domain.service.PhotoModeRecommendation

/**
 * 拍照推荐页面
 * 根据时间、地点、天气等因素推荐最佳拍照设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoRecommendationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotoRecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeRecommendationService()
        viewModel.startRecommendationAnalysis()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRecommendationAnalysis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.photo_recommendation),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 当前环境信息
            item {
                EnvironmentInfoCard(
                    environmentInfo = uiState.environmentInfo
                )
            }
            
            // 智能推荐卡片
            uiState.angleRecommendation?.let { angleRec ->
                item {
                    AngleRecommendationCard(
                        angleRecommendation = angleRec
                    )
                }
            }
            
            uiState.poseRecommendation?.let { poseRec ->
                item {
                    PoseRecommendationCard(
                        poseRecommendation = poseRec
                    )
                }
            }
            
            uiState.lightAnalysis?.let { lightAnalysis ->
                item {
                    LightAnalysisCard(
                        lightAnalysis = lightAnalysis
                    )
                }
            }
            
            uiState.photoModeRecommendation?.let { photoModeRec ->
                item {
                    PhotoModeRecommendationCard(
                        photoModeRecommendation = photoModeRec
                    )
                }
            }
            
            // 推荐列表
            items(uiState.recommendations) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onApplyRecommendation = { viewModel.applyRecommendation(it) }
                )
            }
            
            // 加载状态
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * 角度推荐卡片
 */
@Composable
fun AngleRecommendationCard(
    angleRecommendation: AngleRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "角度推荐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "建议角度: ${angleRecommendation.recommendedAngle}°",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "当前角度: ${String.format("%.1f", angleRecommendation.currentAngle)}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = angleRecommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 姿势推荐卡片
 */
@Composable
fun PoseRecommendationCard(
    poseRecommendation: PoseRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "姿势指导",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "推荐姿势: ${poseRecommendation.poseType.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = poseRecommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 光源分析卡片
 */
@Composable
fun LightAnalysisCard(
    lightAnalysis: LightAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "光源分析",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "亮度等级",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                         text = "${lightAnalysis.lightLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "光源方向",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = lightAnalysis.lightDirection.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "建议补光",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = lightAnalysis.recommendedLightType.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = lightAnalysis.suggestions.firstOrNull() ?: "无建议",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 拍照模式推荐卡片
 */
@Composable
fun PhotoModeRecommendationCard(
    photoModeRecommendation: PhotoModeRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "拍照模式推荐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "推荐模式: ${photoModeRecommendation.mode}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "描述: ${photoModeRecommendation.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "适用场景: ${photoModeRecommendation.suitableScenes.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/**
 * 环境信息卡片
 */
@Composable
fun EnvironmentInfoCard(
    environmentInfo: EnvironmentInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.current_environment),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnvironmentInfoItem(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.time),
                    value = environmentInfo.timeOfDay
                )
                
                EnvironmentInfoItem(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.weather),
                    value = environmentInfo.weather
                )
                
                EnvironmentInfoItem(
                    icon = Icons.Default.Star,
                    label = "光照等级",
                    value = "${environmentInfo.lightLevel}"
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnvironmentInfoItem(
                    icon = Icons.Default.Home,
                    label = "天气",
                    value = environmentInfo.weather
                )
                
                EnvironmentInfoItem(
                    icon = Icons.Default.Place,
                    label = stringResource(R.string.location),
                    value = environmentInfo.location
                )
            }
        }
    }
}

/**
 * 环境信息项
 */
@Composable
fun EnvironmentInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 推荐卡片
 */
@Composable
fun RecommendationCard(
    recommendation: PhotoRecommendation,
    onApplyRecommendation: (PhotoRecommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // 推荐度指示器
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (recommendation.priority) {
                                RecommendationPriority.HIGH -> MaterialTheme.colorScheme.primary
                                RecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                RecommendationPriority.LOW -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (recommendation.priority) {
                            RecommendationPriority.HIGH -> stringResource(R.string.high_priority)
                            RecommendationPriority.MEDIUM -> stringResource(R.string.medium_priority)
                            RecommendationPriority.LOW -> stringResource(R.string.low_priority)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 推荐设置
            Text(
                text = stringResource(R.string.recommended_settings),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "${stringResource(R.string.light_type)}: ${recommendation.lightType}\n" +
                        "${stringResource(R.string.brightness)}: ${recommendation.brightness}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onApplyRecommendation(recommendation) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.apply_recommendation))
            }
        }
    }
}