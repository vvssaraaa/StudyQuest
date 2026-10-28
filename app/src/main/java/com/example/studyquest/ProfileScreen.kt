package com.example.studyquest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyquest.ui.theme.Pink
import com.example.studyquest.ui.theme.PlumDeep
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, questXp: Int) {
    var showRename by remember { mutableStateOf(false) }

    // Everything here is calculated from the saved study sessions
    val totalSeconds = viewModel.totalSeconds
    val level = levelFor(totalSeconds)
    val progress = progressInLevel(totalSeconds)
    val secondsIntoLevel = totalSeconds % SECONDS_PER_LEVEL
    val weekDays = viewModel.weekDays
    val weekSeconds = weekDays.sumOf { it.seconds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Character card ----------
        ProfileCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PlumDeep)
                        .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧛", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Level $level · ${levelTitle(level)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { showRename = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit name")
                }
            }
        }

        // ---------- Level progress ----------
        ProfileCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "LEVEL $level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatHours(secondsIntoLevel)} / ${SECONDS_PER_LEVEL / 3600} h to Level ${level + 1}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ---------- Stats ----------
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                value = "${formatHours(totalSeconds)} h",
                label = "Total study",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "${viewModel.sessionCount}",
                label = "Sessions",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "$questXp",
                label = "Quest XP",
                valueColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        // ---------- Weekly summary ----------
        ProfileCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatHours(weekSeconds)} hours this week",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeeklyChart(days = weekDays)
        }

        // TEMPORARY: remove this button when you no longer need it for testing
        TextButton(
            onClick = { viewModel.addTestSession() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add 1 h test session (temporary)")
        }
    }

    if (showRename) {
        RenameDialog(
            currentName = viewModel.displayName,
            onDismiss = { showRename = false },
            onSave = { newName ->
                viewModel.updateName(newName)
                showRename = false
            }
        )
    }
}

// ---------- Pieces ----------

@Composable
private fun ProfileCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
        )
    ) {
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    ProfileCard(modifier = modifier, padding = 12.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyChart(days: List<DayStat>) {
    // Always scale to at least 1 hour, so an empty week doesn't look strange
    val maxSeconds = maxOf(days.maxOf { it.seconds }, 3600L)
    val barColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val fraction = day.seconds / maxSeconds.toFloat()
            val barHeight = maxOf(4.dp, (100 * fraction).dp)
            val dayName = day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.getDefault())
            val fullName = day.date.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault())

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    // One spoken description per bar for screen readers
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$fullName: ${formatHours(day.seconds)} hours" +
                                if (day.isToday) ", today" else ""
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (day.seconds > 0) {
                    Text(
                        text = formatHours(day.seconds),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(barHeight)
                        .background(
                            color = when {
                                day.seconds == 0L -> barColor.copy(alpha = 0.3f)
                                day.isToday -> Pink
                                else -> barColor
                            },
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your vampire name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) name = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}