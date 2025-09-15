package com.travellight.camera.di

import android.content.Context
import com.travellight.camera.service.CameraService
import com.travellight.camera.data.camera.CameraManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 相机模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    /**
     * 提供CameraService单例
     */
    @Provides
    @Singleton
    fun provideCameraService(
        @ApplicationContext context: Context
    ): CameraService {
        return CameraService(context)
    }
    
    /**
     * 提供CameraManager单例
     */
    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context
    ): CameraManager {
        return CameraManager(context)
    }
}