package com.plumedours.mire

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plumedours.mire.data.*
import com.plumedours.mire.domain.*
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MireViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    val data = repository.data
    private val mutableMessage = MutableStateFlow<String?>(null)
    val message = mutableMessage.asStateFlow()
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    private val mutableFatal = MutableStateFlow<String?>(null)
    val fatal = mutableFatal.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching { repository.load() }
                .onFailure { mutableFatal.value = it.message ?: "Database error" }
        }
    }

    fun dismissMessage() {
        mutableMessage.value = null
    }

    fun tell(message: String) {
        mutableMessage.value = message
    }

    fun change(done: () -> Unit = {}, block: (AppData) -> AppData) {
        viewModelScope.launch {
            try {
                repository.update(block)
                done()
            } catch (e: Exception) {
                mutableMessage.value = e.message ?: "Erreur / Error"
            }
        }
    }

    fun saveSession(session: Session, done: () -> Unit = {}) =
        change(done) { d ->
            d.copy(sessions = d.sessions.filterNot { it.id == session.id } + session)
        }

    fun addImpact(
        sessionId: String,
        end: Int,
        point: LocatedPoint? = null,
        manualScore: Int? = null,
        x: Boolean = false,
    ) = change { d ->
        val s = d.sessions.first { it.id == sessionId }
        check(!s.completed) { "Séance terminée / Session completed" }
        val hits = d.hits(sessionId).filter { it.end == end }
        check(hits.size < s.arrowsPerEnd) { "Volée complète / End complete" }
        if (manualScore != null)
            require(
                manualScore in
                    TargetEngine.allowedScores(s.target, (hits.maxOfOrNull { it.number } ?: 0) + 1)
            ) {
                "La flèche suivante utilise un autre barème / The next arrow uses different scoring"
            }
        val score =
            if (manualScore != null) ShotScore(manualScore, x)
            else
                TargetEngine.score(
                    s.target,
                    point!!.u,
                    point.v,
                    s.arrowDiameterMm,
                    (hits.maxOfOrNull { it.number } ?: 0) + 1,
                )
        val hit =
            Impact(
                sessionId = sessionId,
                end = end,
                number = (hits.maxOfOrNull { it.number } ?: 0) + 1,
                u = point?.u,
                v = point?.v,
                spot = point?.spot,
                score = score.value,
                isX = score.isX,
                manual = manualScore != null,
            )
        d.copy(impacts = d.impacts + hit)
    }

    fun editImpact(impact: Impact) = change { d ->
        d.copy(impacts = d.impacts.map { if (it.id == impact.id) impact else it })
    }

    fun moveImpact(id: String, point: LocatedPoint) = change { d ->
        val old = d.impacts.first { it.id == id }
        val s = d.sessions.first { it.id == old.sessionId }
        val score = TargetEngine.score(s.target, point.u, point.v, s.arrowDiameterMm, old.number)
        d.copy(
            impacts =
                d.impacts.map {
                    if (it.id == id)
                        old.copy(
                            u = point.u,
                            v = point.v,
                            spot = point.spot,
                            score = score.value,
                            isX = score.isX,
                            manual = false,
                            uncertain = false,
                        )
                    else it
                }
        )
    }

    fun removeImpact(id: String) = change { d ->
        val hit = d.impacts.first { it.id == id }
        val remaining =
            d.impacts
                .filterNot { it.id == id }
                .filter { it.sessionId == hit.sessionId && it.end == hit.end }
                .sortedBy { it.number }
        val renumbered = remaining.mapIndexed { i, h -> h.copy(number = i + 1) }
        d.copy(
            impacts =
                d.impacts.filterNot { it.sessionId == hit.sessionId && it.end == hit.end } +
                    renumbered
        )
    }

    fun end(sessionId: String, number: Int) = change { d ->
        d.copy(
            sessions =
                d.sessions.map {
                    if (it.id == sessionId) it.copy(currentEnd = number.coerceAtLeast(1)) else it
                }
        )
    }

    fun complete(sessionId: String, completed: Boolean, done: () -> Unit = {}) =
        change(done) { d ->
            d.copy(
                sessions =
                    d.sessions.map {
                        if (it.id == sessionId)
                            it.copy(
                                completed = completed,
                                endedAt = if (completed) Instant.now().toString() else null,
                            )
                        else it
                    }
            )
        }

    fun deleteSession(id: String, done: () -> Unit) =
        change(done) { d ->
            d.copy(
                sessions = d.sessions.filterNot { it.id == id },
                impacts = d.impacts.filterNot { it.sessionId == id },
                journals = d.journals.filterNot { it.sessionId == id },
                courseTargets = d.courseTargets.filterNot { it.sessionId == id },
            )
        }

    fun saveEquipment(e: Equipment) = change { d ->
        d.copy(equipment = d.equipment.filterNot { it.id == e.id } + e)
    }

    fun saveJournal(j: Journal) = change { d ->
        d.copy(journals = d.journals.filterNot { it.sessionId == j.sessionId } + j)
    }

    fun saveCourse(t: CourseTarget) = change { d ->
        d.copy(
            courseTargets =
                d.courseTargets.filterNot { it.sessionId == t.sessionId && it.number == t.number } +
                    t
        )
    }

    fun prepareImport(text: String, done: (ImportPreview) -> Unit) {
        viewModelScope.launch {
            mutableBusy.value = true
            try {
                val p = withContext(Dispatchers.Default) { BackupImporter.preview(text) }
                done(p)
            } catch (e: Exception) {
                tell(e.message ?: "Import error")
            } finally {
                mutableBusy.value = false
            }
        }
    }

    fun import(p: ImportPreview, replace: Boolean, done: () -> Unit) =
        change({
            tell("Import terminé / Import completed")
            done()
        }) {
            BackupImporter.merge(it, p, replace)
        }
}
