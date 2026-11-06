package com.aviatorpredictorbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.ui.theme.CrimsonBorder
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.StatusGreen
import com.aviatorpredictorbot.ui.theme.StatusGreenBg
import com.aviatorpredictorbot.ui.theme.StatusGreenBorder
import com.aviatorpredictorbot.ui.theme.TextSecondary

@Composable
fun StatusBanner(
    isConnected: Boolean,
    isSimulated: Boolean,
    lastUpdated: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isConnected) StatusGreenBg else Color(0xFF11090B)
    val borderColor = if (isConnected) StatusGreenBorder else CrimsonBorder

    val annotatedText = buildAnnotatedString {
        if (isConnected) {
            withStyle(
                SpanStyle(
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("CONNECTED · BETWAY AVIATOR (SPRIBE)")
            }
            append(" — Live telemetry streaming ($lastUpdated)")
        } else {
            withStyle(
                SpanStyle(
                    color = CrimsonLight,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("FEED PAUSED")
            }
            append(" — ${errorMessage ?: "Tap Sync to resume live Betway Aviator telemetry."}")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("status_indicator_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = annotatedText,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
