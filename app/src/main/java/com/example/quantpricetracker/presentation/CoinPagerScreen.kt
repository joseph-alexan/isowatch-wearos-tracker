package com.example.quantpricetracker.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import kotlin.math.abs

/**
 * Primary experience: one coin at a time, full screen, swipe left/right.
 * This is the professional Wear OS shape — huge hero price, big sparkline,
 * every element scaled to the 1 inch circle the user is actually looking at.
 *
 * Secondary interactions available here:
 *  - Tap currency pill (top) to toggle USD/CAD
 *  - Tap star to pin/unpin favorite
 *  - Tap "Overview" at bottom to jump to the list view
 */
@Composable
fun CoinPagerScreen(
    uiState: UiState,
    onToggleCurrency: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPageChanged: () -> Unit,
    onOpenOverview: () -> Unit
) {
    val coins = uiState.coins
    val pagerState = rememberPagerState(pageCount = { coins.size })

    // Haptic tick on page change — the single biggest "feels pro" touch.
    LaunchedEffect(pagerState.currentPage) {
        // Skip the initial emission when the pager first settles.
        if (pagerState.currentPage > 0 || pagerState.settledPage > 0) {
            onPageChanged()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IsowatchTokens.Background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val coin = coins[page]
            CoinHeroContent(
                coinPrice = coin,
                currency = uiState.currency,
                flash = uiState.priceFlash,
                isFavorite = coin.coin.symbol in uiState.favorites,
                isAmbient = uiState.isAmbient,
                lastUpdated = uiState.lastUpdated,
                errorText = uiState.error,
                onToggleCurrency = onToggleCurrency,
                onToggleFavorite = { onToggleFavorite(coin.coin.symbol) },
                onOpenOverview = onOpenOverview
            )
        }

        // Page indicator — small dots along the bottom of the screen.
        if (!uiState.isAmbient) {
            PageIndicator(
                total = coins.size,
                current = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun CoinHeroContent(
    coinPrice: CoinPrice,
    currency: Currency,
    flash: Boolean,
    isFavorite: Boolean,
    isAmbient: Boolean,
    lastUpdated: String,
    errorText: String?,
    onToggleCurrency: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenOverview: () -> Unit
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
    val textSecondary = if (isAmbient) IsowatchTokens.AmbientTextDim else IsowatchTokens.TextSecondary
    val accent = if (isAmbient) IsowatchTokens.AmbientText else IsowatchTokens.AccentBlue

    // Subtle flash on refresh — fades price color briefly. Skipped in ambient.
    val priceColor by animateColorAsState(
        targetValue = if (flash && !isAmbient && price != null) IsowatchTokens.AccentBlue else textPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "priceFlashAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Top row: currency pill + star, compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TinyPill(
                text = if (currency == Currency.USD) "USD" else "CAD",
                color = accent,
                isAmbient = isAmbient,
                onClick = onToggleCurrency
            )
            Text(
                text = if (isFavorite) "★" else "☆",
                fontSize = IsowatchTokens.MediumLabel,
                color = if (isFavorite) IsowatchTokens.AccentStar else textSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .wrapContentSize(Alignment.Center)
                    .clickableNoRipple { onToggleFavorite() }
            )
        }

        Spacer(Modifier.height(4.dp))

        // Symbol — bold, tracked, small
        Text(
            text = coinPrice.coin.symbol,
            fontSize = IsowatchTokens.SymbolLabel,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // Hero price — the thing the user actually came to see
        Text(
            text = price?.let { formatPrice(it, currency) } ?: "—",
            fontSize = IsowatchTokens.HeroPrice,
            color = priceColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        // 24h change
        if (change != null) {
            Text(
                text = formatChange(change),
                fontSize = IsowatchTokens.ChangePercent,
                color = changeColor,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(4.dp))

        // Sparkline — wide and meaningful
        if (coinPrice.sparkline.size >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(32.dp)
                    .then(
                        if (isAmbient) Modifier
                        else Modifier.background(IsowatchTokens.SurfaceSubtle, RoundedCornerShape(6.dp))
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Sparkline(
                    points = coinPrice.sparkline,
                    modifier = Modifier.fillMaxSize(),
                    color = changeColor,
                    ambient = isAmbient
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Footer — last updated time, or error banner, or overview link.
        // Very compact, gray, 9sp. Never fights for attention.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                errorText != null -> Text(
                    text = "offline",
                    fontSize = IsowatchTokens.MicroLabel,
                    color = IsowatchTokens.Warning
                )
                lastUpdated.isNotEmpty() -> Text(
                    text = "↻ $lastUpdated",
                    fontSize = IsowatchTokens.MicroLabel,
                    color = textSecondary
                )
            }
            if (!isAmbient) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Overview",
                    fontSize = IsowatchTokens.MicroLabel,
                    color = IsowatchTokens.TextTertiary,
                    modifier = Modifier.clickableNoRipple(onClick = onOpenOverview)
                )
            }
        }
    }
}

@Composable
private fun TinyPill(
    text: String,
    color: Color,
    isAmbient: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(
                if (isAmbient)
                    Modifier.border(0.8.dp, IsowatchTokens.AmbientOutline, RoundedCornerShape(8.dp))
                else Modifier.background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            )
            .clickableNoRipple(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = IsowatchTokens.MicroLabel,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PageIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(IsowatchTokens.PagerIndicatorDotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val active = i == current
            Box(
                modifier = Modifier
                    .size(if (active) IsowatchTokens.PagerIndicatorDotSize + 1.dp else IsowatchTokens.PagerIndicatorDotSize)
                    .background(
                        if (active) IsowatchTokens.AccentBlue else IsowatchTokens.TextTertiary,
                        CircleShape
                    )
            )
        }
    }
}
