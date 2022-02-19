package kkris.hatschen.parser

import com.fasterxml.jackson.databind.ObjectMapper
import org.geojson.FeatureCollection
import org.geojson.Polygon
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.util.GeometricShapeFactory
import java.io.File

data class Area(
    val id: Int,
    val polygon: org.locationtech.jts.geom.Polygon
)

class GeoJsonParser(private val jsonPath: String) {

    fun parseAreas(): List<Area> {
        return ObjectMapper()
            .readValue(File(jsonPath).inputStream(), FeatureCollection::class.java)
            .map {
                val areaId = it.getProperty("BEZNR") as Int

                val coordinates = (it.geometry as Polygon).coordinates.flatten().map { c ->
                    Coordinate(c.longitude, c.latitude)
                }

                val jtsPolygon = GeometryFactory().createPolygon(coordinates.toTypedArray())
                Area(areaId, jtsPolygon)
            }
    }
}