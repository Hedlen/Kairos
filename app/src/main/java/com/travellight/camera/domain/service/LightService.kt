package com.travellight.camera.domain.service

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.travellight.camera.data.model.LightType
import com.travellight.camera.ui.theme.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 补光服务
 * 负责管理屏幕亮度和颜色来实现补光效果
 */
@Singleton
class LightService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var originalBrightness: Float = -1f
    private var isLightEnabled = false
    
    /**
     * 启用补光
     */
    fun enableLight(activity: Activity, lightType: LightType, brightness: Float = 1.0f) {
        if (!isLightEnabled) {
            // 保存原始亮度
            originalBrightness = getCurrentBrightness()
        }
        
        isLightEnabled = true
        
        // 设置屏幕亮度
        setScreenBrightness(activity, brightness)
        
        // 根据补光类型设置屏幕颜色
        setScreenColor(activity, lightType)
    }
    
    /**
     * 禁用补光
     */
    fun disableLight(activity: Activity) {
        if (isLightEnabled) {
            isLightEnabled = false
            
            // 恢复原始亮度
            if (originalBrightness >= 0) {
                setScreenBrightness(activity, originalBrightness)
            }
            
            // 清除屏幕颜色覆盖
            clearScreenColor(activity)
        }
    }
    
    /**
     * 更新补光类型
     */
    fun updateLightType(activity: Activity, lightType: LightType) {
        if (isLightEnabled) {
            setScreenColor(activity, lightType)
        }
    }
    
    /**
     * 更新补光亮度
     */
    fun updateBrightness(activity: Activity, brightness: Float) {
        if (isLightEnabled) {
            setScreenBrightness(activity, brightness)
        }
    }
    
    /**
     * 获取当前系统亮度
     */
    private fun getCurrentBrightness(): Float {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255.0f
        } catch (e: Exception) {
            0.5f // 默认亮度
        }
    }
    
    /**
     * 设置屏幕亮度
     */
    private fun setScreenBrightness(activity: Activity, brightness: Float) {
        try {
            val window = activity.window ?: return
            val layoutParams = window.attributes ?: return
            layoutParams.screenBrightness = brightness.coerceIn(0.0f, 1.0f)
            window.attributes = layoutParams
        } catch (e: Exception) {
            android.util.Log.e("LightService", "设置屏幕亮度失败: ${e.message}", e)
        }
    }
    
    /**
     * 根据补光类型设置屏幕颜色
     */
    private fun setScreenColor(activity: Activity, lightType: LightType) {
        try {
            val window = activity.window ?: return
            val color = when (lightType) {
                LightType.WARM -> WarmLight
                LightType.COOL -> CoolLight
                LightType.NATURAL -> NaturalLight
                LightType.SOFT -> SoftLight
            }
            
            // 设置状态栏颜色
            window.statusBarColor = color.copy(alpha = 0.3f).toArgb()
            
            // 设置导航栏颜色
            window.navigationBarColor = color.copy(alpha = 0.3f).toArgb()
        } catch (e: Exception) {
            android.util.Log.e("LightService", "设置屏幕颜色失败: ${e.message}", e)
        }
    }
    
    /**
     * 清除屏幕颜色覆盖
     */
    private fun clearScreenColor(activity: Activity) {
        try {
            val window = activity.window ?: return
            // 恢复默认状态栏和导航栏颜色
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        } catch (e: Exception) {
            android.util.Log.e("LightService", "清除屏幕颜色失败: ${e.message}", e)
        }
    }
    
    /**
     * 检查是否正在补光
     */
    fun isLightOn(): Boolean = isLightEnabled
    
    /**
     * 获取补光颜色
     */
    fun getLightColor(lightType: LightType): Color {
        return when (lightType) {
            LightType.WARM -> WarmLight
            LightType.COOL -> CoolLight
            LightType.NATURAL -> NaturalLight
            LightType.SOFT -> SoftLight
        }
    }
}