package com.plumedours.mire.data

import com.plumedours.mire.domain.*
import java.security.MessageDigest
import java.time.*
import org.json.JSONObject

private fun sha(text: String) =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString("") {
        "%02x".format(it)
    }

data class ImportPreview(
    val data: AppData,
    val fingerprint: String,
    val source: String,
    val warnings: List<String>,
)

object BackupImporter {
    fun preview(text: String): ImportPreview {
        require(text.length <= 50 * 1024 * 1024) {
            "Sauvegarde trop volumineuse (50 Mo) / Backup too large"
        }
        val root = JSONObject(text)
        require(
            root.getInt("version") in
                (if (root.optString("format") == "volee-backup") 1..1 else 1..3)
        ) {
            "Version de sauvegarde non prise en charge / Unsupported backup version"
        }
        val fingerprint = sha(text)
        val result =
            when (root.getString("format")) {
                "mire-backup" ->
                    ImportPreview(
                        Codec.decode(root.getJSONObject("data")),
                        fingerprint,
                        "Mire",
                        emptyList(),
                    )
                "volee-backup" -> legacy(root, text, fingerprint)
                else ->
                    error(
                        "Ce fichier n’est pas une sauvegarde Mire ou Volée / Not a Mire or Volée backup"
                    )
            }
        DataValidator.validate(result.data)
        return result
    }

    fun merge(current: AppData, p: ImportPreview, replace: Boolean): AppData {
        require(replace || p.fingerprint !in current.importKeys) {
            "Cette sauvegarde a déjà été importée / Backup already imported"
        }
        if (replace)
            return p.data.copy(
                settings = current.settings,
                importKeys = (p.data.importKeys + p.fingerprint).distinct(),
            )
        val n = p.data
        fun <T> add(a: List<T>, b: List<T>, id: (T) -> String) =
            a + b.filter { item -> a.none { id(it) == id(item) } }
        return current.copy(
            sessions = add(current.sessions, n.sessions) { it.id },
            impacts = add(current.impacts, n.impacts) { it.id },
            equipment = add(current.equipment, n.equipment) { it.id },
            targets = add(current.targets, n.targets) { it.id },
            journals = add(current.journals, n.journals) { it.sessionId },
            courseTargets = add(current.courseTargets, n.courseTargets) { it.id },
            templates = add(current.templates, n.templates) { it.id },
            goals = add(current.goals, n.goals) { it.id },
            profile = if (current.profile.firstName.isBlank()) n.profile else current.profile,
            bowTypes = (current.bowTypes + n.bowTypes).distinct(),
            importKeys = (current.importKeys + n.importKeys + p.fingerprint).distinct(),
            legacyArchives = (current.legacyArchives + n.legacyArchives).distinct(),
        )
    }

    private fun legacy(root: JSONObject, text: String, hash: String): ImportPreview {
        val tables = root.getJSONObject("tables")
        val prefix = "volee-${hash.take(16)}-"
        fun id(kind: String, value: String) = "$prefix$kind-$value"
        fun ref(row: JSONObject, key: String, kind: String) =
            row.nullString(key)?.let { id(kind, it) }
        fun fields(row: JSONObject) =
            row.keys().asSequence().filter { it != "id" }.associateWith { row.s(it) }
        val targets =
            tables.rows("targets").map { t ->
                val kind = t.s("kind")
                require(kind in listOf("concentric", "trispot", "field", "threeD", "balloon")) {
                    "Type de cible Volée inconnu: $kind"
                }
                TargetSpec(
                    id("target", t.s("id")),
                    t.s("name"),
                    t.num("face_size_cm", 40.0),
                    if (kind == "trispot") Layout.VERTICAL else Layout.SINGLE,
                    when (kind) {
                        "field" -> if (t.optInt("max_score", 10) == 6) Rule.FIELD else Rule.TEN
                        "threeD" -> Rule.THREE_D_11
                        "balloon" -> Rule.BALLOON
                        else -> Rule.TEN
                    },
                    legacy = true,
                )
            }
        val targetMap = targets.associateBy { it.id }
        val equipment =
            tables.rows("bows").map {
                Equipment(id("bow", it.s("id")), "bow", it.s("name"), fields(it))
            } +
                tables.rows("arrow_sets").map {
                    Equipment(id("arrows", it.s("id")), "arrows", it.s("name"), fields(it))
                } +
                tables.rows("equipment_configurations").map {
                    Equipment(
                        id("config", it.s("id")),
                        "config",
                        it.s("name"),
                        fields(it) +
                            mapOf(
                                "bow_id" to (ref(it, "bow_id", "bow") ?: ""),
                                "arrow_set_id" to (ref(it, "arrow_set_id", "arrows") ?: ""),
                            ),
                    )
                }
        val rawImpacts = tables.rows("impacts")
        fun date(v: String): String =
            runCatching { Instant.parse(v).toString() }
                .getOrElse {
                    runCatching { OffsetDateTime.parse(v).toInstant().toString() }
                        .getOrElse {
                            LocalDateTime.parse(v)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toString()
                        }
                }
        fun buildSession(r: JSONObject, template: Boolean = false): Session {
            val target =
                targetMap[ref(r, "target_id", "target")]
                    ?: error("Référence de cible absente / Missing target reference")
            val ends =
                rawImpacts
                    .filter { it.s("session_id") == r.s("id") }
                    .maxOfOrNull { it.getInt("end_number") } ?: 1
            return Session(
                id(if (template) "template-session" else "session", r.s("id")),
                r.s("name"),
                r.s("kind") == "competition",
                if (template) Instant.EPOCH.toString() else date(r.getString("started_at")),
                r.nullString("ended_at")?.let(::date),
                if (target.rule == Rule.BALLOON)
                    target.copy(diameterCm = r.num("balloon_diameter_cm", target.diameterCm))
                else target,
                r.num("distance_m"),
                r.optInt("arrows_per_end", 3),
                r.optInt("planned_ends"),
                ref(r, "bow_id", "bow"),
                ref(r, "arrow_set_id", "arrows"),
                ref(r, "equipment_configuration_id", "config"),
                r.s("location"),
                r.s("notes"),
                r.optInt("completed") == 1,
                ends,
                equipment
                    .find { it.id == ref(r, "arrow_set_id", "arrows") }
                    ?.field("diameter")
                    ?.replace(',', '.')
                    ?.toDoubleOrNull() ?: 0.0,
                hash,
                fields(r),
            )
        }
        val sessions = tables.rows("sessions").map { buildSession(it) }
        val sessionMap = sessions.associateBy { it.id }
        val impacts =
            rawImpacts.map { r ->
                val sid = id("session", r.s("session_id"))
                val session =
                    sessionMap[sid] ?: error("Séance d’impact absente / Missing arrow session")
                val x = r.getDouble("x")
                val y = r.getDouble("y")
                require(x.isFinite() && y.isFinite())
                val point = TargetEngine.legacyPoint(session.target, x, y)
                Impact(
                    id("impact", r.s("id")),
                    sid,
                    r.getInt("end_number"),
                    r.getInt("arrow_number"),
                    point?.u,
                    point?.v,
                    point?.spot,
                    r.getInt("score"),
                    r.optInt("is_x") == 1,
                    r.s("arrow_label"),
                    true,
                    point?.uncertain ?: false,
                    x,
                    y,
                )
            }
        val journals =
            tables.rows("session_journals").map { r ->
                Journal(
                    id("session", r.s("session_id")),
                    r.optInt("feeling", 3),
                    r.optInt("fatigue", 2),
                    r.s("weather"),
                    r.s("wind"),
                    r.s("adjustments"),
                    r.s("comment"),
                    r.s("tags"),
                )
            }
        val goals =
            tables.rows("personal_goals").map { r ->
                Goal(
                    id("goal", r.s("id")),
                    r.s("title"),
                    r.s("kind"),
                    r.num("target_value"),
                    r.optInt("period_days", 30),
                    date(r.getString("created_at")),
                    r.optInt("minimum_sessions", 1),
                    ref(r, "target_id", "target"),
                    r.nullDouble("distance_m"),
                    r.optInt("completed") == 1,
                )
            }
        val profile =
            tables.rows("user_profile").firstOrNull()?.let {
                Profile(it.s("first_name"), it.s("last_name"), it.s("birth_date"))
            } ?: Profile()
        val warnings = buildList {
            add(
                "Les scores et les X historiques sont conservés, sans recalcul. / Historical scores and X values are preserved."
            )
            if (impacts.any { it.uncertain })
                add(
                    "${impacts.count { it.uncertain }} impact(s) trispot ambigu(s) : exclus de l’analyse géométrique jusqu’à correction. / Ambiguous triple-spot arrows excluded until reviewed."
                )
            if (targets.any { !it.positionalAnalysis })
                add(
                    "Les tirs 3D conservent leurs scores et coordonnées originales ; l’analyse sur un blason circulaire est désactivée. / 3D geometry is not analysed as a circular face."
                )
            if (
                tables.rows("sessions").any {
                    !it.s("started_at").endsWith("Z") &&
                        !Regex("[+-]\\d\\d:\\d\\d$").containsMatchIn(it.s("started_at"))
                }
            )
                add(
                    "Les dates sans fuseau utilisent le fuseau de cet appareil. / Dates without timezone use this device’s timezone."
                )
            add(
                "Importer une seule sauvegarde Volée complète : deux exports différents peuvent contenir les mêmes séances. / Import one complete Volée backup; different exports may overlap."
            )
            add(
                "L’archive source complète est conservée dans les sauvegardes Mire. / The complete source is retained in Mire backups."
            )
        }
        return ImportPreview(
            AppData(
                sessions,
                impacts,
                equipment,
                Targets.all + targets,
                journals,
                tables.rows("course_targets").map {
                    CourseTarget(
                        id("course", it.s("id")),
                        id("session", it.s("session_id")),
                        it.getInt("number"),
                        it.s("animal"),
                        it.num("distance_m"),
                    )
                },
                tables.rows("session_templates").map {
                    SessionTemplate(
                        id("template", it.s("id")),
                        it.s("name"),
                        buildSession(it, true),
                    )
                },
                goals,
                profile,
                legacyArchives = listOf(text),
                bowTypes =
                    tables.rows("bow_types").map { it.s("name") }.ifEmpty { AppData().bowTypes },
            ),
            hash,
            "Volée",
            warnings,
        )
    }
}
