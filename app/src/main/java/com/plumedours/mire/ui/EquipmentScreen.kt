@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.domain.*

@Composable
fun EquipmentScreen(data: AppData, vm: MireViewModel) {
    var kind by rememberSaveable { mutableStateOf("bow") }
    var archived by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var edit by remember { mutableStateOf<Equipment?>(null) }
    Page {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((key, label) in
                listOf(
                    "bow" to tr("Arcs", "Bows"),
                    "arrows" to tr("Flèches", "Arrows"),
                    "config" to tr("Configurations", "Setups"),
                )) FilterChip(kind == key, { kind = key }, label = { Text(label) })
        }
        Button(onClick = { edit = Equipment(kind = kind, name = "") }) {
            Text(tr("Ajouter", "Add"))
        }
        Input(search, { search = it }, tr("Rechercher du matériel", "Search equipment"))
        Toggle(tr("Afficher les archives", "Show archived"), archived, { archived = it })
        val items =
            data.equipment
                .filter {
                    it.kind == kind &&
                        (archived || !it.archived) &&
                        (search.isBlank() ||
                            it.name.contains(search, true) ||
                            it.fields.values.any { v -> v.contains(search, true) })
                }
                .sortedBy { it.name.lowercase() }
        if (items.isEmpty())
            EmptyState(
                tr("Prépare ton matériel", "Set up your equipment"),
                tr(
                    "Ajoute tes arcs, tes jeux de flèches et tes configurations. Tu peux aussi tirer sans matériel enregistré.",
                    "Add bows, arrow sets and setups. You can also shoot without saved equipment.",
                ),
            )
        for (e in items) Panel {
            Text(
                e.name + (if (e.archived) tr(" · archivé", " · archived") else ""),
                style = MaterialTheme.typography.titleLarge,
            )
            if (kind == "config") {
                Text(
                    data.equipment.find { it.id == e.field("bow_id") }?.name.orEmpty() +
                        " · " +
                        data.equipment.find { it.id == e.field("arrow_set_id") }?.name.orEmpty()
                )
            } else
                Text(
                    listOf(
                            e.field("type"),
                            e.field("brand"),
                            e.field("model"),
                            if (kind == "arrows") e.field("spine") else e.field("draw_weight"),
                        )
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                )
            if (e.field("notes").isNotBlank()) Text(e.field("notes"))
            Row {
                TextButton(onClick = { edit = e }) { Text(tr("Modifier", "Edit")) }
                TextButton(onClick = { vm.saveEquipment(e.copy(archived = !e.archived)) }) {
                    Text(if (e.archived) tr("Restaurer", "Restore") else tr("Archiver", "Archive"))
                }
            }
        }
    }
    edit?.let { e ->
        EquipmentDialog(data, e, { edit = null }) {
            vm.saveEquipment(it)
            edit = null
        }
    }
}

@Composable
private fun EquipmentDialog(
    data: AppData,
    e: Equipment,
    onDismiss: () -> Unit,
    onSave: (Equipment) -> Unit,
) {
    var name by remember { mutableStateOf(e.name) }
    var fields by remember { mutableStateOf(e.fields) }
    fun set(k: String, v: String) {
        fields = fields + (k to v)
    }
    FormDialog(
        tr("Matériel", "Equipment"),
        onDismiss,
        { onSave(e.copy(name = name.trim(), fields = fields)) },
        enabled = name.isNotBlank(),
    ) {
        Input(name, { name = it }, tr("Nom", "Name"))
        if (e.kind == "config") {
            for ((key, kind, label) in
                listOf(
                    Triple("bow_id", "bow", tr("Arc", "Bow")),
                    Triple("arrow_set_id", "arrows", tr("Jeu de flèches", "Arrow set")),
                )) {
                val items = data.equipment.filter { it.kind == kind && !it.archived }
                Choice(
                    fields[key].orEmpty(),
                    listOf("") + items.map { it.id },
                    label,
                    { id -> data.equipment.find { it.id == id }?.name ?: tr("Aucun", "None") },
                ) {
                    set(key, it)
                }
            }
            for ((key, label) in
                listOf(
                    "sight_mark" to tr("Repère de viseur", "Sight mark"),
                    "brace_height" to tr("Band", "Brace height"),
                    "nocking_point" to tr("Détalonnage", "Nocking point"),
                    "clicker_position" to tr("Position du clicker", "Clicker position"),
                )) Input(fields[key].orEmpty(), { set(key, it) }, label)
        } else {
            if (e.kind == "bow") {
                Choice(
                    fields["type"].orEmpty(),
                    (data.bowTypes + fields["type"].orEmpty()).distinct(),
                    tr("Type d’arc", "Bow type"),
                ) {
                    set("type", it)
                }
                Input(
                    fields["type"].orEmpty(),
                    { set("type", it) },
                    tr("Type personnalisé", "Custom bow type"),
                )
            }
            Input(fields["brand"].orEmpty(), { set("brand", it) }, tr("Marque", "Brand"))
            Input(fields["model"].orEmpty(), { set("model", it) }, tr("Modèle", "Model"))
            val keys =
                if (e.kind == "bow")
                    listOf(
                        "draw_weight",
                        "draw_length",
                        "riser",
                        "limbs",
                        "string",
                        "sight",
                        "rest",
                        "stabilization",
                    )
                else
                    listOf(
                        "material",
                        "spine",
                        "length",
                        "diameter",
                        "total_weight",
                        "point_weight",
                        "fletching",
                        "nock",
                        "quantity",
                    )
            val fr =
                mapOf(
                    "draw_weight" to "Puissance (lb)",
                    "draw_length" to "Allonge (po)",
                    "riser" to "Poignée",
                    "limbs" to "Branches",
                    "string" to "Corde",
                    "sight" to "Viseur",
                    "rest" to "Repose-flèche",
                    "stabilization" to "Stabilisation",
                    "material" to "Matériau",
                    "spine" to "Spine",
                    "length" to "Longueur (po)",
                    "diameter" to "Diamètre extérieur (mm)",
                    "total_weight" to "Poids total (grains)",
                    "point_weight" to "Poids de pointe (grains)",
                    "fletching" to "Empennage",
                    "nock" to "Encoche",
                    "quantity" to "Quantité",
                )
            val en =
                mapOf(
                    "draw_weight" to "Draw weight (lb)",
                    "draw_length" to "Draw length (in)",
                    "riser" to "Riser",
                    "limbs" to "Limbs",
                    "string" to "String",
                    "sight" to "Sight",
                    "rest" to "Arrow rest",
                    "stabilization" to "Stabilization",
                    "material" to "Material",
                    "spine" to "Spine",
                    "length" to "Length (in)",
                    "diameter" to "Outer diameter (mm)",
                    "total_weight" to "Total weight (grains)",
                    "point_weight" to "Point weight (grains)",
                    "fletching" to "Fletching",
                    "nock" to "Nock",
                    "quantity" to "Quantity",
                )
            for (key in keys) Input(
                fields[key].orEmpty(),
                { set(key, it) },
                tr(fr[key]!!, en[key]!!),
            )
        }
        Input(
            fields["notes"].orEmpty(),
            { set("notes", it) },
            tr("Notes", "Notes"),
            multiline = true,
        )
    }
}
