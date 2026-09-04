package com.gamaspace.app.engine.safety

import com.gamaspace.app.engine.core.OptimizationResult
import com.gamaspace.app.engine.core.OptimizationStatus

/**
 * Sistema de seguridad para evitar cambios peligrosos
 * Valida ANTES de ejecutar cualquier optimización
 */
class SafetyGuard {

    /**
     * Lista de operaciones NUNCA permitidas
     */
    private val forbiddenOperations = setOf(
        "disable_thermal_protection",
        "modify_selinux",
        "modify_firmware",
        "modify_partitions",
        "modify_recovery",
        "overclock_cpu",
        "overclock_gpu",
        "disable_security",
        "modify_init",
        "modify_bootloader"
    )

    /**
     * Valida si una operación es segura
     */
    fun validateOperation(operationName: String, parameters: Map<String, Any> = emptyMap()): SafetyCheck {
        // Verificar si está en lista negra
        if (forbiddenOperations.any { operationName.lowercase().contains(it) }) {
            return SafetyCheck(
                safe = false,
                reason = "This operation is not allowed for safety reasons",
                operationName = operationName
            )
        }

        // Validar parámetros
        val parameterCheck = validateParameters(operationName, parameters)
        if (!parameterCheck.safe) {
            return parameterCheck
        }

        return SafetyCheck(
            safe = true,
            operationName = operationName
        )
    }

    /**
     * Valida parámetros específicos de operaciones
     */
    private fun validateParameters(operationName: String, parameters: Map<String, Any>): SafetyCheck {
        return when (operationName.lowercase()) {
            "set_brightness" -> {
                val brightness = (parameters["brightness"] as? Int) ?: 0
                if (brightness < 0 || brightness > 100) {
                    SafetyCheck(
                        safe = false,
                        reason = "Brightness must be between 0 and 100",
                        operationName = operationName
                    )
                } else {
                    SafetyCheck(safe = true, operationName = operationName)
                }
            }
            "close_app" -> {
                val packageName = parameters["package"] as? String ?: ""
                val criticalApps = setOf(
                    "android",
                    "com.android.systemui",
                    "com.android.settings"
                )
                if (criticalApps.any { packageName.startsWith(it) }) {
                    SafetyCheck(
                        safe = false,
                        reason = "Cannot close system critical apps",
                        operationName = operationName
                    )
                } else {
                    SafetyCheck(safe = true, operationName = operationName)
                }
            }
            else -> SafetyCheck(safe = true, operationName = operationName)
        }
    }

    /**
     * Retorna un OptimizationResult BLOQUEADO si no es seguro
     */
    fun blockIfUnsafe(operationName: String, parameters: Map<String, Any> = emptyMap()): OptimizationResult? {
        val check = validateOperation(operationName, parameters)
        return if (!check.safe) {
            OptimizationResult(
                optimizationName = operationName,
                status = OptimizationStatus.FAILED,
                message = "BLOCKED - ${check.reason}",
                verified = false
            )
        } else {
            null
        }
    }
}

/**
 * Resultado de validación de seguridad
 */
data class SafetyCheck(
    val safe: Boolean,
    val reason: String = "Operation is safe",
    val operationName: String = ""
)
