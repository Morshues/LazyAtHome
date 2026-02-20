package com.morshues.lazyathome.websocket

import android.util.Log
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketServerManager @Inject constructor() {

    private var server: EmbeddedServer<*, *>? = null

    private val _commands = MutableSharedFlow<WsMessage>(extraBufferCapacity = 64)
    val commands: SharedFlow<WsMessage> = _commands.asSharedFlow()

    fun start(port: Int) {
        if (server != null) return

        server = embeddedServer(CIO, port = port) {
            install(WebSockets)
            routing {
                webSocket("/") {
                    Log.i(TAG, "Client connected")
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                Log.d(TAG, "Received: $text")
                                try {
                                    val msg = WsMessage.fromJson(text)
                                    if (msg.action == WsMessage.ACTION_PING) {
                                        send(WsMessage.toJson("pong"))
                                    } else {
                                        _commands.tryEmit(msg)
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Invalid message: $text", e)
                                    send(WsMessage.toJson("error", mapOf("message" to "Invalid JSON")))
                                }
                            }
                        }
                    } finally {
                        Log.i(TAG, "Client disconnected")
                    }
                }
            }
        }.also {
            it.start(wait = false)
            Log.i(TAG, "Server started on port $port")
        }
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
