package com.example.studyquest

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Quest(
    val id: Int,
    val title: String,
    val subject: String,
    val xp: Int,
    val isCompleted: Boolean = false
)

// Placeholder goal for the XP bar (will be replaced by study-time levels later)
const val XP_GOAL = 100

class TasksViewModel : ViewModel() {

    // Compose redraws anything that reads this list whenever it changes
    val quests = mutableStateListOf(
        Quest(1, "Study algorithms", "Algdat", 30),
        Quest(2, "Work on webapp project", "Web Applications", 50),
        Quest(3, "Security quiz", "Data Security", 20)
    )

    private var nextId = 4

    fun addQuest(title: String, subject: String, xp: Int) {
        quests.add(Quest(nextId, title, subject, xp))
        nextId++
    }

    fun toggleQuest(id: Int) {
        val index = quests.indexOfFirst { it.id == id }
        if (index != -1) {
            // Replace the item with a changed copy so Compose notices the change
            quests[index] = quests[index].copy(isCompleted = !quests[index].isCompleted)
        }
    }

    fun updateQuest(id: Int, title: String, subject: String, xp: Int) {
        val index = quests.indexOfFirst { it.id == id }
        if (index != -1) {
            quests[index] = quests[index].copy(title = title, subject = subject, xp = xp)
        }
    }

    fun deleteQuest(id: Int) {
        quests.removeAll { it.id == id }
    }
}