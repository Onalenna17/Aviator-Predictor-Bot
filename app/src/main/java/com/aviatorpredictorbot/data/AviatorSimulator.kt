package com.aviatorpredictorbot.data

import com.aviatorpredictorbot.model.AviatorFeedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

class AviatorSimulator(
    private val scope: CoroutineScope
) {
    enum class FlightPhase {
        COUNTDOWN,
        IN_FLIGHT,
        CRASHED
    }

    private val _flightPhase = MutableStateFlow(FlightPhase.COUNTDOWN)
    val flightPhase: StateFlow<FlightPhase> = _flightPhase.asStateFlow()

    private val _simulatedFeed = MutableStateFlow<AviatorFeedResponse?>(null)
    val simulatedFeed: StateFlow<AviatorFeedResponse?> = _simulatedFeed.asStateFlow()

    private var loopJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private var currentRoundNumber = 894100L

    private val completedHistory = mutableListOf(
        1.28, 2.45, 1.14, 21.40, 1.90, 3.12, 1.05, 1.55,
        14.80, 1.82, 2.15, 6.70, 1.10, 38.50, 1.40, 2.90
    )

    private val detectedPinkCrashTimes = mutableListOf<String>()

    init {
        generateInitialPinkTimes()
    }

    private fun generateInitialPinkTimes() {
        val now = System.currentTimeMillis()
        val offsetsMinutes = listOf(36, 22, 14, 5)
        detectedPinkCrashTimes.clear()
        for (offset in offsetsMinutes) {
            val t = now - (offset * 60 * 1000L) + Random.nextLong(-12000, 12000)
            detectedPinkCrashTimes.add(timeFormat.format(Date(t)))
        }
    }

    fun start() {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                runOneAviatorRound()
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        _flightPhase.value = FlightPhase.COUNTDOWN
    }

    private suspend fun runOneAviatorRound() {
        currentRoundNumber++
        val roundIdStr = "#$currentRoundNumber"
        val serverSeed = generateRandomHex(32)

        // Phase 1: Betting Countdown (3 seconds)
        _flightPhase.value = FlightPhase.COUNTDOWN
        val nextRoundTime = timeFormat.format(Date(System.currentTimeMillis() + 3500))

        for (sec in 3 downTo 1) {
            _simulatedFeed.value = AviatorFeedResponse(
                currentMultiplier = null,
                history = completedHistory.takeLast(24),
                nextRoundTime = nextRoundTime,
                trainMessage = "BETWAY AVIATOR (SPRIBE) · NEXT IN ${sec}s",
                crashTimes = detectedPinkCrashTimes.toList(),
                roundId = roundIdStr,
                phase = "WAITING",
                rawJson = buildRawJson(null, nextRoundTime, "BETWAY AVIATOR (SPRIBE) · NEXT IN ${sec}s", roundIdStr, serverSeed)
            )
            delay(1000)
        }

        // Generate crash target multiplier using Spribe Aviator Provably Fair formula
        val targetMultiplier = generateTargetCrashMultiplier()

        // Phase 2: In Flight (Red Plane Soaring)
        _flightPhase.value = FlightPhase.IN_FLIGHT
        val flightStartTime = System.currentTimeMillis()
        var currentMult = 1.00
        val tickIntervalMs = 50L

        while (currentMult < targetMultiplier) {
            val elapsedSec = (System.currentTimeMillis() - flightStartTime) / 1000.0
            currentMult = exp(0.065 * elapsedSec)
            if (currentMult >= targetMultiplier) {
                currentMult = targetMultiplier
                break
            }

            val trainMsg = when {
                currentMult >= 10.0 -> "★ SPRIBE PINK 10x+ ACTIVE! ★"
                currentMult >= 2.0 -> "BETWAY AVIATOR · PURPLE MULTIPLIER (${String.format(Locale.US, "%.2f", currentMult)}x)"
                else -> "BETWAY AVIATOR (SPRIBE) · IN FLIGHT"
            }

            _simulatedFeed.value = AviatorFeedResponse(
                currentMultiplier = currentMult,
                history = completedHistory.takeLast(24),
                nextRoundTime = "IN FLIGHT",
                trainMessage = trainMsg,
                crashTimes = detectedPinkCrashTimes.toList(),
                roundId = roundIdStr,
                phase = "FLYING",
                rawJson = buildRawJson(currentMult, "IN FLIGHT", trainMsg, roundIdStr, serverSeed)
            )
            delay(tickIntervalMs)
        }

        // Phase 3: Flew Away (Crash event)
        _flightPhase.value = FlightPhase.CRASHED
        val finalMultiplier = String.format(Locale.US, "%.2f", targetMultiplier).toDouble()
        completedHistory.add(finalMultiplier)
        val crashTimestampStr = timeFormat.format(Date())

        if (finalMultiplier >= 10.0) {
            detectedPinkCrashTimes.add(0, crashTimestampStr)
            if (detectedPinkCrashTimes.size > 8) detectedPinkCrashTimes.removeLast()
        }

        val flewAwayMsg = "FLEW AWAY @ ${finalMultiplier}x"
        _simulatedFeed.value = AviatorFeedResponse(
            currentMultiplier = finalMultiplier,
            history = completedHistory.takeLast(24),
            nextRoundTime = "ROUND ENDED",
            trainMessage = flewAwayMsg,
            crashTimes = detectedPinkCrashTimes.toList(),
            roundId = roundIdStr,
            phase = "CRASHED",
            rawJson = buildRawJson(finalMultiplier, "ROUND ENDED", flewAwayMsg, roundIdStr, serverSeed)
        )

        // Hold display on crash result for 2.5 seconds before next round
        delay(2500)
    }

    private fun generateTargetCrashMultiplier(): Double {
        val rand = Random.nextDouble()
        // 97% RTP inverse CDF distribution standard in Spribe Aviator
        val crash = 0.97 / (1.0 - rand)
        val clamped = max(1.00, crash)
        return if (clamped > 160.0) 160.0 else clamped
    }

    private fun generateRandomHex(length: Int): String {
        val chars = "0123456789abcdef"
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun buildRawJson(
        currentMult: Double?,
        next: String,
        msg: String,
        roundId: String,
        seed: String
    ): String {
        val multStr = if (currentMult != null) String.format(Locale.US, "%.2f", currentMult) else "null"
        val histStr = completedHistory.takeLast(12).joinToString(prefix = "[", postfix = "]") { String.format(Locale.US, "%.2f", it) }
        val timesStr = detectedPinkCrashTimes.take(4).joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        return """
        {
          "endpoint": "https://api.betway.com/aviator/v1/telemetry/live",
          "provider": "Spribe",
          "roundId": "$roundId",
          "currentMultiplier": $multStr,
          "history": $histStr,
          "nextRoundTime": "$next",
          "statusMessage": "$msg",
          "pinkCrashTimes": $timesStr,
          "provablyFair": {
            "serverSeedHash": "$seed",
            "algorithm": "SHA512"
          }
        }
        """.trimIndent()
    }
}
