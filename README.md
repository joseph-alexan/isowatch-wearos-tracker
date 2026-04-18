# ISOWATCH

Native Wear OS cryptocurrency tracker for ISO 20022-aligned coins.
Built with Kotlin and Jetpack Compose for Wear.

## Tracked coins

QNT · XRP · XLM · ADA · HBAR · ALGO · IOTA · XDC

## Features (v2)

- **Hero pager** — one coin per page, full-screen, swipe to navigate
- **Overview list** — compact list of all coins with sparklines
- **USD / CAD toggle** — persists across app restarts
- **24h change** — muted green/red, arrow indicator
- **Sparklines** — 24-hour autoscaled price line per coin
- **Favorites** — long-press to pin coins to the top
- **Haptic vocabulary** — distinct patterns for tick / confirm / alert
- **Ambient mode** — monochrome, outline-only, OLED-safe
- **Lifecycle-aware polling** — pauses when app is backgrounded
- **Exponential backoff** — graceful recovery from network errors
- **Session-based alerts** — haptic when a coin moves ≥5% since app open

## Build

```
./gradlew :app:assembleDebug
```

Requires Android Studio with Wear OS SDK. Min SDK 30, target SDK 36, Kotlin JVM 11.
