package com.emotionalintelligence.emova.model

import com.emotionalintelligence.emova.R

/** Quick "what's behind it" tags. The id is what gets stored in the database. */
enum class Trigger(val id: String, val emoji: String, val labelResId: Int, val promptName: String) {
    WORK("work", "💼", R.string.w_trigger_work, "work or school"),
    FAMILY("family", "🏠", R.string.w_trigger_family, "family"),
    PEOPLE("people", "👥", R.string.w_trigger_people, "partner or friends"),
    HEALTH("health", "🩺", R.string.w_trigger_health, "health"),
    MONEY("money", "💳", R.string.w_trigger_money, "money"),
    SLEEP("sleep", "😴", R.string.w_trigger_sleep, "sleep or tiredness"),
    LONELY("lonely", "🌫️", R.string.w_trigger_lonely, "loneliness"),
    OTHER("other", "✨", R.string.w_trigger_other, "something else");

    companion object {
        fun fromId(id: String): Trigger? = entries.firstOrNull { it.id == id }
    }
}
