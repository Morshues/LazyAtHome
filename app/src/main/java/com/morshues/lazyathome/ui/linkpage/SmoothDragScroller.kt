package com.morshues.lazyathome.ui.linkpage

import android.os.SystemClock
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.exp

/**
 * Turns bursty drag deltas into a smooth, continuous touch gesture.
 *
 * Deltas are accumulated and drained every vsync with exponential smoothing, so network jitter
 * is absorbed. A whole burst of deltas is a single DOWN..MOVE..CANCEL gesture: the finger is never
 * lifted and re-grabbed mid-burst (it may travel outside the view, just like a real finger
 * dragged off the screen), because every new touchstart has to wait for the page's main thread.
 * The gesture ends with CANCEL rather than UP, so a short burst is never treated as a tap or fling.
 */
class SmoothDragScroller(private val view: View) {

    private val choreographer = Choreographer.getInstance()

    private var centerX = 0f
    private var centerY = 0f

    private var pendingX = 0f
    private var pendingY = 0f

    private var touching = false
    private var downTime = 0L
    private var fingerX = 0f
    private var fingerY = 0f
    private var lastMoveTime = 0L

    private var frameScheduled = false
    private var lastFrameNanos = 0L

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        frameScheduled = false
        onFrame(frameTimeNanos)
    }

    fun setCenter(x: Float, y: Float) {
        centerX = x
        centerY = y
    }

    fun addDelta(dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) return
        pendingX += dx
        pendingY += dy
        if (!touching) startGesture()
        scheduleFrame()
    }

    fun release() {
        choreographer.removeFrameCallback(frameCallback)
        frameScheduled = false
        pendingX = 0f
        pendingY = 0f
        if (touching) endGesture(MotionEvent.ACTION_CANCEL)
    }

    private fun onFrame(frameTimeNanos: Long) {
        val dtMs = if (lastFrameNanos == 0L) {
            FALLBACK_FRAME_MS
        } else {
            ((frameTimeNanos - lastFrameNanos) / 1_000_000f).coerceIn(1f, MAX_FRAME_MS)
        }
        lastFrameNanos = frameTimeNanos

        val now = SystemClock.uptimeMillis()

        if (abs(pendingX) < MIN_PENDING_PX) pendingX = 0f
        if (abs(pendingY) < MIN_PENDING_PX) pendingY = 0f

        if (pendingX != 0f || pendingY != 0f) {
            val ratio = 1f - exp(-dtMs / TAU_MS)
            val stepX = pendingX * ratio
            val stepY = pendingY * ratio
            pendingX -= stepX
            pendingY -= stepY

            fingerX += stepX
            fingerY += stepY
            dispatch(MotionEvent.ACTION_MOVE, fingerX, fingerY, now)
            lastMoveTime = now
            scheduleFrame()
        } else if (touching) {
            if (now - lastMoveTime >= IDLE_UP_MS) {
                endGesture(MotionEvent.ACTION_CANCEL)
                lastFrameNanos = 0L
            } else {
                scheduleFrame()
            }
        } else {
            lastFrameNanos = 0L
        }
    }

    private fun startGesture() {
        val now = SystemClock.uptimeMillis()
        downTime = now
        fingerX = centerX
        fingerY = centerY
        lastMoveTime = now
        touching = true
        dispatch(MotionEvent.ACTION_DOWN, fingerX, fingerY, now)
    }

    private fun endGesture(action: Int) {
        dispatch(action, fingerX, fingerY, SystemClock.uptimeMillis())
        touching = false
    }

    private fun scheduleFrame() {
        if (frameScheduled) return
        frameScheduled = true
        choreographer.postFrameCallback(frameCallback)
    }

    private fun dispatch(action: Int, x: Float, y: Float, eventTime: Long) {
        val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
        view.dispatchTouchEvent(event)
        event.recycle()
    }

    companion object {
        /** Smoothing time constant; roughly match the controller's send interval. */
        private const val TAU_MS = 50f
        /** End the gesture after being idle this long. */
        private const val IDLE_UP_MS = 120L
        private const val MIN_PENDING_PX = 0.5f
        private const val FALLBACK_FRAME_MS = 16f
        private const val MAX_FRAME_MS = 100f
    }
}
