package com.gamaspace.app.engine.apps

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor de aplicaciones REAL
 * Solo cierra apps de las que tenemos lista
 * Nunca mata procesos críticos del sistema
 */
class AppManagerEngine(private val context: Context) {

    private val criticalApps = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.phone",
        "com.android.launcher",
        "com.android.launcher3",
        "com.android.inputdevices",
        "com.android.keychain",
        "com.android.providers.media",
        "android.process.media",
        "com.android.se"
    )

    /**
     * Obtiene lista de apps en ejecución que PUEDEN cerrarse
     */
    suspend fun getClosableApps(): List<ClosableApp> = withContext(Dispatchers.IO) {
        return@withContext try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses ?: emptyList()

            runningApps.mapNotNull { process ->
                if (!isCriticalApp(process.processName)) {
                    ClosableApp(
                        packageName = process.processName,
                        pid = process.pid,
                        uid = process.uid,
                        isSystemApp = isSystemApp(process.processName),
                        memoryUsage = getAppMemoryUsage(process.uid)
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Cierra una aplicación específica
     * Retorna true si se logró, false si falló
     */
    suspend fun closeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Verificación de seguridad
            if (isCriticalApp(packageName)) {
                return@withContext false
            }

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cierra múltiples apps seleccionadas
     */
    suspend fun closeMultipleApps(packageNames: List<String>): List<AppCloseResult> = withContext(Dispatchers.IO) {
        return@withContext packageNames.map { packageName ->
            val success = closeApp(packageName)
            AppCloseResult(
                packageName = packageName,
                closed = success,
                reason = if (success) "App closed" else "Failed to close"
            )
        }
    }

    private fun isCriticalApp(packageName: String): Boolean {
        return criticalApps.any { packageName.startsWith(it) }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getAppMemoryUsage(uid: Int): Float {
        return try {
            val runtime = Runtime.getRuntime()
            (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        } catch (e: Exception) {
            0f
        }
    }
}

/**
 * Aplicación que puede cerrarse
 */
data class ClosableApp(
    val packageName: String,
    val pid: Int,
    val uid: Int,
    val isSystemApp: Boolean,
    val memoryUsage: Float
)

/**
 * Resultado de cierre de app
 */
data class AppCloseResult(
    val packageName: String,
    val closed: Boolean,
    val reason: String
)
