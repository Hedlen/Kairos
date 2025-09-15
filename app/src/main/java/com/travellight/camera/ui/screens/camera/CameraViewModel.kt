package com.travellight.camera.ui.screens.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.travellight.camera.service.CameraService
import com.travellight.camera.data.model.CameraState
import com.travellight.camera.data.model.CameraMode
import com.travellight.camera.data.model.CaptureState
import com.travellight.camera.data.model.PhotoResult
import com.travellight.camera.data.model.CaptureResult
import com.travellight.camera.data.repository.CameraRepository
import javax.inject.Inject

/**
 * 相机界面UI状态
 */
data class CameraUiState(
    val cameraState: CameraState = CameraState.IDLE,
    val captureState: CaptureState = CaptureState.IDLE,
    val currentMode: CameraMode = CameraMode.PHOTO,
    val isFlashEnabled: Boolean = false,
    val zoomLevel: Float = 1.0f,
    val isGridEnabled: Boolean = false,
    val lastPhotoResult: PhotoResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraService: CameraService,
    private val cameraRepository: CameraRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    init {
        // 监听Repository状态变化
        viewModelScope.launch {
            cameraRepository.cameraState.collect { state ->
                _uiState.value = _uiState.value.copy(cameraState = state)
            }
        }
        
        viewModelScope.launch {
            cameraRepository.captureResults.collect { results ->
                val lastResult = results.lastOrNull()
                val photoResult = lastResult?.let {
                    PhotoResult(
                        success = it.isSuccess,
                        uri = it.imageUri,
                        error = it.errorMessage,
                        timestamp = it.timestamp
                    )
                }
                _uiState.value = _uiState.value.copy(lastPhotoResult = photoResult)
            }
        }
    }
    
    /**
     * 初始化相机
     */
    fun initializeCamera() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // 相机服务已准备就绪
                // CameraService will be initialized when openCamera is called
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cameraState = CameraState.OPENED
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "相机初始化失败: ${e.message}",
                    cameraState = CameraState.ERROR
                )
            }
        }
    }
    
    /**
     * 拍照
     */
    fun capturePhoto() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState.cameraState != CameraState.OPENED) {
                    return@launch
                }
                
                _uiState.value = currentState.copy(
                    captureState = CaptureState.CAPTURING
                )
                
                // 执行拍照
                val success = cameraService.capturePhoto()
                
                if (success) {
                    // 创建成功的拍照结果
                    val photoResult = PhotoResult(
                        success = true,
                        uri = null, // URI will be set when image is saved
                        error = null,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    // 转换为CaptureResult并保存
                    val captureResult = CaptureResult(
                        isSuccess = true,
                        imageUri = null,
                        errorMessage = null,
                        timestamp = System.currentTimeMillis()
                    )
                    cameraRepository.saveCaptureResult(captureResult)
                    
                    _uiState.value = _uiState.value.copy(
                        captureState = CaptureState.SUCCESS,
                        lastPhotoResult = photoResult
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        captureState = CaptureState.ERROR,
                        errorMessage = "拍照失败"
                    )
                }
                
                // 延迟重置状态
                kotlinx.coroutines.delay(1000)
                _uiState.value = _uiState.value.copy(
                    captureState = CaptureState.IDLE
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    captureState = CaptureState.ERROR,
                    errorMessage = "拍照失败: ${e.message}"
                )
                
                // 延迟重置状态
                kotlinx.coroutines.delay(2000)
                _uiState.value = _uiState.value.copy(
                    captureState = CaptureState.IDLE,
                    errorMessage = null
                )
            }
        }
    }
    
    /**
     * 切换闪光灯
     */
    fun toggleFlash() {
        viewModelScope.launch {
            val newFlashState = !_uiState.value.isFlashEnabled
            _uiState.value = _uiState.value.copy(isFlashEnabled = newFlashState)
            
            // TODO: 实现闪光灯设置
            // cameraService.setFlashEnabled(newFlashState)
        }
    }
    
    /**
     * 调整缩放级别
     */
    fun adjustZoom(zoomLevel: Float) {
        viewModelScope.launch {
            val clampedZoom = zoomLevel.coerceIn(1.0f, 10.0f)
            _uiState.value = _uiState.value.copy(zoomLevel = clampedZoom)
            
            // TODO: 实现缩放设置
            // cameraService.setZoomLevel(clampedZoom)
        }
    }
    
    /**
     * 切换网格线
     */
    fun toggleGrid() {
        _uiState.value = _uiState.value.copy(
            isGridEnabled = !_uiState.value.isGridEnabled
        )
    }
    
    /**
     * 切换相机模式
     */
    fun switchCameraMode(mode: CameraMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentMode = mode)
            
            // 更新Repository配置
            val currentConfig = cameraRepository.cameraConfig.value
            cameraRepository.updateCameraConfig(
                currentConfig.copy(mode = mode)
            )
        }
    }
    
    /**
     * 释放相机资源
     */
    fun releaseCamera() {
        viewModelScope.launch {
            // TODO: 实现相机资源释放
            // cameraService.release()
            _uiState.value = _uiState.value.copy(
                cameraState = CameraState.IDLE
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        releaseCamera()
    }
}