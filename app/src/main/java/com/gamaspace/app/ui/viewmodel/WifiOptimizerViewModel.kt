package com.gamaspace.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.engine.wifi.WifiMonitorEngine
import com.gamaspace.app.engine.wifi.WifiOptimizerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para optimización Wi-Fi
 */
@HiltViewModel
class WifiOptimizerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val wifiMonitor = WifiMonitorEngine(context)
    private val wifiOptimizer = WifiOptimizerEngine(wifiMonitor)

    private val _wifiState = MutableStateFlow(WifiOptimizerState())
    val wifiState: StateFlow<WifiOptimizerState> = _wifiState.asStateFlow()

    init {
        startWifiMonitoring()
    }

    private fun startWifiMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    val diagnostic = wifiOptimizer.performDiagnostic()
                    val channelAnalysis = wifiOptimizer.analyzeChannelCongestion()

                    _wifiState.value = _wifiState.value.copy(
                        diagnostic = diagnostic,
                        channelAnalysis = channelAnalysis,
                        lastUpdateTime = System.currentTimeMillis()
                    )

                    kotlinx.coroutines.delay(2000) // Actualizar cada 2 segundos
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun optimizeForLowLatency() {
        viewModelScope.launch {
            _wifiState.value = _wifiState.value.copy(isOptimizing = true)
            val results = wifiOptimizer.optimizeForLowLatency()
            _wifiState.value = _wifiState.value.copy(
                isOptimizing = false,
                optimizationResults = results
            )
        }
    }
}

data class WifiOptimizerState(
    val diagnostic: com.gamaspace.app.engine.wifi.WifiDiagnostic = com.gamaspace.app.engine.wifi.WifiDiagnostic(),
    val channelAnalysis: com.gamaspace.app.engine.wifi.ChannelAnalysis = com.gamaspace.app.engine.wifi.ChannelAnalysis(),
    val isOptimizing: Boolean = false,
    val optimizationResults: List<com.gamaspace.app.engine.core.OptimizationResult> = emptyList(),
    val lastUpdateTime: Long = 0
)
