package com.plumedours.mire.domain

/** One entry per visual family; size and scoring are configured separately. */
object TargetCatalog {
    private val names =
        linkedMapOf(
            "wa40" to "Blason couleurs|Colour face",
            "vertical40" to "Trispot vertical|Vertical triple",
            "triangle40" to "Trispot triangulaire|Triangular triple",
            "quad40" to "Quatre blasons|Four faces",
            "field40" to "Campagne|Field",
            "worcester" to "Worcester|Worcester",
            "3d11" to "3D · zones du vital|3D · vital zones",
            "animal-FOX-nature" to "Nature · zones du vital|Nature · vital zones",
            "beursault" to "Beursault|Beursault",
            "balloon" to "Ballon · disque|Balloon · disc",
            "blank" to "Paille|Blank butt",
        )

    fun choices(existing: List<TargetSpec>): List<TargetSpec> {
        val builtins = Targets.all.map { it.id }.toSet()
        return names.map { (id, name) ->
            val t = existing.find { it.id == id } ?: Targets.all.first { it.id == id }
            t.copy(name = name, zoneStyle = "neutral")
        } + existing.filter { it.id !in builtins }
    }
}
