package kkris.hatschen.distance

import kkris.hatschen.util.haversine
import kkris.hatschen.util.latitude
import kkris.hatschen.util.longitude
import org.locationtech.jts.geom.Coordinate

class ManhattanDistanceFunction: DistanceFunction {
    override fun calculate(c1: Coordinate, c2: Coordinate): Int {
        val distance = haversine(c1.latitude, c1.longitude, c2.latitude, c1.longitude) +
                haversine(c2.latitude, c1.longitude, c2.latitude, c2.longitude)

        return distance.toInt()
    }

    override fun getPath(c1: Coordinate, c2: Coordinate): List<Coordinate> {
        return listOf(
            c1,
            Coordinate(c1.longitude, c2.latitude),
            c2
        )
    }
}