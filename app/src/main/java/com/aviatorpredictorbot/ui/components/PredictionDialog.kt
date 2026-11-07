package com.aviatorpredictorbot.ui.components

import com.aviatorpredictorbot.ui.theme.InkDark
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aviatorpredictorbot.model.PredictionInsight
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary

@Composable
fun PredictionDialog(
    insight: PredictionInsight?,
    onDismiss: () -> Unit
) {
    if (insight == null) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3F4F7)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFE2E2E7), RoundedCornerShape(24.dp))
                .testTag("prediction_insight_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BETWAY AVIATOR PREDICTION",
                        color = CrimsonLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = insight.timestamp,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                PredictionTierCard(
                    title = "SAFE CASHOUT TARGET (BLUE/PURPLE)",
                    multiplierRange = insight.safeCashoutRange,
                    color = Color(0xFF1E9E5A),
                    badge = "Low Risk · Auto Cashout"
                )

                Spacer(modifier = Modifier.height(10.dp))

                PredictionTierCard(
                    title = "MODERATE TARGET (PURPLE ZONE)",
                    multiplierRange = insight.moderateRange,
                    color = Color(0xFF1565C0),
                    badge = "Medium Risk · 2x-3.5x"
                )

                Spacer(modifier = Modifier.height(10.dp))

                PredictionTierCard(
                    title = "10x+ SPRIBE PINK BURST",
                    multiplierRange = insight.pinkTargetChance,
                    color = PinkMultiplier,
                    badge = "${insight.roundsSinceLastPink} rounds since last 10x+"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F7))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "SPRIBE PATTERN: ${insight.currentTrend}",
                            color = CrimsonLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = insight.adviceMessage,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Spribe Aviator uses cryptographic Provably Fair RNG (Server Seed + 3 Player Seeds generated per round). Historical rounds cannot alter independent random probability.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_prediction_dialog_button")
                ) {
                    Text(
                        text = "ACKNOWLEDGE & CLOSE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionTierCard(
    title: String,
    multiplierRange: String,
    color: Color,
    badge: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3F4F7))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = badge,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = multiplierRange,
            color = InkDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}
