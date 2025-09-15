package com.travellight.camera.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.travellight.camera.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Room数据库主类
 */
@Database(
    entities = [
        Photo::class,
        UserSettings::class,
        PhotoHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
}

/**
 * 照片数据访问对象
 */
@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<Photo>>
    
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: String): Photo?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)
    
    @Update
    suspend fun updatePhoto(photo: Photo)
    
    @Delete
    suspend fun deletePhoto(photo: Photo)
    
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: String)
    
    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePhotos(): Flow<List<Photo>>
    
    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)
    
    @Query("SELECT * FROM photos WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getPhotosByDateRange(startDate: Date, endDate: Date): Flow<List<Photo>>
    
    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getPhotoCount(): Int
    
    @Query("SELECT COUNT(*) FROM photos WHERE isFavorite = 1")
    suspend fun getFavoritePhotoCount(): Int
    
    @Query("SELECT SUM(fileSize) FROM photos")
    suspend fun getTotalFileSize(): Long?
    
    @Query("SELECT AVG(fileSize) FROM photos")
    suspend fun getAverageFileSize(): Long?
}

/**
 * 设置数据访问对象
 */
@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 'default'")
    fun getUserSettings(): Flow<UserSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)
    
    @Update
    suspend fun updateSettings(settings: UserSettings)
    
    @Query("DELETE FROM user_settings")
    suspend fun clearSettings()
}

/**
 * 历史记录数据访问对象
 */
@Dao
interface HistoryDao {
    @Query("SELECT * FROM photo_history WHERE photoId = :photoId ORDER BY timestamp DESC")
    fun getPhotoHistory(photoId: String): Flow<List<PhotoHistory>>
    
    @Query("SELECT * FROM photo_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PhotoHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PhotoHistory)
    
    @Query("DELETE FROM photo_history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM photo_history WHERE timestamp < :timestamp")
    suspend fun clearHistoryBefore(timestamp: Date)
    
    @Query("DELETE FROM photo_history WHERE photoId = :photoId")
    suspend fun clearPhotoHistory(photoId: String)
}

/**
 * 类型转换器
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let {
            "${it.latitude},${it.longitude},${it.address ?: ""},${it.city ?: ""},${it.country ?: ""}"
        }
    }
    
    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        return locationString?.let {
            val parts = it.split(",")
            if (parts.size >= 2) {
                Location(
                    latitude = parts[0].toDoubleOrNull() ?: 0.0,
                    longitude = parts[1].toDoubleOrNull() ?: 0.0,
                    address = parts.getOrNull(2)?.takeIf { it.isNotEmpty() },
                    city = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
                    country = parts.getOrNull(4)?.takeIf { it.isNotEmpty() }
                )
            } else null
        }
    }
    
    @TypeConverter
    fun fromLightSettings(settings: LightSettings?): String? {
        return settings?.let {
            "${it.type.name},${it.brightness},${it.isEnabled},${it.autoAdjust},${it.colorTemperature ?: ""}"
        }
    }
    
    @TypeConverter
    fun toLightSettings(settingsString: String?): LightSettings? {
        return settingsString?.let {
            val parts = it.split(",")
            if (parts.size >= 4) {
                LightSettings(
                    type = LightType.valueOf(parts[0]),
                    brightness = parts[1].toFloatOrNull() ?: 50f,
                    isEnabled = parts[2].toBooleanStrictOrNull() ?: false,
                    autoAdjust = parts[3].toBooleanStrictOrNull() ?: false,
                    colorTemperature = parts.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toIntOrNull()
                )
            } else null
        }
    }
    
    @TypeConverter
    fun fromCameraSettings(settings: CameraSettings?): String? {
        return settings?.let {
            "${it.resolution.width}x${it.resolution.height},${it.quality.name},${it.flashMode.name},${it.focusMode.name},${it.whiteBalance.name},${it.iso ?: ""},${it.exposureTime ?: ""},${it.aperture ?: ""}"
        }
    }
    
    @TypeConverter
    fun toCameraSettings(settingsString: String?): CameraSettings? {
        return settingsString?.let {
            val parts = it.split(",")
            if (parts.size >= 5) {
                val resolutionParts = parts[0].split("x")
                if (resolutionParts.size == 2) {
                    CameraSettings(
                        resolution = Resolution(
                            width = resolutionParts[0].toIntOrNull() ?: 1920,
                            height = resolutionParts[1].toIntOrNull() ?: 1080
                        ),
                        quality = PhotoQuality.valueOf(parts[1]),
                        flashMode = FlashMode.valueOf(parts[2]),
                        focusMode = FocusMode.valueOf(parts[3]),
                        whiteBalance = WhiteBalance.valueOf(parts[4]),
                        iso = parts.getOrNull(5)?.takeIf { it.isNotEmpty() }?.toIntOrNull(),
                        exposureTime = parts.getOrNull(6)?.takeIf { it.isNotEmpty() }?.toLongOrNull(),
                        aperture = parts.getOrNull(7)?.takeIf { it.isNotEmpty() }?.toFloatOrNull()
                    )
                } else null
            } else null
        }
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    @TypeConverter
    fun fromLightType(type: LightType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toLightType(typeName: String?): LightType? {
        return typeName?.let { LightType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromPhotoQuality(quality: PhotoQuality?): String? {
        return quality?.name
    }
    
    @TypeConverter
    fun toPhotoQuality(qualityName: String?): PhotoQuality? {
        return qualityName?.let { PhotoQuality.valueOf(it) }
    }
    
    @TypeConverter
    fun fromHistoryAction(action: HistoryAction?): String? {
        return action?.name
    }
    
    @TypeConverter
    fun toHistoryAction(actionName: String?): HistoryAction? {
        return actionName?.let { HistoryAction.valueOf(it) }
    }
}

/**
 * 数据库迁移
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 示例迁移：添加新列
        // database.execSQL("ALTER TABLE photos ADD COLUMN newColumn TEXT")
    }
}