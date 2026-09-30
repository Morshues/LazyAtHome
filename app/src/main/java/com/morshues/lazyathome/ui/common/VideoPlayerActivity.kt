package com.morshues.lazyathome.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import com.morshues.lazyathome.databinding.ActivityVideoPlayerBinding
import com.morshues.lazyathome.di.VideoStreamingClient
import com.morshues.lazyathome.player.IPlayable
import com.morshues.lazyathome.player.VideoPlayerLauncherHolder
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.util.formatDurationMSPair
import com.morshues.lazyathome.websocket.WebSocketServerManager
import com.morshues.lazyathome.websocket.WsMessage
import com.morshues.lazyathome.websocket.collectWsCommands
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayerActivity : ComponentActivity() {

    @Inject
    @VideoStreamingClient
    lateinit var videoStreamingOkHttpClient: OkHttpClient

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var serverManager: WebSocketServerManager

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    private val playlist = VideoPlayerLauncherHolder.pendingPlaylist ?: emptyList()
    private var currentIndex: Int = 0

    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton

    private val controlPanelTimeout = 1_000L
    private var remoteSeekMs = 5_000L

    private val hideTimeProgressHandler = Handler(Looper.getMainLooper())
    private val hideTimeProgressRunnable = Runnable {
        binding.timeProgress.isVisible = false
    }

    private fun resetTimeProgressTimeout() {
        player?.let { p ->
            binding.timeProgress.text = formatDurationMSPair(p.currentPosition, p.duration)
            binding.timeProgress.isVisible = true
            hideTimeProgressHandler.removeCallbacks(hideTimeProgressRunnable)
            hideTimeProgressHandler.postDelayed(hideTimeProgressRunnable, controlPanelTimeout)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        @UnstableApi
        override fun handleOnBackPressed() {
            if (binding.playerView.isControllerFullyVisible) {
                binding.playerView.hideController()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            updateTitleVisibility()
            if (state == Player.STATE_ENDED) {
                playVideo(currentIndex + 1)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            refreshNavigationButtons()
            if (events.containsAny(
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                )) {
                broadcastState()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentIndex = intent.getIntExtra(EXTRA_VIDEO_INDEX, 0)

        initializePlayer()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        collectWsCommands(this, serverManager, wsRemoteHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val seekButtonsMs = settingsManager.getButtonSeekStepMs()
        val timeBarSeekMs = settingsManager.getTimeBarSeekStepMs()
        remoteSeekMs = settingsManager.getRemoteSeekStepMs()

        val loadingView = binding.playerView.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
        loadingView.indeterminateTintList = ColorStateList.valueOf(Color.WHITE)

        val timeBar = binding.playerView.findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
        timeBar.setKeyTimeIncrement(timeBarSeekMs)

        nextButton = binding.playerView.findViewById(androidx.media3.ui.R.id.exo_next)
        nextButton.setOnClickListener {
            playVideo(currentIndex+1)
        }

        prevButton = binding.playerView.findViewById(androidx.media3.ui.R.id.exo_prev)
        prevButton.setOnClickListener {
            playVideo(currentIndex-1)
        }

        val dataSourceFactory = OkHttpDataSource.Factory(videoStreamingOkHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)
        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(seekButtonsMs)
            .setSeekForwardIncrementMs(seekButtonsMs)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                addListener(playerListener)
            }
        playVideo(currentIndex)
        binding.playerView.player = player
        binding.playerView.setControllerVisibilityListener(PlayerControlView.VisibilityListener {
            updateTitleVisibility()
            refreshNavigationButtons()
        })
    }

    @OptIn(UnstableApi::class)
    private fun updateTitleVisibility() {
        binding.videoTitle.isVisible =
            binding.playerView.isControllerFullyVisible || player?.isPlaying != true
    }

    private fun refreshNavigationButtons() {
        prevButton.isEnabled = currentIndex > 0
        prevButton.alpha = if (prevButton.isEnabled) 1.0f else 0.3f
        nextButton.isEnabled = currentIndex < playlist.size - 1
        nextButton.alpha = if (nextButton.isEnabled) 1.0f else 0.3f
    }

    @SuppressLint("RestrictedApi")
    @OptIn(UnstableApi::class)
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isLeftRightKey = event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        if (!binding.playerView.isControllerFullyVisible && isLeftRightKey) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        seekBy(-remoteSeekMs)
                    } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        seekBy(remoteSeekMs)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> {
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun seekBy(deltaMs: Long) {
        player?.let { p ->
            val duration = p.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
            p.seekTo((p.currentPosition + deltaMs).coerceIn(0, duration))
            resetTimeProgressTimeout()
        }
    }

    private fun seekTo(positionMs: Long) {
        player?.let { p ->
            val duration = p.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
            p.seekTo(positionMs.coerceIn(0, duration))
            resetTimeProgressTimeout()
        }
    }

    private val wsRemoteHandler = fun (msg: WsMessage): Boolean {
        if (msg.action != WsMessage.ACTION_VIDEO_CONTROL) return false

        val data = msg.data
        when (data?.get("instruction")?.asString) {
            "seek" -> runCatching { data.get("ms")?.asLong }.getOrNull()?.let { seekBy(it) }
            "seek_to" -> {
                val ms = runCatching { data.get("ms")?.asLong }.getOrNull()
                val percent = runCatching { data.get("percent")?.asFloat }.getOrNull()
                if (ms != null) {
                    seekTo(ms)
                } else if (percent != null) {
                    player?.duration?.takeIf { it > 0 }?.let { duration ->
                        seekTo((duration * percent.coerceIn(0f, 100f) / 100f).toLong())
                    }
                }
            }
            "play" -> player?.play()
            "pause" -> player?.pause()
            "play_pause" -> player?.let { if (it.isPlaying) it.pause() else it.play() }
            "next" -> playVideo(currentIndex + 1)
            "previous" -> playVideo(currentIndex - 1)
            "speed" -> runCatching { data.get("value")?.asFloat }.getOrNull()?.let {
                player?.setPlaybackSpeed(it.coerceIn(0.25f, 4.0f))
            }
            "get_state" -> broadcastState()
        }

        return true
    }

    private fun broadcastState() {
        val p = player ?: return
        serverManager.broadcast(WsMessage.EVENT_VIDEO_STATE, mapOf(
            "title" to (playlist.getOrNull(currentIndex)?.title ?: ""),
            "index" to currentIndex.toString(),
            "count" to playlist.size.toString(),
            "position" to p.currentPosition.toString(),
            "duration" to p.duration.coerceAtLeast(0).toString(),
            "isPlaying" to p.isPlaying.toString(),
            "speed" to p.playbackParameters.speed.toString(),
        ))
    }

    private fun playVideo(index: Int) {
        lifecycleScope.launch {
            playlist.getOrNull(index)?.apply {
                binding.videoTitle.text = title
                player?.apply {
                    val mediaItem = MediaItem.Builder()
                        .setUri(resolveUrl())
                        .build()
                    setMediaItem(mediaItem)
                    playWhenReady = true
                    prepare()
                    currentIndex = index
                }
            }
        }
    }

    private fun releasePlayer() {
        player?.removeListener(playerListener)
        player?.release()
        player = null
    }

    companion object {
        private const val EXTRA_VIDEO_INDEX = "extra_video_index"

        fun start(context: Context, list: List<IPlayable>, index: Int) {
            VideoPlayerLauncherHolder.pendingPlaylist = list
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_INDEX, index)
            }
            context.startActivity(intent)
        }
    }
}