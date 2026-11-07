package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.aviatorpredictorbot.ui.theme.LabelGrey
import com.aviatorpredictorbot.ui.theme.DarkField
import com.aviatorpredictorbot.ui.theme.DarkFieldBorder
import com.aviatorpredictorbot.ui.theme.LowBlue
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
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
                color = LabelGrey,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Latest first; show the most recent rounds, wrapping onto a second line and centred.
        val displayList = history.reversed().take(7)

        FlowRow(
            modifier = modifier
                .fillMaxWidth()
                .testTag("history_pills_container"),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            displayList.forEach { record ->
                val pillShape = RoundedCornerShape(50)
                val textColor = if (record.multiplier >= 2.0) NeonCrimson else LowBlue
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(DarkField)
                        .border(
                            width = if (record.isPredictedHit) 1.5.dp else 1.dp,
                            color = if (record.isPredictedHit) Color(0xFFD9A400) else DarkFieldBorder,
                            shape = pillShape
                        )
                        .clickable { onPillClick(record) }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${if (record.isPredictedHit) "★ " else ""}${String.format(Locale.US, "%.2f", record.multiplier)}x",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
