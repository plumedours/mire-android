package com.plumedours.mire.domain

object DataValidator {
    fun validate(d: AppData) {
        fun ids(xs: List<String>) {
            require(xs.size == xs.distinct().size) {
                "Identifiants dupliqués / Duplicate identifiers"
            }
        }
        ids(d.sessions.map { it.id })
        ids(d.impacts.map { it.id })
        ids(d.equipment.map { it.id })
        ids(d.targets.map { it.id })
        ids(d.journals.map { it.sessionId })
        ids(d.courseTargets.map { it.id })
        ids(d.templates.map { it.id })
        ids(d.goals.map { it.id })
        d.goals.forEach {
            require(
                it.value.isFinite() &&
                    it.value > 0 &&
                    it.periodDays in 1..36500 &&
                    it.minimumSessions in 1..10000 &&
                    (it.distanceM == null || it.distanceM.isFinite() && it.distanceM >= 0)
            )
        }
        val sessions = d.sessions.associateBy { it.id }
        d.targets.forEach { require(it.diameterCm.isFinite() && it.diameterCm > 0) }
        d.sessions.forEach {
            require(
                it.arrowsPerEnd in 1..100 &&
                    it.currentEnd > 0 &&
                    it.distanceM.isFinite() &&
                    it.distanceM >= 0 &&
                    it.plannedEnds >= 0 &&
                    it.target.diameterCm.isFinite() &&
                    it.target.diameterCm > 0 &&
                    it.arrowDiameterMm.isFinite() &&
                    it.arrowDiameterMm >= 0
            )
        }
        d.impacts.forEach { i ->
            val s = sessions[i.sessionId] ?: error("Impact sans séance / Orphan arrow")
            require(i.end > 0 && i.number > 0 && i.score in 0..maxOf(s.target.maxScore, 20))
            require(i.u == null || i.u.isFinite())
            require(i.v == null || i.v.isFinite())
            require((i.u == null) == (i.v == null))
            require(i.spot == null || i.spot in 0 until s.target.spotCount)
        }
        require(
            d.impacts.map { Triple(it.sessionId, it.end, it.number) }.distinct().size ==
                d.impacts.size
        ) {
            "Numéros de flèches dupliqués / Duplicate arrow numbers"
        }
        require(d.journals.all { it.sessionId in sessions })
        require(d.courseTargets.all { it.sessionId in sessions })
    }
}
