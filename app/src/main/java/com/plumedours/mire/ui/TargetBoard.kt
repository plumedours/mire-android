package com.plumedours.mire.ui

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.plumedours.mire.domain.*
import kotlin.math.*

/** One gesture recogniser: drag-to-place, release-to-save, two-finger pan/zoom. */
@Composable
fun TargetBoard(
    target: TargetSpec,
    impacts: List<Impact>,
    modifier: Modifier = Modifier,
    heat: Boolean = false,
    overlay: Boolean = false,
    numbers: Boolean = true,
    editable: Boolean = false,
    selected: String? = null,
    preferredSpot: Int? = null,
    arrowDiameterMm: Double = 0.0,
    arrowNumber: Int = 1,
    onShot: (LocatedPoint) -> Unit = {},
    onMove: (String, LocatedPoint) -> Unit = { _, _ -> },
) {
    var zoom by remember(target.id) { mutableFloatStateOf(1f) }
    var pan by remember(target.id) { mutableStateOf(Offset.Zero) }
    var side by remember { mutableFloatStateOf(1f) }
    var boardTop by remember { mutableFloatStateOf(0f) }
    var cursor by remember { mutableStateOf<Offset?>(null) }
    val density = LocalDensity.current
    val aimLift = with(density) { 36.dp.toPx() }
    val aimInset = with(density) { 12.dp.toPx() }
    val shot by rememberUpdatedState(onShot)
    val move by rememberUpdatedState(onMove)
    val selection by rememberUpdatedState(selected)
    val preferred by rememberUpdatedState(preferredSpot)
    fun viewport() =
        BoardViewport(side.toDouble(), zoom.toDouble(), pan.x.toDouble(), pan.y.toDouble())
    fun aim(p: Offset): Point =
        viewport().aim(p.x.toDouble(), p.y.toDouble(), aimLift.toDouble(), aimInset.toDouble())
    fun located(p: Offset): LocatedPoint {
        val cross = aim(p)
        val q = viewport().toBoard(cross.x, cross.y)
        return TargetEngine.locate(target, q.x, q.y, preferred)
    }
    // Rasterise only when data changes. Finger motion reuses this layer in both the board and lens.
    val layer =
        remember(target, impacts, heat, overlay, numbers, selected) {
            Bitmap.createBitmap(1200, 1200, Bitmap.Config.ARGB_8888).also { bitmap ->
                val c = AndroidCanvas(bitmap)
                c.drawColor(0xfff6f8fc.toInt())
                TargetRenderer.draw(c, 1200f, target, impacts, heat, overlay, numbers, selected)
            }
        }

    val description =
        tr(
            "Cible ${target.name}. La croix vise au-dessus du doigt. Glisse puis relâche pour valider.",
            "Target ${target.name}. The crosshair aims above your finger. Drag then release to save.",
        )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp))
        ) {
            Canvas(
                Modifier.fillMaxSize()
                    .semantics { contentDescription = description }
                    .onSizeChanged { side = it.width.toFloat() }
                    .onGloballyPositioned { boardTop = it.boundsInWindow().top }
                    .pointerInput(target, editable, aimLift, aimInset) {
                        if (editable)
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                cursor = down.position
                                var multi = false
                                var cancelled = false
                                var last = down.position
                                try {
                                    do {
                                        val event = awaitPointerEvent()
                                        val pressed = event.changes.count { it.pressed }
                                        if (pressed >= 2) {
                                            multi = true
                                            cursor = null
                                            val centroid =
                                                event.calculateCentroid(useCurrent = false)
                                            val delta = event.calculatePan()
                                            val next =
                                                (zoom * event.calculateZoom()).coerceIn(1f, 5f)
                                            val ratio = next / zoom
                                            val center = Offset(side / 2, side / 2)
                                            pan =
                                                (pan - (centroid - center)) * ratio +
                                                    (centroid - center) +
                                                    delta
                                            zoom = next
                                            if (zoom == 1f) pan = Offset.Zero
                                        } else if (!multi) {
                                            event.changes
                                                .firstOrNull { it.id == down.id }
                                                ?.let {
                                                    last = it.position
                                                    cursor = last
                                                }
                                        }
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })
                                } catch (e: kotlinx.coroutines.CancellationException) {
                                    cancelled = true
                                    throw e
                                } finally {
                                    cursor = null
                                    if (!multi && !cancelled) {
                                        val point = located(last)
                                        if (selection != null) move(selection!!, point)
                                        else shot(point)
                                    }
                                }
                            }
                    }
            ) {
                drawIntoCanvas { wrapped ->
                    val c = wrapped.nativeCanvas
                    val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    val save = c.save()
                    c.clipPath(
                        Path().apply {
                            addRoundRect(
                                RectF(0f, 0f, side, side),
                                28.dp.toPx(),
                                28.dp.toPx(),
                                Path.Direction.CW,
                            )
                        }
                    )
                    c.translate(side / 2 + pan.x, side / 2 + pan.y)
                    c.scale(zoom, zoom)
                    c.translate(-side / 2, -side / 2)
                    c.drawBitmap(layer, null, RectF(0f, 0f, side, side), p)
                    c.restoreToCount(save)
                    cursor?.let { finger ->
                        val cross = aim(finger)
                        val cx = cross.x.toFloat()
                        val cy = cross.y.toFloat()
                        // The board crosshair, preview and saved arrow share the same lifted point.
                        val arm = 8.dp.toPx()
                        p.style = Paint.Style.STROKE
                        p.strokeWidth = 4.dp.toPx()
                        p.color = 0xfff9fcff.toInt()
                        c.drawLine(cx - arm, cy, cx + arm, cy, p)
                        c.drawLine(cx, cy - arm, cx, cy + arm, p)
                        p.strokeWidth = 2.dp.toPx()
                        p.color = 0xff5434dd.toInt()
                        c.drawLine(cx - arm, cy, cx + arm, cy, p)
                        c.drawLine(cx, cy - arm, cx, cy + arm, p)
                        p.style = Paint.Style.FILL
                    }
                }
            }
            cursor?.let { finger ->
                val cross = aim(finger)
                val safeTop = WindowInsets.safeDrawing.getTop(density).toDouble()
                val lens =
                    viewport()
                        .lens(
                            cross.x,
                            cross.y,
                            with(density) { 54.dp.toPx() }.toDouble(),
                            with(density) { 24.dp.toPx() }.toDouble(),
                            boardTop.toDouble(),
                            safeTop + with(density) { 4.dp.toPx() },
                        )
                val loc = located(finger)
                val score = TargetEngine.score(target, loc.u, loc.v, arrowDiameterMm, arrowNumber)
                val label =
                    if (score.isX) "X"
                    else if (target.rule == Rule.BALLOON && score.value > 0) "+1"
                    else score.value.toString()
                AimingLens(layer, viewport(), cross, lens, label)
            }
        }
        if (editable)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FilledTonalIconButton(
                    onClick = {
                        zoom = max(1f, zoom - .5f)
                        if (zoom == 1f) pan = Offset.Zero
                    }
                ) {
                    Text("−")
                }
                Spacer(Modifier.weight(1f))
                FilledTonalIconButton(onClick = { zoom = min(5f, zoom + .5f) }) { Text("+") }
                TextButton(
                    onClick = {
                        zoom = 1f
                        pan = Offset.Zero
                    }
                ) {
                    Text("${decimal(zoom.toDouble())}×")
                }
            }
    }
}
