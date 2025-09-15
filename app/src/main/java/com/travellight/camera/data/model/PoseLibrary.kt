package com.travellight.camera.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 姿势类型枚举
 */
enum class PoseType {
    PORTRAIT,    // 人像
    GROUP,       // 合影
    SELFIE,      // 自拍
    LANDSCAPE,   // 风景
    CLOSE_UP,    // 特写
    STANDING,    // 站立
    SITTING,     // 坐姿
    PROFILE      // 侧面
}

/**
 * 拍摄场景枚举
 */
enum class ShootingScene {
    INDOOR,      // 室内
    OUTDOOR,     // 室外
    LOW_LIGHT,   // 低光
    BRIGHT,      // 明亮
    SINGLE_PERSON // 单人场景
}

/**
 * 姿势模板数据类
 */
data class PoseTemplate(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val poseType: PoseType,
    val suitableScenes: List<ShootingScene>,
    val instructions: List<String>
)

/**
 * 姿势库对象
 */
object PoseLibrary {
    
    /**
     * 获取所有姿势模板
     */
    fun getAllPoses(): List<PoseTemplate> {
        return listOf(
            *createPortraitPoses().toTypedArray(),
            *createGroupPoses().toTypedArray(),
            *createSelfiePoses().toTypedArray(),
            *createLandscapePoses().toTypedArray(),
            *createCloseUpPoses().toTypedArray()
        )
    }
    
    /**
     * 根据姿势类型获取推荐姿势
     */
    fun getPosesByType(poseType: PoseType): List<PoseTemplate> {
        return getAllPoses().filter { it.poseType == poseType }
    }
    
    /**
     * 根据拍摄场景获取推荐姿势
     */
    fun getPosesByScene(scene: ShootingScene): List<PoseTemplate> {
        return getAllPoses().filter { scene in it.suitableScenes }
    }
    
    /**
     * 获取最佳推荐姿势
     */
    fun getRecommendedPose(poseType: PoseType, scene: ShootingScene): PoseTemplate? {
        val poses = getAllPoses().filter { 
            it.poseType == poseType && scene in it.suitableScenes 
        }
        return poses.firstOrNull()
    }
    
    /**
     * 创建人像姿势
     */
    private fun createPortraitPoses(): List<PoseTemplate> {
        return listOf(
            PoseTemplate(
                id = "portrait_classic", 
                name = "经典人像",
                description = "标准人像拍摄姿势",
                icon = Icons.Default.Person,
                poseType = PoseType.PORTRAIT,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.OUTDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "保持自然站姿",
                    "肩膀略微向后",
                    "眼神看向镜头",
                    "微笑自然"
                )
            ),
            PoseTemplate(
                id = "portrait_side",
                name = "侧身人像",
                description = "侧身角度人像拍摄",
                icon = Icons.Default.Person,
                poseType = PoseType.PORTRAIT,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.OUTDOOR),
                instructions = listOf(
                    "身体侧向45度",
                    "头部转向镜头",
                    "手臂自然下垂",
                    "保持优雅姿态"
                )
            )
        )
    }
    
    /**
     * 创建合影姿势
     */
    private fun createGroupPoses(): List<PoseTemplate> {
        return listOf(
            PoseTemplate(
                id = "group_line",
                name = "一字排开",
                description = "多人一字排列合影",
                icon = Icons.Default.Person,
                poseType = PoseType.GROUP,
                suitableScenes = listOf(ShootingScene.OUTDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "所有人站成一排",
                    "身高差异可调整位置",
                    "保持统一朝向",
                    "确保每个人都清晰可见"
                )
            ),
            PoseTemplate(
                id = "group_triangle",
                name = "三角形排列",
                description = "三角形构图合影",
                icon = Icons.Default.Person,
                poseType = PoseType.GROUP,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.OUTDOOR),
                instructions = listOf(
                    "前后错落排列",
                    "形成三角形构图",
                    "中心人物突出",
                    "保持和谐统一"
                )
            )
        )
    }
    
    /**
     * 创建自拍姿势
     */
    private fun createSelfiePoses(): List<PoseTemplate> {
        return listOf(
            PoseTemplate(
                id = "selfie_classic",
                name = "经典自拍",
                description = "标准自拍姿势",
                icon = Icons.Default.Settings,
                poseType = PoseType.SELFIE,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.OUTDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "手臂伸直举高",
                    "略微仰视角度",
                    "自然微笑",
                    "注意光线方向"
                )
            ),
            PoseTemplate(
                id = "selfie_mirror",
                name = "镜子自拍",
                description = "利用镜子的自拍姿势",
                icon = Icons.Default.Home,
                poseType = PoseType.SELFIE,
                suitableScenes = listOf(ShootingScene.INDOOR),
                instructions = listOf(
                    "侧身面向镜子",
                    "手机举至胸前",
                    "避免闪光灯反射",
                    "保持自然姿态"
                )
            )
        )
    }
    
    /**
     * 创建风景姿势
     */
    private fun createLandscapePoses(): List<PoseTemplate> {
        return listOf(
            PoseTemplate(
                id = "landscape_wide",
                name = "广角风景",
                description = "广角风景拍摄",
                icon = Icons.Default.Settings,
                poseType = PoseType.LANDSCAPE,
                suitableScenes = listOf(ShootingScene.OUTDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "选择开阔视野",
                    "注意前景构图",
                    "保持水平线平衡",
                    "利用自然光线"
                )
            ),
            PoseTemplate(
                id = "landscape_sunset",
                name = "日落风景",
                description = "日落时分风景拍摄",
                icon = Icons.Default.Home,
                poseType = PoseType.LANDSCAPE,
                suitableScenes = listOf(ShootingScene.OUTDOOR, ShootingScene.LOW_LIGHT),
                instructions = listOf(
                    "选择合适拍摄时机",
                    "注意剪影效果",
                    "调整曝光补偿",
                    "捕捉色彩变化"
                )
            )
        )
    }
    
    /**
     * 创建特写姿势
     */
    private fun createCloseUpPoses(): List<PoseTemplate> {
        return listOf(
            PoseTemplate(
                id = "closeup_face",
                name = "面部特写",
                description = "面部特写拍摄",
                icon = Icons.Default.Face,
                poseType = PoseType.CLOSE_UP,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "保持面部清洁",
                    "注意眼神光",
                    "控制景深效果",
                    "突出面部特征"
                )
            ),
            PoseTemplate(
                id = "closeup_detail",
                name = "细节特写",
                description = "物体细节特写",
                icon = Icons.Default.Face,
                poseType = PoseType.CLOSE_UP,
                suitableScenes = listOf(ShootingScene.INDOOR, ShootingScene.OUTDOOR, ShootingScene.BRIGHT),
                instructions = listOf(
                    "选择有趣细节",
                    "保持稳定拍摄",
                    "注意对焦精度",
                    "控制背景虚化"
                )
            )
        )
    }
}