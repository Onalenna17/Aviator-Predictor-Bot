package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.ui.theme.InkDark
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

/** "NEXT 07:38:40 (+9m) · 9.47x" line shown under the wheel. Shows "Waiting" below 1.50x. */
@Composable
fun NextRoundLine(
    predictedNext: PredictedNextRound?,
    modifier: Modifier = Modifier
) {
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    val text = if (predictedNext == null) {
        "NEXT · awaiting signal"
    } else {
        val time = predictedNext.nextRoundTime
        val target = targetMillis(time, nowMs)
        val relative = if (target != null) {
            val seconds = ((target - nowMs) / 1000L).coerceAtLeast(0L)
            if (seconds >= 60L) " (+${seconds / 60L}m)" else " (+${seconds}s)"
        } else {
            ""
        }
        val multText = if (predictedNext.predictedMultiplier < 1.50) {
            "Waiting"
        } else {
            String.format(Locale.US, "%.2fx", predictedNext.predictedMultiplier)
        }
        "NEXT ${time.uppercase()}$relative - $multText"
    }

    Text(
        text = text,
        color = InkDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .fillMaxWidth()
            .testTag("next_round_line")
    )
}

/** Parses "HH:mm:ss" as today's time (rolling to tomorrow if it is far in the past); null if not a time. */
private fun targetMillis(time: String, now: Long): Long? {
    val parts = time.trim().split(":")
    if (parts.size != 3) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val s = parts[2].toIntOrNull() ?: return null
    val cal = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, s)
        set(Calendar.MILLISECOND, 0)
    }
    var t = cal.timeInMillis
    if (t < now - 12L * 3600_000L) t += 24L * 3600_000L
    return t
}
