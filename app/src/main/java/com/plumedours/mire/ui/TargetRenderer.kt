package com.plumedours.mire.ui

import android.graphics.*
import com.plumedours.mire.domain.*
import com.plumedours.mire.domain.Point
import kotlin.math.*

/** Shared renderer used by the live board, heatmap, PNG card and PDF exports. */
object TargetRenderer {
    private val colors =
        listOf(
            0xfff8fbf7.toInt(),
            0xff16171d.toInt(),
            0xff00a8d7.toInt(),
            0xffed1530.toInt(),
            0xffffd600.toInt(),
        )

    fun draw(
        canvas: Canvas,
        side: Float,
        target: TargetSpec,
        impacts: List<Impact>,
        heat: Boolean = false,
        overlay: Boolean = false,
        numbers: Boolean = true,
        selected: String? = null,
    ) {
        val save = canvas.save()
        canvas.scale(side / 1000f, side / 1000f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val geometries =
            if (overlay) listOf(SpotGeometry(Point(.5, .5), .46 / target.visibleRadius))
            else TargetEngine.geometry(target)
        for (g in geometries) {
            canvas.save()
            canvas.translate((g.center.x * 1000).toFloat(), (g.center.y * 1000).toFloat())
            val unit = (g.scale * 1000).toFloat()
            paint.style = Paint.Style.FILL
            when (target.rule) {
                Rule.BALLOON -> {
                    paint.style = Paint.Style.FILL
                    paint.color = 0xff183852.toInt()
                    canvas.drawRect(-500f, -500f, 500f, 500f, paint)
                    // Deliberately small relative to its board; its physical diameter is a session
                    // setting.
                    val disk = unit
                    paint.color = 0xffffcf55.toInt()
                    canvas.drawCircle(0f, 0f, disk, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f
                    paint.color = 0xffffeeb5.toInt()
                    canvas.drawCircle(0f, 0f, disk, paint)
                }
                Rule.BLANK -> {
                    paint.color = 0xffd5b582.toInt()
                    canvas.drawCircle(0f, 0f, unit, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = 0xffb2905d.toInt()
                    paint.strokeWidth = 2f
                    for (r in 1..15) canvas.drawCircle(0f, 0f, unit * r / 15, paint)
                }
                Rule.THREE_D_11,
                Rule.THREE_D_12,
                Rule.THREE_D_X,
                Rule.NATURE -> drawAnimal(canvas, target, unit, paint)
                Rule.BEURSAULT -> {
                    paint.color = Color.WHITE
                    canvas.drawCircle(0f, 0f, unit, paint)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = unit * .045f
                    paint.color = 0xff171923.toInt()
                    canvas.drawCircle(0f, 0f, unit * .975f, paint)
                    for (r in listOf(.8f, .55f, .28f, .13f)) {
                        paint.strokeWidth = if (r == .28f) unit * .024f else 2.5f
                        canvas.drawCircle(0f, 0f, unit * r, paint)
                    }
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(0f, 0f, unit * .085f, paint)
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.textSize = unit * .085f
                    for ((radius, label) in listOf(.88f to "1", .66f to "2", .20f to "3")) canvas
                        .drawText(label, radius * unit, unit * .035f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = unit * .07f
                    canvas.drawText("4", 0f, unit * .025f, paint)
                }
                Rule.FIELD -> {
                    for ((i, r) in listOf(1f, .8f, .6f, .4f, .2f, .1f).withIndex()) {
                        paint.style = Paint.Style.FILL
                        paint.color = if (i >= 4) 0xffffd500.toInt() else 0xff14151a.toInt()
                        canvas.drawCircle(0f, 0f, unit * r, paint)
                        paint.style = Paint.Style.STROKE
                        paint.color = if (i >= 4) 0xff171923.toInt() else 0xffebedf0.toInt()
                        paint.strokeWidth = 1.6f
                        canvas.drawCircle(0f, 0f, unit * r, paint)
                    }
                }
                Rule.WORCESTER -> {
                    for (r in 5 downTo 1) {
                        paint.color = if (r == 1) Color.WHITE else 0xff171923.toInt()
                        paint.style = Paint.Style.FILL
                        canvas.drawCircle(0f, 0f, unit * r / 5, paint)
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f
                        paint.color = Color.LTGRAY
                        canvas.drawCircle(0f, 0f, unit * r / 5, paint)
                    }
                }
                else -> {
                    val outer = (target.visibleRadius * 10).roundToInt()
                    for (r in outer downTo 1) {
                        paint.style = Paint.Style.FILL
                        paint.color = colors[(10 - r) / 2]
                        canvas.drawCircle(0f, 0f, unit * r / 10, paint)
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1.5f
                        paint.color = if (r in 7..8) 0xffe3e8ef.toInt() else 0xff1b2434.toInt()
                        if (!(r == 1 && target.centreStyle == "compound"))
                            canvas.drawCircle(0f, 0f, unit * r / 10, paint)
                    }
                    if (target.centreStyle != "recurve")
                        canvas.drawCircle(0f, 0f, unit * .05f, paint)
                }
            }
            if (target.positionalAnalysis && target.rule != Rule.BALLOON) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.8f
                paint.color = 0xff263347.toInt()
                canvas.drawLine(-5f, 0f, 5f, 0f, paint)
                canvas.drawLine(0f, -5f, 0f, 5f, paint)
            }
            canvas.restore()
        }
        val points =
            impacts.mapNotNull { i ->
                val p =
                    if (overlay && i.u != null && i.v != null && !i.uncertain && i.spot != null) {
                        val g = geometries[0]
                        Point(g.center.x + i.u * g.scale, g.center.y + i.v * g.scale)
                    } else if (overlay) null else TargetEngine.onBoard(target, i)
                p?.let { Triple(i, (it.x * 1000).toFloat(), (it.y * 1000).toFloat()) }
            }
        if (heat) {
            // Bound rendering cost for very large histories; all points still enter numerical
            // statistics.
            val step = max(1, ceil(points.size / 2000.0).toInt())
            points
                .filterIndexed { index, _ -> index % step == 0 }
                .forEach { (i, x, y) ->
                    if (!i.uncertain) {
                        paint.style = Paint.Style.FILL
                        paint.shader =
                            RadialGradient(
                                x,
                                y,
                                50f,
                                intArrayOf(0xbbee3048.toInt(), 0x55ff9834, 0x00ff9834),
                                null,
                                Shader.TileMode.CLAMP,
                            )
                        canvas.drawCircle(x, y, 50f, paint)
                    }
                }
            paint.shader = null
        } else
            for ((i, x, y) in points) {
                paint.style = Paint.Style.FILL
                paint.color =
                    if (i.id == selected) 0xff65e4c4.toInt()
                    else if (i.uncertain) 0xffffa726.toInt()
                    else if (overlay && target.spotCount > 1)
                        listOf(0xffed3f80.toInt(), 0xff04a993.toInt(), 0xff7747d7.toInt())
                            .getOrElse(i.spot ?: 0) { 0xffed3f80.toInt() }
                    else 0xffae44f2.toInt()
                canvas.drawCircle(x, y, if (i.id == selected) 15f else 10f, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                paint.strokeWidth = 2.5f
                canvas.drawCircle(x, y, if (i.id == selected) 15f else 10f, paint)
                if (numbers && points.size <= 60) {
                    paint.style = Paint.Style.FILL
                    paint.color = 0xdd17284a.toInt()
                    paint.textSize = 34f
                    paint.typeface = Typeface.DEFAULT_BOLD
                    val label = i.number.toString()
                    val labelWidth = paint.measureText(label)
                    canvas.drawRoundRect(x + 13, y - 44, x + labelWidth + 27, y - 3, 8f, 8f, paint)
                    paint.color = Color.WHITE
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(label, x + 20, y - 12, paint)
                }
            }
        if (heat) {
            val a = TargetEngine.analyze(target, impacts)
            if (a != null) {
                val g = if (overlay || target.spotCount == 1) geometries[0] else null
                if (g != null) {
                    val x = ((g.center.x + a.meanU * g.scale) * 1000).toFloat()
                    val y = ((g.center.y + a.meanV * g.scale) * 1000).toFloat()
                    paint.shader = null
                    paint.color = Color.WHITE
                    paint.strokeWidth = 5f
                    canvas.drawLine(x - 14, y, x + 14, y, paint)
                    canvas.drawLine(x, y - 14, x, y + 14, paint)
                    paint.color = Color.BLACK
                    paint.strokeWidth = 2f
                    canvas.drawLine(x - 14, y, x + 14, y, paint)
                    canvas.drawLine(x, y - 14, x, y + 14, paint)
                }
            }
        }
        canvas.restoreToCount(save)
    }

    private fun drawAnimal(canvas: Canvas, t: TargetSpec, unit: Float, p: Paint) {
        val zones =
            if (t.rule == Rule.NATURE) listOf(AnimalGeometry.outer, AnimalGeometry.kill())
            else
                listOf(
                    AnimalGeometry.outer,
                    AnimalGeometry.kill(),
                    AnimalGeometry.vital(),
                    AnimalGeometry.inner(),
                )
        val fills =
            if (t.rule == Rule.NATURE) listOf(0xffdce5eb.toInt(), 0xffb7cddd.toInt())
            else
                listOf(
                    0xffe4ebf0.toInt(),
                    0xffcedde7.toInt(),
                    0xffafc7d9.toInt(),
                    0xff8baec8.toInt(),
                )
        for ((i, e) in zones.withIndex()) {
            p.style = Paint.Style.FILL
            p.color = fills[i]
            val rect =
                RectF(
                    ((e.x - e.rx) * unit).toFloat(),
                    ((e.y - e.ry) * unit).toFloat(),
                    ((e.x + e.rx) * unit).toFloat(),
                    ((e.y + e.ry) * unit).toFloat(),
                )
            canvas.drawOval(rect, p)
            p.style = Paint.Style.STROKE
            p.strokeWidth = if (i == 0) 3f else 2.5f
            p.color = 0xff314b61.toInt()
            canvas.drawOval(rect, p)
        }
        p.style = Paint.Style.FILL
        p.color = 0xff213b52.toInt()
        p.typeface = Typeface.DEFAULT_BOLD
        p.textAlign = Paint.Align.CENTER
        p.textSize = unit * .075f
        if (t.rule == Rule.NATURE) {
            canvas.drawText("20 / 15", 0f, unit * .025f, p)
            canvas.drawText("15 / 10", unit * .77f, unit * .025f, p)
        } else {
            canvas.drawText(
                if (t.rule == Rule.THREE_D_12) "12"
                else if (t.rule == Rule.THREE_D_X) "10+" else "11",
                0f,
                unit * .025f,
                p,
            )
            canvas.drawText("10", unit * .235f, unit * .025f, p)
            canvas.drawText("8", unit * .48f, unit * .025f, p)
            canvas.drawText("5", unit * .78f, unit * .025f, p)
        }
    }
}
