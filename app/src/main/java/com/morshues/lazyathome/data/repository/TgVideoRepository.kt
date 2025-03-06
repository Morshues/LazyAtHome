package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TgVideoRepository {
    fun fetchVideoList(onSuccess: (List<TgVideoItem>) -> Unit, onError: (String) -> Unit) {
        val requestBody = TgVideoListRequestData(null)
        val call = RetrofitClient.apiService.fetchTgVideoList(requestBody)

        call.enqueue(object : Callback<List<TgVideoItem>> {
            override fun onResponse(call: Call<List<TgVideoItem>>, response: Response<List<TgVideoItem>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("No Response")
                } else {
                    onError("Request Failed, status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<TgVideoItem>>, t: Throwable) {
                onError("Request Failed: ${t.message}")
            }
        })
    }
}