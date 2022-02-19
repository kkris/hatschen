package kkris.hatschen.gtsp

import kkris.hatschen.parser.Area
import kkris.hatschen.sampling.VertexSampleType
import kkris.hatschen.sampling.VertexSampler

object GtspInstanceFactory {
    fun create(areas: List<Area>, vertexSampleType: VertexSampleType): GtspInstance {
        val sampler = VertexSampler.create(vertexSampleType)
        val sets = areas.map {
            sampler.sample(it, areas)
        }

        return GtspInstance(
            name = vertexSampleType.value,
            sets = sets
        )
    }
}