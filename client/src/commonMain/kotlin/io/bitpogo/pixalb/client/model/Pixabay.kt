package io.bitpogo.pixalb.client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PixabayResponse(
    @SerialName("totalHits")
    val total: Int,
    val items: List<PixabayItem>,
)

@Serializable
data class PixabayItem(
    val id: Long,
    val user: String,
    val tags: List<String>,
    val downloads: ULong,
    val likes: ULong,
    val comments: ULong,
    @SerialName("previewURL")
    val preview: String,
    @SerialName("webformatURL")
    val large: String
)
