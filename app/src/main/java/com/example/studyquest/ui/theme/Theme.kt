package com.example.studyquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Lavender,
    secondary = Pink,
    tertiary = Yellow,

    background = Cream,
    surface = Cream,

    onPrimary = Color.White,
    onSecondary = DarkPurple,
    onTertiary = DarkPurple,

    onBackground = DarkPurple,
    onSurface = DarkPurple
)

private val DarkColorScheme = darkColorScheme(
    primary = Lavender,
    secondary = Pink,
    tertiary = Yellow,

    background = DarkPurple,
    surface = DarkPurple,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DarkPurple,

    onBackground = Cream,
    onSurface = Cream
)

@Composable
fun StudyQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}