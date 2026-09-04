package com.gamaspace.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.engine.core.OptimizationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para historial de optimizaciones
 */
class OptimizationHistoryViewModel : ViewModel() {

    private val _history = MutableStateFlow(OptimizationHistoryState())
    val history: StateFlow<OptimizationHistoryState> = _history.asStateFlow()

    /**
     * Agrega un resultado a la historia
     */
    fun addResult(result: OptimizationResult) {
        viewModelScope.launch {
            val updated = _history.value.results.toMutableList()
            updated.add(0, result) // Agregar al principio
            if (updated.size > 100) { // Mantener últimos 100
                updated.removeAt(updated.size - 1)
            }
            _history.value = _history.value.copy(
                results = updated,
                totalApplied = if (result.success) _history.value.totalApplied + 1 else _history.value.totalApplied,
                totalFailed = if (!result.success) _history.value.totalFailed + 1 else _history.value.totalFailed
            )
        }
    }

    /**
     * Agrega múltiples resultados
     */
    fun addResults(results: List<OptimizationResult>) {
        viewModelScope.launch {
            results.forEach { addResult(it) }
        }
    }

    /**
     * Limpia el historial
     */
    fun clearHistory() {
        viewModelScope.launch {
            _history.value = OptimizationHistoryState()
        }
    }
}

data class OptimizationHistoryState(
    val results: List<OptimizationResult> = emptyList(),
    val totalApplied: Int = 0,
    val totalFailed: Int = 0
) {
    val successRate: Float
        get() = if (results.isEmpty()) 0f else {
            (totalApplied.toFloat() / (totalApplied + totalFailed)) * 100
        }
}
