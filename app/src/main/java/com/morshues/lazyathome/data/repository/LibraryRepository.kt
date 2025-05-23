package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.model.LibraryItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryRepository(
    private val api: ApiService
) {
    fun fetchLibraryList(onSuccess: (List<LibraryItem>) -> Unit, onError: (String) -> Unit) {
        val call = api.fetchLibraryList()

        call.enqueue(object : Callback<List<LibraryItem>> {
            override fun onResponse(call: Call<List<LibraryItem>>, response: Response<List<LibraryItem>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    } ?: onError("No Response")
                } else {
                    onError("Request Failed, status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<LibraryItem>>, t: Throwable) {
                onError("Request Failed: ${t.message}")
            }
        })
    }
}