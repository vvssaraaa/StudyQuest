package com.example.studyquest

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DayStat(val date: LocalDate, val seconds: Long, val isToday: Boolean)

// Returns 7 entries, Monday to Sunday of the current week, with the seconds studied each day.
fun weeklyStats(
    sessions: List<StudySession>,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): List<DayStat> {
    val monday = today.with(DayOfWeek.MONDAY)

    // Turn each session's timestamp into a calendar day in the phone's time zone, then add up per day
    val secondsByDate = sessions
        .groupBy { Instant.ofEpochMilli(it.endedAt).atZone(zone).toLocalDate() }
        .mapValues { entry -> entry.value.sumOf { it.durationSeconds.toLong() } }

    return List(7) { i ->
        val date = monday.plusDays(i.toLong())
        DayStat(date, secondsByDate[date] ?: 0L, date == today)
    }
}