package com.morshues.lazyathome.data.network

import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface ApiService {
    @POST("tg/list")
    fun fetchTgVideoList(@Body requestBody: TgVideoListRequestData): Call<List<TgVideoItem>>
}