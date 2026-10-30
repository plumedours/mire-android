package com.plumedours.mire

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.plumedours.mire.domain.*
import com.plumedours.mire.ui.TargetBoard
import com.plumedours.mire.ui.theme.MireTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TargetGestureTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun targetStartsAtTopOfBoardWithoutReservedLensSpace() {
        compose.setContent {
            MireTheme {
                TargetBoard(
                    Targets.all.first(),
                    emptyList(),
                    Modifier.width(300.dp).testTag("board"),
                    editable = true,
                )
            }
        }
        val host = compose.onNodeWithTag("board").fetchSemanticsNode().boundsInRoot
        val target =
            compose
                .onNode(hasContentDescription("Cible", substring = true))
                .fetchSemanticsNode()
                .boundsInRoot
        assertEquals(host.top, target.top, .5f)
        compose.onNodeWithText("La loupe suit la croix au-dessus du doigt").assertDoesNotExist()
    }

    @Test
    fun immediateDragCommitsExactlyOnceOnRelease() {
        val hits = mutableListOf<LocatedPoint>()
        compose.setContent {
            MireTheme {
                TargetBoard(
                    Targets.all.first(),
                    emptyList(),
                    Modifier.width(300.dp),
                    editable = true,
                    onShot = { hits.add(it) },
                )
            }
        }
        compose.onNode(hasContentDescription("Cible", substring = true)).performTouchInput {
            down(center)
            moveTo(center + Offset(15f, 20f))
            up()
        }
        compose.runOnIdle {
            assertEquals(1, hits.size)
            assertTrue(hits.single().u > 0)
            assertTrue(hits.single().v < 0)
        }
    }

    @Test
    fun draggingBeyondBoardCommitsClampedCrosshairOnRelease() {
        val hits = mutableListOf<LocatedPoint>()
        compose.setContent {
            MireTheme {
                TargetBoard(
                    Targets.all.first(),
                    emptyList(),
                    Modifier.width(300.dp),
                    editable = true,
                    onShot = { hits.add(it) },
                )
            }
        }
        val board = compose.onNode(hasContentDescription("Cible", substring = true))
        board.performTouchInput {
            down(center)
            moveTo(Offset(-100f, -100f))
            up()
        }
        compose.runOnIdle {
            assertEquals(1, hits.size)
            val p =
                TargetEngine.onBoard(
                    Targets.all.first(),
                    Impact(
                        sessionId = "s",
                        end = 1,
                        number = 1,
                        u = hits.single().u,
                        v = hits.single().v,
                        spot = hits.single().spot,
                    ),
                )!!
            assertEquals(.04, p.x, 1e-5)
            assertEquals(.04, p.y, 1e-5)
        }
    }

    @Test
    fun twoFingerZoomDoesNotAddAnArrow() {
        val hits = mutableListOf<LocatedPoint>()
        compose.setContent {
            MireTheme {
                TargetBoard(
                    Targets.all.first(),
                    emptyList(),
                    Modifier.width(300.dp),
                    editable = true,
                    onShot = { hits.add(it) },
                )
            }
        }
        compose.onNode(hasContentDescription("Cible", substring = true)).performTouchInput {
            down(0, center - Offset(30f, 0f))
            down(1, center + Offset(30f, 0f))
            moveTo(0, center - Offset(60f, 0f))
            moveTo(1, center + Offset(60f, 0f))
            up(0)
            up(1)
        }
        compose.runOnIdle { assertTrue(hits.isEmpty()) }
    }
}
