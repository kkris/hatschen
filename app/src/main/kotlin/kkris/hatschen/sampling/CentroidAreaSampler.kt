package kkris.hatschen.sampling

import kkris.hatschen.gtsp.GtspNode
import kkris.hatschen.gtsp.GtspSet
import kkris.hatschen.parser.Area

class CentroidAreaSampler: VertexSampler {
    override fun sample(area: Area, allAreas: List<Area>): GtspSet {
        val centroid = area.polygon.centroid.coordinate

        return GtspSet(
            id = area.id,
            members = listOf(
                GtspNode.create(centroid),
                GtspNode.create(centroid)
            )
        )
    }
}