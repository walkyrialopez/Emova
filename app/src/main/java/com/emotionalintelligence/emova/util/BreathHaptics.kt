package com.emotionalintelligence.emova.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Vibration cues so the breathing exercise can be done with eyes closed.
 * Phases are told apart by NUMBER of pulses, not strength (strength varies a lot between phones):
 *
 *   inhale -> 1 short pulse
 *   exhale -> 2 short pulses
 *   hold   -> nothing
 *   done   -> 1 long pulse
 */
class BreathHaptics(context: Context) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    /** False on devices without a vibration motor (some tablets): the UI hides the feature there. */
    val isAvailable: Boolean get() = vibrator?.hasVibrator() == true

    fun inhale() = play(longArrayOf(0, SHORT))
    fun exhale() = play(longArrayOf(0, SHORT, GAP, SHORT))
    fun finish() = play(longArrayOf(0, LONG))

    fun cancel() { vibrator?.cancel() }

    private fun play(timings: LongArray) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        v.vibrate(VibrationEffect.createWaveform(timings, -1))
    }

    companion object {
        private const val SHORT = 180L
        private const val GAP = 180L
        private const val LONG = 800L

        private const val PREFS = "emova_breathing"
        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("haptics_on", true)
        fun setEnabled(context: Context, value: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("haptics_on", value).apply()
        }
        fun introSeen(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("haptics_intro_seen", false)
        fun markIntroSeen(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("haptics_intro_seen", true).apply()
        }
    }
}
