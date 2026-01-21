package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.UrlProvider
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
        get() = UrlProvider.baseUrl + "tg/video_api?id=" + id

    val thumbnail: String
        get() = UrlProvider.baseUrl + "tg/video_thumbs_api?id=" + id

    val durationStr: String
        get() = formatDuration(duration)
}