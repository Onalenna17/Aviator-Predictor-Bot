package com.aviatorpredictorbot.service

import android.util.Log
import com.aviatorpredictorbot.model.GeminiStrategyTip
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiStrategyAnalyzer {

    companion object {
        private const val TAG = "GeminiStrategyAnalyzer"
        private const val MODEL_NAME = "gemini-3.8-flash"
    }

    private val model by lazy {
        Firebase.ai.generativeModel(MODEL_NAME)
    }

    suspend fun analyzeCrashVolatility(multipliers: List<Double>): GeminiStrategyTip = withContext(Dispatchers.IO) {
        val sampleSize = multipliers.take(50)
        val formattedList = sampleSize.map { String.format(Locale.US, "%.2fx", it) }
        val sampleString = formattedList.joinToString(", ")

        val prompt = buildString {
            appendLine("You are an analytical data engine for the Aviator crash game telemetry.")
            appendLine("Here are the last ${sampleSize.size} recorded crash multiplier results in chronological order:")
            appendLine("[$sampleString]")
            appendLine()
            appendLine("Analyze the observed volatility patterns, including cold streaks below 2.0x, clustering of 5x+ multipliers, and pink (>10x) intervals.")
            appendLine("Provide a professional, realistic 2-3 sentence 'Strategy Tip' focusing on discipline, bankroll preservation, and safe cashout targets.")
            appendLine("Do NOT use markdown bolding, asterisks, or bullet points.")
            appendLine("Format your response strictly as:")
            appendLine("GRADE: <2-3 words classification, e.g. Cold Compression, High Volatility Spurt, or Balanced Steady>")
            appendLine("TIP: <Concise 2-3 sentence strategy tip>")
        }

        try {
            val response = withTimeout(45_000L) {
                model.generateContent(prompt)
            }

            val text = response.text.orEmpty().trim()
            if (text.isBlank()) {
                throw IllegalStateException("Gemini returned an empty analysis.")
            }

            parseResponse(text, sampleSize.size, sampleString)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini analysis timed out", e)
            throw IllegalStateException("Gemini analysis timed out after 45 seconds.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini analysis failed", e)
            throw e
        }
    }

    private fun parseResponse(rawText: String, count: Int, sampleSummary: String): GeminiStrategyTip {
        var grade = "Observed Volatility"
        var tip = rawText

        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("GRADE:", ignoreCase = true)) {
                grade = trimmed.substringAfter(":").trim()
            } else if (trimmed.startsWith("TIP:", ignoreCase = true)) {
                tip = trimmed.substringAfter(":").trim()
            }
        }

        if (tip.isBlank()) {
            tip = rawText.replace("GRADE:", "").trim()
        }

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        return GeminiStrategyTip(
            tip = tip,
            volatilityGrade = grade.take(30),
            timestampFormatted = timeStr,
            analyzedRoundsCount = count,
            sampleSummary = sampleSummary
        )
    }
}
