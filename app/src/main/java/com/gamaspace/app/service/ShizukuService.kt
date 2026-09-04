package com.gamaspace.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.rikka.shizuku.Shizuku
import dev.rikka.shizuku.ShizukuProvider

/**
 * Servicio para integración con Shizuku
 * Permite acceso a permisos elevados sin necesidad de root
 */
class ShizukuService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "ShizukuService"
        
        /**
         * Verifica si Shizuku está disponible y conectado
         */
        fun isShizukuAvailable(): Boolean {
            return try {
                Shizuku.checkSelfPermission() == 0 || 
                Shizuku.getState() == Shizuku.STATE_AUTHORIZED
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Solicita permiso a Shizuku
         */
        fun requestShizukuPermission() {
            try {
                if (Shizuku.isPreV11()) {
                    Shizuku.requestPermission(0)
                } else {
                    // Para Shizuku v11+
                    Shizuku.requestPermission(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeShizuku()
    }

    private fun initializeShizuku() {
        scope.launch {
            try {
                if (!isShizukuAvailable()) {
                    requestShizukuPermission()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Mata un proceso usando Shizuku
     */
    fun killProcess(packageName: String): Boolean {
        return try {
            if (!isShizukuAvailable()) {
                return false
            }

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("sh", "-c", "am force-stop $packageName"))
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Limpia caché de una aplicación
     */
    fun clearAppCache(packageName: String): Boolean {
        return try {
            if (!isShizukuAvailable()) {
                return false
            }

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("sh", "-c", "pm clear --cache $packageName"))
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene estadísticas de batería del sistema
     */
    fun getBatteryStats(): String {
        return try {
            if (!isShizukuAvailable()) {
                return ""
            }

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(arrayOf("sh", "-c", "dumpsys battery"))
            val reader = process.inputStream.bufferedReader()
            reader.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Ajusta velocidad de CPU (requiere permisos elevados)
     */
    fun setCpuGovernor(governor: String): Boolean {
        return try {
            if (!isShizukuAvailable()) {
                return false
            }

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(
                arrayOf(
                    "sh", "-c",
                    "echo $governor | tee /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
                )
            )
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Establece velocidad GPU
     */
    fun setGpuFrequency(frequency: String): Boolean {
        return try {
            if (!isShizukuAvailable()) {
                return false
            }

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(
                arrayOf(
                    "sh", "-c",
                    "echo $frequency | tee /sys/class/kgsl/kgsl-3d0/devfreq/max_freq"
                )
            )
            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
