package com.example.quantpricetracker.presentation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "isowatch_prefs")

/**
 * Persists user preferences that must survive app restarts:
 *  - Selected display currency (USD / CAD)
 *  - Set of pinned coin symbols (floated to the top of lists)
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val CURRENCY = stringPreferencesKey("currency")
        val FAVORITES = stringSetPreferencesKey("favorites")
    }

    val currencyFlow: Flow<Currency> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            when (prefs[Keys.CURRENCY]) {
                Currency.CAD.name -> Currency.CAD
                else -> Currency.USD
            }
        }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[Keys.FAVORITES] ?: emptySet() }

    suspend fun setCurrency(currency: Currency) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency.name }
    }

    suspend fun toggleFavorite(coinSymbol: String) {
        context.dataStore.edit { prefs: androidx.datastore.preferences.core.MutablePreferences ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            prefs[Keys.FAVORITES] = if (coinSymbol in current) {
                current - coinSymbol
            } else {
                current + coinSymbol
            }
        }
    }
}
