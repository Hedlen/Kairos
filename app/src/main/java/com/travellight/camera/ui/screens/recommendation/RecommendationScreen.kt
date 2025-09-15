package com.travellight.camera.ui.screens.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travellight.camera.data.model.*
import com.travellight.camera.data.model.RecommendationPriority
import com.travellight.camera.domain.model.EnvironmentInfo
import com.travellight.camera.domain.service.PhotoModeRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.startEnvironmentAnalysis()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部工具栏
        TopAppBar(
            title = { Text("智能拍照推荐") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshRecommendations() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isAnalyzing) {
            AnalyzingIndicator()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 环境信息卡片
                item {
                    EnvironmentInfoCard(environment = uiState.currentEnvironment)
                }
                
                // 综合分析结果
                item {
                    uiState.analysisResult?.let { result ->
                        AnalysisResultCard(result = result)
                    }
                }
                
                // 光源分析
                item {
                    uiState.lightAnalysis?.let { analysis ->
                        LightAnalysisCard(analysis = analysis)
                    }
                }
                
                // 角度推荐
                if (uiState.angleRecommendations.isNotEmpty()) {
                    item {
                        RecommendationSection(
                            title = "拍摄角度推荐",
                            icon = Icons.Default.Add
                        ) {
                            uiState.angleRecommendations.forEach { recommendation ->
                                AngleRecommendationItem(recommendation = recommendation)
                            }
                        }
                    }
                }
                
                // 姿势推荐
                if (uiState.poseRecommendations.isNotEmpty()) {
                    item {
                        RecommendationSection(
                            title = "姿势指导",
                            icon = Icons.Default.Person
                        ) {
                            uiState.poseRecommendations.forEach { recommendation ->
                                PoseRecommendationItem(recommendation = recommendation)
                            }
                        }
                    }
                }
                
                // 相机模式推荐
                if (uiState.cameraModeRecommendations.isNotEmpty()) {
                    item {
                        RecommendationSection(
                            title = "相机模式推荐",
                            icon = Icons.Default.Settings
                        ) {
                            // 相机模式推荐暂时注释，等待数据结构统一
                            // uiState.cameraModeRecommendations.forEach { recommendation ->
                            //     CameraModeRecommendationItem(
                            //         recommendation = recommendation,
                            //         onApply = { viewModel.applyRecommendation(PhotoRecommendation(
                            //             id = "camera_mode",
                            //             title = "相机模式",
                            //             description = recommendation.reason,
                            //             priority = RecommendationPriority.MEDIUM,
                            //             confidence = recommendation.confidence,
                            //             category = "camera"
                            //         )) }
                            //     )
                            // }
                        }
                    }
                }
            }
        }
        
        // 错误信息
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // 可以显示Snackbar或其他错误提示
            }
        }
    }
}

@Composable
fun AnalyzingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在分析环境...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EnvironmentInfoCard(environment: com.travellight.camera.data.model.EnvironmentInfo?) {
    environment?.let {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "环境信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("光照强度", String.format("%.1f", it.lightLevel))
                    InfoItem("色温", String.format("%.0fK", it.temperature))
                    InfoItem("湿度", String.format("%.0f%%", it.humidity))
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnalysisResultCard(result: RecommendationAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "综合分析",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${result.overallScore}分",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "整体评分: ${result.overallScore}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            result.lightAnalysis?.let { analysis ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• ${analysis.recommendation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LightAnalysisCard(analysis: LightAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "光源分析",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("光线质量", analysis.quality)
                InfoItem("光线方向", analysis.lightDirection)
                InfoItem("色温", "${analysis.colorTemperature}K")
            }
            
            if (analysis.recommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "建议：",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "• ${analysis.recommendation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecommendationSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
fun AngleRecommendationItem(recommendation: AngleRecommendation) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${recommendation.recommendedAngle}°",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "置信度: ${(recommendation.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = recommendation.description,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = recommendation.adjustmentTip,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PoseRecommendationItem(recommendation: PoseRecommendation) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recommendation.poseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "置信度: ${(recommendation.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = recommendation.description,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (recommendation.tips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "提示：",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            recommendation.tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CameraModeRecommendationItem(
    recommendation: PhotoModeRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recommendation.mode.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "推荐设置: ${recommendation.settings}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "适用场景: ${recommendation.suitableScenes.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PriorityChip(priority: RecommendationPriority) {
    val (color, text) = when (priority) {
        RecommendationPriority.HIGH -> MaterialTheme.colorScheme.error to "高"
        RecommendationPriority.MEDIUM -> MaterialTheme.colorScheme.primary to "中"
        RecommendationPriority.LOW -> MaterialTheme.colorScheme.outline to "低"
        RecommendationPriority.CRITICAL -> MaterialTheme.colorScheme.error to "紧急"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}