package com.plumedours.mire.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plumedours.mire.R

/** A short entrance animation; database loading and error handling remain independent. */
@Composable
fun BrandSplash(animate: Boolean = true, onFinished: () -> Unit = {}) {
    val phase = remember { Animatable(if (animate) 0f else 1f) }
    val finished by rememberUpdatedState(onFinished)
    if (animate)
        LaunchedEffect(Unit) {
            phase.animateTo(1f, tween(2300, easing = LinearEasing))
            finished()
        }
    val p = phase.value
    val entrance = (p * 3.5f).coerceIn(0f, 1f)
    Box(Modifier.fillMaxSize().background(Color(0xFF0B1328)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.safeDrawingPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(Modifier.size(192.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val inset = 8.dp.toPx()
                    drawArc(
                        Color(0xFF69D7FF).copy(alpha = .24f * p),
                        -90f,
                        360f * p,
                        false,
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size =
                            androidx.compose.ui.geometry.Size(
                                size.width - 2 * inset,
                                size.height - 2 * inset,
                            ),
                        style = Stroke(1.5.dp.toPx()),
                    )
                }
                Image(
                    painterResource(R.drawable.mire_brand_flat),
                    contentDescription = null,
                    modifier =
                        Modifier.size(160.dp).graphicsLayer {
                            alpha = entrance
                            scaleX = .92f + .08f * entrance
                            scaleY = scaleX
                            rotationZ = 0f
                        },
                )
            }
            Text(
                "MIRE",
                color = Color(0xFFF3F8FF),
                fontSize = 36.sp,
                letterSpacing = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.graphicsLayer {
                        alpha = entrance
                        translationY = 8.dp.toPx() * (1 - entrance)
                    },
            )
            Text(
                tr("Votre carnet de tir à l’arc", "Your archery journal"),
                color = Color(0xFFAFCAE2),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.graphicsLayer { alpha = entrance },
            )
            Text(
                tr("Ouverture de votre carnet…", "Opening your journal…"),
                color = Color(0xFFB7C8DC),
                style = MaterialTheme.typography.labelMedium,
            )
            LinearProgressIndicator(
                Modifier.width(112.dp),
                color = Color(0xFFF4C64F),
                trackColor = Color(0xFF253C59),
            )
        }
    }
}
