package com.travellight.camera.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.travellight.camera.data.model.LightType
import com.travellight.camera.data.model.CameraMode
import com.travellight.camera.data.repository.LightRepository
import com.travellight.camera.data.repository.CameraRepository
import javax.inject.Inject

/**
 * 设置项数据类
 */
data class SettingItem(
    val id: String,
    val title: String,
    val description: String,
    val type: SettingType,
    val value: Any,
    val options: List<String> = emptyList()
)

/**
 * 设置类型枚举
 */
enum class SettingType {
    SWITCH,     // 开关
    SLIDER,     // 滑块
    DROPDOWN,   // 下拉选择
    BUTTON      // 按钮
}

/**
 * 设置界面UI状态
 */
data class SettingsUiState(
    val lightSettings: List<SettingItem> = emptyList(),
    val cameraSettings: List<SettingItem> = emptyList(),
    val generalSettings: List<SettingItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val lightRepository: LightRepository,
    private val cameraRepository: CameraRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        observeRepositoryChanges()
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 加载补光设置
                val lightSettings = createLightSettings()
                
                // 加载相机设置
                val cameraSettings = createCameraSettings()
                
                // 加载通用设置
                val generalSettings = createGeneralSettings()
                
                _uiState.value = _uiState.value.copy(
                    lightSettings = lightSettings,
                    cameraSettings = cameraSettings,
                    generalSettings = generalSettings,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载设置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 监听Repository变化
     */
    private fun observeRepositoryChanges() {
        viewModelScope.launch {
            lightRepository.lightSettings.collect {
                updateLightSettings()
            }
        }
        
        viewModelScope.launch {
            cameraRepository.cameraConfig.collect {
                updateCameraSettings()
            }
        }
    }
    
    /**
     * 创建补光设置
     */
    private suspend fun createLightSettings(): List<SettingItem> {
        val lightSettings = lightRepository.lightSettings.value
        
        return listOf(
            SettingItem(
                id = "light_enabled",
                title = "启用补光",
                description = "开启或关闭补光功能",
                type = SettingType.SWITCH,
                value = lightSettings.isLightOn
            ),
            SettingItem(
                id = "light_type",
                title = "默认补光类型",
                description = "选择默认的补光类型",
                type = SettingType.DROPDOWN,
                value = lightSettings.lightType.name,
                options = LightType.values().map { it.name }
            ),
            SettingItem(
                id = "light_brightness",
                title = "默认亮度",
                description = "设置默认的补光亮度",
                type = SettingType.SLIDER,
                value = lightSettings.brightness
            ),
            // Split light config removed - managed in UI layer
            // SettingItem(
            //     id = "split_light_enabled",
            //     title = "启用分屏补光",
            //     description = "开启分屏补光功能",
            //     type = SettingType.SWITCH,
            //     value = false
            // )
        )
    }
    
    /**
     * 创建相机设置
     */
    private suspend fun createCameraSettings(): List<SettingItem> {
        val cameraConfig = cameraRepository.cameraConfig.value
        
        return listOf(
            SettingItem(
                id = "camera_mode",
                title = "默认相机模式",
                description = "选择默认的相机模式",
                type = SettingType.DROPDOWN,
                value = cameraConfig.mode.name,
                options = CameraMode.values().map { it.name }
            ),
            SettingItem(
                id = "flash_enabled",
                title = "默认闪光灯",
                description = "开启或关闭默认闪光灯",
                type = SettingType.SWITCH,
                value = cameraConfig.flashEnabled
            ),
            SettingItem(
                id = "grid_enabled",
                title = "显示网格线",
                description = "在相机预览中显示网格线",
                type = SettingType.SWITCH,
                value = cameraConfig.gridEnabled
            ),
            SettingItem(
                id = "auto_focus",
                title = "自动对焦",
                description = "启用自动对焦功能",
                type = SettingType.SWITCH,
                value = cameraConfig.autoFocus
            )
        )
    }
    
    /**
     * 创建通用设置
     */
    private fun createGeneralSettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                id = "save_location",
                title = "保存位置",
                description = "选择照片保存位置",
                type = SettingType.DROPDOWN,
                value = "内部存储",
                options = listOf("内部存储", "SD卡")
            ),
            SettingItem(
                id = "image_quality",
                title = "图片质量",
                description = "设置图片压缩质量",
                type = SettingType.DROPDOWN,
                value = "高质量",
                options = listOf("低质量", "中等质量", "高质量", "原始质量")
            ),
            SettingItem(
                id = "auto_recommendation",
                title = "自动推荐",
                description = "启用智能拍照推荐",
                type = SettingType.SWITCH,
                value = true
            ),
            SettingItem(
                id = "reset_settings",
                title = "重置设置",
                description = "恢复所有设置到默认值",
                type = SettingType.BUTTON,
                value = "重置"
            )
        )
    }
    
    /**
     * 更新设置值
     */
    fun updateSetting(settingId: String, value: Any) {
        viewModelScope.launch {
            try {
                when (settingId) {
                    // 补光设置
                    "light_enabled" -> {
                        if (value as Boolean) {
                            lightRepository.toggleLight()
                        }
                    }
                    "light_type" -> {
                        val lightType = LightType.valueOf(value as String)
                        lightRepository.updateLightType(lightType)
                    }
                    "light_brightness" -> {
                        lightRepository.updateBrightness(value as Float)
                    }
                    "split_light_enabled" -> {
                        // Split light config is now managed in UI layer
                        // No longer handled here
                    }
                    
                    // 相机设置
                    "camera_mode" -> {
                        val mode = CameraMode.valueOf(value as String)
                        val currentConfig = cameraRepository.cameraConfig.value
                        cameraRepository.updateCameraConfig(
                            currentConfig.copy(mode = mode)
                        )
                    }
                    "flash_enabled" -> {
                        val currentConfig = cameraRepository.cameraConfig.value
                        cameraRepository.updateCameraConfig(
                            currentConfig.copy(flashEnabled = value as Boolean)
                        )
                    }
                    "grid_enabled" -> {
                        val currentConfig = cameraRepository.cameraConfig.value
                        cameraRepository.updateCameraConfig(
                            currentConfig.copy(gridEnabled = value as Boolean)
                        )
                    }
                    "auto_focus" -> {
                        val currentConfig = cameraRepository.cameraConfig.value
                        cameraRepository.updateCameraConfig(
                            currentConfig.copy(autoFocus = value as Boolean)
                        )
                    }
                    
                    // 通用设置
                    "reset_settings" -> {
                        resetAllSettings()
                        return@launch
                    }
                }
                
                // 重新加载设置以反映更改
                loadSettings()
                
                // 显示保存成功消息
                _uiState.value = _uiState.value.copy(
                    saveMessage = "设置已保存",
                    errorMessage = null
                )
                
                // 清除保存消息
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(saveMessage = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "保存设置失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 重置所有设置
     */
    private suspend fun resetAllSettings() {
        try {
            // 重置补光设置
            lightRepository.updateLightType(LightType.NATURAL)
            lightRepository.updateBrightness(0.8f)
            
            // 重置相机设置
            val defaultCameraConfig = com.travellight.camera.data.model.CameraConfig(
                mode = CameraMode.PHOTO,
                flashEnabled = false,
                gridEnabled = false,
                autoFocus = true
            )
            cameraRepository.updateCameraConfig(defaultCameraConfig)
            
            _uiState.value = _uiState.value.copy(
                saveMessage = "所有设置已重置为默认值",
                errorMessage = null
            )
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "重置设置失败: ${e.message}"
            )
        }
    }
    
    /**
     * 更新补光设置显示
     */
    private fun updateLightSettings() {
        viewModelScope.launch {
            val lightSettings = createLightSettings()
            _uiState.value = _uiState.value.copy(lightSettings = lightSettings)
        }
    }
    
    /**
     * 更新相机设置显示
     */
    private fun updateCameraSettings() {
        viewModelScope.launch {
            val cameraSettings = createCameraSettings()
            _uiState.value = _uiState.value.copy(cameraSettings = cameraSettings)
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 清除保存消息
     */
    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }
}