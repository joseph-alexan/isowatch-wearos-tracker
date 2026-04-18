package com.example.quantpricetracker.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text

/**
 * Secondary: compact list view of all coins.
 * Tapping a row jumps back to the pager at that coin's page.
 * Long-press pins/unpins favorites (floats them to top).
 */
@Composable
fun OverviewListScreen(
    uiState: UiState,
    onCoinTap: (String) -> Unit,
    onCoinLongPress: (String) -> Unit,
    onToggleCurrency: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IsowatchTokens.Background)
    ) {
        if (uiState.isLoading && uiState.coins.all { it.priceUsd == null }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "loading",
                        fontSize = IsowatchTokens.MicroLabel,
                        color = IsowatchTokens.TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 24.dp, horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    OverviewHeader(
                        currency = uiState.currency,
                        lastUpdated = uiState.lastUpdated,
                        isRefreshing = uiState.isRefreshing,
                        isAmbient = uiState.isAmbient,
                        error = uiState.error,
                        onToggleCurrency = onToggleCurrency
                    )
                }
                items(uiState.coins) { cp ->
                    CompactCoinRow(
                        coinPrice = cp,
                        currency = uiState.currency,
                        flash = uiState.priceFlash,
                        isFavorite = cp.coin.symbol in uiState.favorites,
                        isAmbient = uiState.isAmbient,
                        onTap = { onCoinTap(cp.coin.symbol) },
                        onLongPress = { onCoinLongPress(cp.coin.symbol) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(
    currency: Currency,
    lastUpdated: String,
    isRefreshing: Boolean,
    isAmbient: Boolean,
    error: String?,
    onToggleCurrency: () -> Unit
) {
    val textSecondary = if (isAmbient) IsowatchTokens.AmbientTextDim else IsowatchTokens.TextSecondary
    val accent = if (isAmbient) IsowatchTokens.AmbientText else IsowatchTokens.AccentBlue

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (isAmbient)
                            Modifier.border(0.8.dp, IsowatchTokens.AmbientOutline, RoundedCornerShape(8.dp))
                        else Modifier.background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    )
                    .clickableNoRipple(onClick = onToggleCurrency)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (currency == Currency.USD) "USD" else "CAD",
                    fontSize = IsowatchTokens.MicroLabel,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp
                )
            } else if (lastUpdated.isNotEmpty()) {
                Text(
                    text = "↻ $lastUpdated",
                    fontSize = IsowatchTokens.MicroLabel,
                    color = textSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "offline — last known prices",
                fontSize = IsowatchTokens.MicroLabel,
                color = IsowatchTokens.Warning,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactCoinRow(
    coinPrice: CoinPrice,
    currency: Currency,
    flash: Boolean,
    isFavorite: Boolean,
    isAmbient: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val price = if (currency == Currency.USD) coinPrice.priceUsd else coinPrice.priceCad
    val change = coinPrice.change24h
    val changeColor = when {
        change == null -> IsowatchTokens.TextSecondary
        isAmbient -> if (change >= 0) IsowatchTokens.AmbientPositive else IsowatchTokens.AmbientNegative
        change >= 0 -> IsowatchTokens.Positive
        else -> IsowatchTokens.Negative
    }
    val textPrimary = if (isAmbient) IsowatchTokens.AmbientText else IsowatchTokens.TextPrimary
    val accent = if (isAmbient) IsowatchTokens.AmbientText else IsowatchTokens.AccentBlue

    val priceColor by animateColorAsState(
        targetValue = if (flash && !isAmbient && price != null) IsowatchTokens.AccentBlue else textPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "rowFlashAnim"
    )

    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isAmbient)
                    Modifier.border(0.8.dp, IsowatchTokens.AmbientOutline, shape)
                else Modifier
            )
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = coinPrice.coin.symbol,
                    fontSize = IsowatchTokens.SymbolLabel,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (isFavorite) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "★",
                        fontSize = IsowatchTokens.MicroLabel,
                        color = if (isAmbient) IsowatchTokens.AmbientTextDim else IsowatchTokens.AccentStar
                    )
                }
            }

            // Sparkline, small, center
            if (coinPrice.sparkline.size >= 2) {
                Sparkline(
                    points = coinPrice.sparkline,
                    modifier = Modifier
                        .width(36.dp)
                        .height(14.dp),
                    color = changeColor,
                    ambient = isAmbient
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (price != null) {
                    Text(
                        text = formatPrice(price, currency),
                        fontSize = IsowatchTokens.MediumLabel,
                        color = priceColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (change != null) {
                        Text(
                            text = formatChange(change),
                            fontSize = IsowatchTokens.MicroLabel,
                            color = changeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Text("—", fontSize = IsowatchTokens.MediumLabel, color = IsowatchTokens.TextSecondary)
                }
            }
        }
    }
}
