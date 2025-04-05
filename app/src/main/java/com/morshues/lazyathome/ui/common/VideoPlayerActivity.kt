package com.morshues.lazyathome.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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

    private fun playNext() {
        lifecycleScope.launch {
            val item = playlist.getOrNull(currentIndex + 1) ?: return@launch
            val url = item.resolveUrl()

            binding.videoTitle.text = item.title ?: ""
            player?.apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .build()
                setMediaItem(mediaItem)
                playWhenReady = true
                prepare()
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentIndex = intent.getIntExtra(EXTRA_VIDEO_INDEX, 0)

        lifecycleScope.launch {
            val item = playlist.getOrNull(currentIndex) ?: return@launch
            val url = item.resolveUrl()
            val title = item.title ?: ""
            initializePlayer(url, title)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoUrl: String, title: String) {
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    updateTitleVisibility()
                    if (state == Player.STATE_ENDED) {
                        playNext()
                    }
                }
            })
            binding.videoTitle.text = title
            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .build()
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
        }
        binding.playerView.player = player
        binding.playerView.setControllerVisibilityListener(PlayerControlView.VisibilityListener {
            updateTitleVisibility()
        })
    }

    @OptIn(UnstableApi::class)
    private fun updateTitleVisibility() {
        binding.videoTitle.isVisible =
            binding.playerView.isControllerFullyVisible || player?.isPlaying != true
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
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