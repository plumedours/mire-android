package com.plumedours.mire.data

import android.content.Context
import androidx.room.*
import com.plumedours.mire.domain.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "records", indices = [Index("kind"), Index("parent")])
data class Record(
    @PrimaryKey val key: String,
    val kind: String,
    val parent: String = "",
    val payload: String,
)

@Dao
interface RecordDao {
    @Query("SELECT * FROM records") suspend fun all(): List<Record>

    @Upsert suspend fun save(rows: List<Record>)

    @Query("DELETE FROM records WHERE `key` IN (:keys)") suspend fun delete(keys: List<String>)
}

@Database(entities = [Record::class], version = 1, exportSchema = true)
abstract class MireDatabase : RoomDatabase() {
    abstract fun records(): RecordDao
}

class Repository(context: Context, databaseName: String = "mire.db") {
    private val db =
        Room.databaseBuilder(context.applicationContext, MireDatabase::class.java, databaseName)
            .build()
    private val lock = Mutex()
    private val mutable = MutableStateFlow<AppData?>(null)
    val data = mutable.asStateFlow()

    suspend fun load() =
        lock.withLock {
            withContext(Dispatchers.IO) {
                val rows = db.records().all()
                mutable.value =
                    if (rows.isEmpty())
                        AppData().also { seed ->
                            db.withTransaction { db.records().save(records(seed)) }
                        }
                    else {
                        val root = JSONObject()
                        rows
                            .groupBy { it.kind }
                            .forEach { (kind, rs) ->
                                if (kind == "archiveChunks") {
                                    val archives =
                                        rs.groupBy { it.parent }
                                            .entries
                                            .sortedBy { it.key.toInt() }
                                            .map { (_, chunks) ->
                                                chunks
                                                    .sortedBy {
                                                        JSONObject(it.payload).getInt("index")
                                                    }
                                                    .joinToString("") {
                                                        JSONObject(it.payload).getString("part")
                                                    }
                                            }
                                    root.put("legacyArchives", JSONArray(archives))
                                } else if (kind == "meta")
                                    rs.forEach {
                                        root.put(
                                            it.key.removePrefix("meta:"),
                                            JSONObject(it.payload).get("value"),
                                        )
                                    }
                                else
                                    root.put(
                                        kind,
                                        JSONArray().also { a ->
                                            rs.forEach { a.put(JSONObject(it.payload)) }
                                        },
                                    )
                            }
                        Codec.decode(root).let { loaded ->
                            val upgraded =
                                loaded.copy(
                                    targets =
                                        com.plumedours.mire.domain.Targets.complete(loaded.targets),
                                    settings =
                                        if (loaded.settings.designVersion < 2)
                                            loaded.settings.copy(theme = "sport", designVersion = 2)
                                        else loaded.settings,
                                )
                            if (upgraded != loaded)
                                db.withTransaction {
                                    db.records()
                                        .save(
                                            records(upgraded).filter { row ->
                                                rows.none { old ->
                                                    old.key == row.key && old.payload == row.payload
                                                }
                                            }
                                        )
                                }
                            upgraded
                        }
                    }
            }
        }

    suspend fun update(change: (AppData) -> AppData) =
        lock.withLock {
            withContext(Dispatchers.IO) {
                val before = mutable.value ?: error("Chargement en cours / Loading")
                val after = change(before)
                validate(after)
                val old = records(before).associateBy { it.key }
                val next = records(after).associateBy { it.key }
                db.withTransaction {
                    val deleted = old.keys - next.keys
                    if (deleted.isNotEmpty())
                        deleted.chunked(500).forEach { db.records().delete(it) }
                    val changed = next.values.filter { old[it.key] != it }
                    if (changed.isNotEmpty()) db.records().save(changed)
                }
                mutable.value = after
            }
        }

    private fun records(d: AppData): List<Record> {
        val j = Codec.encode(d)
        return j.keys()
            .asSequence()
            .flatMap { kind ->
                if (kind == "legacyArchives") {
                    d.legacyArchives.asSequence().flatMapIndexed { archiveIndex, text ->
                        text.chunked(100_000).asSequence().mapIndexed { index, part ->
                            Record(
                                "archives:$archiveIndex:$index",
                                "archiveChunks",
                                archiveIndex.toString(),
                                obj("index" to index, "part" to part).toString(),
                            )
                        }
                    }
                } else if (
                    kind in
                        listOf(
                            "sessions",
                            "impacts",
                            "equipment",
                            "targets",
                            "journals",
                            "courseTargets",
                            "templates",
                            "goals",
                        )
                ) {
                    j.rows(kind).asSequence().map { row ->
                        val id =
                            if (kind == "journals") row.getString("sessionId")
                            else row.getString("id")
                        Record("$kind:$id", kind, row.s("sessionId"), row.toString())
                    }
                } else
                    sequenceOf(
                        Record(
                            "meta:$kind",
                            "meta",
                            payload = obj("value" to j.get(kind)).toString(),
                        )
                    )
            }
            .toList()
    }

    fun close() {
        if (db.isOpen) db.close()
    }

    companion object {
        fun validate(d: AppData) = com.plumedours.mire.domain.DataValidator.validate(d)
    }
}
