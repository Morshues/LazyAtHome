package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.model.LinkPageDeleteRequestData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LinkPageRepository(
    private val api: ApiService
) {
    fun fetchLinkPageList(onSuccess: (List<LinkPage>) -> Unit, onError: (String) -> Unit) {
        val call = api.fetchLinkPageList()

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

    suspend fun deleteLinkPage(id: String): Boolean {
        return try {
            val requestBody = LinkPageDeleteRequestData(id)
            api.deleteLinkPageItem(requestBody)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}