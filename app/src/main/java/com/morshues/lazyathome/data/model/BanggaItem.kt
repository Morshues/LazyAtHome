package com.morshues.lazyathome.data.model

import com.google.gson.annotations.SerializedName

interface BanggaDisplayable {
    val id: String
    val title: String
    val info: String
    val image: BanggaImage
}

data class BanggaImage(
    val url: String,
)

data class BanggaCategoryItem(
    override val id: String,
    override val title: String,
    override val info: String,
    override val image: BanggaImage,
) : BanggaDisplayable

data class BanggaAnimationItem(
    @SerializedName("content")
    val episodes: List<BanggaEpisode>
)

data class BanggaEpisode(
    @SerializedName("contentId")
    override val id: String,
    @SerializedName("contentTitle")
    override val title: String,
    @SerializedName("contentInfo")
    override val info: String,
    @SerializedName("coverimage")
    override val image: BanggaImage
) : BanggaDisplayable

data class BanggaVideoItem(
    @SerializedName("contentId")
    val id: String,
    val video: BanggaSubVideoItem,
)

data class BanggaSubVideoItem(
    val url: String,
)