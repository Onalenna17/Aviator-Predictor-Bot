package com.aviatorpredictorbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aviatorpredictorbot.ui.components.GeminiStrategyCard
import com.aviatorpredictorbot.ui.components.HighMultiplierFrequencyModule
import com.aviatorpredictorbot.ui.components.HistoryPillsRow
import com.aviatorpredictorbot.ui.components.IntervalGeneratorSection
import com.aviatorpredictorbot.ui.components.LiveConnectionSection
import com.aviatorpredictorbot.ui.components.LiveFlightTelemetryCard
import com.aviatorpredictorbot.ui.components.LiveGameAudioCard
import com.aviatorpredictorbot.ui.components.LongTermTrendsCard
import com.aviatorpredictorbot.ui.components.NextRoundLine
import com.aviatorpredictorbot.ui.components.PredictionDialog
import com.aviatorpredictorbot.ui.components.RadarWheelView
import com.aviatorpredictorbot.ui.components.RawJsonDialog
import com.aviatorpredictorbot.ui.components.RecoveryMultCard
import com.aviatorpredictorbot.ui.components.SafeProbabilityStrengthCard
import com.aviatorpredictorbot.ui.components.SettingsScreen
import com.aviatorpredictorbot.ui.components.SplashScreen
import com.aviatorpredictorbot.ui.components.StatusBanner
import com.aviatorpredictorbot.ui.components.StrategyCard
import com.aviatorpredictorbot.ui.components.ThresholdHighlightBanner
import com.aviatorpredictorbot.ui.components.TrainBanner
import com.aviatorpredictorbot.ui.theme.AviatorPredictorTheme
import com.aviatorpredictorbot.ui.theme.ButtonRed
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.LabelGrey
import com.aviatorpredictorbot.ui.theme.LightBackground
import com.aviatorpredictorbot.util.configureAppCheck
import com.aviatorpredictorbot.viewmodel.AviatorViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private val viewModel: AviatorViewModel by viewModels()

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // Native fullscreen edge-to-edge layout
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var showSplash by rememberSaveable { mutableStateOf(true) }
            AviatorPredictorTheme(darkSystemBars = showSplash) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AviatorMainScreen(viewModel = viewModel)
                    AnimatedVisibility(
                        visible = showSplash,
                        exit = fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 1.08f, animationSpec = tween(500))
                    ) {
                        SplashScreen(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}

@Composable
fun AviatorMainScreen(viewModel: AviatorViewModel) {
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val safeProbabilityStrength by viewModel.safeProbabilityStrength.collectAsStateWithLifecycle()

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

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateNotifications(true)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!viewModel.notificationManager.hasNotificationPermission()) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            settings = settings,
            safeProbabilityStrength = safeProbabilityStrength,
            onBack = { viewModel.closeSettings() },
            onUpdateRiskMode = { viewModel.updateRiskMode(it) },
            onUpdateAutoStop = { viewModel.updateAutoStopMultiplier(it) },
            onUpdatePinkThreshold = { viewModel.updatePinkAlertThreshold(it) },
            onUpdateStreakThreshold = { viewModel.updateStreakProtectionThreshold(it) },
            onUpdateSoundMaster = { viewModel.updateSoundMaster(it) },
            onUpdateSoundEngine = { viewModel.updateSoundEngine(it) },
            onUpdateSoundCrash = { viewModel.updateSoundCrash(it) },
            onUpdateSoundCashout = { viewModel.updateSoundCashout(it) },
            onUpdateSoundPinkSiren = { viewModel.updateSoundPinkSiren(it) },
            onUpdateSoundVolume = { viewModel.updateSoundVolume(it) },
            onPreviewSound = { viewModel.previewSound(it) },
            onUpdateHapticFeedback = { viewModel.updateHapticFeedback(it) },
            onPreviewHaptic = { viewModel.previewHaptic() },
            onUpdateNotifications = { viewModel.updateNotifications(it) },
            onSendTestNotification = { viewModel.sendTestNotification() },
            onResetDefaults = { viewModel.resetSettingsToDefaults() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = LightBackground,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(LightBackground),
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
                    // Header Row: Train banner + Settings Gear icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrainBanner(
                            message = trainMessage,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { viewModel.openSettings() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8E8EE))
                                .testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Predictor & audio settings",
                                tint = InkDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Visual notification when the multiplier crosses 10x
                    ThresholdHighlightBanner(
                        isVisible = thresholdNotification.isVisible,
                        currentMultiplier = currentMultiplier,
                        roundId = currentRoundId,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Radar wheel: shows the predicted next round
                    RadarWheelView(
                        predictedNext = predictedNextRound,
                        isPinkHighlightActive = thresholdNotification.isVisible,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )

                    // NEXT <time> (+Nm) · <multiplier or Waiting> with Safe % Strength chip
                    NextRoundLine(
                        predictedNext = predictedNextRound,
                        safeProbabilityStrength = safeProbabilityStrength,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // History pills
                    HistoryPillsRow(
                        history = multiplierHistory,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    // Prediction report button
                    Button(
                        onClick = { viewModel.requestPrediction() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonRed,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(50.dp)
                            .testTag("next_prediction_button")
                    ) {
                        Text(
                            text = "NEXT PREDICTION",
                            color = Color.Black,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Crash time / crash mult / generate intervals (white card)
                    IntervalGeneratorSection(
                        crashTimes = crashTimes,
                        selectedCrashTime = selectedCrashTime,
                        crashMultiplier = inputCrashMultiplier,
                        intervalResult = intervalResult,
                        errorMessage = intervalErrorMessage,
                        onSelectCrashTime = { viewModel.onSelectCrashTime(it) },
                        onCrashMultiplierChange = { viewModel.onCrashMultiplierChanged(it) },
                        onGenerateClick = { viewModel.generateIntervals() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Recovery mult (white card)
                    RecoveryMultCard(
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // ---- Live dashboard sections ----

                    StatusBanner(
                        isConnected = connectionState.isConnected,
                        isSimulated = connectionState.isSimulated,
                        lastUpdated = connectionState.lastUpdated,
                        errorMessage = connectionState.errorMessage,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Real game sound: automatic crash & flight telemetry audio
                    LiveGameAudioCard(
                        soundMasterEnabled = settings.soundMasterEnabled,
                        soundCrashEnabled = settings.soundCrashEnabled,
                        onToggleMasterSound = { viewModel.updateSoundMaster(it) },
                        onPreviewCrash = { viewModel.previewSound("crash") },
                        onOpenSettings = { viewModel.openSettings() },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Live flight stream HUD
                    LiveFlightTelemetryCard(
                        currentMultiplier = currentMultiplier,
                        armedStatus = armedStatus,
                        roundId = currentRoundId,
                        nextRoundText = nextRoundText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 5x+ multiplier frequency over the last 50 sessions
                    HighMultiplierFrequencyModule(
                        stats = frequencyStats,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Room persistence: long-term trends with 7-day auto-cleanup
                    LongTermTrendsCard(
                        stats = longTermStats,
                        recentCrashes = persistedCrashes,
                        retentionConfig = retentionConfig,
                        onManualPruneClick = { viewModel.runDatabaseCleanupManually() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // PROMINENT SAFE % PREDICTION PROBABILITY STRENGTH CARD (LOCATED RIGHT ABOVE LIVE CONNECTED)
                    SafeProbabilityStrengthCard(
                        strength = safeProbabilityStrength,
                        riskMode = settings.riskMode,
                        autoStopMultiplier = settings.autoStopMultiplier,
                        onOpenSettings = { viewModel.openSettings() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Live connection gateway
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
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Strategy information (bottom of the UI)
                    StrategyCard(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    GeminiStrategyCard(
                        state = geminiStrategyState,
                        onAnalyzeClick = { viewModel.analyzeVolatilityWithGemini() },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Disclaimer
                    Text(
                        text = "Official live telemetry monitor for Betway Spribe Aviator. Historical multipliers and interval cycles cannot reliably predict a random future crash. Each round is governed by independent Provably Fair RNG.",
                        color = LabelGrey,
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
}
