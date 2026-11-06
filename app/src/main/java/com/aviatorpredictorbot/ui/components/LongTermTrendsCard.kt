package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.data.local.CrashRecordEntity
import com.aviatorpredictorbot.model.DatabaseRetentionConfig
import com.aviatorpredictorbot.model.LongTermStats
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.PurpleMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun LongTermTrendsCard(
    stats: LongTermStats,
    recentCrashes: List<CrashRecordEntity>,
    retentionConfig: DatabaseRetentionConfig,
    onManualPruneClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF190408),
                        Color(0xFF0A0204)
                    )
                )
            )
            .border(1.5.dp, Color(0xFF8C1026), containerShape)
            .padding(18.dp)
            .testTag("long_term_trends_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ROOM PERSISTED TRENDS",
                color = CrimsonLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF26050C))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SQLite Local DB",
                    color = PinkMultiplier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Data persisted continuously via Room SQLite. Trends remain saved across connection refreshes.",
            color = Color(0xFFAAAAAA),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Stat Metric Tiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMetricTile(
                title = "TOTAL LOGGED",
                value = "${stats.totalRounds}",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            StatMetricTile(
                title = "PINK RATE",
                value = String.format(Locale.US, "%.1f%%", stats.pinkPercentage),
                color = PinkMultiplier,
                modifier = Modifier.weight(1f)
            )
            StatMetricTile(
                title = "AVG CRASH",
                value = String.format(Locale.US, "%.2fx", stats.averageMultiplier),
                color = PurpleMultiplier,
                modifier = Modifier.weight(1f)
            )
            StatMetricTile(
                title = "MAX PEAK",
                value = String.format(Locale.US, "%.1fx", stats.maxMultiplier),
                color = Color(0xFF35E98B),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mini Trend Sparkline Canvas (last 18 persisted rounds)
        if (recentCrashes.isNotEmpty()) {
            Text(
                text = "PERSISTED CRASH DISTRIBUTION",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val displayBars = recentCrashes.take(18).reversed()
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF050102))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val barWidth = size.width / (displayBars.size * 1.4f)
                val spacing = barWidth * 0.4f
                val maxVal = 15.0f

                displayBars.forEachIndexed { i, crash ->
                    val clamped = crash.multiplier.toFloat().coerceIn(1.0f, maxVal)
                    val barHeight = ((clamped / maxVal) * size.height).coerceAtLeast(4.dp.toPx())
                    val barColor = when {
                        crash.isPink -> PinkMultiplier
                        crash.isMid -> PurpleMultiplier
                        else -> CrimsonPrimary
                    }
                    val x = i * (barWidth + spacing)
                    val y = size.height - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Database Layer 7-Day Auto-Cleanup Configuration Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A1810))
                .border(1.dp, Color(0xFF14613B), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DATABASE AUTO-CLEANUP CONFIGURATION",
                    color = Color(0xFF35E98B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Prunes records older than ${retentionConfig.retentionDays} days automatically for peak SQLite performance.",
                    color = Color(0xFFA5D6A7),
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B4D2E))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatMetricTile(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF21050B))
            .border(1.dp, Color(0xFF450B16), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}
