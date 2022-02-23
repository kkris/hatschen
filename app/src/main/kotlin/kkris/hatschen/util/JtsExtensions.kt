package kkris.hatschen.util

import org.locationtech.jts.geom.Coordinate

// in meters: how far coordinates should be pulled from the boundary towards the center
private const val BOUNDARY_SLACK = 10.0

// these extension functions are just for me, I always confuse the x with latitude
val Coordinate.latitude: Double
    get() = y

val Coordinate.longitude: Double
    get() = x


fun pullTowardsCentroid(center: Coordinate, coordinate: Coordinate): Coordinate {
    // vector pointing from coordinate to center
    val vector = Coordinate(
        center.x - coordinate.x,
        center.y - coordinate.y
    )
    val length = haversine(center.latitude, center.longitude, coordinate.latitude, coordinate.longitude)
    val unityVector = Coordinate(
        vector.x / length,
        vector.y / length
    )

    return Coordinate(
        coordinate.x + BOUNDARY_SLACK * unityVector.x,
        coordinate.y + BOUNDARY_SLACK * unityVector.y,
    )
}