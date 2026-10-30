package com.plumedours.mire.domain

import kotlin.math.*

data class Point(val x: Double, val y: Double)

data class SpotGeometry(val center: Point, val scale: Double)

data class LocatedPoint(val u: Double, val v: Double, val spot: Int, val uncertain: Boolean = false)

data class ShotScore(val value: Int, val isX: Boolean = false)

data class Analysis(
    val count: Int,
    val meanU: Double,
    val meanV: Double,
    val offsetCm: Double,
    val meanRadiusCm: Double,
    val widthCm: Double,
    val heightCm: Double,
    val extremeSpreadCm: Double,
    val rmsCm: Double,
)

object TargetEngine {
    fun geometry(target: TargetSpec): List<SpotGeometry> =
        when (target.layout) {
            Layout.SINGLE ->
                listOf(
                    SpotGeometry(
                        Point(.5, .5),
                        (if (target.rule == Rule.BALLOON) .1144 else .44) / target.visibleRadius,
                    )
                )
            Layout.VERTICAL ->
                listOf(.18, .5, .82).map {
                    SpotGeometry(Point(.5, it), .145 / target.visibleRadius)
                }
            Layout.TRIANGLE ->
                listOf(Point(.5, .265), Point(.265, .71), Point(.735, .71)).map {
                    SpotGeometry(it, .215 / target.visibleRadius)
                }
            Layout.QUAD ->
                listOf(Point(.265, .265), Point(.735, .265), Point(.265, .735), Point(.735, .735))
                    .map { SpotGeometry(it, .215 / target.visibleRadius) }
            Layout.FIELD_GRID ->
                (0..2).flatMap { row ->
                    (0..3).map { col ->
                        SpotGeometry(
                            Point(.14 + col * .24, .19 + row * .31),
                            .105 / target.visibleRadius,
                        )
                    }
                }
        }

    fun locate(target: TargetSpec, x: Double, y: Double, preferredSpot: Int? = null): LocatedPoint {
        val geometries = geometry(target)
        val index =
            preferredSpot?.coerceIn(geometries.indices)
                ?: geometries.indices.minBy {
                    hypot(x - geometries[it].center.x, y - geometries[it].center.y)
                }
        val g = geometries[index]
        val display = Point((x - g.center.x) / g.scale, (y - g.center.y) / g.scale)
        val local =
            if (target.animalTarget) AnimalGeometry.fromDisplay(target, display.x, display.y)
            else display
        return LocatedPoint(local.x, local.y, index)
    }

    fun onBoard(target: TargetSpec, impact: Impact): Point? {
        val u = impact.u ?: return null
        val v = impact.v ?: return null
        val spot = impact.spot ?: return null
        val g = geometry(target).getOrNull(spot) ?: return null
        val local = if (target.animalTarget) AnimalGeometry.toDisplay(target, u, v) else Point(u, v)
        return Point(g.center.x + local.x * g.scale, g.center.y + local.y * g.scale)
    }

    fun score(
        target: TargetSpec,
        u: Double,
        v: Double,
        diameterMm: Double = 0.0,
        arrowNumber: Int = 1,
    ): ShotScore {
        require(u.isFinite() && v.isFinite() && diameterMm.isFinite() && diameterMm >= 0)
        if (target.rule == Rule.BLANK) return ShotScore(0)
        require(target.canPlace) {
            "Beursault : valider la zone avec les boutons / confirm the zone using score buttons"
        }
        if (target.animalTarget) return AnimalGeometry.score(target, u, v, arrowNumber)
        val centerRadius = hypot(u, v)
        // Both values are radii: physical tube radius divided by nominal target radius.
        val tube = diameterMm / (target.diameterCm * 10.0)
        val radius = max(0.0, centerRadius - tube)
        if (radius > target.visibleRadius + 1e-10) return ShotScore(0)
        return when (target.rule) {
            Rule.BALLOON -> ShotScore(if (radius <= 1.0 + 1e-10) 1 else 0)
            Rule.WORCESTER -> ShotScore((6 - ceil((radius - 1e-10) * 5).toInt()).coerceIn(1, 5))
            Rule.FIELD -> {
                val s =
                    when {
                        radius <= .1 + 1e-10 -> 6
                        radius <= .2 + 1e-10 -> 5
                        radius <= .4 + 1e-10 -> 4
                        radius <= .6 + 1e-10 -> 3
                        radius <= .8 + 1e-10 -> 2
                        else -> 1
                    }
                ShotScore(s)
            }
            else -> {
                val base = (11 - ceil((radius - 1e-10) * 10).toInt()).coerceIn(1, 10)
                val value =
                    if (target.rule == Rule.INNER_TEN && radius > .05 + 1e-10 && base == 10) 9
                    else base
                ShotScore(value, target.centreStyle != "recurve" && radius <= .05 + 1e-10)
            }
        }
    }

    fun allowedScores(target: TargetSpec, arrowNumber: Int = 1): List<Int> =
        when (target.rule) {
            Rule.THREE_D_11 -> listOf(11, 10, 8, 5, 0)
            Rule.THREE_D_12 -> listOf(12, 10, 8, 5, 0)
            Rule.THREE_D_X -> listOf(10, 8, 5, 0)
            Rule.NATURE -> if (arrowNumber % 2 == 1) listOf(20, 15, 0) else listOf(15, 10, 0)
            Rule.BEURSAULT -> listOf(4, 3, 2, 1, 0)
            Rule.BLANK -> listOf(0)
            else -> (target.maxScore downTo target.lowestScore).toList() + 0
        }

    fun maximum(target: TargetSpec, arrowNumber: Int) =
        if (target.rule == Rule.NATURE && arrowNumber % 2 == 0) 15 else target.maxScore

    fun analyze(target: TargetSpec, impacts: List<Impact>): Analysis? {
        if (!target.positionalAnalysis) return null
        val points =
            impacts.filter { it.u != null && it.v != null && !it.uncertain && it.spot != null }
        if (points.isEmpty()) return null
        val cx = points.sumOf { it.u!! } / points.size
        val cy = points.sumOf { it.v!! } / points.size
        val scale = target.diameterCm / 2
        val radii = points.map { hypot(it.u!! - cx, it.v!! - cy) }
        // Exact diameter of convex hull; avoids a quadratic scan of all arrows for long sessions.
        val hull = hull(points.map { Point(it.u!!, it.v!!) })
        var extreme = 0.0
        if (hull.size == 2) extreme = distance(hull[0], hull[1])
        if (hull.size > 2) {
            var j = 1
            for (i in hull.indices) {
                val next = (i + 1) % hull.size
                while (
                    abs(cross(hull[i], hull[next], hull[(j + 1) % hull.size])) >
                        abs(cross(hull[i], hull[next], hull[j])) + 1e-12
                ) j = (j + 1) % hull.size
                extreme =
                    max(extreme, max(distance(hull[i], hull[j]), distance(hull[next], hull[j])))
            }
        }
        return Analysis(
            points.size,
            cx,
            cy,
            hypot(cx, cy) * scale,
            radii.average() * scale,
            (points.maxOf { it.u!! } - points.minOf { it.u!! }) * scale,
            (points.maxOf { it.v!! } - points.minOf { it.v!! }) * scale,
            extreme * scale,
            sqrt(radii.sumOf { it * it } / points.size) * scale,
        )
    }

    private fun distance(a: Point, b: Point) = hypot(a.x - b.x, a.y - b.y)

    private fun cross(a: Point, b: Point, c: Point) =
        (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

    private fun hull(points: List<Point>): List<Point> {
        val p = points.distinct().sortedWith(compareBy({ it.x }, { it.y }))
        if (p.size <= 2) return p
        val low = mutableListOf<Point>()
        val high = mutableListOf<Point>()
        for (x in p) {
            while (low.size >= 2 && cross(low[low.size - 2], low.last(), x) <= 0) low.removeAt(
                low.lastIndex
            )
            low.add(x)
        }
        for (x in p.asReversed()) {
            while (high.size >= 2 && cross(high[high.size - 2], high.last(), x) <= 0) high.removeAt(
                high.lastIndex
            )
            high.add(x)
        }
        return low.dropLast(1) + high.dropLast(1)
    }

    fun legacyPoint(target: TargetSpec, x: Double, y: Double): LocatedPoint? {
        if (!target.positionalAnalysis) return null
        if (target.layout == Layout.VERTICAL) {
            val centers = listOf(.205, .5, .795)
            val index = centers.indices.minBy { hypot(x - .5, y - centers[it]) }
            val distances = centers.map { hypot(x - .5, y - it) }.sorted()
            // Old visible spot radius .145 corresponds to half of the nominal full-face radius.
            return LocatedPoint(
                (x - .5) / .29,
                (y - centers[index]) / .29,
                index,
                distances[0] > .145 || abs(distances[1] - distances[0]) < .015,
            )
        }
        // The old balloon radius was .12 of the square, unlike concentric faces (.5).
        val scale = if (target.rule == Rule.BALLOON) .12 else .5
        return LocatedPoint((x - .5) / scale, (y - .5) / scale, 0)
    }
}
