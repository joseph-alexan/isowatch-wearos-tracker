package com.example.quantpricetracker.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ISOWATCH design tokens.
 *
 * Philosophy:
 *   - Pure black background everywhere (OLED pixels are OFF, saves battery, infinite contrast).
 *   - One signature accent (Iso Blue) used sparingly.
 *   - Muted green/red for price deltas — saturated hues read as "gaming RGB" on OLED.
 *   - Typography is violent: hero numbers at 36sp+, metadata at 9–10sp. No middle tier.
 *   - Ambient palette is strictly monochrome for burn-in safety.
 */
object IsowatchTokens {

    // === Colors — Active ===
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF000000)                   // No card backgrounds — negative space only
    val SurfaceSubtle = Color(0xFF0A0A0A)             // Used for detail-screen sparkline well only
    val TextPrimary = Color(0xFFF2F2F2)
    val TextSecondary = Color(0xFF9A9A9A)
    val TextTertiary = Color(0xFF5C5C5C)

    val AccentBlue = Color(0xFF7AB8FF)                // Iso signature — cold, calm, instrument-like
    val AccentBlueDim = Color(0xFF4A7BAE)
    val AccentStar = Color(0xFFFFD54F)                // Favorite/pinned indicator

    val Positive = Color(0xFF4ADE80)                  // Muted green — reads premium, not arcade
    val Negative = Color(0xFFF87171)                  // Muted red
    val Warning = Color(0xFFFFB86B)                   // Network banner

    // === Colors — Ambient (monochrome only) ===
    val AmbientBackground = Color(0xFF000000)
    val AmbientText = Color(0xFFCCCCCC)
    val AmbientTextDim = Color(0xFF888888)
    val AmbientOutline = Color(0xFF444444)
    val AmbientPositive = Color(0xFFCCCCCC)
    val AmbientNegative = Color(0xFFAAAAAA)

    // === Typography sizes ===
    val HeroPrice = 30.sp
    val BigPrice = 22.sp
    val MediumLabel = 14.sp
    val ChangePercent = 13.sp
    val SymbolLabel = 12.sp
    val MetadataLabel = 10.sp
    val MicroLabel = 9.sp

    // === Spacing ===
    val PagerIndicatorDotSize = 4.dp
    val PagerIndicatorDotSpacing = 4.dp
}
