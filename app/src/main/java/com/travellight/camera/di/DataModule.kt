package com.travellight.camera.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.travellight.camera.data.database.AppDatabase
import com.travellight.camera.data.database.HistoryDao
import com.travellight.camera.data.database.PhotoDao
import com.travellight.camera.data.database.SettingsDao
import com.travellight.camera.data.repository.SettingsRepositoryImpl
import com.travellight.camera.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据模块依赖注入配置
 * 提供Repository和数据相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    /**
     * 提供Room数据库实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "travel_light_database"
        ).build()
    }
    
    /**
     * 提供PhotoDao
     */
    @Provides
    fun providePhotoDao(database: AppDatabase): PhotoDao {
        return database.photoDao()
    }
    
    /**
     * 提供HistoryDao
     */
    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }
    
    /**
     * 提供SettingsDao
     */
    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    /**
     * 提供设置Repository实现
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(): SettingsRepository {
        return SettingsRepositoryImpl()
    }
}