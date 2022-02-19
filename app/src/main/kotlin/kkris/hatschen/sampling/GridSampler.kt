package kkris.hatschen.sampling

import kkris.hatschen.gtsp.GtspNode
import kkris.hatschen.gtsp.GtspSet
import kkris.hatschen.parser.Area
import kkris.hatschen.util.latitude
import kkris.hatschen.util.longitude
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon

class GridSampler: VertexSampler {
    override fun sample(area: Area, allAreas: List<Area>): GtspSet {
        val nodes = area.polygon.sample().map { coordinate ->
            GtspNode.create(coordinate)
        }

        return GtspSet(
            id = area.id,
            members = nodes
        )
    }
}

// sample a regular grid
private fun Polygon.sample(): List<Coordinate> {
    val points = mutableListOf<Coordinate>()

    val gridSize = 0.004 // about 300 meters (well, in vienna at least)

    val north = coordinates.minByOrNull { it.latitude }!!.latitude
    val south = coordinates.maxByOrNull { it.latitude }!!.latitude
    val east = coordinates.maxByOrNull { it.longitude }!!.longitude
    val west = coordinates.minByOrNull { it.longitude }!!.longitude

    var currentLatitude = north
    var currentLongitude = west

    while (currentLatitude <= south) {
        while (currentLongitude <= east) {
            val candidate = GeometryFactory().createPoint(Coordinate(currentLongitude, currentLatitude))

            if (this.contains(candidate)) {
                points.add(Coordinate(currentLongitude, currentLatitude))
            }

            currentLongitude += gridSize
        }

        currentLatitude += gridSize
        currentLongitude = west
    }

    return points
}