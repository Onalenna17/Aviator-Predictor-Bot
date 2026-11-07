package com.aviatorpredictorbot.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.aviatorpredictorbot.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sin

/**
 * Authentic Betway Spribe Aviator crash game audio engine.
 *
 * Plays real recorded game sound assets extracted directly from the authentic Spribe Aviator game:
 * - Real Spribe "Flew Away!" crash swoosh [R.raw.aviator_crash]
 * - Real accelerating engine / propeller flight loop [R.raw.aviator_flight] with dynamic pitch shift (`setRate`)
 * - Real Aviator game soundtrack [R.raw.aviator_soundtrack] via MediaPlayer
 * - Dual crystal cashout bell chime and 10x+ pink multiplier celebration siren
 *
 * Runs automatically as live flight telemetry updates.
 */
class AviatorGameSoundEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AviatorSoundEngine"
        private const val SAMPLE_RATE = 22050
    }

    var masterEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopFlightEngine()
                stopSoundtrack()
            }
        }

    var engineEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopFlightEngine()
            }
        }

    var crashEnabled: Boolean = true
    var cashoutEnabled: Boolean = true
    var pinkSirenEnabled: Boolean = true
    var volume: Float = 0.90f
        set(value) {
            field = value
            soundtrackPlayer?.setVolume(value * 0.45f, value * 0.45f)
        }

    private var soundPool: SoundPool? = null
    private var crashSoundId = 0
    private var engineSoundId = 0
    private var cashoutSoundId = 0
    private var pinkSoundId = 0
    private var tickSoundId = 0

    private val loadedSounds = ConcurrentHashMap<Int, Boolean>()
    private var activeEngineStreamId = 0
    private val isEnginePlaying = AtomicBoolean(false)
    private var soundtrackPlayer: MediaPlayer? = null

    init {
        initializeAudio()
    }

    private fun initializeAudio() {
        scope.launch(Dispatchers.IO) {
            try {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val pool = SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(attributes)
                    .build()

                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSounds[sampleId] = true
                        Log.d(TAG, "Sound sample $sampleId loaded successfully")
                    }
                }

                soundPool = pool

                // 1. Load real authentic crash "Flew Away" sound
                try {
                    crashSoundId = pool.load(context, R.raw.aviator_crash, 1)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load raw crash sound", e)
                }

                // 2. Load real authentic engine flight loop
                try {
                    engineSoundId = pool.load(context, R.raw.aviator_flight, 1)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load raw flight sound", e)
                }

                // 3. Synthesize crystal cashout bell chime, pink siren, and countdown ticks for accompanying events
                val cacheDir = context.cacheDir
                val cashoutFile = File(cacheDir, "aviator_cashout.wav")
                val pinkFile = File(cacheDir, "aviator_pink.wav")
                val tickFile = File(cacheDir, "aviator_tick.wav")

                writeCashoutWav(cashoutFile)
                writePinkWav(pinkFile)
                writeTickWav(tickFile)

                cashoutSoundId = pool.load(cashoutFile.absolutePath, 1)
                pinkSoundId = pool.load(pinkFile.absolutePath, 1)
                tickSoundId = pool.load(tickFile.absolutePath, 1)

                // Initialize background game soundtrack player
                initSoundtrackPlayer()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Aviator game sound engine", e)
            }
        }
    }

    private fun initSoundtrackPlayer() {
        try {
            soundtrackPlayer = MediaPlayer.create(context, R.raw.aviator_soundtrack)?.apply {
                isLooping = true
                setVolume(volume * 0.40f, volume * 0.40f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating soundtrack player", e)
        }
    }

    /**
     * Plays the real authentic Spribe Aviator crash ("Flew Away!") sound.
     * Stops the engine immediately and triggers the dramatic crash whoosh.
     */
    fun playCrash() {
        if (!masterEnabled || !crashEnabled) return

        // Instantly cut off flight engine sound when plane flies away
        stopFlightEngine()

        val pool = soundPool ?: return
        if (crashSoundId > 0 && loadedSounds[crashSoundId] == true) {
            pool.play(crashSoundId, volume, volume, 2, 0, 1.0f)
            Log.d(TAG, "Played real Aviator crash sound")
        }
    }

    /**
     * Updates real flight engine / propeller audio with dynamic pitch climbing.
     * Starts looped flight audio if not already playing and adjusts playback rate as multiplier grows.
     */
    fun updateFlightEngine(multiplier: Double) {
        if (!masterEnabled || !engineEnabled) {
            stopFlightEngine()
            return
        }

        val pool = soundPool ?: return
        if (engineSoundId <= 0 || loadedSounds[engineSoundId] != true) return

        // Accelerate playback speed & pitch dynamically as multiplier increases
        // Rate moves from 1.0f at 1.0x to 1.75f at 10x+
        val logM = ln(multiplier.coerceAtLeast(1.0))
        val rate = (1.0f + (logM * 0.35f).toFloat()).coerceIn(0.9f, 2.0f)

        if (!isEnginePlaying.get()) {
            val streamId = pool.play(engineSoundId, volume * 0.85f, volume * 0.85f, 1, -1, rate)
            if (streamId > 0) {
                activeEngineStreamId = streamId
                isEnginePlaying.set(true)
            }
        } else if (activeEngineStreamId > 0) {
            pool.setRate(activeEngineStreamId, rate)
            pool.setVolume(activeEngineStreamId, volume * 0.85f, volume * 0.85f)
        }
    }

    /**
     * Stops the active flight propeller engine audio.
     */
    fun stopFlightEngine() {
        val pool = soundPool
        val streamId = activeEngineStreamId
        if (streamId > 0 && pool != null) {
            try {
                pool.stop(streamId)
            } catch (_: Exception) {}
            activeEngineStreamId = 0
        }
        isEnginePlaying.set(false)
    }

    /**
     * Dual crystal bell chime on successful auto-stop cashout.
     */
    fun playCashout() {
        if (!masterEnabled || !cashoutEnabled) return
        val pool = soundPool ?: return
        if (cashoutSoundId > 0 && loadedSounds[cashoutSoundId] == true) {
            pool.play(cashoutSoundId, volume, volume, 2, 0, 1.0f)
        }
    }

    /**
     * Ascending celebration siren when multiplier crosses 10x+ pink threshold.
     */
    fun playPinkSiren() {
        if (!masterEnabled || !pinkSirenEnabled) return
        val pool = soundPool ?: return
        if (pinkSoundId > 0 && loadedSounds[pinkSoundId] == true) {
            pool.play(pinkSoundId, volume, volume, 2, 0, 1.0f)
        }
    }

    /**
     * Countdown tick during round standby.
     */
    fun playCountdownTick() {
        if (!masterEnabled) return
        val pool = soundPool ?: return
        if (tickSoundId > 0 && loadedSounds[tickSoundId] == true) {
            pool.play(tickSoundId, volume * 0.5f, volume * 0.5f, 0, 0, 1.0f)
        }
    }

    fun startSoundtrack() {
        if (!masterEnabled) return
        try {
            if (soundtrackPlayer?.isPlaying != true) {
                soundtrackPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    fun stopSoundtrack() {
        try {
            if (soundtrackPlayer?.isPlaying == true) {
                soundtrackPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    // Interactive Audio Previews
    fun previewCrash() = playCrash()

    fun previewEngine() {
        val pool = soundPool ?: return
        if (engineSoundId > 0 && loadedSounds[engineSoundId] == true) {
            pool.play(engineSoundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun previewCashout() = playCashout()
    fun previewPinkSiren() = playPinkSiren()

    // Synthesis helpers for ancillary casino cues
    private fun writeCashoutWav(file: File) {
        val durationSec = 0.55
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = Math.exp(-6.5 * t)
            val s1 = sin(2.0 * PI * 880.0 * t)
            val s2 = sin(2.0 * PI * 1320.0 * t)
            val s3 = sin(2.0 * PI * 1760.0 * t)
            val sample = ((s1 * 0.5 + s2 * 0.35 + s3 * 0.15) * env * 28000.0).toInt().coerceIn(-32767, 32767)
            pcm[i] = sample.toShort()
        }
        writeWavFile(file, pcm, SAMPLE_RATE)
    }

    private fun writePinkWav(file: File) {
        val durationSec = 1.0
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ShortArray(numSamples)
        val freqs = doubleArrayOf(587.33, 739.99, 880.00, 1174.66)
        val noteSamples = numSamples / freqs.size
        for (i in 0 until numSamples) {
            val noteIdx = (i / noteSamples).coerceAtMost(freqs.size - 1)
            val noteTime = (i % noteSamples).toDouble() / SAMPLE_RATE
            val f = freqs[noteIdx]
            val env = (1.0 - Math.exp(-30.0 * noteTime)) * Math.exp(-2.0 * noteTime)
            val sample = (sin(2.0 * PI * f * noteTime) * env * 26000.0).toInt().coerceIn(-32767, 32767)
            pcm[i] = sample.toShort()
        }
        writeWavFile(file, pcm, SAMPLE_RATE)
    }

    private fun writeTickWav(file: File) {
        val durationSec = 0.06
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = Math.exp(-45.0 * t)
            val sample = (sin(2.0 * PI * 1200.0 * t) * env * 22000.0).toInt().coerceIn(-32767, 32767)
            pcm[i] = sample.toShort()
        }
        writeWavFile(file, pcm, SAMPLE_RATE)
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val numChannels = 1
        val byteRate = sampleRate * numChannels * 2
        val dataSize = pcmData.size * 2
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            out.write("RIFF".toByteArray())
            out.write(intToByteArray(totalSize))
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(intToByteArray(16))
            out.write(shortToByteArray(1))
            out.write(shortToByteArray(numChannels.toShort()))
            out.write(intToByteArray(sampleRate))
            out.write(intToByteArray(byteRate))
            out.write(shortToByteArray(2))
            out.write(shortToByteArray(16))
            out.write("data".toByteArray())
            out.write(intToByteArray(dataSize))

            val buffer = ByteArray(pcmData.size * 2)
            for (i in pcmData.indices) {
                val s = pcmData[i].toInt()
                buffer[i * 2] = (s and 0xFF).toByte()
                buffer[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
            }
            out.write(buffer)
        }
    }

    private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToByteArray(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() shr 8) and 0xFF).toByte()
    )

    fun release() {
        stopFlightEngine()
        stopSoundtrack()
        try {
            soundtrackPlayer?.release()
            soundtrackPlayer = null
        } catch (_: Exception) {}
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }
}
