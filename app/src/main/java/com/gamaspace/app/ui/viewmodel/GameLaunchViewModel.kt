package com.gamaspace.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.data.manager.GameProfile
import com.gamaspace.app.data.manager.GameProfileManager
import com.gamaspace.app.engine.apps.AppManagerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para lanzamiento de juegos
 * Coordina optimizaciones antes de lanzar
 */
@HiltViewModel
class GameLaunchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val profileManager = GameProfileManager(context)
    private val appManager = AppManagerEngine(context)
    private val packageManager = context.packageManager

    private val _launchState = MutableStateFlow(GameLaunchState())
    val launchState: StateFlow<GameLaunchState> = _launchState.asStateFlow()

    /**
     * Prepara y lanza un juego con optimizaciones
     */
    fun launchGame(packageName: String) {
        viewModelScope.launch {
            try {
                _launchState.value = _launchState.value.copy(isLaunching = true)

                // 1. Cargar perfil del juego
                var profile = GameProfile.default(packageName)
                profileManager.getGameProfile(packageName).collect { savedProfile ->
                    profile = savedProfile
                }

                // 2. Aplicar optimizaciones
                _launchState.value = _launchState.value.copy(
                    currentStep = "Applying optimizations...",
                    progress = 30
                )

                // 3. Cerrar apps de fondo si está habilitado
                if (profile.closeBackgroundApps) {
                    _launchState.value = _launchState.value.copy(
                        currentStep = "Closing background apps...",
                        progress = 50
                    )
                    val closableApps = appManager.getClosableApps()
                    appManager.closeMultipleApps(closableApps.map { it.packageName })
                }

                // 4. Lanzar aplicación
                _launchState.value = _launchState.value.copy(
                    currentStep = "Launching game...",
                    progress = 80
                )

                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    _launchState.value = _launchState.value.copy(
                        isLaunching = false,
                        currentStep = "Game launched",
                        progress = 100,
                        lastLaunchedGame = packageName
                    )
                } else {
                    throw Exception("Cannot launch game")
                }
            } catch (e: Exception) {
                _launchState.value = _launchState.value.copy(
                    isLaunching = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Obtiene lista de juegos instalados
     */
    fun getInstalledGames(): List<String> {
        return try {
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            packages.mapNotNull { app ->
                if ((app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                    app.packageName
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Guarda perfil personalizado para un juego
     */
    fun saveGameProfile(profile: GameProfile) {
        viewModelScope.launch {
            profileManager.saveGameProfile(profile)
        }
    }
}

data class GameLaunchState(
    val isLaunching: Boolean = false,
    val currentStep: String = "",
    val progress: Int = 0,
    val lastLaunchedGame: String = "",
    val error: String? = null
)
