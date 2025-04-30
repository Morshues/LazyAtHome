package com.morshues.lazyathome.data.api

import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.model.DeleteRequestData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call

interface ApiService {
    @POST("tg/list")
    fun fetchTgVideoList(@Body requestBody: TgVideoListRequestData): Call<List<TgVideoItem>>

    @POST("tg/delete")
    suspend fun deleteTgItem(@Body requestBody: DeleteRequestData)

    @GET("library/list")
    fun fetchLibraryList(): Call<List<LibraryItem>>

    @GET("link-page/list")
    fun fetchLinkPageList(): Call<List<LinkPage>>

    @POST("link-page/delete")
    suspend fun deleteLinkPageItem(@Body requestBody: DeleteRequestData)
}