package com.travellight.camera.test

import android.util.Log
import com.travellight.camera.data.api.DoubaoApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 豆包API测试类
 * 用于验证API配置和调用功能
 */
@Singleton
class DoubaoApiTest @Inject constructor(
    private val doubaoApiService: DoubaoApiService
) {
    
    companion object {
        private const val TAG = "DoubaoApiTest"
    }
    
    /**
     * 测试场景分析API调用
     */
    fun testSceneAnalysis() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始测试场景分析API...")
                
                val result = doubaoApiService.analyzeSceneAndGenerateAdvice(
                    lightLevel = 65.0f,
                    timeOfDay = "下午",
                    environment = "室内"
                )
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "场景分析API调用成功:")
                        Log.d(TAG, "补光建议: ${response.lightingAdvice}")
                        Log.d(TAG, "角度建议: ${response.angleRecommendation}")
                        Log.d(TAG, "距离建议: ${response.distanceAdvice}")
                        Log.d(TAG, "场景优化: ${response.sceneOptimization}")
                        Log.d(TAG, "光线类型: ${response.lightType}")
                        Log.d(TAG, "亮度值: ${response.brightness}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "场景分析API调用失败: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "场景分析测试异常: ${e.message}", e)
            }
        }
    }
    
    /**
     * 测试姿势指导API调用
     */
    fun testPoseGuidance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始测试姿势指导API...")
                
                val result = doubaoApiService.generatePoseGuidance(
                    scenario = "经典人像拍摄",
                    userPreferences = "自然风格，微笑表情"
                )
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "姿势指导API调用成功:")
                        Log.d(TAG, "姿势指导: ${response.poseInstructions}")
                        Log.d(TAG, "面部表情: ${response.facialExpression}")
                        Log.d(TAG, "身体位置: ${response.bodyPosition}")
                        Log.d(TAG, "手部姿势: ${response.handGestures}")
                        Log.d(TAG, "拍摄贴士: ${response.tips}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "姿势指导API调用失败: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "姿势指导测试异常: ${e.message}", e)
            }
        }
    }
    
    /**
     * 测试API配置
     */
    suspend fun testApiConfiguration() {
        Log.d(TAG, "测试API配置...")
        Log.d(TAG, "API端点: https://ark.cn-beijing.volces.com/api/v3/chat/completions")
        Log.d(TAG, "API密钥已配置: ${System.getenv("ARK_API_KEY") != null}")
        Log.d(TAG, "API配置测试完成")
    }
    
    /**
     * 测试场景分析API
     */
    suspend fun testSceneAnalysisApi() {
        Log.d(TAG, "测试场景分析API...")
        try {
            val result = doubaoApiService.analyzeSceneAndGenerateAdvice(
                lightLevel = 60.0f,
                timeOfDay = "下午",
                environment = "室内"
            )
            Log.d(TAG, "场景分析API调用成功: $result")
        } catch (e: Exception) {
            Log.e(TAG, "场景分析API调用失败: ${e.message}", e)
        }
    }
    
    /**
     * 测试姿势指导API
     */
    suspend fun testPoseGuidanceApi() {
        Log.d(TAG, "测试姿势指导API...")
        try {
            val result = doubaoApiService.generatePoseGuidance(
                scenario = "室内人像拍摄",
                userPreferences = "自然风格，温馨氛围"
            )
            Log.d(TAG, "姿势指导API调用成功: $result")
        } catch (e: Exception) {
            Log.e(TAG, "姿势指导API调用失败: ${e.message}", e)
        }
    }
}