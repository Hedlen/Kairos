package com.travellight.camera.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 照片信息模型
 */
@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey
    val id: String,
    val filePath: String,
    val fileName: String,
    val timestamp: Date,
    val location: Location? = null,
    val lightSettings: LightSettings,
    val cameraSettings: CameraSettings,
    val fileSize: Long,
    val width: Int,
    val height: Int,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
)

/**
 * 位置信息模型
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null
)

/**
 * 补光设置模型
 */
data class LightSettings(
    val type: LightType,
    val brightness: Float, // 0-100
    val isEnabled: Boolean,
    val autoAdjust: Boolean = false,
    val colorTemperature: Int? = null // 色温值（K）
)

/**
 * 相机设置模型
 */
data class CameraSettings(
    val resolution: Resolution,
    val quality: PhotoQuality,
    val flashMode: FlashMode,
    val focusMode: FocusMode,
    val whiteBalance: WhiteBalance,
    val iso: Int? = null,
    val exposureTime: Long? = null,
    val aperture: Float? = null
)

/**
 * 分辨率模型
 */
data class Resolution(
    val width: Int,
    val height: Int
) {
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()
    
    val megapixels: Float
        get() = (width * height) / 1_000_000f
    
    override fun toString(): String = "${width}x${height}"
}

/**
 * 环境信息模型
 */
data class EnvironmentInfo(
    val lightLevel: LightLevel,
    val colorTemperature: Int, // 色温（K）
    val ambientBrightness: Float, // 环境亮度（lux）
    val weather: Weather? = null,
    val timeOfDay: TimeOfDay,
    val location: Location? = null,
    val timestamp: Date = Date()
)

/**
 * 拍照推荐模型
 */
data class PhotoRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val recommendedLightSettings: LightSettings,
    val recommendedCameraSettings: CameraSettings,
    val priority: RecommendationPriority,
    val applicableScenarios: List<PhotoScenario>,
    val tips: List<String> = emptyList(),
    val exampleImageUrl: String? = null
)

/**
 * 用户设置模型
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: String = "default",
    val defaultLightType: LightType = LightType.WARM,
    val defaultBrightness: Float = 50f,
    val autoLightEnabled: Boolean = true,
    val photoQuality: PhotoQuality = PhotoQuality.HIGH,
    val saveLocationEnabled: Boolean = false,
    val gridLinesEnabled: Boolean = false,
    val storageLocation: String = "",
    val autoCleanupEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val language: String = "zh-CN",
    val lastModified: Date = Date()
)

/**
 * 拍照历史记录模型
 */
@Entity(tableName = "photo_history")
data class PhotoHistory(
    @PrimaryKey
    val id: String,
    val photoId: String,
    val action: HistoryAction,
    val timestamp: Date,
    val details: String? = null
)

/**
 * 补光类型枚举
 */
enum class LightType {
    WARM,    // 暖光
    COOL,    // 冷光
    NATURAL, // 自然光
    SOFT     // 柔光
}

/**
 * 照片质量枚举
 */
enum class PhotoQuality {
    HIGH,     // 高质量
    MEDIUM,   // 中等质量
    LOW       // 低质量（节省空间）
}

/**
 * 闪光灯模式枚举
 */
enum class FlashMode {
    AUTO,     // 自动
    ON,       // 开启
    OFF,      // 关闭
    TORCH     // 手电筒模式
}

/**
 * 对焦模式枚举
 */
enum class FocusMode {
    AUTO,           // 自动对焦
    MANUAL,         // 手动对焦
    CONTINUOUS,     // 连续对焦
    INFINITY,       // 无限远对焦
    MACRO           // 微距对焦
}

/**
 * 白平衡枚举
 */
enum class WhiteBalance {
    AUTO,           // 自动
    DAYLIGHT,       // 日光
    CLOUDY,         // 阴天
    TUNGSTEN,       // 钨丝灯
    FLUORESCENT,    // 荧光灯
    SHADE           // 阴影
}

/**
 * 光照水平枚举
 */
enum class LightLevel {
    VERY_DARK,      // 非常暗
    DARK,           // 暗
    DIM,            // 昏暗
    NORMAL,         // 正常
    BRIGHT,         // 明亮
    VERY_BRIGHT     // 非常明亮
}

/**
 * 天气枚举
 */
enum class Weather {
    SUNNY,          // 晴天
    CLOUDY,         // 多云
    OVERCAST,       // 阴天
    RAINY,          // 雨天
    SNOWY,          // 雪天
    FOGGY           // 雾天
}

/**
 * 时间段枚举
 */
enum class TimeOfDay {
    DAWN,           // 黎明
    MORNING,        // 上午
    NOON,           // 中午
    AFTERNOON,      // 下午
    EVENING,        // 傍晚
    NIGHT           // 夜晚
}

/**
 * 推荐优先级枚举
 */
enum class RecommendationPriority {
    LOW,            // 低优先级
    MEDIUM,         // 中等优先级
    HIGH,           // 高优先级
    URGENT          // 紧急
}

/**
 * 拍照场景枚举
 */
enum class PhotoScenario {
    PORTRAIT,       // 人像
    LANDSCAPE,      // 风景
    MACRO,          // 微距
    NIGHT,          // 夜景
    INDOOR,         // 室内
    OUTDOOR,        // 户外
    FOOD,           // 美食
    ARCHITECTURE,   // 建筑
    NATURE,         // 自然
    STREET          // 街拍
}

/**
 * 历史操作枚举
 */
enum class HistoryAction {
    CREATED,        // 创建
    EDITED,         // 编辑
    DELETED,        // 删除
    SHARED,         // 分享
    FAVORITED,      // 收藏
    UNFAVORITED,    // 取消收藏
    EXPORTED        // 导出
}