package com.travellight.camera.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 相机管理器
 * 负责CameraX的初始化、预览、拍照和录像功能
 */
@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var recording: Recording? = null
    
    // 用于存储最新的预览帧
    private var latestImageProxy: ImageProxy? = null

    // 相机状态
    private val _isCameraInitialized = MutableStateFlow(false)
    val isCameraInitialized: StateFlow<Boolean> = _isCameraInitialized.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_AUTO)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    /**
     * 初始化相机
     */
    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ): Result<Unit> {
        return try {
            Log.d("CameraManager", "开始初始化相机")

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()

            // 设置预览
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            // 设置图像捕获
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(_flashMode.value)
                .build()

            // 设置视频捕获
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)
            
            // 设置图像分析用例（用于获取预览帧）
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        Log.d("CameraManager", "收到新的预览帧: ${imageProxy.width}x${imageProxy.height}, 格式: ${imageProxy.format}")
                        // 保存最新的图像帧
                        latestImageProxy?.close()
                        latestImageProxy = imageProxy
                        // 注意：这里不调用imageProxy.close()，因为我们需要保留它用于后续处理
                    }
                }

            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // 绑定用例到生命周期
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture,
                imageAnalysis
            )

            _isCameraInitialized.value = true
            Log.d("CameraManager", "相机初始化成功")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CameraManager", "相机初始化失败", e)
            _isCameraInitialized.value = false
            Result.failure(e)
        }
    }

    /**
     * 拍照
     */
    fun capturePhoto(
        onImageCaptured: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(IllegalStateException("相机未初始化"))
            return
        }

        // 创建输出文件
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 拍照
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraManager", "照片保存成功: ${photoFile.absolutePath}")
                    onImageCaptured(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "拍照失败", exception)
                    onError(exception)
                }
            }
        )
    }

    /**
     * 开始录像
     */
    fun startRecording(
        onVideoRecorded: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val videoCapture = videoCapture ?: run {
            onError(IllegalStateException("相机未初始化"))
            return
        }

        if (recording != null) {
            Log.w("CameraManager", "已在录像中")
            return
        }

        // 创建输出文件
        val videoFile = createVideoFile()
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        // 开始录像
        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d("CameraManager", "开始录像")
                        _isRecording.value = true
                    }

                    is VideoRecordEvent.Finalize -> {
                        Log.d("CameraManager", "录像完成")
                        _isRecording.value = false
                        if (!recordEvent.hasError()) {
                            onVideoRecorded(videoFile)
                        } else {
                            onError(Exception("录像失败: ${recordEvent.error}"))
                        }
                        recording = null
                    }
                }
            }
    }

    /**
     * 停止录像
     */
    fun stopRecording() {
        recording?.stop()
        recording = null
        _isRecording.value = false
    }

    /**
     * 切换闪光灯模式
     */
    fun toggleFlashMode() {
        val newMode = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
            else -> ImageCapture.FLASH_MODE_AUTO
        }

        _flashMode.value = newMode
        imageCapture?.flashMode = newMode
    }

    /**
     * 获取闪光灯模式名称
     */
    fun getFlashModeName(): String {
        return when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> "关闭"
            ImageCapture.FLASH_MODE_AUTO -> "自动"
            ImageCapture.FLASH_MODE_ON -> "开启"
            else -> "自动"
        }
    }
    
    /**
     * 获取当前相机预览帧并转换为Base64
     */
    suspend fun getCameraPreviewFrame(): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val imageProxy = latestImageProxy
                if (imageProxy == null) {
                    Log.w("CameraManager", "没有可用的预览帧")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                
                // 将ImageProxy转换为Bitmap
                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap == null) {
                    Log.w("CameraManager", "无法将ImageProxy转换为Bitmap")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                
                // 将Bitmap转换为Base64
                val base64String = bitmapToBase64(bitmap)
                Log.d("CameraManager", "成功获取预览帧，Base64长度: ${base64String?.length ?: 0}")
                continuation.resume(base64String)
                
            } catch (e: Exception) {
                Log.e("CameraManager", "获取预览帧失败: ${e.message}", e)
                continuation.resume(null)
            }
        }
    }
    
    /**
     * 将ImageProxy转换为Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            Log.d("CameraManager", "开始转换ImageProxy: ${imageProxy.width}x${imageProxy.height}, 格式: ${imageProxy.format}")
            
            when (imageProxy.format) {
                ImageFormat.YUV_420_888 -> {
                    // YUV_420_888格式转换
                    val planes = imageProxy.planes
                    val yPlane = planes[0]
                    val uPlane = planes[1]
                    val vPlane = planes[2]
                    
                    val yBuffer = yPlane.buffer
                    val uBuffer = uPlane.buffer
                    val vBuffer = vPlane.buffer
                    
                    val ySize = yBuffer.remaining()
                    val uSize = uBuffer.remaining()
                    val vSize = vBuffer.remaining()
                    
                    Log.d("CameraManager", "YUV平面信息 - Y: $ySize, U: $uSize, V: $vSize")
                    Log.d("CameraManager", "像素步长 - Y: ${yPlane.pixelStride}, U: ${uPlane.pixelStride}, V: ${vPlane.pixelStride}")
                    Log.d("CameraManager", "行步长 - Y: ${yPlane.rowStride}, U: ${uPlane.rowStride}, V: ${vPlane.rowStride}")
                    
                    // 计算实际需要的数据大小
                    val width = imageProxy.width
                    val height = imageProxy.height
                    val yDataSize = width * height
                    val uvDataSize = yDataSize / 2
                    
                    val nv21 = ByteArray(yDataSize + uvDataSize)
                    
                    // 复制Y平面数据
                    if (yPlane.pixelStride == 1) {
                        yBuffer.get(nv21, 0, ySize)
                    } else {
                        // 处理Y平面有像素步长的情况
                        val yData = ByteArray(ySize)
                        yBuffer.get(yData)
                        var yIndex = 0
                        for (row in 0 until height) {
                            for (col in 0 until width) {
                                nv21[yIndex++] = yData[row * yPlane.rowStride + col * yPlane.pixelStride]
                            }
                        }
                    }
                    
                    // 处理UV平面数据，转换为NV21格式（VUVUVU...）
                    if (uSize > 0 && vSize > 0) {
                        val uData = ByteArray(uSize)
                        val vData = ByteArray(vSize)
                        uBuffer.get(uData)
                        vBuffer.get(vData)
                        
                        var uvIndex = yDataSize
                        val uvWidth = width / 2
                        val uvHeight = height / 2
                        
                        for (row in 0 until uvHeight) {
                            for (col in 0 until uvWidth) {
                                val uIdx = row * uPlane.rowStride + col * uPlane.pixelStride
                                val vIdx = row * vPlane.rowStride + col * vPlane.pixelStride
                                
                                if (vIdx < vData.size && uIdx < uData.size && uvIndex < nv21.size - 1) {
                                    nv21[uvIndex++] = vData[vIdx]  // V在前
                                    nv21[uvIndex++] = uData[uIdx]  // U在后
                                }
                            }
                        }
                        Log.d("CameraManager", "UV数据处理完成，处理了 ${uvIndex - yDataSize} 字节")
                    } else {
                        Log.w("CameraManager", "UV数据为空，跳过UV处理")
                        // 填充默认的UV数据（灰度图像）
                        for (i in yDataSize until nv21.size) {
                            nv21[i] = 128.toByte()  // 中性灰度值
                        }
                    }
                    
                    val yuvImage = YuvImage(
                        nv21,
                        ImageFormat.NV21,
                        imageProxy.width,
                        imageProxy.height,
                        null
                    )
                    
                    val outputStream = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(
                        Rect(0, 0, imageProxy.width, imageProxy.height),
                        80,
                        outputStream
                    )
                    val jpegBytes = outputStream.toByteArray()
                    Log.d("CameraManager", "YUV转JPEG成功，大小: ${jpegBytes.size}")
                    BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                }
                ImageFormat.JPEG -> {
                    // JPEG格式直接解码
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    Log.d("CameraManager", "JPEG直接解码，大小: ${bytes.size}")
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                else -> {
                    Log.w("CameraManager", "不支持的图像格式: ${imageProxy.format}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("CameraManager", "ImageProxy转Bitmap失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 将Bitmap转换为Base64字符串
     */
    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            // 压缩图片以减少数据大小
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("CameraManager", "Bitmap转Base64失败: ${e.message}", e)
            null
        }
    }

    /**
     * 释放相机资源
     */
    fun release() {
        Log.d("CameraManager", "释放相机资源")
        recording?.stop()
        recording = null
        latestImageProxy?.close()
        latestImageProxy = null
        cameraProvider?.unbindAll()
        _isCameraInitialized.value = false
        _isRecording.value = false
    }

    /**
     * 创建图片文件
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "TravelLight_${timeStamp}.jpg"
        // 保存到公共相册目录
        val storageDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
        val appDir = File(storageDir, "TravelLight")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return File(appDir, imageFileName)
    }

    /**
     * 创建视频文件
     */
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "TravelLight_${timeStamp}.mp4"
        // 保存到公共视频目录
        val storageDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
        val appDir = File(storageDir, "TravelLight")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return File(appDir, videoFileName)
    }
}