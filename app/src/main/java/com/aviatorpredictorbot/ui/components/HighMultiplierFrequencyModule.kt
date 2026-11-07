package com.aviatorpredictorbot.ui.components

import com.aviatorpredictorbot.ui.theme.InkDark
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.HighMultiplierFrequencyStats
import com.aviatorpredictorbot.model.MultiplierRecord
import com.aviatorpredictorbot.ui.theme.AccentGold
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.PurpleMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighMultiplierFrequencyModule(
    stats: HighMultiplierFrequencyStats,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(24.dp)
    var selectedRecord by remember { mutableStateOf<MultiplierRecord?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF3F4F7),
                        Color(0xFFF3F4F7)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFE2E2E7), containerShape)
            .padding(18.dp)
            .testTag("frequency_above_5x_module")
    ) {
        // Module Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "5x+ FREQUENCY (LAST 50 CRASHES)",
                    color = CrimsonLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F7))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${stats.totalSampled}/50 Sessions",
                    color = AccentGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // High-level Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrequencyMetricTile(
                title = "5x+ HIT COUNT",
                value = "${stats.countAbove5x} / ${stats.totalSampled}",
                highlightColor = AccentGold,
                modifier = Modifier.weight(1f)
            )
            FrequencyMetricTile(
                title = "FREQUENCY %",
                value = String.format(Locale.US, "%.1f%%", stats.percentageAbove5x),
                highlightColor = if (stats.percentageAbove5x >= 20.0) AccentGold else PurpleMultiplier,
                modifier = Modifier.weight(1f)
            )
            FrequencyMetricTile(
                title = "SINCE LAST 5x+",
                value = "${stats.currentStreakSinceLast5x} rds ago",
                highlightColor = if (stats.currentStreakSinceLast5x >= 7) PinkMultiplier else CrimsonLight,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pattern Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F7))
                .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PATTERN DETECTED: ",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stats.patternStatus,
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stats.patternAdvice,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 50-Session Heatmap Matrix
        Text(
            text = "CHRONOLOGICAL DISTRIBUTION MATRIX (50 SESSIONS)",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of 50 dots
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F7))
                .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(12.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            stats.recent50Crashes.forEachIndexed { idx, item ->
                val (dotColor, dotBorder) = when {
                    item.multiplier >= 10.0 -> Pair(PinkMultiplier, Color(0xFFB0004B))
                    item.multiplier >= 5.0 -> Pair(AccentGold, Color(0xFFD9A400))
                    item.multiplier >= 2.0 -> Pair(PurpleMultiplier, PurpleMultiplier.copy(alpha = 0.4f))
                    else -> Pair(CrimsonPrimary.copy(alpha = 0.6f), Color(0xFFFFFFFF))
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.dp, dotBorder, CircleShape)
                        .clickable { selectedRecord = item },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.multiplier >= 5.0) {
                        Text(
                            text = if (item.multiplier >= 10.0) "★" else "5",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Selected Item Tooltip
        AnimatedVisibility(visible = selectedRecord != null) {
            selectedRecord?.let { record ->
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF3F4F7))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected ${record.roundId}: ${String.format(Locale.US, "%.2fx", record.multiplier)} at ${record.formattedTime}",
                            color = InkDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dismiss",
                            color = CrimsonLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable { selectedRecord = null }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = AccentGold, label = "≥ 5.0x")
            LegendItem(color = PinkMultiplier, label = "≥ 10.0x Pink")
            LegendItem(color = PurpleMultiplier, label = "2.0x - 4.99x")
            LegendItem(color = CrimsonPrimary, label = "< 2.0x")
        }
    }
}

@Composable
private fun FrequencyMetricTile(
    title: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F7))
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = highlightColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}
