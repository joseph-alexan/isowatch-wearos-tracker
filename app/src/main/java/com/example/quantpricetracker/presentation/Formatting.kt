package com.example.quantpricetracker.presentation

import kotlin.math.abs

/**
 * Price formatting with mono-friendly spacing. The key professional touch:
 * we use fixed decimal places per magnitude so the digit columns align across refreshes.
 */
fun formatPrice(price: Double, currency: Currency): String {
    val symbol = "$"
    return when {
        price >= 1000 -> "$symbol${"%,.0f".format(price)}"
        else          -> "$symbol${"%.2f".format(price)}"
    }
}

fun formatChange(change: Double?): String {
    if (change == null) return "—"
    val arrow = if (change >= 0) "▲" else "▼"
    return "$arrow ${"%.2f".format(abs(change))}%"
}
