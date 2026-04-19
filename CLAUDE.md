# ISOWATCH — Context for Claude Code

## What this is
Native Wear OS app tracking real-time ISO 20022-aligned cryptocurrency prices.
Kotlin + Jetpack Compose for Wear. Personal daily-use app — I am the only user.
Built by Joseph Alexan / Alexan Labs.

## Package note
Package is still `com.example.quantpricetracker` from the original template.
The user-facing rebrand to ISOWATCH is complete (app name, theme class, project
name, tokens, README). Package refactor is deferred — use Android Studio's
Refactor → Rename Package to do it atomically later.

## Architecture
- `MainActivity` — entry point. Hosts AmbientLifecycleObserver, lifecycle-aware
  polling, and SwipeDismissableNavHost between pager and overview.
- `CoinPagerScreen` — primary hero view: HorizontalPager, one coin per page
- `OverviewListScreen` — secondary compact list with sparklines
- `CryptoViewModel` — state, polling, haptics, threshold alerts, backoff
- `CoinGeckoApi` — Retrofit. /simple/price for current, /market_chart for sparklines
- `PreferencesRepository` — DataStore for currency + favorites as Flows
- `HapticsController` — centralized haptic vocabulary; prefers
  HapticFeedbackConstants on a View, falls back to VibrationEffect waveforms
- `IsowatchTokens` — single source of truth for colors + typography sizes
- `Sparkline` — Canvas composable, autoscaled, ambient-aware
- `Formatting`, `ModifierExtensions` — small helpers

## Design language
- Pure #000000 everywhere (OLED pixels off = battery saved)
- Single signature accent: cold desaturated blue (#7AB8FF)
- Muted green/red for deltas, not saturated arcade hues
- Violent typography hierarchy: 38sp hero prices, 9-12sp metadata
- Monospace prices (digits don't jiggle between refreshes)
- Ambient mode: strict monochrome, outlines only, no filled shapes

## Rules for Claude Code
- Propose a plan before multi-file refactors; wait for approval.
- Always run `./gradlew :app:assembleDebug` after edits, fix errors before stopping.
- Prefer Wear Compose patterns over Material (non-Wear) where both exist.
- No analytics, no cloud sync — personal app.
- Haptic changes cannot be tested on emulator — flag for on-device verification.
- Commit messages: conventional commits (feat:, fix:, refactor:, perf:, chore:).

## Still to build
- Wear OS Tile showing top 3 pinned coins
- ComplicationDataSourceService for favorite coin on watchface
- Rotary input (onRotaryScrollEvent) to scrub between coins
- Pull-to-refresh gesture on overview list
- 7d/30d toggle on detail view (needs detail view first)
- Package rename to com.alexanlabs.isowatch
- Unit tests for downsample() and checkThresholdAlerts()

## Priorities, in order
1. Glanceability — readable in under 1 second
2. Battery — ambient, paused polling, backoff
3. Immersion — haptics, sparklines, tiles
4. Polish over backwards compatibility
