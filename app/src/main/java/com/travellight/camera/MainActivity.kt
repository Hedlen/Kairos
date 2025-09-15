package com.travellight.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.travellight.camera.ui.navigation.TravelLightNavigation
import com.travellight.camera.ui.theme.TravelLightCameraTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 主Activity
 * 应用程序的入口点，负责权限管理和导航设置
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("MainActivity", "权限请求结果: $permissions")
        
        val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
        
        Log.d("MainActivity", "相机权限: $cameraPermissionGranted")
        
        if (cameraPermissionGranted) {
            // 相机权限已授予，可以使用相机功能
            Log.d("MainActivity", "相机权限已授予")
            onPermissionsGranted()
        } else {
            // 相机权限被拒绝，显示说明
            Log.d("MainActivity", "相机权限被拒绝")
            onPermissionsDenied()
        }
    }
    
    // 权限状态
    private var _permissionsGranted = mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查权限
        checkAndRequestPermissions()
        
        setContent {
            TravelLightCameraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val permissionsGranted by remember { _permissionsGranted }
                    
                    if (permissionsGranted) {
                        // 权限已授予，显示主界面
                        val navController = rememberNavController()
                        TravelLightNavigation(navController = navController)
                    } else {
                        // 权限未授予，显示权限请求界面
                        PermissionRequestScreen(
                            onRequestPermissions = { checkAndRequestPermissions() }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 检查并请求必要权限
     */
    private fun checkAndRequestPermissions() {
        Log.d("MainActivity", "开始检查权限")
        
        val requiredPermissions = mutableListOf<String>()
        
        // 检查相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.CAMERA)
        }
        
        // 检查录音权限（录制视频需要）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        // 检查存储权限（Android 13以下需要）
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            // Android 13+ 使用新的媒体权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }
        
        Log.d("MainActivity", "需要请求的权限: $requiredPermissions")
        
        if (requiredPermissions.isEmpty()) {
            // 所有权限已授予
            Log.d("MainActivity", "所有权限已存在，直接授予")
            onPermissionsGranted()
        } else {
            // 请求权限
            Log.d("MainActivity", "启动权限请求")
            requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }
    
    /**
     * 权限授予后的处理
     */
    private fun onPermissionsGranted() {
        _permissionsGranted.value = true
    }
    
    /**
     * 权限被拒绝后的处理
     */
    private fun onPermissionsDenied() {
        _permissionsGranted.value = false
        // TODO: 显示权限说明对话框或引导用户到设置页面
    }
}

/**
 * 权限请求界面
 */
@Composable
fun PermissionRequestScreen(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "旅游补光相机",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "为了使用拍照、录像和补光功能，需要访问以下权限。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "• 相机权限：用于拍照和预览\n• 录音权限：用于录制视频\n• 存储权限：用于保存照片和视频",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                Log.d("MainActivity", "授权按钮被点击")
                onRequestPermissions()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("授予权限")
        }
    }
}