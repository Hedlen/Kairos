package com.travellight.camera.ui.screens.main

import android.app.Activity
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.travellight.camera.domain.service.LightService
import com.travellight.camera.domain.service.RecommendationService
import com.travellight.camera.data.model.PoseLibrary
import com.travellight.camera.data.model.PoseTemplate
import com.travellight.camera.domain.service.PoseType
import com.travellight.camera.data.model.ShootingScene
import com.travellight.camera.data.model.LightType
import com.travellight.camera.ui.components.SplitLightConfig
import com.travellight.camera.data.repository.LightRepository
import com.travellight.camera.data.repository.CameraRepository
import com.travellight.camera.data.model.CaptureResult

import com.travellight.camera.data.camera.CameraManager
import com.travellight.camera.data.api.DoubaoApiService
import com.travellight.camera.test.DoubaoApiTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import javax.inject.Inject
import android.util.Log

/**
 * 主界面UI状态
 */
data class MainUiState(
    val isLightOn: Boolean = false,
    val selectedLightType: LightType = LightType.NATURAL,
    val brightness: Float = 0.8f,
    val isSplitScreenEnabled: Boolean = false,
    val leftBrightness: Float = 0.8f,
    val rightBrightness: Float = 0.8f,
    val splitLightConfig: SplitLightConfig = SplitLightConfig(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sceneAnalysis: String? = null,
    val poseGuidance: String? = null,
    val isGridEnabled: Boolean = false,
    val isRecommendationEnabled: Boolean = false,
    val detectedPersonCount: Int = 0,
    val isMultiPersonScene: Boolean = false
)

/**
 * 主界面ViewModel
 * 管理补光功能和相机控制的业务逻辑
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val lightService: LightService,
    private val lightRepository: LightRepository,
    private val cameraRepository: CameraRepository,
    private val cameraManager: CameraManager,
    private val recommendationService: RecommendationService,
    private val doubaoApiService: DoubaoApiService,
    private val doubaoApiTest: DoubaoApiTest
) : ViewModel() {
    
    private var currentActivity: Activity? = null
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // 光照分析数据
    val lightAnalysis = recommendationService.lightAnalysis
    val angleRecommendation = recommendationService.angleRecommendation
    
    // 姿势推荐和拍照模式推荐数据
    val poseRecommendation = recommendationService.poseRecommendation
    val photoModeRecommendation = recommendationService.photoModeRecommendation
    
    // 当前推荐姿势
    private val _currentPose = MutableStateFlow<PoseTemplate?>(null)
    val currentPose: StateFlow<PoseTemplate?> = _currentPose.asStateFlow()
    
    /**
     * 设置当前Activity引用
     */
    fun setActivity(activity: Activity) {
        currentActivity = activity
    }
    
    init {
        // 启动传感器监听
        recommendationService.startSensorListening()
        
        // 测试豆包API配置
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "开始测试豆包API配置...")
                doubaoApiTest.testApiConfiguration()
                doubaoApiTest.testSceneAnalysisApi()
                doubaoApiTest.testPoseGuidanceApi()
                Log.d("MainViewModel", "豆包API测试完成")
            } catch (e: Exception) {
                Log.e("MainViewModel", "豆包API测试失败: ${e.message}", e)
            }
        }
        
        // 监听补光状态变化
        viewModelScope.launch {
            lightRepository.lightSettings.collect { lightSettings ->
                _uiState.value = _uiState.value.copy(
                    isLightOn = lightSettings.isLightOn,
                    selectedLightType = lightSettings.lightType,
                    brightness = lightSettings.brightness
                )
            }
        }
        
        // 监听光照分析结果，动态更新UI状态
        viewModelScope.launch {
            lightAnalysis.collect { analysis ->
                analysis?.let {
                    _uiState.value = _uiState.value.copy(
                        brightness = it.lightLevel
                    )
                }
                updatePoseRecommendation()
            }
        }
        
        // 监听角度推荐变化
        viewModelScope.launch {
            angleRecommendation.collect { recommendation ->
                updatePoseRecommendation()
            }
        }
        
        // 监听姿势推荐变化
        viewModelScope.launch {
            poseRecommendation.collect { recommendation ->
                // 姿势推荐变化时可以触发UI更新
                Log.d("MainViewModel", "姿势推荐更新: ${recommendation?.description}")
            }
        }
        
        // 监听拍照模式推荐变化
        viewModelScope.launch {
            photoModeRecommendation.collect { recommendation ->
                // 拍照模式推荐变化时可以触发UI更新
                Log.d("MainViewModel", "拍照模式推荐更新: ${recommendation?.description}")
            }
        }
    }
    
    /**
     * 切换补光开关
     */
    fun toggleLight() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val newLightState = !currentState.isLightOn
                
                _uiState.value = currentState.copy(
                    isLightOn = newLightState
                )
                
                // 更新Repository状态
                lightRepository.toggleLight()
                
                // 添加Activity空指针检查
                val activity = currentActivity
                if (activity == null) {
                    Log.w("MainViewModel", "Activity为空，无法执行补光操作")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "无法访问屏幕控制，请重试"
                    )
                    return@launch
                }
                
                // 检查Activity是否已销毁
                if (activity.isFinishing || activity.isDestroyed) {
                    Log.w("MainViewModel", "Activity已销毁，无法执行补光操作")
                    return@launch
                }
                
                if (newLightState) {
                    if (!currentState.isSplitScreenEnabled) {
                        // 单屏模式
                        lightService.enableLight(
                            activity,
                            currentState.selectedLightType,
                            currentState.brightness
                        )
                    } else {
                        // 分屏模式 - 使用左侧光源类型
                        lightService.enableLight(
                            activity,
                            currentState.selectedLightType,
                            currentState.leftBrightness
                        )
                    }
                } else {
                    lightService.disableLight(activity)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "切换补光失败: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "切换补光失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 选择补光类型
     */
    fun selectLightType(lightType: LightType) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    selectedLightType = lightType
                )
                
                // 更新Repository状态
                lightRepository.updateLightType(lightType)
                
                // 如果当前补光已开启且为单屏模式，更新补光类型
                if (currentState.isLightOn && !currentState.isSplitScreenEnabled) {
                    val activity = currentActivity
                    if (activity == null) {
                        Log.w("MainViewModel", "Activity为空，无法更新补光类型")
                        return@launch
                    }
                    
                    // 检查Activity是否已销毁
                    if (activity.isFinishing || activity.isDestroyed) {
                        Log.w("MainViewModel", "Activity已销毁，无法更新补光类型")
                        return@launch
                    }
                    
                    lightService.updateLightType(activity, lightType)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "选择补光类型失败: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "选择补光类型失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 调节亮度
     */
    fun adjustBrightness(brightness: Float) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    brightness = brightness
                )
                
                // 更新Repository状态
                lightRepository.updateBrightness(brightness)
                
                // 如果当前补光已开启且为单屏模式，更新亮度
                if (currentState.isLightOn && !currentState.isSplitScreenEnabled) {
                    val activity = currentActivity
                    if (activity == null) {
                        Log.w("MainViewModel", "Activity为空，无法调节亮度")
                        return@launch
                    }
                    
                    // 检查Activity是否已销毁
                    if (activity.isFinishing || activity.isDestroyed) {
                        Log.w("MainViewModel", "Activity已销毁，无法调节亮度")
                        return@launch
                    }
                    
                    lightService.updateBrightness(activity, brightness)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "调节亮度失败: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "调节亮度失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 设置亮度
     */
    fun setBrightness(brightness: Float) {
        adjustBrightness(brightness)
    }
    
    /**
     * 设置左侧亮度
     */
    fun setLeftBrightness(brightness: Float) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                leftBrightness = brightness
            )
        }
    }
    
    /**
     * 设置右侧亮度
     */
    fun setRightBrightness(brightness: Float) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                rightBrightness = brightness
            )
        }
    }
    
    /**
     * 更新分屏配置
     */
    fun updateSplitLightConfig(config: SplitLightConfig) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                splitLightConfig = config
            )
            
            // Note: Split light config is now managed locally in UI state
            
            // 如果当前补光已开启，应用新的分屏配置
            if (currentState.isLightOn) {
                currentActivity?.let { activity ->
                    if (!config.isEnabled) {
                        // 单屏模式
                        lightService.enableLight(
                            activity,
                            currentState.selectedLightType,
                            currentState.brightness
                        )
                    } else {
                        // 分屏模式使用左侧区域的设置
                        lightService.enableLight(
                            activity,
                            config.leftTopType,
                            config.leftTopBrightness
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 拍照
     */
    fun takePhoto() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                cameraManager.capturePhoto(
                    onImageCaptured = { file ->
                        viewModelScope.launch {
                            try {
                                // 通知媒体扫描器扫描新文件
                                currentActivity?.let { context ->
                                    android.media.MediaScannerConnection.scanFile(
                                        context,
                                        arrayOf(file.absolutePath),
                                        arrayOf("image/jpeg")
                                    ) { path, uri ->
                                        android.util.Log.d("MediaScanner", "扫描完成: $path -> $uri")
                                    }
                                }
                                
                                // 保存到相册
                                cameraRepository.saveCaptureResult(
                                    CaptureResult(
                                        isSuccess = true,
                                        imageUri = android.net.Uri.fromFile(file),
                                        errorMessage = null,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                
                                // 调用豆包API进行场景分析
                                analyzeSceneWithDoubao()
                                
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = null
                                )
                            } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "保存照片失败: ${e.message}"
                                )
                            }
                        }
                    },
                    onError = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "拍照失败: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "拍照失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        // 停止定期人数检测
        stopPeriodicPersonDetection()
        // 关闭补光
        currentActivity?.let { activity ->
            if (_uiState.value.isLightOn) {
                lightService.disableLight(activity)
            }
        }
        currentActivity = null
        // 停止传感器监听
        recommendationService.stopSensorListening()
    }
    
    /**
     * 更新姿势推荐
     */
    private fun updatePoseRecommendation() {
        Log.d("MainViewModel", "开始更新姿势推荐")
        val lightAnalysisValue = lightAnalysis.value
        val currentScene = determineShootingScene(lightAnalysisValue)
        val poseType = determinePoseType()
        
        Log.d("MainViewModel", "当前场景: $currentScene, 姿势类型: $poseType")
        
        // 在协程中检测人数并更新状态
        viewModelScope.launch {
            Log.d("MainViewModel", "开始检测人数")
            val personCount = detectPersonCount()
            val isMultiPerson = personCount > 1
            
            Log.d("MainViewModel", "检测到人数: $personCount, 是否多人场景: $isMultiPerson")
            
            _uiState.value = _uiState.value.copy(
                detectedPersonCount = personCount,
                isMultiPersonScene = isMultiPerson
            )
            
            // 只在单人场景下提供姿势推荐
            if (!isMultiPerson) {
                Log.d("MainViewModel", "获取单人姿势推荐")
                _currentPose.value = PoseLibrary.getRecommendedPose(poseType, currentScene)
                Log.d("MainViewModel", "姿势推荐: ${_currentPose.value}")
                
                // 同时触发RecommendationService的姿势推荐更新
                recommendationService.triggerRecommendationAnalysis("portrait")
            } else {
                Log.d("MainViewModel", "多人场景，不提供姿势推荐")
                _currentPose.value = null
                
                // 多人场景：触发RecommendationService的合影推荐
                recommendationService.triggerRecommendationAnalysis("group")
            }
        }
    }
    
    /**
     * 手动触发人数检测和拍摄建议更新
     */
    fun refreshShootingRecommendations() {
        viewModelScope.launch {
            try {
                // 同时更新姿势推荐（会自动调用RecommendationService）
                updatePoseRecommendation()
            } catch (e: Exception) {
                Log.e("MainViewModel", "刷新拍摄建议失败: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "刷新建议失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 开始定期人数检测（用于实时更新）
     */
    private var personDetectionJob: kotlinx.coroutines.Job? = null
    
    fun startPeriodicPersonDetection() {
        // 取消之前的检测任务
        personDetectionJob?.cancel()
        
        personDetectionJob = viewModelScope.launch {
            while (coroutineContext.isActive) {
                try {
                    // 每5秒检测一次人数
                    kotlinx.coroutines.delay(5000)
                    updatePoseRecommendation()
                } catch (e: Exception) {
                    android.util.Log.e("PersonDetection", "定期检测失败: ${e.message}")
                }
            }
        }
    }
    
    fun stopPeriodicPersonDetection() {
        personDetectionJob?.cancel()
        personDetectionJob = null
    }
    
    /**
      * 根据光照分析确定拍摄场景
      */
     private fun determineShootingScene(lightAnalysisValue: Any?): ShootingScene {
         // 从RecommendationService获取LightAnalysis，它有lightLevel属性
         if (lightAnalysisValue is com.travellight.camera.domain.service.LightAnalysis) {
             return when {
                 lightAnalysisValue.lightLevel < 0.2f -> ShootingScene.LOW_LIGHT
                 lightAnalysisValue.lightLevel > 0.7f -> ShootingScene.BRIGHT
                 lightAnalysisValue.lightLevel > 0.4f -> ShootingScene.OUTDOOR
                 else -> ShootingScene.INDOOR
             }
         }
         return ShootingScene.INDOOR
     }
    
    /**
     * 确定姿势类型（目前默认为人像，后续可根据场景识别优化）
     */
    private fun determinePoseType(): com.travellight.camera.data.model.PoseType {
        // 目前使用固定的人像类型，后续可以通过AI识别场景来动态确定
        return com.travellight.camera.data.model.PoseType.PORTRAIT
    }
    
    /**
     * 检测当前场景中的人数
     * 使用豆包多模态理解大模型API进行真实的人员数量检测
     */
    private suspend fun detectPersonCount(): Int {
        return try {
            Log.d("PersonDetection", "开始检测人数")
            // 获取当前相机预览帧
            val imageBase64 = getCameraPreviewFrame()
            
            if (imageBase64.isNullOrEmpty()) {
                Log.w("PersonDetection", "无法获取相机预览帧，使用默认值")
                return 1
            }
            
            Log.d("PersonDetection", "获取到预览帧，长度: ${imageBase64.length}")
            
            // 调用豆包API进行人数检测
            Log.d("PersonDetection", "调用豆包API进行人数检测")
            val result = doubaoApiService.detectPersonCount(imageBase64)
            
            if (result.isSuccess) {
                val response = result.getOrNull()
                val personCount = response?.personCount ?: 1
                val confidence = response?.confidence ?: 0
                
                Log.d("PersonDetection", "检测到 $personCount 人，置信度: $confidence%")
                Log.d("PersonDetection", "检测描述: ${response?.description}")
                
                // 只有在置信度较高时才使用检测结果
                return if (confidence > 60) {
                    personCount
                } else {
                    Log.w("PersonDetection", "置信度较低($confidence%)，使用默认值")
                    1
                }
            } else {
                Log.e("PersonDetection", "豆包API调用失败: ${result.exceptionOrNull()?.message}")
                result.exceptionOrNull()?.printStackTrace()
                return 1
            }
        } catch (e: Exception) {
            Log.e("PersonDetection", "人数检测失败: ${e.message}", e)
            // 检测失败时默认返回1（单人）
            return 1
        }
    }
    
    /**
     * 获取当前相机预览帧并转换为Base64
     */
    private suspend fun getCameraPreviewFrame(): String? {
        return try {
            // 从CameraManager获取当前预览帧
            val base64Image = cameraManager.getCameraPreviewFrame()
            
            if (base64Image != null) {
                Log.d("PersonDetection", "成功获取相机预览帧")
            } else {
                Log.w("PersonDetection", "无法获取相机预览帧")
            }
            
            base64Image
        } catch (e: Exception) {
            Log.e("PersonDetection", "获取相机预览帧失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 初始化相机
     */
    fun initializeCamera(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        viewModelScope.launch {
            val result = cameraManager.initializeCamera(lifecycleOwner, surfaceProvider)
            if (result.isFailure) {
                // 处理初始化错误
                result.exceptionOrNull()?.let { exception ->
                    // 可以在这里添加错误处理逻辑
                }
            }
        }
    }
    
    /**
     * 获取相机状态
     */
    val isCameraInitialized = cameraManager.isCameraInitialized
    val isRecording = cameraManager.isRecording
    val flashMode = cameraManager.flashMode
    
    /**
     * 切换闪光灯
     */
    fun toggleFlash() {
        cameraManager.toggleFlashMode()
    }
    
    /**
     * 切换网格显示
     */
    fun toggleGrid() {
        _uiState.value = _uiState.value.copy(
            isGridEnabled = !_uiState.value.isGridEnabled
        )
    }
    
    /**
     * 切换推荐功能
     */
    fun toggleRecommendation() {
        _uiState.value = _uiState.value.copy(
            isRecommendationEnabled = !_uiState.value.isRecommendationEnabled
        )
    }
    
    /**
     * 获取闪光灯模式名称
     */
    fun getFlashModeName(): String {
        return cameraManager.getFlashModeName()
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 增加亮度
     */
    fun increaseBrightness() {
        val currentBrightness = _uiState.value.brightness
        val newBrightness = (currentBrightness + 0.1f).coerceAtMost(1.0f)
        adjustBrightness(newBrightness)
    }
    
    /**
     * 减少亮度
     */
    fun decreaseBrightness() {
        val currentBrightness = _uiState.value.brightness
        val newBrightness = (currentBrightness - 0.1f).coerceAtLeast(0.0f)
        adjustBrightness(newBrightness)
    }
    
    /**
     * 切换分屏模式
     */
    fun toggleSplitScreen() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                isSplitScreenEnabled = !currentState.isSplitScreenEnabled
            )
        }
    }
    
    /**
     * 录制视频
     */
    fun recordVideo() {
        viewModelScope.launch {
            try {
                if (cameraManager.isRecording.value) {
                    // 停止录像
                    cameraManager.stopRecording()
                } else {
                    // 开始录像
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    
                    cameraManager.startRecording(
                        onVideoRecorded = { file ->
                            viewModelScope.launch {
                                try {
                                    // 保存到相册
                                    cameraRepository.saveCaptureResult(
                                        CaptureResult(
                                            isSuccess = true,
                                            imageUri = android.net.Uri.fromFile(file),
                                            errorMessage = null,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = null
                                    )
                                } catch (e: Exception) {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = "保存视频失败: ${e.message}"
                                    )
                                }
                            }
                        },
                        onError = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "录像失败: ${exception.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "录像失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 打开设置
     */
    fun openSettings() {
        // TODO: 导航到设置页面
    }
    
    /**
     * 打开收藏
     */
    fun openFavorites() {
        // TODO: 导航到收藏页面
    }
    
    /**
     * 打开本地相册
     */
    fun openGallery() {
        currentActivity?.let { activity ->
            try {
                // 使用MediaStore打开相册
                val intent = android.content.Intent(android.content.Intent.ACTION_PICK)
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                
                // 检查是否有可用的应用处理此Intent
                val packageManager = activity.packageManager
                if (intent.resolveActivity(packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    // 备用方案：使用ACTION_VIEW打开图片文件夹
                    val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    fallbackIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "vnd.android.cursor.dir/image")
                    
                    if (fallbackIntent.resolveActivity(packageManager) != null) {
                        activity.startActivity(fallbackIntent)
                    } else {
                        // 最后备用方案：打开文件选择器
                        val fileIntent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
                        fileIntent.type = "image/*"
                        fileIntent.addCategory(android.content.Intent.CATEGORY_OPENABLE)
                        
                        if (fileIntent.resolveActivity(packageManager) != null) {
                            activity.startActivity(android.content.Intent.createChooser(fileIntent, "选择图片"))
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "未找到可用的相册应用"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "无法打开相册: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 释放相机资源
     */
    fun releaseCamera() {
        cameraManager.release()
    }
    
    /**
     * 使用豆包API分析场景并生成拍摄建议
     */
    private fun analyzeSceneWithDoubao() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val lightAnalysisValue = lightAnalysis.value
                
                // 获取当前环境信息
                val lightLevel = lightAnalysisValue?.lightLevel ?: currentState.brightness
                val timeOfDay = getCurrentTimeOfDay()
                val environment = determineEnvironment(lightAnalysisValue)
                
                // 调用场景分析API
                val sceneResult = doubaoApiService.analyzeSceneAndGenerateAdvice(
                    lightLevel = lightLevel,
                    timeOfDay = timeOfDay,
                    environment = environment
                )
                
                sceneResult.onSuccess { response ->
                    val analysisText = "光照建议: ${response.lightingAdvice}\n" +
                            "角度推荐: ${response.angleRecommendation}\n" +
                            "距离建议: ${response.distanceAdvice}\n" +
                            "场景优化: ${response.sceneOptimization}"
                    
                    _uiState.value = _uiState.value.copy(
                        sceneAnalysis = analysisText
                    )
                    
                    // 生成姿势指导
                    generatePoseGuidance(environment)
                }.onFailure { error ->
                    android.util.Log.e("DoubaoAPI", "场景分析失败: ${error.message}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("DoubaoAPI", "场景分析异常: ${e.message}")
            }
        }
    }
    
    /**
     * 生成个性化姿势指导
     */
    private suspend fun generatePoseGuidance(environment: String) {
        try {
            val currentPoseValue = currentPose.value
            val scenario = when {
                environment.contains("室外") -> "户外旅游拍摄"
                environment.contains("室内") -> "室内人像拍摄"
                else -> "一般场景拍摄"
            }
            
            val userPreferences = currentPoseValue?.let {
                "偏好姿势类型: ${it.poseType}, 适用场景: ${it.suitableScenes.joinToString(", ")}"
            } ?: "无特定偏好"
            
            val poseResult = doubaoApiService.generatePoseGuidance(
                scenario = scenario,
                userPreferences = userPreferences
            )
            
            poseResult.onSuccess { response ->
                val guidanceText = "姿势指导: ${response.poseInstructions}\n" +
                        "表情建议: ${response.facialExpression}\n" +
                        "身体位置: ${response.bodyPosition}\n" +
                        "手势建议: ${response.handGestures}\n" +
                        "拍摄技巧: ${response.tips}"
                
                _uiState.value = _uiState.value.copy(
                    poseGuidance = guidanceText
                )
            }.onFailure { error ->
                android.util.Log.e("DoubaoAPI", "姿势指导生成失败: ${error.message}")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("DoubaoAPI", "姿势指导生成异常: ${e.message}")
        }
    }
    
    /**
     * 获取当前时间段
     */
    private fun getCurrentTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "上午"
            in 12..17 -> "下午"
            in 18..22 -> "傍晚"
            else -> "夜晚"
        }
    }
    
    /**
     * 判断当前环境类型
     */
    private fun determineEnvironment(lightAnalysisValue: Any?): String {
        val lightLevel = (lightAnalysisValue as? com.travellight.camera.domain.service.LightAnalysis)?.lightLevel ?: 0.5f
        return when {
            lightLevel > 0.7f -> "室外明亮环境"
            lightLevel > 0.3f -> "室内中等光线"
            else -> "室内昏暗环境"
        }
    }
}