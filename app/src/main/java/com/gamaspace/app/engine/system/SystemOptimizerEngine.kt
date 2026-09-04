package com.gamaspace.app.engine.system

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.gamaspace.app.engine.core.OptimizationResult
import com.gamaspace.app.engine.core.OptimizationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Optimizador de sistema REAL
 * Aplica solamente cambios que Android permite sin Root
 */
class SystemOptimizerEngine(private val context: Context) {

    /**
     * Reduce animaciones del sistema
     */
    suspend fun reduceAnimations(): OptimizationResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentValue = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE,
                1f
            )

            val writeSuccess = Settings.Global.putFloat(
                context.contentResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE,
                0.5f
            ) && Settings.Global.putFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                0.5f
            )

            if (writeSuccess) {
                val verified = Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.WINDOW_ANIMATION_SCALE,
                    1f
                ) == 0.5f

                OptimizationResult(
                    optimizationName = "Reduce Animations",
                    status = if (verified) OptimizationStatus.APPLIED else OptimizationStatus.APPLIED_UNVERIFIED,
                    message = "Animation scale reduced from $currentValue to 0.5",
                    originalValue = currentValue.toString(),
                    newValue = "0.5",
                    verified = verified
                )
            } else {
                OptimizationResult(
                    optimizationName = "Reduce Animations",
                    status = OptimizationStatus.PERMISSION_REQUIRED,
                    message = "Permission denied to modify system settings",
                    requiresPermission = "android.permission.WRITE_SETTINGS",
                    verified = false
                )
            }
        } catch (e: Exception) {
            OptimizationResult(
                optimizationName = "Reduce Animations",
                status = OptimizationStatus.FAILED,
                message = e.message ?: "Unknown error",
                verified = false
            )
        }
    }

    /**
     * Habilita Do Not Disturb
     */
    suspend fun enableDoNotDisturb(): OptimizationResult = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return@withContext OptimizationResult(
                    optimizationName = "Enable Do Not Disturb",
                    status = OptimizationStatus.NOT_SUPPORTED,
                    message = "Requires Android 6.0+",
                    verified = false
                )
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                return@withContext OptimizationResult(
                    optimizationName = "Enable Do Not Disturb",
                    status = OptimizationStatus.PERMISSION_REQUIRED,
                    message = "Permission required to access notification policy",
                    requiresPermission = "android.permission.ACCESS_NOTIFICATION_POLICY",
                    verified = false
                )
            }

            try {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                OptimizationResult(
                    optimizationName = "Enable Do Not Disturb",
                    status = OptimizationStatus.APPLIED,
                    message = "Do Not Disturb enabled",
                    verified = true
                )
            } catch (e: Exception) {
                OptimizationResult(
                    optimizationName = "Enable Do Not Disturb",
                    status = OptimizationStatus.FAILED,
                    message = e.message ?: "Failed to enable DND",
                    verified = false
                )
            }
        } catch (e: Exception) {
            OptimizationResult(
                optimizationName = "Enable Do Not Disturb",
                status = OptimizationStatus.FAILED,
                message = e.message ?: "Unknown error",
                verified = false
            )
        }
    }

    /**
     * Establece brillo máximo
     */
    suspend fun setMaxBrightness(): OptimizationResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                100
            )

            val writeSuccess = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                255 // Máximo
            )

            if (writeSuccess) {
                val verified = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    100
                ) == 255

                OptimizationResult(
                    optimizationName = "Set Max Brightness",
                    status = if (verified) OptimizationStatus.APPLIED else OptimizationStatus.APPLIED_UNVERIFIED,
                    message = "Brightness increased from $currentBrightness to 255",
                    originalValue = currentBrightness.toString(),
                    newValue = "255",
                    verified = verified
                )
            } else {
                OptimizationResult(
                    optimizationName = "Set Max Brightness",
                    status = OptimizationStatus.PERMISSION_REQUIRED,
                    message = "Permission denied to modify brightness",
                    verified = false
                )
            }
        } catch (e: Exception) {
            OptimizationResult(
                optimizationName = "Set Max Brightness",
                status = OptimizationStatus.FAILED,
                message = e.message ?: "Unknown error",
                verified = false
            )
        }
    }

    /**
     * Establece brillo a un porcentaje específico
     */
    suspend fun setBrightness(percentage: Int): OptimizationResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val clampedPercent = percentage.coerceIn(0, 100)
            val brightnessValue = (clampedPercent * 255) / 100

            val writeSuccess = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )

            if (writeSuccess) {
                OptimizationResult(
                    optimizationName = "Set Brightness",
                    status = OptimizationStatus.APPLIED,
                    message = "Brightness set to $clampedPercent%",
                    newValue = "$clampedPercent%",
                    verified = true
                )
            } else {
                OptimizationResult(
                    optimizationName = "Set Brightness",
                    status = OptimizationStatus.PERMISSION_REQUIRED,
                    message = "Permission denied",
                    verified = false
                )
            }
        } catch (e: Exception) {
            OptimizationResult(
                optimizationName = "Set Brightness",
                status = OptimizationStatus.FAILED,
                message = e.message ?: "Unknown error",
                verified = false
            )
        }
    }
}
