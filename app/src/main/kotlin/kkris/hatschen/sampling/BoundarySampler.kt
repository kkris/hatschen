package kkris.hatschen.sampling

import kkris.hatschen.distance.HaversineDistanceFunction
import kkris.hatschen.gtsp.GtspNode
import kkris.hatschen.gtsp.GtspSet
import kkris.hatschen.parser.Area
import kkris.hatschen.util.pullTowardsCentroid
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Polygon

// in meters: min distance between two vertices on the same boundary
// smaller values don't make much sense since city blocks are usually at least 50-60m wide
private const val MIN_DISTANCE = 200.0//70.0


class BoundarySampler: VertexSampler {
    override fun sample(area: Area, allAreas: List<Area>): GtspSet {
        val polygons = allAreas.map { it.polygon }

        val polygon = area.polygon
        val others = polygons.filter { it != polygon }
        val nodes = polygon.sampleBoundary(others).map { coordinate ->
            GtspNode.create(coordinate)
        }

        return GtspSet(
            id = area.id,
            members = nodes
        )
    }
}

private fun Polygon.sampleBoundary(others: List<Polygon>): List<Coordinate> {
    val sample = mutableListOf<Coordinate>()

    val centroid = this.centroid.coordinate
    var current = coordinates.first()
    sample.add(current)

    val distance = HaversineDistanceFunction()

    coordinates.forEach { coordinate ->
        val coord = pullTowardsCentroid(centroid, coordinate)

        // we discard any vertex which does not border any other area (this reduces the number of vertices on the outside)
        val intersectionLine = createBoundaryIntersectionLine(centroid, coord)
        if (!others.none { it.intersects(intersectionLine) }) {
            val dist = distance.calculate(coord, current)
            if (dist > MIN_DISTANCE) {
                sample.add(coord)
                current = coord
            }
        }
    }

    return sample
}

// returns a line which can be used to check if moving from the center via the boundary points hits another geometry
// if not, the polygon is itself the outside boundary
private fun createBoundaryIntersectionLine(center: Coordinate, boundaryPoint: Coordinate): LineString {
    // vector pointing from center to boundary point
    val vector = Coordinate(
        boundaryPoint.x - center.x,
        boundaryPoint.y - center.y
    )

    // create line which extends from center to twice the boundary point
    return GeometryFactory().createLineString(
        listOf(
            center,
            Coordinate(
                center.x + 2 * vector.x,
                center.y + 2 * vector.y
            )
        ).toTypedArray()
    )
}