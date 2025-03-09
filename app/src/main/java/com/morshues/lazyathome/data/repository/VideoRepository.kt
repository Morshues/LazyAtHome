package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.model.VideoItem
import com.morshues.lazyathome.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoRepository {
    fun fetchVideoList(onSuccess: (List<VideoItem>) -> Unit, onError: (String) -> Unit) {
        val call = RetrofitClient.apiService.fetchVideoList()

        call.enqueue(object : Callback<List<VideoItem>> {
            override fun onResponse(call: Call<List<VideoItem>>, response: Response<List<VideoItem>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("No Response")
                } else {
                    onError("Request Failed, status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<VideoItem>>, t: Throwable) {
                onError("Request Failed: ${t.message}")
            }
        })
    }
}