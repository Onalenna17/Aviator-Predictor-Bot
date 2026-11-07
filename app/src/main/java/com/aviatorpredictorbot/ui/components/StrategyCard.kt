package com.aviatorpredictorbot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary

/**
 * Strategy card reproducing the official user-provided strategy directive:
 * "Strategy: If a high multiplier doesn't appear at the first time, bet at the second time for better odds."
 */
@Composable
fun StrategyCard(
    modifier: Modifier = Modifier
) {
    var expandedDetails by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(20.dp)

    val strategyAnnotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = Color(0xFFD32F4F),
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
        ) {
            append("Strategy: ")
        }
        withStyle(
            SpanStyle(
                color = Color(0xFFB3102F),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        ) {
            append("If a high multiplier doesn't appear at the first time, bet at the second time for better odds.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFFF3F4F7))
            .border(1.5.dp, Color(0xFFE2E2E7), cardShape)
            .clickable { expandedDetails = !expandedDetails }
            .padding(20.dp)
            .testTag("official_strategy_card")
    ) {
        // Main Strategy Line from screenshot
        Text(
            text = strategyAnnotated,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BETWAY AVIATOR 2-STEP ENTRY PROTOCOL",
                color = Color(0xFF99223D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = if (expandedDetails) "Hide Analysis ▲" else "Read Analysis ▼",
                color = CrimsonLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(visible = expandedDetails) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F7))
                    .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "WHY THIS STRATEGY WORKS ON SPRIBE AVIATOR:",
                    color = PinkMultiplier,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Spribe Provably Fair distributions feature cluster burst waves: when a predicted harmonic window opens but round #1 crashes low (<1.5x), round #2 has a statistically higher probability of absorption rebound.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Execution Rule: If entering on an overdue 5x+ or 10x+ signal and round 1 crashes immediately, stage entry into round 2 with a calibrated cashout target (e.g. 2.0x safe / 5.0x ceiling) before cutting exposure.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Risk Disclaimer: Independent RNG rounds carry inherent variance. Never chase past round 2.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
