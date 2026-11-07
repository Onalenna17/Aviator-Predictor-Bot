package com.aviatorpredictorbot.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.aviatorpredictorbot.model.PredictorRiskMode
import com.aviatorpredictorbot.model.PredictorSettings
import com.aviatorpredictorbot.model.SafeProbabilityStrength
import com.aviatorpredictorbot.ui.theme.ButtonRed
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.LabelGrey
import com.aviatorpredictorbot.ui.theme.LightBackground
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.StatusGreen
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: PredictorSettings,
    safeProbabilityStrength: SafeProbabilityStrength,
    onBack: () -> Unit,
    onUpdateRiskMode: (PredictorRiskMode) -> Unit,
    onUpdateAutoStop: (Double) -> Unit,
    onUpdatePinkThreshold: (Double) -> Unit,
    onUpdateStreakThreshold: (Int) -> Unit,
    onUpdateSoundMaster: (Boolean) -> Unit,
    onUpdateSoundEngine: (Boolean) -> Unit,
    onUpdateSoundCrash: (Boolean) -> Unit,
    onUpdateSoundCashout: (Boolean) -> Unit,
    onUpdateSoundPinkSiren: (Boolean) -> Unit,
    onUpdateSoundVolume: (Float) -> Unit,
    onPreviewSound: (String) -> Unit,
    onUpdateHapticFeedback: (Boolean) -> Unit,
    onPreviewHaptic: () -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onSendTestNotification: () -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back navigation
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PREDICTOR SETTINGS",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Risk Profile · Auto-Stop · Betway Spribe Audio",
                            fontSize = 11.sp,
                            color = LabelGrey
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to telemetry",
                            tint = InkDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to defaults",
                            tint = LabelGrey
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightBackground
                )
            )
        },
        containerColor = LightBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: Predictor Risk Modes
                SectionHeader(
                    title = "PREDICTOR RISK MODE",
                    subtitle = "Adjust prediction algorithm aggressiveness and target zones"
                )

                RiskModeSelector(
                    selectedMode = settings.riskMode,
                    onSelectMode = onUpdateRiskMode
                )

                // SECTION 2: Auto-Stop & Thresholds
                SectionHeader(
                    title = "AUTO-STOP & THRESHOLD VALUES",
                    subtitle = "Configure automated cashout target and alert triggers"
                )

                AutoStopCard(
                    autoStopMultiplier = settings.autoStopMultiplier,
                    safeProbabilityStrength = safeProbabilityStrength,
                    riskMode = settings.riskMode,
                    onAutoStopChanged = onUpdateAutoStop
                )

                ThresholdControlsCard(
                    pinkThreshold = settings.pinkAlertThreshold,
                    streakThreshold = settings.streakProtectionThreshold,
                    onPinkThresholdChanged = onUpdatePinkThreshold,
                    onStreakThresholdChanged = onUpdateStreakThreshold
                )

                // SECTION 3: Betway Spribe Aviator Crash Game Audio
                SectionHeader(
                    title = "BETWAY SPRIBE AVIATOR GAME SOUNDS",
                    subtitle = "Real native crash game audio synthesis & individual sound triggers"
                )

                AudioSettingsCard(
                    masterEnabled = settings.soundMasterEnabled,
                    engineEnabled = settings.soundEngineEnabled,
                    crashEnabled = settings.soundCrashEnabled,
                    cashoutEnabled = settings.soundCashoutEnabled,
                    pinkSirenEnabled = settings.soundPinkSirenEnabled,
                    volume = settings.soundVolume,
                    onMasterToggled = onUpdateSoundMaster,
                    onEngineToggled = onUpdateSoundEngine,
                    onCrashToggled = onUpdateSoundCrash,
                    onCashoutToggled = onUpdateSoundCashout,
                    onPinkSirenToggled = onUpdateSoundPinkSiren,
                    onVolumeChanged = onUpdateSoundVolume,
                    onPreviewSound = onPreviewSound
                )

                // SECTION 4: Haptic Vibration Feedback
                SectionHeader(
                    title = "HAPTIC VIBRATION FEEDBACK",
                    subtitle = "Tactile vibration alerts for high-confidence predictions & pink surges"
                )

                HapticSettingsCard(
                    hapticEnabled = settings.hapticFeedbackEnabled,
                    onHapticToggled = onUpdateHapticFeedback,
                    onPreviewHaptic = onPreviewHaptic
                )

                // SECTION 5: Local Notification Alerts
                SectionHeader(
                    title = "LOCAL NOTIFICATION SYSTEM",
                    subtitle = "System alerts when a high-probability event (≥75% safe score) is predicted"
                )

                NotificationSettingsCard(
                    notificationsEnabled = settings.notificationsEnabled,
                    onNotificationsToggled = onUpdateNotifications,
                    onSendTestNotification = onSendTestNotification
                )

                // SECTION 6: Provably Fair & Safe Strength Info
                ProvablyFairInfoCard()

                // Save / Close Button
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonRed,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("settings_done_button")
                ) {
                    Text(
                        text = "APPLY & RETURN TO FLIGHT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = CrimsonPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = LabelGrey
        )
    }
}

@Composable
private fun RiskModeSelector(
    selectedMode: PredictorRiskMode,
    onSelectMode: (PredictorRiskMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PredictorRiskMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val borderColor = if (isSelected) {
                when (mode) {
                    PredictorRiskMode.CONSERVATIVE -> Color(0xFF1E9E5A)
                    PredictorRiskMode.BALANCED -> Color(0xFF2B70E0)
                    PredictorRiskMode.AGGRESSIVE -> CrimsonPrimary
                }
            } else {
                Color(0xFFE2E2E7)
            }

            val bgColor = if (isSelected) {
                when (mode) {
                    PredictorRiskMode.CONSERVATIVE -> Color(0xFFF0FDF4)
                    PredictorRiskMode.BALANCED -> Color(0xFFF0F6FF)
                    PredictorRiskMode.AGGRESSIVE -> Color(0xFFFFF1F3)
                }
            } else {
                Color(0xFFFFFFFF)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onSelectMode(mode) }
                    .testTag("risk_mode_card_${mode.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = mode.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = InkDark
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(borderColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = mode.expectedSafePercentageRange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = borderColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.subtitle,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) borderColor else Color(0xFFE2E2E7)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoStopCard(
    autoStopMultiplier: Double,
    safeProbabilityStrength: SafeProbabilityStrength,
    riskMode: PredictorRiskMode,
    onAutoStopChanged: (Double) -> Unit
) {
    val presets = listOf(1.20, 1.30, 1.50, 1.80, 2.00, 2.50, 3.00, 5.00)
    val accentColor = Color(safeProbabilityStrength.statusColorHex)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("auto_stop_settings_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET AUTO-STOP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )

                Text(
                    text = String.format(Locale.US, "%.2fx", autoStopMultiplier),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CrimsonPrimary,
                    modifier = Modifier.testTag("auto_stop_value_label")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = autoStopMultiplier.toFloat(),
                onValueChange = { onAutoStopChanged(String.format(Locale.US, "%.2f", it.toDouble()).toDouble()) },
                valueRange = 1.10f..10.00f,
                steps = 88,
                colors = SliderDefaults.colors(
                    thumbColor = CrimsonPrimary,
                    activeTrackColor = CrimsonPrimary,
                    inactiveTrackColor = Color(0xFFE2E2E7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_stop_slider")
            )

            // Preset chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.forEach { preset ->
                    val isSelected = Math.abs(autoStopMultiplier - preset) < 0.05
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CrimsonPrimary else Color(0xFFF3F4F7))
                            .clickable { onAutoStopChanged(preset) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", preset),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else InkDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEEEEF2))
            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Safe % Strength Feedback Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.10f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SAFE PROBABILITY STRENGTH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "${safeProbabilityStrength.percentage}% SAFE (${safeProbabilityStrength.grade})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                    Text(
                        text = safeProbabilityStrength.riskRating,
                        fontSize = 10.sp,
                        color = LabelGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${safeProbabilityStrength.percentage}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ThresholdControlsCard(
    pinkThreshold: Double,
    streakThreshold: Int,
    onPinkThresholdChanged: (Double) -> Unit,
    onStreakThresholdChanged: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Pink 10x alert threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "10x+ High Multiplier Siren Trigger",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Text(
                        text = "Triggers visual & siren alert when flight crosses threshold",
                        fontSize = 11.sp,
                        color = LabelGrey
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5.0, 10.0, 20.0).forEach { th ->
                        val isSel = Math.abs(pinkThreshold - th) < 0.1
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PinkMultiplier else Color(0xFFF3F4F7))
                                .clickable { onPinkThresholdChanged(th) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${th.toInt()}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else InkDark
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEF2))

            // Cold streak protection threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cold Streak Protection Threshold",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Text(
                        text = "Advises cooldown if consecutive sub-1.50x crashes occur",
                        fontSize = 11.sp,
                        color = LabelGrey
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2, 3, 4, 5).forEach { streak ->
                        val isSel = streakThreshold == streak
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CrimsonPrimary else Color(0xFFF3F4F7))
                                .clickable { onStreakThresholdChanged(streak) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$streak rds",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else InkDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSettingsCard(
    masterEnabled: Boolean,
    engineEnabled: Boolean,
    crashEnabled: Boolean,
    cashoutEnabled: Boolean,
    pinkSirenEnabled: Boolean,
    volume: Float,
    onMasterToggled: (Boolean) -> Unit,
    onEngineToggled: (Boolean) -> Unit,
    onCrashToggled: (Boolean) -> Unit,
    onCashoutToggled: (Boolean) -> Unit,
    onPinkSirenToggled: (Boolean) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onPreviewSound: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("audio_settings_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (masterEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Master sound",
                        tint = if (masterEnabled) CrimsonPrimary else LabelGrey
                    )
                    Column {
                        Text(
                            text = "MASTER GAME AUDIO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkDark
                        )
                        Text(
                            text = "Native Betway Spribe audio synthesizer",
                            fontSize = 11.sp,
                            color = LabelGrey
                        )
                    }
                }

                Switch(
                    checked = masterEnabled,
                    onCheckedChange = onMasterToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("master_audio_switch")
                )
            }

            // Volume Slider
            if (masterEnabled) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Game Sound Volume",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark
                        )
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                    }
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChanged,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = CrimsonPrimary,
                            activeTrackColor = CrimsonPrimary
                        ),
                        modifier = Modifier.testTag("sound_volume_slider")
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEF2))

            // Sub-audio toggles
            AudioRowItem(
                title = "🛩️ Flight Engine / Propeller Drone",
                subtitle = "Accelerating pitch climbing with multiplier",
                enabled = masterEnabled && engineEnabled,
                onCheckedChange = onEngineToggled,
                onPreview = { onPreviewSound("engine") },
                testTag = "engine_sound_switch"
            )

            AudioRowItem(
                title = "💥 Flew Away / Crash Sound",
                subtitle = "Descending explosion swoosh at round end",
                enabled = masterEnabled && crashEnabled,
                onCheckedChange = onCrashToggled,
                onPreview = { onPreviewSound("crash") },
                testTag = "crash_sound_switch"
            )

            AudioRowItem(
                title = "🔔 Cashout Win Bell Chime",
                subtitle = "Dual crystal chime on auto-stop hit",
                enabled = masterEnabled && cashoutEnabled,
                onCheckedChange = onCashoutToggled,
                onPreview = { onPreviewSound("cashout") },
                testTag = "cashout_sound_switch"
            )

            AudioRowItem(
                title = "🚨 10x+ Pink Multiplier Siren",
                subtitle = "Ascending celebration arpeggio",
                enabled = masterEnabled && pinkSirenEnabled,
                onCheckedChange = onPinkSirenToggled,
                onPreview = { onPreviewSound("pink") },
                testTag = "pink_siren_switch"
            )
        }
    }
}

@Composable
private fun AudioRowItem(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onPreview: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = LabelGrey
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF0F1F5))
                    .clickable { onPreview() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test sound",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "TEST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CrimsonPrimary
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun HapticSettingsCard(
    hapticEnabled: Boolean,
    onHapticToggled: (Boolean) -> Unit,
    onPreviewHaptic: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("haptic_settings_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptic feedback",
                        tint = if (hapticEnabled) CrimsonPrimary else LabelGrey
                    )
                    Column {
                        Text(
                            text = "PREDICTION HAPTIC ALERTS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkDark
                        )
                        Text(
                            text = "Dual-pulse vibration on high-confidence predictions (≥75% safe)",
                            fontSize = 11.sp,
                            color = LabelGrey
                        )
                    }
                }

                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onHapticToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("haptic_feedback_switch")
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEF2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📳 Test Haptic Vibration",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Text(
                        text = "Simulates high-confidence prediction tactile pulse pattern",
                        fontSize = 10.sp,
                        color = LabelGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hapticEnabled) CrimsonPrimary else Color(0xFFE2E2E7))
                        .clickable(enabled = hapticEnabled) { onPreviewHaptic() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("test_haptic_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Test vibration",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    onNotificationsToggled: (Boolean) -> Unit,
    onSendTestNotification: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E2E7), RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("notification_settings_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification alerts",
                        tint = if (notificationsEnabled) CrimsonPrimary else LabelGrey
                    )
                    Column {
                        Text(
                            text = "HIGH-PROBABILITY ALERTS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = InkDark
                        )
                        Text(
                            text = "Instant system notification when bot predicts ≥75% safe score",
                            fontSize = 11.sp,
                            color = LabelGrey
                        )
                    }
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CrimsonPrimary
                    ),
                    modifier = Modifier.testTag("notifications_switch")
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEF2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔔 Test High-Probability Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Text(
                        text = "Triggers immediate sample notification with safe score and target",
                        fontSize = 10.sp,
                        color = LabelGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (notificationsEnabled) CrimsonPrimary else Color(0xFFE2E2E7))
                        .clickable(enabled = notificationsEnabled) { onSendTestNotification() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("test_notification_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test notification",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvablyFairInfoCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LabelGrey,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Provably Fair RNG & Probability Integrity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Betway Aviator generates multipliers via SHA-512 combining 1 Operator Seed and 3 Player Seeds. Safe % strength uses Spribe's 97% Return-to-Player mathematical probability model with real-time volatility weighting. Each round remains independently determined by cryptographic RNG.",
                    fontSize = 10.sp,
                    color = LabelGrey,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
