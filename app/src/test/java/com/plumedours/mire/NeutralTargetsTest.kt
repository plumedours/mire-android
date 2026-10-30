package com.plumedours.mire

import com.plumedours.mire.data.*
import com.plumedours.mire.domain.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class NeutralTargetsTest {
    @Test
    fun catalogueGroupsSizesAndScoringVariantsButKeepsCustomTargets() {
        val custom = TargetSpec("custom", "Ma cible", 33.0)
        val choices = TargetCatalog.choices(Targets.all + custom)
        assertEquals(12, choices.size)
        assertEquals(choices.size, choices.map { it.id }.distinct().size)
        assertEquals(
            1,
            choices.count { it.rule in listOf(Rule.THREE_D_11, Rule.THREE_D_12, Rule.THREE_D_X) },
        )
        assertEquals(1, choices.count { it.family == "field" })
        assertEquals(custom, choices.last())
        assertTrue(choices.filter { it.animalTarget }.all { it.zoneStyle == "neutral" })
    }

    @Test
    fun neutralZonesHaveTheSameBoundariesForEverySpecies() {
        for (animal in Animal.entries) {
            val t = TargetSpec("vital", "Vital", rule = Rule.THREE_D_12, animal = animal)
            for ((x, score) in
                listOf(0.0 to 12, .2 to 10, .5 to 8, .8 to 5, 1.0 to 0)) assertEquals(
                score,
                TargetEngine.score(t, x, 0.0).value,
            )
        }
    }

    @Test
    fun natureWoundAndVitalFollowArrowOrder() {
        val t = TargetSpec("nature", "Nature", rule = Rule.NATURE)
        assertEquals(20, TargetEngine.score(t, 0.0, 0.0, arrowNumber = 1).value)
        assertEquals(15, TargetEngine.score(t, 0.0, 0.0, arrowNumber = 2).value)
        assertEquals(15, TargetEngine.score(t, .8, 0.0, arrowNumber = 1).value)
        assertEquals(10, TargetEngine.score(t, .8, 0.0, arrowNumber = 2).value)
        assertEquals(0, TargetEngine.score(t, 0.0, .9).value)
    }

    @Test
    fun legacyAnimalCoordinatesAreNotRewrittenDuringBackup() {
        val original = TargetSpec("old", "Ancien", rule = Rule.THREE_D_11, animal = Animal.DEER)
        val old = Codec.target(original).apply { remove("zoneStyle") }
        val target = Codec.target(old)
        assertEquals("animal-v1", target.zoneStyle)
        val s = Session(name = "Ancien", target = target)
        val i =
            Impact(sessionId = s.id, end = 1, number = 1, u = -.1, v = .005, spot = 0, score = 11)
        val data = AppData(sessions = listOf(s), impacts = listOf(i))
        val restored = BackupImporter.preview(Codec.backup(data)).data
        assertEquals(data, restored)
        val screen = TargetEngine.onBoard(target, i)!!
        assertEquals(.5, screen.x, 1e-9)
        assertEquals(.5, screen.y, 1e-9)
        val raw = TargetEngine.locate(target, screen.x, screen.y)
        assertEquals(i.u!!, raw.u, 1e-9)
        assertEquals(i.v!!, raw.v, 1e-9)
    }

    @Test
    fun configuredTargetSnapshotSurvivesCompleteBackup() {
        val t =
            TargetCatalog.choices(Targets.all)
                .first()
                .copy(
                    diameterCm = 122.0,
                    rule = Rule.INNER_TEN,
                    outerScore = 5,
                    centreStyle = "compound",
                )
        val d = AppData(sessions = listOf(Session(name = "Réglages", target = t)))
        assertEquals(d, BackupImporter.preview(Codec.backup(d)).data)
        assertNotEquals(Targets.all.first(), d.sessions.first().target)
    }

    @Test
    fun smallBalloonUsesTheSameGeometryForDrawingAndScoring() {
        val t = Targets.all.first { it.rule == Rule.BALLOON }
        val g = TargetEngine.geometry(t).single()
        assertEquals(5.0, t.diameterCm, 1e-9)
        assertTrue(g.scale < .15)
        val inside = TargetEngine.locate(t, .5 + g.scale * .99, .5)
        val outside = TargetEngine.locate(t, .5 + g.scale * 1.01, .5)
        assertEquals(1, TargetEngine.score(t, inside.u, inside.v).value)
        assertEquals(0, TargetEngine.score(t, outside.u, outside.v).value)
        // A 5 mm shaft catches a line 0.1 normalized radii outside a 5 cm balloon.
        assertEquals(1, TargetEngine.score(t, 1.09, 0.0, diameterMm = 5.0).value)
    }

    @Test
    fun previousNativeBackupVersionsRemainImportable() {
        val data = AppData()
        for (version in 1..3) assertEquals(
            data,
            BackupImporter.preview(
                    JSONObject(Codec.backup(data)).put("version", version).toString()
                )
                .data,
        )
    }
}
