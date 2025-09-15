package com.travellight.camera.data.model

import com.travellight.camera.data.model.CameraMode

/**
 * 环境信息
 */
data class EnvironmentInfo(
    val lightLevel: Float = 0f,        // 光照强度 (0-1)
    val temperature: Float = 0f,       // 色温
    val humidity: Float = 0f,          // 湿度
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 推荐优先级
 */
enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 拍照推荐
 */
data class PhotoRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val confidence: Float,             // 推荐置信度 (0-1)
    val category: String,              // 推荐类别
    val actionText: String? = null,    // 操作建议文本
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 角度推荐
 */
data class AngleRecommendation(
    val recommendedAngle: Float,       // 推荐角度 (度)
    val currentAngle: Float,           // 当前角度
    val confidence: Float,             // 置信度
    val description: String,           // 描述
    val adjustmentTip: String          // 调整提示
)

/**
 * 姿势推荐
 */
data class PoseRecommendation(
    val poseName: String,              // 姿势名称
    val description: String,           // 描述
    val difficulty: String,            // 难度等级
    val tips: List<String>,            // 姿势提示
    val confidence: Float              // 置信度
)

/**
 * 光源分析
 */
data class LightAnalysis(
    val lightDirection: String,        // 光源方向
    val lightIntensity: Float,         // 光照强度
    val colorTemperature: Float,       // 色温
    val recommendation: String,        // 推荐建议
    val quality: String               // 光照质量评估
)

/**
 * 拍照模式推荐
 */
data class PhotoModeRecommendation(
    val mode: CameraMode,              // 推荐模式
    val reason: String,                // 推荐原因
    val settings: Map<String, Any>,    // 推荐设置
    val confidence: Float              // 置信度
)

/**
 * 推荐分析结果
 */
data class RecommendationAnalysis(
    val angleRecommendation: AngleRecommendation? = null,
    val poseRecommendation: PoseRecommendation? = null,
    val lightAnalysis: LightAnalysis? = null,
    val photoModeRecommendation: PhotoModeRecommendation? = null,
    val overallScore: Float = 0f,      // 整体评分
    val timestamp: Long = System.currentTimeMillis()
)