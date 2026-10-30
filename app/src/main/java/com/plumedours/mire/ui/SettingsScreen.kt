@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.domain.*
import java.time.LocalDate

@Composable
fun SettingsScreen(
    data: AppData,
    vm: MireViewModel,
    onImport: () -> Unit,
    onExport: (String, String?) -> Unit,
) {
    var profile by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf<TargetSpec?>(null) }
    var showTargets by remember { mutableStateOf(false) }
    var typeDialog by remember { mutableStateOf(false) }
    val settings = data.settings
    Page {
        Panel {
            SectionTitle(tr("Profil", "Profile"))
            Text(
                listOf(data.profile.firstName, data.profile.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { tr("Ton profil d’archer", "Your archer profile") }
            )
            OutlinedButton(onClick = { profile = true }) {
                Text(tr("Modifier mon profil", "Edit profile"))
            }
        }
        Panel {
            SectionTitle(tr("Apparence et confort", "Appearance and comfort"))
            Choice(
                if (settings.theme == "dark") "sport" else settings.theme,
                listOf("sport", "forest", "plum", "graphite", "light", "system"),
                tr("Thème", "Theme"),
                {
                    when (it) {
                        "sport" -> tr("Sport · bleu nuit", "Sport · midnight blue")
                        "light" -> tr("Clair coloré", "Colourful light")
                        "forest" -> tr("Forêt · vert profond", "Forest · deep green")
                        "plum" -> tr("Prune · violet et rose", "Plum · purple and pink")
                        "graphite" -> tr("Graphite · ambre", "Graphite · amber")
                        else -> tr("Système", "System")
                    }
                },
            ) {
                vm.change { d -> d.copy(settings = d.settings.copy(theme = it)) }
            }
            Choice(
                settings.language,
                listOf("fr", "en", "system"),
                tr("Langue", "Language"),
                {
                    when (it) {
                        "fr" -> "Français"
                        "en" -> "English"
                        else -> tr("Système", "System")
                    }
                },
            ) {
                vm.change { d -> d.copy(settings = d.settings.copy(language = it)) }
            }
            Toggle(
                tr("Retour haptique", "Haptic feedback"),
                settings.haptics,
                { vm.change { d -> d.copy(settings = d.settings.copy(haptics = it)) } },
            )
            Toggle(
                tr("Garder l’écran allumé pendant le tir", "Keep screen awake while scoring"),
                settings.keepScreenOn,
                { vm.change { d -> d.copy(settings = d.settings.copy(keepScreenOn = it)) } },
            )
            Toggle(
                tr("Numéroter les impacts", "Show arrow numbers"),
                settings.showNumbers,
                { vm.change { d -> d.copy(settings = d.settings.copy(showNumbers = it)) } },
            )
            Toggle(
                tr("Mode gaucher sur écran large", "Left-handed wide-screen layout"),
                settings.leftHanded,
                { vm.change { d -> d.copy(settings = d.settings.copy(leftHanded = it)) } },
            )
        }
        Panel {
            SectionTitle(
                tr("Sauvegardes et migration", "Backups and migration"),
                tr(
                    "Retrouvez vos séances, votre matériel et vos objectifs sur un autre appareil.",
                    "Take your sessions, equipment and goals to another device.",
                ),
            )
            Button(onClick = { onExport("backup", null) }, modifier = Modifier.fillMaxWidth()) {
                Glyph("download")
                Spacer(Modifier.width(10.dp))
                Text(tr("Créer une sauvegarde", "Create backup"))
            }
            OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                Text(tr("Importer Volée ou Mire", "Import Volée or Mire"))
            }
            Text(
                tr(
                    "L’import affiche un aperçu avant toute écriture. La fusion garde les données existantes ; la restauration remplace le carnet après confirmation.",
                    "Import previews changes before writing. Merge keeps existing records; restore replaces the journal after confirmation.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                tr(
                    "Les sauvegardes contiennent des données personnelles. Enregistre-les dans un emplacement que tu contrôles.",
                    "Backups contain personal information. Save them somewhere you control.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Panel {
            SectionTitle(tr("Cibles et types d’arcs", "Targets and bow types"))
            OutlinedButton(onClick = { target = TargetSpec(newId(), "") }) {
                Text(tr("Créer une cible personnalisée", "Create custom target"))
            }
            Toggle(tr("Afficher le catalogue", "Show catalogue"), showTargets, { showTargets = it })
            if (showTargets)
                for (t in TargetCatalog.choices(data.targets)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(targetName(t), Modifier.weight(1f))
                        TextButton(onClick = { target = t }) { Text(tr("Détails", "Details")) }
                    }
                }
            Text(
                tr(
                    "Vos anciens résultats sont conservés lorsque vous modifiez une cible.",
                    "Your past results are preserved when you edit a target.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(data.bowTypes.joinToString(" · "))
            TextButton(onClick = { typeDialog = true }) {
                Text(tr("Ajouter un type d’arc", "Add bow type"))
            }
        }
        Panel {
            SectionTitle(tr("Exports", "Exports"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (key in listOf("pdf", "xlsx", "csv")) OutlinedButton(
                    onClick = { onExport(key, null) }
                ) {
                    Text(key.uppercase())
                }
            }
            Text(
                tr(
                    "PDF au format Lettre. Excel contient les séances et les impacts. Le JSON sert à restaurer toutes les données.",
                    "Letter-size PDF. Excel includes sessions and arrows. JSON restores the complete dataset.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Panel {
            SectionTitle("Mire · 1.4.3")
            Text(tr("Carnet de tir à l’arc", "Archery journal"))
            Text(
                tr(
                    "Votre progression, toujours à portée de main. Disponible hors ligne, sans compte.",
                    "Your progress, always at hand. Available offline, without an account.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    if (profile)
        ProfileDialog(data.profile, { profile = false }) { p ->
            vm.change { d -> d.copy(profile = p) }
            profile = false
        }
    target?.let { t ->
        TargetDialog(t, { target = null }) { updated ->
            vm.change { d ->
                d.copy(targets = d.targets.filterNot { it.id == updated.id } + updated)
            }
            target = null
        }
    }
    if (typeDialog) {
        var name by remember { mutableStateOf("") }
        FormDialog(
            tr("Type d’arc", "Bow type"),
            { typeDialog = false },
            {
                vm.change { d -> d.copy(bowTypes = (d.bowTypes + name.trim()).distinct().sorted()) }
                typeDialog = false
            },
            enabled = name.isNotBlank(),
        ) {
            Input(name, { name = it }, tr("Nom", "Name"))
        }
    }
}

@Composable
private fun ProfileDialog(p: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var first by remember { mutableStateOf(p.firstName) }
    var last by remember { mutableStateOf(p.lastName) }
    var birth by remember { mutableStateOf(p.birthDate.take(10)) }
    val valid =
        birth.isBlank() ||
            runCatching { LocalDate.parse(birth) <= LocalDate.now() }.getOrDefault(false)
    FormDialog(
        tr("Mon profil", "My profile"),
        onDismiss,
        { onSave(Profile(first.trim(), last.trim(), birth)) },
        enabled = valid,
    ) {
        Input(first, { first = it }, tr("Prénom", "First name"))
        Input(last, { last = it }, tr("Nom", "Last name"))
        Input(
            birth,
            { birth = it },
            tr("Naissance · AAAA-MM-JJ (facultatif)", "Birth date · YYYY-MM-DD (optional)"),
        )
    }
}

@Composable
private fun TargetDialog(t: TargetSpec, onDismiss: () -> Unit, onSave: (TargetSpec) -> Unit) {
    var name by remember { mutableStateOf(t.name) }
    var diameter by remember { mutableStateOf(t.diameterCm.toString()) }
    var layout by remember { mutableStateOf(t.layout) }
    var rule by remember { mutableStateOf(t.rule) }
    var outer by remember { mutableIntStateOf(t.lowestScore) }
    var centre by remember { mutableStateOf(t.centreStyle) }
    val valid =
        name.isNotBlank() &&
            numeric(diameter)?.let { it in .1..1000.0 } == true &&
            (layout == Layout.SINGLE || rule in listOf(Rule.TEN, Rule.INNER_TEN, Rule.FIELD))
    FormDialog(
        tr("Définition de cible", "Target definition"),
        onDismiss,
        {
            onSave(
                t.copy(
                    name = name.trim(),
                    diameterCm = numeric(diameter)!!,
                    layout = layout,
                    rule = rule,
                    reduced = false,
                    outerScore = if (rule in listOf(Rule.TEN, Rule.INNER_TEN)) outer else 0,
                    centreStyle = centre,
                    zoneStyle = "neutral",
                )
            )
        },
        enabled = valid,
    ) {
        Input(name, { name = it }, tr("Nom", "Name"))
        Input(
            diameter,
            { diameter = it },
            tr("Diamètre nominal du blason complet (cm)", "Nominal full-face diameter (cm)"),
            true,
        )
        Choice(
            layout,
            Layout.entries,
            tr("Disposition", "Layout"),
            {
                when (it) {
                    Layout.SINGLE -> tr("Simple", "Single")
                    Layout.VERTICAL -> tr("Trispot vertical", "Vertical triple")
                    Layout.TRIANGLE -> tr("Trispot triangulaire", "Triangular triple")
                    Layout.QUAD -> tr("Quatre blasons", "Four faces")
                    Layout.FIELD_GRID -> tr("Douze blasons", "Twelve faces")
                }
            },
        ) {
            layout = it
        }
        Choice(
            rule,
            Rule.entries,
            tr("Barème", "Scoring"),
            {
                when (it) {
                    Rule.TEN -> tr("10 zones · grand 10", "10 zones · full 10")
                    Rule.INNER_TEN -> tr("10 zones · petit 10", "10 zones · inner 10")
                    Rule.FIELD -> tr("Campagne · 6 zones", "Field · 6 zones")
                    Rule.WORCESTER -> "Worcester · 5"
                    Rule.THREE_D_11 -> "3D · 11 / 10 / 8 / 5"
                    Rule.THREE_D_12 -> "3D · 12 / 10 / 8 / 5"
                    Rule.THREE_D_X -> "3D · 10+ / 10 / 8 / 5"
                    Rule.NATURE -> tr("Nature · 20 / 15 / 10", "Nature · 20 / 15 / 10")
                    Rule.BEURSAULT -> "Beursault · 4 / 3 / 2 / 1"
                    Rule.BALLOON -> tr("Ballon · touché/manqué", "Balloon · hit/miss")
                    Rule.BLANK -> tr("Paille · sans score", "Blank · no score")
                }
            },
        ) {
            rule = it
        }
        if (rule in listOf(Rule.TEN, Rule.INNER_TEN)) {
            Choice(
                outer,
                listOf(1, 5, 6),
                tr("Zones affichées", "Visible rings"),
                { if (it == 1) tr("1 à 10 · complet", "1 to 10 · full") else "$it–10" },
            ) {
                outer = it
            }
            Choice(
                centre,
                listOf("standard", "recurve", "compound"),
                tr("Dessin du centre", "Centre style"),
                {
                    when (it) {
                        "recurve" -> tr("Classique sans X", "Recurve without X")
                        "compound" -> tr("Poulies · petit 10", "Compound · inner 10")
                        else -> tr("Standard avec X", "Standard with X")
                    }
                },
            ) {
                centre = it
            }
        }
    }
}
