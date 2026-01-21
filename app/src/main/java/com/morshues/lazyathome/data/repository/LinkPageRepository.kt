package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.EditLinkPageRequestData
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.model.LinkPageListRequestData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LinkPageRepository @Inject constructor(
    private val api: ApiService
) {
    fun fetchLinkPageList(nsfw: Boolean, onSuccess: (List<LinkPage>) -> Unit, onError: (String) -> Unit) {
        val requestBody = LinkPageListRequestData(nsfw)
        val call = api.fetchLinkPageList(requestBody)

        call.enqueue(object : Callback<List<LinkPage>> {
            override fun onResponse(call: Call<List<LinkPage>>, response: Response<List<LinkPage>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("No Response")
                } else {
                    onError("Request Failed, status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<LinkPage>>, t: Throwable) {
                onError("Request Failed: ${t.message}")
            }
        })
    }

    suspend fun editLinkPage(id: String, data: EditLinkPageRequestData): LinkPage? {
        return try {
            api.editLinkPageItem(id, data).body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteLinkPage(id: String): Boolean {
        return try {
            api.deleteLinkPageItem(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}