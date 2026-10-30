@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.plumedours.mire.domain.*
import java.time.LocalDate

@Composable
fun SessionForm(
    data: AppData,
    initial: Session? = null,
    onDismiss: () -> Unit,
    onSave: (Session) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var competition by rememberSaveable { mutableStateOf(initial?.competition ?: false) }
    var targetJson by rememberSaveable {
        mutableStateOf(
            com.plumedours.mire.data.Codec.target(
                    initial?.target ?: TargetCatalog.choices(data.targets).first()
                )
                .toString()
        )
    }
    var distance by rememberSaveable { mutableStateOf((initial?.distanceM ?: 18.0).toString()) }
    var arrows by rememberSaveable { mutableStateOf((initial?.arrowsPerEnd ?: 3).toString()) }
    var ends by rememberSaveable { mutableStateOf((initial?.plannedEnds ?: 10).toString()) }
    var bowId by rememberSaveable { mutableStateOf(initial?.bowId.orEmpty()) }
    var arrowId by rememberSaveable { mutableStateOf(initial?.arrowsId.orEmpty()) }
    var configId by rememberSaveable { mutableStateOf(initial?.configurationId.orEmpty()) }
    var diameter by rememberSaveable {
        mutableStateOf((initial?.arrowDiameterMm ?: 0.0).toString())
    }
    var location by rememberSaveable { mutableStateOf(initial?.location.orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initial?.notes.orEmpty()) }
    var balloon by rememberSaveable {
        mutableStateOf(
            (initial?.target?.takeIf { it.rule == Rule.BALLOON }?.diameterCm ?: 5.0).toString()
        )
    }
    var targetCount by rememberSaveable {
        mutableStateOf(initial?.extra?.get("target_count") ?: "12")
    }
    var laps by rememberSaveable { mutableStateOf(initial?.extra?.get("laps") ?: "2") }
    val target = com.plumedours.mire.data.Codec.target(org.json.JSONObject(targetJson))
    val isCourse = target.isCourse
    var targetPicker by remember { mutableStateOf(false) }
    val valid =
        (!isCourse || (targetCount.toIntOrNull() in 1..1000 && laps.toIntOrNull() in 1..100)) &&
            (target.rule != Rule.NATURE || arrows.toIntOrNull() == 2) &&
            numeric(distance)?.let { it in 0.0..2000.0 } == true &&
            arrows.toIntOrNull() in 1..100 &&
            ends.toIntOrNull()?.let { it in 0..10000 } == true &&
            numeric(diameter)?.let { it in 0.0..30.0 } == true &&
            (target.rule != Rule.BALLOON || numeric(balloon)?.let { it in .1..500.0 } == true)
    FormDialog(
        tr("Nouvelle séance", "New session"),
        onDismiss,
        {
            onSave(
                Session(
                    name =
                        name.trim().ifBlank {
                            target.name.substringBefore("|") + " · " + LocalDate.now()
                        },
                    competition = competition,
                    target =
                        if (target.rule == Rule.BALLOON)
                            target.copy(diameterCm = numeric(balloon)!!)
                        else target,
                    distanceM = numeric(distance)!!,
                    arrowsPerEnd = arrows.toInt(),
                    plannedEnds =
                        if (isCourse) targetCount.toInt() * laps.toInt() else ends.toInt(),
                    bowId = bowId.ifBlank { null },
                    arrowsId = arrowId.ifBlank { null },
                    configurationId = configId.ifBlank { null },
                    location = location.trim(),
                    notes = notes.trim(),
                    arrowDiameterMm = numeric(diameter)!!,
                    extra =
                        initial?.extra.orEmpty() +
                            mapOf(
                                "target_count" to (if (isCourse) targetCount else "1"),
                                "laps" to (if (isCourse) laps else "1"),
                            ),
                )
            )
        },
        saveText = tr("Démarrer", "Start"),
        enabled = valid,
    ) {
        Input(name, { name = it }, tr("Nom de la séance", "Session name"))
        Toggle(tr("Compétition", "Competition"), competition, { competition = it })
        TargetSelection(target) { targetPicker = true }
        if (targetPicker)
            TargetPicker(data.targets, target, { targetPicker = false }) { chosen ->
                targetJson = com.plumedours.mire.data.Codec.target(chosen).toString()
                if (chosen.rule == Rule.BALLOON) balloon = chosen.diameterCm.toString()
                if (chosen.rule == Rule.NATURE) arrows = "2"
                if (chosen.rule == Rule.BEURSAULT) {
                    arrows = "1"
                    distance = "50"
                    ends = "40"
                }
            }
        if (target.rule == Rule.BALLOON)
            Input(
                balloon,
                { balloon = it },
                tr("Diamètre du ballon (cm)", "Balloon diameter (cm)"),
                true,
            )
        Input(distance, { distance = it }, tr("Distance (m)", "Distance (m)"), true)
        Input(
            arrows,
            { arrows = it },
            tr("Flèches par volée (1–100)", "Arrows per end (1–100)"),
            true,
        )
        if (isCourse) {
            Input(
                targetCount,
                { targetCount = it },
                tr("Nombre de cibles du parcours", "Number of course targets"),
                true,
            )
            Input(laps, { laps = it }, tr("Nombre de tours", "Number of laps"), true)
        } else
            Input(
                ends,
                { ends = it },
                tr("Volées prévues · 0 = libre", "Planned ends · 0 = unlimited"),
                true,
            )
        val bows = data.equipment.filter { it.kind == "bow" && !it.archived }
        val sets = data.equipment.filter { it.kind == "arrows" && !it.archived }
        val configs = data.equipment.filter { it.kind == "config" && !it.archived }
        val none = tr("Aucun", "None")
        Choice(
            configId,
            listOf("") + configs.map { it.id },
            tr("Configuration de matériel", "Equipment setup"),
            { id -> configs.find { it.id == id }?.name ?: none },
        ) { id ->
            configId = id
            configs
                .find { it.id == id }
                ?.let {
                    bowId = it.field("bow_id")
                    arrowId = it.field("arrow_set_id")
                    diameter =
                        sets.find { it.id == arrowId }?.field("diameter").orEmpty().ifBlank { "0" }
                }
        }
        Choice(
            bowId,
            listOf("") + bows.map { it.id },
            tr("Arc", "Bow"),
            { id -> bows.find { it.id == id }?.name ?: none },
        ) {
            bowId = it
        }
        Choice(
            arrowId,
            listOf("") + sets.map { it.id },
            tr("Jeu de flèches", "Arrow set"),
            { id -> sets.find { it.id == id }?.name ?: none },
        ) { id ->
            arrowId = id
            diameter = sets.find { it.id == id }?.field("diameter").orEmpty().ifBlank { "0" }
        }
        Input(
            diameter,
            { diameter = it },
            tr("Diamètre du tube (mm) · 0 = point", "Shaft diameter (mm) · 0 = point"),
            true,
        )
        Input(location, { location = it }, tr("Lieu", "Location"))
        Input(notes, { notes = it }, tr("Notes", "Notes"), multiline = true)
    }
}
