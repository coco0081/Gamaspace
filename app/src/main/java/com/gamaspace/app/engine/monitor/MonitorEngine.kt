package com.gamaspace.app.engine.monitor

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Motor de monitoreo REAL de rendimiento
 * Lee datos directamente del sistema - SIN simulación
 */
class MonitorEngine(private val context: Context) {

    /**
     * Obtiene uso de CPU en porcentaje
     * Lee directamente de /proc/stat
     */
    suspend fun getCpuUsage(): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = BufferedReader(InputStreamReader(File("/proc/stat").inputStream()))
            val line = reader.readLine()
            reader.close()

            val tokens = line.split("\\s+".toRegex())
            if (tokens.size < 5) return@withContext 0f

            val user = tokens[1].toLong()
            val nice = tokens[2].toLong()
            val system = tokens[3].toLong()
            val idle = tokens[4].toLong()

            val totalTime = user + nice + system + idle
            val usage = ((totalTime - idle).toFloat() / totalTime) * 100
            usage.coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Obtiene uso de RAM en MB
     * Lee directamente de /proc/meminfo
     */
    suspend fun getRamUsage(): MemoryUsage = withContext(Dispatchers.IO) {
        return@withContext try {
            val reader = BufferedReader(InputStreamReader(File("/proc/meminfo").inputStream()))
            val lines = reader.readLines()
            reader.close()

            var totalKb = 0L
            var freeKb = 0L
            var availableKb = 0L

            for (line in lines) {
                when {
                    line.startsWith("MemTotal:") -> totalKb = line.split("\\s+".toRegex())[1].toLongOrNull() ?: 0
                    line.startsWith("MemFree:") -> freeKb = line.split("\\s+".toRegex())[1].toLongOrNull() ?: 0
                    line.startsWith("MemAvailable:") -> availableKb = line.split("\\s+".toRegex())[1].toLongOrNull() ?: 0
                }
            }

            val usedKb = totalKb - freeKb
            val usagePercent = if (totalKb > 0) (usedKb.toFloat() / totalKb) * 100 else 0f

            MemoryUsage(
                totalMb = totalKb / 1024f,
                usedMb = usedKb / 1024f,
                freeMb = freeKb / 1024f,
                availableMb = availableKb / 1024f,
                usagePercent = usagePercent.coerceIn(0f, 100f)
            )
        } catch (e: Exception) {
            MemoryUsage()
        }
    }

    /**
     * Obtiene temperatura del dispositivo
     * Lee desde múltiples ubicaciones posibles
     */
    suspend fun getTemperature(): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            val thermalPaths = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp",
                "/sys/class/hwmon/hwmon0/temp1_input",
                "/data/thermal/thermal-engine/zone0"
            )

            for (path in thermalPaths) {
                try {
                    val file = File(path)
                    if (file.exists()) {
                        val temp = file.readText().trim().toLongOrNull() ?: continue
                        // Convertir de milígrados a grados Celsius
                        return@withContext if (temp > 1000) {
                            (temp / 1000).toFloat()
                        } else {
                            temp.toFloat()
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Obtiene nivel de batería y estado
     */
    suspend fun getBatteryStatus(): BatteryStatus = withContext(Dispatchers.IO) {
        return@withContext try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            val temperature = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE)
            val voltage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE)
            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            val health = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_HEALTH)
            val plugged = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_PLUGGED)

            BatteryStatus(
                level = level.coerceIn(0, 100),
                temperature = temperature / 10f,
                voltage = voltage,
                status = status,
                health = health,
                isCharging = plugged > 0
            )
        } catch (e: Exception) {
            BatteryStatus()
        }
    }

    /**
     * Obtiene información de procesos en ejecución
     */
    suspend fun getRunningApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses ?: emptyList()

            runningApps.map { process ->
                AppInfo(
                    name = process.processName,
                    pid = process.pid,
                    uid = process.uid,
                    importance = process.importance,
                    importanceReasonCode = process.importanceReasonCode
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene información de memoria nativa (GPU aproximada)
     */
    suspend fun getNativeMemory(): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            val runtime = Runtime.getRuntime()
            val nativeHeap = Debug.getNativeHeap().sumOf { it.size_in_bytes.toLong() }
            (nativeHeap / (1024f * 1024f)) // Convertir a MB
        } catch (e: Exception) {
            0f
        }
    }
}

/**
 * Información de uso de memoria
 */
data class MemoryUsage(
    val totalMb: Float = 0f,
    val usedMb: Float = 0f,
    val freeMb: Float = 0f,
    val availableMb: Float = 0f,
    val usagePercent: Float = 0f
)

/**
 * Información de batería
 */
data class BatteryStatus(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val status: Int = 0,
    val health: Int = 0,
    val isCharging: Boolean = false
)

/**
 * Información de aplicación en ejecución
 */
data class AppInfo(
    val name: String,
    val pid: Int,
    val uid: Int,
    val importance: Int,
    val importanceReasonCode: Int
)
