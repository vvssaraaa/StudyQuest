package com.example.studyquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color


private val StudyQuestColorScheme = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF171525),

    secondary = PixelCyan,
    onSecondary = Color(0xFF171525),

    tertiary = Pink,
    onTertiary = Color(0xFF171525),

    background = Cream,
    onBackground = SoftWhite,

    surface = DarkPurple,
    onSurface = SoftWhite,

    surfaceVariant = DeepPurple,
    onSurfaceVariant = MutedPurple,

    error = Color(0xFFFF7A90),
    onError = Color(0xFF171525),

    outline = Color(0xFF5B5277)
)

@Composable
fun StudyQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyQuestColorScheme,
        typography = Typography,
        content = content
    )
}