package ru.k.kbook_api.repository.entity.dish

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
import ru.k.kbook_api.repository.entity.product.ContentTypeDbo

@Entity
@Table(name = "dish_images")
class DishImageDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    var dish: DishDbo,

    var url: String? = null,

    @Lob
    @Column(name = "image", columnDefinition = "BLOB")
    var image: ByteArray? = null,

    @Enumerated(EnumType.STRING)
    var contentType: ContentTypeDbo = ContentTypeDbo.URL,
)