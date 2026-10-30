@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plumedours.mire.domain.*
import java.time.*

@Composable
fun SessionCard(
    d: AppData,
    s: Session,
    onDelete: () -> Unit,
    onReopen: () -> Unit,
    onClick: () -> Unit,
) {
    var deleting by rememberSaveable(s.id) { mutableStateOf(false) }
    val deleteLabel = tr("Supprimer la séance ${s.name}", "Delete session ${s.name}")
    val h = d.hits(s.id)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) { Glyph("target") }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    s.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${s.distanceM} m · ${dateLabel(s.startedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    if (s.completed)
                        tr("Terminée · ${h.size} flèches", "Completed · ${h.size} arrows")
                    else
                        tr("En cours · volée ${s.currentEnd}", "In progress · end ${s.currentEnd}"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${h.sumOf {it.score}}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("pts", style = MaterialTheme.typography.labelSmall)
            }
        }
        HorizontalDivider(
            Modifier.padding(horizontal = 18.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = if (s.completed) onReopen else onClick) {
                MaterialActionIcon(
                    if (s.completed) MaterialAction.RESTORE else MaterialAction.PLAY,
                    Modifier.size(20.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text(if (s.completed) tr("Rouvrir", "Reopen") else tr("Reprendre", "Resume"))
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { deleting = true },
                modifier = Modifier.semantics { contentDescription = deleteLabel },
                colors =
                    IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
            ) {
                MaterialActionIcon(MaterialAction.DELETE)
            }
        }
    }
    if (deleting)
        SessionDeletionDialog(s, { deleting = false }) {
            deleting = false
            onDelete()
        }
}

@Composable
fun HomeScreen(
    data: AppData,
    onNew: () -> Unit,
    onSession: (Session) -> Unit,
    onHistory: () -> Unit,
    onImport: () -> Unit,
    onQuick: (TargetSpec) -> Unit,
    onDelete: (Session) -> Unit,
    onReopen: (Session) -> Unit,
) {
    val threshold = Instant.now().minus(Duration.ofDays(30)).toString()
    val recent = data.sessions.filter { it.startedAt >= threshold && it.completed }
    val ids = recent.map { it.id }.toSet()
    val arrows = data.impacts.filter { it.sessionId in ids }
    Page {
        SectionTitle(
            tr(
                "Bonjour${data.profile.firstName.takeIf {it.isNotBlank()}?.let {", $it"}.orEmpty()}",
                "Hello${data.profile.firstName.takeIf {it.isNotBlank()}?.let {", $it"}.orEmpty()}",
            ),
            tr(
                "Une flèche à la fois. Une progression qui se voit.",
                "One arrow at a time. Progress you can see.",
            ),
        )
        Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent) {
            Box(
                Modifier.fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer,
                            )
                        )
                    )
            ) {
                Canvas(Modifier.matchParentSize()) {
                    for (i in 1..5) drawCircle(
                        Color.White.copy(alpha = .07f),
                        size.width * (.08f + i * .09f),
                        Offset(size.width * .95f, size.height * .30f),
                        style = Stroke(9.dp.toPx()),
                    )
                }
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        tr("TON TERRAIN. TON RYTHME.", "YOUR RANGE. YOUR PACE."),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        tr("Fais parler\nchaque flèche.", "Make every\narrow count."),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        tr(
                            "Cible, parcours ou défi ballon : à toi de jouer.",
                            "Targets, courses or balloon challenges: your call.",
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Button(
                        onClick = onNew,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                    ) {
                        Glyph("plus")
                        Spacer(Modifier.width(10.dp))
                        Text(tr("C’est parti", "Let’s shoot"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        SectionTitle(tr("Choisis ton terrain", "Choose your game"))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for ((id, label) in
                listOf(
                    "wa40" to tr("Blason", "Target"),
                    "triangle40" to tr("Trispot", "Triple spot"),
                    "field40" to tr("Campagne", "Field"),
                    "3d11" to tr("Tir 3D", "3D archery"),
                    "animal-FOX-nature" to tr("Nature", "Nature"),
                    "balloon" to tr("Ballon", "Balloon"),
                )) {
                val face = TargetCatalog.choices(data.targets).first { it.id == id }
                Card(
                    onClick = { onQuick(face) },
                    modifier = Modifier.widthIn(min = 145.dp).weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        TargetThumbnail(face, Modifier.width(48.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        SectionTitle(tr("Tes 30 derniers jours", "Your last 30 days"))
        Stats(
            "${recent.size}" to tr("Séances terminées", "Completed sessions"),
            "${arrows.size}" to tr("Flèches tirées", "Arrows shot"),
            "${recent.map {it.startedAt.take(10)}.distinct().size}" to
                tr("Jours de pratique", "Practice days"),
        )
        val running = data.sessions.filter { !it.completed }.sortedByDescending { it.startedAt }
        if (running.isNotEmpty()) {
            SectionTitle(tr("À reprendre", "Continue shooting"))
            running.take(3).forEach { s ->
                SessionCard(data, s, { onDelete(s) }, { onReopen(s) }) { onSession(s) }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                tr("Dernières séances", "Recent sessions"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onHistory) { Text(tr("Tout voir", "View all")) }
        }
        val completed = data.sessions.filter { it.completed }.sortedByDescending { it.startedAt }
        completed.take(5).forEach { s ->
            SessionCard(data, s, { onDelete(s) }, { onReopen(s) }) { onSession(s) }
        }
        if (data.sessions.isEmpty())
            EmptyState(
                tr("Ton carnet commence ici", "Your journal starts here"),
                tr(
                    "Commence une séance ou retrouve ton historique Volée.",
                    "Start a session or bring your Volée history with you.",
                ),
                tr("Importer Volée / Mire", "Import Volée / Mire"),
                onImport,
            )
        Text(
            tr(
                "Tes données restent sur cet appareil. Pense à créer une sauvegarde dans les réglages.",
                "Your data stays on this device. Create a backup from Settings.",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun HistoryScreen(
    data: AppData,
    onNew: () -> Unit,
    onSession: (Session) -> Unit,
    onExport: (String, String?) -> Unit,
    onDelete: (Session) -> Unit,
    onReopen: (Session) -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableIntStateOf(0) }
    var kind by rememberSaveable { mutableIntStateOf(0) }
    var target by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(30) }
    val sessions =
        data.sessions
            .filter { s ->
                (search.isBlank() ||
                    listOf(s.name, s.location, s.target.name, s.notes).any {
                        it.contains(search, true)
                    }) &&
                    (status == 0 || s.completed == (status == 1)) &&
                    (kind == 0 || s.competition == (kind == 2)) &&
                    (target.isEmpty() || s.target.id == target)
            }
            .sortedByDescending { it.startedAt }
    Page {
        Button(onClick = onNew) {
            Glyph("plus")
            Spacer(Modifier.width(8.dp))
            Text(tr("Nouvelle séance", "New session"))
        }
        Input(
            search,
            {
                search = it
                count = 30
            },
            tr("Rechercher une séance, un lieu, une cible…", "Search sessions, places, targets…"),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(tr("Toutes", "All"), tr("Terminées", "Completed"), tr("En cours", "In progress"))
                .forEachIndexed { i, text ->
                    FilterChip(status == i, { status = i }, label = { Text(text) })
                }
        }
        Choice(
            kind,
            listOf(0, 1, 2),
            tr("Pratique", "Practice"),
            {
                when (it) {
                    1 -> tr("Entraînement", "Training")
                    2 -> tr("Compétition", "Competition")
                    else -> tr("Toutes", "All")
                }
            },
        ) {
            kind = it
        }
        Choice(
            target,
            listOf("") + data.sessions.map { it.target.id }.distinct(),
            tr("Cible", "Target"),
            { id ->
                data.sessions.find { it.target.id == id }?.target?.let { targetName(it) }
                    ?: tr("Toutes les cibles", "All targets")
            },
        ) {
            target = it
        }
        Text(
            tr("${sessions.size} séance(s)", "${sessions.size} session(s)"),
            style = MaterialTheme.typography.labelLarge,
        )
        for (s in sessions.take(count)) SessionCard(data, s, { onDelete(s) }, { onReopen(s) }) {
            onSession(s)
        }
        if (sessions.size > count)
            OutlinedButton(onClick = { count += 30 }, modifier = Modifier.fillMaxWidth()) {
                Text(tr("Afficher plus", "Show more"))
            }
        if (sessions.isEmpty())
            EmptyState(
                tr("Aucune séance", "No sessions"),
                tr(
                    "Modifie tes filtres ou démarre une nouvelle séance.",
                    "Change your filters or start a new session.",
                ),
            )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (key in listOf("pdf", "xlsx", "csv")) OutlinedButton(
                onClick = { onExport(key, null) }
            ) {
                Text(tr("Tout exporter", "Export all") + " · ${key.uppercase()}")
            }
        }
    }
}
