package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.EditTgVideoRequestData
import com.morshues.lazyathome.data.model.TgVideoListRequestData
import com.morshues.lazyathome.data.model.TgVideoItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TgVideoRepository(
    private val api: ApiService
) {
    fun fetchVideoList(nsfw: Boolean, onSuccess: (List<TgVideoItem>) -> Unit, onError: (String) -> Unit) {
        val requestBody = TgVideoListRequestData(nsfw)
        val call = api.fetchTgVideoList(requestBody)

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

    suspend fun editLinkPage(id: String, data: EditTgVideoRequestData): TgVideoItem? {
        return try {
            api.editTgVideoItem(id, data).body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteTgItem(id: String): Boolean {
        return try {
            api.deleteTgItem(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}