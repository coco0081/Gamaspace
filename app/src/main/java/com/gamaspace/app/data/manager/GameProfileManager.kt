package com.gamaspace.app.data.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "game_profiles")

/**
 * Gestor de perfiles de juego
 * Guarda y carga configuración personalizada por juego
 */
class GameProfileManager(private val context: Context) {

    /**
     * Obtiene el perfil de un juego
     */
    fun getGameProfile(packageName: String): Flow<GameProfile> {
        val key = stringPreferencesKey(packageName)
        return context.dataStore.data.map { preferences ->
            val json = preferences[key]
            if (json != null) {
                Json.decodeFromString(json)
            } else {
                GameProfile.default(packageName)
            }
        }
    }

    /**
     * Guarda el perfil de un juego
     */
    suspend fun saveGameProfile(profile: GameProfile) = withContext(Dispatchers.IO) {
        val key = stringPreferencesKey(profile.packageName)
        context.dataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(profile)
        }
    }

    /**
     * Obtiene todos los perfiles guardados
     */
    fun getAllProfiles(): Flow<List<GameProfile>> {
        return context.dataStore.data.map { preferences ->
            preferences.asMap().values.mapNotNull { value ->
                try {
                    Json.decodeFromString(value as String)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}

/**
 * Perfil de configuración para un juego
 */
@kotlinx.serialization.Serializable
data class GameProfile(
    val packageName: String,
    val gameName: String,
    val performanceMode: String = "MAX_PERFORMANCE",
    val wifiMode: String = "LOW_LATENCY",
    val enableOverlay: Boolean = true,
    val visibleStats: List<String> = listOf("cpu", "ram", "temp", "ping", "fps"),
    val brightness: Int = 100,
    val maxRefreshRate: Boolean = true,
    val enableDnd: Boolean = true,
    val closeBackgroundApps: Boolean = true
) {
    companion object {
        fun default(packageName: String) = GameProfile(
            packageName = packageName,
            gameName = packageName.split(".").last()
        )
    }
}
