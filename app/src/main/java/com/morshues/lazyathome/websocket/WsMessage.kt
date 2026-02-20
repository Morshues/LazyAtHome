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

        const val ACTION_PING = "ping"
    }
}
