package ru.k.kbook_api.repository.entity.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "product_images")
data class ProductImageDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "product_id", insertable = false, updatable = false)
    val productId: Long? = null,

    val url: String? = null,

    @Lob
    @Column(name = "image", columnDefinition="BLOB")
    val image: ByteArray? = null,

    @Enumerated(EnumType.STRING)
    val contentType: ContentTypeDbo
)
