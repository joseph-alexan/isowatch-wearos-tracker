#ISOWATCH — Wear OS ISO 20022 Crypto Tracker

![Wear OS](https://img.shields.io/badge/Platform-Wear%20OS-4285F4?style=flat-square&logo=wear-os&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/API-CoinGecko-brightgreen?style=flat-square)
![Branch](https://img.shields.io/badge/Branch-feature%2Fmulti--coin--upgrades-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

A minimalist, real-time cryptocurrency price tracker built natively for **Wear OS** using **Jetpack Compose**. Displays live prices for 8 coins directly on your wrist, with 24h change indicators, haptic alerts, and a USD/CAD currency toggle.

This app intentionally tracks a curated selection of **ISO 20022 compliant cryptocurrencies** — digital assets aligned with the global financial messaging standard being adopted by SWIFT, central banks, and major financial institutions worldwide.

---

## ISO 20022 Compliant Coin Tracker

ISO 20022 is the international standard for financial messaging used in payments, securities, and trade finance. Several of the coins tracked in this app have been identified as ISO 20022 compliant or compatible, meaning they are built to integrate with the modern global financial system.

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

These coins represent the intersection of **blockchain technology and traditional financial infrastructure** — a space increasingly relevant to cybersecurity professionals working in fintech, banking, and critical infrastructure protection.

---

## Features

| Feature | Description |
|---|---|
| 8 Coins Tracked | QNT, XRP, XLM, ADA, ALGO, HBAR, IOTA, XDC |
| 24h % Change | Green/red indicators showing price movement |
| USD / CAD Toggle | Tap to switch currency instantly |
| Haptic Alerts | Watch vibrates on price moves of 5% or more |
| Price Flash Animation | Visual feedback on every price update |
| Auto Refresh | Prices update automatically every 60 seconds |
| Resilient UI | Last known prices preserved on network error |
| Battery Aware | Auto-refresh pauses when screen is off |

---

## Architecture & Tech Stack

```
presentation/
├── MainActivity.kt        # Compose UI — ScalingLazyColumn coin list
├── CryptoViewModel.kt     # AndroidViewModel — state management & haptic
├── CoinGeckoApi.kt        # Retrofit interface — CoinGecko REST API
└── theme/
    └── Theme.kt           # Wear OS Material Theme
```

- **Language:** Kotlin 100%
- **UI:** Jetpack Compose for Wear OS
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Networking:** Retrofit 2 + OkHttp + Gson
- **Async:** Kotlin Coroutines
- **API:** [CoinGecko Free API](https://www.coingecko.com/en/api)

---

## Security & Privacy

- No API keys stored — uses CoinGecko's public free-tier endpoint
- No user data collected — fully local, no accounts or tracking
- No sensitive permissions — only `INTERNET`, `VIBRATE`, `ACCESS_NETWORK_STATE`
- Read-only API calls — no write operations or authentication required
- All network calls made over HTTPS

This project demonstrates an understanding of minimal permission principles, secure API consumption, and data privacy by design — core concepts in application security.

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Wear OS emulator or physical Wear OS device (API 30+)
- JDK 11+

### Installation

```bash
# Clone the repository
git clone https://github.com/joseph-alexan/wearos-crypto-tracker.git

# Switch to the feature branch
cd wearos-crypto-tracker
git checkout feature/multi-coin-upgrades
```

Open the project in Android Studio, let Gradle sync, then run on a Wear OS emulator or device.

---

## API Reference

This app uses the CoinGecko Simple Price API (free, no key required):

```
GET https://api.coingecko.com/api/v3/simple/price
    ?ids=quant-network,ripple,stellar,cardano,...
    &vs_currencies=usd,cad
    &include_24hr_change=true
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable release — original QNT-only tracker |
| `feature/multi-coin-upgrades` | Active development — multi-coin, haptic, animations |

---

## About This Project

This project was built to demonstrate practical Android and Wear OS development skills including:

- Native Wear OS UI with Jetpack Compose and `ScalingLazyColumn`
- MVVM architecture with clean separation of concerns
- REST API integration with Retrofit and coroutines
- State management with `StateFlow` and `collectAsState`
- Haptic feedback using `VibrationEffect` across API levels
- Real-world error handling — graceful degradation on network failure

---

## Author

**Joseph Alexan**
- GitHub: [@joseph-alexan](https://github.com/joseph-alexan)
- Interests: Cybersecurity · Android Development · Blockchain

---

## License

This project is licensed under the MIT License.
