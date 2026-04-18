package com.example.quantpricetracker.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

enum class Currency { USD, CAD }

data class UiState(
    val coins: List<CoinPrice> = Coin.values().map {
        CoinPrice(it, null, null, null, emptyList())
    },
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastUpdated: String = "",
    val currency: Currency = Currency.USD,
    val priceFlash: Boolean = false,
    val isAmbient: Boolean = false
)

class CryptoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ISOWATCH"
        private const val POLL_NORMAL_MS = 120_000L    // 2 min active
        private const val POLL_AMBIENT_MS = 300_000L   // 5 min ambient
        private const val ALERT_THRESHOLD_PCT = 5.0
        private val BACKOFF_LADDER_MS = longArrayOf(5_000, 15_000, 60_000, 300_000)
    }

    private val api = CoinGeckoApi.create()
    private val prefs = PreferencesRepository(application)
    private val haptics = HapticsController(application)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val sessionStartPrices = mutableMapOf<String, Double>()
    private val alertedCoins = mutableSetOf<String>()

    private var pollingJob: Job? = null
    private var consecutiveFailures = 0

    init {
        viewModelScope.launch {
            val savedCurrency = prefs.currencyFlow.first()
            val savedFavorites = prefs.favoritesFlow.first()
            _uiState.value = _uiState.value.copy(
                currency = savedCurrency,
                favorites = savedFavorites,
                coins = sortCoinsByFavorites(_uiState.value.coins, savedFavorites)
            )
            fetchPrices(isInitial = true)
        }
        viewModelScope.launch {
            prefs.favoritesFlow.collect { favs ->
                _uiState.value = _uiState.value.copy(
                    favorites = favs,
                    coins = sortCoinsByFavorites(_uiState.value.coins, favs)
                )
            }
        }
    }

    /** Start the polling loop. Safe to call repeatedly — only one runs at a time. */
    fun startPolling() {
        if (pollingJob?.isActive == true) return
        Log.d(TAG, "Starting polling loop")
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val waitMs = nextWaitMs()
                delay(waitMs)
                if (!_uiState.value.isLoading && !_uiState.value.isRefreshing) {
                    fetchPrices(isInitial = false)
                }
            }
        }
    }

    fun stopPolling() {
        Log.d(TAG, "Stopping polling loop")
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun nextWaitMs(): Long {
        // Backoff takes priority when there have been failures.
        if (consecutiveFailures > 0) {
            val idx = min(consecutiveFailures - 1, BACKOFF_LADDER_MS.size - 1)
            return BACKOFF_LADDER_MS[idx]
        }
        return if (_uiState.value.isAmbient) POLL_AMBIENT_MS else POLL_NORMAL_MS
    }

    fun setAmbient(ambient: Boolean) {
        if (_uiState.value.isAmbient == ambient) return
        Log.d(TAG, "Ambient mode: $ambient")
        _uiState.value = _uiState.value.copy(isAmbient = ambient)
    }

    fun manualRefresh() {
        haptics.perform(HapticsController.Pattern.REFRESH)
        consecutiveFailures = 0 // user-initiated reset
        viewModelScope.launch { fetchPrices(isInitial = false) }
    }

    fun toggleCurrency() {
        val next = if (_uiState.value.currency == Currency.USD) Currency.CAD else Currency.USD
        _uiState.value = _uiState.value.copy(currency = next)
        haptics.perform(HapticsController.Pattern.TICK)
        viewModelScope.launch { prefs.setCurrency(next) }
    }

    fun toggleFavorite(coinSymbol: String) {
        haptics.perform(HapticsController.Pattern.LONG_PRESS)
        viewModelScope.launch { prefs.toggleFavorite(coinSymbol) }
    }

    /** Called from UI on page change (HorizontalPager swipe). */
    fun onPageChanged() {
        haptics.perform(HapticsController.Pattern.TICK)
    }

    private suspend fun fetchPrices(isInitial: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = isInitial,
            isRefreshing = !isInitial,
            error = null
        )
        try {
            val ids = Coin.values().joinToString(",") { it.id }
            Log.d(TAG, "Fetching prices for ${Coin.values().size} coins")
            val response = withContext(Dispatchers.IO) { api.getPrices(ids = ids) }
            Log.d(TAG, "Price fetch OK — ${response.size} coins returned")

            val baseCoins = Coin.values().map { coin ->
                val data = response[coin.id]
                val existingSparkline = _uiState.value.coins
                    .firstOrNull { it.coin == coin }?.sparkline ?: emptyList()
                CoinPrice(
                    coin = coin,
                    priceUsd = data?.get("usd"),
                    priceCad = data?.get("cad"),
                    change24h = data?.get("usd_24h_change"),
                    sparkline = existingSparkline
                )
            }

            checkThresholdAlerts(baseCoins)

            val sorted = sortCoinsByFavorites(baseCoins, _uiState.value.favorites)
            val now = timeFormat.format(Date())

            _uiState.value = _uiState.value.copy(
                coins = sorted,
                isLoading = false,
                isRefreshing = false,
                error = null,
                lastUpdated = now,
                priceFlash = true
            )

            consecutiveFailures = 0
            delay(600)
            _uiState.value = _uiState.value.copy(priceFlash = false)

            // Fetch sparklines opportunistically — they come from a separate endpoint
            // per coin, so we do them in the background without blocking price display.
            if (isInitial || _uiState.value.coins.any { it.sparkline.isEmpty() }) {
                fetchSparklinesInBackground()
            }

        } catch (e: Exception) {
            consecutiveFailures++
            Log.w(TAG, "Price fetch failed (attempt $consecutiveFailures): ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                error = "Network error — showing last known prices"
            )
        }
    }

    private fun fetchSparklinesInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            Coin.values().forEach { coin ->
                try {
                    val chart = api.getMarketChart(id = coin.id, currency = "usd", days = 1)
                    val prices = chart.prices.mapNotNull { it.getOrNull(1) }
                    val sampled = downsample(prices, targetSize = 24)
                    val current = _uiState.value.coins
                    val updated = current.map { cp ->
                        if (cp.coin == coin) cp.copy(sparkline = sampled) else cp
                    }
                    _uiState.value = _uiState.value.copy(
                        coins = sortCoinsByFavorites(updated, _uiState.value.favorites)
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Sparkline fetch failed for ${coin.symbol}: ${e.message}")
                }
            }
        }
    }

    private fun downsample(source: List<Double>, targetSize: Int): List<Double> {
        if (source.size <= targetSize) return source
        val step = source.size.toDouble() / targetSize
        return (0 until targetSize).map { i ->
            source[(i * step).toInt().coerceAtMost(source.lastIndex)]
        }
    }

    /**
     * Session-based alert: compares current price to the price when the app session
     * began. Fires a haptic alert once per coin per session when |change| >= 5%.
     * Much more useful than comparing adjacent 60s refreshes (which rarely cross 5%).
     */
    private fun checkThresholdAlerts(coins: List<CoinPrice>) {
        var triggered = false
        coins.forEach { cp ->
            val price = cp.priceUsd ?: return@forEach
            val symbol = cp.coin.symbol
            val start = sessionStartPrices[symbol]
            if (start == null) {
                sessionStartPrices[symbol] = price
                return@forEach
            }
            val changePct = abs((price - start) / start * 100)
            if (changePct >= ALERT_THRESHOLD_PCT && symbol !in alertedCoins) {
                alertedCoins += symbol
                triggered = true
                Log.d(TAG, "Threshold crossed for $symbol: ${"%.2f".format(changePct)}%")
            }
        }
        if (triggered) haptics.perform(HapticsController.Pattern.ALERT)
    }

    private fun sortCoinsByFavorites(
        coins: List<CoinPrice>,
        favorites: Set<String>
    ): List<CoinPrice> {
        if (favorites.isEmpty()) return coins
        val (fav, rest) = coins.partition { it.coin.symbol in favorites }
        return fav + rest
    }
}
