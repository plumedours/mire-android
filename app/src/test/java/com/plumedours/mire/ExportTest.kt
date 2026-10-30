package com.plumedours.mire

import com.plumedours.mire.domain.*
import com.plumedours.mire.export.Exports
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class ExportTest {
    private val s =
        Session(
            id = "s",
            name = "Été & <cible>",
            target = Targets.all[0],
            startedAt = "2026-10-16T12:00:00Z",
        )
    private val d =
        AppData(
            sessions = listOf(s),
            impacts =
                listOf(
                    Impact(
                        sessionId = "s",
                        end = 2,
                        number = 1,
                        score = 9,
                        label = "=danger; \"test\"",
                    )
                ),
        )

    @Test
    fun csvUsesUtf8AndEscapes() {
        val text = String(Exports.csv(d), Charsets.UTF_8)
        assertTrue(text.startsWith("\uFEFF"))
        assertTrue(text.contains("Été"))
        assertTrue(text.contains("'=danger; \"\"test\"\""))
        assertTrue(text.contains("\r\n"))
    }

    @Test
    fun workbookXmlIsValidAndIncludesAllArrows() {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(Exports.xlsx(d))).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                entries[e.name] = z.readBytes()
            }
        }
        assertTrue(
            entries.keys.containsAll(
                listOf(
                    "[Content_Types].xml",
                    "_rels/.rels",
                    "xl/workbook.xml",
                    "xl/worksheets/sheet1.xml",
                    "xl/worksheets/sheet2.xml",
                )
            )
        )
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        for ((key, bytes) in entries) if (key.endsWith(".xml") || key.endsWith(".rels"))
            builder.parse(ByteArrayInputStream(bytes))
        val sheet = String(entries["xl/worksheets/sheet2.xml"]!!)
        assertTrue(sheet.contains("=danger"))
        assertTrue(sheet.contains("&quot;test&quot;"))
        assertFalse(sheet.contains("<f>"))
    }
}
