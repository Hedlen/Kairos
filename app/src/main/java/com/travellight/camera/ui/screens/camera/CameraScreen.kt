package com.travellight.camera.ui.screens.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import com.travellight.camera.data.model.LightType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import com.travellight.camera.ui.screens.main.MainViewModel
import com.travellight.camera.data.model.PoseLibrary
import com.travellight.camera.data.model.PoseTemplate
import com.travellight.camera.domain.service.PoseType
import com.travellight.camera.domain.service.PoseRecommendation
import com.travellight.camera.data.model.ShootingScene

/**
 * 相机主界面
 */
@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 设置Activity引用
    LaunchedEffect(Unit) {
        if (context is Activity) {
            viewModel.setActivity(context)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 相机预览区域
        CameraPreviewArea(
            modifier = Modifier.fillMaxSize(),
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            viewModel = viewModel
        )
        
        // 顶部控制栏
        CameraTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
            onBackClick = onBackClick,
            onRecommendationToggle = { viewModel.toggleRecommendation() },
            isRecommendationEnabled = uiState.isRecommendationEnabled,
            viewModel = viewModel
        )
        
        // 底部控制栏
        CameraBottomControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            onTakePhoto = { viewModel.takePhoto() },
            onOpenGallery = { viewModel.openGallery() },
            onToggleGrid = { viewModel.toggleGrid() },
            isLoading = false
        )
    }
}

/**
 * 相机预览区域
 */
@Composable
fun CameraPreviewArea(
    modifier: Modifier = Modifier,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    viewModel: MainViewModel = hiltViewModel()
) {
    val currentPose by viewModel.currentPose.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val context = LocalContext.current
    
    Box(
        modifier = modifier
    ) {
        // 使用remember缓存PreviewView实例
        val previewView = remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        }
        
        // 相机预览
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // 使用LaunchedEffect初始化相机，避免重复初始化
        LaunchedEffect(lifecycleOwner) {
            viewModel.initializeCamera(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = previewView.surfaceProvider
            )
            viewModel.startPeriodicPersonDetection()
        }
        
        // 在组件销毁时停止检测
        DisposableEffect(Unit) {
            onDispose {
                viewModel.stopPeriodicPersonDetection()
            }
        }
        
        // 网格线
        if (uiState.isGridEnabled) {
            GridLines(modifier = Modifier.fillMaxSize())
        }
        
        // 拍摄建议（左侧）
        if (uiState.isRecommendationEnabled) {
            ShootingRecommendations(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                viewModel = viewModel
            )
        }
        
        // 姿势指导覆盖层（推荐功能开启且为单人场景时显示）
        if (uiState.isRecommendationEnabled && !uiState.isMultiPersonScene) {
            PoseGuideOverlay(
                modifier = Modifier.fillMaxSize(),
                currentPose = currentPose
            )
        }
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onRecommendationToggle: () -> Unit,
    isRecommendationEnabled: Boolean,
    viewModel: MainViewModel
) {
    var isLightPanelExpanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 顶部按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 补光设置下拉按钮
            IconButton(
                onClick = { isLightPanelExpanded = !isLightPanelExpanded },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (uiState.isLightOn) Color.Yellow.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isLightPanelExpanded) Icons.Default.ExpandLess else Icons.Default.WbSunny,
                    contentDescription = "补光设置",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 推荐开关按钮
            IconButton(
                onClick = onRecommendationToggle,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isRecommendationEnabled) Color.Blue.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "推荐开关",
                    tint = Color.White
                )
            }
        }
        
        // 下拉式补光控制面板
         AnimatedVisibility(
             visible = isLightPanelExpanded,
             enter = slideInVertically(animationSpec = tween(300)) + fadeIn(),
             exit = slideOutVertically(animationSpec = tween(300)) + fadeOut()
         ) {
             DropdownLightControlPanel(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(top = 8.dp),
                 viewModel = viewModel,
                 onDismiss = { isLightPanelExpanded = false }
             )
         }
    }
}

/**
 * 下拉式补光控制面板
 */
@Composable
fun DropdownLightControlPanel(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "补光设置",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // 光线强度推荐模式选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "推荐模式",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                         onClick = { 
                             if (!uiState.isRecommendationEnabled) {
                                 viewModel.toggleRecommendation()
                             }
                         },
                         modifier = Modifier.height(32.dp),
                         colors = ButtonDefaults.buttonColors(
                             containerColor = if (uiState.isRecommendationEnabled) Color.Blue else Color.Gray.copy(alpha = 0.5f),
                             contentColor = Color.White
                         ),
                         shape = RoundedCornerShape(6.dp)
                     ) {
                         Text(
                             text = "自动",
                             fontSize = 12.sp
                         )
                     }
                     Button(
                         onClick = { 
                             if (uiState.isRecommendationEnabled) {
                                 viewModel.toggleRecommendation()
                             }
                         },
                         modifier = Modifier.height(32.dp),
                         colors = ButtonDefaults.buttonColors(
                             containerColor = if (!uiState.isRecommendationEnabled) Color(0xFFFFA500) else Color.Gray.copy(alpha = 0.5f),
                             contentColor = Color.White
                         ),
                         shape = RoundedCornerShape(6.dp)
                     ) {
                         Text(
                             text = "手动",
                             fontSize = 12.sp
                         )
                     }
                }
            }
            
            // 模式说明文字
            Text(
                text = if (uiState.isRecommendationEnabled) 
                    "自动模式：根据环境光线智能调整补光强度" 
                else 
                    "手动模式：可自由调节补光参数",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            
            // 补光开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "补光",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Switch(
                    checked = uiState.isLightOn,
                    onCheckedChange = { viewModel.toggleLight() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Yellow,
                        checkedTrackColor = Color.Yellow.copy(alpha = 0.5f)
                    )
                )
            }
            
            // 光线类型选择（仅手动模式显示）
            if (uiState.isLightOn && !uiState.isRecommendationEnabled) {
                Text(
                    text = "光线类型",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LightType.values().forEach { type ->
                        val isSelected = uiState.selectedLightType == type
                        Button(
                            onClick = { viewModel.selectLightType(type) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color.Yellow else Color.Gray.copy(alpha = 0.5f),
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when (type) {
                                    LightType.WARM -> "暖光"
                                    LightType.COOL -> "冷光"
                                    LightType.NATURAL -> "自然光"
                                    LightType.SOFT -> "柔光"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            // 自动模式下显示推荐的光线类型
            if (uiState.isLightOn && uiState.isRecommendationEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "推荐光线类型",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (uiState.selectedLightType) {
                            LightType.WARM -> "暖光"
                            LightType.COOL -> "冷光"
                            LightType.NATURAL -> "自然光"
                            LightType.SOFT -> "柔光"
                        },
                        color = Color.Yellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "💡 已根据场景自动选择最佳光线类型",
                    color = Color.Blue.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            // 亮度调节 - 根据模式显示不同的控制界面
            if (uiState.isLightOn) {
                if (uiState.isRecommendationEnabled) {
                    // 自动模式：显示智能推荐的光线强度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "智能亮度",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(uiState.brightness * 100).toInt()}%",
                            color = Color.Yellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // 显示智能调整状态
                    Text(
                        text = "✨ 根据环境光线自动调整中",
                        color = Color.Blue.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    // 手动模式：显示手动调节控件
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "亮度",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(uiState.brightness * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Slider(
                        value = uiState.brightness,
                        onValueChange = { viewModel.adjustBrightness(it) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Yellow,
                            activeTrackColor = Color.Yellow,
                            inactiveTrackColor = Color.Gray
                        )
                    )
                    
                    // 快捷亮度按钮（仅手动模式显示）
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.3f to "30%", 0.6f to "60%", 1.0f to "100%").forEach { (brightness, label) ->
                            Button(
                                onClick = { viewModel.adjustBrightness(brightness) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray.copy(alpha = 0.5f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 拍摄建议组件 - 左右对称透明设计
 */
@Composable
fun ShootingRecommendations(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val lightAnalysis by viewModel.lightAnalysis.collectAsState()
    val angleRecommendation by viewModel.angleRecommendation.collectAsState()
    val poseRecommendation by viewModel.poseRecommendation.collectAsState()
    val photoModeRecommendation by viewModel.photoModeRecommendation.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧区域：光线参数、拍摄角度等智能推荐信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和刷新按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "智能推荐",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
                
                // 刷新按钮
                IconButton(
                    onClick = { viewModel.refreshShootingRecommendations() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新建议",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // 人数检测
            RecommendationItem(
                icon = Icons.Default.Person,
                text = "人数: ${uiState.detectedPersonCount}${if (uiState.isMultiPersonScene) " (多人)" else " (单人)"}",
                color = if (uiState.isMultiPersonScene) Color.Yellow else Color.White
            )
            
            // 光线建议
            lightAnalysis?.let { analysis ->
                RecommendationItem(
                    icon = Icons.Default.WbSunny,
                    text = "光线: ${getLightLevelText(analysis.lightLevel)}",
                    color = Color.Yellow
                )
                
                // 光线强度详细信息
                Text(
                    text = "强度: ${(analysis.lightLevel * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 20.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
            
            // 角度建议
            angleRecommendation?.let { recommendation ->
                RecommendationItem(
                    icon = Icons.Default.RotateRight,
                    text = "角度: ${recommendation.description}",
                    color = Color.Cyan
                )
                
                // 角度详细信息
                Text(
                    text = "当前: ${recommendation.currentAngle.toInt()}° → 推荐: ${recommendation.recommendedAngle.toInt()}°",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 20.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
            
            // 拍照模式推荐
            photoModeRecommendation?.let { recommendation ->
                RecommendationItem(
                    icon = Icons.Default.CameraAlt,
                    text = "模式: ${recommendation.description}",
                    color = Color.Magenta
                )
            }
        }
        
        // 右侧区域：摆拍姿势示意图及文字说明
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 姿势推荐标题
            Text(
                text = "姿势推荐",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black,
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
            
            // 姿势推荐内容
            if (uiState.isMultiPersonScene) {
                // 多人场景提示
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "多人场景",
                        tint = Color.Yellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "多人场景",
                        color = Color.Yellow,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                    Text(
                        text = "建议保持队形\n注意间距分布",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            } else {
                // 单人姿势推荐
                poseRecommendation?.let { recommendation ->
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 简笔线稿图（使用Canvas绘制）
                        PoseLineDrawing(
                            modifier = Modifier.size(48.dp),
                            poseType = recommendation.poseType
                        )
                        
                        Text(
                            text = recommendation.description,
                            color = Color.Green,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                        
                        // 姿势提示
                        if (recommendation.tips.isNotEmpty()) {
                            Text(
                                text = recommendation.tips.take(2).joinToString("\n"),
                                color = Color.White,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black,
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 推荐项组件
 */
@Composable
fun RecommendationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                    blurRadius = 2f
                )
            )
        )
    }
}

/**
 * 获取光线等级文本
 */
fun getLightLevelText(lightLevel: Float): String {
    return when {
        lightLevel < 0.3f -> "偏暗"
        lightLevel < 0.7f -> "适中"
        else -> "充足"
    }
}

/**
 * 网格线
 */
@Composable
fun GridLines(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        
        // 绘制九宫格线
        for (i in 1..2) {
            // 垂直线
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(width * i / 3, 0f),
                end = Offset(width * i / 3, height),
                strokeWidth = 1.dp.toPx()
            )
            // 水平线
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, height * i / 3),
                end = Offset(width, height * i / 3),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

/**
 * 姿势指导覆盖层
 */
@Composable
fun PoseGuideOverlay(
    modifier: Modifier = Modifier,
    currentPose: PoseTemplate?
) {
    Box(
        modifier = modifier
    ) {
        // 右侧姿势推荐线条图（仅单人场景）
        currentPose?.let { pose ->
            if (ShootingScene.SINGLE_PERSON in pose.suitableScenes) {
                PoseLineDrawing(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(120.dp),
                    poseType = mapPoseType(pose.poseType)
                )
            }
        }
    }
}

/**
 * 将data.model包的PoseType映射到domain.service包的PoseType
 */
private fun mapPoseType(dataPoseType: com.travellight.camera.data.model.PoseType): PoseType {
    return when (dataPoseType) {
        com.travellight.camera.data.model.PoseType.PORTRAIT -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.GROUP -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.SELFIE -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.LANDSCAPE -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.CLOSE_UP -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.STANDING -> PoseType.STANDING
        com.travellight.camera.data.model.PoseType.SITTING -> PoseType.SITTING
        com.travellight.camera.data.model.PoseType.PROFILE -> PoseType.PROFILE
    }
}

/**
 * 姿势线条图绘制组件
 */
@Composable
fun PoseLineDrawing(
    modifier: Modifier = Modifier,
    poseType: PoseType
) {
    Canvas(
        modifier = modifier
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val strokeWidth = 3.dp.toPx()
        
        when (poseType) {
            PoseType.STANDING -> {
                drawStandingPose(centerX, centerY, strokeWidth)
            }
            PoseType.SITTING -> {
                drawSittingPose(centerX, centerY, strokeWidth)
            }
            PoseType.PROFILE -> {
                drawProfilePose(centerX, centerY, strokeWidth)
            }
            PoseType.GROUP -> {
                drawStandingPose(centerX, centerY, strokeWidth)
            }
            PoseType.LYING -> {
                drawLyingPose(centerX, centerY, strokeWidth)
            }
            PoseType.JUMPING -> {
                drawJumpingPose(centerX, centerY, strokeWidth)
            }
            PoseType.WALKING -> {
                drawWalkingPose(centerX, centerY, strokeWidth)
            }
        }
    }
}

/**
 * 绘制站立姿势
 */
fun DrawScope.drawStandingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyHeight = 40.dp.toPx()
    val armLength = 25.dp.toPx()
    val legLength = 30.dp.toPx()
    
    // 头部
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - bodyHeight / 2 - headRadius),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 2),
        end = Offset(centerX, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 左臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX - armLength, centerY),
        strokeWidth = strokeWidth
    )
    
    // 右臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX + armLength, centerY),
        strokeWidth = strokeWidth
    )
    
    // 左腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX - 10.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
    
    // 右腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX + 10.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制坐姿
 */
fun DrawScope.drawSittingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyHeight = 30.dp.toPx()
    val armLength = 20.dp.toPx()
    val legLength = 25.dp.toPx()
    
    // 头部
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - bodyHeight / 2 - headRadius),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 2),
        end = Offset(centerX, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 左臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX - armLength, centerY + 5.dp.toPx()),
        strokeWidth = strokeWidth
    )
    
    // 右臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX + armLength, centerY + 5.dp.toPx()),
        strokeWidth = strokeWidth
    )
    
    // 左腿（水平）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX - legLength, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 右腿（水平）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX + legLength, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制侧面姿势
 */
fun DrawScope.drawProfilePose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyHeight = 40.dp.toPx()
    val armLength = 20.dp.toPx()
    val legLength = 30.dp.toPx()
    
    // 头部（侧面）
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - bodyHeight / 2 - headRadius),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 2),
        end = Offset(centerX, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 前臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX + armLength, centerY),
        strokeWidth = strokeWidth
    )
    
    // 前腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX + 10.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
    
    // 后腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX - 5.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制特写姿势
 */
fun DrawScope.drawCloseUpPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 25.dp.toPx()
    val neckHeight = 15.dp.toPx()
    val shoulderWidth = 30.dp.toPx()
    
    // 头部（较大）
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - neckHeight),
        style = Stroke(width = strokeWidth)
    )
    
    // 颈部
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - neckHeight + headRadius),
        end = Offset(centerX, centerY),
        strokeWidth = strokeWidth
    )
    
    // 肩膀
    drawLine(
        color = Color.White,
        start = Offset(centerX - shoulderWidth / 2, centerY),
        end = Offset(centerX + shoulderWidth / 2, centerY),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制躺姿
 */
fun DrawScope.drawLyingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyLength = 40.dp.toPx()
    val armLength = 20.dp.toPx()
    val legLength = 25.dp.toPx()
    
    // 头部
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX - bodyLength / 2 - headRadius, centerY),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体（水平）
    drawLine(
        color = Color.White,
        start = Offset(centerX - bodyLength / 2, centerY),
        end = Offset(centerX + bodyLength / 2, centerY),
        strokeWidth = strokeWidth
    )
    
    // 上臂
    drawLine(
        color = Color.White,
        start = Offset(centerX - bodyLength / 4, centerY),
        end = Offset(centerX - bodyLength / 4, centerY - armLength),
        strokeWidth = strokeWidth
    )
    
    // 下臂
    drawLine(
        color = Color.White,
        start = Offset(centerX + bodyLength / 4, centerY),
        end = Offset(centerX + bodyLength / 4, centerY + armLength),
        strokeWidth = strokeWidth
    )
    
    // 左腿
    drawLine(
        color = Color.White,
        start = Offset(centerX + bodyLength / 2, centerY),
        end = Offset(centerX + bodyLength / 2, centerY + legLength),
        strokeWidth = strokeWidth
    )
    
    // 右腿
    drawLine(
        color = Color.White,
        start = Offset(centerX + bodyLength / 2, centerY),
        end = Offset(centerX + bodyLength / 2, centerY - legLength),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制跳跃姿势
 */
fun DrawScope.drawJumpingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyHeight = 35.dp.toPx()
    val armLength = 25.dp.toPx()
    val legLength = 30.dp.toPx()
    
    // 头部
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - bodyHeight / 2 - headRadius),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 2),
        end = Offset(centerX, centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 左臂（向上）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX - armLength, centerY - bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 右臂（向上）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX + armLength, centerY - bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 左腿（弯曲）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX - 15.dp.toPx(), centerY + bodyHeight / 2 + legLength / 2),
        strokeWidth = strokeWidth
    )
    
    // 右腿（弯曲）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + bodyHeight / 2),
        end = Offset(centerX + 15.dp.toPx(), centerY + bodyHeight / 2 + legLength / 2),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制行走姿势
 */
fun DrawScope.drawWalkingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    val headRadius = 15.dp.toPx()
    val bodyHeight = 40.dp.toPx()
    val armLength = 25.dp.toPx()
    val legLength = 30.dp.toPx()
    
    // 头部
    drawCircle(
        color = Color.White,
        radius = headRadius,
        center = Offset(centerX, centerY - bodyHeight / 2 - headRadius),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体（稍微倾斜）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 2),
        end = Offset(centerX + 3.dp.toPx(), centerY + bodyHeight / 2),
        strokeWidth = strokeWidth
    )
    
    // 左臂（向后摆）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX - armLength, centerY + 5.dp.toPx()),
        strokeWidth = strokeWidth
    )
    
    // 右臂（向前摆）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - bodyHeight / 4),
        end = Offset(centerX + armLength, centerY - 5.dp.toPx()),
        strokeWidth = strokeWidth
    )
    
    // 左腿（向前迈）
    drawLine(
        color = Color.White,
        start = Offset(centerX + 3.dp.toPx(), centerY + bodyHeight / 2),
        end = Offset(centerX + 15.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
    
    // 右腿（支撑）
    drawLine(
        color = Color.White,
        start = Offset(centerX + 3.dp.toPx(), centerY + bodyHeight / 2),
        end = Offset(centerX - 5.dp.toPx(), centerY + bodyHeight / 2 + legLength),
        strokeWidth = strokeWidth
    )
}

/**
 * 底部控制栏
 */
@Composable
fun CameraBottomControls(
    modifier: Modifier = Modifier,
    onTakePhoto: () -> Unit,
    onOpenGallery: () -> Unit,
    onToggleGrid: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 网格切换按钮
        IconButton(
            onClick = onToggleGrid,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "网格",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 拍照按钮
        IconButton(
            onClick = onTakePhoto,
            modifier = Modifier
                .size(72.dp)
                .background(
                    Color.White,
                    CircleShape
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Icon(
                     imageVector = Icons.Default.Add,
                     contentDescription = "拍照",
                     tint = Color.Black,
                     modifier = Modifier.size(32.dp)
                 )
            }
        }
        
        // 相册按钮
        IconButton(
            onClick = onOpenGallery,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                 imageVector = Icons.Default.Image,
                 contentDescription = "相册",
                 tint = Color.White,
                 modifier = Modifier.size(24.dp)
             )
        }
    }
}