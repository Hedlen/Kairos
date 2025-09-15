package com.travellight.camera.domain.repository

import com.travellight.camera.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 照片数据仓库接口
 */
interface PhotoRepository {
    /**
     * 获取所有照片
     */
    fun getAllPhotos(): Flow<List<Photo>>
    
    /**
     * 根据ID获取照片
     */
    suspend fun getPhotoById(id: String): Photo?
    
    /**
     * 保存照片
     */
    suspend fun savePhoto(photo: Photo): Result<String>
    
    /**
     * 删除照片
     */
    suspend fun deletePhoto(id: String): Result<Unit>
    
    /**
     * 更新照片信息
     */
    suspend fun updatePhoto(photo: Photo): Result<Unit>
    
    /**
     * 获取收藏的照片
     */
    fun getFavoritePhotos(): Flow<List<Photo>>
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(id: String): Result<Unit>
    
    /**
     * 根据标签搜索照片
     */
    fun searchPhotosByTags(tags: List<String>): Flow<List<Photo>>
    
    /**
     * 根据日期范围获取照片
     */
    fun getPhotosByDateRange(startDate: Long, endDate: Long): Flow<List<Photo>>
    
    /**
     * 获取照片统计信息
     */
    suspend fun getPhotoStats(): PhotoStats
}

/**
 * 设置数据仓库接口
 */
interface SettingsRepository {
    /**
     * 获取用户设置
     */
    fun getUserSettings(): Flow<UserSettings>
    
    /**
     * 更新用户设置
     */
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
    
    /**
     * 重置为默认设置
     */
    suspend fun resetToDefaults(): Result<Unit>
    
    /**
     * 导出设置
     */
    suspend fun exportSettings(): Result<String>
    
    /**
     * 导入设置
     */
    suspend fun importSettings(settingsJson: String): Result<Unit>
}

/**
 * 推荐系统仓库接口
 */
interface RecommendationRepository {
    /**
     * 根据环境信息获取推荐
     */
    suspend fun getRecommendations(environmentInfo: EnvironmentInfo): Result<List<PhotoRecommendation>>
    
    /**
     * 获取所有可用推荐
     */
    suspend fun getAllRecommendations(): Result<List<PhotoRecommendation>>
    
    /**
     * 根据场景获取推荐
     */
    suspend fun getRecommendationsByScenario(scenario: PhotoScenario): Result<List<PhotoRecommendation>>
    
    /**
     * 更新推荐数据
     */
    suspend fun updateRecommendations(): Result<Unit>
    
    /**
     * 记录推荐使用情况
     */
    suspend fun recordRecommendationUsage(recommendationId: String): Result<Unit>
}

/**
 * 环境感知仓库接口
 */
interface EnvironmentRepository {
    /**
     * 获取当前环境信息
     */
    suspend fun getCurrentEnvironment(): Result<EnvironmentInfo>
    
    /**
     * 开始环境监测
     */
    suspend fun startEnvironmentMonitoring(): Result<Unit>
    
    /**
     * 停止环境监测
     */
    suspend fun stopEnvironmentMonitoring(): Result<Unit>
    
    /**
     * 获取环境变化流
     */
    fun getEnvironmentUpdates(): Flow<EnvironmentInfo>
    
    /**
     * 校准环境传感器
     */
    suspend fun calibrateSensors(): Result<Unit>
}

/**
 * 相机控制仓库接口
 */
interface CameraRepository {
    /**
     * 初始化相机
     */
    suspend fun initializeCamera(): Result<Unit>
    
    /**
     * 释放相机资源
     */
    suspend fun releaseCamera(): Result<Unit>
    
    /**
     * 拍照
     */
    suspend fun capturePhoto(settings: CameraSettings): Result<String>
    
    /**
     * 获取可用分辨率
     */
    suspend fun getAvailableResolutions(): Result<List<Resolution>>
    
    /**
     * 设置相机参数
     */
    suspend fun applyCameraSettings(settings: CameraSettings): Result<Unit>
    
    /**
     * 获取相机状态
     */
    fun getCameraState(): Flow<CameraState>
    
    /**
     * 开始预览
     */
    suspend fun startPreview(): Result<Unit>
    
    /**
     * 停止预览
     */
    suspend fun stopPreview(): Result<Unit>
    
    /**
     * 切换前后摄像头
     */
    suspend fun switchCamera(): Result<Unit>
}

/**
 * 补光控制仓库接口
 */
interface LightRepository {
    /**
     * 开启补光
     */
    suspend fun enableLight(settings: LightSettings): Result<Unit>
    
    /**
     * 关闭补光
     */
    suspend fun disableLight(): Result<Unit>
    
    /**
     * 调整补光亮度
     */
    suspend fun adjustBrightness(brightness: Float): Result<Unit>
    
    /**
     * 切换补光类型
     */
    suspend fun switchLightType(type: LightType): Result<Unit>
    
    /**
     * 获取补光状态
     */
    fun getLightState(): Flow<LightState>
    
    /**
     * 自动调节补光
     */
    suspend fun autoAdjustLight(environmentInfo: EnvironmentInfo): Result<LightSettings>
    
    /**
     * 获取支持的补光类型
     */
    suspend fun getSupportedLightTypes(): Result<List<LightType>>
}

/**
 * 历史记录仓库接口
 */
interface HistoryRepository {
    /**
     * 添加历史记录
     */
    suspend fun addHistory(history: PhotoHistory): Result<Unit>
    
    /**
     * 获取照片的历史记录
     */
    fun getPhotoHistory(photoId: String): Flow<List<PhotoHistory>>
    
    /**
     * 获取所有历史记录
     */
    fun getAllHistory(): Flow<List<PhotoHistory>>
    
    /**
     * 清除历史记录
     */
    suspend fun clearHistory(): Result<Unit>
    
    /**
     * 删除指定时间之前的历史记录
     */
    suspend fun clearHistoryBefore(timestamp: Long): Result<Unit>
}

/**
 * 存储管理仓库接口
 */
interface StorageRepository {
    /**
     * 获取存储空间信息
     */
    suspend fun getStorageInfo(): Result<StorageInfo>
    
    /**
     * 清理缓存
     */
    suspend fun clearCache(): Result<Unit>
    
    /**
     * 清理临时文件
     */
    suspend fun clearTempFiles(): Result<Unit>
    
    /**
     * 导出照片到外部存储
     */
    suspend fun exportPhoto(photoId: String, destinationPath: String): Result<Unit>
    
    /**
     * 批量导出照片
     */
    suspend fun exportPhotos(photoIds: List<String>, destinationPath: String): Result<Unit>
    
    /**
     * 备份数据
     */
    suspend fun backupData(): Result<String>
    
    /**
     * 恢复数据
     */
    suspend fun restoreData(backupPath: String): Result<Unit>
}

/**
 * 数据传输对象
 */
data class PhotoStats(
    val totalPhotos: Int,
    val favoritePhotos: Int,
    val totalSize: Long,
    val averageSize: Long,
    val mostUsedLightType: LightType?,
    val mostUsedScenario: PhotoScenario?
)

data class CameraState(
    val isInitialized: Boolean,
    val isPreviewActive: Boolean,
    val currentCamera: CameraType,
    val availableCameras: List<CameraType>,
    val currentSettings: CameraSettings?
)

data class LightState(
    val isEnabled: Boolean,
    val currentSettings: LightSettings?,
    val supportedTypes: List<LightType>,
    val batteryLevel: Float? = null
)

data class StorageInfo(
    val totalSpace: Long,
    val availableSpace: Long,
    val usedSpace: Long,
    val cacheSize: Long,
    val tempFilesSize: Long
)

enum class CameraType {
    FRONT,
    BACK
}