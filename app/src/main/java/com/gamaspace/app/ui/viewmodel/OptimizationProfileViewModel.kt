package com.gamaspace.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.data.model.OptimizationProfile
import com.gamaspace.app.repository.GamaspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI de Perfiles de Optimización
 */
data class OptimizationProfileState(
    val profiles: List<OptimizationProfile> = emptyList(),
    val activeProfile: OptimizationProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel para gestionar Perfiles de Optimización
 */
@HiltViewModel
class OptimizationProfileViewModel @Inject constructor(
    private val repository: GamaspaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OptimizationProfileState())
    val state: StateFlow<OptimizationProfileState> = _state.asStateFlow()

    init {
        loadProfiles()
        initializeDefaultProfiles()
    }

    /**
     * Carga todos los perfiles disponibles
     */
    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                repository.getAllProfiles().collect { profiles ->
                    _state.value = _state.value.copy(
                        profiles = profiles,
                        activeProfile = profiles.find { it.isActive },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Error al cargar perfiles"
                )
            }
        }
    }

    /**
     * Inicializa los perfiles por defecto si no existen
     */
    private fun initializeDefaultProfiles() {
        viewModelScope.launch {
            try {
                val defaultProfile = repository.getProfileByName("default")
                if (defaultProfile == null) {
                    // Crear perfiles por defecto
                    val defaultProfiles = listOf(
                        OptimizationProfile(
                            profileName = "default",
                            description = "Perfil equilibrado",
                            ramOptimization = true,
                            cpuOptimization = true,
                            gpuOptimization = false,
                            batteryOptimization = false,
                            screenBrightness = 50,
                            screenRefreshRate = 60,
                            backgroundLimit = false,
                            animationScale = 1.0f,
                            isActive = true
                        ),
                        OptimizationProfile(
                            profileName = "gaming",
                            description = "Perfil optimizado para juegos",
                            ramOptimization = true,
                            cpuOptimization = true,
                            gpuOptimization = true,
                            batteryOptimization = false,
                            screenBrightness = 100,
                            screenRefreshRate = 120,
                            backgroundLimit = true,
                            animationScale = 1.5f,
                            isActive = false
                        ),
                        OptimizationProfile(
                            profileName = "battery_saving",
                            description = "Ahorro máximo de batería",
                            ramOptimization = true,
                            cpuOptimization = true,
                            gpuOptimization = false,
                            batteryOptimization = true,
                            screenBrightness = 30,
                            screenRefreshRate = 30,
                            backgroundLimit = true,
                            animationScale = 0.5f,
                            isActive = false
                        )
                    )

                    defaultProfiles.forEach { profile ->
                        repository.insertProfile(profile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Activa un perfil de optimización
     */
    fun activateProfile(profileName: String) {
        viewModelScope.launch {
            try {
                repository.setActiveProfile(profileName)
                loadProfiles()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al activar perfil"
                )
            }
        }
    }

    /**
     * Crea un perfil personalizado
     */
    fun createCustomProfile(profile: OptimizationProfile) {
        viewModelScope.launch {
            try {
                repository.insertProfile(profile)
                loadProfiles()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al crear perfil"
                )
            }
        }
    }

    /**
     * Actualiza un perfil existente
     */
    fun updateProfile(profile: OptimizationProfile) {
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
                loadProfiles()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al actualizar perfil"
                )
            }
        }
    }

    /**
     * Obtiene un perfil por nombre
     */
    fun getProfileByName(name: String) {
        viewModelScope.launch {
            try {
                val profile = repository.getProfileByName(name)
                _state.value = _state.value.copy(
                    activeProfile = profile
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al obtener perfil"
                )
            }
        }
    }
}
