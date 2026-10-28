package com.example.studyquest

    // Everything is stored in seconds (as Long) and only turned into hours for display.
    const val SECONDS_PER_LEVEL = 10 * 60 * 60L   // 10 hours = 36,000 seconds

    // 10 h -> level 1, 20 h -> level 2, ... (new users start at level 0)
    fun levelFor(totalSeconds: Long): Int = (totalSeconds / SECONDS_PER_LEVEL).toInt()

    // 0f..1f progress towards the next level (the remainder after removing whole levels)
    fun progressInLevel(totalSeconds: Long): Float =
        (totalSeconds % SECONDS_PER_LEVEL).toFloat() / SECONDS_PER_LEVEL

    fun levelTitle(level: Int): String {
        val titles = listOf(
            "Fledgling",
            "Night Stalker",
            "Shadow Walker",
            "Crimson Noble",
            "Vampire Count",
            "Ancient Elder"
        )
        return titles[level.coerceIn(0, titles.lastIndex)]
    }

    fun formatHours(seconds: Long): String = "%.1f".format(seconds / 3600.0)
