package com.aviatorpredictorbot.model

data class MultiplierRecord(
    val id: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "",
    val roundId: String = ""
) {
    val isPink: Boolean get() = multiplier >= 10.0
    val isHigh5x: Boolean get() = multiplier >= 5.0
    val isMid: Boolean get() = multiplier >= 2.0 && multiplier < 10.0
    val isLow: Boolean get() = multiplier < 2.0
}

data class HighMultiplierFrequencyStats(
    val totalSampled: Int = 0,
    val countAbove5x: Int = 0,
    val percentageAbove5x: Double = 0.0,
    val currentStreakSinceLast5x: Int = 0,
    val averageSpacing: Double = 0.0,
    val recent50Crashes: List<MultiplierRecord> = emptyList(),
    val patternStatus: String = "Monitoring",
    val patternAdvice: String = ""
)

data class DatabaseRetentionConfig(
    val retentionDays: Int = 7,
    val autoCleanupEnabled: Boolean = true,
    val lastCleanupTime: String = "Active",
    val totalPrunedRecords: Int = 0
)

data class PredictedNextRound(
    val predictedMultiplier: Double,
    val safeCashoutTarget: String,
    val targetZone: String,
    val confidencePercentage: Int,
    val nextRoundId: String,
    val nextRoundTime: String,
    val trendReasoning: String
)

data class AviatorFeedResponse(
    val currentMultiplier: Double? = null,
    val history: List<Double>? = null,
    val nextRoundTime: String? = null,
    val trainMessage: String? = null,
    val crashTimes: List<String>? = null,
    val roundId: String? = null,
    val phase: String? = null,
    val rawJson: String = ""
)

data class FeedConnectionState(
    val isConnected: Boolean = true,
    val isConnecting: Boolean = false,
    val lastUpdated: String = "LIVE",
    val errorMessage: String? = null,
    val endpointUrl: String = "https://api.betway.com/aviator/v1/telemetry/live",
    val isSimulated: Boolean = false,
    val pollIntervalSeconds: Int = 2,
    val isAutoConnected: Boolean = true
)

data class ThresholdNotification(
    val isVisible: Boolean = false,
    val multiplier: Double = 0.0,
    val roundId: String = "",
    val message: String = ""
)

data class LongTermStats(
    val totalRounds: Int = 0,
    val pinkCount: Int = 0,
    val pinkPercentage: Double = 0.0,
    val averageMultiplier: Double = 0.0,
    val maxMultiplier: Double = 0.0
)

data class IntervalWindow(
    val label: String,
    val targetTimeRange: String,
    val relativeMinutes: String,
    val probabilityTier: String,
    val description: String
)

data class IntervalAnalysisResult(
    val baseTime: String,
    val baseMultiplier: Double,
    val windows: List<IntervalWindow>,
    val pinkFrequencyAvg: Double,
    val analysisNotes: String
)

data class PredictionInsight(
    val safeCashoutRange: String,
    val moderateRange: String,
    val pinkTargetChance: String,
    val roundsSinceLastPink: Int,
    val currentTrend: String,
    val adviceMessage: String,
    val timestamp: String,
    val provablyFairHash: String = ""
)
