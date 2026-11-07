package com.aviatorpredictorbot.data

import com.aviatorpredictorbot.model.AviatorFeedResponse
import java.util.regex.Pattern

object AviatorFeedParser {

    /**
     * Pure Kotlin parser for official Betway Aviator (Spribe) telemetry streams.
     */
    fun parse(jsonString: String): AviatorFeedResponse {
        val trimmed = jsonString.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw IllegalArgumentException("Invalid JSON response: must be a JSON object")
        }

        // 1. Current Multiplier
        val currentMultiplier = extractDouble(
            trimmed,
            "\"currentMultiplier\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)",
            "\"current_multiplier\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)",
            "\"multiplier\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)",
            "\"flightMultiplier\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)"
        )?.takeIf { it.isFinite() && it >= 1.0 }

        // 2. History / Completed Rounds
        val historyList = mutableListOf<Double>()
        val historyArrayStr = extractArrayBlock(trimmed, "history")
            ?: extractArrayBlock(trimmed, "completedRounds")
            ?: extractArrayBlock(trimmed, "rounds")
            ?: extractArrayBlock(trimmed, "previousCrashes")

        if (historyArrayStr != null) {
            val numPattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)")
            val matcher = numPattern.matcher(historyArrayStr)
            while (matcher.find()) {
                val d = matcher.group(1)?.toDoubleOrNull()
                if (d != null && d.isFinite() && d >= 1.0) {
                    historyList.add(d)
                }
            }
        }

        val trimmedHistory = historyList.takeLast(30)

        // 3. Next Round Time
        val nextRoundTime = extractString(
            trimmed,
            "\"nextRoundTime\"\\s*:\\s*\"([^\"]+)\"",
            "\"next_round_time\"\\s*:\\s*\"([^\"]+)\"",
            "\"countdown\"\\s*:\\s*\"([^\"]+)\""
        )

        // 4. Train / Status Message
        val trainMessage = extractString(
            trimmed,
            "\"trainMessage\"\\s*:\\s*\"([^\"]+)\"",
            "\"train_message\"\\s*:\\s*\"([^\"]+)\"",
            "\"statusMessage\"\\s*:\\s*\"([^\"]+)\"",
            "\"status\"\\s*:\\s*\"([^\"]+)\""
        )

        // 5. Crash Times
        val crashTimes = mutableListOf<String>()
        val crashTimesArrayStr = extractArrayBlock(trimmed, "crashTimes")
            ?: extractArrayBlock(trimmed, "pinkCrashTimes")
        if (crashTimesArrayStr != null) {
            val strPattern = Pattern.compile("\"([^\"]+)\"")
            val matcher = strPattern.matcher(crashTimesArrayStr)
            while (matcher.find()) {
                val timeStr = matcher.group(1)
                if (!timeStr.isNullOrBlank()) {
                    crashTimes.add(timeStr)
                }
            }
        }

        // 6. Round ID
        val roundId = extractString(
            trimmed,
            "\"roundId\"\\s*:\\s*\"([^\"]+)\"",
            "\"round_id\"\\s*:\\s*\"([^\"]+)\"",
            "\"gameId\"\\s*:\\s*\"([^\"]+)\""
        )

        // 7. Phase
        val phase = extractString(
            trimmed,
            "\"phase\"\\s*:\\s*\"([^\"]+)\"",
            "\"state\"\\s*:\\s*\"([^\"]+)\""
        )

        if (currentMultiplier == null && trimmedHistory.isEmpty()) {
            throw IllegalArgumentException("No valid Betway Aviator multiplier or completed rounds found")
        }

        return AviatorFeedResponse(
            currentMultiplier = currentMultiplier,
            history = trimmedHistory,
            nextRoundTime = nextRoundTime,
            trainMessage = trainMessage,
            crashTimes = crashTimes,
            roundId = roundId,
            phase = phase,
            rawJson = jsonString
        )
    }

    private fun extractDouble(text: String, vararg patterns: String): Double? {
        for (pat in patterns) {
            val p = Pattern.compile(pat)
            val m = p.matcher(text)
            if (m.find()) {
                val value = m.group(1)?.toDoubleOrNull()
                if (value != null) return value
            }
        }
        return null
    }

    private fun extractString(text: String, vararg patterns: String): String? {
        for (pat in patterns) {
            val p = Pattern.compile(pat)
            val m = p.matcher(text)
            if (m.find()) {
                val value = m.group(1)
                if (value != null && value.isNotBlank()) return value
            }
        }
        return null
    }

    private fun extractArrayBlock(text: String, key: String): String? {
        val keyPattern = Pattern.compile("\"$key\"\\s*:\\s*\\[")
        val matcher = keyPattern.matcher(text)
        if (!matcher.find()) return null

        val start = matcher.end() - 1
        var depth = 0
        for (i in start until text.length) {
            val c = text[i]
            if (c == '[') depth++
            else if (c == ']') {
                depth--
                if (depth == 0) {
                    return text.substring(start + 1, i)
                }
            }
        }
        return null
    }
}
