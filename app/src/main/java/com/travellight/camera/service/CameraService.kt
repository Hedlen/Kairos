package com.travellight.camera.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.annotation.SuppressLint
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 相机服务类，负责Camera2 API的集成和管理
 */
@Singleton
class CameraService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CameraService"
        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080
    }

    // 相机状态
    private val _cameraState = MutableStateFlow(CameraState.CLOSED)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    // 拍照状态
    private val _captureState = MutableStateFlow(CaptureState.IDLE)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    // 相机管理器和设备
    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    // 后台线程处理
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    // 预览相关
    private var previewSize: Size? = null
    private var cameraId: String? = null
    private var previewSurface: Surface? = null

    /**
     * 相机状态枚举
     */
    enum class CameraState {
        CLOSED,
        OPENING,
        OPENED,
        PREVIEWING,
        ERROR
    }

    /**
     * 拍照状态枚举
     */
    enum class CaptureState {
        IDLE,
        CAPTURING,
        CAPTURED,
        ERROR
    }

    /**
     * 拍照结果数据类
     */
    data class CaptureResult(
        val success: Boolean,
        val filePath: String? = null,
        val error: String? = null
    )

    /**
     * 检查相机权限
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 启动后台线程
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    /**
     * 停止后台线程
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error stopping background thread", e)
        }
    }

    /**
     * 获取最佳预览尺寸
     */
    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size {
        val bigEnough = mutableListOf<Size>()
        val notBigEnough = mutableListOf<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * h / w
            ) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        return when {
            bigEnough.isNotEmpty() -> bigEnough.minByOrNull { it.width * it.height }!!
            notBigEnough.isNotEmpty() -> notBigEnough.maxByOrNull { it.width * it.height }!!
            else -> {
                Log.e(TAG, "Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }

    /**
     * 设置相机
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                
                // 我们使用后置摄像头
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue

                // 设置图像读取器用于拍照
                val largest = map.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.width * it.height }!!
                imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 1)
                imageReader?.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)

                // 选择预览尺寸
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    width, height, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, largest
                )

                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error setting up camera outputs", e)
            _cameraState.value = CameraState.ERROR
        }
    }

    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    fun openCamera(width: Int, height: Int, surface: Surface) {
        if (!hasCameraPermission()) {
            Log.e(TAG, "Camera permission not granted")
            _cameraState.value = CameraState.ERROR
            return
        }

        startBackgroundThread()
        setUpCameraOutputs(width, height)
        previewSurface = surface

        try {
            _cameraState.value = CameraState.OPENING
            cameraManager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error opening camera", e)
            _cameraState.value = CameraState.ERROR
        }
    }

    /**
     * 关闭相机
     */
    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        stopBackgroundThread()
        _cameraState.value = CameraState.CLOSED
    }

    /**
     * 创建预览会话
     */
    private fun createCameraPreviewSession() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                createCameraPreviewSessionNew()
            } else {
                createCameraPreviewSessionLegacy()
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error creating camera preview session", e)
            _cameraState.value = CameraState.ERROR
        }
    }
    
    /**
     * 创建预览会话 (API 28+)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun createCameraPreviewSessionNew() {
        val surfaces = listOf(previewSurface!!, imageReader?.surface!!)
        val outputConfigurations = surfaces.map { OutputConfiguration(it) }
        
        val sessionConfiguration = SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            outputConfigurations,
            context.mainExecutor,
            captureSessionCallback
        )
        
        cameraDevice?.createCaptureSession(sessionConfiguration)
    }
    
    /**
     * 创建预览会话 (API < 28)
     */
    private fun createCameraPreviewSessionLegacy() {
        val surfaces = listOf(previewSurface!!, imageReader?.surface!!)
        cameraDevice?.createCaptureSession(surfaces, captureSessionCallback, backgroundHandler)
    }

    /**
     * 拍照
     */
    fun capturePhoto(): Boolean {
        if (cameraState.value != CameraState.PREVIEWING) {
            Log.w(TAG, "Camera not ready for capture")
            return false
        }

        try {
            _captureState.value = CaptureState.CAPTURING
            
            val reader = imageReader ?: return false
            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(reader.surface)
            
            // 设置自动对焦和自动曝光
            captureBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            captureBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    Log.d(TAG, "Photo capture completed")
                }
                
                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    Log.e(TAG, "Photo capture failed: ${failure.reason}")
                    _captureState.value = CaptureState.ERROR
                }
            }
            
            captureSession?.capture(captureBuilder?.build()!!, captureCallback, backgroundHandler)
            return true
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error capturing photo", e)
            _captureState.value = CaptureState.ERROR
            return false
        }
    }

    /**
     * 相机状态回调
     */
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            _cameraState.value = CameraState.OPENED
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
            _cameraState.value = CameraState.CLOSED
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
            _cameraState.value = CameraState.ERROR
            Log.e(TAG, "Camera error: $error")
        }
    }

    /**
     * 捕获会话回调
     */
    private val captureSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            if (cameraDevice == null) return
            
            captureSession = session
            try {
                val previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder?.addTarget(previewSurface!!)
                
                // 设置自动对焦
                previewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                
                val previewRequest = previewRequestBuilder?.build()
                session.setRepeatingRequest(previewRequest!!, null, backgroundHandler)
                _cameraState.value = CameraState.PREVIEWING
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error starting camera preview", e)
                _cameraState.value = CameraState.ERROR
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "Camera session configuration failed")
            _cameraState.value = CameraState.ERROR
        }
    }

    /**
     * 图像可用监听器
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        backgroundHandler?.post {
            val image = reader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_${timeStamp}.jpg"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            try {
                FileOutputStream(file).use { output ->
                    output.write(bytes)
                }
                Log.d(TAG, "Photo saved: ${file.absolutePath}")
                _captureState.value = CaptureState.CAPTURED
            } catch (e: IOException) {
                Log.e(TAG, "Error saving photo", e)
                _captureState.value = CaptureState.ERROR
            } finally {
                image.close()
            }
        }
    }

    /**
     * 获取预览尺寸
     */
    fun getPreviewSize(): Size? = previewSize

    /**
     * 重置拍照状态
     */
    fun resetCaptureState() {
        _captureState.value = CaptureState.IDLE
    }
}