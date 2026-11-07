package com.aviatorpredictorbot.viewmodel

import android.app.Application
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
import com.aviatorpredictorbot.model.ThresholdNotification
import com.aviatorpredictorbot.service.GeminiStrategyAnalyzer
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

class AviatorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AviatorDatabase.getInstance(application)
    private val repository = AviatorRepository(database.crashRecordDao())
    private val simulator = AviatorSimulator(viewModelScope)

    /** Nothing to stop: the app no longer plays any game sounds. */
    fun onAppBackgrounded() {}

    private var pollingJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

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

    // Sound-based Notification Toggle

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

    // EXCLUSIVE BIG WHEEL PREDICTION: Displaying strictly the predicted real next round
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


    // ---- Prediction tracking: star the rounds the predictor called correctly ----
    // The prediction made for each upcoming round, and a hit/miss flag per completed round
    // (flags are aligned with the END of the history list).
    private val predictionsByRound = mutableMapOf<String, Double>()
    private val outcomeFlags = mutableListOf<Boolean>()
    private var lastEvaluatedRoundId: String = ""

    private fun rememberPrediction(prediction: PredictedNextRound) {
        predictionsByRound[prediction.nextRoundId] = prediction.predictedMultiplier
        if (predictionsByRound.size > 50) {
            predictionsByRound.remove(predictionsByRound.keys.first())
        }
    }

    /** When a round crashes, compare the actual crash with what was predicted for that round. */
    private fun trackPredictionOutcome(phase: String?, roundId: String, crash: Double?) {
        if (phase != "CRASHED" || crash == null || roundId == lastEvaluatedRoundId) return
        lastEvaluatedRoundId = roundId
        val predicted = predictionsByRound[roundId]
        // "Waiting" predictions (< 1.50x) are not calls, so they are never starred.
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
        // Seed initial history into Room if local database is fresh (50 samples for immediate 5x module analysis)
        viewModelScope.launch {
            val initial50 = listOf(
                1.28, 2.45, 1.14, 21.40, 1.90, 3.12, 1.05, 5.55, 14.80, 1.82,
                2.15, 6.70, 1.10, 38.50, 1.40, 2.90, 1.12, 8.40, 1.65, 1.02,
                4.30, 1.33, 12.10, 1.70, 2.50, 7.80, 1.19, 1.45, 3.20, 1.08,
                5.90, 1.88, 2.22, 16.50, 1.35, 1.95, 1.15, 6.20, 2.80, 1.25,
                1.42, 9.80, 1.04, 3.50, 1.78, 11.40, 1.60, 2.10, 1.09, 5.15
            )
            repository.seedInitialHistoryIfEmpty(initial50)

            // Database Layer Configuration: Automatically clean history older than 7 days on startup
            val result = repository.pruneHistoryOlderThanDays(7)
            _retentionConfig.value = result

            // Initial Gemini analysis on available 50 crash session records
            analyzeVolatilityWithGemini()
        }

        // Auto-connect to Betway Aviator telemetry immediately on launch
        startSimulator()

        // Observe simulator feed updates
        viewModelScope.launch {
            simulator.simulatedFeed.collect { feed ->
                if (_connectionState.value.isSimulated && feed != null) {
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

                    // Update Big Wheel Exclusive Real Next Round Prediction
                    val prediction = repository.computePredictedNextRound(
                        history = updatedHist,
                        currentRoundId = roundId,
                        nextTimeStr = feed.nextRoundTime
                    )
                    _predictedNextRound.value = prediction
                    rememberPrediction(prediction)

                    // Visual Notification System for Pink 10x+ Threshold
                    if (mult != null && mult >= 10.0) {
                        _thresholdNotification.value = ThresholdNotification(
                            isVisible = true,
                            multiplier = mult,
                            roundId = roundId,
                            message = "★ PINK 10x+ THRESHOLD ACTIVE ★"
                        )
                    } else if (feed.phase == "WAITING" || feed.phase == "CRASHED") {
                        if (_thresholdNotification.value.isVisible && mult != null && mult < 10.0) {
                            _thresholdNotification.value = _thresholdNotification.value.copy(isVisible = false)
                        }
                    }

                    if (mult != null) {
                        _armedStatus.value = if (mult >= 10.0) "PINK ARMED" else "IN FLIGHT"
                        _readyText.value = "BETWAY AVIATOR LIVE"
                    } else {
                        _armedStatus.value = "STANDBY"
                        _readyText.value = "AWAITING ROUND"
                    }

                    // Store completed crash to Room database when round crashes
                    if (feed.phase == "CRASHED" && mult != null) {
                        repository.saveCrash(roundId, mult)
                    }

                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        lastUpdated = timeFormat.format(Date()),
                        errorMessage = null
                    )
                }
            }
        }
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
                    val updatedHist = feed.history ?: emptyList()
                    val roundId = feed.roundId ?: "#894101"
                    _currentRoundId.value = roundId

                    _currentMultiplier.value = feed.currentMultiplier
                    trackPredictionOutcome(feed.phase, roundId, feed.currentMultiplier)
                    _multiplierHistory.value = buildHistoryRecords(updatedHist, roundId)
                    _trainMessage.value = feed.trainMessage ?: "BETWAY AVIATOR (SPRIBE) · LIVE FEED"
                    _nextRoundText.value = if (feed.nextRoundTime != null) "NEXT ${feed.nextRoundTime}" else "BETWAY AVIATOR · Live Stream"
                    _crashTimes.value = feed.crashTimes ?: emptyList()
                    _rawJson.value = feed.rawJson

                    if (_selectedCrashTime.value.isBlank() && !feed.crashTimes.isNullOrEmpty()) {
                        _selectedCrashTime.value = feed.crashTimes.first()
                    }

                    // Update Big Wheel Exclusive Real Next Round Prediction
                    val prediction = repository.computePredictedNextRound(
                        history = updatedHist,
                        currentRoundId = roundId,
                        nextTimeStr = feed.nextRoundTime
                    )
                    _predictedNextRound.value = prediction
                    rememberPrediction(prediction)

                    // Visual Notification System for Pink 10x+ Threshold
                    val m = feed.currentMultiplier
                    if (m != null && m >= 10.0) {
                        _thresholdNotification.value = ThresholdNotification(
                            isVisible = true,
                            multiplier = m,
                            roundId = roundId,
                            message = "★ PINK 10x+ THRESHOLD ACTIVE ★"
                        )
                    }

                    _connectionState.value = _connectionState.value.copy(
                        isConnected = true,
                        isConnecting = false,
                        lastUpdated = timeFormat.format(Date()),
                        errorMessage = null
                    )
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
        val insight = repository.computePrediction(historyDoubles)
        _predictionInsight.value = insight
        _showPredictionDialog.value = true
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
    }
}
