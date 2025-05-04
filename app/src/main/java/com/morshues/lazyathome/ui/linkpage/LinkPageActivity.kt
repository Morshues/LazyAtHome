package com.morshues.lazyathome.ui.linkpage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.morshues.lazyathome.databinding.ActivityLinkPageBinding


class LinkPageActivity : ComponentActivity() {
    private lateinit var binding: ActivityLinkPageBinding
    private lateinit var remoteControlHelper: RemoteControlHelper

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        remoteControlHelper = RemoteControlHelper(
            context = this,
            webView = binding.webView,
            dragAnchor = binding.dragAnchor,
            controlPanel = binding.controlPanel,
            controlModeText = binding.controlModeText,
        )

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            val url = intent.getStringExtra(EXTRA_URL)
            if (url != null) {
                webViewClient = myWebViewClient
                loadUrl(url)
            } else {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (remoteControlHelper.onKeyEvent(event)) return true
        return super.dispatchKeyEvent(event)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!remoteControlHelper.onBackPressed()) finish()
        }
    }

    private val myWebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)

            view.evaluateJavascript("""
                document.querySelectorAll('iframe').forEach(item => item.style.display = 'none')
                document.querySelectorAll('script').forEach(item => item.remove())

                // Remove All shadowRoot
                const treeWalker = document.createTreeWalker(document.body, NodeFilter.SHOW_ELEMENT)
                while (treeWalker.nextNode()) {
                    const el = treeWalker.currentNode
                    if (el.shadowRoot) {
                      el.shadowRoot.innerHTML = ''
                    }
                }
            """.trimIndent(), null)

            binding.webView.post {
                remoteControlHelper
                    .setDragCenter(binding.webView.width / 2f, binding.webView.height / 2f)
            }
        }
    }

    companion object {
        private const val EXTRA_URL = "extra_url"

        fun start(context: Context, url: String) {
            val intent = Intent(context, LinkPageActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
            context.startActivity(intent)
        }
    }
}
