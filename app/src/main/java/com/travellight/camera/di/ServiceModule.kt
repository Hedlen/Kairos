package com.travellight.camera.di

import android.content.Context
import com.travellight.camera.domain.service.LightService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 服务层依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideLightService(
        @ApplicationContext context: Context
    ): LightService {
        return LightService(context)
    }
}