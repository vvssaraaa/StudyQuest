package com.example.studyquest

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.util.UUID

data class Quest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String = "",
    val xp: Int = 20,
    val isCompleted: Boolean = false
)

const val XP_GOAL = 500

class TasksViewModel : ViewModel() {

    val quests = mutableStateListOf<Quest>()

    fun addQuest(title: String, subject: String, xp: Int) {
        quests.add(
            Quest(
                title = title,
                subject = subject,
                xp = xp
            )
        )
    }

    fun toggleQuest(id: String) {
        val index = quests.indexOfFirst { it.id == id }
        if (index != -1) {
            val quest = quests[index]
            quests[index] = quest.copy(
                isCompleted = !quest.isCompleted
            )
        }
    }

    fun updateQuest(
        id: String,
        title: String,
        subject: String,
        xp: Int
    ) {
        val index = quests.indexOfFirst { it.id == id }
        if (index != -1) {
            quests[index] = quests[index].copy(
                title = title,
                subject = subject,
                xp = xp
            )
        }
    }

    fun deleteQuest(id: String) {
        quests.removeAll { it.id == id }
    }
}
