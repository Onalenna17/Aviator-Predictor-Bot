package com.aviatorpredictorbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviatorpredictorbot.ui.components.HighMultiplierFrequencyModule
import com.aviatorpredictorbot.ui.components.HistoryPillsRow
import com.aviatorpredictorbot.ui.components.IntervalGeneratorSection
import com.aviatorpredictorbot.ui.components.LiveConnectionSection
import com.aviatorpredictorbot.ui.components.LiveFlightTelemetryCard
import com.aviatorpredictorbot.ui.components.LongTermTrendsCard
import com.aviatorpredictorbot.ui.components.PredictionDialog
import com.aviatorpredictorbot.ui.components.RadarWheelView
import com.aviatorpredictorbot.ui.components.RawJsonDialog
import com.aviatorpredictorbot.ui.components.StatusBanner
import com.aviatorpredictorbot.ui.components.StrategyCard
import com.aviatorpredictorbot.ui.components.ThresholdHighlightBanner
import com.aviatorpredictorbot.ui.components.TrainBanner
import com.aviatorpredictorbot.ui.theme.AccentGold
import com.aviatorpredictorbot.ui.theme.AviatorPredictorTheme
import com.aviatorpredictorbot.ui.theme.CrimsonLight
import com.aviatorpredictorbot.ui.theme.CrimsonPrimary
import com.aviatorpredictorbot.ui.theme.DeepDarkBackground
import com.aviatorpredictorbot.ui.theme.PinkMultiplier
import com.aviatorpredictorbot.ui.theme.TextMuted
import com.aviatorpredictorbot.ui.theme.TextSecondary
import com.aviatorpredictorbot.viewmodel.AviatorViewModel

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.core.view.WindowCompat
import com.aviatorpredictorbot.ui.components.GeminiStrategyCard
import com.aviatorpredictorbot.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: AviatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // Native fullscreen edge-to-edge layout
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AviatorPredictorTheme {
                AviatorMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AviatorMainScreen(viewModel: AviatorViewModel) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val currentMultiplier by viewModel.currentMultiplier.collectAsStateWithLifecycle()
    val multiplierHistory by viewModel.multiplierHistory.collectAsStateWithLifecycle()
    val trainMessage by viewModel.trainMessage.collectAsStateWithLifecycle()
    val armedStatus by viewModel.armedStatus.collectAsStateWithLifecycle()
    val readyText by viewModel.readyText.collectAsStateWithLifecycle()
    val nextRoundText by viewModel.nextRoundText.collectAsStateWithLifecycle()
    val currentRoundId by viewModel.currentRoundId.collectAsStateWithLifecycle()
    val crashTimes by viewModel.crashTimes.collectAsStateWithLifecycle()
    val rawJson by viewModel.rawJson.collectAsStateWithLifecycle()

    val predictedNextRound by viewModel.predictedNextRound.collectAsStateWithLifecycle()
    val thresholdNotification by viewModel.thresholdNotification.collectAsStateWithLifecycle()
    val isSoundAlertEnabled by viewModel.isSoundAlertEnabled.collectAsStateWithLifecycle()

    val persistedCrashes by viewModel.persistedCrashes.collectAsStateWithLifecycle()
    val longTermStats by viewModel.longTermStats.collectAsStateWithLifecycle()
    val frequencyStats by viewModel.highMultiplierFrequencyStats.collectAsStateWithLifecycle()
    val retentionConfig by viewModel.retentionConfig.collectAsStateWithLifecycle()
    val geminiStrategyState by viewModel.geminiStrategyState.collectAsStateWithLifecycle()

    val selectedCrashTime by viewModel.selectedCrashTime.collectAsStateWithLifecycle()
    val inputCrashMultiplier by viewModel.inputCrashMultiplier.collectAsStateWithLifecycle()
    val intervalResult by viewModel.intervalResult.collectAsStateWithLifecycle()
    val intervalErrorMessage by viewModel.intervalErrorMessage.collectAsStateWithLifecycle()

    val predictionInsight by viewModel.predictionInsight.collectAsStateWithLifecycle()
    val showPredictionDialog by viewModel.showPredictionDialog.collectAsStateWithLifecycle()
    val showRawJsonDialog by viewModel.showRawJsonDialog.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepDarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF36000D),
                            Color(0xFF080203),
                            DeepDarkBackground
                        ),
                        startY = 0f,
                        endY = 1600f
                    )
                )
        ) {
            // Top Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(CrimsonPrimary)
                    .testTag("top_accent_bar")
            )

            // Scrollable Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Notification System: Subtle UI highlight when multiplier crosses 10x
                    ThresholdHighlightBanner(
                        isVisible = thresholdNotification.isVisible,
                        currentMultiplier = currentMultiplier,
                        roundId = currentRoundId,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Sound Alert & Controls Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF190408))
                            .border(1.dp, Color(0xFF420B16), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleSoundAlert() }
                                .testTag("toggle_sound_alert_button")
                        ) {
                            Icon(
                                imageVector = if (isSoundAlertEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle Sound Alert",
                                tint = if (isSoundAlertEnabled) AccentGold else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSoundAlertEnabled) "Sound Alert (>10x): ON" else "Sound Alert (>10x): OFF",
                                color = if (isSoundAlertEnabled) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Test Sound Alert Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E070F))
                                .clickable { viewModel.testSoundAlert() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("test_sound_alert_button")
                        ) {
                            Text(
                                text = "Test Chime ♫",
                                color = CrimsonLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Header Banner
                    TrainBanner(
                        message = trainMessage,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Status Indicator
                    StatusBanner(
                        isConnected = connectionState.isConnected,
                        isSimulated = connectionState.isSimulated,
                        lastUpdated = connectionState.lastUpdated,
                        errorMessage = connectionState.errorMessage,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Strategy Card from User Attachment
                    StrategyCard(
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Gemini AI Volatility Strategy Tip based on last 50 crash records
                    GeminiStrategyCard(
                        state = geminiStrategyState,
                        onAnalyzeClick = { viewModel.analyzeVolatilityWithGemini() },
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Big Wheel: Exclusively Displays Predicted Real Next Round
                    RadarWheelView(
                        predictedNext = predictedNextRound,
                        isPinkHighlightActive = thresholdNotification.isVisible,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Flight Stream HUD
                    LiveFlightTelemetryCard(
                        currentMultiplier = currentMultiplier,
                        armedStatus = armedStatus,
                        roundId = currentRoundId,
                        nextRoundText = nextRoundText,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // History Multipliers Row
                    HistoryPillsRow(
                        history = multiplierHistory,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Prediction Breakdown Action Button
                    Button(
                        onClick = { viewModel.requestPrediction() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(48.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF50032),
                                        Color(0xFFFF0038)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("next_prediction_button")
                    ) {
                        Text(
                            text = "NEXT PREDICTION REPORT",
                            color = Color(0xFF080000),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Visual Module: 5x+ Multiplier Frequency over Last 50 Sessions
                    HighMultiplierFrequencyModule(
                        stats = frequencyStats,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Room Persistence Layer: Long-term trends card with 7-Day Auto-Cleanup status
                    LongTermTrendsCard(
                        stats = longTermStats,
                        recentCrashes = persistedCrashes,
                        retentionConfig = retentionConfig,
                        onManualPruneClick = { viewModel.runDatabaseCleanupManually() },
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Crash Time & Mult Interval Generator Section
                    IntervalGeneratorSection(
                        crashTimes = crashTimes,
                        selectedCrashTime = selectedCrashTime,
                        crashMultiplier = inputCrashMultiplier,
                        intervalResult = intervalResult,
                        errorMessage = intervalErrorMessage,
                        onSelectCrashTime = { viewModel.onSelectCrashTime(it) },
                        onCrashMultiplierChange = { viewModel.onCrashMultiplierChanged(it) },
                        onGenerateClick = { viewModel.generateIntervals() },
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Official Betway Aviator Connection Gateway Section
                    LiveConnectionSection(
                        state = connectionState,
                        hasRawJson = rawJson.isNotBlank(),
                        onEndpointUrlChange = { viewModel.onEndpointUrlChanged(it) },
                        onConnectClick = { viewModel.connectFeed() },
                        onDisconnectClick = { viewModel.disconnect("Feed paused by user.") },
                        onStartSimulatorClick = {
                            if (connectionState.isSimulated) {
                                viewModel.disconnect("Feed paused.")
                            } else {
                                viewModel.startSimulator()
                            }
                        },
                        onViewRawJsonClick = { viewModel.openRawJsonDialog() },
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    // Educational Disclaimer
                    Text(
                        text = "Official live telemetry monitor for Betway Spribe Aviator. Historical multipliers and interval cycles cannot reliably predict a random future crash. Each round is governed by independent Provably Fair RNG.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .testTag("footer_disclaimer")
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showPredictionDialog) {
        PredictionDialog(
            insight = predictionInsight,
            onDismiss = { viewModel.dismissPredictionDialog() }
        )
    }

    if (showRawJsonDialog) {
        RawJsonDialog(
            rawJson = rawJson,
            onDismiss = { viewModel.dismissRawJsonDialog() }
        )
    }
}
