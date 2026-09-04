package com.gamaspace.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.data.model.CacheData
import com.gamaspace.app.repository.GamaspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI de Limpieza de Memoria
 */
data class MemoryCleanupState(
    val cacheDataList: List<CacheData> = emptyList(),
    val totalCacheSize: Long = 0L,
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val cleanedSize: Long = 0L,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestionar la Limpieza de Memoria
 */
@HiltViewModel
class MemoryCleanupViewModel @Inject constructor(
    private val repository: GamaspaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryCleanupState())
    val state: StateFlow<MemoryCleanupState> = _state.asStateFlow()

    init {
        scanCacheData()
    }

    /**
     * Escanea datos de caché disponibles
     */
    private fun scanCacheData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isScanning = true)
                
                repository.getAllCacheData().collect { cacheList ->
                    val totalSize = cacheList.sumOf { it.cacheSize }
                    _state.value = _state.value.copy(
                        cacheDataList = cacheList,
                        totalCacheSize = totalSize,
                        isScanning = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Error al escanear caché",
                    isScanning = false
                )
            }
        }
    }

    /**
     * Limpia el caché seleccionable
     */
    fun cleanSelectableCache() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isCleaning = true)
                
                val cleanableCache = repository.getCleanableCacheData()
                val totalToClean = cleanableCache.sumOf { it.cacheSize }
                
                // Simular limpieza
                cleanableCache.forEach { cache ->
                    // Aquí iría la lógica de limpieza real
                    repository.deleteCacheData(cache.packageName)
                }

                _state.value = _state.value.copy(
                    cleanedSize = totalToClean,
                    isCleaning = false,
                    successMessage = "Se liberaron ${formatBytes(totalToClean)} de memoria",
                    totalCacheSize = 0L,
                    cacheDataList = emptyList()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al limpiar caché",
                    isCleaning = false
                )
            }
        }
    }

    /**
     * Limpia el caché de una aplicación específica
     */
    fun cleanAppCache(packageName: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isCleaning = true)
                
                val cacheData = _state.value.cacheDataList.find { it.packageName == packageName }
                if (cacheData != null) {
                    val cleanedSize = cacheData.cacheSize
                    repository.deleteCacheData(packageName)
                    
                    _state.value = _state.value.copy(
                        cleanedSize = cleanedSize,
                        successMessage = "Se liberaron ${formatBytes(cleanedSize)} de la app",
                        isCleaning = false
                    )
                    
                    scanCacheData()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al limpiar caché de la app",
                    isCleaning = false
                )
            }
        }
    }

    /**
     * Limpia archivos temporales del sistema
     */
    fun cleanSystemTemp() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isCleaning = true)
                
                val tempCache = _state.value.cacheDataList.filter { it.tempFileSize > 0 }
                val totalTemp = tempCache.sumOf { it.tempFileSize }
                
                tempCache.forEach { cache ->
                    repository.deleteCacheData(cache.packageName)
                }

                _state.value = _state.value.copy(
                    cleanedSize = totalTemp,
                    isCleaning = false,
                    successMessage = "Se limpiaron archivos temporales (${formatBytes(totalTemp)})"
                )
                
                scanCacheData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al limpiar archivos temporales",
                    isCleaning = false
                )
            }
        }
    }

    /**
     * Refresca el escaneo de caché
     */
    fun refreshScan() {
        _state.value = _state.value.copy(
            successMessage = null,
            error = null,
            cleanedSize = 0L
        )
        scanCacheData()
    }

    /**
     * Formatea bytes a formato legible
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1073741824 -> String.format("%.2f GB", bytes / 1073741824.0)
            bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576.0)
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
