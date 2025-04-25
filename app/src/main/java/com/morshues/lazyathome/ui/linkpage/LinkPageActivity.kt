package com.morshues.lazyathome.ui.linkpage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.morshues.lazyathome.R
import com.morshues.lazyathome.databinding.ActivityLinkPageBinding
import com.morshues.lazyathome.settings.SettingsManager


class LinkPageActivity : ComponentActivity() {
    private lateinit var binding: ActivityLinkPageBinding

    private var dragCenterX = 0
    private var dragCenterY = 0
    private var dragScrollSpeed = 100f

    private var controlPanelVisible = false
    private var currentMode = RemoteMode.DRAG_SCROLL
    private var currentZoom = 1.0f

    private val controlPanelTimeout = 5000L
    private val hidePanelHandler = Handler(Looper.getMainLooper())
    private val hidePanelRunnable = Runnable {
        binding.controlPanel.isVisible = false
        controlPanelVisible = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dragScrollSpeed = SettingsManager.getPageScrollSpeed(this)

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
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    return handleCenterKey()
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    return handleDirectionalKey(Direction.UP)
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    return handleDirectionalKey(Direction.DOWN)
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    return handleDirectionalKey(Direction.LEFT)
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    return handleDirectionalKey(Direction.RIGHT)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showControlPanel() {
        controlPanelVisible = true
        binding.controlPanel.isVisible = true
        updatePanel()
        resetPanelTimeout()
    }

    private fun resetPanelTimeout() {
        hidePanelHandler.removeCallbacks(hidePanelRunnable)
        hidePanelHandler.postDelayed(hidePanelRunnable, controlPanelTimeout)
    }

    private fun updatePanel() {
        when (currentMode) {
            RemoteMode.DRAG_SCROLL ->
                binding.controlModeText.text = getString(R.string.link_page_mode_drag_scroll)
            RemoteMode.CHANGE_DRAG_POSITION ->
                binding.controlModeText.text = getString(R.string.link_page_mode_change_drag_pos)
            RemoteMode.ZOOM_MODE ->
                binding.controlModeText.text = getString(R.string.link_page_mode_zoom)
        }
        binding.dragAnchor.isVisible = (currentMode == RemoteMode.CHANGE_DRAG_POSITION)
    }

    private fun handleCenterKey(): Boolean {
        if (!controlPanelVisible) {
            showControlPanel()
        } else {
            val nextModeIndex = (currentMode.ordinal + 1) % RemoteMode.entries.size
            currentMode = RemoteMode.entries[nextModeIndex]
            updatePanel()
            resetPanelTimeout()
        }
        return true
    }

    private fun handleDirectionalKey(direction: Direction): Boolean {
        when (currentMode) {
            RemoteMode.DRAG_SCROLL -> {
                simulateDragScroll(binding.webView, direction)
            }

            RemoteMode.CHANGE_DRAG_POSITION -> {
                when (direction) {
                    Direction.UP -> {
                        dragCenterY = (dragCenterY - 5).coerceAtLeast(1)
                    }
                    Direction.DOWN -> {
                        dragCenterY = (dragCenterY + 5).coerceAtMost(binding.webView.height)
                    }
                    Direction.LEFT -> {
                        dragCenterX = (dragCenterX - 5).coerceAtLeast(1)
                    }
                    Direction.RIGHT -> {
                        dragCenterX = (dragCenterX + 5).coerceAtMost(binding.webView.width)
                    }
                }
                updateDragAnchor()
            }

            RemoteMode.ZOOM_MODE -> {
                if (direction == Direction.UP) {
                    currentZoom += 0.1f
                } else if (direction == Direction.DOWN) {
                    currentZoom -= 0.1f
                }
                currentZoom = currentZoom.coerceIn(0.5f, 5.0f)
                binding.webView.evaluateJavascript("document.body.style.zoom = '${currentZoom}'", null)
//                binding.webView.setInitialScale((currentZoom * 100).toInt())
            }
        }
        return true
    }

    private fun simulateDragScroll(webView: WebView, direction: Direction) {
        val (deltaX, deltaY) = when (direction) {
            Direction.UP -> Pair(0f, dragScrollSpeed)
            Direction.DOWN -> Pair(0f, -dragScrollSpeed)
            Direction.LEFT -> Pair(-dragScrollSpeed, 0f)
            Direction.RIGHT -> Pair(dragScrollSpeed, 0f)
        }

        val endX = dragCenterX + deltaX
        val endY = dragCenterY + deltaY

        val downTime = SystemClock.uptimeMillis()

        val touchDown = MotionEvent.obtain(
            downTime, downTime,
            MotionEvent.ACTION_DOWN,
            dragCenterX.toFloat(), dragCenterY.toFloat(), 0
        )
        webView.dispatchTouchEvent(touchDown)
        touchDown.recycle()

        val moveEvent = MotionEvent.obtain(
            downTime,
            downTime + 10,
            MotionEvent.ACTION_MOVE,
            endX, endY, 0
        )
        webView.dispatchTouchEvent(moveEvent)
        moveEvent.recycle()
    }

    private fun updateDragAnchor() {
        val layoutParams = binding.dragAnchor.layoutParams as FrameLayout.LayoutParams

        layoutParams.marginStart = dragCenterX
        layoutParams.topMargin = dragCenterY

        binding.dragAnchor.layoutParams = layoutParams
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.controlPanel.isVisible) {
                hidePanelHandler.removeCallbacks(hidePanelRunnable)
                binding.controlPanel.isVisible = false
                controlPanelVisible = false
            } else {
                finish()
            }
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
                dragCenterX = binding.webView.width / 2
                dragCenterY = binding.webView.height / 2
                updateDragAnchor()
            }
        }
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT,
    }

    enum class RemoteMode {
        DRAG_SCROLL,
        CHANGE_DRAG_POSITION,
        ZOOM_MODE
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
