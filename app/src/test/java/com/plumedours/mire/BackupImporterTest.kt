package com.plumedours.mire

import com.plumedours.mire.data.*
import com.plumedours.mire.domain.*
import org.junit.Assert.*
import org.junit.Test

class BackupImporterTest {
    private val sample =
        """{"format":"volee-backup","version":1,"tables":{"user_profile":[{"id":1,"first_name":"Max","last_name":"Test","birth_date":"1990-01-01"}],"targets":[{"id":1,"name":"Trispot","kind":"trispot","face_size_cm":40,"max_score":10}],"bows":[{"id":1,"name":"Arc","type":"Classique","notes":"à conserver"}],"arrow_sets":[{"id":1,"name":"Flèches","quantity":12,"diameter":"5.5"}],"sessions":[{"id":1,"name":"Test","kind":"training","started_at":"2026-01-01T12:00:00Z","ended_at":"2026-01-01T13:00:00Z","bow_id":1,"arrow_set_id":1,"target_id":1,"distance_m":18,"arrows_per_end":3,"completed":1}],"impacts":[{"id":1,"session_id":1,"end_number":1,"arrow_number":1,"x":0.5,"y":0.205,"score":9,"is_x":0},{"id":2,"session_id":1,"end_number":2,"arrow_number":1,"x":0.5,"y":0.5,"score":10,"is_x":1}],"session_journals":[{"session_id":1,"feeling":4,"comment":"Belle séance"}],"session_templates":[{"id":1,"name":"Modèle","kind":"training","target_id":1,"distance_m":18,"arrows_per_end":3}],"personal_goals":[{"id":1,"title":"But","kind":"arrows","target_value":300,"period_days":30,"created_at":"2026-01-01T00:00:00Z"}],"course_targets":[],"equipment_configurations":[],"bow_types":[{"id":1,"name":"Classique"}],"unknown_future_table":[{"x":"kept"}]}}"""

    @Test
    fun legacyPreservesScoresAndEnds() {
        val p = BackupImporter.preview(sample)
        assertEquals(1, p.data.sessions.size)
        assertEquals(listOf(9, 10), p.data.impacts.map { it.score })
        assertEquals(listOf(1, 2), p.data.impacts.map { it.end })
        assertTrue(p.data.impacts[1].isX)
        assertEquals("Max", p.data.profile.firstName)
        assertEquals(.0, p.data.impacts[0].v!!, 1e-9)
        assertEquals(0, p.data.impacts[0].spot)
        assertEquals(1, p.data.impacts[1].spot)
        assertEquals(sample, p.data.legacyArchives.single())
    }

    @Test
    fun linksAndExtraFieldsPreserved() {
        val d = BackupImporter.preview(sample).data
        assertTrue(d.equipment.any { it.id == d.sessions[0].bowId })
        assertEquals("à conserver", d.equipment.first { it.kind == "bow" }.field("notes"))
        assertEquals("Belle séance", d.journals.single().comment)
        assertEquals(1, d.templates.size)
        assertEquals(1, d.goals.size)
        assertEquals(5.5, d.sessions[0].arrowDiameterMm, 1e-9)
    }

    @Test
    fun nativeRoundtrip() {
        val d = BackupImporter.preview(sample).data
        val next = BackupImporter.preview(Codec.backup(d)).data
        assertEquals(d, next)
    }

    @Test
    fun mergeKeepsExistingAndAddsOnce() {
        val p = BackupImporter.preview(sample)
        val merged = BackupImporter.merge(AppData(), p, false)
        assertEquals(1, merged.sessions.size)
        assertEquals(2, merged.impacts.size)
        assertTrue(p.fingerprint in merged.importKeys)
        assertEquals(Targets.all.size + 1, merged.targets.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateImportRejected() {
        val p = BackupImporter.preview(sample)
        BackupImporter.merge(BackupImporter.merge(AppData(), p, false), p, false)
    }

    @Test
    fun restoreReplacesButKeepsSettings() {
        val p = BackupImporter.preview(sample)
        val d = BackupImporter.merge(AppData(settings = Settings(language = "en")), p, true)
        assertEquals("en", d.settings.language)
        assertEquals(1, d.sessions.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsFutureVersion() {
        BackupImporter.preview(sample.replace("\"version\":1", "\"version\":99"))
    }

    @Test(expected = Exception::class)
    fun rejectsBrokenLinks() {
        BackupImporter.preview(sample.replace("\"session_id\":1", "\"session_id\":999"))
    }

    @Test(expected = Exception::class)
    fun rejectsDuplicateArrowNumber() {
        BackupImporter.preview(sample.replace("\"end_number\":2", "\"end_number\":1"))
    }

    @Test(expected = Exception::class)
    fun rejectsUnknownFormat() {
        BackupImporter.preview(sample.replace("volee-backup", "random"))
    }

    @Test
    fun nativeIdMergeDoesNotDuplicate() {
        val original = BackupImporter.preview(sample).data
        val p = BackupImporter.preview(Codec.backup(original))
        val merged = BackupImporter.merge(original, p, false)
        assertEquals(original.sessions, merged.sessions)
        assertEquals(original.impacts, merged.impacts)
    }
}
