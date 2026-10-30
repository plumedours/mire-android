package com.plumedours.mire.data

import com.plumedours.mire.domain.*
import org.json.JSONArray
import org.json.JSONObject

internal fun obj(vararg pairs: Pair<String, Any?>): JSONObject =
    JSONObject().also { j -> pairs.forEach { (k, v) -> j.put(k, wrap(v)) } }

private fun wrap(v: Any?): Any =
    when (v) {
        null -> JSONObject.NULL
        is Map<*, *> ->
            JSONObject().also { j -> v.forEach { (k, x) -> j.put(k.toString(), wrap(x)) } }
        is Iterable<*> -> JSONArray().also { a -> v.forEach { a.put(wrap(it)) } }
        else -> v
    }

internal fun JSONObject.s(k: String, default: String = "") =
    if (isNull(k)) default else optString(k, default)

internal fun JSONObject.nullString(k: String): String? =
    if (isNull(k) || !has(k)) null else get(k).toString()

internal fun JSONObject.num(k: String, default: Double = 0.0) =
    if (isNull(k)) default else optDouble(k, default)

internal fun JSONObject.nullDouble(k: String): Double? =
    if (isNull(k) || !has(k)) null else getDouble(k)

internal fun JSONObject.rows(k: String) =
    optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getJSONObject(it) } }.orEmpty()

internal fun JSONObject.strings(k: String) =
    optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty()

internal fun JSONObject.map(k: String): Map<String, String> =
    optJSONObject(k)?.let { o -> o.keys().asSequence().associateWith { o.s(it) } }.orEmpty()

object Codec {
    fun target(t: TargetSpec) =
        obj(
            "id" to t.id,
            "name" to t.name,
            "diameterCm" to t.diameterCm,
            "layout" to t.layout.name,
            "rule" to t.rule.name,
            "reduced" to t.reduced,
            "legacy" to t.legacy,
            "outerScore" to t.outerScore,
            "centreStyle" to t.centreStyle,
            "animal" to t.animal.name,
            "zoneStyle" to t.zoneStyle,
        )

    fun target(j: JSONObject) =
        TargetSpec(
            j.getString("id"),
            j.getString("name"),
            j.getDouble("diameterCm"),
            Layout.valueOf(j.getString("layout")),
            Rule.valueOf(j.getString("rule")),
            j.optBoolean("reduced"),
            j.optBoolean("legacy"),
            j.optInt("outerScore"),
            j.s("centreStyle", "standard"),
            Animal.valueOf(j.s("animal", "BOAR")),
            j.s(
                "zoneStyle",
                if (j.s("rule") in listOf("THREE_D_11", "THREE_D_12", "THREE_D_X", "NATURE"))
                    "animal-v1"
                else "neutral",
            ),
        )

    fun session(s: Session) =
        obj(
            "id" to s.id,
            "name" to s.name,
            "competition" to s.competition,
            "startedAt" to s.startedAt,
            "endedAt" to s.endedAt,
            "target" to target(s.target),
            "distanceM" to s.distanceM,
            "arrowsPerEnd" to s.arrowsPerEnd,
            "plannedEnds" to s.plannedEnds,
            "bowId" to s.bowId,
            "arrowsId" to s.arrowsId,
            "configurationId" to s.configurationId,
            "location" to s.location,
            "notes" to s.notes,
            "completed" to s.completed,
            "currentEnd" to s.currentEnd,
            "arrowDiameterMm" to s.arrowDiameterMm,
            "legacySource" to s.legacySource,
            "extra" to s.extra,
        )

    fun session(j: JSONObject) =
        Session(
            j.getString("id"),
            j.getString("name"),
            j.optBoolean("competition"),
            j.getString("startedAt"),
            j.nullString("endedAt"),
            target(j.getJSONObject("target")),
            j.num("distanceM", 18.0),
            j.optInt("arrowsPerEnd", 3),
            j.optInt("plannedEnds", 10),
            j.nullString("bowId"),
            j.nullString("arrowsId"),
            j.nullString("configurationId"),
            j.s("location"),
            j.s("notes"),
            j.optBoolean("completed"),
            j.optInt("currentEnd", 1),
            j.num("arrowDiameterMm"),
            j.nullString("legacySource"),
            j.map("extra"),
        )

    fun impact(i: Impact) =
        obj(
            "id" to i.id,
            "sessionId" to i.sessionId,
            "end" to i.end,
            "number" to i.number,
            "u" to i.u,
            "v" to i.v,
            "spot" to i.spot,
            "score" to i.score,
            "isX" to i.isX,
            "label" to i.label,
            "manual" to i.manual,
            "uncertain" to i.uncertain,
            "legacyX" to i.legacyX,
            "legacyY" to i.legacyY,
        )

    fun impact(j: JSONObject) =
        Impact(
            j.getString("id"),
            j.getString("sessionId"),
            j.getInt("end"),
            j.getInt("number"),
            j.nullDouble("u"),
            j.nullDouble("v"),
            if (j.isNull("spot")) null else j.getInt("spot"),
            j.getInt("score"),
            j.optBoolean("isX"),
            j.s("label"),
            j.optBoolean("manual"),
            j.optBoolean("uncertain"),
            j.nullDouble("legacyX"),
            j.nullDouble("legacyY"),
        )

    fun equipment(e: Equipment) =
        obj(
            "id" to e.id,
            "kind" to e.kind,
            "name" to e.name,
            "fields" to e.fields,
            "archived" to e.archived,
        )

    fun equipment(j: JSONObject) =
        Equipment(
            j.getString("id"),
            j.getString("kind"),
            j.getString("name"),
            j.map("fields"),
            j.optBoolean("archived"),
        )

    fun journal(j: Journal) =
        obj(
            "sessionId" to j.sessionId,
            "feeling" to j.feeling,
            "fatigue" to j.fatigue,
            "weather" to j.weather,
            "wind" to j.wind,
            "adjustments" to j.adjustments,
            "comment" to j.comment,
            "tags" to j.tags,
        )

    fun journal(j: JSONObject) =
        Journal(
            j.getString("sessionId"),
            j.optInt("feeling", 3),
            j.optInt("fatigue", 2),
            j.s("weather"),
            j.s("wind"),
            j.s("adjustments"),
            j.s("comment"),
            j.s("tags"),
        )

    fun goal(g: Goal) =
        obj(
            "id" to g.id,
            "title" to g.title,
            "kind" to g.kind,
            "value" to g.value,
            "periodDays" to g.periodDays,
            "createdAt" to g.createdAt,
            "minimumSessions" to g.minimumSessions,
            "targetId" to g.targetId,
            "distanceM" to g.distanceM,
            "completed" to g.completed,
        )

    fun goal(j: JSONObject) =
        Goal(
            j.getString("id"),
            j.getString("title"),
            j.s("kind", "arrows"),
            j.getDouble("value"),
            j.optInt("periodDays", 30),
            j.getString("createdAt"),
            j.optInt("minimumSessions", 1),
            j.nullString("targetId"),
            j.nullDouble("distanceM"),
            j.optBoolean("completed"),
        )

    fun encode(d: AppData): JSONObject =
        obj(
            "sessions" to d.sessions.map(::session),
            "impacts" to d.impacts.map(::impact),
            "equipment" to d.equipment.map(::equipment),
            "targets" to d.targets.map(::target),
            "journals" to d.journals.map(::journal),
            "courseTargets" to
                d.courseTargets.map {
                    obj(
                        "id" to it.id,
                        "sessionId" to it.sessionId,
                        "number" to it.number,
                        "animal" to it.animal,
                        "distanceM" to it.distanceM,
                    )
                },
            "templates" to
                d.templates.map {
                    obj("id" to it.id, "name" to it.name, "session" to session(it.session))
                },
            "goals" to d.goals.map(::goal),
            "profile" to
                obj(
                    "firstName" to d.profile.firstName,
                    "lastName" to d.profile.lastName,
                    "birthDate" to d.profile.birthDate,
                ),
            "settings" to
                obj(
                    "language" to d.settings.language,
                    "theme" to d.settings.theme,
                    "haptics" to d.settings.haptics,
                    "keepScreenOn" to d.settings.keepScreenOn,
                    "leftHanded" to d.settings.leftHanded,
                    "showNumbers" to d.settings.showNumbers,
                    "designVersion" to d.settings.designVersion,
                ),
            "importKeys" to d.importKeys,
            "legacyArchives" to d.legacyArchives,
            "bowTypes" to d.bowTypes,
        )

    fun decode(j: JSONObject): AppData {
        val p = j.optJSONObject("profile") ?: JSONObject()
        val s = j.optJSONObject("settings") ?: JSONObject()
        return AppData(
            j.rows("sessions").map { session(it) },
            j.rows("impacts").map { impact(it) },
            j.rows("equipment").map { equipment(it) },
            j.rows("targets").map { target(it) }.ifEmpty { Targets.all },
            j.rows("journals").map { journal(it) },
            j.rows("courseTargets").map {
                CourseTarget(
                    it.getString("id"),
                    it.getString("sessionId"),
                    it.getInt("number"),
                    it.s("animal"),
                    it.num("distanceM"),
                )
            },
            j.rows("templates").map {
                SessionTemplate(
                    it.getString("id"),
                    it.getString("name"),
                    session(it.getJSONObject("session")),
                )
            },
            j.rows("goals").map { goal(it) },
            Profile(p.s("firstName"), p.s("lastName"), p.s("birthDate")),
            Settings(
                s.s("language", "fr"),
                s.s("theme", "sport"),
                s.optBoolean("haptics", true),
                s.optBoolean("keepScreenOn", true),
                s.optBoolean("leftHanded"),
                s.optBoolean("showNumbers", true),
                s.optInt("designVersion", 1),
            ),
            j.strings("importKeys"),
            j.strings("legacyArchives"),
            j.strings("bowTypes").ifEmpty { AppData().bowTypes },
        )
    }

    fun backup(d: AppData): String =
        obj(
                "format" to "mire-backup",
                "version" to 3,
                "created_at" to java.time.Instant.now().toString(),
                "data" to encode(d),
            )
            .toString(2)
}
