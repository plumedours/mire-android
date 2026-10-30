package com.plumedours.mire.domain

/** Neutral vital-zone sketch. No animal is selected or drawn. */
object AnimalGeometry {
    data class Ellipse(val x: Double, val y: Double, val rx: Double, val ry: Double) {
        fun contains(x: Double, y: Double) =
            ((x - this.x) / rx) * ((x - this.x) / rx) + ((y - this.y) / ry) * ((y - this.y) / ry) <=
                1
    }

    fun kill(animal: Animal = Animal.BOAR) = Ellipse(0.0, 0.0, .62, .46)

    fun vital(animal: Animal = Animal.BOAR) = Ellipse(0.0, 0.0, .35, .26)

    fun inner(animal: Animal = Animal.BOAR) = Ellipse(0.0, 0.0, .14, .105)

    val outer = Ellipse(0.0, 0.0, .94, .76)

    // Kept solely to display older 1.1 coordinates in the new neutral vital frame.
    private fun legacyKill(animal: Animal) =
        when (animal) {
            Animal.BOAR -> Ellipse(-.22, -.005, .34, .30)
            Animal.DEER -> Ellipse(-.1, .005, .28, .255)
            Animal.FOX -> Ellipse(-.22, -.01, .245, .21)
        }

    fun toDisplay(target: TargetSpec, u: Double, v: Double): Point {
        if (target.zoneStyle != "animal-v1") return Point(u, v)
        val old = legacyKill(target.animal)
        val neutral = kill()
        return Point((u - old.x) * neutral.rx / old.rx, (v - old.y) * neutral.ry / old.ry)
    }

    fun fromDisplay(target: TargetSpec, u: Double, v: Double): Point {
        if (target.zoneStyle != "animal-v1") return Point(u, v)
        val old = legacyKill(target.animal)
        val neutral = kill()
        return Point(u * old.rx / neutral.rx + old.x, v * old.ry / neutral.ry + old.y)
    }

    fun score(target: TargetSpec, u: Double, v: Double, arrow: Int): ShotScore {
        val p = toDisplay(target, u, v)
        if (!outer.contains(p.x, p.y)) return ShotScore(0)
        if (target.rule == Rule.NATURE)
            return ShotScore(
                if (kill().contains(p.x, p.y)) {
                    if (arrow % 2 == 1) 20 else 15
                } else {
                    if (arrow % 2 == 1) 15 else 10
                }
            )
        if (inner().contains(p.x, p.y))
            return when (target.rule) {
                Rule.THREE_D_12 -> ShotScore(12)
                Rule.THREE_D_X -> ShotScore(10, true)
                else -> ShotScore(11)
            }
        if (vital().contains(p.x, p.y)) return ShotScore(10)
        if (kill().contains(p.x, p.y)) return ShotScore(8)
        return ShotScore(5)
    }
}

data class LensGeometry(val center: Point, val radius: Double)

/** Pure view transform, kept separate from the pointer recogniser and covered by tests. */
data class BoardViewport(
    val side: Double,
    val zoom: Double = 1.0,
    val panX: Double = 0.0,
    val panY: Double = 0.0,
) {
    fun toBoard(x: Double, y: Double) =
        Point(
            ((x - side / 2 - panX) / zoom + side / 2) / side,
            ((y - side / 2 - panY) / zoom + side / 2) / side,
        )

    fun toScreen(x: Double, y: Double) =
        Point(
            (x * side - side / 2) * zoom + side / 2 + panX,
            (y * side - side / 2) * zoom + side / 2 + panY,
        )

    /** Screen-space aiming offset stays constant regardless of target zoom. */
    fun aim(x: Double, y: Double, lift: Double, inset: Double): Point {
        val edge = inset.coerceIn(0.0, side / 2)
        return Point(x.coerceIn(edge, side - edge), (y - lift).coerceIn(edge, side - edge))
    }

    /** Continuous offset above the crosshair. Top overflow uses the reserved lens area. */
    fun magnifier(x: Double, y: Double, radius: Double, gap: Double): Point {
        val edge = (radius + gap).coerceAtMost(side / 2)
        return Point(x.coerceIn(edge, side - edge), y - radius - gap)
    }

    /** Fit above the crosshair and below system insets without switching sides. */
    fun lens(
        x: Double,
        y: Double,
        maxRadius: Double,
        gap: Double,
        topInWindow: Double,
        safeTop: Double,
    ): LensGeometry {
        val radius =
            kotlin.math.min(maxRadius, kotlin.math.max(1.0, (topInWindow + y - safeTop - gap) / 2))
        return LensGeometry(magnifier(x, y, radius, gap), radius)
    }
}
