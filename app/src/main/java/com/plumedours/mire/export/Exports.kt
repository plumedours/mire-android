package com.plumedours.mire.export

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.plumedours.mire.domain.*
import com.plumedours.mire.ui.TargetRenderer
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Exports {
    fun csv(d: AppData): ByteArray {
        fun cell(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
        val rows =
            mutableListOf(
                listOf(
                    "Session ID",
                    "Date",
                    "Session",
                    "Target",
                    "Distance m",
                    "End",
                    "Arrow",
                    "Score",
                    "X",
                    "Spot",
                    "U",
                    "V",
                    "Manual",
                    "Review",
                    "Label",
                )
            )
        d.sessions
            .sortedBy { it.startedAt }
            .forEach { s ->
                d.hits(s.id).forEach { h ->
                    rows.add(
                        listOf(
                            s.id,
                            s.startedAt,
                            s.name,
                            s.target.name.substringBefore('|'),
                            "${s.distanceM}",
                            "${h.end}",
                            "${h.number}",
                            "${h.score}",
                            "${h.isX}",
                            h.spot?.let { "${it+1}" }.orEmpty(),
                            h.u?.toString().orEmpty(),
                            h.v?.toString().orEmpty(),
                            "${h.manual}",
                            "${h.uncertain}",
                            h.label,
                        )
                    )
                }
            }
        return ("\uFEFF" +
                rows.joinToString("\r\n") { row ->
                    row.joinToString(";") {
                        cell(
                            if (
                                (it.startsWith("=") ||
                                    it.startsWith("+") ||
                                    it.startsWith("-") ||
                                    it.startsWith("@")) && it.toDoubleOrNull() == null
                            )
                                "'$it"
                            else it
                        )
                    }
                })
            .toByteArray(Charsets.UTF_8)
    }

    fun xlsx(d: AppData): ByteArray {
        val summary =
            listOf(
                listOf(
                    "ID",
                    "Date",
                    "Session",
                    "Target",
                    "Distance m",
                    "Arrows",
                    "Score",
                    "Average",
                    "Completed",
                )
            ) +
                d.sessions
                    .sortedBy { it.startedAt }
                    .map { s ->
                        val hits = d.hits(s.id)
                        listOf(
                            s.id,
                            s.startedAt,
                            s.name,
                            s.target.name.substringBefore('|'),
                            s.distanceM,
                            hits.size,
                            hits.sumOf { it.score },
                            if (hits.isEmpty()) 0.0 else hits.map { it.score }.average(),
                            s.completed.toString(),
                        )
                    }
        val arrows =
            listOf(
                listOf(
                    "Session ID",
                    "End",
                    "Arrow",
                    "Score",
                    "X",
                    "Spot",
                    "U",
                    "V",
                    "Label",
                    "Review",
                )
            ) +
                d.impacts.map { h ->
                    listOf(
                        h.sessionId,
                        h.end,
                        h.number,
                        h.score,
                        h.isX.toString(),
                        h.spot?.plus(1) ?: "",
                        h.u ?: "",
                        h.v ?: "",
                        h.label,
                        h.uncertain.toString(),
                    )
                }
        fun xml(v: Any?) =
            v.toString()
                .filter { it == '\n' || it == '\r' || it == '\t' || it.code >= 32 }
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
        fun sheet(rows: List<List<Any>>) =
            """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""" +
                rows
                    .mapIndexed { r, row ->
                        "<row r=\"${r+1}\">" +
                            row.joinToString("") { value ->
                                if (value is Number) "<c><v>$value</v></c>"
                                else
                                    "<c t=\"inlineStr\"><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>"
                            } +
                            "</row>"
                    }
                    .joinToString("") +
                "</sheetData></worksheet>"
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun entry(name: String, text: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entry(
                "[Content_Types].xml",
                """<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""",
            )
            entry(
                "_rels/.rels",
                """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""",
            )
            entry(
                "xl/workbook.xml",
                """<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Sessions" sheetId="1" r:id="rId1"/><sheet name="Arrows" sheetId="2" r:id="rId2"/></sheets></workbook>""",
            )
            entry(
                "xl/_rels/workbook.xml.rels",
                """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/></Relationships>""",
            )
            entry("xl/worksheets/sheet1.xml", sheet(summary))
            entry("xl/worksheets/sheet2.xml", sheet(arrows))
        }
        return out.toByteArray()
    }

    fun pdf(d: AppData, sessionId: String? = null): ByteArray {
        val doc = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val sessions =
            d.sessions
                .filter { sessionId == null || it.id == sessionId }
                .sortedByDescending { it.startedAt }
        var pageNumber = 0
        fun page(title: String, body: (Canvas) -> Unit) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(612, 792, ++pageNumber).create())
            val c = page.canvas
            c.drawColor(Color.WHITE)
            paint.color = 0xff0a7e70.toInt()
            paint.textSize = 24f
            paint.typeface = Typeface.DEFAULT_BOLD
            c.drawText("MIRE", 36f, 45f, paint)
            paint.color = Color.DKGRAY
            paint.textSize = 12f
            c.drawText(title.take(80), 36f, 70f, paint)
            body(c)
            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            c.drawText("Mire · ${LocalDate.now()} · $pageNumber", 36f, 765f, paint)
            doc.finishPage(page)
        }
        if (sessions.isEmpty()) page("Aucune séance / No sessions") {}
        for (s in sessions) {
            val hits = d.hits(s.id)
            page(s.name) { c ->
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.DKGRAY
                paint.textSize = 11f
                c.drawText(
                    "${s.startedAt.take(10)} · ${s.distanceM} m · ${s.target.name.substringBefore('|')}"
                        .take(95),
                    36f,
                    95f,
                    paint,
                )
                c.drawText(
                    "Score ${hits.sumOf { it.score }} · ${hits.size} flèches / arrows · ${hits.count { it.isX }} X",
                    36f,
                    116f,
                    paint,
                )
                run {
                    c.save()
                    c.translate(126f, 140f)
                    TargetRenderer.draw(c, 360f, s.target, hits, numbers = false)
                    c.restore()
                }
                val a = TargetEngine.analyze(s.target, hits)
                if (a != null)
                    c.drawText(
                        "${a.count} positions · écart centre ${"%.2f".format(a.offsetCm)} cm · diamètre groupe ${"%.2f".format(a.extremeSpreadCm)} cm",
                        36f,
                        525f,
                        paint,
                    )
                c.drawText("Lieu / Location: ${s.location}".take(95), 36f, 551f, paint)
                val j = d.journals.find { it.sessionId == s.id }
                val notes =
                    listOf(s.notes, j?.comment.orEmpty(), j?.adjustments.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                wrapText(notes, 95).take(8).forEachIndexed { index, line ->
                    c.drawText(line, 36f, 575f + index * 16, paint)
                }
            }
            val details =
                hits.flatMap { h ->
                    listOf(
                            "${h.end}",
                            "${h.number}",
                            if (h.isX) "X" else "${h.score}",
                            h.spot?.let { "${it+1}" }.orEmpty(),
                            h.label,
                        )
                        .joinToString("    ·    ")
                        .let { wrapText(it, 90) }
                }
            for (chunk in details.chunked(32)) page("${s.name} · Flèches / Arrows") { c ->
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.DKGRAY
                paint.textSize = 10f
                c.drawText(
                    "Volée / End · Flèche / Arrow · Score · Spot · Repère / Label",
                    36f,
                    103f,
                    paint,
                )
                chunk.forEachIndexed { index, text ->
                    c.drawText(text, 36f, 128f + index * 18, paint)
                }
            }
            val j = d.journals.find { it.sessionId == s.id }
            val context =
                listOf(
                        "Lieu / Location: ${s.location}",
                        s.notes,
                        j?.let { "Ressenti / Feeling: ${it.feeling}/5 · Fatigue: ${it.fatigue}/5" }
                            .orEmpty(),
                        j?.weather.orEmpty(),
                        j?.wind.orEmpty(),
                        j?.adjustments.orEmpty(),
                        j?.comment.orEmpty(),
                        j?.tags.orEmpty(),
                    )
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
            val lines = context.lines().flatMap { wrapText(it, 90) }
            for (chunk in lines.chunked(34)) page("${s.name} · Journal") { c ->
                paint.typeface = Typeface.DEFAULT
                paint.color = Color.DKGRAY
                paint.textSize = 11f
                chunk.forEachIndexed { index, text ->
                    c.drawText(text, 36f, 106f + index * 18, paint)
                }
            }
        }
        return ByteArrayOutputStream().use { out ->
            try {
                doc.writeTo(out)
                out.toByteArray()
            } finally {
                doc.close()
            }
        }
    }

    private fun wrapText(text: String, limit: Int): List<String> {
        val lines = mutableListOf<String>()
        var line = ""
        text
            .split(Regex("\\s+"))
            .flatMap { it.chunked(limit) }
            .forEach { word ->
                if (line.length + word.length > limit) {
                    lines.add(line)
                    line = ""
                }
                line += (if (line.isEmpty()) "" else " ") + word
            }
        if (line.isNotEmpty()) lines.add(line)
        return lines
    }

    fun card(d: AppData, s: Session): ByteArray {
        val image = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val c = Canvas(image)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawColor(0xff101d2a.toInt())
        p.color = 0xff6ee7c7.toInt()
        p.textSize = 55f
        p.typeface = Typeface.DEFAULT_BOLD
        c.drawText("MIRE", 64f, 95f, p)
        p.color = Color.WHITE
        p.textSize = 40f
        c.drawText(s.name.take(40), 64f, 166f, p)
        p.textSize = 27f
        p.typeface = Typeface.DEFAULT
        c.drawText("${s.startedAt.take(10)} · ${s.distanceM} m", 64f, 218f, p)
        c.save()
        c.translate(90f, 260f)
        TargetRenderer.draw(c, 900f, s.target, d.hits(s.id), numbers = false)
        c.restore()
        val h = d.hits(s.id)
        p.color = Color.WHITE
        p.textSize = 34f
        c.drawText(
            "${h.sumOf { it.score }} pts     ${h.size} flèches / arrows     ${h.count { it.isX }} X",
            64f,
            1240f,
            p,
        )
        p.textSize = 23f
        p.color = 0xffaebdca.toInt()
        c.drawText("${s.target.name.substringBefore('|').take(65)}", 64f, 1295f, p)
        return ByteArrayOutputStream().use { out ->
            image.compress(Bitmap.CompressFormat.PNG, 100, out)
            image.recycle()
            out.toByteArray()
        }
    }

    fun share(context: Context, bytes: ByteArray, name: String, mime: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        dir.listFiles()
            ?.filter { System.currentTimeMillis() - it.lastModified() > 86400000 }
            ?.forEach { it.delete() }
        val file = File(dir, name)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent =
            Intent(Intent.ACTION_SEND)
                .setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Mire"))
    }
}
