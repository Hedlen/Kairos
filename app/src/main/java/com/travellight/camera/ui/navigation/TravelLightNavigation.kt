package com.travellight.camera.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.travellight.camera.ui.screens.camera.CameraScreen
import com.travellight.camera.ui.screens.recommendation.RecommendationScreen
import com.travellight.camera.ui.screens.settings.SettingsScreen
import com.travellight.camera.ui.screens.LogViewerScreen

/**
 * 应用主导航组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelLightNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // 定义底部导航项
    val bottomNavItems = listOf(
        BottomNavItem(
            route = "camera",
            title = "拍照",
            icon = Icons.Default.CameraAlt,
            selectedIcon = Icons.Default.CameraAlt
        ),
        BottomNavItem(
            route = "settings",
            title = "设置",
            icon = Icons.Default.Settings,
            selectedIcon = Icons.Default.Settings
        )
    )
    
    // 判断是否显示底部导航栏
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // 避免多个相同目标在栈中
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 避免重复选择同一项时创建多个副本
                                    launchSingleTop = true
                                    // 重新选择之前选择的项时恢复状态
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "camera",
            modifier = Modifier.padding(paddingValues)
        ) {
            // 相机界面
            composable("camera") {
                CameraScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 推荐界面
            composable("recommendation") {
                RecommendationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 设置界面
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogViewer = {
                        navController.navigate("log_viewer")
                    }
                )
            }
            
            // 日志查看界面
            composable("log_viewer") {
                LogViewerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/**
 * 导航路由常量
 */
object NavigationRoutes {
    const val CAMERA = "camera"
    const val RECOMMENDATION = "recommendation"
    const val SETTINGS = "settings"
    const val LOG_VIEWER = "log_viewer"
}

/**
 * 导航扩展函数
 */
fun NavController.navigateToCamera() {
    navigate(NavigationRoutes.CAMERA) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToRecommendation() {
    navigate(NavigationRoutes.RECOMMENDATION)
}

fun NavController.navigateToSettings() {
    navigate(NavigationRoutes.SETTINGS)
}