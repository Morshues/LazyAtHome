package com.morshues.lazyathome.player

data class StaticPlayableItem(
    val url: String,
    override val title: String?
) : IPlayable {
    override suspend fun resolveUrl(): String = url
}