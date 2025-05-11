package com.morshues.lazyathome.data.api

import com.morshues.lazyathome.data.model.EditLinkPageRequestData
import com.morshues.lazyathome.data.model.EditTgVideoRequestData
import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.model.LinkPageListRequestData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Path

interface ApiService {
    @POST("tg/list")
    fun fetchTgVideoList(@Body requestBody: TgVideoListRequestData): Call<List<TgVideoItem>>

    @POST("tg/{id}/edit")
    suspend fun editTgVideoItem(
        @Path("id") id: String,
        @Body requestBody: EditTgVideoRequestData,
    ): Response<TgVideoItem>

    @POST("tg/{id}/delete")
    suspend fun deleteTgItem(@Path("id") id: String)

    @GET("library/list")
    fun fetchLibraryList(): Call<List<LibraryItem>>

    @POST("link-page/list")
    fun fetchLinkPageList(@Body requestBody: LinkPageListRequestData): Call<List<LinkPage>>

    @POST("link-page/{id}/edit")
    suspend fun editLinkPageItem(
        @Path("id") id: String,
        @Body requestBody: EditLinkPageRequestData,
    ): Response<LinkPage>

    @POST("link-page/{id}/delete")
    suspend fun deleteLinkPageItem(@Path("id") id: String)
}