package com.morshues.lazyathome.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.morshues.lazyathome.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : ComponentActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

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

        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: return
        initializePlayer(videoUrl)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initializePlayer(videoUrl: String) {
        player = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
        }
        binding.playerView.player = player
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
        private const val EXTRA_VIDEO_URL = "extra_video_url"

        fun start(context: Context, url: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, url)
            }
            context.startActivity(intent)
        }
    }
}