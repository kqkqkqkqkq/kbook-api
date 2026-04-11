package ru.k.kbook_api.repository.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlinx.datetime.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    @Size(min = 2, message = "Название должно быть минимум 2 символа")
    @NotBlank
    val name: String,

    @OneToMany(mappedBy = "productId", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val images: MutableList<ProductImage> = mutableListOf(),

    @Column(nullable = false)
    @Min(0)
    val caloricity: Double,  // ккал/100г

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val protein: Double,     // г/100г

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val fat: Double,         // г/100г

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val carb: Double,        // г/100г

    @Column(name = "composition", columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    val category: ProductCategory,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    val cookingRequired: CookingRequired,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_flags", joinColumns = [JoinColumn(name = "product_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "flag")
    val flags: MutableSet<ProductFlag> = mutableSetOf(),

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null
)

enum class ProductCategory {
    FROZEN,     // Замороженный
    MEAT,       // Мясной
    VEGETABLES, // Овощи
    GREENS,     // Зелень
    SPICES,     // Специи
    CEREALS,    // Крупы
    CANNED,     // Консервы
    LIQUID,     // Жидкость
    SWEETS      // Сладости
}

enum class CookingRequired {
    READY_TO_EAT,      // Готовый к употреблению
    SEMI_FINISHED,     // Полуфабрикат
    REQUIRES_COOKING   // Требует приготовления
}

enum class ProductFlag {
    VEGAN,         // Веган
    GLUTEN_FREE,   // Без глютена
    SUGAR_FREE     // Без сахара
}

@Entity
@Table(name = "product_images")
data class ProductImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "product_id", insertable = false, updatable = false)
    val productId: Long? = null,

    val url: String? = null,
//    @Lob
    val image: ByteArray? = null,
    @Enumerated(EnumType.STRING) val contentType: ContentType
)

enum class ContentType {
    IMAGE,  // файл изображения (ByteArray)
    URL     // ссылка (String)
}