package com.travellight.camera.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 绘制站立姿势
 */
fun DrawScope.drawStandingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 头部
    drawCircle(
        color = Color.White,
        radius = size.width * 0.08f,
        center = Offset(centerX, centerY - size.height * 0.3f),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.22f),
        end = Offset(centerX, centerY + size.height * 0.1f),
        strokeWidth = strokeWidth
    )
    
    // 左臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.1f),
        end = Offset(centerX - size.width * 0.15f, centerY),
        strokeWidth = strokeWidth
    )
    
    // 右臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.1f),
        end = Offset(centerX + size.width * 0.15f, centerY),
        strokeWidth = strokeWidth
    )
    
    // 左腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.1f),
        end = Offset(centerX - size.width * 0.08f, centerY + size.height * 0.35f),
        strokeWidth = strokeWidth
    )
    
    // 右腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.1f),
        end = Offset(centerX + size.width * 0.08f, centerY + size.height * 0.35f),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制坐姿
 */
fun DrawScope.drawSittingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 头部
    drawCircle(
        color = Color.White,
        radius = size.width * 0.08f,
        center = Offset(centerX, centerY - size.height * 0.25f),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.17f),
        end = Offset(centerX, centerY + size.height * 0.05f),
        strokeWidth = strokeWidth
    )
    
    // 左臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.05f),
        end = Offset(centerX - size.width * 0.12f, centerY + size.height * 0.02f),
        strokeWidth = strokeWidth
    )
    
    // 右臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.05f),
        end = Offset(centerX + size.width * 0.12f, centerY + size.height * 0.02f),
        strokeWidth = strokeWidth
    )
    
    // 左腿（坐姿）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.05f),
        end = Offset(centerX - size.width * 0.15f, centerY + size.height * 0.05f),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = Color.White,
        start = Offset(centerX - size.width * 0.15f, centerY + size.height * 0.05f),
        end = Offset(centerX - size.width * 0.15f, centerY + size.height * 0.25f),
        strokeWidth = strokeWidth
    )
    
    // 右腿（坐姿）
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.05f),
        end = Offset(centerX + size.width * 0.15f, centerY + size.height * 0.05f),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = Color.White,
        start = Offset(centerX + size.width * 0.15f, centerY + size.height * 0.05f),
        end = Offset(centerX + size.width * 0.15f, centerY + size.height * 0.25f),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制侧面姿势
 */
fun DrawScope.drawProfilePose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 头部（侧面）
    drawCircle(
        color = Color.White,
        radius = size.width * 0.08f,
        center = Offset(centerX + size.width * 0.02f, centerY - size.height * 0.3f),
        style = Stroke(width = strokeWidth)
    )
    
    // 身体
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.22f),
        end = Offset(centerX, centerY + size.height * 0.1f),
        strokeWidth = strokeWidth
    )
    
    // 前臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.1f),
        end = Offset(centerX + size.width * 0.18f, centerY - size.height * 0.05f),
        strokeWidth = strokeWidth
    )
    
    // 后臂
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY - size.height * 0.1f),
        end = Offset(centerX - size.width * 0.1f, centerY),
        strokeWidth = strokeWidth
    )
    
    // 前腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.1f),
        end = Offset(centerX + size.width * 0.1f, centerY + size.height * 0.35f),
        strokeWidth = strokeWidth
    )
    
    // 后腿
    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY + size.height * 0.1f),
        end = Offset(centerX - size.width * 0.05f, centerY + size.height * 0.35f),
        strokeWidth = strokeWidth
    )
}

/**
 * 绘制躺姿
 */
fun DrawScope.drawLyingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 简化为站立姿势
    drawStandingPose(centerX, centerY, strokeWidth)
}

/**
 * 绘制跳跃姿势
 */
fun DrawScope.drawJumpingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 简化为站立姿势
    drawStandingPose(centerX, centerY, strokeWidth)
}

/**
 * 绘制行走姿势
 */
fun DrawScope.drawWalkingPose(centerX: Float, centerY: Float, strokeWidth: Float) {
    // 简化为站立姿势
    drawStandingPose(centerX, centerY, strokeWidth)
}