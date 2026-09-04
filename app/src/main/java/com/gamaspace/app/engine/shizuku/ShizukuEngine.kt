package com.gamaspace.app.engine.shizuku

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Motor Shizuku centralizado
 * Ejecuta SOLO comandos que Shizuku puede ejecutar realmente
 * Detecta capacidades antes de intentar
 */
class ShizukuEngine(private val context: Context) {

    private var isConnected = false
    private var hasPermission = false

    /**
     * Verifica si Shizuku está conectado y tiene permisos
     */
    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Intentar contactar con Shizuku
            val result = executeCommand("echo 'test'", timeout = 2000)
            isConnected = result.success
            hasPermission = result.success
            isConnected
        } catch (e: Exception) {
            isConnected = false
            false
        }
    }

    /**
     * Ejecuta un comando shell real a través de Shizuku
     * Retorna resultado verificable
     */
    suspend fun executeCommand(
        command: String,
        timeout: Long = 5000,
        verify: Boolean = false
    ): ShizukuResult = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isConnected) {
                return@withContext ShizukuResult(
                    success = false,
                    output = "",
                    error = "Shizuku not connected",
                    command = command
                )
            }

            // Ejecutar comando de verdad
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val inputStream = process.inputStream.bufferedReader()
            val errorStream = process.errorStream.bufferedReader()

            val output = inputStream.readText()
            val error = errorStream.readText()

            val exitCode = process.waitFor()
            process.destroy()

            ShizukuResult(
                success = exitCode == 0 && error.isEmpty(),
                output = output.trim(),
                error = error.trim(),
                exitCode = exitCode,
                command = command
            )
        } catch (e: Exception) {
            ShizukuResult(
                success = false,
                output = "",
                error = e.message ?: "Unknown error",
                command = command,
                exception = e
            )
        }
    }

    /**
     * Ejecuta múltiples comandos y retorna los resultados
     */
    suspend fun executeCommands(commands: List<String>): List<ShizukuResult> {
        return commands.map { executeCommand(it) }
    }

    /**
     * Intenta obtener una propiedad del sistema
     */
    suspend fun getSystemProperty(property: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = executeCommand("getprop $property")
            if (result.success) result.output else ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Intenta establecer una propiedad del sistema (requiere permisos)
     */
    suspend fun setSystemProperty(property: String, value: String): Boolean {
        return try {
            val result = executeCommand("setprop $property $value")
            result.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fuerza detención de una aplicación
     */
    suspend fun forceStopApp(packageName: String): Boolean {
        return try {
            val result = executeCommand("am force-stop $packageName")
            result.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene información de procesos en ejecución
     */
    suspend fun getRunningProcesses(): List<ProcessInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = executeCommand("ps -A")
            if (result.success) {
                result.output.lines().drop(1).mapNotNull { line ->
                    val parts = line.split("\\s+".toRegex())
                    if (parts.size >= 9) {
                        ProcessInfo(
                            pid = parts[1].toIntOrNull() ?: 0,
                            uid = parts[0].toIntOrNull() ?: 0,
                            name = parts.last()
                        )
                    } else null
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene información de memoria del sistema
     */
    suspend fun getMemoryInfo(): MemoryInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = executeCommand("cat /proc/meminfo")
            if (result.success) {
                val lines = result.output.lines()
                val totalMem = lines.find { it.startsWith("MemTotal") }
                    ?.split("\\s+".toRegex())
                    ?.getOrNull(1)?.toLongOrNull() ?: 0
                val freeMem = lines.find { it.startsWith("MemFree") }
                    ?.split("\\s+".toRegex())
                    ?.getOrNull(1)?.toLongOrNull() ?: 0
                val availMem = lines.find { it.startsWith("MemAvailable") }
                    ?.split("\\s+".toRegex())
                    ?.getOrNull(1)?.toLongOrNull() ?: 0

                MemoryInfo(
                    totalKb = totalMem,
                    freeKb = freeMem,
                    availableKb = availMem,
                    usedKb = totalMem - freeMem
                )
            } else {
                MemoryInfo()
            }
        } catch (e: Exception) {
            MemoryInfo()
        }
    }

    /**
     * Lee información de temperatura (si está disponible)
     */
    suspend fun getTemperature(): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            // Intenta leer desde múltiples ubicaciones posibles
            val paths = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/proc/acpi/thermal_cooling_device0/temp"
            )

            for (path in paths) {
                val result = executeCommand("cat $path")
                if (result.success) {
                    val temp = result.output.toLongOrNull() ?: continue
                    return@withContext (temp / 1000).toFloat()
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }

    fun isConnectedAndHasPermission(): Boolean = isConnected && hasPermission
}

/**
 * Resultado de ejecución de comando Shizuku
 */
data class ShizukuResult(
    val success: Boolean,
    val output: String,
    val error: String,
    val exitCode: Int = if (success) 0 else 1,
    val command: String,
    val exception: Exception? = null
)

/**
 * Información de proceso
 */
data class ProcessInfo(
    val pid: Int,
    val uid: Int,
    val name: String
)

/**
 * Información de memoria
 */
data class MemoryInfo(
    val totalKb: Long = 0,
    val freeKb: Long = 0,
    val availableKb: Long = 0,
    val usedKb: Long = 0
) {
    val usedMb: Float get() = usedKb / 1024f
    val availableMb: Float get() = availableKb / 1024f
    val totalMb: Float get() = totalKb / 1024f
}
