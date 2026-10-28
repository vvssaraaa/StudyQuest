package com.example.studyquest

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TimerMode { STUDY, BREAK }

fun durationMillis(mode: TimerMode, studyMinutes: Int, breakMinutes: Int): Long {
    val minutes = if (mode == TimerMode.STUDY) studyMinutes else breakMinutes
    return minutes * 60_000L
}

class TimerViewModel : ViewModel() {

    var mode by mutableStateOf(TimerMode.STUDY)
        private set
    var studyMinutes by mutableIntStateOf(25)
        private set
    var breakMinutes by mutableIntStateOf(5)
        private set
    var remainingMillis by mutableLongStateOf(25 * 60_000L)
        private set
    var isRunning by mutableStateOf(false)
        private set

    val totalMillis: Long
        get() = durationMillis(mode, studyMinutes, breakMinutes)

    private var tickJob: Job? = null
    private var endTime = 0L   // the moment (SystemClock.elapsedRealtime) the countdown ends

    // ---------- Actions the screen can call ----------

    fun toggleRunning() {
        if (isRunning) pause() else start()
    }

    fun selectMode(newMode: TimerMode) {
        stopClock()
        mode = newMode
        remainingMillis = durationMillis(newMode, studyMinutes, breakMinutes)
    }

    fun reset() {
        stopClock()
        remainingMillis = totalMillis
    }

    fun saveSettings(newStudyMinutes: Int, newBreakMinutes: Int) {
        studyMinutes = newStudyMinutes
        breakMinutes = newBreakMinutes
        reset()
    }

    // ---------- Internals ----------

    private fun start() {
        if (isRunning) return
        isRunning = true
        endTime = SystemClock.elapsedRealtime() + remainingMillis

        // Compare against the real clock instead of subtracting 1 second per tick,
        // so the time stays accurate even if a tick is late.
        tickJob = viewModelScope.launch {
            while (true) {
                remainingMillis = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                if (remainingMillis == 0L) break
                delay(200)
            }
            onFinished()
        }
    }

    private fun pause() {
        // Store the exact time left, then stop ticking
        remainingMillis = (endTime - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        stopClock()
    }

    private fun stopClock() {
        tickJob?.cancel()
        tickJob = null
        isRunning = false
    }

    private fun onFinished() {
        if (mode == TimerMode.STUDY) {
            // TODO: save the completed study session here (later milestone)
            selectMode(TimerMode.BREAK)
        } else {
            selectMode(TimerMode.STUDY)
        }
    }
}