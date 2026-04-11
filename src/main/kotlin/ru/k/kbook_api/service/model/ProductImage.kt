package ru.k.kbook_api.service.model

data class ProductImage(
    val id: Long? = null,
    val productId: Long? = null,
    val url: String? = null,
    val image: ByteArray? = null,
    val contentType: ContentType
)
