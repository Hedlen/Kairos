package com.travellight.camera.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String,
    val fullLine: String
)

/**
 * 日志查看界面
 * 显示应用运行时的日志信息，便于问题排查
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var autoRefresh by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // 自动刷新逻辑
    LaunchedEffect(autoRefresh) {
        while (autoRefresh) {
            loadLogs(context) { newLogs ->
                logs = newLogs
            }
            delay(2000) // 每2秒刷新一次
        }
    }
    
    // 初始加载
    LaunchedEffect(Unit) {
        loadLogs(context) { newLogs ->
            logs = newLogs
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部工具栏
        TopAppBar(
            title = { Text("应用日志") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                // 自动刷新开关
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "自动刷新",
                        fontSize = 12.sp
                    )
                    Switch(
                        checked = autoRefresh,
                        onCheckedChange = { autoRefresh = it },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                
                // 手动刷新按钮
                IconButton(
                    onClick = {
                        isLoading = true
                        loadLogs(context) { newLogs ->
                            logs = newLogs
                            isLoading = false
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                
                // 清空日志按钮
                IconButton(
                    onClick = {
                        logs = emptyList()
                    }
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "清空")
                }
            }
        )
        
        // 日志统计信息
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val errorCount = logs.count { it.level == "E" }
                val warningCount = logs.count { it.level == "W" }
                val infoCount = logs.count { it.level == "I" }
                val debugCount = logs.count { it.level == "D" }
                
                LogLevelChip("错误", errorCount, Color.Red)
                LogLevelChip("警告", warningCount, Color(0xFFFF9800))
                LogLevelChip("信息", infoCount, Color.Blue)
                LogLevelChip("调试", debugCount, Color.Gray)
            }
        }
        
        // 加载指示器
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 日志列表
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { logEntry ->
                LogEntryItem(logEntry = logEntry)
            }
        }
        
        // 自动滚动到底部
        LaunchedEffect(logs.size) {
            if (logs.isNotEmpty() && autoRefresh) {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }
}

@Composable
fun LogLevelChip(
    label: String,
    count: Int,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
fun LogEntryItem(logEntry: LogEntry) {
    val backgroundColor = when (logEntry.level) {
        "E" -> Color.Red.copy(alpha = 0.1f)
        "W" -> Color(0xFFFF9800).copy(alpha = 0.1f)
        "I" -> Color.Blue.copy(alpha = 0.1f)
        "D" -> Color.Gray.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val textColor = when (logEntry.level) {
        "E" -> Color.Red
        "W" -> Color(0xFFFF9800)
        "I" -> Color.Blue
        "D" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // 时间戳和级别
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = logEntry.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "[${logEntry.level}] ${logEntry.tag}",
                    fontSize = 10.sp,
                    color = textColor,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 日志消息
            Text(
                text = logEntry.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 加载应用日志
 */
private fun loadLogs(
    context: Context,
    onLogsLoaded: (List<LogEntry>) -> Unit
) {
    try {
        val packageName = context.packageName
        val process = Runtime.getRuntime().exec("logcat -d -v time --pid=\${android.os.Process.myPid()}")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        
        val logs = mutableListOf<LogEntry>()
        var line: String?
        
        while (bufferedReader.readLine().also { line = it } != null) {
            line?.let { logLine ->
                parseLogLine(logLine, packageName)?.let { logEntry ->
                    logs.add(logEntry)
                }
            }
        }
        
        bufferedReader.close()
        process.destroy()
        
        // 只保留最近的500条日志
        val recentLogs = logs.takeLast(500)
        onLogsLoaded(recentLogs)
        
    } catch (e: Exception) {
        Log.e("LogViewerScreen", "加载日志失败", e)
        // 如果无法获取系统日志，显示应用内部日志
        onLogsLoaded(getInternalLogs())
    }
}

/**
 * 解析日志行
 */
private fun parseLogLine(line: String, packageName: String): LogEntry? {
    try {
        // 日志格式: MM-dd HH:mm:ss.SSS  PID  TID LEVEL TAG: MESSAGE
        val regex = """(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEF])\s+([^:]+):\s*(.*)""".toRegex()
        val matchResult = regex.find(line)
        
        return if (matchResult != null) {
            val (timestamp, pid, tid, level, tag, message) = matchResult.destructured
            LogEntry(
                timestamp = timestamp,
                level = level,
                tag = tag.trim(),
                message = message.trim(),
                fullLine = line
            )
        } else {
            null
        }
    } catch (e: Exception) {
        return null
    }
}

/**
 * 获取应用内部日志（备用方案）
 */
private fun getInternalLogs(): List<LogEntry> {
    val currentTime = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    return listOf(
        LogEntry(
            timestamp = currentTime,
            level = "I",
            tag = "LogViewer",
            message = "日志查看器已启动",
            fullLine = ""
        ),
        LogEntry(
            timestamp = currentTime,
            level = "W",
            tag = "LogViewer",
            message = "无法获取系统日志，显示应用内部日志",
            fullLine = ""
        ),
        LogEntry(
            timestamp = currentTime,
            level = "I",
            tag = "LogViewer",
            message = "提示：在开发者选项中启用USB调试可获取更详细的日志信息",
            fullLine = ""
        )
    )
}