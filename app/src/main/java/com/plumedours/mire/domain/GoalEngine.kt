package com.plumedours.mire.domain

import java.time.*
import kotlin.math.*

data class GoalProgress(val current: Double?, val ratio: Double, val eligibleSessions: Int)

object GoalEngine {
    fun evaluate(d: AppData, g: Goal, now: Instant = Instant.now()): GoalProgress {
        val since = now.minus(Duration.ofDays(g.periodDays.toLong()))
        val sessions =
            d.sessions.filter {
                it.completed &&
                    runCatching {
                            Instant.parse(it.startedAt) >= since &&
                                Instant.parse(it.startedAt) <= now
                        }
                        .getOrDefault(false) &&
                    (g.targetId == null || it.target.id == g.targetId) &&
                    (g.distanceM == null || abs(it.distanceM - g.distanceM) < .01) &&
                    (g.kind != "balloonRate" || it.target.rule == Rule.BALLOON)
            }
        val ids = sessions.map { it.id }.toSet()
        val hits = d.impacts.filter { it.sessionId in ids }
        val geometry =
            sessions.mapNotNull { s ->
                TargetEngine.analyze(s.target, hits.filter { it.sessionId == s.id })
                    ?.takeIf { it.count >= 3 }
                    ?.let { s to it }
            }
        val current =
            when (g.kind) {
                "sessions" -> sessions.size.toDouble()
                "arrows" -> hits.size.toDouble()
                "average" -> hits.takeIf { it.isNotEmpty() }?.map { it.score }?.average()
                "balloonRate" ->
                    hits
                        .takeIf { it.isNotEmpty() }
                        ?.let { it.count { h -> h.score > 0 } * 100.0 / it.size }
                // Express legacy percentage goals relative to nominal face diameter, computed per
                // session.
                "dispersion" ->
                    geometry
                        .takeIf { it.isNotEmpty() }
                        ?.map { (s, a) -> a.extremeSpreadCm / s.target.diameterCm * 100 }
                        ?.average()
                "centeredness" ->
                    geometry
                        .takeIf { it.isNotEmpty() }
                        ?.map { (s, a) -> a.offsetCm / s.target.diameterCm * 100 }
                        ?.average()
                "consistency" ->
                    geometry
                        .takeIf { it.isNotEmpty() }
                        ?.map { (s, a) ->
                            (100 * (1 - a.meanRadiusCm / (s.target.diameterCm * .25))).coerceIn(
                                0.0,
                                100.0,
                            )
                        }
                        ?.average()
                else -> null
            }
        val inverse = g.kind in listOf("dispersion", "centeredness")
        val ratio =
            if (current == null || sessions.size < g.minimumSessions || g.value <= 0) 0.0
            else if (inverse) {
                if (current <= g.value) 1.0 else g.value / current
            } else current / g.value
        return GoalProgress(current, ratio.coerceIn(0.0, 1.0), sessions.size)
    }
}
