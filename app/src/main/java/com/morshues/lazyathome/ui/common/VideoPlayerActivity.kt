package com.morshues.lazyathome.ui.common

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import com.morshues.lazyathome.databinding.ActivityVideoPlayerBinding
import com.morshues.lazyathome.player.IPlayable
import com.morshues.lazyathome.player.VideoPlayerLauncherHolder
import kotlinx.coroutines.launch

class VideoPlayerActivity : ComponentActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    private val playlist = VideoPlayerLauncherHolder.pendingPlaylist ?: emptyList()
    private var currentIndex: Int = 0

    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton

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

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val loadingView = binding.playerView.findViewById<ProgressBar>(androidx.media3.ui.R.id.exo_buffering)
        loadingView.indeterminateTintList = ColorStateList.valueOf(Color.WHITE)

        val timeBar = binding.playerView.findViewById<DefaultTimeBar>(androidx.media3.ui.R.id.exo_progress)
        timeBar.setKeyTimeIncrement(5_000)

        nextButton = binding.playerView.findViewById(androidx.media3.ui.R.id.exo_next)
        nextButton.setOnClickListener {
            playVideo(currentIndex+1)
        }

        prevButton = binding.playerView.findViewById(androidx.media3.ui.R.id.exo_prev)
        prevButton.setOnClickListener {
            playVideo(currentIndex-1)
        }

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(30_000)
            .setSeekForwardIncrementMs(30_000)
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

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        releasePlayer()
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