package com.example.quantpricetracker.presentation

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Coins tracked by ISOWATCH — all ISO 20022-aligned.
 * Order here is the default display order (before favorites sort applied).
 */
enum class Coin(
    val id: String,
    val symbol: String,
    val displayName: String
) {
    QNT("quant-network", "QNT", "Quant"),
    XRP("ripple", "XRP", "XRP"),
    XLM("stellar", "XLM", "Stellar"),
    ADA("cardano", "ADA", "Cardano"),
    HBAR("hedera-hashgraph", "HBAR", "Hedera"),
    ALGO("algorand", "ALGO", "Algorand"),
    IOTA("iota", "IOTA", "IOTA"),
    XDC("xdce-crowd-sale", "XDC", "XDC Network")
}

data class CoinPrice(
    val coin: Coin,
    val priceUsd: Double?,
    val priceCad: Double?,
    val change24h: Double?,
    val sparkline: List<Double> = emptyList()
)

data class MarketChartResponse(
    val prices: List<List<Double>> = emptyList()
)

interface CoinGeckoApi {
    @GET("api/v3/simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") currencies: String = "usd,cad",
        @Query("include_24hr_change") include24hrChange: Boolean = true
    ): Map<String, Map<String, Double>>

    @GET("api/v3/coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: Int = 1,
        @Query("interval") interval: String = "hourly"
    ): MarketChartResponse

    companion object {
        private const val BASE_URL = "https://api.coingecko.com/"

        fun create(): CoinGeckoApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CoinGeckoApi::class.java)
        }
    }
}
