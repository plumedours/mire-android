@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.plumedours.mire.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plumedours.mire.domain.*

object ScoreColours {
    fun background(t: TargetSpec, score: Int): Color =
        when {
            t.rule == Rule.BALLOON -> if (score > 0) Color(0xFF25BB8E) else Color(0xFFBF4260)
            score == 0 -> Color(0xFF334665)
            t.rule == Rule.FIELD -> if (score >= 5) Color(0xFFFFD529) else Color(0xFF171A21)
            t.rule == Rule.WORCESTER -> if (score == 5) Color(0xFFF7F8F2) else Color(0xFF171A21)
            t.animalTarget ->
                when {
                    score >= 11 -> Color(0xFFFFCB60)
                    score == 10 -> Color(0xFFFC9C59)
                    score == 8 -> Color(0xFF68BBDD)
                    else -> Color(0xFF738EAB)
                }
            t.rule == Rule.BEURSAULT ->
                when (score) {
                    4 -> Color(0xFF191E29)
                    3 -> Color(0xFFDF6571)
                    2 -> Color(0xFF49C1EA)
                    else -> Color(0xFFF4F7FC)
                }
            score >= 9 -> Color(0xFFFFD529)
            score >= 7 -> Color(0xFFEE2344)
            score >= 5 -> Color(0xFF17B0DF)
            score >= 3 -> Color(0xFF171A21)
            else -> Color(0xFFF6F8F3)
        }

    fun foreground(t: TargetSpec, s: Int): Color {
        val c = background(t, s)
        val l = .2126 * c.red + .7152 * c.green + .0722 * c.blue
        return if (l > .58) Color(0xFF101E37) else Color.White
    }
}

@Composable
fun ScorePad(
    target: TargetSpec,
    nextArrow: Int,
    enabled: Boolean,
    onScore: (Int, Boolean) -> Unit,
) {
    val scores = TargetEngine.allowedScores(target, nextArrow)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (target.rule == Rule.NATURE)
            Text(
                tr(
                    "Flèche $nextArrow · ${if(nextArrow%2==1) "tuée 20 / blessée 15" else "tuée 15 / blessée 10"}",
                    "Arrow $nextArrow · ${if(nextArrow%2==1) "kill 20 / wound 15" else "kill 15 / wound 10"}",
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val xAllowed =
                (target.rule in listOf(Rule.TEN, Rule.INNER_TEN) &&
                    target.centreStyle != "recurve") || target.rule == Rule.THREE_D_X
            val values =
                if (xAllowed) listOf(10 to true) + scores.map { it to false }
                else scores.map { it to false }
            for ((score, x) in values) Button(
                onClick = { onScore(score, x) },
                enabled = enabled,
                modifier =
                    if (target.rule == Rule.BALLOON)
                        Modifier.widthIn(min = 126.dp).weight(1f).height(66.dp)
                    else Modifier.widthIn(min = 62.dp).height(60.dp),
                shape = RoundedCornerShape(17.dp),
                border =
                    BorderStroke(
                        1.dp,
                        if (score in 1..2) Color(0xFFA9B9D4) else Color.White.copy(.16f),
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = ScoreColours.background(target, score),
                        contentColor = ScoreColours.foreground(target, score),
                        disabledContainerColor = ScoreColours.background(target, score).copy(.35f),
                        disabledContentColor = ScoreColours.foreground(target, score).copy(.5f),
                    ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    if (target.rule == Rule.BALLOON) {
                        if (score > 0) tr("✓ Touché", "✓ Hit") else tr("✕ Manqué", "✕ Miss")
                    } else if (x) if (target.rule == Rule.THREE_D_X) "10+" else "X"
                    else if (score == 0) "M" else score.toString(),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}
