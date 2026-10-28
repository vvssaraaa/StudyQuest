package com.example.studyquest

import androidx.compose.runtime.mutableStateListOf

// One completed study session (only completed sessions are ever saved)
data class StudySession(
    val sessionKey: String,      // random id created when the session starts, used to prevent duplicates
    val endedAt: Long,           // epoch millis
    val durationSeconds: Int     // actual focused seconds
)

// "object" = there is exactly one of it in the whole app (a singleton).
// For now the sessions only live in memory. Later this is the ONE file you change
// to save them with Room; the timer and the profile keep calling the same functions.
object StudyRepository {

    val sessions = mutableStateListOf<StudySession>()

    fun addSession(session: StudySession) {
        // Same sessionKey = the same session, so never record it twice
        if (sessions.any { it.sessionKey == session.sessionKey }) return
        sessions.add(session)
    }
}