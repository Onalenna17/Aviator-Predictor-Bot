package com.aviatorpredictorbot

import com.aviatorpredictorbot.data.AviatorFeedParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testParseAviatorFeedJson() {
        val sampleJson = """
            {
                "game": "Betway Aviator Spribe",
                "roundId": "#894101",
                "currentMultiplier": 5.40,
                "history": [1.20, 3.40, 18.60, 2.10],
                "nextRoundTime": "14:20:00",
                "trainMessage": "BETWAY AVIATOR (SPRIBE) · LIVE FEED",
                "crashTimes": ["14:15:30", "14:02:10"]
            }
        """.trimIndent()

        val parsed = AviatorFeedParser.parse(sampleJson)
        assertEquals(5.40, parsed.currentMultiplier!!, 0.001)
        assertEquals(4, parsed.history?.size)
        assertEquals("#894101", parsed.roundId)
        assertEquals("14:20:00", parsed.nextRoundTime)
        assertEquals("BETWAY AVIATOR (SPRIBE) · LIVE FEED", parsed.trainMessage)
        assertEquals(2, parsed.crashTimes?.size)
    }

    @Test
    fun testPinkMultiplierThresholdDetection() {
        val mult = 12.80
        val isPink = mult >= 10.0
        assertTrue("Multiplier over 10x must trigger pink threshold", isPink)
    }

    @Test
    fun testFrequencyAbove5xCalculation() {
        val sample50 = listOf(
            1.20, 2.40, 5.80, 1.10, 8.20, 14.50, 1.30, 2.10, 6.40, 1.05
        )
        val count5x = sample50.count { it >= 5.0 }
        assertEquals(4, count5x)
        val pct = (count5x.toDouble() / sample50.size) * 100.0
        assertEquals(40.0, pct, 0.001)
    }

    @Test
    fun test7DayCutoffCalculation() {
        val now = 1700000000000L
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000L
        val cutoff = now - sevenDaysMs
        val oldRecordTime = now - (8L * 24 * 60 * 60 * 1000L)
        val recentRecordTime = now - (2L * 24 * 60 * 60 * 1000L)
        assertTrue(oldRecordTime < cutoff)
        assertTrue(recentRecordTime > cutoff)
    }

    @Test
    fun testGeminiStrategyTipModel() {
        val tip = com.aviatorpredictorbot.model.GeminiStrategyTip(
            tip = "High volatility detected. Staged 2-step cashouts recommended.",
            volatilityGrade = "High Volatility",
            timestampFormatted = "12:00:00",
            analyzedRoundsCount = 50,
            sampleSummary = "1.20x, 5.50x, 14.80x"
        )
        assertEquals(50, tip.analyzedRoundsCount)
        assertEquals("High Volatility", tip.volatilityGrade)
        assertTrue(tip.tip.contains("2-step"))
    }
}
