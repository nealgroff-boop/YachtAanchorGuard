package com.nautical.yachtanchorguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nautical.yachtanchorguard.data.model.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("GENERAL SETTINGS", style = MaterialTheme.typography.titleSmall, color = Color(0xFF00658B), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Units
        SettingsRow(
            title = "Units",
            subtitle = "Current: ${settings.units.replaceFirstChar { it.uppercase() }}",
            icon = Icons.Default.Straighten
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text("CHANGE")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Meters") },
                        onClick = { onUpdateSettings(settings.copy(units = "meters")); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Feet") },
                        onClick = { onUpdateSettings(settings.copy(units = "feet")); expanded = false }
                    )
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // GPS Accuracy
        SettingsRow(
            title = "GPS Accuracy Threshold",
            subtitle = "Ignore fixes worse than ${settings.accuracyThreshold.toInt()}m",
            icon = Icons.Default.GpsFixed
        ) {
            // Simple slider or text input could go here
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("ALARM & SMS", style = MaterialTheme.typography.titleSmall, color = Color(0xFF00658B), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        // SMS Alerts
        SettingsRow(
            title = "SMS Alerts",
            subtitle = if (settings.smsEnabled) "Enabled for ${settings.smsPhoneNumber}" else "Disabled",
            icon = Icons.Default.Sms
        ) {
            Switch(
                checked = settings.smsEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(smsEnabled = it)) }
            )
        }
        
        // SMS Keyword
        SettingsRow(
            title = "Remote Query Keyword",
            subtitle = "Current: ${settings.smsKeyword}",
            icon = Icons.Default.Key
        ) {
            // Edit button
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("ADVANCED", style = MaterialTheme.typography.titleSmall, color = Color(0xFF00658B), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Test Mode
        SettingsRow(
            title = "Test Mode",
            subtitle = "Simulate drift for testing alarms",
            icon = Icons.Default.BugReport
        ) {
            Switch(
                checked = settings.testModeEnabled,
                onCheckedChange = { onUpdateSettings(settings.copy(testModeEnabled = it)) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* Reset to Defaults */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("RESET TO DEFAULTS", color = Color.DarkGray)
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4A6278))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        action()
    }
}
