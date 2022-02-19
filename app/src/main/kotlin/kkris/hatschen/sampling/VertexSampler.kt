package kkris.hatschen.sampling

import kkris.hatschen.gtsp.GtspSet
import kkris.hatschen.parser.Area

enum class VertexSampleType(val value: String) {
    CENTROID("centroid"),
    CENTER_BIAS("center-bias"),
    GRID("grid"),
    BOUNDARY("boundary")
}

interface VertexSampler {
    fun sample(area: Area, allAreas: List<Area>): GtspSet

    companion object {
        fun create(type: VertexSampleType): VertexSampler {
            return when (type) {
                VertexSampleType.CENTROID -> CentroidAreaSampler()
                VertexSampleType.CENTER_BIAS -> CenterBiasAreaSampler()
                VertexSampleType.GRID -> GridSampler()
                VertexSampleType.BOUNDARY -> BoundarySampler()
            }
        }
    }
}