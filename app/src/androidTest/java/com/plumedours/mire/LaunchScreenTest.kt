package com.plumedours.mire

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.plumedours.mire.ui.BrandSplash
import com.plumedours.mire.ui.theme.MireTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LaunchScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun introRemainsVisibleForMoreThanTwoSecondsAndCompletesOnce() {
        var finished = 0
        compose.mainClock.autoAdvance = false
        compose.setContent { MireTheme { BrandSplash { finished++ } } }
        compose.mainClock.advanceTimeBy(2100)
        compose.onNodeWithText("MIRE").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, finished) }
        compose.mainClock.advanceTimeBy(350)
        compose.runOnIdle { assertEquals(1, finished) }
        compose.mainClock.advanceTimeBy(3000)
        compose.runOnIdle { assertEquals(1, finished) }
    }
}
