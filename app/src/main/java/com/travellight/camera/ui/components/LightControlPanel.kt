package com.travellight.camera.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.travellight.camera.data.model.LightType

/**
 * 补光控制面板组件
 * 包含补光开关、类型选择和亮度调节
 */
@Composable
fun LightControlPanel(
    isLightOn: Boolean,
    selectedLightType: LightType,
    brightness: Float,
    onToggleLight: () -> Unit,
    onSelectLightType: (LightType) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 补光开关
            LightSwitch(
                isEnabled = isLightOn,
                onToggle = { _ -> onToggleLight() },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (isLightOn) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 补光类型选择
                LightTypeSelector(
                    selectedType = selectedLightType,
                    onTypeSelected = onSelectLightType,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 亮度控制
                BrightnessSlider(
                    brightness = brightness,
                    onBrightnessChanged = onBrightnessChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}