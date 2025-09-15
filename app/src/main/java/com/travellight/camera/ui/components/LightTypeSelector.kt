package com.travellight.camera.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travellight.camera.data.model.LightType

/**
 * 补光类型选择器
 * 提供水平滚动的补光类型选项
 */
@Composable
fun LightTypeSelector(
    selectedType: LightType,
    onTypeSelected: (LightType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val lightTypes = listOf(
        LightType.NATURAL,
        LightType.WARM,
        LightType.COOL,
        LightType.SOFT
    )
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "补光类型",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lightTypes) { lightType ->
                    LightTypeItem(
                        lightType = lightType,
                        isSelected = lightType == selectedType,
                        onClick = {
                            if (enabled) {
                                onTypeSelected(lightType)
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}

/**
 * 简化版补光类型选择器（仅选项按钮）
 */
@Composable
fun SimpleLightTypeSelector(
    selectedType: LightType,
    onTypeSelected: (LightType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val lightTypes = listOf(
        LightType.NATURAL,
        LightType.WARM,
        LightType.COOL,
        LightType.SOFT
    )
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(lightTypes) { lightType ->
            LightTypeItem(
                lightType = lightType,
                isSelected = lightType == selectedType,
                onClick = {
                    if (enabled) {
                        onTypeSelected(lightType)
                    }
                },
                modifier = Modifier.width(70.dp)
            )
        }
    }
}