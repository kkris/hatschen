package kkris.hatschen.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371

    val dLat = deg2rad(lat2 - lat1)
    val dLon = deg2rad(lon2 - lon1)

    val a = sin(dLat / 2.0) * sin(dLat / 2.0) + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * sin(dLon / 2.0) * sin(dLon / 2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (R * c) * 1000.0
}

private fun deg2rad(deg: Double): Double {
    return deg * (Math.PI / 180.0)
}

private fun rad2deg(rad: Double): Double {
    return rad * (180.0 / Math.PI)
}