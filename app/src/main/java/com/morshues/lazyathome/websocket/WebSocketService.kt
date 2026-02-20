package com.morshues.lazyathome.websocket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.morshues.lazyathome.settings.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

@AndroidEntryPoint
class WebSocketService : Service() {

    @Inject lateinit var serverManager: WebSocketServerManager
    @Inject lateinit var settingsManager: SettingsManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = settingsManager.getWebSocketPort()
        serverManager.start(port)

        val ip = getLocalIpAddress() ?: "unknown"
        Log.i(TAG, "WebSocket service started at ws://$ip:$port")

        return START_STICKY
    }

    override fun onDestroy() {
        serverManager.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.asSequence()
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get local IP", e)
            null
        }
    }

    companion object {
        private const val TAG = "WebSocketService"
    }
}
