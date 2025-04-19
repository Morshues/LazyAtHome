package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.RetrofitClient
import java.util.Locale

data class TgVideoItem(
    val id: String,
    val filename: String,
    val duration: Float,
    val imgBase64: String,
) {
    val url: String
        get() = RetrofitClient.baseUrl + "tg/video?id=" + id

    val thumbnail: String
        get() = RetrofitClient.baseUrl + "tg/video_thumbs?id=" + id

    val durationStr: String
        get() = formatDuration(duration)

    companion object {
        fun formatDuration(seconds: Float): String {
            val totalSeconds = seconds.toInt()
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60

            return when {
                hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                minutes > 0 -> String.format(Locale.getDefault(), "%d:%02d", minutes, secs)
                else -> String.format(Locale.getDefault(), "%d", secs)
            }
        }
    }
}