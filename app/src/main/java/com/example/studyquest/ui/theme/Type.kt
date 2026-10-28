package com.example.studyquest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

// Start from Material 3's standard text styles (sizes, spacing...) and only swap the font.
private val defaults = Typography()

private fun TextStyle.withAppFont() = copy(fontFamily = AppFont)

val Typography = Typography(
    displayLarge = defaults.displayLarge.withAppFont(),
    displayMedium = defaults.displayMedium.withAppFont(),
    displaySmall = defaults.displaySmall.withAppFont(),
    headlineLarge = defaults.headlineLarge.withAppFont(),
    headlineMedium = defaults.headlineMedium.withAppFont(),
    headlineSmall = defaults.headlineSmall.withAppFont(),
    titleLarge = defaults.titleLarge.withAppFont(),
    titleMedium = defaults.titleMedium.withAppFont(),
    titleSmall = defaults.titleSmall.withAppFont(),
    bodyLarge = defaults.bodyLarge.withAppFont(),
    bodyMedium = defaults.bodyMedium.withAppFont(),
    bodySmall = defaults.bodySmall.withAppFont(),
    labelLarge = defaults.labelLarge.withAppFont(),
    labelMedium = defaults.labelMedium.withAppFont(),
    labelSmall = defaults.labelSmall.withAppFont()
)