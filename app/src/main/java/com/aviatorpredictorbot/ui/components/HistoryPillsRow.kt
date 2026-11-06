package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.MultiplierRecord
import com.aviatorpredictorbot.ui.theme.BlueMultiplier
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.PurpleMultiplier
import com.aviatorpredictorbot.ui.theme.RedMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import java.util.Locale

@Composable
fun HistoryPillsRow(
    history: List<MultiplierRecord>,
    modifier: Modifier = Modifier,
    onPillClick: (MultiplierRecord) -> Unit = {}
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("empty_history_text"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Connecting to Betway Aviator live multiplier feed...",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        val displayList = history.reversed()
        val scrollState = rememberScrollState()

        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 8.dp)
                .testTag("history_pills_container"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayList.forEach { record ->
                val (textColor, bgColor, borderColor) = when {
                    record.isPink -> Triple(
                        PinkMultiplier,
                        Color(0xFF33061A),
                        PinkMultiplier.copy(alpha = 0.6f)
                    )
                    record.isMid -> Triple(
                        PurpleMultiplier,
                        Color(0xFF23082E),
                        PurpleMultiplier.copy(alpha = 0.5f)
                    )
                    else -> Triple(
                        RedMultiplier,
                        Color(0xFF24050B),
                        Color(0xFF420B16)
                    )
                }

                val pillShape = RoundedCornerShape(22.dp)

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(bgColor)
                        .border(1.dp, borderColor, pillShape)
                        .clickable { onPillClick(record) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val prefix = if (record.isPink) "★ " else ""
                    Text(
                        text = "$prefix${String.format(Locale.US, "%.2f", record.multiplier)}x",
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
