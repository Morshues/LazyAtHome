package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.RetrofitClient

data class TgVideoItem(
    val id: String,
) {
    val url: String
        get() = RetrofitClient.BASE_URL + "tg/video?fileId=" + id
}