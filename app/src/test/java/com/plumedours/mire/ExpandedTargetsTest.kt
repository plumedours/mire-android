package com.plumedours.mire

import com.plumedours.mire.data.*
import com.plumedours.mire.domain.*
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class ExpandedTargetsTest {
    @Test
    fun allFamiliesArePresent() {
        assertEquals(
            setOf("target", "multi", "field", "animal", "balloon", "tradition"),
            Targets.all.map { it.family }.toSet(),
        )
        assertEquals(Targets.all.size, Targets.all.map { it.id }.distinct().size)
    }

    @Test
    fun fourAndTwelveFaceLayoutsMatchTheirCounts() {
        for (t in Targets.all) {
            assertEquals(t.spotCount, TargetEngine.geometry(t).size)
        }
    }

    @Test
    fun fullFieldFacesAreNotCroppedOnMultiSpot() {
        for (t in Targets.all.filter { it.rule == Rule.FIELD }) assertEquals(
            1.0,
            t.visibleRadius,
            1e-9,
        )
    }

    @Test
    fun sixRingQuadIncludesFive() {
        val t = Targets.all.first { it.id == "quad80" }
        assertEquals(.6, t.visibleRadius, 1e-9)
        assertEquals(5, TargetEngine.score(t, .60, 0.0).value)
        assertEquals(0, TargetEngine.score(t, .601, 0.0).value)
    }

    @Test
    fun allAnimalCentresScoreAndMissesRemainMisses() {
        for (t in Targets.all.filter { it.animalTarget }) {
            val k = AnimalGeometry.kill(t.animal)
            val hit = TargetEngine.score(t, k.x, k.y)
            assertEquals(if (t.rule == Rule.NATURE) 20 else t.maxScore, hit.value)
            assertEquals(0, TargetEngine.score(t, 1.2, 1.2).value)
        }
    }

    @Test
    fun natureUsesArrowOrder() {
        val t = Targets.all.first { it.rule == Rule.NATURE }
        val p = AnimalGeometry.kill(t.animal)
        assertEquals(20, TargetEngine.score(t, p.x, p.y, arrowNumber = 1).value)
        assertEquals(15, TargetEngine.score(t, p.x, p.y, arrowNumber = 2).value)
        assertEquals(listOf(20, 15, 0), TargetEngine.allowedScores(t, 1))
        assertEquals(listOf(15, 10, 0), TargetEngine.allowedScores(t, 2))
        assertEquals(35, TargetEngine.maximum(t, 1) + TargetEngine.maximum(t, 2))
    }

    @Test
    fun animalScoreAndXAreDistinct() {
        val t = Targets.all.first { it.rule == Rule.THREE_D_X }
        val p = AnimalGeometry.kill(t.animal)
        assertTrue(TargetEngine.score(t, p.x, p.y).isX)
        assertEquals(10, TargetEngine.score(t, p.x, p.y).value)
    }

    @Test
    fun animalPositionsDoNotBecomeCircularCentimetres() {
        val t = Targets.all.first { it.animalTarget }
        assertNull(
            TargetEngine.analyze(
                t,
                listOf(Impact(sessionId = "s", end = 1, number = 1, u = .0, v = .0)),
            )
        )
    }

    @Test
    fun catalogueUpgradePreservesExistingDefinitions() {
        val custom = Targets.all.first().copy(name = "Custom", diameterCm = 55.0)
        val upgraded = Targets.complete(listOf(custom))
        assertEquals(Targets.all.size, upgraded.size)
        assertEquals(custom, upgraded.first { it.id == custom.id })
    }

    @Test
    fun newTargetPropertiesSurviveBackups() {
        val t = Targets.all.first { it.rule == Rule.NATURE && it.animal == Animal.FOX }
        val s = Session(name = "Nature", target = t)
        val d = AppData(sessions = listOf(s))
        assertEquals(d, BackupImporter.preview(Codec.backup(d)).data)
        assertEquals(t, Codec.target(Codec.target(t)))
    }

    @Test
    fun olderMireTargetStillLoads() {
        val old = Codec.target(Targets.all.first())
        old.remove("outerScore")
        old.remove("centreStyle")
        old.remove("animal")
        val target = Codec.target(old)
        assertEquals(1.0, target.visibleRadius, 1e-9)
        assertEquals(Animal.BOAR, target.animal)
    }

    @Test
    fun oldNativeBackupVersionIsStillAccepted() {
        val d = AppData()
        val old = org.json.JSONObject(Codec.backup(d)).put("version", 1)
        assertEquals(d, BackupImporter.preview(old.toString()).data)
    }

    @Test
    fun viewportRoundTripsThroughZoomAndPan() {
        val random = Random(19)
        repeat(100) {
            val v =
                BoardViewport(
                    600.0,
                    random.nextDouble(1.0, 5.0),
                    random.nextDouble(-300.0, 300.0),
                    random.nextDouble(-300.0, 300.0),
                )
            val x = random.nextDouble()
            val y = random.nextDouble()
            val p = v.toScreen(x, y)
            val q = v.toBoard(p.x, p.y)
            assertEquals(x, q.x, 1e-9)
            assertEquals(y, q.y, 1e-9)
        }
    }

    @Test
    fun magnifierFollowsBothAxesWithoutFlipping() {
        val v = BoardViewport(600.0)
        val a = v.magnifier(250.0, 350.0, 60.0, 20.0)
        val b = v.magnifier(270.0, 370.0, 60.0, 20.0)
        assertEquals(20.0, b.x - a.x, 1e-9)
        assertEquals(20.0, b.y - a.y, 1e-9)
    }

    @Test
    fun magnifierStaysWithinHorizontalEdges() {
        val v = BoardViewport(600.0)
        for (x in listOf(-100.0, 0.0, 300.0, 600.0, 700.0)) {
            val p = v.magnifier(x, 300.0, 60.0, 20.0)
            assertTrue(p.x in 60.0..540.0)
            assertEquals(220.0, p.y, 1e-9)
        }
    }

    @Test
    fun fieldTenAndTwentyPercentBoundariesAreCorrect() {
        val t = Targets.all.first { it.rule == Rule.FIELD }
        assertEquals(6, TargetEngine.score(t, .1, 0.0).value)
        assertEquals(5, TargetEngine.score(t, .1001, 0.0).value)
        assertEquals(5, TargetEngine.score(t, .2, 0.0).value)
        assertEquals(4, TargetEngine.score(t, .2001, 0.0).value)
    }

    @Test
    fun recurveTripleDoesNotInventXCircle() {
        val t = Targets.all.first { it.centreStyle == "recurve" }
        assertEquals(10, TargetEngine.score(t, 0.0, 0.0).value)
        assertFalse(TargetEngine.score(t, 0.0, 0.0).isX)
    }
}
