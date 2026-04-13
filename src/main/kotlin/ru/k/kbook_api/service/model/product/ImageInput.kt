package ru.k.kbook_api.service.model.product

data class ImageInput(
    val url: String? = null,
    val image: ByteArray? = null,
    val contentType: ContentType
)
