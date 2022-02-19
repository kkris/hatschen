package kkris.hatschen.sampling

import kkris.hatschen.gtsp.GtspInstance
import kkris.hatschen.gtsp.GtspNode
import kkris.hatschen.gtsp.GtspSet
import kkris.hatschen.parser.Area
import kkris.hatschen.util.pullTowardsCentroid
import org.locationtech.jts.geom.Coordinate

class CenterBiasAreaSampler: VertexSampler {
    companion object {
        private val CENTER = Coordinate(16.368832165735192, 48.20883540210374)
    }

    override fun sample(area: Area, allAreas: List<Area>): GtspSet {
        val nearestToCenter = area.polygon.coordinates.minByOrNull {
            it.distance(CENTER)
        }?.let {
            pullTowardsCentroid(area.polygon.centroid.coordinate, it)
        }!!

        return GtspSet(
            id = area.id,
            members = listOf(
                GtspNode.create(nearestToCenter),
                GtspNode.create(nearestToCenter),
            )
        )
    }
}