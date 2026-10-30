package com.plumedours.mire.domain

import java.time.Instant
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

enum class Layout {
    SINGLE,
    VERTICAL,
    TRIANGLE,
    QUAD,
    FIELD_GRID,
}

enum class Rule {
    TEN,
    INNER_TEN,
    FIELD,
    WORCESTER,
    THREE_D_11,
    THREE_D_12,
    THREE_D_X,
    NATURE,
    BEURSAULT,
    BALLOON,
    BLANK,
}

enum class Animal {
    BOAR,
    DEER,
    FOX,
}

data class TargetSpec(
    val id: String,
    val name: String,
    val diameterCm: Double = 40.0,
    val layout: Layout = Layout.SINGLE,
    val rule: Rule = Rule.TEN,
    val reduced: Boolean = false,
    val legacy: Boolean = false,
    val outerScore: Int = 0,
    val centreStyle: String = "standard",
    val animal: Animal = Animal.BOAR,
    val zoneStyle: String = "neutral",
) {
    val spotCount
        get() =
            when (layout) {
                Layout.SINGLE -> 1
                Layout.QUAD -> 4
                Layout.FIELD_GRID -> 12
                else -> 3
            }

    // Coordinates use the radius of the nominal full face, independently of its cropped appearance.
    val lowestScore
        get() =
            if (rule in listOf(Rule.TEN, Rule.INNER_TEN)) {
                if (outerScore in 1..10) outerScore else if (reduced || spotCount > 1) 6 else 1
            } else 1

    val visibleRadius
        get() = if (rule in listOf(Rule.TEN, Rule.INNER_TEN)) (11 - lowestScore) / 10.0 else 1.0

    val animalTarget
        get() = rule in listOf(Rule.THREE_D_11, Rule.THREE_D_12, Rule.THREE_D_X, Rule.NATURE)

    val isCourse
        get() = animalTarget || rule == Rule.FIELD

    val canPlace
        get() = rule != Rule.BEURSAULT

    val family
        get() =
            when {
                animalTarget -> "animal"
                rule == Rule.BALLOON -> "balloon"
                rule == Rule.FIELD -> "field"
                rule in listOf(Rule.BEURSAULT, Rule.WORCESTER, Rule.BLANK) -> "tradition"
                spotCount > 1 -> "multi"
                else -> "target"
            }

    val maxScore
        get() =
            when (rule) {
                Rule.FIELD -> 6
                Rule.WORCESTER -> 5
                Rule.THREE_D_11 -> 11
                Rule.THREE_D_12 -> 12
                Rule.NATURE -> 20
                Rule.BEURSAULT -> 4
                Rule.BALLOON -> 1
                Rule.BLANK -> 0
                else -> 10
            }

    val positionalAnalysis
        get() = !animalTarget && rule != Rule.BEURSAULT
}

object Targets {
    val all: List<TargetSpec> = buildList {
        for (size in listOf(40, 60, 80, 122)) add(
            TargetSpec("wa$size", "$size cm · 10 zones|$size cm · 10 rings", size.toDouble())
        )
        add(TargetSpec("compound40", "40 cm · poulies|40 cm · compound", rule = Rule.INNER_TEN))
        for ((layout, key, label, en) in
            listOf(
                listOf(Layout.VERTICAL, "vertical", "Vertical", "Vertical"),
                listOf(Layout.TRIANGLE, "triangle", "Triangle", "Triangle"),
            )) {
            layout as Layout
            key as String
            label as String
            en as String
            add(
                TargetSpec(
                    "${key}40",
                    "Trispot $label · standard|$en triple · standard",
                    layout = layout,
                )
            )
            add(
                TargetSpec(
                    if (key == "vertical") "verticalInner" else "triangleInner",
                    "Trispot $label · poulies|$en triple · compound",
                    layout = layout,
                    rule = Rule.INNER_TEN,
                    centreStyle = "compound",
                )
            )
            add(
                TargetSpec(
                    "${key}Recurve",
                    "Trispot $label · classique|$en triple · recurve",
                    layout = layout,
                    centreStyle = "recurve",
                )
            )
        }
        add(
            TargetSpec(
                "quad80",
                "4 blasons · 80 cm · zones 5–10|4 faces · 80 cm · rings 5–10",
                80.0,
                Layout.QUAD,
                outerScore = 5,
            )
        )
        add(
            TargetSpec(
                "quad40",
                "4 blasons · 40 cm · zones 5–10|4 faces · 40 cm · rings 5–10",
                40.0,
                Layout.QUAD,
                outerScore = 5,
            )
        )
        add(TargetSpec("reduced80", "80 cm · zones 6–10|80 cm · rings 6–10", 80.0, reduced = true))
        add(TargetSpec("six80", "80 cm · zones 5–10|80 cm · rings 5–10", 80.0, outerScore = 5))
        for (size in listOf(20, 40, 60, 80)) add(
            TargetSpec(
                "field$size",
                "Campagne $size cm|Field $size cm",
                size.toDouble(),
                rule = Rule.FIELD,
            )
        )
        add(
            TargetSpec(
                "fieldQuad40",
                "Campagne · 4 blasons 40 cm|Field · 4 faces 40 cm",
                40.0,
                Layout.QUAD,
                Rule.FIELD,
            )
        )
        add(
            TargetSpec(
                "fieldTriple20",
                "Campagne · trispot 20 cm|Field · vertical triple 20 cm",
                20.0,
                Layout.VERTICAL,
                Rule.FIELD,
            )
        )
        add(
            TargetSpec(
                "fieldGrid20",
                "Campagne · 12 blasons 20 cm|Field · 12 faces 20 cm",
                20.0,
                Layout.FIELD_GRID,
                Rule.FIELD,
            )
        )
        add(
            TargetSpec(
                "beursault",
                "Beursault · 4 / 3 / 2 / 1|Beursault · 4 / 3 / 2 / 1",
                45.0,
                rule = Rule.BEURSAULT,
            )
        )
        add(
            TargetSpec(
                "worcester",
                "Worcester · 5 zones|Worcester · 5 rings",
                40.64,
                rule = Rule.WORCESTER,
            )
        )
        for (animal in Animal.entries) {
            val fr =
                when (animal) {
                    Animal.BOAR -> "Sanglier"
                    Animal.DEER -> "Chevreuil"
                    Animal.FOX -> "Renard"
                }
            val en =
                when (animal) {
                    Animal.BOAR -> "Boar"
                    Animal.DEER -> "Deer"
                    Animal.FOX -> "Fox"
                }
            for ((rule, suffix, score) in
                listOf(
                    Triple(Rule.THREE_D_11, "11", "11 / 10 / 8 / 5"),
                    Triple(Rule.THREE_D_12, "12", "12 / 10 / 8 / 5"),
                    Triple(Rule.THREE_D_X, "x", "10+ / 10 / 8 / 5"),
                    Triple(Rule.NATURE, "nature", "nature · 20 / 15 / 10"),
                )) {
                val id =
                    if (animal == Animal.BOAR && suffix in listOf("11", "12")) "3d$suffix"
                    else "animal-${animal.name}-$suffix"
                add(TargetSpec(id, "$fr · $score|$en · $score", rule = rule, animal = animal))
            }
        }
        add(
            TargetSpec(
                "balloon",
                "Ballon · touché / manqué|Balloon · hit / miss",
                5.0,
                rule = Rule.BALLOON,
            )
        )
        add(
            TargetSpec(
                "blank",
                "Paille · sans score|Blank butt · no score",
                40.0,
                rule = Rule.BLANK,
            )
        )
    }

    fun complete(existing: List<TargetSpec>) =
        existing + all.filter { seed -> existing.none { it.id == seed.id } }
}

data class Equipment(
    val id: String = newId(),
    val kind: String,
    val name: String,
    val fields: Map<String, String> = emptyMap(),
    val archived: Boolean = false,
) {
    fun field(key: String) = fields[key].orEmpty()
}

data class Session(
    val id: String = newId(),
    val name: String,
    val competition: Boolean = false,
    val startedAt: String = Instant.now().toString(),
    val endedAt: String? = null,
    val target: TargetSpec,
    val distanceM: Double = 18.0,
    val arrowsPerEnd: Int = 3,
    val plannedEnds: Int = 10,
    val bowId: String? = null,
    val arrowsId: String? = null,
    val configurationId: String? = null,
    val location: String = "",
    val notes: String = "",
    val completed: Boolean = false,
    val currentEnd: Int = 1,
    val arrowDiameterMm: Double = 0.0,
    val legacySource: String? = null,
    val extra: Map<String, String> = emptyMap(),
)

data class Impact(
    val id: String = newId(),
    val sessionId: String,
    val end: Int,
    val number: Int,
    val u: Double? = null,
    val v: Double? = null,
    val spot: Int? = 0,
    val score: Int = 0,
    val isX: Boolean = false,
    val label: String = "",
    val manual: Boolean = false,
    val uncertain: Boolean = false,
    val legacyX: Double? = null,
    val legacyY: Double? = null,
)

data class Journal(
    val sessionId: String,
    val feeling: Int = 3,
    val fatigue: Int = 2,
    val weather: String = "",
    val wind: String = "",
    val adjustments: String = "",
    val comment: String = "",
    val tags: String = "",
)

data class CourseTarget(
    val id: String = newId(),
    val sessionId: String,
    val number: Int,
    val animal: String = "",
    val distanceM: Double = 0.0,
)

data class SessionTemplate(val id: String = newId(), val name: String, val session: Session)

data class Goal(
    val id: String = newId(),
    val title: String,
    val kind: String = "arrows",
    val value: Double,
    val periodDays: Int = 30,
    val createdAt: String = Instant.now().toString(),
    val minimumSessions: Int = 1,
    val targetId: String? = null,
    val distanceM: Double? = null,
    val completed: Boolean = false,
)

data class Profile(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
)

data class Settings(
    val language: String = "fr",
    val theme: String = "sport",
    val haptics: Boolean = true,
    val keepScreenOn: Boolean = true,
    val leftHanded: Boolean = false,
    val showNumbers: Boolean = true,
    val designVersion: Int = 2,
)

data class AppData(
    val sessions: List<Session> = emptyList(),
    val impacts: List<Impact> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val targets: List<TargetSpec> = Targets.all,
    val journals: List<Journal> = emptyList(),
    val courseTargets: List<CourseTarget> = emptyList(),
    val templates: List<SessionTemplate> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val profile: Profile = Profile(),
    val settings: Settings = Settings(),
    val importKeys: List<String> = emptyList(),
    val legacyArchives: List<String> = emptyList(),
    val bowTypes: List<String> =
        listOf("Classique", "À poulies", "Barebow", "Longbow", "Traditionnel"),
) {
    fun hits(id: String) =
        impacts.filter { it.sessionId == id }.sortedWith(compareBy({ it.end }, { it.number }))
}
