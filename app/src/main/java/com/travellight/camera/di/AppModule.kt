package com.travellight.camera.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt依赖注入模块
 * 提供应用级别的单例依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * DataStore扩展属性
     */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "travel_light_preferences"
    )
    
    /**
     * 提供DataStore实例
     * 用于存储用户设置和应用配置
     */
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}