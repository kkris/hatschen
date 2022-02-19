package kkris.hatschen.distance

import org.locationtech.jts.geom.Coordinate

enum class DistanceMetric(val value: String) {
    HAVERSINE("haversine"),
    MANHATTAN("manhattan"),
    FOOTPATH("footpath")
}

interface DistanceFunction {
    // distance in meters
    fun calculate(c1: Coordinate, c2: Coordinate): Int

    fun getPath(c1: Coordinate, c2: Coordinate): List<Coordinate>

    companion object {
        fun create(type: DistanceMetric, osmPath: String?): DistanceFunction {
            return when (type) {
                DistanceMetric.HAVERSINE -> HaversineDistanceFunction()
                DistanceMetric.MANHATTAN -> ManhattanDistanceFunction()
                DistanceMetric.FOOTPATH -> FootpathDistanceFunction(osmPath ?: "No OSM file provided. Required for the footpath distance metric.")
            }
        }
    }
}