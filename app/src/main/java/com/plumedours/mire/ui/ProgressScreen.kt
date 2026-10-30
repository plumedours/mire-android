@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.domain.*
import java.time.*

@Composable
fun ProgressScreen(
    data: AppData,
    vm: MireViewModel,
    onSession: (Session) -> Unit,
    onTemplate: (Session) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var days by rememberSaveable { mutableIntStateOf(90) }
    var target by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var bow by rememberSaveable { mutableStateOf("") }
    var goalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    val since =
        if (days == 0) "" else Instant.now().minus(Duration.ofDays(days.toLong())).toString()
    val sessions =
        data.sessions
            .filter {
                it.completed &&
                    it.startedAt >= since &&
                    (target.isBlank() || it.target.id == target) &&
                    (distance.isBlank() || it.distanceM.toString() == distance) &&
                    (bow.isBlank() || it.bowId == bow)
            }
            .sortedBy { it.startedAt }
    val ids = sessions.map { it.id }.toSet()
    val hits = data.impacts.filter { it.sessionId in ids }
    Page {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                    tr("Progression", "Progress"),
                    tr("Objectifs", "Goals"),
                    tr("Modèles", "Templates"),
                )
                .forEachIndexed { i, t -> FilterChip(tab == i, { tab = i }, label = { Text(t) }) }
        }
        when (tab) {
            0 -> {
                Choice(
                    days,
                    listOf(30, 90, 365, 0),
                    tr("Période", "Period"),
                    {
                        if (it == 0) tr("Tout l’historique", "All time")
                        else tr("$it jours", "$it days")
                    },
                ) {
                    days = it
                }
                Choice(
                    target,
                    listOf("") + data.sessions.map { it.target.id }.distinct(),
                    tr("Cible et barème", "Target and scoring"),
                    { id ->
                        data.sessions.find { it.target.id == id }?.target?.let { targetName(it) }
                            ?: tr("Toutes", "All")
                    },
                ) {
                    target = it
                }
                Choice(
                    distance,
                    listOf("") +
                        data.sessions
                            .map { it.distanceM.toString() }
                            .distinct()
                            .sortedBy { it.toDouble() },
                    tr("Distance", "Distance"),
                    { if (it.isBlank()) tr("Toutes", "All") else "$it m" },
                ) {
                    distance = it
                }
                Choice(
                    bow,
                    listOf("") + data.equipment.filter { it.kind == "bow" }.map { it.id },
                    tr("Arc", "Bow"),
                    { id -> data.equipment.find { it.id == id }?.name ?: tr("Tous", "All") },
                ) {
                    bow = it
                }
                Stats(
                    "${sessions.size}" to tr("Séances", "Sessions"),
                    "${hits.size}" to tr("Flèches", "Arrows"),
                    "${hits.count {it.isX}}" to "X",
                )
                val chart =
                    sessions.mapNotNull { s ->
                        val h = data.hits(s.id)
                        if (h.isEmpty() || s.target.maxScore == 0) null
                        else
                            h.sumOf { it.score } * 100.0 /
                                (h.sumOf { TargetEngine.maximum(s.target, it.number) })
                    }
                Panel {
                    SectionTitle(
                        tr("Taux de score par séance", "Score percentage per session"),
                        tr(
                            "Score obtenu / maximum possible · ordre chronologique",
                            "Score / maximum possible · chronological order",
                        ),
                    )
                    TrendChart(chart)
                    if (chart.isNotEmpty())
                        Text(
                            tr(
                                "Dernier taux : ${decimal(chart.last(),1)} %",
                                "Latest rate: ${decimal(chart.last(),1)} %",
                            )
                        )
                    Text(
                        tr(
                            "Filtre une même cible, distance et arc pour comparer des conditions proches.",
                            "Filter the same target, distance and bow to compare similar conditions.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                SectionTitle(tr("Repères par configuration", "Benchmarks by configuration"))
                val groups =
                    sessions.groupBy {
                        listOf(
                            it.target.id,
                            it.target.rule.name,
                            it.target.diameterCm.toString(),
                            it.distanceM.toString(),
                            it.arrowsPerEnd.toString(),
                            it.bowId.orEmpty(),
                        )
                    }
                if (groups.isEmpty())
                    Text(
                        tr(
                            "Termine une séance pour voir ta progression.",
                            "Complete a session to see your progress.",
                        )
                    )
                groups.values.forEach { group ->
                    val ranked =
                        group.map { it to data.hits(it.id) }.filter { it.second.isNotEmpty() }
                    val best = ranked.maxByOrNull { (_, h) -> h.map { it.score }.average() }
                    if (best != null)
                        Panel {
                            Text(
                                "${targetName(group[0].target)} · ${group[0].distanceM} m",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                tr(
                                    "Meilleure moyenne : ${decimal(best.second.map {it.score}.average(),2)} · ${group.size} séance(s)",
                                    "Best average: ${decimal(best.second.map {it.score}.average(),2)} · ${group.size} session(s)",
                                )
                            )
                            TextButton(onClick = { onSession(best.first) }) {
                                Text(tr("Ouvrir la séance", "Open session"))
                            }
                        }
                }
            }
            1 -> {
                Button(
                    onClick = {
                        editingGoal = null
                        goalDialog = true
                    }
                ) {
                    Text(tr("Nouvel objectif", "New goal"))
                }
                Text(
                    tr(
                        "Séances terminées sur la période choisie. Placez au moins trois flèches par séance pour analyser vos groupements.",
                        "Completed sessions in the selected period. Place at least three arrows per session to analyse your groups.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                for (g in data.goals) {
                    val p = GoalEngine.evaluate(data, g)
                    Panel {
                        Text(g.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${p.current?.let {decimal(it,2)} ?: "—"} / ${decimal(g.value,2)} · ${g.periodDays} ${tr("jours","days")}"
                        )
                        LinearProgressIndicator(
                            progress = { p.ratio.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            tr(
                                "${p.eligibleSessions}/${g.minimumSessions} séances minimum · ${if(g.completed) "classé terminé" else "suivi actif"}",
                                "${p.eligibleSessions}/${g.minimumSessions} minimum sessions · ${if(g.completed) "marked complete" else "active"}",
                            ),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    editingGoal = g
                                    goalDialog = true
                                }
                            ) {
                                Text(tr("Modifier", "Edit"))
                            }
                            TextButton(
                                onClick = {
                                    vm.change { d ->
                                        d.copy(
                                            goals =
                                                d.goals.map {
                                                    if (it.id == g.id)
                                                        it.copy(completed = !it.completed)
                                                    else it
                                                }
                                        )
                                    }
                                }
                            ) {
                                Text(tr("Marquer / rouvrir", "Mark / reopen"))
                            }
                            TextButton(
                                onClick = {
                                    vm.change { d ->
                                        d.copy(goals = d.goals.filterNot { it.id == g.id })
                                    }
                                }
                            ) {
                                Text(tr("Supprimer", "Delete"))
                            }
                        }
                    }
                }
                if (data.goals.isEmpty())
                    EmptyState(
                        tr("Choisis un cap", "Choose a goal"),
                        tr(
                            "Volume de flèches, fréquence, moyenne ou groupement : suis un objectif mesurable.",
                            "Arrow volume, frequency, average or grouping: track a measurable goal.",
                        ),
                    )
            }
            2 -> {
                Text(
                    tr(
                        "Enregistre une séance comme modèle depuis son résumé. Le modèle conserve la cible et les réglages, sans ses impacts.",
                        "Save a session as a template from its summary. Templates keep target and settings without arrows.",
                    )
                )
                for (t in data.templates) Panel {
                    Text(t.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${targetName(t.session.target)} · ${t.session.distanceM} m · ${t.session.arrowsPerEnd} ${tr("flèches / volée","arrows / end")}"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onTemplate(t.session) }) { Text(tr("Utiliser", "Use")) }
                        TextButton(
                            onClick = {
                                vm.change { d ->
                                    d.copy(templates = d.templates.filterNot { it.id == t.id })
                                }
                            }
                        ) {
                            Text(tr("Supprimer", "Delete"))
                        }
                    }
                }
            }
        }
    }
    if (goalDialog)
        GoalDialog(data, editingGoal, { goalDialog = false }) { g ->
            vm.change { d -> d.copy(goals = d.goals.filterNot { it.id == g.id } + g) }
            goalDialog = false
        }
}

@Composable
private fun TrendChart(values: List<Double>) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = .25f)
    Canvas(Modifier.fillMaxWidth().height(180.dp)) {
        for (i in 0..4) {
            val y = size.height * i / 4
            drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        val points =
            values.mapIndexed { i, v ->
                Offset(
                    if (values.size == 1) size.width / 2 else size.width * i / (values.size - 1),
                    size.height * (1 - v.coerceIn(0.0, 100.0).toFloat() / 100),
                )
            }
        if (points.size > 1) {
            val path =
                Path().apply {
                    moveTo(points[0].x, points[0].y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
            drawPath(path, color, style = Stroke(3.dp.toPx()))
        }
        points.forEach { drawCircle(color, 4.dp.toPx(), it) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(tr("Ancien", "Earlier"), style = MaterialTheme.typography.labelSmall)
        Text("0–100 %", style = MaterialTheme.typography.labelSmall)
        Text(tr("Récent", "Latest"), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun GoalDialog(
    data: AppData,
    initial: Goal?,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var kind by remember { mutableStateOf(initial?.kind ?: "arrows") }
    var value by remember { mutableStateOf(initial?.value?.toString() ?: "300") }
    var days by remember { mutableStateOf(initial?.periodDays?.toString() ?: "30") }
    var minimum by remember { mutableStateOf(initial?.minimumSessions?.toString() ?: "1") }
    var target by remember { mutableStateOf(initial?.targetId.orEmpty()) }
    var distance by remember { mutableStateOf(initial?.distanceM?.toString().orEmpty()) }
    val valid =
        title.isNotBlank() &&
            numeric(value)?.let { it > 0 } == true &&
            days.toIntOrNull()?.let { it in 1..36500 } == true &&
            minimum.toIntOrNull()?.let { it in 1..10000 } == true &&
            (distance.isBlank() || numeric(distance)?.let { it >= 0 } == true)
    FormDialog(
        tr("Objectif personnel", "Personal goal"),
        onDismiss,
        {
            onSave(
                Goal(
                    initial?.id ?: newId(),
                    title,
                    kind,
                    numeric(value)!!,
                    days.toInt(),
                    initial?.createdAt ?: Instant.now().toString(),
                    minimum.toInt(),
                    target.ifBlank { null },
                    numeric(distance),
                    initial?.completed ?: false,
                )
            )
        },
        enabled = valid,
    ) {
        Input(title, { title = it }, tr("Titre", "Title"))
        Choice(
            kind,
            listOf(
                "arrows",
                "sessions",
                "average",
                "dispersion",
                "centeredness",
                "consistency",
                "balloonRate",
            ),
            tr("Indicateur", "Metric"),
            {
                when (it) {
                    "arrows" -> tr("Nombre de flèches", "Arrow count")
                    "sessions" -> tr("Nombre de séances", "Session count")
                    "average" -> tr("Moyenne par flèche", "Average per arrow")
                    "dispersion" ->
                        tr("Écart maximal (% diamètre) ≤", "Extreme spread (% diameter) ≤")
                    "centeredness" ->
                        tr("Décentrage (% diamètre) ≤", "Centre offset (% diameter) ≤")
                    "consistency" -> tr("Régularité /100", "Consistency /100")
                    else -> tr("Réussite ballon (%)", "Balloon hit rate (%)")
                }
            },
        ) {
            kind = it
        }
        Input(value, { value = it }, tr("Valeur cible", "Target value"), true)
        Input(days, { days = it }, tr("Période en jours", "Period in days"), true)
        Input(minimum, { minimum = it }, tr("Minimum de séances", "Minimum sessions"), true)
        Choice(
            target,
            listOf("") + data.targets.map { it.id },
            tr("Limiter à une cible", "Limit to a target"),
            { id -> data.targets.find { it.id == id }?.name ?: tr("Toutes", "All") },
        ) {
            target = it
        }
        Input(
            distance,
            { distance = it },
            tr("Distance (m) · vide = toutes", "Distance (m) · blank = all"),
            true,
        )
    }
}
