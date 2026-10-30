package com.plumedours.mire

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.plumedours.mire.domain.*
import com.plumedours.mire.ui.SessionCard
import com.plumedours.mire.ui.theme.MireTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SessionActionsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun deletingRequiresConfirmationAndDoesNotOpenTheSession() {
        val session = Session(name = "Séance test", target = Targets.all.first())
        var deleted = 0
        var opened = 0
        compose.setContent {
            MireTheme {
                SessionCard(AppData(sessions = listOf(session)), session, { deleted++ }, {}) {
                    opened++
                }
            }
        }
        compose.onNodeWithContentDescription("Supprimer la séance Séance test").performClick()
        compose.onNodeWithText("Supprimer cette séance ?").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, deleted)
            assertEquals(0, opened)
        }
        compose.onNodeWithText("Annuler").performClick()
        compose.runOnIdle { assertEquals(0, deleted) }
        compose.onNodeWithContentDescription("Supprimer la séance Séance test").performClick()
        compose.onNodeWithText("Supprimer", useUnmergedTree = true).performClick()
        compose.runOnIdle {
            assertEquals(1, deleted)
            assertEquals(0, opened)
        }
    }

    @Test
    fun reopeningUsesDedicatedActionWithoutDeletionOrOpeningSummary() {
        val session = Session(name = "Terminée", target = Targets.all.first(), completed = true)
        var reopened = 0
        var deleted = 0
        var opened = 0
        compose.setContent {
            MireTheme {
                SessionCard(
                    AppData(sessions = listOf(session)),
                    session,
                    { deleted++ },
                    { reopened++ },
                ) {
                    opened++
                }
            }
        }
        compose.onNodeWithText("Rouvrir").performClick()
        compose.runOnIdle {
            assertEquals(1, reopened)
            assertEquals(0, deleted)
            assertEquals(0, opened)
        }
    }
}
