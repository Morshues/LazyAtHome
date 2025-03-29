package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.BanggaApiService
import com.morshues.lazyathome.data.model.BanggaAnimationItem
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaVideoItem
import com.morshues.lazyathome.data.network.BanggaRetrofitClient

class BanggaRepository(
    private val api: BanggaApiService = BanggaRetrofitClient.apiService
) {

    suspend fun fetchCategoryItems(): Result<List<BanggaCategoryItem>> {
        return try {
            val data = api.fetchCategory()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAnimationItem(id: String): Result<BanggaAnimationItem> {
        return try {
            val data = api.fetchAnimationItem(id)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchVideoItem(id: String): Result<BanggaVideoItem> {
        return try {
            val data = api.fetchVideoItem(id)
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}