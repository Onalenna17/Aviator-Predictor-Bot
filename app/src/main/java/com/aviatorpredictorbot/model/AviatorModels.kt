package com.aviatorpredictorbot.model

enum class PredictorRiskMode(
    val title: String,
    val shortLabel: String,
    val subtitle: String,
    val baseTargetMultiplier: Double,
    val defaultAutoStop: Double,
    val expectedSafePercentageRange: String
) {
    CONSERVATIVE(
        title = "Conservative (Low Risk)",
        shortLabel = "LOW RISK",
        subtitle = "Capital preservation · High win probability (88%-96%) at sub-1.40x auto-stop",
        baseTargetMultiplier = 1.35,
        defaultAutoStop = 1.30,
        expectedSafePercentageRange = "88% - 96%"
    ),
    BALANCED(
        title = "Balanced (Standard)",
        shortLabel = "BALANCED",
        subtitle = "Optimal risk/reward · 72%-85% probability capturing 1.80x-2.80x purple cycles",
        baseTargetMultiplier = 2.15,
        defaultAutoStop = 1.80,
        expectedSafePercentageRange = "72% - 85%"
    ),
    AGGRESSIVE(
        title = "Aggressive (Pink Hunter)",
        shortLabel = "HIGH RISK",
        subtitle = "High volatility · Pink 10x+ harmonic surge timing with larger drawdown tolerance",
        baseTargetMultiplier = 10.50,
        defaultAutoStop = 5.00,
        expectedSafePercentageRange = "45% - 68%"
    )
}

data class SafeProbabilityStrength(
    val percentage: Int = 85,
    val grade: String = "STRONG",
    val statusColorHex: Long = 0xFF1E9E5A,
    val targetMultiplier: Double = 1.45,
    val riskRating: String = "LOW RISK",
    val formulaSummary: String = "Spribe 97% RTP curve + Cold streak rebound coefficient",
    val recommendation: String = "Auto-cashout aligned with low volatility zone."
)

data class PredictorSettings(
    val riskMode: PredictorRiskMode = PredictorRiskMode.BALANCED,
    val autoStopMultiplier: Double = 1.80,
    val pinkAlertThreshold: Double = 10.0,
    val streakProtectionThreshold: Int = 3,
    val soundMasterEnabled: Boolean = true,
    val soundEngineEnabled: Boolean = true,
    val soundCrashEnabled: Boolean = true,
    val soundCashoutEnabled: Boolean = true,
    val soundPinkSirenEnabled: Boolean = true,
    val soundVolume: Float = 0.85f,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

data class MultiplierRecord(
    val id: String,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "",
    val roundId: String = "",
    /** True when the predictor called this round: the prediction (>= 1.50x) was reached by the actual crash. */
    val isPredictedHit: Boolean = false
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
    val trendReasoning: String,
    val safeProbabilityStrength: SafeProbabilityStrength = SafeProbabilityStrength()
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
    val provablyFairHash: String = "",
    val safeProbabilityStrength: SafeProbabilityStrength = SafeProbabilityStrength()
)
