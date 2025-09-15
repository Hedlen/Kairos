@file:OptIn(ExperimentalMaterial3Api::class)
package com.travellight.camera.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogViewer: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    
    // 设置已在ViewModel的init块中加载
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部工具栏
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showResetDialog = true }) {
                    Icon(Icons.Default.Refresh, contentDescription = "重置")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 补光设置
            item {
                SettingsSection(
                    title = "补光设置",
                    icon = Icons.Default.Star
                ) {
                    uiState.lightSettings.forEach { setting ->
                        SettingItem(
                            setting = setting,
                            onValueChange = { newValue ->
                                viewModel.updateSetting(setting.id, newValue)
                            }
                        )
                    }
                }
            }
            
            // 相机设置
            item {
                SettingsSection(
                    title = "相机设置",
                    icon = Icons.Default.Home
                ) {
                    uiState.cameraSettings.forEach { setting ->
                        SettingItem(
                            setting = setting,
                            onValueChange = { newValue ->
                                viewModel.updateSetting(setting.id, newValue)
                            }
                        )
                    }
                }
            }
            
            // 通用设置
            item {
                SettingsSection(
                    title = "通用设置",
                    icon = Icons.Default.Settings
                ) {
                    uiState.generalSettings.forEach { setting ->
                        SettingItem(
                            setting = setting,
                            onValueChange = { newValue ->
                                when (setting.title) {
                                    "日志查看" -> {
                                        onNavigateToLogViewer()
                                    }
                                    "重置设置" -> {
                                        showResetDialog = true
                                    }
                                    else -> {
                                        viewModel.updateSetting(setting.id, newValue)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // 关于信息
            item {
                AboutSection()
            }
        }
    }
    
    // 重置确认对话框
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置设置") },
            text = { Text("确定要重置所有设置到默认值吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: 实现重置功能
                        showResetDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
fun SettingItem(
    setting: SettingItem,
    onValueChange: (Any) -> Unit
) {
    Column {
        when (setting.type) {
            SettingType.SWITCH -> {
                SwitchSettingItem(
                    title = setting.title,
                    description = setting.description,
                    value = setting.value as Boolean,
                    onValueChange = onValueChange
                )
            }
            SettingType.SLIDER -> {
                SliderSettingItem(
                    title = setting.title,
                    description = setting.description,
                    value = setting.value as Float,
                    range = (0f..1f),
                    onValueChange = onValueChange
                )
            }
            SettingType.DROPDOWN -> {
                DropdownSettingItem(
                    title = setting.title,
                    description = setting.description,
                    value = setting.value as String,
                    options = setting.options,
                    onValueChange = onValueChange
                )
            }
            SettingType.BUTTON -> {
                ButtonSettingItem(
                    title = setting.title,
                    description = setting.description,
                    onValueChange = onValueChange
                )
            }
        }
        
        if (setting != setting) { // 不是最后一个项目
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String?,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}

@Composable
fun SliderSettingItem(
    title: String,
    description: String?,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DropdownSettingItem(
    title: String,
    description: String?,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ButtonSettingItem(
    title: String,
    description: String?,
    onValueChange: (Any) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Button(
            onClick = { onValueChange(Unit) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = title)
        }
    }
}

@Composable
fun TextSettingItem(
    title: String,
    description: String?,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AboutItem("应用名称", "旅游补光相机")
            AboutItem("版本", "1.0.0")
            AboutItem("开发者", "Travel Light Team")
            AboutItem("联系方式", "support@travellight.com")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "专为旅游摄影设计的智能补光相机应用，提供专业的补光功能和智能拍照推荐。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AboutItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}