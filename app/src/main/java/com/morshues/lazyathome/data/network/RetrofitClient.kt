package com.morshues.lazyathome.data.network

import com.google.gson.GsonBuilder
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LibraryItemDeserializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = BuildConfig.BASE_URL

    private val gson = GsonBuilder()
        .registerTypeAdapter(LibraryItem::class.java, LibraryItemDeserializer())
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}