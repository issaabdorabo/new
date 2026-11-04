package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8), // Bright sky cyan
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = RoseDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryLighter,
    onPrimaryContainer = PrimaryBlueVariant,
    secondary = TechCyan,
    onSecondary = Color.White,
    secondaryContainer = TechCyanLight,
    onSecondaryContainer = TechCyanDark,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = RoseDanger,
    onError = Color.White
)

private val LuxuryRedColorScheme = darkColorScheme(
    primary = LuxuryRedPrimary,
    onPrimary = Color.White,
    primaryContainer = LuxuryRedPrimaryContainer,
    onPrimaryContainer = LuxuryRedOnPrimaryContainer,
    secondary = LuxuryRedSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF450A0A),
    onSecondaryContainer = Color(0xFFFECACA),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color.Black,
    background = LuxuryRedBg,
    onBackground = Color(0xFFFFF1F2),
    surface = LuxuryRedSurface,
    onSurface = Color(0xFFFFF1F2),
    surfaceVariant = LuxuryRedSurfaceVariant,
    onSurfaceVariant = Color(0xFFFDA4AF),
    outline = LuxuryRedBorder,
    error = RoseDanger,
    onError = Color.White
)

private val LuxuryEmeraldColorScheme = darkColorScheme(
    primary = LuxuryEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = LuxuryEmeraldPrimaryContainer,
    onPrimaryContainer = LuxuryEmeraldOnPrimaryContainer,
    secondary = LuxuryEmeraldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF022C22),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color.Black,
    background = LuxuryEmeraldBg,
    onBackground = Color(0xFFECFDF5),
    surface = LuxuryEmeraldSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = LuxuryEmeraldSurfaceVariant,
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = LuxuryEmeraldBorder,
    error = RoseDanger,
    onError = Color.White
)

private val LuxuryGoldColorScheme = darkColorScheme(
    primary = LuxuryGoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = LuxuryGoldPrimaryContainer,
    onPrimaryContainer = LuxuryGoldOnPrimaryContainer,
    secondary = LuxuryGoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    background = LuxuryGoldBg,
    onBackground = Color(0xFFFFFBEB),
    surface = LuxuryGoldSurface,
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = LuxuryGoldSurfaceVariant,
    onSurfaceVariant = Color(0xFFFCD34D),
    outline = LuxuryGoldBorder,
    error = RoseDanger,
    onError = Color.White
)

@Composable
fun AutoPartsProTheme(
    themeMode: String = "system",
    darkTheme: Boolean = when (themeMode) {
        "dark", "luxury_red", "luxury_emerald", "luxury_gold" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "luxury_red" -> LuxuryRedColorScheme
        "luxury_emerald" -> LuxuryEmeraldColorScheme
        "luxury_gold" -> LuxuryGoldColorScheme
        "dark" -> DarkColorScheme
        "light" -> LightColorScheme
        else -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
