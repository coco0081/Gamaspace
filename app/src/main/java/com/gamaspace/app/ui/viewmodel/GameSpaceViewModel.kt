package com.gamaspace.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.engine.boost.BoostEngine
import com.gamaspace.app.engine.core.DeviceCapabilities
import com.gamaspace.app.engine.monitor.MonitorEngine
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
 * ViewModel principal de Game Space
 * Coordina todos los engines y actualiza la UI
 */
@HiltViewModel
class GameSpaceViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val monitorEngine = MonitorEngine(context)
    private val wifiMonitorEngine = WifiMonitorEngine(context)
    private val boostEngine = BoostEngine(context, com.gamaspace.app.engine.shizuku.ShizukuEngine(context))

    // Estado público
    private val _uiState = MutableStateFlow(GameSpaceUiState())
    val uiState: StateFlow<GameSpaceUiState> = _uiState.asStateFlow()

    init {
        initializeEngines()
        startMonitoring()
    }

    private fun initializeEngines() {
        viewModelScope.launch {
            boostEngine.initialize()
            val capabilities = DeviceCapabilities.detect(context)
            _uiState.value = _uiState.value.copy(
                deviceCapabilities = capabilities
            )
        }
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    val cpu = monitorEngine.getCpuUsage()
                    val ram = monitorEngine.getRamUsage()
                    val temp = monitorEngine.getTemperature()
                    val battery = monitorEngine.getBatteryStatus()
                    val ping = wifiMonitorEngine.measurePing().latencyMs

                    _uiState.value = _uiState.value.copy(
                        cpuUsage = cpu,
                        ramUsage = ram,
                        temperature = temp,
                        battery = battery,
                        wifiPing = ping
                    )

                    kotlinx.coroutines.delay(1000) // Actualizar cada segundo
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    
    }

    fun applyProfile(profileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true)
            val results = boostEngine.applyProfile(profileName)
            _uiState.value = _uiState.value.copy(
                isOptimizing = false,
                lastOptimizationResults = results,
                currentProfile = profileName
            )
        }
    }

    fun optimizeWifi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true)
            val wifiOptimizer = WifiOptimizerEngine(wifiMonitorEngine)
            val results = wifiOptimizer.optimizeForLowLatency()
            _uiState.value = _uiState.value.copy(
                isOptimizing = false,
                lastOptimizationResults = results
            )
        }
    }
}

data class GameSpaceUiState(
    val cpuUsage: Float = 0f,
    val ramUsage: com.gamaspace.app.engine.monitor.MemoryUsage = com.gamaspace.app.engine.monitor.MemoryUsage(),
    val temperature: Float = 0f,
    val battery: com.gamaspace.app.engine.monitor.BatteryStatus = com.gamaspace.app.engine.monitor.BatteryStatus(),
    val wifiPing: Int = 0,
    val isOptimizing: Boolean = false,
    val currentProfile: String = "MAX_PERFORMANCE",
    val lastOptimizationResults: List<com.gamaspace.app.engine.core.OptimizationResult> = emptyList(),
    val deviceCapabilities: DeviceCapabilities = DeviceCapabilities()
)
