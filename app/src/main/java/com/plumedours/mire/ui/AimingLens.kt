package com.plumedours.mire.ui

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.plumedours.mire.domain.*
import kotlin.math.roundToInt

/**
 * A non-focusable overlay keeps the lens visible above scroll containers without layout padding.
 */
@Composable
internal fun AimingLens(
    layer: Bitmap,
    viewport: BoardViewport,
    cross: Point,
    lens: LensGeometry,
    label: String,
) {
    val density = LocalDensity.current
    val margin = with(density) { 4.dp.toPx() }
    val badge = with(density) { 22.dp.toPx() }
    val radius = lens.radius.toFloat()
    val center = radius + margin
    val position =
        remember(lens, margin) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset =
                    IntOffset(
                        (anchorBounds.left + lens.center.x - radius - margin).roundToInt(),
                        (anchorBounds.top + lens.center.y - radius - margin).roundToInt(),
                    )
            }
        }
    val pos = viewport.toBoard(cross.x, cross.y)
    Popup(
        popupPositionProvider = position,
        properties =
            PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false,
            ),
    ) {
        Canvas(
            Modifier.size(
                with(density) { (2 * center).toDp() },
                with(density) { (2 * center + badge).toDp() },
            )
        ) {
            drawIntoCanvas { wrapped ->
                val c = wrapped.nativeCanvas
                val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                c.save()
                c.clipPath(Path().apply { addCircle(center, center, radius, Path.Direction.CW) })
                c.drawColor(0xfff6f8fc.toInt())
                c.translate(center, center)
                c.scale((viewport.zoom * 2.4).toFloat(), (viewport.zoom * 2.4).toFloat())
                c.translate((-pos.x * viewport.side).toFloat(), (-pos.y * viewport.side).toFloat())
                c.drawBitmap(
                    layer,
                    null,
                    RectF(0f, 0f, viewport.side.toFloat(), viewport.side.toFloat()),
                    p,
                )
                c.restore()
                p.style = Paint.Style.STROKE
                p.strokeWidth = 3.dp.toPx()
                p.color = 0xff65d8ff.toInt()
                c.drawCircle(center, center, radius, p)
                val arm = 7.dp.toPx()
                p.strokeWidth = 3.dp.toPx()
                p.color = 0xffffffff.toInt()
                c.drawLine(center - arm, center, center + arm, center, p)
                c.drawLine(center, center - arm, center, center + arm, p)
                p.strokeWidth = 1.dp.toPx()
                p.color = 0xff13203a.toInt()
                c.drawLine(center - arm, center, center + arm, center, p)
                c.drawLine(center, center - arm, center, center + arm, p)
                p.style = Paint.Style.FILL
                p.color = 0xff17284a.toInt()
                val half = 18.dp.toPx()
                val baseline = center + radius + 9.dp.toPx()
                c.drawRoundRect(
                    center - half,
                    center + radius - 4.dp.toPx(),
                    center + half,
                    baseline + 5.dp.toPx(),
                    8.dp.toPx(),
                    8.dp.toPx(),
                    p,
                )
                p.textAlign = Paint.Align.CENTER
                p.textSize = 14.dp.toPx()
                p.typeface = android.graphics.Typeface.DEFAULT_BOLD
                p.color = 0xfff4f8ff.toInt()
                c.drawText(label, center, baseline, p)
            }
        }
    }
}
