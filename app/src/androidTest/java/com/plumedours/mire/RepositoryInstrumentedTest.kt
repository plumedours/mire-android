package com.plumedours.mire

import androidx.test.platform.app.InstrumentationRegistry
import com.plumedours.mire.data.Repository
import com.plumedours.mire.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Run on an emulator/device: ./gradlew connectedDebugAndroidTest */
class RepositoryInstrumentedTest {
    @Test
    fun persistsEveryArrowAndSeedCatalogue() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "mire-test-${newId()}.db"
        context.deleteDatabase(databaseName)
        val first = Repository(context, databaseName)
        try {
            first.load()
            val s = Session(id = "test-session", name = "Persistence test", target = Targets.all[0])
            val h = Impact(sessionId = s.id, end = 1, number = 1, u = .1, v = .0, score = 10)
            first.update { it.copy(sessions = listOf(s), impacts = listOf(h)) }
            first.close()
            val second = Repository(context, databaseName)
            try {
                second.load()
                assertEquals(s, second.data.value!!.sessions.single())
                assertEquals(h, second.data.value!!.impacts.single())
                assertEquals(
                    Targets.all.sortedBy { it.id },
                    second.data.value!!.targets.sortedBy { it.id },
                )
            } finally {
                second.close()
            }
        } finally {
            first.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun failedMutationDoesNotChangeJournal() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "mire-test-${newId()}.db"
        context.deleteDatabase(databaseName)
        val repo = Repository(context, databaseName)
        try {
            repo.load()
            val before = repo.data.value
            try {
                repo.update {
                    it.copy(impacts = listOf(Impact(sessionId = "missing", end = 1, number = 1)))
                }
                fail("Must reject an orphan arrow")
            } catch (_: IllegalStateException) {}
            assertEquals(before, repo.data.value)
        } finally {
            repo.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun largeLegacySourceSurvivesCursorWindowLimit() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "mire-test-${newId()}.db"
        context.deleteDatabase(databaseName)
        val repo = Repository(context, databaseName)
        val source = "é".repeat(2_000_000)
        try {
            repo.load()
            repo.update { it.copy(legacyArchives = listOf(source)) }
            repo.close()
            val reopened = Repository(context, databaseName)
            try {
                reopened.load()
                assertEquals(source, reopened.data.value!!.legacyArchives.single())
            } finally {
                reopened.close()
            }
        } finally {
            repo.close()
            context.deleteDatabase(databaseName)
        }
    }
}

class ExportInstrumentedTest {
    @Test
    fun nativePdfAndPngAreReadable() {
        val s =
            Session(
                id = "export-test",
                name = "Native export test",
                target = Targets.all.first(),
                completed = true,
            )
        val data =
            AppData(
                sessions = listOf(s),
                impacts =
                    listOf(Impact(sessionId = s.id, end = 1, number = 1, u = .1, v = .1, score = 9)),
            )
        val pdf = com.plumedours.mire.export.Exports.pdf(data, s.id)
        assertTrue(String(pdf.take(5).toByteArray(), Charsets.US_ASCII).startsWith("%PDF-"))
        val png = com.plumedours.mire.export.Exports.card(data, s)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(png, 0, png.size)
        assertNotNull(bitmap)
        assertEquals(1080, bitmap.width)
        assertEquals(1350, bitmap.height)
        bitmap.recycle()
    }
}
