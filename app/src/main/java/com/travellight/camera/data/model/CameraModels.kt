package com.travellight.camera.data.model

import android.net.Uri

/**
 * 相机状态枚举
 */
enum class CameraState {
    IDLE,
    OPENING,
    OPENED,
    CAPTURING,
    ERROR,
    CLOSED
}

/**
 * 拍照状态枚举
 */
enum class CaptureState {
    IDLE,
    CAPTURING,
    SUCCESS,
    ERROR
}

/**
 * 相机模式枚举
 */
enum class CameraMode {
    PHOTO,
    VIDEO,
    PORTRAIT,
    NIGHT
}

/**
 * 拍照结果
 */
data class CaptureResult(
    val isSuccess: Boolean,
    val imageUri: Uri? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 照片结果
 */
data class PhotoResult(
    val success: Boolean,
    val filePath: String? = null,
    val uri: Uri? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 相机配置
 */
data class CameraConfig(
    val flashEnabled: Boolean = false,
    val mode: CameraMode = CameraMode.PHOTO,
    val cameraMode: CameraMode = CameraMode.PHOTO,
    val imageQuality: Int = 95,
    val enableHdr: Boolean = false,
    val enableStabilization: Boolean = true,
    val gridEnabled: Boolean = false,
    val autoFocus: Boolean = true
)

/**
 * 相机信息
 */
data class CameraInfo(
    val cameraId: String,
    val isFrontFacing: Boolean,
    val supportedResolutions: List<String>,
    val hasFlash: Boolean,
    val supportsHdr: Boolean
)

/**
 * 相机模式推荐
 */
data class CameraModeRecommendation(
    val mode: CameraMode,              // 推荐模式
    val reason: String,                // 推荐原因
    val settings: Map<String, String>, // 推荐设置
    val priority: com.travellight.camera.data.model.RecommendationPriority // 优先级
)