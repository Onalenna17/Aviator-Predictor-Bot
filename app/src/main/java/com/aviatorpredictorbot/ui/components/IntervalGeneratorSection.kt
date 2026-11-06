package com.aviatorpredictorbot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.IntervalAnalysisResult
import com.aviatorpredictorbot.ui.theme.CrimsonBorder
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun IntervalGeneratorSection(
    crashTimes: List<String>,
    selectedCrashTime: String,
    crashMultiplier: String,
    intervalResult: IntervalAnalysisResult?,
    errorMessage: String?,
    onSelectCrashTime: (String) -> Unit,
    onCrashMultiplierChange: (String) -> Unit,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val containerShape = RoundedCornerShape(24.dp)
    val inputShape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF170304),
                        Color(0xFF090202)
                    )
                )
            )
            .border(1.5.dp, Color(0xFF8C1026), containerShape)
            .padding(20.dp)
            .testTag("interval_generator_section")
    ) {
        Text(
            text = "BETWAY AVIATOR CRASH TIME (PINK 10x+)",
            color = CrimsonLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
                .background(Color(0xFF050202))
                .border(1.5.dp, Color(0xFF5B111E), inputShape)
                .clickable { expandedDropdown = true }
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .testTag("crash_time_dropdown_trigger")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedCrashTime.ifBlank {
                        if (crashTimes.isEmpty()) "No verified crash times available" else "Select a verified pink crash time"
                    },
                    color = if (selectedCrashTime.isNotBlank()) CrimsonLight else TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Time",
                    tint = CrimsonLight
                )
            }

            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier
                    .background(Color(0xFF140508))
                    .border(1.dp, CrimsonBorder)
            ) {
                if (crashTimes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No verified pink crash times available", color = TextMuted) },
                        onClick = { expandedDropdown = false }
                    )
                } else {
                    crashTimes.forEach { time ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "★ $time (Pink 10x+)",
                                    color = PinkMultiplier,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                onSelectCrashTime(time)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CRASH MULTIPLIER",
            color = CrimsonLight,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = crashMultiplier,
            onValueChange = onCrashMultiplierChange,
            placeholder = {
                Text(
                    text = "e.g. 14.50",
                    color = Color(0xFF888888),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            shape = inputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF050202),
                unfocusedContainerColor = Color(0xFF050202),
                focusedBorderColor = CrimsonLight,
                unfocusedBorderColor = Color(0xFF5B111E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crash_mult_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onGenerateClick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF50031),
                            Color(0xFFFF123B)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("generate_intervals_button")
        ) {
            Text(
                text = "GENERATE SPRIBE INTERVALS",
                color = Color(0xFF080000),
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF26050C))
                    .border(1.dp, Color(0xFF7A1428), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("interval_error_box")
            ) {
                Text(
                    text = errorMessage,
                    color = CrimsonLight,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        AnimatedVisibility(
            visible = intervalResult != null,
            enter = fadeIn() + slideInVertically()
        ) {
            intervalResult?.let { result ->
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF140407))
                        .border(1.dp, Color(0xFF7A1428), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("interval_result_display")
                ) {
                    Text(
                        text = "VERIFIED BASE: ${result.baseTime} · ${String.format(Locale.US, "%.2f", result.baseMultiplier)}x",
                        color = PinkMultiplier,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    result.windows.forEach { win ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF21060D))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = win.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = win.relativeMinutes,
                                    color = CrimsonLight,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Target Time: ${win.targetTimeRange}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tier: ${win.probabilityTier} — ${win.description}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.analysisNotes,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
