package com.gamaspace.app.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de overlay en-game
 * Panel flotante que aparece durante el gaming
 */
class PerformanceOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isOverlayVisible = false

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_SHOW_OVERLAY -> showOverlay()
            ACTION_HIDE_OVERLAY -> hideOverlay()
            ACTION_UPDATE_STATS -> updateStats(intent)
            ACTION_STOP_SERVICE -> stopSelf()
        }

        return START_STICKY
    }

    private fun showOverlay() {
        if (isOverlayVisible) return

        try {
            val layoutInflater = LayoutInflater.from(this)
            overlayView = createOverlayView()

            val params = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                width = 300
                height = 200
                gravity = Gravity.TOP or Gravity.RIGHT
                x = 0
                y = 100
            }

            windowManager.addView(overlayView, params)
            isOverlayVisible = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        if (!isOverlayVisible || overlayView == null) return

        try {
            windowManager.removeView(overlayView)
            overlayView = null
            isOverlayVisible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStats(intent: Intent) {
        val cpu = intent.getFloatExtra("cpu", 0f)
        val ram = intent.getFloatExtra("ram", 0f)
        val temp = intent.getFloatExtra("temp", 0f)
        val ping = intent.getIntExtra("ping", 0)
        val fps = intent.getIntExtra("fps", 0)

        scope.launch {
            // Actualizar vista con nuevos datos
        }
    }

    private fun createOverlayView(): View {
        // Crear vista de overlay personalizada
        // Esto será un ComposeView en implementación real
        return View(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW_OVERLAY = "com.gamaspace.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.gamaspace.HIDE_OVERLAY"
        const val ACTION_UPDATE_STATS = "com.gamaspace.UPDATE_STATS"
        const val ACTION_STOP_SERVICE = "com.gamaspace.STOP_SERVICE"
    }
}
