package com.morshues.lazyathome.websocket

import android.content.Context
import android.content.Intent
import android.util.Log
import com.morshues.lazyathome.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketServerManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var server: EmbeddedServer<*, *>? = null
    private val sessions = Collections.synchronizedSet<DefaultWebSocketSession>(LinkedHashSet())

    @Volatile var currentScreen: String? = null

    private val _commands = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    val commands: SharedFlow<WsMessage> = _commands.asSharedFlow()

    fun start(port: Int) {
        if (server != null) return

        server = embeddedServer(CIO, port = port) {
            install(WebSockets)
            routing {
                webSocket("/") {
                    sessions += this
                    Log.i(TAG, "Client connected (${sessions.size} total)")
                    currentScreen?.let { send(WsMessage.toJson(WsMessage.EVENT_CURRENT_SCREEN, mapOf("name" to it))) }
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                Log.d(TAG, "Received: $text")
                                try {
                                    val msg = WsMessage.fromJson(text)
                                    when (msg.action) {
                                        WsMessage.ACTION_PING -> send(WsMessage.toJson(WsMessage.EVENT_PONG))
                                        WsMessage.ACTION_HOME -> navigateHome()
                                        else -> {
                                            _commands.tryEmit(msg)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Invalid message: $text", e)
                                    send(WsMessage.toJson(WsMessage.EVENT_ERROR, mapOf("message" to "Invalid JSON")))
                                }
                            }
                        }
                    } finally {
                        sessions -= this
                        Log.i(TAG, "Client disconnected (${sessions.size} remaining)")
                    }
                }
            }
        }.also {
            it.start(wait = false)
            Log.i(TAG, "Server started on port $port")
        }
    }

    fun broadcast(action: String, data: Map<String, String>? = null) {
        val message = WsMessage.toJson(action, data)
        scope.launch {
            sessions.forEach { session ->
                runCatching { session.send(message) }
            }
        }
    }

    private fun navigateHome() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }

    fun broadcastScreenUpdated(screen: String) {
        broadcast(WsMessage.EVENT_CURRENT_SCREEN, mapOf("name" to screen))
        currentScreen = screen
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        Log.i(TAG, "Server stopped")
    }

    companion object {
        private const val TAG = "WebSocketServer"
    }
}
