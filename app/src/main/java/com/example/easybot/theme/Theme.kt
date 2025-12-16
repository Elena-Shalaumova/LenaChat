package com.example.easybot.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Используем новые цвета, которые мы определили
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue, // Используем наш основной синий
    onPrimary = Color.White,
    secondary = UserMessageBlue,
    onSecondary = Color.Black,
    tertiary = ModelMessageGrey,
    onTertiary = Color.Black,
    //background = Color(0xFF121212),
    background = DarkBackgroundBlue,
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B20),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue, // Используем наш основной синий
    onPrimary = Color.White,
    secondary = UserMessageBlue,
    onSecondary = Color.Black,
    tertiary = ModelMessageGrey,
    onTertiary = Color.Black,
    background = Color(0xFFF2F4F7),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun EasyBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Отключаем, чтобы всегда использовать нашу тему
    content: @Composable () -> Unit
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
