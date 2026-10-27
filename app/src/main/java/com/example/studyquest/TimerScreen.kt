package com.example.studyquest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    // Only this screen cares whether the settings dialog is open, so it stays local
    var showSettings by remember { mutableStateOf(false) }

    // ---------- Values read from the ViewModel ----------
    val mode = viewModel.mode
    val isRunning = viewModel.isRunning
    val remainingMillis = viewModel.remainingMillis
    val totalMillis = viewModel.totalMillis

    // ---------- Values calculated for display ----------
    val hasStarted = remainingMillis < totalMillis
    val elapsedFraction = 1f - remainingMillis / totalMillis.toFloat()
    // Round UP so the display shows 25:00 at the start and only hits 00:00 at the very end
    val displaySeconds = ((remainingMillis + 999) / 1000).toInt()
    val minutesLeft = displaySeconds / 60
    val secondsLeft = displaySeconds % 60
    val timeText = "%02d:%02d".format(minutesLeft, secondsLeft)

    // ---------- Colours for the current mode ----------
    // Study = mint (forest), Break = yellow (moonlight).
    // The text always says which mode it is too, so colour is never the only clue.
    val studyAccent = MaterialTheme.colorScheme.primary
    val breakAccent = MaterialTheme.colorScheme.tertiary
    val accent = if (mode == TimerMode.STUDY) studyAccent else breakAccent
    val onAccent = MaterialTheme.colorScheme.onPrimary   // dark forest colour, readable on both accents

    // ---------- Layout ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top: mode buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeButton(
                text = "Pomodoro",
                selected = mode == TimerMode.STUDY,
                canSwitch = !isRunning,
                accent = studyAccent,
                onAccent = onAccent,
                onClick = { viewModel.selectMode(TimerMode.STUDY) }
            )
            ModeButton(
                text = "Break",
                selected = mode == TimerMode.BREAK,
                canSwitch = !isRunning,
                accent = breakAccent,
                onAccent = onAccent,
                onClick = { viewModel.selectMode(TimerMode.BREAK) }
            )
        }

        // Middle: the highlighted timer card (weight(1f) = take all the free space)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(2.dp, accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (mode == TimerMode.STUDY) "FOCUS TIME" else "BREAK TIME",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = accent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeText,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        // Screen readers read "25 minutes 0 seconds remaining" instead of "25:00"
                        modifier = Modifier.semantics {
                            contentDescription = "$minutesLeft minutes $secondsLeft seconds remaining"
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { elapsedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // Bottom: reset, start/pause, settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = { viewModel.reset() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
            }
            Button(
                onClick = { viewModel.toggleRunning() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = onAccent
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = when {
                        isRunning -> "Pause"
                        hasStarted -> "Resume"
                        else -> "Start"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = { showSettings = true },
                enabled = !isRunning
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Timer settings")
            }
        }
    }

    if (showSettings) {
        TimerSettingsDialog(
            studyMinutes = viewModel.studyMinutes,
            breakMinutes = viewModel.breakMinutes,
            onDismiss = { showSettings = false },
            onSave = { newStudy, newBreak ->
                viewModel.saveSettings(newStudy, newBreak)
                showSettings = false
            }
        )
    }
}

// ---------- Small pieces ----------

@Composable
fun ModeButton(
    text: String,
    selected: Boolean,
    canSwitch: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onAccent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    if (selected) {
        // Filled in this mode's colour. Does nothing when tapped (it would reset the timer).
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent)
        ) {
            Text(text, fontWeight = FontWeight.Bold)
        }
    } else {
        // Solid (not see-through) so the forest behind it never hurts the text contrast
        OutlinedButton(
            onClick = onClick,
            enabled = canSwitch,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(text)
        }
    }
}

@Composable
fun TimerSettingsDialog(
    studyMinutes: Int,
    breakMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (study: Int, brk: Int) -> Unit
) {
    // Local "draft" values: only applied when the user presses Save
    var study by remember { mutableIntStateOf(studyMinutes) }
    var brk by remember { mutableIntStateOf(breakMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer settings") },
        text = {
            Column {
                Text("Presets (study / break)", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20 to 10, 25 to 5, 40 to 10).forEach { (s, b) ->
                        OutlinedButton(
                            onClick = {
                                study = s
                                brk = b
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("$s/$b")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                MinutesStepper(label = "Study", value = study) { study = it }
                MinutesStepper(label = "Break", value = brk) { brk = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(study, brk) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MinutesStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) { Text("−") }
            Text("$value min")
            TextButton(onClick = { onChange((value + 1).coerceAtMost(120)) }) { Text("+") }
        }
    }
}