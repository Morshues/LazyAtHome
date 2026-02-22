package com.morshues.lazyathome.websocket

import com.google.gson.Gson
import com.google.gson.JsonObject

data class WsMessage(
    val action: String,
    val data: JsonObject? = null,
) {

    companion object {
        private val gson = Gson()

        fun fromJson(json: String): WsMessage = gson.fromJson(json, WsMessage::class.java)

        fun toJson(action: String, data: Map<String, String>? = null): String {
            val msg = WsMessage(action, data?.let { gson.toJsonTree(it).asJsonObject })
            return gson.toJson(msg)
        }

        // Client -> Server
        const val ACTION_PING = "ping"
        const val ACTION_HOME = "home"
        const val ACTION_BACK = "back"
        const val ACTION_OPEN_URL = "open_url"
        const val ACTION_MAIN_NAVIGATE = "main_navigate"

        // Server -> Client
        const val EVENT_PONG = "pong"
        const val EVENT_ERROR = "error"
        const val EVENT_CURRENT_SCREEN = "current_screen"
    }
}
