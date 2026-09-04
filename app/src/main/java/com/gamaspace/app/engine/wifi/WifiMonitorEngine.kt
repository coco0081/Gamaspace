package com.gamaspace.app.engine.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

/**
 * Motor de monitoreo Wi-Fi REAL
 * Obtiene datos verificables de la red
 */
class WifiMonitorEngine(private val context: Context) {

    /**
     * Obtiene información actual de Wi-Fi
     */
    suspend fun getWifiInfo(): WifiInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo

            if (connectionInfo == null) {
                return@withContext WifiInfo()
            }

            WifiInfo(
                ssid = connectionInfo.ssid?.trim('"') ?: "Unknown",
                linkSpeed = connectionInfo.linkSpeed,
                ipAddress = connectionInfo.ipAddress.toString(),
                macAddress = connectionInfo.macAddress ?: "Unknown",
                frequency = if (android.os.Build.VERSION.SDK_INT >= 30) {
                    connectionInfo.frequency
                } else {
                    0
                },
                rssi = connectionInfo.rssi,
                connected = true
            )
        } catch (e: Exception) {
            WifiInfo()
        }
    }

    /**
     * Mide latencia (ping) a un servidor
     */
    suspend fun measurePing(host: String = "8.8.8.8", port: Int = 53): PingResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetAddress.getByName(host).socketAddress, 3000)
            socket.close()
            val endTime = System.currentTimeMillis()
            val latency = (endTime - startTime).toInt()

            PingResult(
                host = host,
                latencyMs = latency,
                success = true
            )
        } catch (e: Exception) {
            PingResult(
                host = host,
                latencyMs = -1,
                success = false,
                error = e.message ?: "Connection failed"
            )
        }
    }

    /**
     * Calcula jitter (variación de latencia)
     * Realiza múltiples pings y calcula la desviación estándar
     */
    suspend fun measureJitter(host: String = "8.8.8.8", samples: Int = 5): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            val latencies = mutableListOf<Int>()

            repeat(samples) {
                val result = measurePing(host)
                if (result.success) {
                    latencies.add(result.latencyMs)
                }
            }

            if (latencies.isEmpty()) return@withContext 0f

            val average = latencies.average()
            val variance = latencies.map { (it - average) * (it - average) }.average()
            val stdDev = Math.sqrt(variance).toFloat()

            stdDev
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Mide pérdida de paquetes enviando pings
     */
    suspend fun measurePacketLoss(host: String = "8.8.8.8", samples: Int = 10): Float = withContext(Dispatchers.IO) {
        return@withContext try {
            var successCount = 0

            repeat(samples) {
                val result = measurePing(host)
                if (result.success) {
                    successCount++
                }
            }

            val lossPercent = ((samples - successCount).toFloat() / samples) * 100
            lossPercent.coerceIn(0f, 100f)
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * Obtiene información de red actual
     */
    suspend fun getNetworkInfo(): NetworkInfo = withContext(Dispatchers.IO) {
        return@withContext try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            if (capabilities != null) {
                NetworkInfo(
                    isConnected = true,
                    isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                    isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
                    downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps,
                    upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
                )
            } else {
                NetworkInfo(isConnected = false)
            }
        } catch (e: Exception) {
            NetworkInfo(isConnected = false)
        }
    }
}

/**
 * Información de Wi-Fi actual
 */
data class WifiInfo(
    val ssid: String = "Not Connected",
    val linkSpeed: Int = 0,
    val ipAddress: String = "",
    val macAddress: String = "",
    val frequency: Int = 0,
    val rssi: Int = 0,
    val connected: Boolean = false
) {
    /**
     * Califica la señal Wi-Fi
     */
    fun getSignalQuality(): String {
        return when {
            rssi >= -50 -> "EXCELLENT"
            rssi >= -60 -> "VERY GOOD"
            rssi >= -70 -> "GOOD"
            rssi >= -80 -> "FAIR"
            else -> "POOR"
        }
    }
}

/**
 * Resultado de medición de ping
 */
data class PingResult(
    val host: String,
    val latencyMs: Int,
    val success: Boolean,
    val error: String? = null
)

/**
 * Información de red
 */
data class NetworkInfo(
    val isConnected: Boolean = false,
    val isWifi: Boolean = false,
    val isCellular: Boolean = false,
    val downstreamBandwidth: Int = 0,
    val upstreamBandwidth: Int = 0
)
