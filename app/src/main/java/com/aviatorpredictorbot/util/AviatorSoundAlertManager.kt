package com.aviatorpredictorbot.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AviatorSoundAlertManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            Log.e("AviatorSoundAlert", "ToneGenerator init failed", e)
        }
    }

    /**
     * Plays a distinct high-frequency alert chime when a predicted high-value multiplier (above 10x) is detected.
     */
    fun playHighValueAlert() {
        try {
            // TONE_CDMA_ALERT_CALL_GUARD or TONE_PROP_BEEP2 gives a distinct high-value chime
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
            triggerHapticPattern()
        } catch (e: Exception) {
            Log.e("AviatorSoundAlert", "Failed to play sound tone", e)
        }
    }

    private fun triggerHapticPattern() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 140, 70, 220), -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(longArrayOf(0, 140, 70, 220), -1)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
