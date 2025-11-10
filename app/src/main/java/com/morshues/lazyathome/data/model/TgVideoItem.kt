package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.RetrofitClient
import com.morshues.lazyathome.util.formatDuration

data class TgVideoItem(
    val id: String,
    val filename: String,
    val duration: Float,
    val imgBase64: String,
    val nsfw: Boolean,
    val createdAt: String,
) {
    val url: String
        get() = RetrofitClient.baseUrl + "tg/video?id=" + id

    val thumbnail: String
        get() = RetrofitClient.baseUrl + "tg/video_thumbs?id=" + id

    val durationStr: String
        get() = formatDuration(duration)
}