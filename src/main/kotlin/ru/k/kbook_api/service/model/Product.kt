package ru.k.kbook_api.service.model

import kotlinx.datetime.LocalDateTime

data class Product(
    val id: Long? = null,
    val name: String,
    val images: List<ProductImage> = emptyList(),
    val caloricity: Double,
    val protein: Double,
    val fat: Double,
    val carb: Double,
    val description: String? = null,
    val category: ProductCategory,
    val cookingRequired: CookingRequired,
    val flags: Set<ProductFlag> = emptySet(),
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    val totalNutrition: Double
        get() = protein + fat + carb

    val isNutritionValid: Boolean
        get() = protein in 0.0..100.0 &&
                fat in 0.0..100.0 &&
                carb in 0.0..100.0 &&
                totalNutrition <= 100.0

    fun hasFlag(flag: ProductFlag): Boolean = flag in flags

    val isVegan: Boolean
        get() = hasFlag(ProductFlag.VEGAN)

    val isGlutenFree: Boolean
        get() = hasFlag(ProductFlag.GLUTEN_FREE)

    val isSugarFree: Boolean
        get() = hasFlag(ProductFlag.SUGAR_FREE)
}
