package com.morshues.lazyathome.data.api

import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LinkPage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Path

interface ApiService {
    @POST("tg/list")
    fun fetchTgVideoList(@Body requestBody: TgVideoListRequestData): Call<List<TgVideoItem>>

    @POST("tg/{id}/delete")
    suspend fun deleteTgItem(@Path("id") id: String)

    @GET("library/list")
    fun fetchLibraryList(): Call<List<LibraryItem>>

    @GET("link-page/list")
    fun fetchLinkPageList(): Call<List<LinkPage>>

    @POST("link-page/{id}/delete")
    suspend fun deleteLinkPageItem(@Path("id") id: String)
}