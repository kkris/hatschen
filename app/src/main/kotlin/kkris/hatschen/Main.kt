package kkris.hatschen

import com.fasterxml.jackson.databind.ObjectMapper
import kkris.hatschen.distance.DistanceFunction
import kkris.hatschen.distance.DistanceMetric
import kkris.hatschen.distance.DistanceMetric.FOOTPATH
import kkris.hatschen.distance.DistanceMetric.values
import kkris.hatschen.gtsp.GtspInstanceFactory
import kkris.hatschen.gtsp.GtspTour
import kkris.hatschen.parser.GeoJsonParser
import kkris.hatschen.sampling.VertexSampleType
import kkris.hatschen.util.Cmd
import kkris.hatschen.util.haversine
import kkris.hatschen.util.latitude
import kkris.hatschen.util.longitude
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import mu.KotlinLogging
import org.geojson.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
    val parser = ArgParser("hatschen")
    val areas by parser.option(ArgType.String, shortName = "a", description = "GeoJSON file containing the areas as polygons")
        .required()
    val sample by parser.option(
        ArgType.Choice(listOf("centroid", "center-bias", "grid", "boundary"), { it }), shortName = "s", description = "Vertex sampling method within areas")
        .default("grid")
    val distance by parser.option(
        ArgType.Choice(listOf("haversine", "manhattan", "footpath"), { it }), shortName = "d", description = "Distance calculation method")
        .default("haversine")
    val osmFile by parser.option(ArgType.String, description = "OpenStreetMaps file used for footpath routing (osm.pbf file)")
    val outputDir by parser.option(ArgType.String, shortName = "o", description = "Output directory to which result files are written").required()
    parser.parse(args)

    if (!File(areas).exists()) {
        throw IllegalArgumentException("Area GeoJSON file '$areas' does not exist.")
    }
    if (!File(outputDir).exists()) {
        throw IllegalArgumentException("Output directory '$outputDir' does not exist.")
    }
    if (osmFile != null && !File(osmFile).exists()) {
        throw IllegalArgumentException("OSM file '$osmFile' does not exist.")
    }

    run(
        areas,
        VertexSampleType.values().first { it.value == sample },
        values().first { it.value == distance },
        osmFile,
        Paths.get(outputDir)
    )
}

private fun run(areasPath: String, vertexSampleType: VertexSampleType, distanceMetric: DistanceMetric, osmFile: String?, outputDirectory: Path) {
    val logger = KotlinLogging.logger {}

    logger.info { "Computing shortest tour." }
    logger.debug { "Sampling method: $vertexSampleType" }
    logger.debug { "Distance metric: $distanceMetric" }

    // load areas
    logger.info { "Parsing areas..." }
    val areas = GeoJsonParser(areasPath).parseAreas()
    logger.info { "Found ${areas.size} areas" }

    // generate GTSP instance
    logger.info { "Creating GTSP instance..." }
    val instance = GtspInstanceFactory
        .create(areas, vertexSampleType)
    logger.info { "Created GTSP instance (#vertices: ${instance.nodes.size})" }

    val verticesPath = outputDirectory.resolve("vertices.json")
    logger.debug { "Writing vertices to '$verticesPath'" }
    verticesPath
        .toFile()
        .writeText(
            ObjectMapper().writeValueAsString(instance.vertices)
        )

    logger.info { "Calculating distances and formatting GTSP problem instance..." }
    val weightingFunction = DistanceFunction.create(distanceMetric, osmFile)
    val formattedInstance = instance.format(weightingFunction)

    val gtspPath = outputDirectory.resolve("instance.gtsp")
    logger.debug { "Writing GTSP instance to '$gtspPath'" }
    gtspPath
        .toFile()
        .writeText(formattedInstance)

    logger.info { "Starting GLNS solver..." }
    val tourPath = outputDirectory.resolve("tour.gtsp")
    Cmd.run(listOf(
        "julia",
        "-e",
        "import GLNS; GLNS.solver(\"${gtspPath}\", output = \"${tourPath}\")"
    ))
    logger.info { "GLNS solver finished. Tour written to '$tourPath'" }

    // load tour and generate a path from it (as a geojson linestring)
    val tour = GtspTour.load(tourPath)

    logger.info { "Generating path from tour..." }
    val collection = FeatureCollection()
    val pathFunction = if (osmFile != null) DistanceFunction.create(FOOTPATH, osmFile) else weightingFunction

    val closedTour = tour.tour + listOf(tour.tour.first())
    val path = closedTour.zipWithNext { a, b ->
        val n1 = instance.nodes.first { it.id == a }
        val n2 = instance.nodes.first { it.id == b }

        pathFunction.getPath(n1.coordinate, n2.coordinate).map {
            LngLatAlt(it.longitude, it.latitude)
        }
    }

    // we don't need to come back to the start, so we can remove the longest segment in the tour
    var longestSegmentIndex = -1
    var longestSegmentDistance = -1.0
    path.forEachIndexed { i, segment ->
        val distance = segment.zipWithNext { a, b ->
            haversine(a.latitude, a.longitude, b.latitude, b.longitude)
        }.sum()

        if (distance > longestSegmentDistance) {
            longestSegmentDistance = distance
            longestSegmentIndex = i
        }
    }

    val openPath = ((longestSegmentIndex + 1 until path.size) + (0 until longestSegmentIndex)).map {
        path[it]
    }

    val pathFeature = Feature()
    pathFeature.geometry = LineString(*openPath.flatten().toTypedArray())
    collection.add(pathFeature)

    tour.tour.map { nodeId ->
        val node = instance.nodes.first { it.id == nodeId }
        LngLatAlt(node.coordinate.longitude, node.coordinate.latitude)
    }.forEach { vertex ->
        val vertexFeature = Feature()
        vertexFeature.geometry = Point(vertex)
        collection.add(vertexFeature)
    }

    val tourFeaturePath = outputDirectory.resolve("tour.json")
    tourFeaturePath
        .toFile()
        .writeText(
            ObjectMapper().writeValueAsString(collection)
        )

    logger.info { "Finished. Tour written to '$tourFeaturePath'" }

    outputDirectory.resolve("areas.json")
        .toFile()
        .writeText(File(areasPath).readText())

    logger.info { "Generating KML files..." }
    Cmd.run(listOf(
        "node",
        "scripts/convert.js",
        outputDirectory.toString()
    ))
}