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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aviatorpredictorbot.model.IntervalAnalysisResult
import com.aviatorpredictorbot.ui.theme.ButtonRed
import com.aviatorpredictorbot.ui.theme.DarkField
import com.aviatorpredictorbot.ui.theme.DarkFieldBorder
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import com.aviatorpredictorbot.ui.theme.LabelGrey
import com.aviatorpredictorbot.ui.theme.LightBorder
import com.aviatorpredictorbot.ui.theme.LightCard
import com.aviatorpredictorbot.ui.theme.SignalRed
import com.aviatorpredictorbot.ui.theme.WaitingGrey
import java.util.Locale

internal val LightFieldShape = RoundedCornerShape(16.dp)
internal val LightCardShape = RoundedCornerShape(24.dp)

@Composable
internal fun lightFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LightCard,
    unfocusedContainerColor = LightCard,
    focusedBorderColor = InkDark,
    unfocusedBorderColor = LightBorder,
    focusedTextColor = InkDark,
    unfocusedTextColor = InkDark,
    cursorColor = InkDark
)

@Composable
internal fun LightFieldLabel(text: String) {
    Text(
        text = text,
        color = LabelGrey,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp
    )
}

@Composable
internal fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkField,
    unfocusedContainerColor = DarkField,
    focusedBorderColor = ButtonRed,
    unfocusedBorderColor = DarkFieldBorder,
    focusedTextColor = NeonCrimson,
    unfocusedTextColor = NeonCrimson,
    cursorColor = NeonCrimson
)

@Composable
internal fun RedFieldLabel(text: String) {
    Text(
        text = text,
        color = ButtonRed,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    )
}

/** "20:37:43" -> "8:37:43 PM" (display only; the stored value stays HH:mm:ss). */
internal fun to12Hour(time: String): String {
    val parts = time.trim().split(":")
    if (parts.size != 3) return time
    val h = parts[0].toIntOrNull() ?: return time
    if (h !in 0..23) return time
    val suffix = if (h >= 12) "PM" else "AM"
    val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
    return "$h12:${parts[1]}:${parts[2]} $suffix"
}

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(LightCardShape)
            .background(Color(0xFFFAFAFC))
            .border(1.5.dp, ButtonRed, LightCardShape)
            .padding(14.dp)
            .testTag("interval_generator_section")
    ) {
        RedFieldLabel("CRASH TIME (PINK 10x+) HH:MM:SS")

        Spacer(modifier = Modifier.height(10.dp))

        // Time field: tap to pick one of the verified pink crash times
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LightFieldShape)
                .background(DarkField)
                .border(1.dp, DarkFieldBorder, LightFieldShape)
                .clickable { expandedDropdown = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("crash_time_dropdown_trigger")
        ) {
            Text(
                text = if (selectedCrashTime.isNotBlank()) to12Hour(selectedCrashTime) else "Select crash time",
                color = if (selectedCrashTime.isNotBlank()) NeonCrimson else WaitingGrey,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select crash time",
                tint = NeonCrimson,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterEnd)
            )

            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier
                    .background(DarkField)
                    .border(1.dp, DarkFieldBorder)
            ) {
                if (crashTimes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No verified pink crash times available", color = WaitingGrey) },
                        onClick = { expandedDropdown = false }
                    )
                } else {
                    crashTimes.forEach { time ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "★ ${to12Hour(time)} (Pink 10x+)",
                                    color = NeonCrimson,
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

        RedFieldLabel("CRASH MULT")

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = crashMultiplier,
            onValueChange = onCrashMultiplierChange,
            placeholder = {
                Text(
                    text = "e.g. 12.50",
                    color = WaitingGrey,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center, color = NeonCrimson),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            shape = LightFieldShape,
            colors = darkFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("crash_mult_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onGenerateClick()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonRed,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("generate_intervals_button")
        ) {
            Text(
                text = "GENERATE INTERVALS",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.sp
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF0F0))
                    .border(1.dp, Color(0xFFFFC9C9), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("interval_error_box")
            ) {
                Text(
                    text = errorMessage,
                    color = SignalRed,
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
                        .background(Color(0xFFF7F7F9))
                        .border(1.dp, LightBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("interval_result_display")
                ) {
                    Text(
                        text = "VERIFIED BASE: ${result.baseTime} · ${String.format(Locale.US, "%.2f", result.baseMultiplier)}x",
                        color = SignalRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    result.windows.forEach { win ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightCard)
                                .border(1.dp, LightBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = win.label,
                                    color = InkDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = win.relativeMinutes,
                                    color = SignalRed,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Target Time: ${win.targetTimeRange}",
                                color = LabelGrey,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Tier: ${win.probabilityTier} — ${win.description}",
                                color = WaitingGrey,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result.analysisNotes,
                        color = LabelGrey,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
