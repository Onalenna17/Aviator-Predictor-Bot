package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.model.SafeProbabilityStrength
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.LabelGrey
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

/** "NEXT 07:38:40 (+9m) · 9.47x" line shown under the wheel, with Safe % Probability Strength. */
@Composable
fun NextRoundLine(
    predictedNext: PredictedNextRound?,
    safeProbabilityStrength: SafeProbabilityStrength? = null,
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

    val prob = safeProbabilityStrength ?: predictedNext?.safeProbabilityStrength
    val probColor = if (prob != null) {
        if (prob.percentage >= 75) Color(0xFF047857) else if (prob.percentage >= 60) Color(0xFF1D4ED8) else Color(0xFFBE123C)
    } else Color(0xFF047857)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = InkDark,
            fontFamily = com.aviatorpredictorbot.ui.theme.InterFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.testTag("next_round_line")
        )

        if (prob != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("next_round_safe_prob_chip")
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(probColor.copy(alpha = 0.14f))
                        .border(1.dp, probColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "SAFE STRENGTH: ${prob.percentage}%",
                        color = probColor,
                        fontFamily = com.aviatorpredictorbot.ui.theme.InterFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "· Auto-Stop: ${String.format(Locale.US, "%.2fx", prob.targetMultiplier)}",
                    color = Color(0xFF334155),
                    fontFamily = com.aviatorpredictorbot.ui.theme.InterFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
