package com.example.quantpricetracker.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Centralized haptic vocabulary. Each action in the app has a distinct feel;
 * consistency is the point — users learn "that tick means page changed."
 *
 * Preference order:
 *   1. System HapticFeedbackConstants on a View (respects user's haptic strength
 *      setting and matches every other app on the OS).
 *   2. Custom VibrationEffect waveform for alert-style patterns where no
 *      system constant is a good fit.
 *
 * Haptics do NOT fire on the emulator. Verify on real hardware.
 */
class HapticsController(private val context: Context) {

    /** Use when you have a View (e.g. inside a clickable). Feels native. */
    fun performOnView(view: View, pattern: Pattern) {
        val code = when (pattern) {
            Pattern.TICK -> {
                // SEGMENT_TICK added in API 30 — we target minSdk 30, so safe.
                HapticFeedbackConstants.SEGMENT_TICK
            }
            Pattern.CONFIRM -> HapticFeedbackConstants.CONTEXT_CLICK
            Pattern.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
            Pattern.REFRESH -> HapticFeedbackConstants.CLOCK_TICK
            Pattern.ALERT -> {
                // No good system constant for a double-buzz — fall through to vibrator.
                vibrateWaveform(longArrayOf(0, 80, 60, 80))
                return
            }
            Pattern.ERROR -> {
                vibrateWaveform(longArrayOf(0, 120))
                return
            }
        }
        try {
            view.performHapticFeedback(code)
        } catch (_: Exception) { /* ignore */ }
    }

    /** Use from ViewModel / non-View contexts. Falls back to Vibrator API. */
    fun perform(pattern: Pattern) {
        val effect = when (pattern) {
            Pattern.TICK -> longArrayOf(0, 18)
            Pattern.CONFIRM -> longArrayOf(0, 35, 25, 35)
            Pattern.LONG_PRESS -> longArrayOf(0, 45)
            Pattern.REFRESH -> longArrayOf(0, 15)
            Pattern.ALERT -> longArrayOf(0, 80, 60, 80)
            Pattern.ERROR -> longArrayOf(0, 120)
        }
        vibrateWaveform(effect)
    }

    private fun vibrateWaveform(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) { /* Vibration unavailable — silently skip. */ }
    }

    enum class Pattern {
        /** Sharp tick — page change, toggle, rotary detent. */
        TICK,
        /** Soft confirm — successful action. */
        CONFIRM,
        /** Native long-press — matches OS. */
        LONG_PRESS,
        /** Subliminal pulse — background refresh done. */
        REFRESH,
        /** Double-buzz — threshold crossed, needs attention. */
        ALERT,
        /** Long buzz — something went wrong. */
        ERROR
    }
}
