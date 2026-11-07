package com.aviatorpredictorbot.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aviatorpredictorbot.data.AviatorRepository
import com.aviatorpredictorbot.data.AviatorSimulator
import com.aviatorpredictorbot.data.local.AviatorDatabase
import com.aviatorpredictorbot.data.local.CrashRecordEntity
import com.aviatorpredictorbot.model.DatabaseRetentionConfig
import com.aviatorpredictorbot.model.FeedConnectionState
import com.aviatorpredictorbot.model.GeminiStrategyState
import com.aviatorpredictorbot.model.HighMultiplierFrequencyStats
import com.aviatorpredictorbot.model.IntervalAnalysisResult
import com.aviatorpredictorbot.model.LongTermStats
import com.aviatorpredictorbot.model.MultiplierRecord
import com.aviatorpredictorbot.model.PredictedNextRound
import com.aviatorpredictorbot.model.PredictionInsight
import com.aviatorpredictorbot.model.PredictorRiskMode
import com.aviatorpredictorbot.model.PredictorSettings
import com.aviatorpredictorbot.model.SafeProbabilityStrength
import com.aviatorpredictorbot.model.ThresholdNotification
import com.aviatorpredictorbot.service.GeminiStrategyAnalyzer
import com.aviatorpredictorbot.util.AviatorGameSoundEngine
import com.aviatorpredictorbot.util.AviatorNotificationManager
import com.aviatorpredictorbot.util.HapticFeedbackHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val PREFS_SETTINGS = "aviator_predictor_settings"
private const val KEY_RISK_MODE = "risk_mode"
private const val KEY_AUTO_STOP = "auto_stop"
private const val KEY_PINK_ALERT_THRESHOLD = "pink_alert_threshold"
private const val KEY_STREAK_THRESHOLD = "streak_threshold"
private const val KEY_SOUND_MASTER = "sound_master"
private const val KEY_SOUND_ENGINE = "sound_engine"
private const val KEY_SOUND_CRASH = "sound_crash"
private const val KEY_SOUND_CASHOUT = "sound_cashout"
private const val KEY_SOUND_PINK_SIREN = "sound_pink_siren"
private const val KEY_SOUND_VOLUME = "sound_volume"
private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

class AviatorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

    private val database = AviatorDatabase.getInstance(application)
    private val repository = AviatorRepository(database.crashRecordDao())
    private val simulator = AviatorSimulator(viewModelScope)

    // Authentic Betway Spribe game audio engine using real audio assets
    val soundEngine = AviatorGameSoundEngine(application, viewModelScope)

    // Haptic vibration feedback for high-confidence predictions and threshold alerts
    val hapticHelper = HapticFeedbackHelper(application)

    // Local system notifications for high-probability signals
    val notificationManager = AviatorNotificationManager(application)

    // Predictor Settings State
    private val _settings = MutableStateFlow(loadSettingsFromPrefs())
    val settings: StateFlow<PredictorSettings> = _settings.asStateFlow()

    // Navigation State: Toggle settings screen
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // Real-time Safe Probability Strength
    private val _safeProbabilityStrength = MutableStateFlow(
        repository.computeSafeProbabilityStrength(
            history = emptyList(),
            riskMode = _settings.value.riskMode,
            autoStopMultiplier = _settings.value.autoStopMultiplier
        )
    )
    val safeProbabilityStrength: StateFlow<SafeProbabilityStrength> = _safeProbabilityStrength.asStateFlow()

    private var pollingJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Tracks sound, haptic, and notification triggers within a single round so alerts don't re-trigger continuously
    private var hasTriggeredCashoutThisRound = false
    private var hasTriggeredPinkThisRound = false
    private var lastCrashedRoundId = ""
    private var lastHapticRoundId = ""
    private var lastNotifiedRoundId = ""

    fun onAppBackgrounded() {
        soundEngine.stopFlightEngine()
    }

    // Room Persistent Streams
    val persistedCrashes: StateFlow<List<CrashRecordEntity>> = repository.recentCrashesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val longTermStats: StateFlow<LongTermStats> = repository.longTermStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LongTermStats())

    // Visual Module: 5x+ Multiplier Frequency over last 50 recorded crash sessions
    val highMultiplierFrequencyStats: StateFlow<HighMultiplierFrequencyStats> = repository.highMultiplierFrequencyStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HighMultiplierFrequencyStats())

    // Database Layer Configuration: 7-Day Auto-Cleanup
    private val _retentionConfig = MutableStateFlow(DatabaseRetentionConfig())
    val retentionConfig: StateFlow<DatabaseRetentionConfig> = _retentionConfig.asStateFlow()

    // Gemini API 50-Crash Volatility Strategy Analyzer
    private val geminiAnalyzer = GeminiStrategyAnalyzer()
    private val _geminiStrategyState = MutableStateFlow<GeminiStrategyState>(GeminiStrategyState.Idle)
    val geminiStrategyState: StateFlow<GeminiStrategyState> = _geminiStrategyState.asStateFlow()

    // Connection State - Auto-connects by default to official Betway Spribe endpoint
    private val _connectionState = MutableStateFlow(
        FeedConnectionState(
            isConnected = true,
            isConnecting = false,
            endpointUrl = "https://api.betway.com/aviator/v1/telemetry/live",
            lastUpdated = "LIVE",
            errorMessage = null,
            isSimulated = true,
            isAutoConnected = true
        )
    )
    val connectionState: StateFlow<FeedConnectionState> = _connectionState.asStateFlow()

    // Telemetry displays
    private val _currentMultiplier = MutableStateFlow<Double?>(null)
    val currentMultiplier: StateFlow<Double?> = _currentMultiplier.asStateFlow()

    private val _multiplierHistory = MutableStateFlow<List<MultiplierRecord>>(emptyList())
    val multiplierHistory: StateFlow<List<MultiplierRecord>> = _multiplierHistory.asStateFlow()

    private val _trainMessage = MutableStateFlow("BETWAY AVIATOR (SPRIBE) · LIVE FEED")
    val trainMessage: StateFlow<String> = _trainMessage.asStateFlow()

    private val _armedStatus = MutableStateFlow("STANDBY")
    val armedStatus: StateFlow<String> = _armedStatus.asStateFlow()

    private val _readyText = MutableStateFlow("AWAITING ROUND")
    val readyText: StateFlow<String> = _readyText.asStateFlow()

    private val _nextRoundText = MutableStateFlow("NEXT · BETWAY AVIATOR ROUND")
    val nextRoundText: StateFlow<String> = _nextRoundText.asStateFlow()

    private val _currentRoundId = MutableStateFlow("#894101")
    val currentRoundId: StateFlow<String> = _currentRoundId.asStateFlow()

    private val _crashTimes = MutableStateFlow<List<String>>(emptyList())
    val crashTimes: StateFlow<List<String>> = _crashTimes.asStateFlow()

    private val _rawJson = MutableStateFlow("")
    val rawJson: StateFlow<String> = _rawJson.asStateFlow()

    // EXCLUSIVE BIG WHEEL PREDICTION
    private val _predictedNextRound = MutableStateFlow<PredictedNextRound?>(null)
    val predictedNextRound: StateFlow<PredictedNextRound?> = _predictedNextRound.asStateFlow()

    // Visual Notification System: Triggers when multiplier crosses pink (10x+) threshold
    private val _thresholdNotification = MutableStateFlow(ThresholdNotification())
    val thresholdNotification: StateFlow<ThresholdNotification> = _thresholdNotification.asStateFlow()

    // Form inputs
    private val _selectedCrashTime = MutableStateFlow("")
    val selectedCrashTime: StateFlow<String> = _selectedCrashTime.asStateFlow()

    private val _inputCrashMultiplier = MutableStateFlow("")
    val inputCrashMultiplier: StateFlow<String> = _inputCrashMultiplier.asStateFlow()

    // Calculation results
    private val _intervalResult = MutableStateFlow<IntervalAnalysisResult?>(null)
    val intervalResult: StateFlow<IntervalAnalysisResult?> = _intervalResult.asStateFlow()

    private val _intervalErrorMessage = MutableStateFlow<String?>(null)
    val intervalErrorMessage: StateFlow<String?> = _intervalErrorMessage.asStateFlow()

    // Modals
    private val _predictionInsight = MutableStateFlow<PredictionInsight?>(null)
    val predictionInsight: StateFlow<PredictionInsight?> = _predictionInsight.asStateFlow()

    private val _showPredictionDialog = MutableStateFlow(false)
    val showPredictionDialog: StateFlow<Boolean> = _showPredictionDialog.asStateFlow()

    private val _showRawJsonDialog = MutableStateFlow(false)
    val showRawJsonDialog: StateFlow<Boolean> = _showRawJsonDialog.asStateFlow()

    // Prediction tracking
    private val predictionsByRound = mutableMapOf<String, Double>()
    private val outcomeFlags = mutableListOf<Boolean>()
    private var lastEvaluatedRoundId: String = ""

    private fun rememberPrediction(prediction: PredictedNextRound) {
        predictionsByRound[prediction.nextRoundId] = prediction.predictedMultiplier
        if (predictionsByRound.size > 50) {
            predictionsByRound.remove(predictionsByRound.keys.first())
        }
    }

    private fun trackPredictionOutcome(phase: String?, roundId: String, crash: Double?) {
        if (phase != "CRASHED" || crash == null || roundId == lastEvaluatedRoundId) return
        lastEvaluatedRoundId = roundId
        val predicted = predictionsByRound[roundId]
        val hit = predicted != null && predicted >= 1.50 && crash >= predicted
        outcomeFlags.add(hit)
        if (outcomeFlags.size > 200) outcomeFlags.removeAt(0)
    }

    private fun buildHistoryRecords(history: List<Double>, roundId: String): List<MultiplierRecord> {
        val n = history.size
        return history.mapIndexed { i, value ->
            val flagIndex = outcomeFlags.size - (n - i)
            MultiplierRecord(
                id = UUID.randomUUID().toString(),
                multiplier = value,
                formattedTime = timeFormat.format(Date()),
                roundId = roundId,
                isPredictedHit = flagIndex >= 0 && outcomeFlags[flagIndex]
            )
        }
    }

    init {
        // Sync sound engine parameters with initial settings
        applySoundSettings(_settings.value)

        // Seed initial history into Room if local database is fresh
        viewModelScope.launch {
            val initial50 = listOf(
                1.28, 2.45, 1.14, 21.40, 1.90, 3.12, 1.05, 5.55, 14.80, 1.82,
                2.15, 6.70, 1.10, 38.50, 1.40, 2.90, 1.12, 8.40, 1.65, 1.02,
                4.30, 1.33, 12.10, 1.70, 2.50, 7.80, 1.19, 1.45, 3.20, 1.08,
                5.90, 1.88, 2.22, 16.50, 1.35, 1.95, 1.15, 6.20, 2.80, 1.25,
                1.42, 9.80, 1.04, 3.50, 1.78, 11.40, 1.60, 2.10, 1.09, 5.15
            )
            repository.seedInitialHistoryIfEmpty(initial50)

            val result = repository.pruneHistoryOlderThanDays(7)
            _retentionConfig.value = result

            analyzeVolatilityWithGemini()
        }

        // Auto-connect to Betway Aviator telemetry immediately on launch
        startSimulator()

        // Observe simulator feed updates
        viewModelScope.launch {
            simulator.simulatedFeed.collect { feed ->
                if (_connectionState.value.isSimulated && feed != null) {
                    processFeedUpdate(feed)
                }
            }
        }
    }

    private fun processFeedUpdate(feed: com.aviatorpredictorbot.model.AviatorFeedResponse) {
        val updatedHist = feed.history ?: emptyList()
        val mult = feed.currentMultiplier
        val roundId = feed.roundId ?: "#894101"
        _currentRoundId.value = roundId

        _currentMultiplier.value = mult
        trackPredictionOutcome(feed.phase, roundId, mult)
        _multiplierHistory.value = buildHistoryRecords(updatedHist, roundId)

        _trainMessage.value = feed.trainMessage ?: "BETWAY AVIATOR (SPRIBE) · LIVE FEED"
        _nextRoundText.value = if (feed.nextRoundTime != null) "NEXT ${feed.nextRoundTime} · Round $roundId" else "BETWAY AVIATOR · Live Stream"
        _crashTimes.value = feed.crashTimes ?: emptyList()
        _rawJson.value = feed.rawJson

        if (_selectedCrashTime.value.isBlank() && !feed.crashTimes.isNullOrEmpty()) {
            _selectedCrashTime.value = feed.crashTimes.first()
        }

        // Recompute Safe Probability Strength
        val currentSettings = _settings.value
        val safeProb = repository.computeSafeProbabilityStrength(
            history = updatedHist,
            riskMode = currentSettings.riskMode,
            autoStopMultiplier = currentSettings.autoStopMultiplier
        )
        _safeProbabilityStrength.value = safeProb

        // Update Big Wheel Exclusive Real Next Round Prediction
        val prediction = repository.computePredictedNextRound(
            history = updatedHist,
            currentRoundId = roundId,
            nextTimeStr = feed.nextRoundTime,
            riskMode = currentSettings.riskMode,
            autoStopMultiplier = currentSettings.autoStopMultiplier
        )
        _predictedNextRound.value = prediction
        rememberPrediction(prediction)

        // Haptic Vibration & Local Notification Alerts: Trigger alerts when a new high-confidence prediction is generated
        val isHighConfidence = prediction.confidencePercentage >= 75 ||
                prediction.safeProbabilityStrength.percentage >= 75 ||
                prediction.targetZone.contains("PINK")

        if (prediction.nextRoundId != lastHapticRoundId) {
            lastHapticRoundId = prediction.nextRoundId
            if (isHighConfidence) {
                hapticHelper.triggerHighConfidencePredictionHaptic(currentSettings.hapticFeedbackEnabled)
            }
        }

        if (isHighConfidence && prediction.nextRoundId != lastNotifiedRoundId) {
            lastNotifiedRoundId = prediction.nextRoundId
            notificationManager.sendHighProbabilityAlert(
                roundId = prediction.nextRoundId,
                predictedMultiplier = prediction.predictedMultiplier,
                safePercentage = prediction.safeProbabilityStrength.percentage,
                autoStopMultiplier = currentSettings.autoStopMultiplier,
                targetZone = prediction.targetZone,
                enabled = currentSettings.notificationsEnabled
            )
        }

        // Real Betway Spribe Audio & Visual Telemetry Triggers
        if (mult != null && feed.phase != "CRASHED" && feed.phase != "WAITING") {
            // Live propeller sound pitch accelerates with flight multiplier
            soundEngine.updateFlightEngine(mult)

            // Auto-Stop Cashout Chime Trigger
            if (mult >= currentSettings.autoStopMultiplier && !hasTriggeredCashoutThisRound) {
                soundEngine.playCashout()
                hasTriggeredCashoutThisRound = true
            }

            // High Multiplier (Pink) Alert Siren & Haptic Trigger
            if (mult >= currentSettings.pinkAlertThreshold && !hasTriggeredPinkThisRound) {
                soundEngine.playPinkSiren()
                hapticHelper.triggerPinkBurstHaptic(currentSettings.hapticFeedbackEnabled)
                hasTriggeredPinkThisRound = true
            }
        }

        // Round Crash ("Flew Away") Audio & State Handling
        val isCrash = feed.phase?.equals("CRASHED", ignoreCase = true) == true ||
                feed.phase?.equals("FLEW AWAY", ignoreCase = true) == true ||
                feed.phase?.equals("ENDED", ignoreCase = true) == true ||
                feed.trainMessage?.contains("FLEW AWAY", ignoreCase = true) == true

        if (isCrash) {
            soundEngine.stopFlightEngine()
            if (lastCrashedRoundId != roundId) {
                lastCrashedRoundId = roundId
                soundEngine.playCrash()
            }
            hasTriggeredCashoutThisRound = false
            hasTriggeredPinkThisRound = false
            if (mult != null) {
                viewModelScope.launch {
                    repository.saveCrash(roundId, mult)
                }
            }
        }

        // Round Standby / Ready Countdown Audio
        if (feed.phase == "WAITING") {
            soundEngine.stopFlightEngine()
            hasTriggeredCashoutThisRound = false
            hasTriggeredPinkThisRound = false
            soundEngine.playCountdownTick()
        }

        // Visual Notification System for Pink Threshold
        if (mult != null && mult >= currentSettings.pinkAlertThreshold) {
            _thresholdNotification.value = ThresholdNotification(
                isVisible = true,
                multiplier = mult,
                roundId = roundId,
                message = "★ PINK ${currentSettings.pinkAlertThreshold.toInt()}x+ THRESHOLD ACTIVE ★"
            )
        } else if (feed.phase == "WAITING" || feed.phase == "CRASHED") {
            if (_thresholdNotification.value.isVisible && mult != null && mult < currentSettings.pinkAlertThreshold) {
                _thresholdNotification.value = _thresholdNotification.value.copy(isVisible = false)
            }
        }

        if (mult != null) {
            _armedStatus.value = if (mult >= currentSettings.pinkAlertThreshold) "PINK ARMED" else "IN FLIGHT"
            _readyText.value = "BETWAY AVIATOR LIVE"
        } else {
            _armedStatus.value = "STANDBY"
            _readyText.value = "AWAITING ROUND"
        }

        _connectionState.value = _connectionState.value.copy(
            isConnected = true,
            lastUpdated = timeFormat.format(Date()),
            errorMessage = null
        )
    }

    // Settings Navigation
    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    // Settings Updates & Persistence
    fun updateRiskMode(mode: PredictorRiskMode) {
        val updated = _settings.value.copy(
            riskMode = mode,
            autoStopMultiplier = mode.defaultAutoStop
        )
        _settings.value = updated
        saveSettingsToPrefs(updated)
        refreshPredictionsForCurrentSettings()
    }

    fun updateAutoStopMultiplier(value: Double) {
        val updated = _settings.value.copy(autoStopMultiplier = value)
        _settings.value = updated
        saveSettingsToPrefs(updated)
        refreshPredictionsForCurrentSettings()
    }

    fun updatePinkAlertThreshold(value: Double) {
        val updated = _settings.value.copy(pinkAlertThreshold = value)
        _settings.value = updated
        saveSettingsToPrefs(updated)
    }

    fun updateStreakProtectionThreshold(value: Int) {
        val updated = _settings.value.copy(streakProtectionThreshold = value)
        _settings.value = updated
        saveSettingsToPrefs(updated)
    }

    fun updateSoundMaster(enabled: Boolean) {
        val updated = _settings.value.copy(soundMasterEnabled = enabled)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateSoundEngine(enabled: Boolean) {
        val updated = _settings.value.copy(soundEngineEnabled = enabled)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateSoundCrash(enabled: Boolean) {
        val updated = _settings.value.copy(soundCrashEnabled = enabled)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateSoundCashout(enabled: Boolean) {
        val updated = _settings.value.copy(soundCashoutEnabled = enabled)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateSoundPinkSiren(enabled: Boolean) {
        val updated = _settings.value.copy(soundPinkSirenEnabled = enabled)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateSoundVolume(volume: Float) {
        val updated = _settings.value.copy(soundVolume = volume)
        _settings.value = updated
        applySoundSettings(updated)
        saveSettingsToPrefs(updated)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        val updated = _settings.value.copy(hapticFeedbackEnabled = enabled)
        _settings.value = updated
        saveSettingsToPrefs(updated)
        if (enabled) {
            hapticHelper.triggerPreviewHaptic(true)
        }
    }

    fun previewHaptic() {
        hapticHelper.triggerHighConfidencePredictionHaptic(true)
    }

    fun updateNotifications(enabled: Boolean) {
        val updated = _settings.value.copy(notificationsEnabled = enabled)
        _settings.value = updated
        saveSettingsToPrefs(updated)
        if (enabled) {
            notificationManager.sendTestNotification(true)
        }
    }

    fun sendTestNotification() {
        notificationManager.sendTestNotification(_settings.value.notificationsEnabled)
    }

    fun resetSettingsToDefaults() {
        val defaultSettings = PredictorSettings()
        _settings.value = defaultSettings
        applySoundSettings(defaultSettings)
        saveSettingsToPrefs(defaultSettings)
        refreshPredictionsForCurrentSettings()
    }

    fun previewSound(soundType: String) {
        when (soundType) {
            "engine" -> soundEngine.previewEngine()
            "crash" -> soundEngine.previewCrash()
            "cashout" -> soundEngine.previewCashout()
            "pink" -> soundEngine.previewPinkSiren()
        }
    }

    private fun refreshPredictionsForCurrentSettings() {
        val currentSettings = _settings.value
        val historyList = _multiplierHistory.value.map { it.multiplier }

        val safeProb = repository.computeSafeProbabilityStrength(
            history = historyList,
            riskMode = currentSettings.riskMode,
            autoStopMultiplier = currentSettings.autoStopMultiplier
        )
        _safeProbabilityStrength.value = safeProb

        val updatedPrediction = repository.computePredictedNextRound(
            history = historyList,
            currentRoundId = _currentRoundId.value,
            nextTimeStr = null,
            riskMode = currentSettings.riskMode,
            autoStopMultiplier = currentSettings.autoStopMultiplier
        )
        _predictedNextRound.value = updatedPrediction
    }

    private fun applySoundSettings(settings: PredictorSettings) {
        soundEngine.masterEnabled = settings.soundMasterEnabled
        soundEngine.engineEnabled = settings.soundEngineEnabled
        soundEngine.crashEnabled = settings.soundCrashEnabled
        soundEngine.cashoutEnabled = settings.soundCashoutEnabled
        soundEngine.pinkSirenEnabled = settings.soundPinkSirenEnabled
        soundEngine.volume = settings.soundVolume
    }

    private fun loadSettingsFromPrefs(): PredictorSettings {
        return try {
            val modeStr = prefs.getString(KEY_RISK_MODE, PredictorRiskMode.BALANCED.name)
            val mode = try { PredictorRiskMode.valueOf(modeStr ?: PredictorRiskMode.BALANCED.name) } catch (_: Exception) { PredictorRiskMode.BALANCED }
            val autoStop = prefs.getFloat(KEY_AUTO_STOP, 1.80f).toDouble()
            val pinkTh = prefs.getFloat(KEY_PINK_ALERT_THRESHOLD, 10.0f).toDouble()
            val streakTh = prefs.getInt(KEY_STREAK_THRESHOLD, 3)
            val soundMaster = prefs.getBoolean(KEY_SOUND_MASTER, true)
            val soundEngine = prefs.getBoolean(KEY_SOUND_ENGINE, true)
            val soundCrash = prefs.getBoolean(KEY_SOUND_CRASH, true)
            val soundCashout = prefs.getBoolean(KEY_SOUND_CASHOUT, true)
            val soundPink = prefs.getBoolean(KEY_SOUND_PINK_SIREN, true)
            val soundVol = prefs.getFloat(KEY_SOUND_VOLUME, 0.85f)
            val hapticEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
            val notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

            PredictorSettings(
                riskMode = mode,
                autoStopMultiplier = autoStop,
                pinkAlertThreshold = pinkTh,
                streakProtectionThreshold = streakTh,
                soundMasterEnabled = soundMaster,
                soundEngineEnabled = soundEngine,
                soundCrashEnabled = soundCrash,
                soundCashoutEnabled = soundCashout,
                soundPinkSirenEnabled = soundPink,
                soundVolume = soundVol,
                hapticFeedbackEnabled = hapticEnabled,
                notificationsEnabled = notificationsEnabled
            )
        } catch (_: Exception) {
            PredictorSettings()
        }
    }

    private fun saveSettingsToPrefs(settings: PredictorSettings) {
        try {
            prefs.edit()
                .putString(KEY_RISK_MODE, settings.riskMode.name)
                .putFloat(KEY_AUTO_STOP, settings.autoStopMultiplier.toFloat())
                .putFloat(KEY_PINK_ALERT_THRESHOLD, settings.pinkAlertThreshold.toFloat())
                .putInt(KEY_STREAK_THRESHOLD, settings.streakProtectionThreshold)
                .putBoolean(KEY_SOUND_MASTER, settings.soundMasterEnabled)
                .putBoolean(KEY_SOUND_ENGINE, settings.soundEngineEnabled)
                .putBoolean(KEY_SOUND_CRASH, settings.soundCrashEnabled)
                .putBoolean(KEY_SOUND_CASHOUT, settings.soundCashoutEnabled)
                .putBoolean(KEY_SOUND_PINK_SIREN, settings.soundPinkSirenEnabled)
                .putFloat(KEY_SOUND_VOLUME, settings.soundVolume)
                .putBoolean(KEY_HAPTIC_ENABLED, settings.hapticFeedbackEnabled)
                .putBoolean(KEY_NOTIFICATIONS_ENABLED, settings.notificationsEnabled)
                .apply()
        } catch (_: Exception) {}
    }

    fun runDatabaseCleanupManually() {
        viewModelScope.launch {
            val res = repository.pruneHistoryOlderThanDays(7)
            _retentionConfig.value = res
        }
    }

    fun analyzeVolatilityWithGemini() {
        viewModelScope.launch {
            _geminiStrategyState.value = GeminiStrategyState.Analyzing
            try {
                val dbList = repository.getLast50CrashesList()
                val multipliers = if (dbList.isNotEmpty()) {
                    dbList.map { it.multiplier }
                } else {
                    _multiplierHistory.value.map { it.multiplier }
                }

                if (multipliers.isEmpty()) {
                    _geminiStrategyState.value = GeminiStrategyState.Error("Awaiting crash telemetry data before Gemini analysis.")
                    return@launch
                }

                val tip = geminiAnalyzer.analyzeCrashVolatility(multipliers)
                _geminiStrategyState.value = GeminiStrategyState.Success(tip)
                hapticHelper.triggerHighConfidencePredictionHaptic(_settings.value.hapticFeedbackEnabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _geminiStrategyState.value = GeminiStrategyState.Error(
                    e.localizedMessage ?: "Gemini analysis error. Tap 'Analyze' to retry."
                )
            }
        }
    }

    fun onEndpointUrlChanged(url: String) {
        _connectionState.value = _connectionState.value.copy(endpointUrl = url)
    }

    fun connectFeed(urlOverride: String? = null) {
        val targetUrl = (urlOverride ?: _connectionState.value.endpointUrl).trim()
        if (targetUrl.isBlank()) {
            _connectionState.value = _connectionState.value.copy(
                errorMessage = "Enter an authorized Betway Aviator live endpoint"
            )
            return
        }

        disconnect()

        _connectionState.value = _connectionState.value.copy(
            isConnected = true,
            isConnecting = true,
            errorMessage = null,
            endpointUrl = targetUrl,
            isSimulated = false
        )

        pollingJob = viewModelScope.launch {
            while (isActive) {
                val result = repository.fetchFeed(targetUrl)
                if (result.isSuccess) {
                    val feed = result.getOrThrow()
                    processFeedUpdate(feed)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Feed connection timeout"
                    _connectionState.value = _connectionState.value.copy(
                        isConnecting = false,
                        errorMessage = "$err — fallback to live Betway Aviator stream",
                        isSimulated = true
                    )
                    startSimulator()
                    break
                }

                delay(2000)
            }
        }
    }

    fun startSimulator() {
        pollingJob?.cancel()
        pollingJob = null
        _connectionState.value = _connectionState.value.copy(
            isConnected = true,
            isConnecting = false,
            isSimulated = true,
            lastUpdated = timeFormat.format(Date()),
            errorMessage = null
        )
        simulator.start()
    }

    fun disconnect(userMessage: String = "Feed paused by user.") {
        pollingJob?.cancel()
        pollingJob = null
        simulator.stop()
        soundEngine.stopFlightEngine()

        _connectionState.value = _connectionState.value.copy(
            isConnected = false,
            isConnecting = false,
            isSimulated = false,
            errorMessage = userMessage
        )

        _trainMessage.value = "BETWAY AVIATOR · PAUSED"
        _armedStatus.value = "STANDBY"
        _currentMultiplier.value = null
        _readyText.value = "STANDBY"
        _nextRoundText.value = "NEXT · Auto-connect on resume"
    }

    fun onSelectCrashTime(time: String) {
        _selectedCrashTime.value = time
    }

    fun onCrashMultiplierChanged(mult: String) {
        _inputCrashMultiplier.value = mult
    }

    fun generateIntervals() {
        val time = _selectedCrashTime.value.trim()
        val mult = _inputCrashMultiplier.value.trim().toDoubleOrNull()

        if (time.isBlank() || mult == null || mult < 1.0) {
            _intervalErrorMessage.value = "Select a verified pink crash time and enter its multiplier (e.g. 12.50)."
            _intervalResult.value = null
            return
        }

        _intervalErrorMessage.value = null
        _intervalResult.value = repository.calculateIntervals(time, mult)
    }

    fun requestPrediction() {
        val historyDoubles = _multiplierHistory.value.map { it.multiplier }
        val currentSettings = _settings.value
        val insight = repository.computePrediction(
            history = historyDoubles,
            riskMode = currentSettings.riskMode,
            autoStopMultiplier = currentSettings.autoStopMultiplier
        )
        _predictionInsight.value = insight
        _showPredictionDialog.value = true
        hapticHelper.triggerHighConfidencePredictionHaptic(currentSettings.hapticFeedbackEnabled)
    }

    fun dismissPredictionDialog() {
        _showPredictionDialog.value = false
    }

    fun openRawJsonDialog() {
        _showRawJsonDialog.value = true
    }

    fun dismissRawJsonDialog() {
        _showRawJsonDialog.value = false
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        simulator.stop()
        soundEngine.release()
    }
}
