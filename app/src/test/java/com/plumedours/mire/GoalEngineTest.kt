package com.plumedours.mire

import com.plumedours.mire.domain.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class GoalEngineTest {
    private val now = Instant.parse("2026-10-16T12:00:00Z")
    private val session =
        Session(
            id = "s",
            name = "Test",
            target = Targets.all[0],
            startedAt = "2026-10-10T12:00:00Z",
            completed = true,
        )
    private val data =
        AppData(
            sessions = listOf(session),
            impacts = listOf(Impact(sessionId = "s", end = 1, number = 1, score = 10)),
        )

    @Test
    fun arrowGoal() {
        assertEquals(
            .5,
            GoalEngine.evaluate(data, Goal(title = "Flèches", value = 2.0), now).ratio,
            1e-9,
        )
    }

    @Test
    fun minimumSessions() {
        assertEquals(
            0.0,
            GoalEngine.evaluate(
                    data,
                    Goal(title = "Flèches", value = 1.0, minimumSessions = 2),
                    now,
                )
                .ratio,
            1e-9,
        )
    }

    @Test
    fun ignoresRunning() {
        val d = data.copy(sessions = listOf(session.copy(completed = false)))
        assertEquals(
            0.0,
            GoalEngine.evaluate(d, Goal(title = "Flèches", value = 1.0), now).ratio,
            1e-9,
        )
    }

    @Test
    fun ignoresOld() {
        assertEquals(
            0.0,
            GoalEngine.evaluate(data, Goal(title = "Flèches", value = 1.0, periodDays = 1), now)
                .ratio,
            1e-9,
        )
    }

    @Test
    fun filtersTarget() {
        assertEquals(
            0.0,
            GoalEngine.evaluate(data, Goal(title = "Flèches", value = 1.0, targetId = "other"), now)
                .ratio,
            1e-9,
        )
    }

    @Test
    fun noGeometryIsNotPerfect() {
        val p =
            GoalEngine.evaluate(
                data,
                Goal(title = "Groupe", kind = "dispersion", value = 10.0),
                now,
            )
        assertNull(p.current)
        assertEquals(0.0, p.ratio, 1e-9)
    }
}
