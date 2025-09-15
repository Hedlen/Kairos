package com.travellight.camera.ui.screens.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.travellight.camera.data.model.PhotoRecommendation
import com.travellight.camera.data.model.AngleRecommendation
import com.travellight.camera.data.model.PoseRecommendation
import com.travellight.camera.data.model.LightAnalysis
import com.travellight.camera.data.model.CameraMode
import com.travellight.camera.data.model.CameraModeRecommendation
import com.travellight.camera.data.model.RecommendationAnalysis
import com.travellight.camera.data.model.PhotoModeRecommendation
import com.travellight.camera.data.model.EnvironmentInfo
import com.travellight.camera.data.model.RecommendationPriority
import com.travellight.camera.data.repository.RecommendationRepository
import javax.inject.Inject

/**
 * 推荐界面UI状态
 */
data class RecommendationUiState(
    val currentEnvironment: EnvironmentInfo? = null,
    val photoRecommendations: List<PhotoRecommendation> = emptyList(),
    val angleRecommendations: List<AngleRecommendation> = emptyList(),
    val poseRecommendations: List<PoseRecommendation> = emptyList(),
    val lightAnalysis: LightAnalysis? = null,
    val cameraModeRecommendations: List<CameraModeRecommendation> = emptyList(),
    val analysisResult: RecommendationAnalysis? = null,
    val isAnalyzing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()
    
    init {
        // 监听Repository状态变化
        viewModelScope.launch {
            recommendationRepository.environmentInfo.collect { environment ->
                _uiState.value = _uiState.value.copy(currentEnvironment = environment)
            }
        }
        
        viewModelScope.launch {
            recommendationRepository.recommendations.collect { recommendations ->
                _uiState.value = _uiState.value.copy(photoRecommendations = recommendations)
            }
        }
        
        viewModelScope.launch {
            recommendationRepository.analysisResult.collect { result ->
                _uiState.value = _uiState.value.copy(analysisResult = result)
            }
        }
    }
    
    /**
     * 开始环境分析
     */
    fun startEnvironmentAnalysis() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = true,
                    errorMessage = null
                )
                
                // 模拟环境检测
                val environmentInfo = EnvironmentInfo(
                    lightLevel = 0.2f + kotlin.random.Random.nextFloat() * 0.6f,
                    temperature = (2700..6500).random().toFloat(),
                    humidity = (40..70).random().toFloat(),
                    timestamp = System.currentTimeMillis()
                )
                
                // 更新环境信息
                recommendationRepository.updateEnvironmentInfo(environmentInfo)
                
                // 生成推荐
                generateRecommendations(environmentInfo)
                
                _uiState.value = _uiState.value.copy(isAnalyzing = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = "环境分析失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 生成推荐
     */
    private suspend fun generateRecommendations(environment: EnvironmentInfo) {
        // 生成角度推荐
        val angleRecommendations = generateAngleRecommendations(environment)
        _uiState.value = _uiState.value.copy(angleRecommendations = angleRecommendations)
        
        // 生成姿势推荐
        val poseRecommendations = generatePoseRecommendations(environment)
        _uiState.value = _uiState.value.copy(poseRecommendations = poseRecommendations)
        
        // 生成光源分析
        val lightAnalysis = generateLightAnalysis(environment)
        _uiState.value = _uiState.value.copy(lightAnalysis = lightAnalysis)
        
        // 生成相机模式推荐
        val cameraModeRecommendations = generateCameraModeRecommendations(environment)
        _uiState.value = _uiState.value.copy(cameraModeRecommendations = cameraModeRecommendations)
        
        // 生成综合分析结果
        val analysisResult = RecommendationAnalysis(
            angleRecommendation = angleRecommendations.firstOrNull(),
            poseRecommendation = poseRecommendations.firstOrNull(),
            lightAnalysis = lightAnalysis,
            photoModeRecommendation = PhotoModeRecommendation(
                mode = cameraModeRecommendations.firstOrNull()?.mode ?: CameraMode.PHOTO,
                reason = cameraModeRecommendations.firstOrNull()?.reason ?: "自动模式",
                settings = cameraModeRecommendations.firstOrNull()?.settings ?: emptyMap(),
                confidence = 0.7f + kotlin.random.Random.nextFloat() * 0.25f
            ),
            overallScore = (70..95).random().toFloat()
        )
        
        // 更新分析结果到UI状态
        _uiState.value = _uiState.value.copy(analysisResult = analysisResult)
    }
    
    /**
     * 生成角度推荐
     */
    private fun generateAngleRecommendations(environment: EnvironmentInfo): List<AngleRecommendation> {
        return listOf(
            AngleRecommendation(
                recommendedAngle = if (environment.lightLevel < 0.3f) 30f else 45f,
                currentAngle = 0f,
                confidence = 0.8f,
                description = if (environment.lightLevel < 0.3f) "低角度拍摄，配合补光效果更佳" else "标准角度，光线充足",
                adjustmentTip = "基于当前光照条件优化"
            ),
            AngleRecommendation(
                recommendedAngle = 60f,
                currentAngle = 0f,
                confidence = 0.6f,
                description = "高角度拍摄，适合展现环境背景",
                adjustmentTip = "增加画面层次感"
            )
        )
    }
    
    /**
     * 生成姿势推荐
     */
    private fun generatePoseRecommendations(environment: EnvironmentInfo): List<PoseRecommendation> {
        return listOf(
            PoseRecommendation(
                poseName = if (environment.lightLevel < 0.5f) "室内标准姿势" else "户外自然姿势",
                description = if (environment.lightLevel < 0.5f) "身体略向前倾，面向光源" else "自然站立，利用环境光",
                difficulty = "简单",
                tips = listOf(
                    "保持自然微笑",
                    "肩膀放松",
                    if (environment.lightLevel < 0.5f) "面向窗户或光源" else "避免逆光拍摄"
                ),
                confidence = 0.8f
            )
        )
    }
    
    /**
     * 生成光源分析
     */
    private fun generateLightAnalysis(environment: EnvironmentInfo): LightAnalysis {
        return LightAnalysis(
            lightDirection = "前方45度",
            lightIntensity = environment.lightLevel,
            colorTemperature = environment.temperature,
            recommendation = when {
                environment.lightLevel < 0.3f -> "建议使用补光灯或移至光线更好的位置"
                environment.lightLevel > 0.8f -> "光线过强，建议调整角度或使用遮光板"
                else -> "光线条件良好，可以直接拍摄"
            },
            quality = when {
                environment.lightLevel < 0.3f -> "较差"
                environment.lightLevel > 0.8f -> "过强"
                else -> "良好"
            }
        )
    }
    
    /**
     * 生成相机模式推荐
     */
    private fun generateCameraModeRecommendations(environment: EnvironmentInfo): List<CameraModeRecommendation> {
        return listOf(
            CameraModeRecommendation(
                mode = if (environment.lightLevel < 0.4f) CameraMode.NIGHT else CameraMode.PHOTO,
                reason = if (environment.lightLevel < 0.4f) "光线较暗，夜景模式可提升画质" else "光线充足，自动模式即可",
                settings = mapOf(
                    "iso" to if (environment.lightLevel < 0.4f) "800" else "200",
                    "exposure" to if (environment.lightLevel < 0.4f) "+1" else "0"
                ),
                priority = RecommendationPriority.HIGH
            )
        )
    }
    
    /**
     * 应用推荐设置
     */
    fun applyRecommendation(recommendation: PhotoRecommendation) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // TODO: 实际应用推荐设置到相机
                // 这里可以调用相机服务来应用设置
                
                kotlinx.coroutines.delay(500) // 模拟应用过程
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "应用推荐失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 刷新推荐
     */
    fun refreshRecommendations() {
        val currentEnvironment = _uiState.value.currentEnvironment
        if (currentEnvironment != null) {
            viewModelScope.launch {
                generateRecommendations(currentEnvironment)
            }
        } else {
            startEnvironmentAnalysis()
        }
    }
}