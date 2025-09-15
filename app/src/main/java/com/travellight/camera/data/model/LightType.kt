package com.travellight.camera.data.model

/**
 * 补光类型枚举
 */
enum class LightType {
    WARM,     // 暖光
    COOL,     // 冷光
    NATURAL,  // 自然光
    SOFT      // 柔光
}

// SplitLightConfig moved to ui.components package

/**
 * 补光设置
 */
data class LightSettings(
    val lightType: LightType = LightType.NATURAL,
    val brightness: Float = 0.5f,
    val isLightOn: Boolean = false
)