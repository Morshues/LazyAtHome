package com.morshues.lazyathome.websocket

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.morshues.lazyathome.ui.linkpage.LinkPageActivity
import kotlinx.coroutines.launch

private const val TAG = "WsCommandHandler"
private const val ACTION_KEY = "key"
private const val ACTION_OPEN_URL = "open_url"

/**
 * Collects WebSocket commands while [lifecycleOwner] is RESUMED.
 *
 * @param handler return `true` if handled, `false` to fall through to default handling
 *   (key-event dispatch + open_url).
 */
fun Activity.collectWsCommands(
    lifecycleOwner: LifecycleOwner,
    serverManager: WebSocketServerManager,
    handler: ((WsMessage) -> Boolean)? = null,
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            serverManager.commands.collect { msg ->
                if (handler?.invoke(msg) == true) return@collect
                handleDefaultCommand(msg)
            }
        }
    }
}

/**
 * Collects WebSocket commands while this Fragment's view is RESUMED.
 * Call this in [Fragment.onViewCreated].
 *
 * @param handler return `true` if handled, `false` to fall through to default handling
 *   (key-event dispatch + open_url on the host activity).
 */
fun Fragment.collectWsCommands(
    serverManager: WebSocketServerManager,
    handler: ((WsMessage) -> Boolean)? = null,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            serverManager.commands.collect { msg ->
                if (handler?.invoke(msg) == true) return@collect
                activity?.handleDefaultCommand(msg)
            }
        }
    }
}

private fun Activity.handleDefaultCommand(msg: WsMessage) {
    when (msg.action) {
        ACTION_KEY -> {
            val keyCodeName = msg.data?.get("keyCode")?.asString ?: return
            val keyCode = keyCodeFromName(keyCodeName) ?: run {
                Log.w(TAG, "Unknown keyCode: $keyCodeName")
                return
            }
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
        ACTION_OPEN_URL -> {
            val url = msg.data?.get("url")?.asString ?: return
            LinkPageActivity.start(this, url)
        }
    }
}

private fun keyCodeFromName(name: String): Int? {
    return try {
        KeyEvent::class.java.getField("KEYCODE_$name").getInt(null)
    } catch (e: Exception) {
        null
    }
}
