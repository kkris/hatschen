package kkris.hatschen.gtsp

import java.nio.file.Path

data class GtspTour(
    val cost: Double,
    val tour: List<Int>
) {
    companion object {
        fun load(path: Path): GtspTour {
            return parse(path.toFile().readText())
        }

        private fun parse(s: String): GtspTour {
            var cost = 0.0
            var tour = emptyList<Int>()

            s.split("\n")
                .map { line ->
                    line
                        .split(":")
                        .map { it.trim() }
                        .let { Pair(it[0], it[1]) }
                }.forEach { (key, value) ->
                    when (key) {
                        "Tour Cost" -> cost = value.toDouble()
                        "Tour" -> tour = value
                            .replace("[", "")
                            .replace("]", "")
                            .split(",")
                            .map { it.trim().toInt() }
                    }
                }

            return GtspTour(cost, tour)
        }
    }
}