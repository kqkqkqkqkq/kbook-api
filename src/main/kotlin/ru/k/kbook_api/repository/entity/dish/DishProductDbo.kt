package ru.k.kbook_api.repository.entity.dish

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMin
import ru.k.kbook_api.repository.entity.product.ProductDbo

@Entity
@Table(name = "dish_products")
class DishProductDbo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    var dish: DishDbo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductDbo,

    @Column(nullable = false)
    @field:DecimalMin(value = "0.0", inclusive = false)
    var quantity: Double,
)
