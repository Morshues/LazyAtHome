package com.morshues.lazyathome.data.api

import com.morshues.lazyathome.data.model.BanggaAnimationItem
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaVideoItem
import retrofit2.http.GET
import retrofit2.http.Path

interface BanggaApiService {
    @GET("server/web/category")
    suspend fun fetchCategory(): List<BanggaCategoryItem>

    @GET("server/web/animation/{id}")
    suspend fun fetchAnimationItem(@Path("id") id: String): BanggaAnimationItem

    @GET("server/web/animation/video/{id}")
    suspend fun fetchVideoItem(@Path("id") id: String): BanggaVideoItem
}