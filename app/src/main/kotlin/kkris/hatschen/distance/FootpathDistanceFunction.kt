package kkris.hatschen.distance

import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.config.Profile
import com.graphhopper.routing.AlgorithmOptions
import kkris.hatschen.util.latitude
import kkris.hatschen.util.longitude
import org.locationtech.jts.geom.Coordinate
import kotlin.io.path.createTempDirectory

// in meters: maximum distance worth computing
// other distances are approximated using the manhattan distance
private const val DIRECT_DISTANCE_THRESHOLD = 4_000.0

class FootpathDistanceFunction(private val osmPath: String): DistanceFunction {
    private val haversine = HaversineDistanceFunction()
    private val manhattan = ManhattanDistanceFunction()

    private val gh: GraphHopper by lazy {
        val instance = GraphHopper()

        instance.profiles = listOf(
            Profile("foot").setVehicle("foot").setWeighting("shortest"),
        )
        instance.osmFile = osmPath
        val graphHopperLocation = createTempDirectory()
        instance.graphHopperLocation = graphHopperLocation.toString()
        instance.importOrLoad()

        instance
    }

    override fun calculate(c1: Coordinate, c2: Coordinate): Int {
        val path = calculatePath(c1, c2, maxDirectDistance = DIRECT_DISTANCE_THRESHOLD)

        // if the distance exceeds the threshold, we estimate the distance using the manhattan distance
        // however, we overestimate the distance to avoid being better than the footpath distance
        return path?.best?.distance?.toInt() ?: (manhattan.calculate(c1, c2) * 1.5).toInt()
    }

    override fun getPath(c1: Coordinate, c2: Coordinate): List<Coordinate> {
        return calculatePath(c1, c2, maxDirectDistance = Double.MAX_VALUE)?.best?.points?.map {
            Coordinate(it.getLon(), it.getLat())
        } ?: listOf()
    }

    private fun calculatePath(c1: Coordinate, c2: Coordinate, maxDirectDistance: Double): GHResponse? {
        val directDistance = haversine.calculate(c1, c2)
        if (directDistance >= maxDirectDistance) {
            return null
        }

        val request = GHRequest(
            c1.latitude,
            c1.longitude,
            c2.latitude,
            c2.longitude
        )
        request.profile = "foot"

        return gh.route(request)
    }
}