package com.zhiguapp.ui.theme

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
    primary = CyanGlow,
    secondary = Mint,
    tertiary = Amber,
    error = Coral,
    background = Night,
    surface = NightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = InkBlue,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    secondary = Mint,
    tertiary = Amber,
    error = Coral,
    background = Mist,
    surface = Frost,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = InkBlue,
    onBackground = InkBlue,
    onSurface = InkBlue
)

@Composable
fun ZhiGuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
