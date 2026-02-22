package com.morshues.lazyathome.websocket

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.morshues.lazyathome.ui.linkpage.LinkPageActivity
import kotlinx.coroutines.launch

/**
 * Collects WebSocket commands while [lifecycleOwner] is RESUMED.
 *
 * @param handler return `true` if handled, `false` to fall through to default handling
 *   (key-event dispatch + open_url).
 */
fun ComponentActivity.collectWsCommands(
    lifecycleOwner: LifecycleOwner,
    serverManager: WebSocketServerManager,
    handler: ((WsMessage) -> Boolean)? = null,
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            serverManager.broadcastScreenUpdated(lifecycleOwner.javaClass.simpleName)
            serverManager.commands.collect { msg ->
                if (handler?.invoke(msg) == true) return@collect
                handleDefaultCommand(msg)
            }
        }
    }
}

private fun ComponentActivity.handleDefaultCommand(msg: WsMessage) {
    when (msg.action) {
        WsMessage.ACTION_BACK -> onBackPressedDispatcher.onBackPressed()
        WsMessage.ACTION_OPEN_URL -> {
            val url = msg.data?.get("url")?.asString ?: return
            LinkPageActivity.start(this, url)
        }
    }
}