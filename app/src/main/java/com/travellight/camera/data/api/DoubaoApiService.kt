package com.travellight.camera.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 豆包多模态理解大模型API服务
 * 基于火山引擎豆包大模型API
 */
@Singleton
class DoubaoApiService @Inject constructor() {
    
    companion object {
        private const val TAG = "DoubaoApiService"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    // 火山引擎豆包大模型API配置
    private val baseUrl = "https://ark.cn-beijing.volces.com/api/v3/"
    private val apiKey = System.getenv("ARK_API_KEY") ?: "e7572adb-3288-400c-8b2e-6e95335af07f" // 从环境变量获取API密钥
    private val modelEndpoint = "doubao-seed-1-6-vision-250815" // 豆包视觉理解模型
    
    init {
        Log.d(TAG, "DoubaoApiService初始化，API Key: ${apiKey.take(10)}...")
        Log.d(TAG, "Base URL: $baseUrl")
        Log.d(TAG, "Model Endpoint: $modelEndpoint")
    }
    
    /**
     * 分析场景并生成拍摄建议
     */
    suspend fun analyzeSceneAndGenerateAdvice(
        lightLevel: Float,
        timeOfDay: String,
        environment: String
    ): Result<SceneAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = createSceneAnalysisRequest(lightLevel, timeOfDay, environment)
            val request = Request.Builder()
                .url("${baseUrl}chat/completions")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val analysisResponse = parseSceneAnalysisResponse(responseBody)
                Result.success(analysisResponse)
            } else {
                Result.failure(Exception("API调用失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成个性化姿势指导
     */
    suspend fun generatePoseGuidance(
        scenario: String,
        userPreferences: String
    ): Result<PoseGuidanceResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = createPoseGuidanceRequest(scenario, userPreferences)
            val request = Request.Builder()
                .url("${baseUrl}chat/completions")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val guidanceResponse = parsePoseGuidanceResponse(responseBody)
                Result.success(guidanceResponse)
            } else {
                Result.failure(Exception("API调用失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检测图像中的人数
     */
    suspend fun detectPersonCount(
        imageBase64: String
    ): Result<PersonCountResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始人数检测API调用")
            Log.d(TAG, "图片Base64长度: ${imageBase64.length}")
            
            val requestBody = createPersonCountRequest(imageBase64)
            val request = Request.Builder()
                .url("${baseUrl}chat/completions")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            
            Log.d(TAG, "发送请求到: ${request.url}")
            
            val response = client.newCall(request).execute()
            Log.d(TAG, "API响应状态: ${response.code} ${response.message}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "API响应内容: $responseBody")
                val countResponse = parsePersonCountResponse(responseBody)
                Log.d(TAG, "解析结果: $countResponse")
                Result.success(countResponse)
            } else {
                val errorBody = response.body?.string()
                Log.e(TAG, "API调用失败: ${response.code} ${response.message}")
                Log.e(TAG, "错误响应: $errorBody")
                Result.failure(Exception("API调用失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "人数检测异常: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创建场景分析请求体
     */
    private fun createSceneAnalysisRequest(
        lightLevel: Float,
        timeOfDay: String,
        environment: String
    ): RequestBody {
        val prompt = """
            作为专业摄影助手，请分析当前拍摄环境并提供建议：
            
            环境信息：
            - 光线强度：$lightLevel%
            - 时间：$timeOfDay
            - 环境：$environment
            
            请提供以下建议（以JSON格式回复）：
            {
                "lightingAdvice": "补光建议",
                "angleRecommendation": "推荐拍摄角度",
                "distanceAdvice": "最佳拍摄距离",
                "sceneOptimization": "场景优化建议",
                "lightType": "推荐补光类型（暖光/冷光/自然光）",
                "brightness": "推荐亮度值（0-100）"
            }
        """.trimIndent()
        
        val jsonBody = JSONObject().apply {
            put("model", modelEndpoint)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 1000)
            put("temperature", 0.7)
        }
        
        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }
    
    /**
     * 创建姿势指导请求体
     */
    private fun createPoseGuidanceRequest(
        scenario: String,
        userPreferences: String
    ): RequestBody {
        val prompt = """
            作为专业摄影指导师，请为以下场景提供姿势指导：
            
            拍摄场景：$scenario
            用户偏好：$userPreferences
            
            请提供详细的姿势指导（以JSON格式回复）：
            {
                "poseInstructions": "具体姿势指导步骤",
                "facialExpression": "面部表情建议",
                "bodyPosition": "身体位置调整",
                "handGestures": "手部姿势建议",
                "tips": "拍摄小贴士"
            }
        """.trimIndent()
        
        val jsonBody = JSONObject().apply {
            put("model", modelEndpoint)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 800)
            put("temperature", 0.8)
        }
        
        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }
    
    /**
     * 创建人数检测请求体
     */
    private fun createPersonCountRequest(imageBase64: String): RequestBody {
        val prompt = """
            请分析这张图片中有多少个人，并以JSON格式回复：
            {
                "personCount": 人数（数字）,
                "confidence": 置信度（0-100）,
                "description": "检测结果描述"
            }
            
            请仔细观察图片，准确统计人数。如果图片模糊或无法确定，请在description中说明。
        """.trimIndent()
        
        val jsonBody = JSONObject().apply {
            put("model", modelEndpoint)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$imageBase64")
                            })
                        })
                    })
                })
            })
            put("max_tokens", 300)
            put("temperature", 0.3)
        }
        
        return jsonBody.toString().toRequestBody("application/json".toMediaType())
    }
    
    /**
     * 解析场景分析响应
     */
    private fun parseSceneAnalysisResponse(responseBody: String?): SceneAnalysisResponse {
        return try {
            val jsonResponse = JSONObject(responseBody ?: "")
            val choices = jsonResponse.getJSONArray("choices")
            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            
            // 尝试解析JSON内容
            val contentJson = JSONObject(content)
            SceneAnalysisResponse(
                lightingAdvice = contentJson.optString("lightingAdvice", "建议使用自然光拍摄"),
                angleRecommendation = contentJson.optString("angleRecommendation", "平视角度拍摄"),
                distanceAdvice = contentJson.optString("distanceAdvice", "保持1-2米距离"),
                sceneOptimization = contentJson.optString("sceneOptimization", "注意背景整洁"),
                lightType = contentJson.optString("lightType", "自然光"),
                brightness = contentJson.optInt("brightness", 50)
            )
        } catch (e: Exception) {
            // 如果解析失败，返回默认建议
            SceneAnalysisResponse(
                lightingAdvice = "建议使用柔和的补光，避免强烈阴影",
                angleRecommendation = "尝试从稍高角度拍摄，突出主体",
                distanceAdvice = "保持适中距离，确保主体清晰",
                sceneOptimization = "注意背景简洁，突出主体",
                lightType = "暖光",
                brightness = 60
            )
        }
    }
    
    /**
     * 解析姿势指导响应
     */
    private fun parsePoseGuidanceResponse(responseBody: String?): PoseGuidanceResponse {
        return try {
            val jsonResponse = JSONObject(responseBody ?: "")
            val choices = jsonResponse.getJSONArray("choices")
            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            
            // 尝试解析JSON内容
            val contentJson = JSONObject(content)
            PoseGuidanceResponse(
                poseInstructions = contentJson.optString("poseInstructions", "保持自然放松的姿态"),
                facialExpression = contentJson.optString("facialExpression", "微笑，眼神自然"),
                bodyPosition = contentJson.optString("bodyPosition", "身体略微侧向，显得更自然"),
                handGestures = contentJson.optString("handGestures", "手部自然下垂或轻松摆放"),
                tips = contentJson.optString("tips", "放松心情，展现真实的自己")
            )
        } catch (e: Exception) {
            // 如果解析失败，返回默认指导
            PoseGuidanceResponse(
                poseInstructions = "站直身体，肩膀放松，保持自信姿态",
                facialExpression = "自然微笑，眼神看向镜头",
                bodyPosition = "身体稍微转向一侧，显得更有层次",
                handGestures = "双手自然摆放，避免僵硬",
                tips = "深呼吸，放松心情，展现最好的自己"
            )
        }
    }
    
    /**
     * 解析人数检测响应
     */
    private fun parsePersonCountResponse(responseBody: String?): PersonCountResponse {
        return try {
            Log.d(TAG, "开始解析人数检测响应")
            val jsonResponse = JSONObject(responseBody ?: "")
            val choices = jsonResponse.getJSONArray("choices")
            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            
            Log.d(TAG, "API返回的content: $content")
            
            // 尝试解析JSON内容
            val contentJson = JSONObject(content)
            val result = PersonCountResponse(
                personCount = contentJson.optInt("personCount", 1),
                confidence = contentJson.optInt("confidence", 50),
                description = contentJson.optString("description", "检测到人员")
            )
            Log.d(TAG, "解析成功: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "解析人数检测响应失败: ${e.message}", e)
            Log.d(TAG, "原始响应: $responseBody")
            // 如果解析失败，返回默认值
            val defaultResult = PersonCountResponse(
                personCount = 1,
                confidence = 50,
                description = "无法准确检测，默认为单人"
            )
            Log.d(TAG, "返回默认结果: $defaultResult")
            defaultResult
        }
    }
}

/**
 * 场景分析响应数据类
 */
data class SceneAnalysisResponse(
    val lightingAdvice: String,
    val angleRecommendation: String,
    val distanceAdvice: String,
    val sceneOptimization: String,
    val lightType: String,
    val brightness: Int
)

/**
 * 姿势指导响应数据类
 */
data class PoseGuidanceResponse(
    val poseInstructions: String,
    val facialExpression: String,
    val bodyPosition: String,
    val handGestures: String,
    val tips: String
)

/**
 * 人数检测响应数据类
 */
data class PersonCountResponse(
    val personCount: Int,
    val confidence: Int,
    val description: String
)