package com.olddragon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Cores da Grifinória
val GryffindorRed = Color(0xFF740001)
val GryffindorGold = Color(0xFFD3A625)
val GryffindorDarkRed = Color(0xFF460000)
val GryffindorLightGold = Color(0xFFF4E4BC)
val GryffindorBurgundy = Color(0xFF8B0000)

// Cores de apoio para RPG
val DragonBlack = Color(0xFF1A1A1A)
val ScrollParchment = Color(0xFFF5F2E8)
val MedievalBrown = Color(0xFF8B4513)
val DarkGold = Color(0xFFB8860B)
val BloodRed = Color(0xFF8B0000)

// Esquema de cores customizado para tema escuro/claro
private val DarkColorScheme = darkColorScheme(
    primary = GryffindorGold,
    onPrimary = DragonBlack,
    primaryContainer = GryffindorDarkRed,
    onPrimaryContainer = GryffindorLightGold,

    secondary = GryffindorRed,
    onSecondary = GryffindorLightGold,
    secondaryContainer = GryffindorBurgundy,
    onSecondaryContainer = GryffindorLightGold,

    tertiary = DarkGold,
    onTertiary = DragonBlack,

    error = BloodRed,
    onError = Color.White,

    background = DragonBlack,
    onBackground = GryffindorLightGold,
    surface = Color(0xFF2D1B1B),
    onSurface = GryffindorLightGold,

    surfaceVariant = Color(0xFF3D2626),
    onSurfaceVariant = ScrollParchment,

    outline = GryffindorGold,
    outlineVariant = MedievalBrown
)

private val LightColorScheme = lightColorScheme(
    primary = GryffindorRed,
    onPrimary = Color.White,
    primaryContainer = GryffindorLightGold,
    onPrimaryContainer = GryffindorDarkRed,

    secondary = GryffindorGold,
    onSecondary = DragonBlack,
    secondaryContainer = Color(0xFFFFF8DC),
    onSecondaryContainer = MedievalBrown,

    tertiary = MedievalBrown,
    onTertiary = Color.White,

    error = BloodRed,
    onError = Color.White,

    background = ScrollParchment,
    onBackground = DragonBlack,
    surface = Color.White,
    onSurface = DragonBlack,

    surfaceVariant = Color(0xFFF0E6D6),
    onSurfaceVariant = MedievalBrown,

    outline = GryffindorRed,
    outlineVariant = GryffindorGold
)

// Tipografia inspirada em pergaminhos medievais
val RPGTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.3.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.2.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun OldDragonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RPGTypography,
        content = content
    )
}