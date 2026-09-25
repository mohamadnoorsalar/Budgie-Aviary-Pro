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
    primary = ForestEmeraldDark,
    onPrimary = Color(0xFF003828),
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = Color(0xFF8CFCD0),
    secondary = AvianGoldDark,
    onSecondary = Color(0xFF422F00),
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = Color(0xFFFFE09C),
    tertiary = PlumageTealDark,
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = TealContainerDark,
    onTertiaryContainer = Color(0xFFA5EEFB),
    error = ErrorRedDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = AviarySurfaceDark,
    onBackground = Color(0xFFE0E5E1),
    surface = AviaryCardDark,
    onSurface = Color(0xFFE0E5E1),
    surfaceVariant = AviarySurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC0CCC5),
    outline = OutlineDark,
    outlineVariant = Color(0xFF3F4B46)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestEmeraldLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = Color(0xFF002116),
    secondary = AvianGoldLight,
    onSecondary = Color.White,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = Color(0xFF2E1F00),
    tertiary = PlumageTealLight,
    onTertiary = Color.White,
    tertiaryContainer = TealContainerLight,
    onTertiaryContainer = Color(0xFF001F25),
    error = ErrorRedLight,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = AviarySandLight,
    onBackground = Color(0xFF171D1B),
    surface = AviarySurfaceLight,
    onSurface = Color(0xFF171D1B),
    surfaceVariant = AviarySurfaceVariantLight,
    onSurfaceVariant = Color(0xFF3F4A45),
    outline = OutlineLight,
    outlineVariant = Color(0xFFC0CCC5)
)

@Composable
fun BudgieAviaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve aviary emerald & gold branding consistently
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = BudgieAviaryTheme(darkTheme, dynamicColor, content)
