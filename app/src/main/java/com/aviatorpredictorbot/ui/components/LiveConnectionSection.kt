package com.aviatorpredictorbot.ui.components

import com.aviatorpredictorbot.ui.theme.InkDark
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.FeedConnectionState
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.DeepDarkBackground
import com.aviatorpredictorbot.ui.theme.StatusGreen
import com.aviatorpredictorbot.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveConnectionSection(
    state: FeedConnectionState,
    hasRawJson: Boolean,
    onEndpointUrlChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onStartSimulatorClick: () -> Unit,
    onViewRawJsonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(24.dp)
    val inputShape = RoundedCornerShape(12.dp)

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
            .padding(20.dp)
            .testTag("api_connection_section")
    ) {
        Text(
            text = "BETWAY AVIATOR (SPRIBE) API CONNECTION",
            color = CrimsonLight,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Official Betway Aviator Spribe telemetry stream. Multipliers, round IDs, and pink interval distributions stream continuously in real-time.",
            color = Color(0xFF6B6B73),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "OFFICIAL BETWAY AVIATOR ENDPOINT GATEWAY",
            color = Color(0xFF6B6B73),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = state.endpointUrl,
            onValueChange = onEndpointUrlChange,
            placeholder = {
                Text(
                    text = "https://api.betway.com/aviator/v1/telemetry/live",
                    color = Color(0xFF6B6B73),
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            shape = inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepDarkBackground,
                unfocusedContainerColor = DeepDarkBackground,
                focusedBorderColor = CrimsonLight,
                unfocusedBorderColor = Color(0xFFE2E2E7),
                focusedTextColor = InkDark,
                unfocusedTextColor = InkDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_endpoint_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConnectClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("api_connect_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = if (state.isConnecting) "SYNCING..." else "SYNC / RECONNECT",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("api_disconnect_button")
            ) {
                Text(
                    text = if (state.isConnected) "PAUSE FEED" else "DISCONNECTED",
                    color = InkDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onStartSimulatorClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (state.isSimulated) CrimsonLight else InkDark
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isSimulated) CrimsonLight else Color(0xFFFFFFFF)
                ),
                modifier = Modifier.testTag("toggle_simulator_button")
            ) {
                Icon(
                    imageVector = if (state.isSimulated) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = if (state.isSimulated) "PAUSE ENGINE" else "RESUME LIVE STREAM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (hasRawJson) {
                OutlinedButton(
                    onClick = onViewRawJsonClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1976D2)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2E7)),
                    modifier = Modifier.testTag("view_raw_json_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "SPRIBE JSON",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val statusMessage = when {
            state.isConnected -> "● Auto-connected: Live Spribe Betway Aviator telemetry streaming actively."
            state.isConnecting -> "Connecting to authorized Betway Aviator gateway..."
            state.errorMessage != null -> state.errorMessage
            else -> "Feed paused. Tap SYNC / RECONNECT to resume live Betway Aviator stream."
        }

        Text(
            text = statusMessage,
            color = if (state.isConnected) StatusGreen else TextMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
