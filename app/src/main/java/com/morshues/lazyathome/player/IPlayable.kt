package com.morshues.lazyathome.player

interface IPlayable {
    val title: String
    suspend fun resolveUrl(): String
}