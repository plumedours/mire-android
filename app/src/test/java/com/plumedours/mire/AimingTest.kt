package com.plumedours.mire

import com.plumedours.mire.domain.*
import org.junit.Assert.*
import org.junit.Test

class AimingTest {
    @Test
    fun crosshairIsAboveFingerAndStopsAtEveryEdge() {
        val v = BoardViewport(600.0)
        assertEquals(Point(300.0, 264.0), v.aim(300.0, 300.0, 36.0, 12.0))
        assertEquals(Point(12.0, 12.0), v.aim(-200.0, -200.0, 36.0, 12.0))
        assertEquals(Point(588.0, 588.0), v.aim(900.0, 900.0, 36.0, 12.0))
        assertEquals(Point(300.0, 12.0), v.aim(300.0, 10.0, 36.0, 12.0))
    }

    @Test
    fun liftStaysConstantOnScreenAfterZoomAndPan() {
        val v = BoardViewport(600.0, 3.0, 40.0, -25.0)
        val finger = Point(320.0, 360.0)
        val cross = v.aim(finger.x, finger.y, 36.0, 12.0)
        val board = v.toBoard(cross.x, cross.y)
        val screen = v.toScreen(board.x, board.y)
        assertEquals(finger.x, screen.x, 1e-9)
        assertEquals(finger.y - 36.0, screen.y, 1e-9)
    }

    @Test
    fun lensFollowsContinuouslyAboveCrosshairAcrossCentreAndEdges() {
        val v = BoardViewport(600.0)
        var previous: Point? = null
        for (y in -50..700) {
            val cross = v.aim(300.0, y.toDouble(), 36.0, 12.0)
            val lens = v.magnifier(cross.x, cross.y, 60.0, 20.0)
            assertEquals(80.0, cross.y - lens.y, 1e-9)
            assertTrue(lens.y + 60.0 < cross.y)
            assertTrue(lens.y - 60.0 >= -136.0)
            previous?.let { assertTrue(kotlin.math.abs(lens.y - it.y) <= 1.0000001) }
            previous = lens
        }
    }

    @Test
    fun savedPositionAndScoreUseVisibleCrosshairOnSingleAndMultiSpotTargets() {
        for (t in Targets.all.filter { !it.animalTarget && it.canPlace }) {
            val view = BoardViewport(600.0, 2.0, 20.0, -10.0)
            val cross = view.aim(325.0, 355.0, 36.0, 12.0)
            val board = view.toBoard(cross.x, cross.y)
            val saved = TargetEngine.locate(t, board.x, board.y)
            val restored =
                TargetEngine.onBoard(
                    t,
                    Impact(
                        sessionId = "s",
                        end = 1,
                        number = 1,
                        u = saved.u,
                        v = saved.v,
                        spot = saved.spot,
                    ),
                )!!
            val screen = view.toScreen(restored.x, restored.y)
            assertEquals(cross.x, screen.x, 1e-9)
            assertEquals(cross.y, screen.y, 1e-9)
            assertEquals(
                TargetEngine.score(t, saved.u, saved.v),
                TargetEngine.score(
                    t,
                    TargetEngine.locate(t, restored.x, restored.y).u,
                    TargetEngine.locate(t, restored.x, restored.y).v,
                ),
            )
        }
    }

    @Test
    fun floatingLensFitsAboveCrosshairWithoutReservedLayoutSpace() {
        val v = BoardViewport(600.0)
        for (top in listOf(95.0, 150.0, 300.0)) for (y in 12..588) {
            val lens = v.lens(300.0, y.toDouble(), 54.0, 24.0, top, 28.0)
            assertTrue(lens.radius <= 54.0)
            assertTrue(top + lens.center.y - lens.radius >= 28.0 - 1e-9)
            assertEquals(y.toDouble() - 24.0, lens.center.y + lens.radius, 1e-9)
        }
    }

    @Test
    fun floatingLensPositionAndSizeStayContinuousAtWindowAndCanvasEdges() {
        val v = BoardViewport(600.0)
        var previous: LensGeometry? = null
        for (step in -100..700) {
            val cross = v.aim(step.toDouble(), step.toDouble(), 36.0, 12.0)
            val lens = v.lens(cross.x, cross.y, 54.0, 24.0, 95.0, 28.0)
            previous?.let {
                assertTrue(kotlin.math.abs(lens.center.x - it.center.x) <= 1.0000001)
                assertTrue(kotlin.math.abs(lens.center.y - it.center.y) <= 1.0000001)
                assertTrue(kotlin.math.abs(lens.radius - it.radius) <= .5000001)
            }
            previous = lens
        }
    }
}
