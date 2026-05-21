package com.nautical.yachtanchorguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.nautical.yachtanchorguard.data.model.AnchorData
import com.nautical.yachtanchorguard.data.model.GpsFix
import com.nautical.yachtanchorguard.util.GpsUtils
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color as AndroidColor

@Composable
fun MapScreen(
    gpsFix: GpsFix?,
    anchor: AnchorData?,
    recentFixes: List<GpsFix>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // 1. Draw Historical Track
                if (recentFixes.isNotEmpty()) {
                    val track = Polyline(mapView).apply {
                        outlinePaint.color = AndroidColor.BLUE
                        outlinePaint.strokeWidth = 5f
                        setPoints(recentFixes.map { GeoPoint(it.latitude, it.longitude) })
                    }
                    mapView.overlays.add(track)
                }

                // 2. Draw Anchor and Drift Radius
                anchor?.let {
                    val anchorPoint = GeoPoint(it.latitude, it.longitude)
                    
                    // Anchor Marker
                    val anchorMarker = Marker(mapView).apply {
                        position = anchorPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Anchor Position"
                    }
                    mapView.overlays.add(anchorMarker)

                    // Drift Radius Circle
                    val circlePoints = Polygon.pointsAsCircle(anchorPoint, it.driftRadius.toDouble())
                    val circle = Polygon(mapView).apply {
                        points = circlePoints
                        fillPaint.color = AndroidColor.argb(50, 0, 255, 0)
                        outlinePaint.color = AndroidColor.GREEN
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(circle)
                }

                // 3. Draw Yacht Position
                gpsFix?.let {
                    val yachtPoint = GeoPoint(it.latitude, it.longitude)
                    val yachtMarker = Marker(mapView).apply {
                        position = yachtPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        rotation = it.bearing
                        title = "Yacht Position"
                    }
                    mapView.overlays.add(yachtMarker)
                    
                    // Center map on yacht if it's the first fix
                    if (mapView.tag == null) {
                        mapView.controller.setCenter(yachtPoint)
                        mapView.tag = "centered"
                    }
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { /* Zoom In handled by MapView */ },
                containerColor = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            FloatingActionButton(
                onClick = { /* Zoom Out handled by MapView */ },
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
