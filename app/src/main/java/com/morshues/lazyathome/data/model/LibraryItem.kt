package com.morshues.lazyathome.data.model

import com.morshues.lazyathome.data.network.UrlProvider

sealed class LibraryItem {
    abstract val name: String
    abstract val type: String

    data class FolderItem(
        override val name: String,
        override val type: String,
        val children: List<LibraryItem>
    ) : LibraryItem()

    data class VideoItem(
        override val name: String,
        override val type: String,
        val path: String,
        val thumbnail: String
    ) : LibraryItem() {
        val url: String
            get() = UrlProvider.baseUrl + "library/" + path
        val src: String
            get() = UrlProvider.baseUrl + "library/" + thumbnail
    }
}