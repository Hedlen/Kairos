package com.travellight.camera.domain.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.travellight.camera.domain.model.LightType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * 拍照角度推荐
 */
data class AngleRecommendation(
    val currentAngle: Float,
    val recommendedAngle: Float,
    val angleType: AngleType,
    val description: String,
    val confidence: Float
)

/**
 * 角度类型
 */
enum class AngleType {
    HORIZONTAL,     // 水平
    VERTICAL,       // 垂直
    DIAGONAL_UP,    // 斜向上
    DIAGONAL_DOWN,  // 斜向下
    PORTRAIT,       // 人像
    LANDSCAPE       // 风景
}

/**
 * 姿势推荐
 */
data class PoseRecommendation(
    val poseType: PoseType,
    val description: String,
    val tips: List<String>,
    val difficulty: PoseDifficulty
)

/**
 * 姿势类型
 */
enum class PoseType {
    STANDING,       // 站立
    SITTING,        // 坐姿
    LYING,          // 躺姿
    JUMPING,        // 跳跃
    WALKING,        // 行走
    PROFILE,        // 侧面
    GROUP           // 合影
}

/**
 * 姿势难度
 */
enum class PoseDifficulty {
    EASY,           // 简单
    MEDIUM,         // 中等
    HARD            // 困难
}

/**
 * 光源分析结果
 */
data class LightAnalysis(
    val lightLevel: Float,          // 光照强度 (0-1)
    val lightDirection: LightDirection,
    val recommendedLightType: LightType,
    val needsFlash: Boolean,
    val suggestions: List<String>
)

/**
 * 光源方向
 */
enum class LightDirection {
    FRONT,          // 正面光
    BACK,           // 背光
    SIDE,           // 侧光
    TOP,            // 顶光
    MIXED           // 混合光
}

/**
 * 拍照模式推荐
 */
data class PhotoModeRecommendation(
    val mode: PhotoMode,
    val description: String,
    val settings: PhotoSettings,
    val suitableScenes: List<String>
)

/**
 * 拍照模式
 */
enum class PhotoMode {
    PORTRAIT,       // 人像模式
    LANDSCAPE,      // 风景模式
    MACRO,          // 微距模式
    NIGHT,          // 夜景模式
    SPORT,          // 运动模式
    FOOD,           // 美食模式
    SELFIE          // 自拍模式
}

/**
 * 拍照设置
 */
data class PhotoSettings(
    val iso: Int,
    val shutterSpeed: String,
    val aperture: String,
    val focusMode: String,
    val whiteBalance: String
)

/**
 * 智能推荐服务
 */
@Singleton
class RecommendationService @Inject constructor() : SensorEventListener {
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null
    
    // 传感器数据
    private val _accelerometerData = MutableStateFlow(FloatArray(3))
    private val _lightLevel = MutableStateFlow(0f)
    
    // 推荐结果
    private val _angleRecommendation = MutableStateFlow<AngleRecommendation?>(null)
    val angleRecommendation: StateFlow<AngleRecommendation?> = _angleRecommendation.asStateFlow()
    
    private val _poseRecommendation = MutableStateFlow<PoseRecommendation?>(null)
    val poseRecommendation: StateFlow<PoseRecommendation?> = _poseRecommendation.asStateFlow()
    
    private val _lightAnalysis = MutableStateFlow<LightAnalysis?>(null)
    val lightAnalysis: StateFlow<LightAnalysis?> = _lightAnalysis.asStateFlow()
    
    private val _photoModeRecommendation = MutableStateFlow<PhotoModeRecommendation?>(null)
    val photoModeRecommendation: StateFlow<PhotoModeRecommendation?> = _photoModeRecommendation.asStateFlow()
    
    /**
     * 初始化传感器
     */
    fun initializeSensors(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    
    /**
     * 开始监听传感器
     */
    fun startSensorListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    /**
     * 停止监听传感器
     */
    fun stopSensorListening() {
        sensorManager?.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    _accelerometerData.value = it.values.clone()
                    analyzeAngle(it.values)
                }
                Sensor.TYPE_LIGHT -> {
                    _lightLevel.value = it.values[0]
                    analyzeLighting(it.values[0])
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 传感器精度变化处理
    }
    
    /**
     * 分析拍照角度
     */
    private fun analyzeAngle(accelerometerValues: FloatArray) {
        val x = accelerometerValues[0]
        val y = accelerometerValues[1]
        val z = accelerometerValues[2]
        
        // 计算设备倾斜角度
        val pitch = atan2(-x, sqrt(y * y + z * z)) * 180 / PI
        val roll = atan2(y, z) * 180 / PI
        
        val currentAngle = pitch.toFloat()
        
        // 根据角度推荐最佳拍摄角度
        val recommendation = when {
            abs(currentAngle) < 5 -> AngleRecommendation(
                currentAngle = currentAngle,
                recommendedAngle = 0f,
                angleType = AngleType.HORIZONTAL,
                description = "水平拍摄，适合风景和建筑",
                confidence = 0.9f
            )
            currentAngle > 15 -> AngleRecommendation(
                currentAngle = currentAngle,
                recommendedAngle = 30f,
                angleType = AngleType.DIAGONAL_UP,
                description = "仰拍角度，突出主体高大感",
                confidence = 0.8f
            )
            currentAngle < -15 -> AngleRecommendation(
                currentAngle = currentAngle,
                recommendedAngle = -30f,
                angleType = AngleType.DIAGONAL_DOWN,
                description = "俯拍角度，展现全景视角",
                confidence = 0.8f
            )
            else -> AngleRecommendation(
                currentAngle = currentAngle,
                recommendedAngle = 0f,
                angleType = AngleType.PORTRAIT,
                description = "人像拍摄角度",
                confidence = 0.7f
            )
        }
        
        _angleRecommendation.value = recommendation
    }
    
    /**
     * 分析光照条件
     */
    private fun analyzeLighting(lightLevel: Float) {
        val normalizedLight = (lightLevel / 40000f).coerceIn(0f, 1f)
        
        val analysis = when {
            normalizedLight < 0.1f -> LightAnalysis(
                lightLevel = normalizedLight,
                lightDirection = LightDirection.MIXED,
                recommendedLightType = LightType.WARM,
                needsFlash = true,
                suggestions = listOf(
                    "光线较暗，建议开启补光",
                    "使用暖光模式增强氛围",
                    "考虑调整拍摄位置"
                )
            )
            normalizedLight < 0.3f -> LightAnalysis(
                lightLevel = normalizedLight,
                lightDirection = LightDirection.MIXED,
                recommendedLightType = LightType.NATURAL,
                needsFlash = false,
                suggestions = listOf(
                    "光线适中，可使用自然光模式",
                    "注意避免阴影遮挡"
                )
            )
            normalizedLight < 0.7f -> LightAnalysis(
                lightLevel = normalizedLight,
                lightDirection = LightDirection.FRONT,
                recommendedLightType = LightType.COOL,
                needsFlash = false,
                suggestions = listOf(
                    "光线充足，建议使用冷光模式",
                    "可尝试不同角度拍摄"
                )
            )
            else -> LightAnalysis(
                lightLevel = normalizedLight,
                lightDirection = LightDirection.TOP,
                recommendedLightType = LightType.SOFT,
                needsFlash = false,
                suggestions = listOf(
                    "光线过强，建议使用柔光模式",
                    "避免过度曝光",
                    "可考虑遮挡部分光源"
                )
            )
        }
        
        _lightAnalysis.value = analysis
    }
    
    /**
     * 获取姿势推荐
     */
    fun getPoseRecommendation(sceneType: String): PoseRecommendation {
        return when (sceneType.lowercase()) {
            "portrait", "人像" -> PoseRecommendation(
                poseType = PoseType.STANDING,
                description = "经典站立姿势",
                tips = listOf(
                    "保持自然站立",
                    "肩膀放松",
                    "微笑看向镜头",
                    "手臂自然下垂或轻放身侧"
                ),
                difficulty = PoseDifficulty.EASY
            )
            "group", "合影" -> PoseRecommendation(
                poseType = PoseType.GROUP,
                description = "团体合影姿势",
                tips = listOf(
                    "身高较高的人站后排",
                    "保持适当间距",
                    "统一看向镜头",
                    "避免遮挡他人"
                ),
                difficulty = PoseDifficulty.MEDIUM
            )
            "action", "运动" -> PoseRecommendation(
                poseType = PoseType.JUMPING,
                description = "动感跳跃姿势",
                tips = listOf(
                    "同时起跳",
                    "表情自然",
                    "注意安全",
                    "多拍几张选最佳"
                ),
                difficulty = PoseDifficulty.HARD
            )
            else -> PoseRecommendation(
                poseType = PoseType.SITTING,
                description = "舒适坐姿",
                tips = listOf(
                    "背部挺直",
                    "双手自然放置",
                    "表情放松"
                ),
                difficulty = PoseDifficulty.EASY
            )
        }
    }
    
    /**
     * 获取拍照模式推荐
     */
    fun getPhotoModeRecommendation(lightLevel: Float, sceneType: String): PhotoModeRecommendation {
        return when {
            lightLevel < 0.2f -> PhotoModeRecommendation(
                mode = PhotoMode.NIGHT,
                description = "夜景模式",
                settings = PhotoSettings(
                    iso = 800,
                    shutterSpeed = "1/30s",
                    aperture = "f/2.8",
                    focusMode = "连续对焦",
                    whiteBalance = "自动"
                ),
                suitableScenes = listOf("夜景", "室内", "昏暗环境")
            )
            sceneType.contains("人") -> PhotoModeRecommendation(
                mode = PhotoMode.PORTRAIT,
                description = "人像模式",
                settings = PhotoSettings(
                    iso = 200,
                    shutterSpeed = "1/125s",
                    aperture = "f/2.0",
                    focusMode = "单点对焦",
                    whiteBalance = "日光"
                ),
                suitableScenes = listOf("人像", "自拍", "合影")
            )
            sceneType.contains("风景") -> PhotoModeRecommendation(
                mode = PhotoMode.LANDSCAPE,
                description = "风景模式",
                settings = PhotoSettings(
                    iso = 100,
                    shutterSpeed = "1/250s",
                    aperture = "f/8.0",
                    focusMode = "无限远",
                    whiteBalance = "日光"
                ),
                suitableScenes = listOf("风景", "建筑", "自然")
            )
            else -> PhotoModeRecommendation(
                mode = PhotoMode.SELFIE,
                description = "自拍模式",
                settings = PhotoSettings(
                    iso = 200,
                    shutterSpeed = "1/60s",
                    aperture = "f/2.4",
                    focusMode = "面部对焦",
                    whiteBalance = "自动"
                ),
                suitableScenes = listOf("自拍", "近距离拍摄")
            )
        }
    }
    
    /**
     * 手动触发推荐分析
     */
    fun triggerRecommendationAnalysis(sceneType: String = "general") {
        try {
            // 更新姿势推荐
            _poseRecommendation.value = getPoseRecommendation(sceneType)
            
            // 更新拍照模式推荐
            _photoModeRecommendation.value = getPhotoModeRecommendation(_lightLevel.value, sceneType)
        } catch (e: Exception) {
            // 记录错误但不抛出异常，避免闪退
            android.util.Log.e("RecommendationService", "触发推荐分析失败: ${e.message}", e)
        }
    }
}