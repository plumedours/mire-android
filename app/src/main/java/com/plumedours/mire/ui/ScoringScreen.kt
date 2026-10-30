@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.domain.*
import java.time.*
import kotlinx.coroutines.delay

@Composable
fun ScoringScreen(data: AppData, s: Session, vm: MireViewModel, onSummary: () -> Unit) {
    val hits = data.hits(s.id)
    val current = hits.filter { it.end == s.currentEnd }
    val targetCount = s.extra["target_count"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val lap = (s.currentEnd - 1) / targetCount + 1
    val targetNo = (s.currentEnd - 1) % targetCount + 1
    val isCourse = s.target.isCourse
    var selected by rememberSaveable(s.id, s.currentEnd) { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Impact?>(null) }
    var finish by remember { mutableStateOf(false) }
    var spot by rememberSaveable(s.id) { mutableIntStateOf(-1) }
    var journal by remember { mutableStateOf(false) }
    var course by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    DisposableEffect(data.settings.keepScreenOn) {
        if (data.settings.keepScreenOn)
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    val haptics = LocalHapticFeedback.current
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(s.id) {
        while (true) {
            delay(1000)
            now = Instant.now()
        }
    }
    val duration =
        runCatching { Duration.between(Instant.parse(s.startedAt), now).seconds.coerceAtLeast(0) }
            .getOrDefault(0)
    fun feedback() {
        if (data.settings.haptics) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    tr("Volée ${s.currentEnd}", "End ${s.currentEnd}"),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "${current.size}/${s.arrowsPerEnd} · ${duration/60}m ${duration%60}s" +
                        (if (isCourse)
                            " · ${tr("Cible","Target")} $targetNo · ${tr("Tour","Lap")} $lap"
                        else ""),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            TextButton(onClick = { journal = true }) { Text(tr("Journal", "Journal")) }
            FilledTonalButton(onClick = { finish = true }) { Text(tr("Terminer", "Finish")) }
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            val wide = maxWidth >= 700.dp && maxHeight >= 340.dp
            val board: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (s.target.spotCount > 1)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                spot == -1,
                                { spot = -1 },
                                label = { Text(tr("Auto", "Auto")) },
                            )
                            for (i in 0 until s.target.spotCount) FilterChip(
                                spot == i,
                                { spot = i },
                                label = { Text("Spot ${i+1}") },
                            )
                        }
                    TargetBoard(
                        s.target,
                        current,
                        editable =
                            s.target.canPlace &&
                                (current.size < s.arrowsPerEnd || selected != null),
                        arrowDiameterMm = s.arrowDiameterMm,
                        arrowNumber = current.size + 1,
                        selected = selected,
                        preferredSpot = spot.takeIf { it >= 0 },
                        numbers = data.settings.showNumbers,
                        onShot = {
                            vm.addImpact(s.id, s.currentEnd, it)
                            feedback()
                        },
                        onMove = { id, p ->
                            vm.moveImpact(id, p)
                            selected = null
                            feedback()
                        },
                    )
                    if (s.target.animalTarget)
                        Text(
                            tr("Zones du vital", "Vital zones"),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    if (!s.target.canPlace)
                        Text(
                            tr(
                                "Beursault : utilise les boutons pour confirmer le score et les cordons.",
                                "Beursault: use the buttons to confirm the score and line cutters.",
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )
                }
            }
            val controls: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (s.target.rule == Rule.BALLOON)
                        Stats(
                            "${hits.count{it.score>0}}" to tr("Touchés", "Hits"),
                            "${if(hits.isEmpty())"—" else decimal(hits.count{it.score>0}*100.0/hits.size)} %" to
                                tr("Réussite", "Hit rate"),
                        )
                    else
                        Stats(
                            "${current.sumOf{it.score}}" to tr("Cette volée", "This end"),
                            "${hits.sumOf{it.score}}" to tr("Total séance", "Session score"),
                        )
                    if (selected != null)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                tr(
                                    "Touche la cible pour déplacer la flèche sélectionnée.",
                                    "Tap the target to move the selected arrow.",
                                ),
                                Modifier.padding(12.dp),
                            )
                        }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (h in current) InputChip(
                            selected == h.id,
                            onClick = { editing = h },
                            label = {
                                Text(
                                    "${h.number} · ${if(h.isX) "X" else h.score}${if(h.uncertain) " !" else ""}"
                                )
                            },
                        )
                    }
                    Text(
                        tr("Saisie rapide · sans position", "Quick score · no position"),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    ScorePad(s.target, current.size + 1, current.size < s.arrowsPerEnd) { score, x
                        ->
                        vm.addImpact(s.id, s.currentEnd, manualScore = score, x = x)
                        feedback()
                    }
                    Text(
                        tr(
                            "Glisse le doigt pour viser, puis relâche pour enregistrer. Pince avec deux doigts pour zoomer. Pour déplacer une flèche, sélectionne sa pastille.",
                            "Drag to aim, release to save. Pinch with two fingers to zoom. Select an arrow chip before moving it.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { current.lastOrNull()?.let { vm.removeImpact(it.id) } },
                            enabled = current.isNotEmpty(),
                        ) {
                            Text(tr("Annuler flèche", "Undo arrow"))
                        }
                        if (selected != null)
                            TextButton(onClick = { selected = null }) {
                                Text(tr("Désélectionner", "Deselect"))
                            }
                    }
                    val endOptions =
                        (1..maxOf(s.currentEnd, hits.maxOfOrNull { it.end } ?: 1)).toList()
                    Choice(
                        s.currentEnd,
                        endOptions,
                        tr("Consulter / modifier une volée", "View / edit an end"),
                        { it.toString() },
                    ) {
                        vm.end(s.id, it)
                        selected = null
                    }
                    Button(
                        onClick = {
                            vm.end(s.id, s.currentEnd + 1)
                            selected = null
                        },
                        enabled = current.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (current.size < s.arrowsPerEnd)
                                tr("Valider la volée incomplète", "Save incomplete end")
                            else tr("Volée suivante", "Next end")
                        )
                    }
                    if (s.plannedEnds > 0)
                        Text(
                            tr(
                                "Objectif : ${s.plannedEnds} volées. Tu peux poursuivre au-delà.",
                                "Plan: ${s.plannedEnds} ends. You can continue beyond it.",
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    if (s.target.isCourse)
                        OutlinedButton(onClick = { course = true }) {
                            Text(
                                tr(
                                    "Cible du parcours · distance / animal",
                                    "Course target · distance / animal",
                                )
                            )
                        }
                    OutlinedButton(onClick = onSummary, modifier = Modifier.fillMaxWidth()) {
                        Text(tr("Voir le résumé", "View summary"))
                    }
                }
            }
            if (wide)
                Row(
                    Modifier.fillMaxSize().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    if (data.settings.leftHanded) {
                        Box(Modifier.weight(1f).fillMaxHeight().verticalScrollCompat()) {
                            controls()
                        }
                        Box(Modifier.weight(1.1f).fillMaxHeight().verticalScrollCompat()) {
                            board()
                        }
                    } else {
                        Box(Modifier.weight(1.1f).fillMaxHeight().verticalScrollCompat()) {
                            board()
                        }
                        Box(Modifier.weight(1f).fillMaxHeight().verticalScrollCompat()) {
                            controls()
                        }
                    }
                }
            else
                Page {
                    board()
                    controls()
                }
        }
    }
    if (finish)
        Confirm(
            tr("Terminer la séance ?", "Finish session?"),
            tr(
                "Les impacts sont déjà enregistrés. La séance pourra être rouverte depuis son résumé.",
                "Your arrows are saved. You can reopen this session from its summary.",
            ),
            { finish = false },
            {
                vm.complete(s.id, true)
                finish = false
                onSummary()
            },
        )
    editing?.let { h ->
        ImpactDialog(
            h,
            s,
            { editing = null },
            {
                vm.editImpact(it)
                editing = null
            },
            {
                vm.removeImpact(h.id)
                editing = null
            },
            {
                selected = h.id
                editing = null
            },
        )
    }
    if (journal)
        JournalDialog(
            data.journals.find { it.sessionId == s.id } ?: Journal(s.id),
            { journal = false },
        ) {
            vm.saveJournal(it)
            journal = false
        }
    if (course)
        CourseDialog(
            data.courseTargets.find { it.sessionId == s.id && it.number == targetNo }
                ?: CourseTarget(sessionId = s.id, number = targetNo, distanceM = s.distanceM),
            { course = false },
        ) {
            vm.saveCourse(it)
            course = false
        }
}

@Composable private fun Modifier.verticalScrollCompat() = this.verticalScroll(rememberScrollState())

@Composable
fun ImpactDialog(
    h: Impact,
    s: Session,
    onDismiss: () -> Unit,
    onSave: (Impact) -> Unit,
    onDelete: () -> Unit,
    onMove: (() -> Unit)? = null,
) {
    var score by remember { mutableIntStateOf(h.score) }
    var isX by remember { mutableStateOf(h.isX) }
    var label by remember { mutableStateOf(h.label) }
    var spot by remember { mutableIntStateOf(h.spot ?: 0) }
    FormDialog(
        tr("Flèche ${h.number} · volée ${h.end}", "Arrow ${h.number} · end ${h.end}"),
        onDismiss,
        {
            onSave(
                h.copy(
                    score = score,
                    isX = isX && score == 10,
                    label = label,
                    spot = if (h.u != null) spot else null,
                    manual = true,
                )
            )
        },
    ) {
        Choice(
            score,
            (TargetEngine.allowedScores(s.target, h.number) + h.score).distinct(),
            tr("Score", "Score"),
            { it.toString() },
        ) {
            score = it
            if (score != 10) isX = false
        }
        if (score == 10 && s.target.rule in listOf(Rule.TEN, Rule.INNER_TEN, Rule.THREE_D_X))
            Toggle("X", isX, { isX = it })
        Input(label, { label = it }, tr("Repère de flèche / note", "Arrow label / note"))
        if (s.target.spotCount > 1 && h.u != null)
            Choice(
                spot,
                (0 until s.target.spotCount).toList(),
                tr("Spot visé", "Aimed spot"),
                { "Spot ${it+1}" },
            ) {
                spot = it
            }
        if (h.uncertain)
            Text(
                tr(
                    "Position importée ambiguë : utilise Déplacer et replace l’impact pour le réintégrer à l’analyse.",
                    "Ambiguous imported position: use Move and place it again to include it in the analysis.",
                )
            )
        if (onMove != null && s.target.canPlace)
            OutlinedButton(onClick = onMove) { Text(tr("Déplacer sur la cible", "Move on target")) }
        TextButton(onClick = onDelete) {
            Text(
                tr("Supprimer cette flèche", "Delete this arrow"),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun JournalDialog(j: Journal, onDismiss: () -> Unit, onSave: (Journal) -> Unit) {
    var feeling by remember { mutableIntStateOf(j.feeling) }
    var fatigue by remember { mutableIntStateOf(j.fatigue) }
    var weather by remember { mutableStateOf(j.weather) }
    var wind by remember { mutableStateOf(j.wind) }
    var adjustments by remember { mutableStateOf(j.adjustments) }
    var comment by remember { mutableStateOf(j.comment) }
    var tags by remember { mutableStateOf(j.tags) }
    FormDialog(
        tr("Journal de séance", "Session journal"),
        onDismiss,
        {
            onSave(
                j.copy(
                    feeling = feeling,
                    fatigue = fatigue,
                    weather = weather,
                    wind = wind,
                    adjustments = adjustments,
                    comment = comment,
                    tags = tags,
                )
            )
        },
    ) {
        Choice(
            feeling,
            (1..5).toList(),
            tr("Ressenti · 1 difficile → 5 excellent", "Feeling · 1 difficult → 5 excellent"),
        ) {
            feeling = it
        }
        Choice(
            fatigue,
            (1..5).toList(),
            tr("Fatigue · 1 faible → 5 forte", "Fatigue · 1 low → 5 high"),
        ) {
            fatigue = it
        }
        Input(weather, { weather = it }, tr("Météo", "Weather"))
        Input(wind, { wind = it }, tr("Vent", "Wind"))
        Input(adjustments, { adjustments = it }, tr("Réglages", "Adjustments"), multiline = true)
        Input(comment, { comment = it }, tr("Commentaire", "Comment"), multiline = true)
        Input(tags, { tags = it }, tr("Mots-clés", "Tags"))
    }
}

@Composable
fun CourseDialog(t: CourseTarget, onDismiss: () -> Unit, onSave: (CourseTarget) -> Unit) {
    var animal by remember { mutableStateOf(t.animal) }
    var distance by remember { mutableStateOf(t.distanceM.toString()) }
    FormDialog(
        tr("Cible ${t.number}", "Target ${t.number}"),
        onDismiss,
        { onSave(t.copy(animal = animal, distanceM = numeric(distance)!!)) },
        enabled = numeric(distance)?.let { it >= 0 } == true,
    ) {
        Input(animal, { animal = it }, tr("Animal / repère", "Animal / marker"))
        Input(distance, { distance = it }, tr("Distance (m)", "Distance (m)"), true)
    }
}
