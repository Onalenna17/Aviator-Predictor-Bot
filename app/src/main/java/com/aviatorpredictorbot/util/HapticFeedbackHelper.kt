package com.aviatorpredictorbot.util

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Manages tactile haptic feedback for predictor bot events, specifically targeting
 * high-confidence prediction alerts and harmonic threshold detections.
 */
class HapticFeedbackHelper(private val context: Context) {

    companion object {
        private const val TAG = "HapticFeedbackHelper"
    }

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to obtain vibrator service: ${e.message}")
            null
        }
    }

    /**
     * Distinctive double-pulse haptic pattern for a newly generated high-confidence prediction.
     * Pattern: 60ms pulse, 60ms pause, 110ms pulse.
     */
    fun triggerHighConfidencePredictionHaptic(enabled: Boolean = true) {
        if (!enabled) return
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Waveform with varying amplitudes for a crisp modern haptic feel
                val timings = longArrayOf(0, 50, 60, 100)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 50, 60, 100), -1)
            }
            Log.d(TAG, "High-confidence prediction haptic triggered")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to vibrate: ${e.message}")
        }
    }

    /**
     * High-energy celebratory triple-pulse for a 10x+ Pink harmonic threshold hit.
     */
    fun triggerPinkBurstHaptic(enabled: Boolean = true) {
        if (!enabled) return
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 50, 70, 50, 140)
                val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 40, 50, 70, 50, 140), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to vibrate: ${e.message}")
        }
    }

    /**
     * Short crisp click for user interactions (e.g. testing haptic feedback in settings).
     */
    fun triggerPreviewHaptic(enabled: Boolean = true) {
        if (!enabled) return
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(45)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to vibrate: ${e.message}")
        }
    }
}
