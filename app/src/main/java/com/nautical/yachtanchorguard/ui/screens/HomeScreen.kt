package com.nautical.yachtanchorguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.util.GpsUtils

@Composable
fun HomeScreen(
    gpsFix: GpsFix?,
    anchor: AnchorData?,
    onSetAnchor: (Double, Double, Float) -> Unit,
    onAdjustRadius: (Float) -> Unit,
    onAcknowledgeAlarm: () -> Unit,
    isAlarmActive: Boolean
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Header
        StatusHeader(isAlarmActive, anchor?.isArmed ?: false, gpsFix != null)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Dashboard Card
        DashboardCard(gpsFix, anchor)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { 
                    if (gpsFix != null) {
                        onSetAnchor(gpsFix.latitude, gpsFix.longitude, anchor?.driftRadius ?: 50f)
                    }
                },
                modifier = Modifier.weight(1f).height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00658B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Anchor, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SET ANCHOR", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = { /* Open Radius Dialog */ },
                modifier = Modifier.weight(1f).height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6278)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SettingsInputAntenna, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RADIUS", fontWeight = FontWeight.Bold)
            }
        }
        
        if (isAlarmActive) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAcknowledgeAlarm,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("ACKNOWLEDGE ALARM", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // GPS Details Card
        GpsDetailsCard(gpsFix)
    }
}

@Composable
fun StatusHeader(isAlarmActive: Boolean, isArmed: Boolean, hasGps: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusBadge(
            text = if (isAlarmActive) "ALARM!" else if (isArmed) "ARMED" else "DISARMED",
            color = if (isAlarmActive) Color(0xFFB3261E) else if (isArmed) Color(0xFF34C759) else Color.Gray,
            modifier = Modifier.weight(1f)
        )
        StatusBadge(
            text = if (hasGps) "GPS OK" else "NO GPS",
            color = if (hasGps) Color(0xFF34C759) else Color(0xFFB3261E),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun DashboardCard(gpsFix: GpsFix?, anchor: AnchorData?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("DISTANCE TO ANCHOR", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            
            val distance = if (gpsFix != null && anchor != null) {
                GpsUtils.calculateDistance(gpsFix.latitude, gpsFix.longitude, anchor.latitude, anchor.longitude)
            } else null
            
            val bearing = if (gpsFix != null && anchor != null) {
                GpsUtils.calculateBearing(gpsFix.latitude, gpsFix.longitude, anchor.latitude, anchor.longitude)
            } else null

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (distance != null) GpsUtils.formatDistance(distance, "meters") else "---",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = if (distance != null && anchor != null && distance > anchor.driftRadius) Color(0xFFB3261E) else Color(0xFF00658B)
                )
                
                if (bearing != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).rotate(bearing),
                        tint = Color(0xFF00658B)
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("Drift Radius", "${anchor?.driftRadius?.toInt() ?: 50}m", Icons.Default.Radar)
                InfoItem("Bearing", if (bearing != null) "${bearing.toInt()}° ${GpsUtils.bearingToCompassDirection(bearing)}" else "---", Icons.Default.Explore)
            }
        }
    }
}

@Composable
fun GpsDetailsCard(gpsFix: GpsFix?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GPS STATUS", fontWeight = FontWeight.Bold, color = Color(0xFF00658B))
            Spacer(modifier = Modifier.height(12.dp))
            
            DetailRow("Latitude", if (gpsFix != null) String.format("%.6f", gpsFix.latitude) else "---")
            DetailRow("Longitude", if (gpsFix != null) String.format("%.6f", gpsFix.longitude) else "---")
            DetailRow("Accuracy", if (gpsFix != null) "${gpsFix.accuracy.toInt()}m" else "---")
            DetailRow("Satellites", if (gpsFix != null) "${gpsFix.satellites}" else "---")
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

// Helper for rotation
fun Modifier.rotate(degrees: Float): Modifier = this.then(
    androidx.compose.ui.draw.rotate(degrees)
)
