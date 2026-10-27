package com.example.studyquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ForestColorScheme = darkColorScheme(
    // Main accent: buttons, study timer, progress bars, checkboxes
    primary = MintLight,
    onPrimary = ForestDeep,

    secondary = LavenderLight,
    onSecondary = ForestDeep,

    // Break timer and XP rewards
    tertiary = Yellow,
    onTertiary = ForestDeep,

    background = ForestDeep,
    onBackground = Cream,

    surface = ForestCard,
    onSurface = Cream,
    surfaceVariant = ForestMid,
    onSurfaceVariant = SageText,
    outline = SageOutline,

    // Selected nav item and chips
    secondaryContainer = ForestLight,
    onSecondaryContainer = Cream,

    // Where Material 3 components pick their background colour:
    surfaceContainer = ForestCard,                              // bottom navigation bar
    surfaceContainerHigh = ForestCard,                          // dialogs
    surfaceContainerHighest = ForestMid.copy(alpha = 0.90f)     // cards (slightly see-through)
)

@Composable
fun StudyQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForestColorScheme,
        typography = Typography,
        content = content
    )
}