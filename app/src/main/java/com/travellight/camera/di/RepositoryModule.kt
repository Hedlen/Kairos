package com.travellight.camera.di

import com.travellight.camera.data.repository.CameraRepository
import com.travellight.camera.data.repository.CameraRepositoryImpl
import com.travellight.camera.data.repository.HistoryRepositoryImpl
import com.travellight.camera.data.repository.LightRepository
import com.travellight.camera.data.repository.LightRepositoryImpl
import com.travellight.camera.data.repository.RecommendationRepository
import com.travellight.camera.data.repository.RecommendationRepositoryImpl
import com.travellight.camera.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository模块 - 提供数据仓库的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindLightRepository(
        lightRepositoryImpl: LightRepositoryImpl
    ): LightRepository
    
    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraRepositoryImpl: CameraRepositoryImpl
    ): CameraRepository
    
    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(
        recommendationRepositoryImpl: RecommendationRepositoryImpl
    ): RecommendationRepository
    
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository
}