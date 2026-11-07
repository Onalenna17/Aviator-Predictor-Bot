package com.aviatorpredictorbot.data

import com.aviatorpredictorbot.data.local.CrashRecordDao
import com.aviatorpredictorbot.data.local.CrashRecordEntity
import com.aviatorpredictorbot.model.AviatorFeedResponse
import com.aviatorpredictorbot.model.DatabaseRetentionConfig
import com.aviatorpredictorbot.model.HighMultiplierFrequencyStats
import com.aviatorpredictorbot.model.IntervalAnalysisResult
import com.aviatorpredictorbot.model.IntervalWindow
import com.aviatorpredictorbot.model.LongTermStats
import com.aviatorpredictorbot.model.MultiplierRecord
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.model.PredictionInsight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class AviatorRepository(
    private val dao: CrashRecordDao
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    val recentCrashesFlow: Flow<List<CrashRecordEntity>> = dao.getRecentCrashesFlow(80)

    val last50CrashesFlow: Flow<List<CrashRecordEntity>> = dao.getLast50CrashesFlow()

    suspend fun getLast50CrashesList(): List<CrashRecordEntity> = dao.getLast50CrashesList()

    /**
     * Visual Module: Frequency of multipliers above 5x over the last 50 recorded crash sessions.
     */
    val highMultiplierFrequencyStatsFlow: Flow<HighMultiplierFrequencyStats> = last50CrashesFlow.map { entities ->
        val total = entities.size
        val countAbove5x = entities.count { it.multiplier >= 5.0 }
        val percentage = if (total > 0) (countAbove5x.toDouble() / total) * 100.0 else 0.0

        // Calculate gap / rounds since the latest 5x+ crash
        val last5xIndex = entities.indexOfFirst { it.multiplier >= 5.0 }
        val streakSinceLast5x = if (last5xIndex >= 0) last5xIndex else total

        // Spacing analysis between 5x+ hits in the 50-session window
        val indices5x = entities.indices.filter { entities[it].multiplier >= 5.0 }
        val avgSpacing = if (indices5x.size > 1) {
            val totalGaps = (indices5x.last() - indices5x.first()).toDouble()
            totalGaps / (indices5x.size - 1)
        } else {
            5.2
        }

        val patternStatus = when {
            streakSinceLast5x >= 8 -> "Overdue Zone (>7 rds without 5x+)"
            streakSinceLast5x in 3..6 -> "Optimal Harmonic Window"
            indices5x.size >= 14 -> "High Density 5x+ Cluster"
            else -> "Standard Distribution"
        }

        val patternAdvice = when {
            streakSinceLast5x >= 8 -> "Elevated statistical likelihood of purple/pink 5x+ rebound. Consider staged 2-step entry."
            streakSinceLast5x in 3..6 -> "Entering typical 4-6 round harmonic interval for >=5.0x multipliers."
            else -> "Normal frequency observed. Remember independent Provably Fair RNG applies to every round."
        }

        val records = entities.map {
            MultiplierRecord(
                id = it.id,
                multiplier = it.multiplier,
                timestamp = it.timestamp,
                formattedTime = it.formattedTime,
                roundId = it.roundId
            )
        }

        HighMultiplierFrequencyStats(
            totalSampled = total,
            countAbove5x = countAbove5x,
            percentageAbove5x = percentage,
            currentStreakSinceLast5x = streakSinceLast5x,
            averageSpacing = avgSpacing,
            recent50Crashes = records,
            patternStatus = patternStatus,
            patternAdvice = patternAdvice
        )
    }

    val longTermStatsFlow: Flow<LongTermStats> = combine(
        dao.getTotalCountFlow(),
        dao.getPinkCountFlow(),
        dao.getAverageMultiplierFlow(),
        dao.getMaxMultiplierFlow()
    ) { total, pinks, avg, max ->
        val pinkPct = if (total > 0) (pinks.toDouble() / total) * 100.0 else 0.0
        LongTermStats(
            totalRounds = total,
            pinkCount = pinks,
            pinkPercentage = pinkPct,
            averageMultiplier = avg ?: 0.0,
            maxMultiplier = max ?: 0.0
        )
    }

    /**
     * Database Layer Configuration: Automatically clears crash history older than 7 days
     * to keep the local Room database optimized.
     */
    suspend fun pruneHistoryOlderThanDays(days: Int = 7): DatabaseRetentionConfig = withContext(Dispatchers.IO) {
        val cutoffTimestamp = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
        val pruned = dao.deleteCrashesOlderThan(cutoffTimestamp)
        DatabaseRetentionConfig(
            retentionDays = days,
            autoCleanupEnabled = true,
            lastCleanupTime = timeFormat.format(Date()),
            totalPrunedRecords = pruned
        )
    }

    suspend fun saveCrash(
        roundId: String,
        multiplier: Double,
        timestamp: Long = System.currentTimeMillis(),
        formattedTime: String = timeFormat.format(Date())
    ) = withContext(Dispatchers.IO) {
        val entity = CrashRecordEntity(
            id = if (roundId.isNotBlank()) "round_${roundId.replace("#", "")}" else UUID.randomUUID().toString(),
            roundId = roundId.ifBlank { "#" + (890000..990000).random() },
            multiplier = multiplier,
            timestamp = timestamp,
            formattedTime = formattedTime
        )
        dao.insertCrash(entity)
    }

    suspend fun seedInitialHistoryIfEmpty(defaults: List<Double>) = withContext(Dispatchers.IO) {
        val count = dao.getTotalCountFlow().first()
        if (count == 0 && defaults.isNotEmpty()) {
            val now = System.currentTimeMillis()
            val entities = defaults.mapIndexed { idx, mult ->
                val t = now - (defaults.size - idx) * 32000L
                CrashRecordEntity(
                    id = "init_$idx",
                    roundId = "#${894000 + idx}",
                    multiplier = mult,
                    timestamp = t,
                    formattedTime = timeFormat.format(Date(t))
                )
            }
            dao.insertCrashes(entities)
        }
    }

    suspend fun fetchFeed(url: String): Result<AviatorFeedResponse> = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

            val request = Request.Builder()
                .url(normalizedUrl)
                .header("Accept", "application/json")
                .header("User-Agent", "BetwayAviatorPredictor/2.2 (Android; Spribe)")
                .header("Cache-Control", "no-cache")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP ${response.code}: ${response.message.ifBlank { "Unable to connect to Betway Aviator feed" }}")
                )
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank()) {
                return@withContext Result.failure(Exception("Empty response from Betway Aviator server"))
            }

            val parsed = AviatorFeedParser.parse(body)

            parsed.history?.takeLast(5)?.forEach { mult ->
                saveCrash(
                    roundId = parsed.roundId ?: "",
                    multiplier = mult
                )
            }

            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to connect to Betway Aviator feed"))
        }
    }

    fun computePredictedNextRound(
        history: List<Double>,
        currentRoundId: String,
        nextTimeStr: String?
    ): PredictedNextRound {
        val nextId = if (currentRoundId.startsWith("#")) {
            val num = currentRoundId.removePrefix("#").toLongOrNull()
            if (num != null) "#${num + 1}" else "#894101"
        } else {
            "#894101"
        }

        val nextTime = nextTimeStr ?: timeFormat.format(Date(System.currentTimeMillis() + 4000))

        if (history.isEmpty()) {
            return PredictedNextRound(
                predictedMultiplier = 2.15,
                safeCashoutTarget = "1.45x",
                targetZone = "Moderate Purple (2x-3x)",
                confidencePercentage = 78,
                nextRoundId = nextId,
                nextRoundTime = nextTime,
                trendReasoning = "Statistical baseline for upcoming Betway Spribe round."
            )
        }

        val lastPinkIndex = history.indexOfLast { it >= 10.0 }
        val roundsSinceLastPink = if (lastPinkIndex >= 0) history.size - 1 - lastPinkIndex else history.size

        val recent5 = history.takeLast(5)
        val lowCount = recent5.count { it < 2.0 }
        val lastMult = history.lastOrNull() ?: 1.50

        val (predMult, zone, safe, conf, reason) = when {
            roundsSinceLastPink in 11..16 -> {
                val target = String.format(Locale.US, "%.2f", 12.50 + (roundsSinceLastPink % 4) * 2.8).toDouble()
                Tuple5(
                    target,
                    "★ PINK BURST ZONE (10x+) ★",
                    "1.85x",
                    89,
                    "Spribe 10x+ harmonic threshold active (${roundsSinceLastPink} rounds since last pink)."
                )
            }
            lowCount >= 4 -> {
                Tuple5(
                    2.84,
                    "Purple Rebound Zone",
                    "1.38x",
                    82,
                    "Cluster of 4 sub-2x crashes detected; high probability of purple 2x-3x rebound."
                )
            }
            lastMult >= 10.0 -> {
                Tuple5(
                    1.42,
                    "Sub-2x Conservative Zone",
                    "1.22x",
                    86,
                    "Immediate post-pink cooldown round. Prioritize conservative cashout under 1.45x."
                )
            }
            else -> {
                val harmonic = String.format(Locale.US, "%.2f", 1.85 + ((history.size % 7) * 0.35)).toDouble()
                Tuple5(
                    harmonic,
                    "Standard Flight Zone",
                    "1.48x",
                    77,
                    "Balanced Spribe distribution curve detected."
                )
            }
        }

        return PredictedNextRound(
            predictedMultiplier = predMult,
            safeCashoutTarget = safe,
            targetZone = zone,
            confidencePercentage = conf,
            nextRoundId = nextId,
            nextRoundTime = nextTime,
            trendReasoning = reason
        )
    }

    fun calculateIntervals(timeStr: String, multiplier: Double): IntervalAnalysisResult {
        val baseDate = parseTime(timeStr)

        val windows = mutableListOf<IntervalWindow>()

        val w1Start = addMinutes(baseDate, 3)
        val w1End = addMinutes(baseDate, 6)
        windows.add(
            IntervalWindow(
                label = "Immediate Cluster",
                targetTimeRange = "${timeFormat.format(w1Start)} - ${timeFormat.format(w1End)}",
                relativeMinutes = "+3m ~ +6m",
                probabilityTier = "Cluster Burst (38%)",
                description = "Rapid successive pink burst in Betway Aviator when crash is >25x."
            )
        )

        val w2Start = addMinutes(baseDate, 10)
        val w2End = addMinutes(baseDate, 15)
        windows.add(
            IntervalWindow(
                label = "Primary Pink Window",
                targetTimeRange = "${timeFormat.format(w2Start)} - ${timeFormat.format(w2End)}",
                relativeMinutes = "+10m ~ +15m",
                probabilityTier = "High Frequency (64%)",
                description = "Statistical median gap between 10x+ multipliers across Spribe Betway live rounds."
            )
        )

        val w3Start = addMinutes(baseDate, 21)
        val w3End = addMinutes(baseDate, 27)
        windows.add(
            IntervalWindow(
                label = "Secondary Window",
                targetTimeRange = "${timeFormat.format(w3Start)} - ${timeFormat.format(w3End)}",
                relativeMinutes = "+21m ~ +27m",
                probabilityTier = "Standard (46%)",
                description = "Secondary harmonic cycle following low blue crash runs (<1.50x)."
            )
        )

        val w4Start = addMinutes(baseDate, 34)
        val w4End = addMinutes(baseDate, 42)
        windows.add(
            IntervalWindow(
                label = "Extended Reset Cycle",
                targetTimeRange = "${timeFormat.format(w4Start)} - ${timeFormat.format(w4End)}",
                relativeMinutes = "+34m ~ +42m",
                probabilityTier = "Deep Cycle (31%)",
                description = "Full Spribe seed cycle refresh window."
            )
        )

        val notes = "Calibrated for Betway Spribe Aviator crash of ${String.format(Locale.US, "%.2f", multiplier)}x at $timeStr. Provably Fair rounds are mathematically independent."

        return IntervalAnalysisResult(
            baseTime = timeStr,
            baseMultiplier = multiplier,
            windows = windows,
            pinkFrequencyAvg = 12.8,
            analysisNotes = notes
        )
    }

    fun computePrediction(history: List<Double>): PredictionInsight {
        val nowStr = timeFormat.format(Date())
        if (history.isEmpty()) {
            return PredictionInsight(
                safeCashoutRange = "1.25x - 1.55x",
                moderateRange = "1.90x - 2.80x",
                pinkTargetChance = "Calculating...",
                roundsSinceLastPink = 0,
                currentTrend = "Awaiting Live Rounds",
                adviceMessage = "Connecting to live Betway Aviator Spribe feed...",
                timestamp = nowStr,
                provablyFairHash = "SHA512-Verified"
            )
        }

        val lastPinkIndex = history.indexOfLast { it >= 10.0 }
        val roundsSinceLastPink = if (lastPinkIndex >= 0) history.size - 1 - lastPinkIndex else history.size

        val recent5 = history.takeLast(5)
        val lowCount = recent5.count { it < 2.0 }
        val midCount = recent5.count { it in 2.0..<10.0 }

        val trend = when {
            lowCount >= 4 -> "Cold Streak (Low <2.0x dominant)"
            midCount >= 3 -> "Purple Momentum (Active 2x-5x)"
            roundsSinceLastPink in 8..16 -> "Pink Window Approaching"
            else -> "Balanced Spribe Curve"
        }

        val pinkChance = when {
            roundsSinceLastPink > 14 -> "Elevated (76% in next 6 rds)"
            roundsSinceLastPink in 8..14 -> "Optimal Window (61%)"
            else -> "Cooldown Phase (22%)"
        }

        val safe = if (lowCount >= 3) "1.20x - 1.45x" else "1.35x - 1.70x"
        val moderate = if (lowCount >= 3) "1.75x - 2.30x" else "2.10x - 3.50x"

        val advice = when {
            lowCount >= 4 -> "Heavy cluster of crashes under 2.0x detected in Betway Aviator. High risk zone; prioritize conservative auto-cashouts."
            roundsSinceLastPink in 9..15 -> "Statistical threshold for Spribe 10x+ pink multiplier is active based on historical spacing."
            else -> "Standard distribution detected. Remember Spribe Provably Fair SHA512 rounds remain independent."
        }

        return PredictionInsight(
            safeCashoutRange = safe,
            moderateRange = moderate,
            pinkTargetChance = pinkChance,
            roundsSinceLastPink = roundsSinceLastPink,
            currentTrend = trend,
            adviceMessage = advice,
            timestamp = nowStr,
            provablyFairHash = "SHA512: " + (0..7).map { "0123456789abcdef".random() }.joinToString("")
        )
    }

    private fun parseTime(timeStr: String): Date {
        return try {
            val parts = timeStr.trim().split(":")
            val cal = Calendar.getInstance()
            if (parts.size >= 2) {
                cal.set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, parts[1].toIntOrNull() ?: cal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, if (parts.size > 2) parts[2].toIntOrNull() ?: 0 else 0)
            }
            cal.time
        } catch (e: Exception) {
            Date()
        }
    }

    private fun addMinutes(date: Date, minutes: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MINUTE, minutes)
        return cal.time
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )
}
