package com.gamaspace.app.engine.core

import java.io.Serializable

/**
 * Resultado verificable de cualquier optimización
 * NO simula resultados - solo reporta lo que realmente pasó
 */
data class OptimizationResult(
    val optimizationName: String,
    val status: OptimizationStatus,
    val message: String,
    val originalValue: String? = null,
    val newValue: String? = null,
    val verified: Boolean = false,
    val requiresRoot: Boolean = false,
    val requiresShizuku: Boolean = false,
    val requiresPermission: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val errorDetails: String? = null
) : Serializable {
    val success: Boolean
        get() = status == OptimizationStatus.APPLIED && verified

    val canRetry: Boolean
        get() = status == OptimizationStatus.FAILED && !requiresRoot && !requiresShizuku
}

enum class OptimizationStatus {
    APPLIED,           // ✓ Aplicado y verificado
    APPLIED_UNVERIFIED, // ✓ Aplicado pero no se pudo verificar
    FAILED,            // ✕ Falló - ver message
    NOT_SUPPORTED,     // ✕ No compatible con este dispositivo
    ROOT_REQUIRED,     // ✕ Requiere Root
    SHIZUKU_REQUIRED,  // ✕ Requiere Shizuku
    PERMISSION_REQUIRED, // ✕ Requiere permiso específico
    NOT_APPLICABLE,    // - No aplica a este dispositivo
    PENDING            // ⏳ En proceso
}
