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
import com.morshues.lazyathome.di.AppModule
import com.morshues.lazyathome.player.IPlayable
import com.morshues.lazyathome.player.VideoPlayerLauncherHolder
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.util.formatDurationMSPair
import kotlinx.coroutines.launch

class VideoPlayerActivity : ComponentActivity() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val seekButtonsMs = SettingsManager.getButtonSeekStepMs(this)
        val timeBarSeekMs = SettingsManager.getTimeBarSeekStepMs(this)
        remoteSeekMs = SettingsManager.getRemoteSeekStepMs(this)

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

        val dataSourceFactory = OkHttpDataSource.Factory(AppModule.videoStreamingOkHttpClient)
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
                    player?.let { p ->
                        val position = p.currentPosition
                        val duration = p.duration
                        if (event.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            val newPosition = (position - remoteSeekMs).coerceAtLeast(0)
                            p.seekTo(newPosition)
                            resetTimeProgressTimeout()
                        } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            val newPosition = (position + remoteSeekMs).coerceAtMost(duration)
                            p.seekTo(newPosition)
                            resetTimeProgressTimeout()
                        }
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