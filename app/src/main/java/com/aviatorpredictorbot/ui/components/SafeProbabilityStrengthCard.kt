package com.aviatorpredictorbot.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.PredictorRiskMode
import com.aviatorpredictorbot.model.SafeProbabilityStrength
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.InterFontFamily
import com.aviatorpredictorbot.ui.theme.JetBrainsMonoFontFamily
import com.aviatorpredictorbot.ui.theme.LabelGrey
import java.util.Locale

/**
 * Prominent visual display for Prediction Probability Strength & Safe % gauge.
 * Highlights the real-time statistical safety margin based on Betway Spribe Provably Fair curve
 * and the user's configured risk mode & auto-stop threshold.
 *
 * Designed with high-contrast, crystal-clear typography optimized for light theme readability.
 */
@Composable
fun SafeProbabilityStrengthCard(
    strength: SafeProbabilityStrength,
    riskMode: PredictorRiskMode,
    autoStopMultiplier: Double,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)

    // Select high-contrast light-theme safe status colors (WCAG AAA compliant on white backgrounds)
    val accentColor = when (riskMode) {
        PredictorRiskMode.CONSERVATIVE -> Color(0xFF047857) // Deep Emerald
        PredictorRiskMode.BALANCED -> Color(0xFF1D4ED8)     // Deep Cobalt Blue
        PredictorRiskMode.AGGRESSIVE -> Color(0xFFBE123C)   // Deep Crimson Rose
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (strength.percentage.toFloat() / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "prob_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color.White)
            .border(1.5.dp, Color(0xFFE2E8F0), cardShape)
            .padding(16.dp)
            .testTag("safe_probability_strength_card")
    ) {
        // Header row: Icon + Title + Risk Mode Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Safe Probability",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "SAFE PROBABILITY STRENGTH",
                        color = InkDark,
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Spribe Mathematical Clearance Score",
                        color = LabelGrey,
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Risk badge
            val badgeBg = when (riskMode) {
                PredictorRiskMode.CONSERVATIVE -> Color(0xFFD1FAE5)
                PredictorRiskMode.BALANCED -> Color(0xFFDBEAFE)
                PredictorRiskMode.AGGRESSIVE -> Color(0xFFFFE4E6)
            }
            val badgeBorder = when (riskMode) {
                PredictorRiskMode.CONSERVATIVE -> Color(0xFF059669)
                PredictorRiskMode.BALANCED -> Color(0xFF2563EB)
                PredictorRiskMode.AGGRESSIVE -> Color(0xFFE11D48)
            }
            val badgeTextColor = when (riskMode) {
                PredictorRiskMode.CONSERVATIVE -> Color(0xFF065F46)
                PredictorRiskMode.BALANCED -> Color(0xFF1E40AF)
                PredictorRiskMode.AGGRESSIVE -> Color(0xFF9F1239)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeBg)
                    .border(1.dp, badgeBorder, RoundedCornerShape(50))
                    .clickable { onOpenSettings() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("risk_mode_badge")
            ) {
                Text(
                    text = riskMode.shortLabel,
                    color = badgeTextColor,
                    fontFamily = InterFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Percentage Meter & Grade (high contrast font and colors)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${strength.percentage}%",
                    color = accentColor,
                    fontFamily = InterFontFamily,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("safe_percentage_display")
                )
                Text(
                    text = "SAFE",
                    color = accentColor,
                    fontFamily = InterFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = strength.grade,
                    color = accentColor,
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = strength.riskRating,
                    color = Color(0xFF1E293B),
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated Segmented Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.85f),
                                accentColor
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Key stats row: High legibility with JetBrains Mono numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricPill(
                label = "AUTO-STOP",
                value = String.format(Locale.US, "%.2fx", autoStopMultiplier),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "TARGET",
                value = String.format(Locale.US, "%.2fx", strength.targetMultiplier),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                label = "RTP BASE",
                value = "97.0%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recommendation & Settings shortcut with deep contrast text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .clickable { onOpenSettings() }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strength.recommendation,
                color = Color(0xFF0F172A),
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Adjust settings",
                tint = Color(0xFF475569),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(vertical = 7.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF334155),
            fontFamily = InterFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}
