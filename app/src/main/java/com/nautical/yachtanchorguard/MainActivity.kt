package com.nautical.yachtanchorguard

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nautical.yachtanchorguard.data.db.AppDatabase
import com.nautical.yachtanchorguard.data.preferences.PreferencesManager
import com.nautical.yachtanchorguard.data.repository.AnchorRepository
import com.nautical.yachtanchorguard.service.GpsTrackingService
import com.nautical.yachtanchorguard.ui.screens.HomeScreen
import com.nautical.yachtanchorguard.ui.screens.MapScreen
import com.nautical.yachtanchorguard.ui.screens.SettingsScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: AnchorRepository
    private lateinit var preferencesManager: PreferencesManager

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        repository = AnchorRepository(database)
        preferencesManager = PreferencesManager(this)

        setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            
            // Permissions handling
            val permissionState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS
                )
            )

            LaunchedEffect(Unit) {
                permissionState.launchMultiplePermissionRequest()
            }

            // Data states
            val anchor by repository.getAnchorFlow().collectAsStateWithLifecycle(initialValue = null)
            val settings by preferencesManager.appSettingsFlow.collectAsStateWithLifecycle(initialValue = com.nautical.yachtanchorguard.data.model.AppSettings())
            val currentGpsFix by repository.getLatestGpsFixFlow().collectAsStateWithLifecycle(initialValue = null)
            
            var isAlarmActive by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Yacht Anchor Guard", fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF00658B),
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val items = listOf(
                            Triple("Home", "home", Icons.Default.Home),
                            Triple("Map", "map", Icons.Default.Map),
                            Triple("Settings", "settings", Icons.Default.Settings)
                        )
                        
                        items.forEach { (label, route, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("home") {
                        HomeScreen(
                            gpsFix = currentGpsFix,
                            anchor = anchor,
                            onSetAnchor = { lat, lon, radius ->
                                scope.launch {
                                    repository.setAnchor(lat, lon, radius)
                                    startGpsService()
                                }
                            },
                            onAdjustRadius = { radius ->
                                scope.launch { repository.updateAnchorDriftRadius(radius) }
                            },
                            onAcknowledgeAlarm = {
                                isAlarmActive = false
                                // Logic to silence alarm
                            },
                            isAlarmActive = isAlarmActive
                        )
                    }
                    composable("map") {
                        MapScreen(
                            gpsFix = currentGpsFix,
                            anchor = anchor,
                            recentFixes = emptyList() // Would fetch from repository
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settings = settings,
                            onUpdateSettings = { newSettings ->
                                scope.launch {
                                    preferencesManager.updateUnits(newSettings.units)
                                    preferencesManager.updateSmsEnabled(newSettings.smsEnabled)
                                    preferencesManager.updateSmsPhoneNumber(newSettings.smsPhoneNumber)
                                    preferencesManager.updateSmsKeyword(newSettings.smsKeyword)
                                    preferencesManager.updateTestModeEnabled(newSettings.testModeEnabled)
                                }
                            },
                            onTestAlarm = {
                                // Logic to play test alarm sound
                            }
                        )
                    }
                }
            }
        }
    }

    private fun startGpsService() {
        val intent = Intent(this, GpsTrackingService::class.java)
        intent.action = "com.nautical.yachtanchorguard.START_TRACKING"
        startForegroundService(intent)
    }
}
