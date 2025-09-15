package com.travellight.camera.data.repository

import android.net.Uri
import com.travellight.camera.data.model.CameraConfig
import com.travellight.camera.data.model.CameraInfo
import com.travellight.camera.data.model.CameraState
import com.travellight.camera.data.model.CaptureResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 相机数据仓库接口
 */
interface CameraRepository {
    val cameraState: StateFlow<CameraState>
    val cameraConfig: StateFlow<CameraConfig>
    val captureResults: Flow<List<CaptureResult>>
    
    suspend fun getCameraInfo(): List<CameraInfo>
    suspend fun updateCameraConfig(config: CameraConfig)
    suspend fun saveCaptureResult(result: CaptureResult)
    suspend fun getCaptureHistory(): List<CaptureResult>
    suspend fun deleteCaptureResult(uri: Uri)
}

/**
 * 相机数据仓库实现
 */
@Singleton
class CameraRepositoryImpl @Inject constructor() : CameraRepository {
    
    private val _cameraState = MutableStateFlow(CameraState.IDLE)
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    private val _cameraConfig = MutableStateFlow(CameraConfig())
    override val cameraConfig: StateFlow<CameraConfig> = _cameraConfig.asStateFlow()
    
    private val _captureResults = MutableStateFlow<List<CaptureResult>>(emptyList())
    override val captureResults: Flow<List<CaptureResult>> = _captureResults.asStateFlow()
    
    override suspend fun getCameraInfo(): List<CameraInfo> {
        // TODO: 实现获取相机信息
        return listOf(
            CameraInfo(
                cameraId = "0",
                isFrontFacing = false,
                supportedResolutions = listOf("1920x1080", "1280x720"),
                hasFlash = true,
                supportsHdr = true
            ),
            CameraInfo(
                cameraId = "1",
                isFrontFacing = true,
                supportedResolutions = listOf("1920x1080", "1280x720"),
                hasFlash = false,
                supportsHdr = false
            )
        )
    }
    
    override suspend fun updateCameraConfig(config: CameraConfig) {
        _cameraConfig.value = config
    }
    
    override suspend fun saveCaptureResult(result: CaptureResult) {
        val currentResults = _captureResults.value.toMutableList()
        currentResults.add(0, result) // 添加到列表开头
        _captureResults.value = currentResults
        // TODO: 保存到本地存储
    }
    
    override suspend fun getCaptureHistory(): List<CaptureResult> {
        // TODO: 从本地存储加载
        return _captureResults.value
    }
    
    override suspend fun deleteCaptureResult(uri: Uri) {
        val currentResults = _captureResults.value.toMutableList()
        currentResults.removeAll { it.imageUri == uri }
        _captureResults.value = currentResults
        // TODO: 从本地存储删除
    }
    
    internal fun updateCameraState(state: CameraState) {
        _cameraState.value = state
    }
}