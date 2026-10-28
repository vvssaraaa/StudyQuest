package com.example.studyquest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class ProfileViewModel : ViewModel() {

    var displayName by mutableStateOf("Young Vampire")
        private set

    fun updateName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) displayName = trimmed.take(20)
    }

    // Everything below is CALCULATED from the saved sessions, never stored separately.
    // Because it reads the sessions list, the screen updates when a session is added.
    val totalSeconds: Long
        get() = StudyRepository.sessions.sumOf { it.durationSeconds.toLong() }

    val sessionCount: Int
        get() = StudyRepository.sessions.size

    val weekDays: List<DayStat>
        get() = weeklyStats(StudyRepository.sessions)

    // TEMPORARY: lets you test levels and the chart without studying for hours. Remove later.
    fun addTestSession() {
        StudyRepository.addSession(
            StudySession(
                sessionKey = UUID.randomUUID().toString(),
                endedAt = System.currentTimeMillis(),
                durationSeconds = 3600
            )
        )
    }
}