package com.example.studyquest

import android.os.SystemClock
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class TimerMode { STUDY, BREAK }

fun durationMillis(mode: TimerMode, studyMinutes: Int, breakMinutes: Int): Long {
    val minutes = if (mode == TimerMode.STUDY) studyMinutes else breakMinutes
    return minutes * 60_000L
}

@Composable
fun TimerScreen() {
    //State
    var mode by remember { mutableStateOf(TimerMode.STUDY) }
    var studyMinutes by remember { mutableIntStateOf(25) }
    var breakMinutes by remember { mutableIntStateOf(5) }
    var remainingMillis by remember { mutableLongStateOf(25 * 60_000L) }
    var isRunning by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    //Values calculated from state
    val totalMillis = durationMillis(mode, studyMinutes, breakMinutes)
    val hasStarted = remainingMillis < totalMillis
    val elapsedFraction = 1f - remainingMillis / totalMillis.toFloat()
    // Round UP so the display shows 25:00 at the start and only hits 00:00 at the very end
    val displaySeconds = ((remainingMillis + 999) / 1000).toInt()
    val timeText = "%02d:%02d".format(displaySeconds / 60, displaySeconds % 60)

    // Actions
    fun selectMode(newMode: TimerMode) {
        mode = newMode
        isRunning = false
        remainingMillis = durationMillis(newMode, studyMinutes, breakMinutes)
    }

    fun resetTimer() {
        isRunning = false
        remainingMillis = durationMillis(mode, studyMinutes, breakMinutes)
    }

    // Runs whenever isRunning changes. We remember WHEN the timer should end and
    // compare against the real clock, instead of subtracting 1 second each tick.
    // That keeps the time accurate even if a tick is late or the app is in the background.
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect

        val endTime = SystemClock.elapsedRealtime() + remainingMillis
        while (true) {
            remainingMillis = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            if (remainingMillis == 0L) break
            delay(200)
        }

        // The countdown reached zero
        if (mode == TimerMode.STUDY) {
            // TODO: save the completed study session here (later milestone)
            selectMode(TimerMode.BREAK)
        } else {
            selectMode(TimerMode.STUDY)
        }
    }

    //colors
    val containerColor =
        if (mode == TimerMode.STUDY) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondary
    val contentColor =
        if (mode == TimerMode.STUDY) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.primary

    // layout
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: mode buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModeButton(
                    text = "Study",
                    selected = mode == TimerMode.STUDY,
                    canSwitch = !isRunning,
                    onClick = { selectMode(TimerMode.STUDY) }
                )
                ModeButton(
                    text = "Break",
                    selected = mode == TimerMode.BREAK,
                    canSwitch = !isRunning,
                    onClick = { selectMode(TimerMode.BREAK) }
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
                    color = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (mode == TimerMode.STUDY) "FOCUS TIME" else "BREAK TIME",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = timeText,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { elapsedFraction },
                            modifier = Modifier.fillMaxWidth(),
                            color = contentColor,
                            trackColor = contentColor.copy(alpha = 0.25f)
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
                IconButton(onClick = { resetTimer() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
                }
                Button(
                    onClick = { isRunning = !isRunning },
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
                        style = MaterialTheme.typography.titleMedium
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
    }

    if (showSettings) {
        TimerSettingsDialog(
            studyMinutes = studyMinutes,
            breakMinutes = breakMinutes,
            onDismiss = { showSettings = false },
            onSave = { newStudy, newBreak ->
                studyMinutes = newStudy
                breakMinutes = newBreak
                resetTimer()
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
    onClick: () -> Unit
) {
    if (selected) {
        // Do nothing when the already-selected mode is tapped (it would reset the timer)
        Button(onClick = {}) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, enabled = canSwitch) { Text(text) }
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
