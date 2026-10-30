@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.domain.*
import java.time.*
import kotlinx.coroutines.launch

@Composable
fun SummaryScreen(
    data: AppData,
    s: Session,
    vm: MireViewModel,
    onResume: () -> Unit,
    onRepeat: (Session) -> Unit,
    onDeleted: () -> Unit,
    onExport: (String, String?) -> Unit,
) {
    val hits = data.hits(s.id)
    val ends = hits.map { it.end }.distinct().sorted()
    val pager = rememberPagerState { ends.size + 1 }
    val scope = rememberCoroutineScope()
    var heat by rememberSaveable { mutableStateOf(false) }
    var overlay by rememberSaveable { mutableStateOf(s.target.spotCount > 1) }
    var filterSpot by rememberSaveable { mutableIntStateOf(-1) }
    var journal by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Impact?>(null) }
    val activeEnd = ends.getOrNull(pager.currentPage - 1).takeIf { pager.currentPage > 0 }
    val active =
        hits
            .filter { activeEnd == null || it.end == activeEnd }
            .filter { filterSpot < 0 || it.spot == filterSpot }
    val a = remember(s.target, active) { TargetEngine.analyze(s.target, active) }
    val previous =
        data.sessions
            .filter {
                it.id != s.id &&
                    it.completed &&
                    it.startedAt < s.startedAt &&
                    it.target.id == s.target.id &&
                    it.target.rule == s.target.rule &&
                    it.target.diameterCm == s.target.diameterCm &&
                    it.distanceM == s.distanceM
            }
            .maxByOrNull { it.startedAt }
    Page {
        SectionTitle(
            s.name,
            "${dateLabel(s.startedAt)} · ${s.distanceM} m · ${if(s.competition) tr("Compétition","Competition") else tr("Entraînement","Training")}",
        )
        if (!s.completed)
            Text(
                tr(
                    "Séance en cours · chaque impact est sauvegardé",
                    "Session in progress · every arrow is saved",
                ),
                color = MaterialTheme.colorScheme.primary,
            )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { if (s.completed) vm.complete(s.id, false, onResume) else onResume() }
            ) {
                MaterialActionIcon(
                    if (s.completed) MaterialAction.RESTORE else MaterialAction.PLAY,
                    Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(if (s.completed) tr("Rouvrir", "Reopen") else tr("Reprendre", "Resume"))
            }
            OutlinedButton(onClick = { onRepeat(s) }) { Text(tr("Répéter", "Repeat")) }
            OutlinedButton(onClick = { journal = true }) { Text(tr("Journal", "Journal")) }
            OutlinedButton(
                onClick = { delete = true },
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
            ) {
                MaterialActionIcon(MaterialAction.DELETE, Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Supprimer", "Delete"))
            }

            OutlinedButton(
                onClick = {
                    vm.change { d ->
                        d.copy(
                            templates = d.templates + SessionTemplate(name = s.name, session = s)
                        )
                    }
                    vm.tell("Modèle enregistré / Template saved")
                }
            ) {
                Text(tr("En faire un modèle", "Save as template"))
            }
        }
        Panel {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pager.animateScrollToPage((pager.currentPage - 1).coerceAtLeast(0))
                        }
                    },
                    enabled = pager.currentPage > 0,
                ) {
                    Text("←")
                }
                Text(
                    if (pager.currentPage == 0) tr("Séance complète", "Full session")
                    else tr("Volée $activeEnd", "End $activeEnd"),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(
                    onClick = {
                        scope.launch {
                            pager.animateScrollToPage(
                                (pager.currentPage + 1).coerceAtMost(ends.size)
                            )
                        }
                    },
                    enabled = pager.currentPage < ends.size,
                ) {
                    Text("→")
                }
            }
            Choice(
                pager.currentPage,
                (0..ends.size).toList(),
                tr("Afficher", "Show"),
                {
                    if (it == 0) tr("Tous les impacts", "All arrows")
                    else tr("Volée ${ends[it-1]}", "End ${ends[it-1]}")
                },
            ) { page ->
                scope.launch { pager.animateScrollToPage(page) }
            }
            if (s.target.spotCount > 1) {
                Toggle(
                    tr("Superposer les spots autour du centre", "Overlay spots on a common centre"),
                    overlay,
                    { overlay = it },
                )
                Choice(
                    filterSpot,
                    listOf(-1) + (0 until s.target.spotCount),
                    tr("Filtrer les spots", "Filter spots"),
                    { if (it < 0) tr("Tous", "All") else "Spot ${it+1}" },
                ) {
                    filterSpot = it
                }
            }
            if (s.target.canPlace) Toggle(tr("Carte de chaleur", "Heatmap"), heat, { heat = it })
            HorizontalPager(
                pager,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { index ->
                val end = if (index == 0) null else ends[index - 1]
                val pageHits =
                    hits
                        .filter { end == null || it.end == end }
                        .filter { filterSpot < 0 || it.spot == filterSpot }
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TargetBoard(
                        s.target,
                        pageHits,
                        Modifier.widthIn(max = 620.dp).fillMaxWidth(),
                        heat && s.target.canPlace,
                        overlay,
                        data.settings.showNumbers,
                    )
                }
            }
            if (overlay && s.target.spotCount > 1 && !heat)
                Text(
                    tr(
                        "Spot 1 : rose · spot 2 : vert · spot 3 : violet",
                        "Spot 1: pink · spot 2: green · spot 3: purple",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
        }
        Stats(
            "${active.sumOf {it.score}}" to tr("Score sélection", "Selected score"),
            "${active.size}" to tr("Flèches", "Arrows"),
            (if (active.isEmpty()) "—" else decimal(active.map { it.score }.average(), 2)) to
                tr("Moyenne / flèche", "Average / arrow"),
            "${active.count {it.isX}}" to "X",
        )
        if (
            s.target.animalTarget ||
                s.target.rule == Rule.BEURSAULT ||
                s.target.rule == Rule.BALLOON
        )
            Panel {
                SectionTitle(tr("Répartition des touches", "Hit distribution"))
                for (score in
                    (if (s.target.rule == Rule.NATURE) listOf(20, 15, 10, 0)
                    else TargetEngine.allowedScores(s.target))) Text(
                    "${if(s.target.rule==Rule.BALLOON) {if(score>0) tr("Touché","Hit") else tr("Manqué","Miss")} else score.toString()} · ${active.count{it.score==score}}"
                )
                if (s.target.rule == Rule.BALLOON && active.isNotEmpty())
                    Text(
                        "${decimal(active.count{it.score>0}*100.0/active.size)} % ${tr("de réussite","hit rate")}",
                        color = MaterialTheme.colorScheme.primary,
                    )
                if (s.target.rule == Rule.BEURSAULT)
                    Text(
                        "${active.count{it.score>0}} ${tr("honneurs","honours")} · ${active.count{it.score>=3}} ${tr("chapelets","chapelets")}"
                    )
            }
        val elapsed =
            s.endedAt?.let {
                runCatching {
                        Duration.between(Instant.parse(s.startedAt), Instant.parse(it)).toMinutes()
                    }
                    .getOrNull()
            }
        if (elapsed != null) Text(tr("Durée : $elapsed min", "Duration: $elapsed min"))
        if (a != null)
            Panel {
                SectionTitle(
                    tr("Précision et groupement", "Precision and grouping"),
                    tr(
                        "${a.count} positions exploitables sur ${active.size} flèches",
                        "${a.count} usable positions out of ${active.size} arrows",
                    ),
                )
                Stats(
                    "${decimal(a.offsetCm,2)} cm" to tr("Décalage du centre", "Centre offset"),
                    "${decimal(a.meanRadiusCm,2)} cm" to
                        tr("Rayon moyen du groupe", "Mean group radius"),
                    "${decimal(a.extremeSpreadCm,2)} cm" to tr("Écart maximal", "Extreme spread"),
                )
                Text(
                    tr(
                        "Largeur ${decimal(a.widthCm,2)} cm · hauteur ${decimal(a.heightCm,2)} cm · RMS ${decimal(a.rmsCm,2)} cm",
                        "Width ${decimal(a.widthCm,2)} cm · height ${decimal(a.heightCm,2)} cm · RMS ${decimal(a.rmsCm,2)} cm",
                    )
                )
                Text(
                    tr("Analyse des impacts positionnés.", "Analysis of positioned arrows."),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        if (previous != null && hits.isNotEmpty()) {
            val old = data.hits(previous.id)
            if (old.isNotEmpty()) {
                val delta = hits.map { it.score }.average() - old.map { it.score }.average()
                Panel {
                    Text(
                        tr(
                            "Comparaison à la séance précédente compatible",
                            "Compared with previous compatible session",
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "${if(delta>=0) "+" else ""}${decimal(delta,2)} ${tr("point / flèche","points / arrow")}"
                    )
                    Text(previous.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (hits.any { it.uncertain })
            Panel {
                Text(
                    tr(
                        "${hits.count {it.uncertain}} position(s) importée(s) à vérifier",
                        "${hits.count {it.uncertain}} imported position(s) need review",
                    )
                )
                Text(
                    tr(
                        "Rouvre la séance, sélectionne la flèche et déplace-la sur le bon spot.",
                        "Reopen the session, select the arrow and move it onto the correct spot.",
                    )
                )
            }
        Panel {
            SectionTitle(tr("Feuille de marque", "Score sheet"))
            if (ends.isEmpty()) Text(tr("Aucune flèche enregistrée", "No arrows recorded"))
            for (end in ends) {
                val rows = hits.filter { it.end == end }
                Text(
                    tr(
                        "Volée $end · ${rows.sumOf {it.score}} points",
                        "End $end · ${rows.sumOf {it.score}} points",
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (h in rows) AssistChip(
                        onClick = { editing = h },
                        label = {
                            Text(
                                "${h.number}: ${if(h.isX) "X" else h.score}${if(h.label.isNotBlank()) " · ${h.label}" else ""}"
                            )
                        },
                    )
                }
                data.courseTargets
                    .find {
                        it.sessionId == s.id &&
                            it.number ==
                                (end - 1) %
                                    (s.extra["target_count"]?.toIntOrNull()?.coerceAtLeast(1)
                                        ?: 1) + 1
                    }
                    ?.let {
                        Text(
                            "${it.animal} · ${it.distanceM} m",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                HorizontalDivider()
            }
        }
        val j = data.journals.find { it.sessionId == s.id }
        if (j != null || s.notes.isNotBlank() || s.location.isNotBlank())
            Panel {
                SectionTitle(tr("Journal et contexte", "Journal and context"))
                Text(
                    listOf(
                            s.location,
                            s.notes,
                            j?.weather.orEmpty(),
                            j?.wind.orEmpty(),
                            j?.adjustments.orEmpty(),
                            j?.comment.orEmpty(),
                            j?.tags.orEmpty(),
                        )
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                )
                if (j != null)
                    Text(
                        tr(
                            "Ressenti ${j.feeling}/5 · fatigue ${j.fatigue}/5",
                            "Feeling ${j.feeling}/5 · fatigue ${j.fatigue}/5",
                        )
                    )
            }
        Panel {
            SectionTitle(tr("Exporter et partager", "Export and share"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((key, label) in
                    listOf(
                        "pdf" to "PDF",
                        "xlsx" to "Excel",
                        "csv" to "CSV",
                        "card" to tr("Partager l’image", "Share image"),
                        "card-save" to tr("Enregistrer l’image", "Save image"),
                    )) OutlinedButton(onClick = { onExport(key, s.id) }) { Text(label) }
            }
        }
    }
    if (journal)
        JournalDialog(
            data.journals.find { it.sessionId == s.id } ?: Journal(s.id),
            { journal = false },
        ) {
            vm.saveJournal(it)
            journal = false
        }
    if (delete)
        SessionDeletionDialog(s, { delete = false }) {
            delete = false
            vm.deleteSession(s.id, onDeleted)
        }
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
        )
    }
}
