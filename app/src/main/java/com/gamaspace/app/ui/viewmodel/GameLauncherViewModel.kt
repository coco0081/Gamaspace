package com.gamaspace.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamaspace.app.data.model.AppModel
import com.gamaspace.app.data.model.GameHistory
import com.gamaspace.app.repository.GamaspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI del Launcher de Juegos
 */
data class GameLauncherState(
    val allApps: List<AppModel> = emptyList(),
    val games: List<AppModel> = emptyList(),
    val gameHistory: List<GameHistory> = emptyList(),
    val isLoading: Boolean = true,
    val selectedGame: AppModel? = null,
    val error: String? = null
)

/**
 * ViewModel para el Launcher de Juegos
 */
@HiltViewModel
class GameLauncherViewModel @Inject constructor(
    private val repository: GamaspaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameLauncherState())
    val state: StateFlow<GameLauncherState> = _state.asStateFlow()

    init {
        loadGames()
        loadGameHistory()
    }

    /**
     * Carga la lista de juegos
     */
    private fun loadGames() {
        viewModelScope.launch {
            try {
                repository.getAllGames().collect { games ->
                    _state.value = _state.value.copy(
                        games = games,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Error al cargar juegos",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Carga todas las aplicaciones
     */
    fun loadAllApps() {
        viewModelScope.launch {
            try {
                repository.getAllApps().collect { apps ->
                    _state.value = _state.value.copy(
                        allApps = apps
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Error al cargar aplicaciones"
                )
            }
        }
    }

    /**
     * Carga el historial de juegos
     */
    private fun loadGameHistory() {
        viewModelScope.launch {
            try {
                repository.getAllGameSessions().collect { history ->
                    _state.value = _state.value.copy(
                        gameHistory = history
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Error al cargar historial"
                )
            }
        }
    }

    /**
     * Selecciona un juego
     */
    fun selectGame(game: AppModel) {
        _state.value = _state.value.copy(
            selectedGame = game
        )
    }

    /**
     * Lanza un juego con optimización
     */
    fun launchGame(packageName: String, optimize: Boolean = true) {
        viewModelScope.launch {
            try {
                val app = _state.value.games.find { it.packageName == packageName }
                if (app != null && optimize) {
                    // Aquí iría la lógica de optimización
                    applyGamingProfile(packageName)
                }

                // Crear entrada de historial
                val session = GameHistory(
                    packageName = packageName,
                    gameName = app?.appName ?: "Juego desconocido",
                    playDate = System.currentTimeMillis()
                )
                repository.insertGameSession(session)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al lanzar juego"
                )
            }
        }
    }

    /**
     * Aplica el perfil de gaming
     */
    private suspend fun applyGamingProfile(packageName: String) {
        try {
            val gamingProfile = repository.getProfileByName("gaming")
            if (gamingProfile != null) {
                repository.setActiveProfile("gaming")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Marca una app como juego
     */
    fun markAsGame(packageName: String, isGame: Boolean) {
        viewModelScope.launch {
            try {
                val app = repository.getAppByPackage(packageName)
                if (app != null) {
                    repository.updateApp(app.copy(isGame = isGame))
                    loadGames()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al marcar app como juego"
                )
            }
        }
    }

    /**
     * Obtiene el historial de un juego específico
     */
    fun getGameSessionHistory(packageName: String) {
        viewModelScope.launch {
            try {
                repository.getGameSessionsByPackage(packageName).collect { history ->
                    _state.value = _state.value.copy(
                        gameHistory = history
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al obtener historial del juego"
                )
            }
        }
    }

    /**
     * Obtiene estadísticas de un juego
     */
    suspend fun getGameStats(packageName: String): Pair<Long, Long> {
        return try {
            val avgDuration = repository.getAveragePlayDuration(packageName)
            val totalDuration = repository.getTotalPlayDuration(packageName)
            Pair(avgDuration, totalDuration)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }

    /**
     * Limpia el historial de juegos antiguos
     */
    fun cleanOldGameHistory() {
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
                repository.deleteOldGameSessions(thirtyDaysAgo)
                loadGameHistory()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al limpiar historial"
                )
            }
        }
    }
}
