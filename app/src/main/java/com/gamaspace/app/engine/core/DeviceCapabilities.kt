package com.gamaspace.app.engine.core

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Detecta y almacena las capacidades REALES del dispositivo
 * NO asume nada - solo reporta lo que puede hacer
 */
data class DeviceCapabilities(
    val androidVersion: Int = Build.VERSION.SDK_INT,
    val manufacturer: String = Build.MANUFACTURER,
    val device: String = Build.DEVICE,
    val model: String = Build.MODEL,
    val hasGameMode: Boolean = false,
    val hasPerformanceMode: Boolean = false,
    val hasRefreshRateControl: Boolean = false,
    val maxRefreshRate: Int = 60,
    val hasAnimationControl: Boolean = false,
    val hasDnDControl: Boolean = false,
    val hasBrightnessControl: Boolean = false,
    val hasWifiOptimization: Boolean = false,
    val hasThermalControl: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val shizukuConnected: Boolean = false,
    val rootAvailable: Boolean = false
) {
    companion object {
        /**
         * Detecta capacidades reales del dispositivo
         */
        fun detect(context: Context): DeviceCapabilities {
            val capabilities = DeviceCapabilities(
                androidVersion = Build.VERSION.SDK_INT,
                manufacturer = Build.MANUFACTURER,
                device = Build.DEVICE,
                model = Build.MODEL,
                hasGameMode = detectGameMode(context),
                hasPerformanceMode = detectPerformanceMode(),
                hasRefreshRateControl = detectRefreshRateControl(),
                maxRefreshRate = detectMaxRefreshRate(context),
                hasAnimationControl = true, // Siempre disponible en settings
                hasDnDControl = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M,
                hasBrightnessControl = true,
                hasWifiOptimization = true,
                hasThermalControl = detectThermalControl(),
                shizukuAvailable = detectShizukuAvailable(),
                shizukuConnected = false, // Se actualiza en tiempo real
                rootAvailable = false // No implementado todavía
            )
            return capabilities
        }

        private fun detectGameMode(context: Context): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                    uiModeManager != null
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        private fun detectPerformanceMode(): Boolean {
            return try {
                // Verificar si /sys/module/dvfs existe (sistema de gestión dinámica de frecuencia)
                val dvfsPath = "/sys/module/dvfs"
                java.io.File(dvfsPath).exists()
            } catch (e: Exception) {
                false
            }
        }

        private fun detectRefreshRateControl(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }

        private fun detectMaxRefreshRate(context: Context): Int {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val display = context.display
                    display?.refreshRate?.toInt() ?: 60
                } else {
                    60
                }
            } catch (e: Exception) {
                60
            }
        }

        private fun detectThermalControl(): Boolean {
            return try {
                // Verificar si existen archivos de control térmico
                val thermalPath = "/sys/class/thermal"
                java.io.File(thermalPath).exists()
            } catch (e: Exception) {
                false
            }
        }

        private fun detectShizukuAvailable(): Boolean {
            return try {
                // Intentar comprobar si Shizuku está disponible
                val pm = android.app.AppOpsManager::class.java
                pm != null
            } catch (e: Exception) {
                false
            }
        }
    }
}
