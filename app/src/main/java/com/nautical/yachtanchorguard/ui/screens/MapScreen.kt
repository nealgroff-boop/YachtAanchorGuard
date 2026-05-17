package com.nautical.yachtanchorguard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.util.GpsUtils
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapScreen(
    gpsFix: GpsFix?,
    anchor: AnchorData?,
    recentFixes: List<GpsFix>
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF001E2E))) {
        // Radar View
        RadarView(
            modifier = Modifier.fillMaxSize(),
            gpsFix = gpsFix,
            anchor = anchor,
            recentFixes = recentFixes
        )
        
        // Overlay Controls
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { /* Zoom In */ },
                containerColor = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            FloatingActionButton(
                onClick = { /* Zoom Out */ },
                containerColor = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
        }
        
        // Bottom Info Bar
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ANCHOR POSITION", color = Color.LightGray, fontSize = 10.sp)
                    Text(
                        if (anchor != null) String.format("%.5f, %.5f", anchor.latitude, anchor.longitude) else "Not Set",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (gpsFix != null && anchor != null) {
                    val dist = GpsUtils.calculateDistance(gpsFix.latitude, gpsFix.longitude, anchor.latitude, anchor.longitude)
                    Text(
                        GpsUtils.formatDistance(dist, "meters"),
                        color = if (dist > anchor.driftRadius) Color.Red else Color.Cyan,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun RadarView(
    modifier: Modifier,
    gpsFix: GpsFix?,
    anchor: AnchorData?,
    recentFixes: List<GpsFix>
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.width.coerceAtMost(size.height) / 2 * 0.8f
        
        // Draw Radar Circles
        for (i in 1..4) {
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.2f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Draw Crosshair
        drawLine(
            color = Color.Cyan.copy(alpha = 0.2f),
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.Cyan.copy(alpha = 0.2f),
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx()
        )
        
        // Draw Anchor (Center)
        if (anchor != null) {
            drawCircle(
                color = Color.Yellow,
                radius = 8.dp.toPx(),
                center = center
            )
            
            // Draw Drift Radius Circle
            // Scale: let's say maxRadius represents 100m for now
            val scale = maxRadius / 100f 
            drawCircle(
                color = Color.Red.copy(alpha = 0.3f),
                radius = anchor.driftRadius * scale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Draw Yacht Position
        if (gpsFix != null && anchor != null) {
            val distance = GpsUtils.calculateDistance(gpsFix.latitude, gpsFix.longitude, anchor.latitude, anchor.longitude)
            val bearing = GpsUtils.calculateBearing(anchor.latitude, anchor.longitude, gpsFix.latitude, gpsFix.longitude)
            
            val scale = maxRadius / 100f
            val r = distance * scale
            val angleRad = Math.toRadians((bearing - 90).toDouble())
            
            val yachtPos = Offset(
                center.x + (r * cos(angleRad)).toFloat(),
                center.y + (r * sin(angleRad)).toFloat()
            )
            
            // Draw Track
            if (recentFixes.isNotEmpty()) {
                var lastPoint = center // Start from anchor for visualization if needed, or first fix
                recentFixes.forEachIndexed { index, fix ->
                    val d = GpsUtils.calculateDistance(fix.latitude, fix.longitude, anchor.latitude, anchor.longitude)
                    val b = GpsUtils.calculateBearing(anchor.latitude, anchor.longitude, fix.latitude, fix.longitude)
                    val rad = d * scale
                    val ang = Math.toRadians((b - 90).toDouble())
                    val point = Offset(
                        center.x + (rad * cos(ang)).toFloat(),
                        center.y + (rad * sin(ang)).toFloat()
                    )
                    if (index > 0) {
                        drawLine(Color.White.copy(alpha = 0.5f), lastPoint, point, strokeWidth = 2.dp.toPx())
                    }
                    lastPoint = point
                }
            }
            
            // Draw Boat Icon (Simple Triangle)
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = yachtPos
            )
        }
    }
}
