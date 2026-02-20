package com.morshues.lazyathome

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.morshues.lazyathome.websocket.WebSocketServerManager
import com.morshues.lazyathome.websocket.WebSocketService
import com.morshues.lazyathome.websocket.collectWsCommands
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Loads [MainFragment].
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var serverManager: WebSocketServerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }

        startService(Intent(this, WebSocketService::class.java))

        collectWsCommands(this, serverManager)
    }
}