package com.plumedours.mire

import com.plumedours.mire.domain.*
import kotlin.math.*
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class TargetEngineTest {
    private val face = Targets.all.first()

    @Test
    fun centreAndOutside() {
        assertEquals(10, TargetEngine.score(face, 0.0, 0.0).value)
        assertTrue(TargetEngine.score(face, 0.0, 0.0).isX)
        assertEquals(0, TargetEngine.score(face, 1.01, 0.0).value)
    }

    @Test
    fun eachRing() {
        for (score in 1..10) {
            val radius = (10 - score + .5) / 10
            assertEquals(score, TargetEngine.score(face, radius, 0.0).value)
        }
    }

    @Test
    fun exactLinesCountUp() {
        for (n in 1..10) assertEquals(11 - n, TargetEngine.score(face, n / 10.0, 0.0).value)
    }

    @Test
    fun lineCutterUsesTubeRadius() {
        assertEquals(9, TargetEngine.score(face, .105, 0.0).value)
        assertEquals(10, TargetEngine.score(face, .105, 0.0, 6.0).value)
        assertEquals(9, TargetEngine.score(face, .12, 0.0, 6.0).value)
    }

    @Test
    fun compoundInnerTen() {
        val f = face.copy(rule = Rule.INNER_TEN)
        assertEquals(10, TargetEngine.score(f, .05, 0.0).value)
        assertEquals(9, TargetEngine.score(f, .051, 0.0).value)
        assertEquals(9, TargetEngine.score(f, .15, 0.0).value)
    }

    @Test
    fun reducedFaceMissesBelowSix() {
        val f = face.copy(reduced = true)
        assertEquals(6, TargetEngine.score(f, .5, 0.0).value)
        assertEquals(0, TargetEngine.score(f, .501, 0.0).value)
    }

    @Test
    fun fieldScores() {
        val f = face.copy(rule = Rule.FIELD)
        for ((score, radius) in
            listOf(6 to .05, 5 to .15, 4 to .30, 3 to .50, 2 to .70, 1 to .9)) assertEquals(
            score,
            TargetEngine.score(f, radius, 0.0).value,
        )
    }

    @Test
    fun worcesterScores() {
        val f = face.copy(rule = Rule.WORCESTER)
        for (n in 1..5) assertEquals(6 - n, TargetEngine.score(f, (n - .5) / 5, 0.0).value)
    }

    @Test
    fun balloonTubeAndBlank() {
        val b = face.copy(rule = Rule.BALLOON, diameterCm = 5.0)
        assertEquals(0, TargetEngine.score(b, 1.05, 0.0).value)
        assertEquals(1, TargetEngine.score(b, 1.05, 0.0, 3.0).value)
        assertEquals(0, TargetEngine.score(face.copy(rule = Rule.BLANK), 0.0, 0.0).value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun noAutomaticBeursault() {
        TargetEngine.score(face.copy(rule = Rule.BEURSAULT), 0.0, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNaN() {
        TargetEngine.score(face, Double.NaN, 0.0)
    }

    @Test
    fun allLayoutsRoundTrip() {
        for (t in Targets.all.filter { it.positionalAnalysis }) for (i in 0 until t.spotCount) {
            val h = Impact(sessionId = "s", end = 1, number = 1, u = .12, v = -.09, spot = i)
            val p = TargetEngine.onBoard(t, h)!!
            val q = TargetEngine.locate(t, p.x, p.y, i)
            assertEquals(.12, q.u, 1e-9)
            assertEquals(-.09, q.v, 1e-9)
            assertEquals(i, q.spot)
        }
    }

    @Test
    fun threeCentresOverlayToZero() {
        val t = face.copy(layout = Layout.VERTICAL)
        val hits =
            TargetEngine.geometry(t).mapIndexed { i, g ->
                val p = TargetEngine.locate(t, g.center.x, g.center.y)
                Impact(sessionId = "s", end = 1, number = i + 1, u = p.u, v = p.v, spot = p.spot)
            }
        val a = TargetEngine.analyze(t, hits)!!
        assertEquals(0.0, a.extremeSpreadCm, 1e-9)
        assertEquals(0.0, a.offsetCm, 1e-9)
    }

    @Test
    fun legacyTripleConversion() {
        val t = face.copy(layout = Layout.VERTICAL)
        for ((i, y) in listOf(.205, .5, .795).withIndex()) {
            val p = TargetEngine.legacyPoint(t, .5, y)!!
            assertEquals(i, p.spot)
            assertEquals(0.0, p.u, 1e-9)
            assertEquals(0.0, p.v, 1e-9)
            assertFalse(p.uncertain)
        }
    }

    @Test
    fun legacyAmbiguousExcluded() {
        val t = face.copy(layout = Layout.VERTICAL)
        assertTrue(TargetEngine.legacyPoint(t, .5, .3525)!!.uncertain)
    }

    @Test
    fun legacyBalloonScale() {
        val p = TargetEngine.legacyPoint(face.copy(rule = Rule.BALLOON), .62, .5)!!
        assertEquals(1.0, p.u, 1e-9)
    }

    @Test
    fun analysisUnitsAndUnknownPositions() {
        val hits =
            listOf(
                Impact(sessionId = "s", end = 1, number = 1, u = -.1, v = 0.0),
                Impact(sessionId = "s", end = 1, number = 2, u = .1, v = 0.0),
                Impact(sessionId = "s", end = 1, number = 3, score = 10, manual = true),
            )
        val a = TargetEngine.analyze(face, hits)!!
        assertEquals(2, a.count)
        assertEquals(4.0, a.extremeSpreadCm, 1e-9)
        assertEquals(2.0, a.meanRadiusCm, 1e-9)
    }

    @Test
    fun uncertainPositionsExcluded() {
        assertNull(
            TargetEngine.analyze(
                face,
                listOf(
                    Impact(sessionId = "s", end = 1, number = 1, u = .1, v = .1, uncertain = true)
                ),
            )
        )
    }

    @Test
    fun convexDiameterMatchesBruteForce() {
        val random = Random(42)
        repeat(40) {
            val h =
                (1..70).map {
                    Impact(
                        sessionId = "s",
                        end = 1,
                        number = it,
                        u = random.nextDouble(-1.0, 1.0),
                        v = random.nextDouble(-1.0, 1.0),
                    )
                }
            var max = 0.0
            for (a in h) for (b in h) max = max(max, hypot(a.u!! - b.u!!, a.v!! - b.v!!))
            assertEquals(
                max * face.diameterCm / 2,
                TargetEngine.analyze(face, h)!!.extremeSpreadCm,
                1e-8,
            )
        }
    }

    @Test
    fun singleAndDuplicatePoints() {
        val h = Impact(sessionId = "s", end = 1, number = 1, u = .1, v = .1)
        assertEquals(0.0, TargetEngine.analyze(face, listOf(h, h))!!.extremeSpreadCm, 1e-9)
    }
}
