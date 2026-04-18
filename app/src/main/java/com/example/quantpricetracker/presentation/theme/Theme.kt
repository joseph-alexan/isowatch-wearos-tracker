package com.example.quantpricetracker.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

/**
 * ISOWATCH theme — deliberately minimal. All colors are defined inline at use-sites
 * because we want dense control over the palette for OLED ambient performance
 * and the design language is strict monochrome + two accents.
 */
@Composable
fun IsowatchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}
