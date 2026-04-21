package ru.k.kbook_api.repository.entity.dish

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
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.k.kbook_api.repository.entity.product.ProductDbo
import java.time.LocalDateTime

@Entity
@Table(name = "dishes")
class DishDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 255)
    @field:Size(min = 2, message = "Название должно быть минимум 2 символа")
    @field:NotBlank
    var name: String,

    @OneToMany(mappedBy = "dish", cascade = [CascadeType.ALL], 
               orphanRemoval = true, fetch = FetchType.EAGER)
    var images: MutableSet<DishImageDbo> = mutableSetOf(),

    @Column(nullable = false)
    @field:DecimalMin("0.0")
    var caloricity: Double,

    @Column(nullable = false)
    @field:DecimalMin("0.0")
    var protein: Double,

    @Column(nullable = false)
    @field:DecimalMin("0.0")
    var fat: Double,

    @Column(nullable = false)
    @field:DecimalMin("0.0")
    var carb: Double,

    @OneToMany(mappedBy = "dish", cascade = [CascadeType.ALL], 
               orphanRemoval = true, fetch = FetchType.EAGER)
    var products: MutableList<DishProductDbo> = mutableListOf(),

    @Column(nullable = false)
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Размер порции должен быть больше 0")
    var portionSize: Double,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @field:NotNull
    var category: DishCategoryDbo,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_flags", joinColumns = [JoinColumn(name = "dish_id")])
    @Enumerated(EnumType.STRING)
    var flags: MutableSet<DishFlagDbo> = linkedSetOf(),

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null,
)
