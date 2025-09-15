package com.travellight.camera.viewmodel

import android.app.Application
import android.content.Context
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travellight.camera.service.CameraService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 相机页面ViewModel
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cameraService: CameraService
) : AndroidViewModel(context as Application) {

    // UI状态
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    // 相机状态
    val cameraState = cameraService.cameraState
    val captureState = cameraService.captureState

    // 权限状态
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        checkPermissions()
        observeCameraStates()
    }

    /**
     * 相机UI状态数据类
     */
    data class CameraUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isFlashEnabled: Boolean = false,
        val currentCameraMode: CameraMode = CameraMode.PHOTO,
        val zoomLevel: Float = 1.0f,
        val lastCapturedImagePath: String? = null,
        val showCaptureAnimation: Boolean = false
    )

    /**
     * 相机模式枚举
     */
    enum class CameraMode {
        PHOTO,
        VIDEO,
        PORTRAIT
    }

    /**
     * 检查权限
     */
    private fun checkPermissions() {
        _hasPermission.value = cameraService.hasCameraPermission()
    }

    /**
     * 监听相机状态变化
     */
    private fun observeCameraStates() {
        viewModelScope.launch {
            cameraService.cameraState.collect { state ->
                when (state) {
                    CameraService.CameraState.OPENING -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    CameraService.CameraState.PREVIEWING -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    }
                    CameraService.CameraState.ERROR -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "相机启动失败，请检查权限设置"
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }

        viewModelScope.launch {
            cameraService.captureState.collect { state ->
                when (state) {
                    CameraService.CaptureState.CAPTURING -> {
                        _uiState.value = _uiState.value.copy(showCaptureAnimation = true)
                    }
                    CameraService.CaptureState.CAPTURED -> {
                        _uiState.value = _uiState.value.copy(
                            showCaptureAnimation = false,
                            lastCapturedImagePath = "照片已保存"
                        )
                        // 重置拍照状态
                        cameraService.resetCaptureState()
                    }
                    CameraService.CaptureState.ERROR -> {
                        _uiState.value = _uiState.value.copy(
                            showCaptureAnimation = false,
                            error = "拍照失败，请重试"
                        )
                        cameraService.resetCaptureState()
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(showCaptureAnimation = false)
                    }
                }
            }
        }
    }

    /**
     * 打开相机
     */
    fun openCamera(width: Int, height: Int, surface: Surface) {
        if (!_hasPermission.value) {
            _uiState.value = _uiState.value.copy(error = "需要相机权限才能使用此功能")
            return
        }
        
        viewModelScope.launch {
            try {
                cameraService.openCamera(width, height, surface)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "相机启动失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * 关闭相机
     */
    fun closeCamera() {
        cameraService.closeCamera()
    }

    /**
     * 拍照
     */
    fun capturePhoto() {
        if (cameraState.value != CameraService.CameraState.PREVIEWING) {
            _uiState.value = _uiState.value.copy(error = "相机未就绪，请稍后再试")
            return
        }

        viewModelScope.launch {
            val success = cameraService.capturePhoto()
            if (!success) {
                _uiState.value = _uiState.value.copy(error = "拍照失败，请重试")
            }
        }
    }

    /**
     * 切换闪光灯
     */
    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(
            isFlashEnabled = !_uiState.value.isFlashEnabled
        )
    }

    /**
     * 切换相机模式
     */
    fun switchCameraMode(mode: CameraMode) {
        _uiState.value = _uiState.value.copy(currentCameraMode = mode)
    }

    /**
     * 设置缩放级别
     */
    fun setZoomLevel(zoom: Float) {
        val clampedZoom = zoom.coerceIn(1.0f, 10.0f)
        _uiState.value = _uiState.value.copy(zoomLevel = clampedZoom)
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 更新权限状态
     */
    fun updatePermissionStatus(granted: Boolean) {
        _hasPermission.value = granted
        if (!granted) {
            _uiState.value = _uiState.value.copy(error = "需要相机权限才能使用此功能")
        } else {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    /**
     * 获取预览尺寸
     */
    fun getPreviewSize() = cameraService.getPreviewSize()

    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        cameraService.closeCamera()
    }
}