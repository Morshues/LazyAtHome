package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.RetrofitClient

data class VideoItem(
    val id: String,
    private val thumbnail: String,
) {
    val url: String
        get() = RetrofitClient.BASE_URL + "video/v/" + id
    val src: String
        get() = RetrofitClient.BASE_URL + "video/thumbnails/" + thumbnail
}