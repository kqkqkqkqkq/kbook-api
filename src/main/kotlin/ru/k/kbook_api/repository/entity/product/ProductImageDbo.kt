package ru.k.kbook_api.repository.entity.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "product_images")
data class ProductImageDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: ProductDbo? = null,

    val url: String? = null,

    @Lob
    @Column(name = "image", columnDefinition="BLOB")
    val image: ByteArray? = null,

    @Enumerated(EnumType.STRING)
    val contentType: ContentTypeDbo
)
