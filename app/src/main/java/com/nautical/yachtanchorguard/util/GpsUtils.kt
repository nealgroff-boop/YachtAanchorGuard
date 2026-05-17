package com.nautical.yachtanchorguard.util

import kotlin.math.*

/**
 * Utility functions for GPS calculations
 */
object GpsUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * Returns distance in meters
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_METERS * c

        return distance.toFloat()
    }

    /**
     * Calculate bearing (direction) from one point to another
     * Returns bearing in degrees (0-360)
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360

        return bearing
    }

    /**
     * Calculate new coordinates given a starting point, distance, and bearing
     * Returns pair of (latitude, longitude)
     */
    fun calculateNewCoordinates(
        startLat: Double,
        startLon: Double,
        distanceMeters: Float,
        bearingDegrees: Float
    ): Pair<Double, Double> {
        val angularDistance = distanceMeters / EARTH_RADIUS_METERS
        val bearing = Math.toRadians(bearingDegrees.toDouble())
        val lat1 = Math.toRadians(startLat)
        val lon1 = Math.toRadians(startLon)

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
            cos(lat1) * sin(angularDistance) * cos(bearing)
        )

        val lon2 = lon1 + atan2(
            sin(bearing) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return Pair(
            Math.toDegrees(lat2),
            Math.toDegrees(lon2)
        )
    }

    /**
     * Convert meters to feet
     */
    fun metersToFeet(meters: Float): Float {
        return meters * 3.28084f
    }

    /**
     * Convert feet to meters
     */
    fun feetToMeters(feet: Float): Float {
        return feet / 3.28084f
    }

    /**
     * Format distance with appropriate unit
     */
    fun formatDistance(distanceMeters: Float, units: String): String {
        return if (units == "feet") {
            val feet = metersToFeet(distanceMeters)
            String.format("%.1f ft", feet)
        } else {
            String.format("%.1f m", distanceMeters)
        }
    }

    /**
     * Convert bearing degrees to compass direction
     */
    fun bearingToCompassDirection(bearing: Float): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((bearing + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }

    /**
     * Check if a point is within a circular radius
     */
    fun isWithinRadius(
        pointLat: Double,
        pointLon: Double,
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Float
    ): Boolean {
        val distance = calculateDistance(pointLat, pointLon, centerLat, centerLon)
        return distance <= radiusMeters
    }
}
