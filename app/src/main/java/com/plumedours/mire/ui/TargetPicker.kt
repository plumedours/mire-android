@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.plumedours.mire.domain.*

@Composable
fun targetName(t: TargetSpec): String {
    val name = t.name
    return if (LocalEnglish.current) name.substringAfter('|', name) else name.substringBefore('|')
}

@Composable
fun TargetThumbnail(t: TargetSpec, modifier: Modifier = Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(16.dp),
        color = androidx.compose.ui.graphics.Color(0xFFF6F8FC),
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawIntoCanvas {
                TargetRenderer.draw(it.nativeCanvas, size.width, t, emptyList(), numbers = false)
            }
        }
    }
}

@Composable
fun TargetSelection(t: TargetSpec, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(.45f)),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TargetThumbnail(t, Modifier.width(78.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    tr("TON BLASON", "YOUR TARGET"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    targetName(t),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (t.animalTarget)
                        when (t.rule) {
                            Rule.NATURE -> "20 / 15 / 10"
                            Rule.THREE_D_12 -> "12 / 10 / 8 / 5"
                            Rule.THREE_D_X -> "10+ / 10 / 8 / 5"
                            else -> "11 / 10 / 8 / 5"
                        }
                    else
                        "${t.diameterCm} cm" +
                            if (t.rule in listOf(Rule.TEN, Rule.INNER_TEN))
                                " · ${t.lowestScore}–10 · " +
                                    if (t.rule == Rule.INNER_TEN) tr("petit 10", "inner 10")
                                    else tr("grand 10", "full 10")
                            else "",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    tr("Changer ou paramétrer →", "Change or configure →"),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun TargetPicker(
    targets: List<TargetSpec>,
    selected: TargetSpec,
    onDismiss: () -> Unit,
    onSelect: (TargetSpec) -> Unit,
) {
    var family by remember { mutableStateOf(selected.family) }
    var configuring by remember { mutableStateOf<TargetSpec?>(null) }
    val families =
        listOf(
            "target" to tr("Classiques", "Target"),
            "multi" to tr("Multi-spots", "Multi-spot"),
            "field" to tr("Campagne", "Field"),
            "animal" to tr("Nature & 3D", "Nature & 3D"),
            "balloon" to tr("Ballon", "Balloon"),
            "tradition" to tr("Tradition", "Tradition"),
        )
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.widthIn(max = 760.dp).fillMaxWidth(.96f).fillMaxHeight(.94f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle(tr("Choisis ton blason", "Choose your target"))
                    TextButton(onClick = onDismiss) { Text(tr("Fermer", "Close")) }
                }
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for ((key, label) in families) FilterChip(
                        family == key,
                        { family = key },
                        label = { Text(label) },
                    )
                }
                val choices = TargetCatalog.choices(targets).filter { it.family == family }
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(145.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(choices.size, key = { choices[it].id }) { i ->
                        val t = choices[i]
                        Card(
                            onClick = { configuring = if (t.id == selected.id) selected else t },
                            shape = RoundedCornerShape(20.dp),
                            border =
                                BorderStroke(
                                    if (t.id == selected.id) 2.dp else 1.dp,
                                    if (t.id == selected.id) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                ),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                        ) {
                            Column(
                                Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TargetThumbnail(t)
                                Text(
                                    targetName(t),
                                    style = MaterialTheme.typography.labelLarge,
                                    minLines = 2,
                                )
                                Text(
                                    if (t.rule == Rule.BALLOON) tr("Touché / manqué", "Hit / miss")
                                    else if (t.animalTarget) tr("Zones du vital", "Vital zones")
                                    else "${t.spotCount} ${tr("spot(s)","spot(s)")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    configuring?.let { t ->
        TargetConfiguration(t, { configuring = null }) { configured ->
            onSelect(configured)
            onDismiss()
        }
    }
}

@Composable
private fun TargetConfiguration(
    t: TargetSpec,
    onDismiss: () -> Unit,
    onSave: (TargetSpec) -> Unit,
) {
    var diameter by remember(t) { mutableStateOf(t.diameterCm.toString()) }
    var rule by remember(t) { mutableStateOf(t.rule) }
    var layout by remember(t) { mutableStateOf(t.layout) }
    var outer by remember(t) { mutableIntStateOf(t.lowestScore) }
    var centre by remember(t) { mutableStateOf(t.centreStyle) }
    val classic = t.rule in listOf(Rule.TEN, Rule.INNER_TEN)
    val threeD = t.rule in listOf(Rule.THREE_D_11, Rule.THREE_D_12, Rule.THREE_D_X)
    val preview =
        t.copy(
            diameterCm = numeric(diameter) ?: t.diameterCm,
            rule = rule,
            layout = layout,
            outerScore = if (classic) outer else 0,
            reduced = false,
            centreStyle = centre,
            zoneStyle = "neutral",
        )
    FormDialog(
        tr("Paramétrer le blason", "Configure target"),
        onDismiss,
        { onSave(preview) },
        enabled = numeric(diameter)?.let { it in .1..1000.0 } == true,
    ) {
        TargetThumbnail(preview, Modifier.width(180.dp).align(Alignment.CenterHorizontally))
        Text(targetName(t), style = MaterialTheme.typography.titleMedium)
        if (!t.animalTarget)
            Input(
                diameter,
                { diameter = it },
                tr("Diamètre nominal (cm)", "Nominal diameter (cm)"),
                true,
            )
        if (classic) {
            Choice(
                rule,
                listOf(Rule.TEN, Rule.INNER_TEN),
                tr("Barème", "Scoring"),
                {
                    if (it == Rule.TEN) tr("Grand 10", "Full 10")
                    else tr("Petit 10 · poulies", "Inner 10 · compound")
                },
            ) {
                rule = it
                centre = if (it == Rule.INNER_TEN) "compound" else "standard"
            }
            Choice(
                outer,
                listOf(1, 5, 6),
                tr("Zones visibles", "Visible rings"),
                { if (it == 1) tr("1–10 · complet", "1–10 · full") else "$it–10" },
            ) {
                outer = it
            }
            Choice(
                centre,
                if (rule == Rule.INNER_TEN) listOf("compound") else listOf("standard", "recurve"),
                tr("Centre", "Centre"),
                {
                    when (it) {
                        "compound" -> tr("Petit 10", "Inner 10")
                        "recurve" -> tr("Sans X", "Without X")
                        else -> tr("Avec X", "With X")
                    }
                },
            ) {
                centre = it
            }
        }
        if (t.rule == Rule.FIELD)
            Choice(
                layout,
                Layout.entries,
                tr("Disposition", "Layout"),
                {
                    when (it) {
                        Layout.SINGLE -> tr("Simple", "Single")
                        Layout.VERTICAL -> tr("Trispot vertical", "Vertical triple")
                        Layout.TRIANGLE -> tr("Trispot triangle", "Triangle triple")
                        Layout.QUAD -> tr("4 blasons", "4 faces")
                        Layout.FIELD_GRID -> tr("12 blasons", "12 faces")
                    }
                },
            ) {
                layout = it
            }
        if (threeD)
            Choice(
                rule,
                listOf(Rule.THREE_D_11, Rule.THREE_D_12, Rule.THREE_D_X),
                tr("Barème", "Scoring"),
                {
                    when (it) {
                        Rule.THREE_D_12 -> "12 / 10 / 8 / 5"
                        Rule.THREE_D_X -> "10+ / 10 / 8 / 5"
                        else -> "11 / 10 / 8 / 5"
                    }
                },
            ) {
                rule = it
            }
        if (t.rule == Rule.NATURE)
            Text(
                tr(
                    "1re flèche : 20 / 15 / 0 · 2e flèche : 15 / 10 / 0",
                    "1st arrow: 20 / 15 / 0 · 2nd arrow: 15 / 10 / 0",
                )
            )
    }
}
