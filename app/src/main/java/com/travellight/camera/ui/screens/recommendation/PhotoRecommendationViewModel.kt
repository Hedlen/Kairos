package com.travellight.camera.ui.screens.recommendation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.travellight.camera.domain.service.RecommendationService
import com.travellight.camera.domain.service.AngleRecommendation
import com.travellight.camera.domain.service.PoseRecommendation
import com.travellight.camera.domain.service.LightAnalysis
import com.travellight.camera.domain.service.PhotoModeRecommendation
import javax.inject.Inject

/**
 * 环境信息数据类
 */
data class EnvironmentInfo(
    val timeOfDay: String = "",
    val weather: String = "",
    val location: String = "",
    val lightLevel: Int = 0
)

/**
 * 推荐优先级
 */
enum class RecommendationPriority {
    HIGH, MEDIUM, LOW
}

/**
 * 拍照推荐数据类
 */
data class PhotoRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val lightType: String,
    val brightness: Int,
    val priority: RecommendationPriority,
    val reason: String
)

/**
 * 拍照推荐页面UI状态
 */
data class PhotoRecommendationUiState(
    val environmentInfo: EnvironmentInfo = EnvironmentInfo(),
    val recommendations: List<PhotoRecommendation> = emptyList(),
    val angleRecommendation: AngleRecommendation? = null,
    val poseRecommendation: PoseRecommendation? = null,
    val lightAnalysis: LightAnalysis? = null,
    val photoModeRecommendation: PhotoModeRecommendation? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 拍照推荐ViewModel
 * 管理环境分析和拍照推荐的业务逻辑
 */
@HiltViewModel
class PhotoRecommendationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recommendationService: RecommendationService
) : ViewModel() {
    
    private var isInitialized = false
    
    private val _uiState = MutableStateFlow(PhotoRecommendationUiState())
    val uiState: StateFlow<PhotoRecommendationUiState> = _uiState.asStateFlow()
    
    init {
        // 监听推荐服务的状态变化
        viewModelScope.launch {
            combine(
                recommendationService.angleRecommendation,
                recommendationService.poseRecommendation,
                recommendationService.lightAnalysis,
                recommendationService.photoModeRecommendation
            ) { angle, pose, light, photoMode ->
                _uiState.value = _uiState.value.copy(
                    angleRecommendation = angle,
                    poseRecommendation = pose,
                    lightAnalysis = light,
                    photoModeRecommendation = photoMode
                )
            }
        }
    }
    
    /**
     * 初始化推荐服务
     */
    fun initializeRecommendationService() {
        if (!isInitialized) {
            recommendationService.initializeSensors(context)
            isInitialized = true
        }
    }
    
    /**
     * 开始推荐分析
     */
    fun startRecommendationAnalysis(sceneType: String = "general") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // 初始化传感器
                initializeRecommendationService()
                
                // 开始监听传感器
                recommendationService.startSensorListening()
                
                // 触发推荐分析
                recommendationService.triggerRecommendationAnalysis(sceneType)
                
                // 获取环境信息
                val environmentInfo = getCurrentEnvironmentInfo()
                
                // 生成基础推荐
                val recommendations = generateRecommendations(environmentInfo)
                
                _uiState.value = _uiState.value.copy(
                    environmentInfo = environmentInfo,
                    recommendations = recommendations,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
    
    /**
     * 停止推荐分析
     */
    fun stopRecommendationAnalysis() {
        recommendationService.stopSensorListening()
    }
    
    /**
     * 加载推荐信息（兼容旧接口）
     */
    fun loadRecommendations() {
        startRecommendationAnalysis()
    }
    
    /**
     * 应用推荐设置
     */
    fun applyRecommendation(recommendation: PhotoRecommendation) {
        viewModelScope.launch {
            try {
                // TODO: 将推荐设置应用到主界面
                // 1. 设置补光类型
                // 2. 调整亮度
                // 3. 返回主界面
                
                // 这里可以通过SharedViewModel或者Repository来传递设置
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
    
    /**
     * 获取当前环境信息
     */
    private suspend fun getCurrentEnvironmentInfo(): EnvironmentInfo {
        // TODO: 实现环境信息获取
        // 1. 获取当前时间
        // 2. 获取天气信息（可选）
        // 3. 获取位置信息（可选）
        // 4. 检测环境光照水平
        
        return EnvironmentInfo(
            timeOfDay = getCurrentTimeOfDay(),
            weather = "晴朗", // 模拟数据
            location = "户外", // 模拟数据
            lightLevel = 70 // 模拟数据
        )
    }
    
    /**
     * 根据环境信息生成推荐
     */
    private suspend fun generateRecommendations(environmentInfo: EnvironmentInfo): List<PhotoRecommendation> {
        val recommendations = mutableListOf<PhotoRecommendation>()
        
        // 根据时间推荐
        when (environmentInfo.timeOfDay) {
            "早晨" -> {
                recommendations.add(
                    PhotoRecommendation(
                        id = "morning_warm",
                        title = "早晨暖光推荐",
                        description = "早晨光线柔和，建议使用暖光补光，营造温馨氛围",
                        lightType = "暖光",
                        brightness = 60,
                        priority = RecommendationPriority.HIGH,
                        reason = "早晨自然光偏暖，暖光补光效果更自然"
                    )
                )
            }
            "中午" -> {
                recommendations.add(
                    PhotoRecommendation(
                        id = "noon_natural",
                        title = "中午自然光推荐",
                        description = "中午光线充足，建议使用自然光模式，减少过度曝光",
                        lightType = "自然光",
                        brightness = 40,
                        priority = RecommendationPriority.MEDIUM,
                        reason = "中午光线强烈，适度补光即可"
                    )
                )
            }
            "傍晚" -> {
                recommendations.add(
                    PhotoRecommendation(
                        id = "evening_soft",
                        title = "傍晚柔光推荐",
                        description = "傍晚光线渐暗，建议使用柔光补光，保持画面层次",
                        lightType = "柔光",
                        brightness = 80,
                        priority = RecommendationPriority.HIGH,
                        reason = "傍晚需要更多补光来平衡光线"
                    )
                )
            }
            "夜晚" -> {
                recommendations.add(
                    PhotoRecommendation(
                        id = "night_cool",
                        title = "夜晚冷光推荐",
                        description = "夜晚拍摄建议使用冷光，突出夜景氛围",
                        lightType = "冷光",
                        brightness = 90,
                        priority = RecommendationPriority.HIGH,
                        reason = "夜晚需要强补光，冷光更适合夜景"
                    )
                )
            }
        }
        
        // 根据环境光照水平推荐
        if (environmentInfo.lightLevel < 30) {
            recommendations.add(
                PhotoRecommendation(
                    id = "low_light_boost",
                    title = "低光环境增强",
                    description = "检测到光线不足，建议提高补光亮度",
                    lightType = "自然光",
                    brightness = 95,
                    priority = RecommendationPriority.HIGH,
                    reason = "环境光线不足，需要强补光"
                )
            )
        }
        
        return recommendations
    }
    
    /**
     * 获取当前时段
     */
    private fun getCurrentTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..10 -> "早晨"
            in 11..14 -> "中午"
            in 15..18 -> "下午"
            in 19..21 -> "傍晚"
            else -> "夜晚"
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        stopRecommendationAnalysis()
    }
}