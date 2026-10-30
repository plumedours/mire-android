package com.plumedours.mire.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val sport =
    darkColorScheme(
        primary = Color(0xFF69D7FF),
        onPrimary = Color(0xFF051C34),
        primaryContainer = Color(0xFF173E70),
        onPrimaryContainer = Color(0xFFD3EAFF),
        secondary = Color(0xFFB5A1FF),
        onSecondary = Color(0xFF24174C),
        secondaryContainer = Color(0xFF352960),
        onSecondaryContainer = Color(0xFFE8DEFF),
        tertiary = Color(0xFFFFBD68),
        onTertiary = Color(0xFF3A2100),
        tertiaryContainer = Color(0xFF68421D),
        onTertiaryContainer = Color(0xFFFFE4B7),
        background = Color(0xFF0B1328),
        surface = Color(0xFF15223D),
        onSurface = Color(0xFFF1F5FF),
        onBackground = Color(0xFFF1F5FF),
        surfaceVariant = Color(0xFF25365A),
        onSurfaceVariant = Color(0xFFB4C3DF),
        outline = Color(0xFF667DA3),
        outlineVariant = Color(0xFF304469),
        surfaceContainer = Color(0xFF1A2B49),
        surfaceContainerLow = Color(0xFF101C33),
        surfaceContainerHigh = Color(0xFF223453),
    )
private val light =
    lightColorScheme(
        primary = Color(0xFF3159D1),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD5E2FF),
        onPrimaryContainer = Color(0xFF112A70),
        secondary = Color(0xFF7653C7),
        secondaryContainer = Color(0xFFE8DCFF),
        onSecondaryContainer = Color(0xFF342056),
        tertiary = Color(0xFFB85B1D),
        tertiaryContainer = Color(0xFFFFDDAB),
        onTertiaryContainer = Color(0xFF4D2504),
        background = Color(0xFFE4ECFF),
        onBackground = Color(0xFF14254A),
        surface = Color(0xFFF0F4FF),
        onSurface = Color(0xFF152749),
        surfaceVariant = Color(0xFFD4E0F8),
        onSurfaceVariant = Color(0xFF455A80),
        outline = Color(0xFF778DB0),
        outlineVariant = Color(0xFFB9CBE9),
        surfaceContainer = Color(0xFFE4E8FF),
        surfaceContainerLow = Color(0xFFEAF2FF),
        surfaceContainerHigh = Color(0xFFDCE5FA),
    )

private fun palette(
    primary: Long,
    secondary: Long,
    tertiary: Long,
    background: Long,
    surface: Long,
    primaryContainer: Long,
    secondaryContainer: Long,
    tertiaryContainer: Long,
) =
    sport.copy(
        primary = Color(primary),
        secondary = Color(secondary),
        tertiary = Color(tertiary),
        background = Color(background),
        surface = Color(surface),
        primaryContainer = Color(primaryContainer),
        secondaryContainer = Color(secondaryContainer),
        tertiaryContainer = Color(tertiaryContainer),
        surfaceVariant = Color(surface),
        surfaceContainer = Color(surface),
        surfaceContainerLow = Color(background),
        surfaceContainerHigh = Color(primaryContainer),
        outlineVariant = Color(primaryContainer),
        onSurface = Color(0xFFF2F3EF),
        onBackground = Color(0xFFF2F3EF),
        onSurfaceVariant = Color(0xFFC2C9C4),
        onPrimaryContainer = Color(0xFFF2F3EF),
        onSecondaryContainer = Color(0xFFF2F3EF),
        onTertiaryContainer = Color(0xFFF2F3EF),
    )

private val forest =
    palette(
        0xFF8CDCA7,
        0xFFB8D58A,
        0xFFE9BC81,
        0xFF0D1C17,
        0xFF1A3027,
        0xFF224D38,
        0xFF39482A,
        0xFF544027,
    )
private val plum =
    palette(
        0xFFD3B1FF,
        0xFFF5ACD2,
        0xFFFFCA96,
        0xFF201326,
        0xFF33213D,
        0xFF513563,
        0xFF62324E,
        0xFF573D28,
    )
private val graphite =
    palette(
        0xFFFFCC80,
        0xFFACC9E8,
        0xFFA9D5C0,
        0xFF17191D,
        0xFF272A30,
        0xFF574125,
        0xFF304457,
        0xFF304A3D,
    )

@Composable
fun MireTheme(mode: String = "sport", content: @Composable () -> Unit) {
    val scheme =
        when (mode) {
            "forest" -> forest
            "plum" -> plum
            "graphite" -> graphite
            "light" -> light
            "system" -> if (isSystemInDarkTheme()) sport else light
            else -> sport
        }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        shapes =
            Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(18.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(30.dp),
            ),
        content = content,
    )
}
