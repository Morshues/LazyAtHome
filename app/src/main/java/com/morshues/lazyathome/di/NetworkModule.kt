package com.morshues.lazyathome.di

import com.google.gson.GsonBuilder
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.api.AuthApiService
import com.morshues.lazyathome.data.api.BanggaApiService
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.model.LibraryItemDeserializer
import com.morshues.lazyathome.data.network.DynamicUrlInterceptor
import com.morshues.lazyathome.data.network.TokenAuthenticator
import com.morshues.lazyathome.data.network.TokenInterceptor
import com.morshues.lazyathome.data.repository.AuthRepository
import com.morshues.lazyathome.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // region OkHttpClient

    @Provides
    @Singleton
    fun provideDynamicUrlInterceptor(settingsManager: SettingsManager): DynamicUrlInterceptor {
        return DynamicUrlInterceptor(settingsManager)
    }

    @Provides
    @Singleton
    fun provideTokenInterceptor(
        settingsManager: SettingsManager,
        authRepository: AuthRepository
    ): TokenInterceptor {
        return TokenInterceptor(settingsManager, authRepository)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        settingsManager: SettingsManager,
        authRepository: AuthRepository
    ): TokenAuthenticator {
        return TokenAuthenticator(settingsManager, authRepository)
    }

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedOkHttpClient(
        dynamicUrlInterceptor: DynamicUrlInterceptor,
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(dynamicUrlInterceptor)
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @VideoStreamingClient
    fun provideVideoStreamingOkHttpClient(
        tokenInterceptor: TokenInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // endregion

    // region ApiService

    @Provides
    @Singleton
    fun provideAuthApiService(
        dynamicUrlInterceptor: DynamicUrlInterceptor
    ): AuthApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(dynamicUrlInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(
        @AuthenticatedClient okHttpClient: OkHttpClient
    ): ApiService {
        val gson = GsonBuilder()
            .registerTypeAdapter(LibraryItem::class.java, LibraryItemDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBanggaApiService(): BanggaApiService {
        return Retrofit.Builder()
            .baseUrl("https://twbangga.moe.edu.tw/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BanggaApiService::class.java)
    }

    // endregion
}
