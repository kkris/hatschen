package kkris.hatschen.gtsp

import kkris.hatschen.distance.DistanceFunction
import kkris.hatschen.distance.HaversineDistanceFunction
import kkris.hatschen.util.latitude
import kkris.hatschen.util.longitude
import kotlinx.coroutines.*
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.LngLatAlt
import org.geojson.Point
import org.locationtech.jts.geom.Coordinate
import java.util.concurrent.atomic.AtomicInteger

data class GtspNode private constructor(
    val id: Int,
    val coordinate: Coordinate
) {
    companion object {
        private val idSequence = AtomicInteger(0)
        private fun nextId() = idSequence.incrementAndGet()

        fun create(coordinate: Coordinate) = GtspNode(
            nextId(),
            coordinate
        )
    }
}

data class GtspSet(
    val id: Int,
    val members: List<GtspNode>
) {
    init {
        if (members.size < 2) {
            throw IllegalStateException("GtspSet.members must have at least two nodes (can be with equal coordinates), otherwise the solver will not correctly compute a good tour.")
        }
    }
}

data class GtspInstance(
    val name: String,
    val sets: List<GtspSet> = emptyList()
) {
    val nodes: List<GtspNode>
        get() = sets.flatMap { it.members }

    val vertices: FeatureCollection
        get() {
            val collection = FeatureCollection()
            nodes.forEach { node ->
                val feature = Feature()
                feature.geometry = Point(LngLatAlt(node.coordinate.longitude, node.coordinate.latitude))
                collection.add(feature)
            }

            return collection
        }

    fun format(distanceFunction: DistanceFunction): String {
        val nodes = sets.flatMap { it.members }

        val setsSection = sets.joinToString(separator = "\n") {
            val ids = it.members.map { it.id }.joinToString(separator = " ")
            "${it.id} $ids -1"
        }

        val weights = calculateWeights(distanceFunction)

        return """
NAME : $name
TYPE : GTSP
DIMENSION : ${nodes.size}
GTSP_SETS : ${sets.size}
EDGE_WEIGHT_TYPE : EXPLICIT
EDGE_WEIGHT_FORMAT : FULL_MATRIX
EDGE_WEIGHT_SECTION
$weights
GTSP_SET_SECTION
$setsSection
EOF
        """.trimIndent()
    }

    private fun calculateWeights(distanceFunction: DistanceFunction): String {
        return runBlocking(Dispatchers.Default) { // MAXIMUM PARALLELISM
            nodes.pmap { n1 ->
                nodes.joinToString(separator = " ") { n2 ->
                    distanceFunction.calculate(n1.coordinate, n2.coordinate).toString()
                }
            }.joinToString(separator = "\n")
        }
    }
}

private suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}
