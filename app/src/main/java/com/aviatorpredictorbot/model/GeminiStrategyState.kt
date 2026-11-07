package com.aviatorpredictorbot.model

data class GeminiStrategyTip(
    val tip: String,
    val volatilityGrade: String,
    val timestampFormatted: String,
    val analyzedRoundsCount: Int,
    val sampleSummary: String
)

sealed interface GeminiStrategyState {
    data object Idle : GeminiStrategyState
    data object Analyzing : GeminiStrategyState
    data class Success(val tip: GeminiStrategyTip) : GeminiStrategyState
    data class Error(val message: String) : GeminiStrategyState
}
