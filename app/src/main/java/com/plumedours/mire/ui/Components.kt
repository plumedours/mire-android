@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

val LocalEnglish = staticCompositionLocalOf { false }

@Composable fun tr(fr: String, en: String) = if (LocalEnglish.current) en else fr

fun decimal(value: Double, digits: Int = 1) =
    String.format(Locale.getDefault(), "%.${digits}f", value)

fun dateLabel(value: String) =
    runCatching {
            Instant.parse(value)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm"))
        }
        .getOrDefault(value.take(16))

fun numeric(s: String) = s.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

@Composable
fun Glyph(name: String, modifier: Modifier = Modifier.size(24.dp)) {
    val color = LocalContentColor.current
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * .075f)
        fun p(x: Float, y: Float) = Offset(w * x, h * y)
        fun line(a: Float, b: Float, c: Float, d: Float) =
            drawLine(color, p(a, b), p(c, d), w * .075f)
        when (name) {
            "home" -> {
                line(.12f, .45f, .5f, .12f)
                line(.5f, .12f, .88f, .45f)
                line(.22f, .4f, .22f, .87f)
                line(.22f, .87f, .78f, .87f)
                line(.78f, .87f, .78f, .4f)
                line(.45f, .87f, .45f, .6f)
            }
            "target" -> {
                drawCircle(color, w * .39f, style = stroke)
                drawCircle(color, w * .23f, style = stroke)
                drawCircle(color, w * .065f)
            }
            "chart" -> {
                line(.17f, .15f, .17f, .83f)
                line(.17f, .83f, .88f, .83f)
                line(.3f, .67f, .48f, .45f)
                line(.48f, .45f, .64f, .55f)
                line(.64f, .55f, .88f, .2f)
            }
            "equipment" -> {
                drawRoundRect(
                    color,
                    p(.17f, .32f),
                    androidx.compose.ui.geometry.Size(w * .66f, h * .53f),
                    CornerRadius(w * .07f),
                    style = stroke,
                )
                line(.35f, .3f, .35f, .15f)
                line(.35f, .15f, .65f, .15f)
                line(.65f, .15f, .65f, .3f)
                line(.17f, .53f, .83f, .53f)
            }
            "settings" -> {
                drawCircle(color, w * .18f, style = stroke)
                drawCircle(color, w * .36f, style = stroke)
                for (i in 0..7) {
                    val a = i * Math.PI / 4
                    drawLine(
                        color,
                        p(
                            .5f + (.36 * kotlin.math.cos(a)).toFloat(),
                            .5f + (.36 * kotlin.math.sin(a)).toFloat(),
                        ),
                        p(
                            .5f + (.47 * kotlin.math.cos(a)).toFloat(),
                            .5f + (.47 * kotlin.math.sin(a)).toFloat(),
                        ),
                        w * .075f,
                    )
                }
            }
            "back" -> {
                line(.7f, .15f, .3f, .5f)
                line(.3f, .5f, .7f, .85f)
            }
            "plus" -> {
                line(.5f, .18f, .5f, .82f)
                line(.18f, .5f, .82f, .5f)
            }
            "more" -> {
                listOf(.2f, .5f, .8f).forEach { drawCircle(color, w * .055f, p(.5f, it)) }
            }
            "check" -> {
                line(.15f, .5f, .4f, .76f)
                line(.4f, .76f, .86f, .22f)
            }
            "download" -> {
                line(.5f, .12f, .5f, .65f)
                line(.28f, .45f, .5f, .67f)
                line(.5f, .67f, .72f, .45f)
                line(.15f, .75f, .15f, .9f)
                line(.15f, .9f, .85f, .9f)
                line(.85f, .9f, .85f, .75f)
            }
            else -> {
                drawCircle(color, w * .35f, style = stroke)
                drawCircle(color, w * .06f)
            }
        }
    }
}

@Composable
fun Page(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = .28f),
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = 1200.dp)
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.width(4.dp)
                    .height(22.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (subtitle != null)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f),
                RoundedCornerShape(24.dp),
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Panel {
        Glyph("target", Modifier.size(44.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) Button(onClick = onAction) { Text(action) }
    }
}

@Composable
fun Stats(vararg metrics: Pair<String, String>) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for ((index, metric) in metrics.withIndex()) {
            val (value, label) = metric
            val accent =
                listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    Color(0xFF49D4B0),
                )[index % 4]
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = lerp(MaterialTheme.colorScheme.surface, accent, .13f),
                modifier = Modifier.widthIn(min = 130.dp).weight(1f),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        value,
                        color = accent,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun Input(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    number: Boolean = false,
    multiline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value,
        onValue,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions =
            KeyboardOptions(keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text),
    )
}

@Composable
fun <T> Choice(
    value: T,
    options: List<T>,
    label: String,
    text: @Composable (T) -> String = { it.toString() },
    onChange: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text(value), modifier = Modifier.weight(1f))
                Text("▾")
            }
            DropdownMenu(
                expanded,
                { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text(option)) },
                        onClick = {
                            expanded = false
                            onChange(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun Toggle(title: String, value: Boolean, onValue: (Boolean) -> Unit, description: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (description != null)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
        Switch(value, onValue)
    }
}

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveText: String? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            Modifier.widthIn(max = 640.dp).fillMaxWidth(.94f).fillMaxHeight(.92f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(tr("Annuler", "Cancel")) }
                    Button(onClick = onSave, enabled = enabled) {
                        Text(saveText ?: tr("Enregistrer", "Save"))
                    }
                }
            }
        }
    }
}

@Composable
fun Confirm(title: String, text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(tr("Confirmer", "Confirm")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Annuler", "Cancel")) } },
    )
}
