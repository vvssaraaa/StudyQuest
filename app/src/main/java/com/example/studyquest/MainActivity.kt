package com.example.studyquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.studyquest.ui.theme.StudyQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyQuestTheme(darkTheme = false) {
                StudyQuestApp()
            }
        }
    }
}

// ---------- Data ----------

data class Quest(
    val id: Int,
    val title: String,
    val subject: String,
    val xp: Int,
    val isCompleted: Boolean = false
)

// Placeholder goal for the XP bar (will be replaced by study-time levels later)
const val XP_GOAL = 100

// ---------- Main screen ----------

@Composable
fun StudyQuestApp() {
    // The list of quests. Compose redraws the screen whenever this list changes.
    val quests = remember {
        mutableStateListOf(
            Quest(1, "Study algorithms", "Algdat", 30),
            Quest(2, "Work on webapp project", "Web Applications", 50),
            Quest(3, "Security quiz", "Data Security", 20)
        )
    }
    var nextId by remember { mutableIntStateOf(4) }

    // Dialog state: is it visible, and which quest is being edited (null = adding a new one)
    var showDialog by remember { mutableStateOf(false) }
    var editingQuest by remember { mutableStateOf<Quest?>(null) }

    // Values calculated from the list (not stored separately)
    val earnedXp = quests.filter { it.isCompleted }.sumOf { it.xp }
    val progress = (earnedXp / XP_GOAL.toFloat()).coerceIn(0f, 1f)
    val openQuests = quests.filter { !it.isCompleted }
    val doneQuests = quests.filter { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "StudyQuest",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "$earnedXp / $XP_GOAL xp")
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🔥 0 day streak",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's quests",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        // weight(1f) = take all remaining space, so the button below stays at the bottom
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (quests.isEmpty()) {
                item { Text("No quests yet. Add your first one!") }
            }

            items(openQuests, key = { it.id }) { quest ->
                QuestCard(
                    quest = quest,
                    onToggle = {
                        val index = quests.indexOfFirst { it.id == quest.id }
                        if (index != -1) {
                            // Replace the item with a changed copy so Compose notices
                            quests[index] = quest.copy(isCompleted = !quest.isCompleted)
                        }
                    },
                    onEdit = {
                        editingQuest = quest
                        showDialog = true
                    }
                )
            }

            if (doneQuests.isNotEmpty()) {
                item {
                    Text(
                        text = "Completed (${doneQuests.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(doneQuests, key = { it.id }) { quest ->
                    QuestCard(
                        quest = quest,
                        onToggle = {
                            val index = quests.indexOfFirst { it.id == quest.id }
                            if (index != -1) {
                                quests[index] = quest.copy(isCompleted = !quest.isCompleted)
                            }
                        },
                        onEdit = {
                            editingQuest = quest
                            showDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                editingQuest = null
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add new quest!")
        }
    }

    if (showDialog) {
        QuestDialog(
            quest = editingQuest,
            onDismiss = { showDialog = false },
            onSave = { title, subject, xp ->
                val existing = editingQuest
                if (existing == null) {
                    quests.add(Quest(nextId, title, subject, xp))
                    nextId++
                } else {
                    val index = quests.indexOfFirst { it.id == existing.id }
                    if (index != -1) {
                        quests[index] = existing.copy(title = title, subject = subject, xp = xp)
                    }
                }
                showDialog = false
            },
            onDelete = {
                val existing = editingQuest
                if (existing != null) {
                    quests.removeAll { it.id == existing.id }
                }
                showDialog = false
            }
        )
    }
}

// ---------- Quest card ----------

@Composable
fun QuestCard(
    quest: Quest,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (quest.isCompleted) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = quest.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit() }
            ) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else null
                )
                if (quest.subject.isNotBlank()) {
                    Text(
                        text = quest.subject,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                text = "+${quest.xp} XP",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

// ---------- Add / edit dialog ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestDialog(
    quest: Quest?,                       // null = adding, not null = editing
    onDismiss: () -> Unit,
    onSave: (title: String, subject: String, xp: Int) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(quest?.title ?: "") }
    var subject by remember { mutableStateOf(quest?.subject ?: "") }
    var xp by remember { mutableIntStateOf(quest?.xp ?: 20) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quest == null) "New quest" else "Edit quest") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("XP reward", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 30, 50).forEach { option ->
                        FilterChip(
                            selected = xp == option,
                            onClick = { xp = option },
                            label = { Text("$option") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), subject.trim(), xp) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (quest != null) {
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}