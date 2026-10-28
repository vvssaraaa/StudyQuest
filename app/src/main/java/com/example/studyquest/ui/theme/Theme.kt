package com.example.studyquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VampireForestColorScheme = darkColorScheme(
    // Main accent: buttons, study timer, checkboxes
    primary = MintLight,
    onPrimary = ForestDeep,

    // Vampire accent: level bar, titles
    secondary = LavenderLight,
    onSecondary = ForestDeep,

    // Break timer and XP rewards
    tertiary = Yellow,
    onTertiary = ForestDeep,

    background = ForestDeep,
    onBackground = Cream,

    surface = PlumCard,
    onSurface = Cream,
    surfaceVariant = ForestMid,
    onSurfaceVariant = SageText,
    outline = SageOutline,

    // Selected tab and chips
    secondaryContainer = PlumLight,
    onSecondaryContainer = Cream,

    // Where Material 3 components pick their background colour:
    surfaceContainer = PlumCard,                                // bottom navigation bar
    surfaceContainerHigh = PlumCard,                            // dialogs, timer card, profile cards
    surfaceContainerHighest = ForestMid.copy(alpha = 0.90f)     // quest cards (slightly see-through)
)

@Composable
fun StudyQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VampireForestColorScheme,
        typography = Typography,
        content = content
    )
}