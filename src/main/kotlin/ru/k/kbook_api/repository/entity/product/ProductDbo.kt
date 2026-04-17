package ru.k.kbook_api.repository.entity.product

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class ProductDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    @Size(min = 2, message = "Название должно быть минимум 2 символа")
    @NotBlank
    val name: String,

    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val images: MutableList<ProductImageDbo> = mutableListOf(),

    @Column(nullable = false)
    @Min(0)
    val caloricity: Double,

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val protein: Double,

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val fat: Double,

    @Column(nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    val carb: Double,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    val category: ProductCategoryDbo,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    val cookingRequired: CookingRequiredDbo,

    @Column(name = "flag")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_flags", joinColumns = [JoinColumn(name = "product_id")])
    @Enumerated(EnumType.STRING)
    val flags: MutableSet<ProductFlagDbo> = mutableSetOf(),

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null,
)
