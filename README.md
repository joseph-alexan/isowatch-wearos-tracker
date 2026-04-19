# ISOWATCH — Wear OS ISO 20022 Crypto Tracker

![Wear OS](https://img.shields.io/badge/Platform-Wear%20OS-4285F4?style=flat-square&logo=wear-os&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![API](https://img.shields.io/badge/API-CoinGecko-brightgreen?style=flat-square)
![Version](https://img.shields.io/badge/Version-2.0-success?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

**ISOWATCH** is a minimalist, real-time price tracker for **ISO 20022 compliant cryptocurrencies**, built natively for **Wear OS** using **Jetpack Compose**. Glanceable, battery-aware, and designed for the 1.2" OLED canvas — not a phone UI shrunk to fit.

Built for Web3 traders, DeFi users, and builders who follow the coins positioned at the convergence of on-chain infrastructure and global financial messaging standards.

---

## ISO 20022 Compliant Coin Tracker

ISO 20022 is the international standard for financial messaging used in payments, securities, and trade finance. The coins tracked in this app have been identified as ISO 20022 compliant or compatible — built to integrate with the modern global financial system.

| Coin | Symbol | ISO 20022 Status |
|---|---|---|
| Quant | QNT | Compliant — interoperability layer for financial networks |
| XRP | XRP | Compliant — adopted for cross-border payments via RippleNet |
| Stellar | XLM | Compliant — designed for global remittance and payments |
| Cardano | ADA | Compliant — smart contract platform aligned with standards |
| Algorand | ALGO | Compliant — used by financial institutions and central banks |
| Hedera | HBAR | Compliant — enterprise-grade distributed ledger |
| IOTA | IOTA | Compliant — machine economy and IoT financial transactions |
| XDC Network | XDC | Compliant — trade finance and cross-border settlement |

These coins represent the intersection of **Web3 infrastructure and traditional financial rails** — a space where on-chain settlement, cross-border payments, and the next generation of DeFi protocols actually meet institutional adoption.

---

## What's New in v2

Version 2.0 is a ground-up UX and architecture overhaul focused on making ISOWATCH feel like a native Wear OS app — not an Android app ported to a watch.

**Primary experience redesigned.** The app now opens into a **HorizontalPager** where each coin gets the full screen: hero-sized monospace price, 24h change with muted green/red indicators, and a 24-hour autoscaled sparkline. Swipe between coins with haptic ticks on each page change. The original scrolling list is preserved as a secondary **Overview** screen, reachable from any coin page.

**Ambient mode implemented properly.** Uses `AmbientLifecycleObserver` to switch into a burn-in-safe monochrome layout when the watch dims — outline-only surfaces, no filled shapes, thinner sparkline stroke, and polling backs off from 60 seconds to 5 minutes to preserve battery.

**Persistent preferences.** Currency selection (USD/CAD) and pinned favorites survive app restarts via Jetpack DataStore, exposed as reactive Flows. Long-press any coin to pin it — favorites float to the top of both the pager and list views.

**Lifecycle-aware polling.** The refresh loop is wrapped in `repeatOnLifecycle(STARTED)`, so network calls pause automatically when the app is backgrounded. On failure, an exponential backoff (5s → 15s → 60s → 5min) prevents hammering CoinGecko during outages.

**Professional haptic vocabulary.** Centralized `HapticsController` maps each user action to a distinct pattern using `HapticFeedbackConstants` where possible (for consistency with the OS), with `VibrationEffect` waveforms as fallback for custom patterns. Page changes tick, threshold crossings buzz, errors pulse long.

**Design system.** All colors, typography sizes, and spacing live in a single `IsowatchTokens` object. Palette is deliberately restrained: pure `#000000` black for OLED efficiency, a single cold signature blue (`#7AB8FF`), and muted greens/reds instead of saturated arcade hues. Prices render in monospace so digits don't shift between refreshes.

---

## Features

| Feature | Description |
|---|---|
| Hero Pager | Swipe between 8 coins, full-screen per coin, haptic page ticks |
| Overview List | Compact secondary view with inline sparklines for all coins |
| 24h Sparklines | Canvas-drawn, autoscaled, ambient-aware |
| USD / CAD Toggle | Tap to switch, persists across restarts |
| Favorites | Long-press to pin, floats to top |
| Ambient Mode | Monochrome, outline-only, burn-in safe |
| Haptic Vocabulary | Distinct patterns for tick, confirm, alert, error |
| Session Alerts | Haptic fires when a coin moves ≥5% since app launch |
| Exponential Backoff | Graceful network recovery: 5s → 15s → 60s → 5min |
| Resilient UI | Last known prices preserved on network failure |

---

## Architecture

```
presentation/
├── MainActivity.kt             # Entry: AmbientLifecycleObserver + SwipeDismissableNavHost
├── CryptoViewModel.kt          # State, polling, haptics, threshold alerts, backoff
├── CoinGeckoApi.kt             # Retrofit: /simple/price and /market_chart
├── CoinPagerScreen.kt          # Primary: HorizontalPager, one coin per page
├── OverviewListScreen.kt       # Secondary: compact ScalingLazyColumn
├── Sparkline.kt                # Canvas composable, autoscaled, ambient-aware
├── PreferencesRepository.kt    # DataStore wrapper — currency + favorites as Flows
├── HapticsController.kt        # Centralized haptic patterns
├── IsowatchTokens.kt           # Design tokens — palette, typography, spacing
├── Formatting.kt               # Consistent price/change formatters
├── ModifierExtensions.kt       # Shared Modifier helpers
└── theme/
    └── Theme.kt                # IsowatchTheme — Wear Material
```

### Tech Stack

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose for Wear OS
- **Architecture:** MVVM (AndroidViewModel + StateFlow, lifecycle-aware)
- **Navigation:** Wear Compose Navigation (`SwipeDismissableNavHost`)
- **Persistence:** Jetpack DataStore (Preferences)
- **Networking:** Retrofit 2 + OkHttp + Gson
- **Async:** Kotlin Coroutines (`repeatOnLifecycle`, `StateFlow`, `Flow`)
- **Ambient:** `androidx.wear:wear` 1.3.0 `AmbientLifecycleObserver`
- **API:** [CoinGecko Free API](https://www.coingecko.com/en/api) — `/simple/price` + `/coins/{id}/market_chart`

---

## Design Philosophy

ISOWATCH follows a few deliberate principles that shape every screen:

**Glanceability first.** Every screen is readable in under one second. Hero prices render at 30sp in monospace; metadata shrinks to 9–10sp. There is no middle typography tier — contrast forces hierarchy.

**Subtraction over addition.** Most "pro feel" on Wear OS comes from removing chrome, not adding features. No persistent header, no always-visible timestamps, no redundant refresh buttons — content fills the screen.

**OLED-native palette.** Pure `#000000` everywhere means OLED pixels are *off*, not rendered. This saves battery and creates infinite contrast. Card backgrounds are transparent; only negative space separates elements.

**Ambient mode is not an afterthought.** The dimmed view is designed, not degraded — thinner strokes, monochrome, shifted surfaces for burn-in safety, longer polling intervals for battery.

**Haptics speak a vocabulary.** Each action has a distinct feel. Page changes tick, confirmations pulse, alerts double-buzz. Users learn the language.

---

## Security & Privacy

- **No API keys** — uses CoinGecko's public free-tier endpoint
- **No user data collected** — fully local, no accounts, no analytics, no cloud sync
- **Minimal permissions** — `INTERNET`, `VIBRATE`, `ACCESS_NETWORK_STATE`, `RECEIVE_AMBIENT_MODE`
- **Read-only API calls** — no write operations, no authentication required
- **HTTPS only** — all network calls over TLS
- **Local preferences storage** — DataStore writes to app-private storage

Built with minimal-permission principles, secure API consumption, and privacy by design — drawing on application security fundamentals and Web3's ethos of user sovereignty over data.

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Wear OS emulator (Wear OS 4+, API 30+) or physical Wear OS device
- JDK 11+

### Installation

```bash
git clone https://github.com/joseph-alexan/isowatch-wearos-tracker.git
cd isowatch-wearos-tracker
git checkout feature/multi-coin-upgrades
```

Open the project in Android Studio, let Gradle sync, then run on a Wear OS emulator or device.

### Build

```bash
./gradlew :app:assembleDebug
```

Min SDK 30, target SDK 36, Kotlin JVM target 11.

---

## Usage

- **Swipe left/right** on the main screen to move between coins
- **Tap the USD/CAD pill** at the top-left to toggle currency
- **Tap the star** at the top-right (or long-press a row in Overview) to pin a favorite
- **Tap "Overview"** at the bottom to jump to the compact list view
- **Swipe right** from any secondary screen to go back
- **Lower your wrist** to trigger ambient mode — UI switches to monochrome

---

## Roadmap

Planned for future versions:

- **Wear OS Tile** — top 3 pinned coins, one swipe from the watchface
- **Watchface Complication** — favorite coin price on the watchface itself
- **Rotary input** — bezel/crown scrubs between coins with haptic detents
- **Pull-to-refresh** on the overview list with visible indicator
- **7d / 30d chart timeframes** on a detail view
- **Package rename** — `com.example.quantpricetracker` → `com.alexanlabs.isowatch`

---

## API Reference

### Current prices
```
GET https://api.coingecko.com/api/v3/simple/price
    ?ids=quant-network,ripple,stellar,cardano,algorand,hedera-hashgraph,iota,xdce-crowd-sale
    &vs_currencies=usd,cad
    &include_24hr_change=true
```

### 24h price history (sparklines)
```
GET https://api.coingecko.com/api/v3/coins/{id}/market_chart
    ?vs_currency=usd
    &days=1
    &interval=hourly
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable release — v1 single-coin baseline |
| `feature/multi-coin-upgrades` | v2 — active development, ISOWATCH rebrand |

---

## About This Project

A Web3/DeFi-focused Wear OS app demonstrating production-quality mobile development for the crypto ecosystem:

- Native Wear OS UI with Jetpack Compose — `HorizontalPager`, `ScalingLazyColumn`, `SwipeDismissableNavHost`
- MVVM with lifecycle-aware polling, `StateFlow`, and reactive DataStore Flows
- REST API integration with Retrofit, coroutines, and background sparkline fetching
- Real ambient mode using `AmbientLifecycleObserver` with burn-in-safe rendering
- Haptic vocabulary using `HapticFeedbackConstants` for OS-native feel
- Exponential backoff and graceful error handling on network failure
- Centralized design tokens and a deliberate OLED-first palette
- Clean separation of concerns — one file per responsibility

---

## Author

**Joseph Alexan** — Alexan Labs
- GitHub: [@joseph-alexan](https://github.com/joseph-alexan)
- Focus: Cybersecurity · Web3 / DeFi · Blockchain · AI

---

## License

MIT License.