package com.morshues.lazyathome.data.network

import com.morshues.lazyathome.data.api.BanggaApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BanggaRetrofitClient {
    private const val BASE_URL = "https://twbangga.moe.edu.tw/"

    val apiService: BanggaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BanggaApiService::class.java)
    }
}