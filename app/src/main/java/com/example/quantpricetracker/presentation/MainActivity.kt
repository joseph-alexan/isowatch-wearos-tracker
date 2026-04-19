package com.example.quantpricetracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.quantpricetracker.presentation.theme.IsowatchTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var ambientObserver: AmbientLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IsowatchTheme {
                val viewModel: CryptoViewModel = viewModel()

                // Ambient mode scaffold. AmbientLifecycleObserver wires into the Activity
                // lifecycle and receives enter/exit/update callbacks when the watch dims.
                DisposableEffect(Unit) {
                    ambientObserver = AmbientLifecycleObserver(
                        this@MainActivity,
                        object : AmbientLifecycleObserver.AmbientLifecycleCallback {
                            override fun onEnterAmbient(
                                ambientDetails: AmbientLifecycleObserver.AmbientDetails
                            ) {
                                viewModel.setAmbient(true)
                            }

                            override fun onExitAmbient() {
                                viewModel.setAmbient(false)
                            }

                            override fun onUpdateAmbient() {
                                // Fires ~once per minute while ambient. Opportunity to shift
                                // pixels for burn-in safety; we rely on the OS's own shift
                                // plus minimal filled surfaces, so no-op here.
                            }
                        }
                    )
                    lifecycle.addObserver(ambientObserver)
                    onDispose { lifecycle.removeObserver(ambientObserver) }
                }

                // Lifecycle-aware polling: starts when STARTED, stops when STOPPED.
                // repeatOnLifecycle restarts the block on each STARTED transition,
                // which is exactly what we want (app foregrounded = polling active).
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            viewModel.startPolling()
                            try {
                                awaitCancellation()
                            } finally {
                                viewModel.stopPolling()
                            }
                        }
                    }
                }

                IsowatchApp(viewModel)
            }
        }
    }
}

@Composable
fun IsowatchApp(viewModel: CryptoViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.PAGER
    ) {
        composable(Routes.PAGER) {
            CoinPagerScreen(
                uiState = uiState,
                onToggleCurrency = { viewModel.toggleCurrency() },
                onToggleFavorite = { symbol -> viewModel.toggleFavorite(symbol) },
                onPageChanged = { viewModel.onPageChanged() },
                onOpenOverview = { navController.navigate(Routes.OVERVIEW) }
            )
        }
        composable(Routes.OVERVIEW) {
            OverviewListScreen(
                uiState = uiState,
                onCoinTap = { _ ->
                    // Tapping a row returns to the pager. The pager starts at its last-
                    // remembered page; a future version could deep-link to the tapped coin
                    // by passing symbol through a shared state holder.
                    navController.popBackStack()
                },
                onCoinLongPress = { symbol -> viewModel.toggleFavorite(symbol) },
                onToggleCurrency = { viewModel.toggleCurrency() }
            )
        }
    }
}

private object Routes {
    const val PAGER = "pager"
    const val OVERVIEW = "overview"
}
