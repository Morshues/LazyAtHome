package com.morshues.lazyathome.ui.linkpage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.morshues.lazyathome.R
import com.morshues.lazyathome.settings.SettingsManager
import java.lang.ref.WeakReference

class RemoteControlHelper(
    context: Context,
    private val webView: WebView,
    private val dragAnchor: View,
    private val controlPanel: View,
    private val controlModeText: TextView,
) {
    private val contextRef = WeakReference(context)

    private val keyDownTimes = mutableMapOf<Int, Long>()
    private val longClickThreshold = 500L
    private val controlPanelTimeout = 5000L
    private val hidePanelHandler = Handler(Looper.getMainLooper())
    private val hidePanelRunnable = Runnable {
        controlPanel.isVisible = false
    }

    private var dragScrollSpeed = SettingsManager.getPageScrollSpeed(context)
    private var dragCenterX = 0f
    private var dragCenterY = 0f
    private var currentMode = RemoteMode.DRAG_SCROLL
    private var currentZoom = 1.0f

    fun initScripts() {
        webView.evaluateJavascript("""
            console.error("evaluate" + JSON.stringify(window.__invertSetup))
            if (typeof window.__invertSetup === 'undefined') {
                window.__invertSetup = true;
                window.__invertState = false;
                window.__invertStyle = document.createElement('style');
                document.head.appendChild(window.__invertStyle);
        
                window.toggleInvert = function() {
                    window.__invertState = !window.__invertState;
                    if (window.__invertState) {
                        window.__invertStyle.innerHTML = `
                            body {
                                filter: invert(1) hue-rotate(180deg) !important;
                                background: #000 !important;
                            }
                            img, video, picture, iframe {
                                filter: invert(1) hue-rotate(180deg) !important;
                            }
                        `;
                    } else {
                        window.__invertStyle.innerHTML = '';
                    }
                };
            }
        """.trimIndent(), null)
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (event.repeatCount == 0) {
                            keyDownTimes[event.keyCode] = SystemClock.uptimeMillis()
                        } else {
                            handleHover(event.keyCode, event.repeatCount)
                        }
                        return true
                    }
                    KeyEvent.ACTION_UP -> {
                        val downTime = keyDownTimes.remove(event.keyCode)
                        if (downTime != null) {
                            val pressDuration = SystemClock.uptimeMillis() - downTime
                            if (pressDuration >= longClickThreshold) {
                                handleLongClick(event.keyCode)
                            } else {
                                handleClick(event.keyCode)
                            }
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    fun onBackPressed(): Boolean {
        return if (controlPanel.isVisible) {
            hidePanelHandler.removeCallbacks(hidePanelRunnable)
            controlPanel.isVisible = false
            true
        } else {
            false
        }
    }

    fun setDragCenter(x: Float, y: Float) {
        dragCenterX = x
        dragCenterY = y
        updateDragAnchor()
    }

    private fun handleClick(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> handleCenterKey(false)
            KeyEvent.KEYCODE_DPAD_UP -> handleDirectionalKey(Direction.UP)
            KeyEvent.KEYCODE_DPAD_DOWN -> handleDirectionalKey(Direction.DOWN)
            KeyEvent.KEYCODE_DPAD_LEFT -> handleDirectionalKey(Direction.LEFT)
            KeyEvent.KEYCODE_DPAD_RIGHT -> handleDirectionalKey(Direction.RIGHT)
        }
    }

    private fun handleLongClick(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> handleCenterKey(true)
        }
    }

    private fun handleHover(keyCode: Int, repeatCount: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (repeatCount == 1) {
                    showControlPanel()
                    keyDownTimes.remove(keyCode)
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> handleDirectionalKey(Direction.UP)
            KeyEvent.KEYCODE_DPAD_DOWN -> handleDirectionalKey(Direction.DOWN)
            KeyEvent.KEYCODE_DPAD_LEFT -> handleDirectionalKey(Direction.LEFT)
            KeyEvent.KEYCODE_DPAD_RIGHT -> handleDirectionalKey(Direction.RIGHT)
        }
    }

    private fun handleCenterKey(isLongClick: Boolean) {
        if (!controlPanel.isVisible) {
            if (isLongClick) {
                showControlPanel()
            } else {
                simulateWebViewClick(webView, dragCenterX, dragCenterY)
            }
        } else {
            val nextModeIndex = (currentMode.ordinal + 1) % RemoteMode.entries.size
            currentMode = RemoteMode.entries[nextModeIndex]
            updatePanel()
            resetPanelTimeout()
        }
    }

    private fun handleDirectionalKey(direction: Direction) {
        when (currentMode) {
            RemoteMode.DRAG_SCROLL -> simulateDragScroll(webView, direction)
            RemoteMode.CHANGE_DRAG_POSITION -> {
                when (direction) {
                    Direction.UP -> dragCenterY = (dragCenterY - 20).coerceAtLeast(1f)
                    Direction.DOWN -> dragCenterY = (dragCenterY + 20).coerceAtMost(webView.height.toFloat())
                    Direction.LEFT -> dragCenterX = (dragCenterX - 20).coerceAtLeast(1f)
                    Direction.RIGHT -> dragCenterX = (dragCenterX + 20).coerceAtMost(webView.width.toFloat())
                }
                updateDragAnchor()
            }
            RemoteMode.ZOOM_MODE -> {
                if (direction == Direction.UP) {
                    currentZoom += 0.1f
                } else if (direction == Direction.DOWN) {
                    currentZoom -= 0.1f
                }
                webView.evaluateJavascript("document.body.style.zoom = '${currentZoom}'", null)
                // webView.setInitialScale((currentZoom * 100).toInt())
            }
            RemoteMode.EXTRA -> handleExtraMode(direction)
        }
    }

    private fun handleExtraMode(direction: Direction) {
        when (direction) {
            Direction.UP -> {
                webView.evaluateJavascript("""
                    window.toggleInvert()
                """.trimIndent(), null)
            }
            else -> Toast.makeText(contextRef.get(), "No function", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePanel() {
        val resId = when (currentMode) {
            RemoteMode.DRAG_SCROLL -> R.string.link_page_mode_drag_scroll
            RemoteMode.CHANGE_DRAG_POSITION -> R.string.link_page_mode_change_drag_pos
            RemoteMode.ZOOM_MODE -> R.string.link_page_mode_zoom
            RemoteMode.EXTRA -> R.string.link_page_mode_extra
        }
        controlModeText.text = contextRef.get()?.getString(resId)
        dragAnchor.isVisible = (currentMode == RemoteMode.CHANGE_DRAG_POSITION)
    }

    private fun updateDragAnchor() {
        val layoutParams = dragAnchor.layoutParams as FrameLayout.LayoutParams
        layoutParams.marginStart = dragCenterX.toInt()
        layoutParams.topMargin = dragCenterY.toInt()
        dragAnchor.layoutParams = layoutParams
    }

    private fun showControlPanel() {
        controlPanel.isVisible = true
        updatePanel()
        resetPanelTimeout()
    }

    private fun resetPanelTimeout() {
        hidePanelHandler.removeCallbacks(hidePanelRunnable)
        hidePanelHandler.postDelayed(hidePanelRunnable, controlPanelTimeout)
    }

    private fun simulateWebViewClick(webView: WebView, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(
            downTime, downTime,
            MotionEvent.ACTION_DOWN,
            x, y, 0
        )
        val upEvent = MotionEvent.obtain(
            downTime, downTime + 100,
            MotionEvent.ACTION_UP,
            x, y, 0
        )
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
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
            dragCenterX, dragCenterY, 0
        )
        webView.dispatchTouchEvent(touchDown)
        touchDown.recycle()
        val moveEvent = MotionEvent.obtain(
            downTime, downTime + 10,
            MotionEvent.ACTION_MOVE,
            endX, endY, 0
        )
        webView.dispatchTouchEvent(moveEvent)
        moveEvent.recycle()
    }

    enum class Direction { UP, DOWN, LEFT, RIGHT }
    enum class RemoteMode { DRAG_SCROLL, CHANGE_DRAG_POSITION, ZOOM_MODE, EXTRA }
}
