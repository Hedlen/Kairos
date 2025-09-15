package com.travellight.camera.data.repository

import com.travellight.camera.data.model.LightSettings
import com.travellight.camera.data.model.LightType

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 补光功能数据仓库接口
 */
interface LightRepository {
    val lightSettings: StateFlow<LightSettings>
    
    suspend fun updateLightType(lightType: LightType)
    suspend fun updateBrightness(brightness: Float)
    suspend fun toggleLight()

    suspend fun saveLightSettings(settings: LightSettings)
    suspend fun loadLightSettings(): LightSettings
}

/**
 * 补光功能数据仓库实现
 */
@Singleton
class LightRepositoryImpl @Inject constructor() : LightRepository {
    
    private val _lightSettings = MutableStateFlow(LightSettings())
    override val lightSettings: StateFlow<LightSettings> = _lightSettings.asStateFlow()
    
    override suspend fun updateLightType(lightType: LightType) {
        _lightSettings.value = _lightSettings.value.copy(lightType = lightType)
    }
    
    override suspend fun updateBrightness(brightness: Float) {
        _lightSettings.value = _lightSettings.value.copy(brightness = brightness)
    }
    
    override suspend fun toggleLight() {
        _lightSettings.value = _lightSettings.value.copy(
            isLightOn = !_lightSettings.value.isLightOn
        )
    }
    

    
    override suspend fun saveLightSettings(settings: LightSettings) {
        _lightSettings.value = settings
        // TODO: 保存到本地存储
    }
    
    override suspend fun loadLightSettings(): LightSettings {
        // TODO: 从本地存储加载
        return _lightSettings.value
    }
}